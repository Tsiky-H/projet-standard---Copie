-- =====================================================================
-- RENOMMAGE DES COLONNES DE CINE_RESERVATION_FILLE
--   PRIX_UNITAIRE  -> PRIXUNITAIRE
--   MONTANT_REMISE -> MONTANTREMISE
-- Raison : APJ ne garde, pour l'INSERT, que les attributs Java dont le nom est
-- identique a une colonne (casse ignoree). Les attributs prixUnitaire et
-- montantRemise ne correspondaient pas aux colonnes avec tiret bas.
-- Fichier sans accents. A executer avec l'utilisateur proprietaire du schema.
-- =====================================================================
SET SQLBLANKLINES ON
SET DEFINE OFF
SET SERVEROUTPUT ON

-- ---------------------------------------------------------------------
-- 1. L'index fonctionnel cite MONTANT_REMISE : le supprimer avant le renommage
-- ---------------------------------------------------------------------
DROP INDEX UK_CINE_RF_UNE_REMISE;

-- ---------------------------------------------------------------------
-- 2. Renommage (les contraintes CHECK suivent automatiquement)
-- ---------------------------------------------------------------------
ALTER TABLE CINE_RESERVATION_FILLE RENAME COLUMN PRIX_UNITAIRE TO PRIXUNITAIRE;
ALTER TABLE CINE_RESERVATION_FILLE RENAME COLUMN MONTANT_REMISE TO MONTANTREMISE;

-- ---------------------------------------------------------------------
-- 3. Recreer l'index : une seule place remisee par reservation
-- ---------------------------------------------------------------------
CREATE UNIQUE INDEX UK_CINE_RF_UNE_REMISE ON CINE_RESERVATION_FILLE (
    CASE WHEN MONTANTREMISE > 0 THEN IDRESERVATION END
);

-- ---------------------------------------------------------------------
-- 4. Vues qui citent les anciennes colonnes
-- ---------------------------------------------------------------------
CREATE OR REPLACE VIEW CINE_RESERVATION_FILLE_LIB AS
SELECT f.ID, f.IDRESERVATION, f.IDSEANCE, f.IDPLACE, f.PRIXUNITAIRE, f.MONTANTREMISE, f.ACTIF,
    p.NUMERO AS IDPLACELIB,
    c.VAL    AS IDCATEGORIELIB,
    f.PRIXUNITAIRE - f.MONTANTREMISE AS MONTANTNET
FROM CINE_RESERVATION_FILLE f
JOIN CINE_PLACE p ON p.ID = f.IDPLACE
JOIN CINE_CATEGORIE_PLACE c ON c.ID = p.IDCATEGORIE;

-- Les lignes restent presentes apres annulation : historique et montants conserves.
CREATE OR REPLACE VIEW CINE_RESERVATION_LIB AS
SELECT r.ID, r.IDCLIENT, r.IDSEANCE, r.DATERESERVATION, r.DATEEXPIRATION, r.ETAT,
    c.NOM AS IDCLIENTLIB,
    se.LIBELLE AS IDSEANCELIB,
    se.IDFILMLIB AS FILMLIB,
    se.IDSALLELIB AS SALLELIB,
    se.DATEHEUREDEBUT AS SEANCEDEBUT,
    se.DATEHEUREFIN   AS SEANCEFIN,
    NVL(t.NB, 0)     AS NBPLACES,
    NVL(t.BRUT, 0)   AS MONTANTBRUT,
    NVL(t.REMISE, 0) AS MONTANTREMISE,
    NVL(t.BRUT, 0) - NVL(t.REMISE, 0) AS MONTANTTOTAL,
    CASE WHEN r.DATEEXPIRATION IS NULL THEN 'ACHAT DIRECT' ELSE 'RESERVATION' END AS TYPELIB
FROM CINE_RESERVATION r
JOIN CLIENT c ON c.ID = r.IDCLIENT
JOIN CINE_SEANCE_LIB se ON se.ID = r.IDSEANCE
LEFT JOIN (SELECT IDRESERVATION,
                COUNT(*) AS NB,
                SUM(PRIXUNITAIRE) AS BRUT,
                SUM(MONTANTREMISE) AS REMISE
        FROM CINE_RESERVATION_FILLE
        GROUP BY IDRESERVATION) t
    ON t.IDRESERVATION = r.ID;

-- ---------------------------------------------------------------------
-- 5. Trigger de ligne : il ne recopie plus le prix depuis la categorie.
--    Si le service n'envoie pas PRIXUNITAIRE, l'insertion echoue (NOT NULL)
--    au lieu de masquer l'erreur.
-- ---------------------------------------------------------------------
CREATE OR REPLACE TRIGGER TRG_CINE_RES_FILLE_BI
BEFORE INSERT ON CINE_RESERVATION_FILLE
FOR EACH ROW
DECLARE
    v_etat         CINE_RESERVATION.ETAT%TYPE;
    v_seance       CINE_RESERVATION.IDSEANCE%TYPE;
    v_salle_seance CINE_SEANCE.IDSALLE%TYPE;
    v_salle_place  CINE_PLACE.IDSALLE%TYPE;
BEGIN
    SELECT ETAT, IDSEANCE INTO v_etat, v_seance
    FROM CINE_RESERVATION WHERE ID = :NEW.IDRESERVATION;
    IF v_etat <> 1 THEN
        RAISE_APPLICATION_ERROR(-20130,
            'Des places ne peuvent etre ajoutees qu''a une reservation en attente.');
    END IF;
    IF :NEW.IDSEANCE IS NULL THEN
        :NEW.IDSEANCE := v_seance;
    END IF;
    SELECT IDSALLE INTO v_salle_seance FROM CINE_SEANCE WHERE ID = :NEW.IDSEANCE;
    SELECT IDSALLE INTO v_salle_place FROM CINE_PLACE WHERE ID = :NEW.IDPLACE;
    IF v_salle_place <> v_salle_seance THEN
        RAISE_APPLICATION_ERROR(-20131, 'Cette place n''appartient pas a la salle de la seance.');
    END IF;
EXCEPTION
    WHEN NO_DATA_FOUND THEN
        RAISE_APPLICATION_ERROR(-20132, 'Reservation, seance ou place introuvable.');
END;
/

-- ---------------------------------------------------------------------
-- 6. VERIFICATION
-- ---------------------------------------------------------------------
-- 6.1 doit renvoyer 0 ligne
SELECT OBJECT_NAME, OBJECT_TYPE, STATUS
FROM USER_OBJECTS
WHERE OBJECT_NAME LIKE '%CINE%' AND STATUS <> 'VALID';

-- 6.2 colonnes finales de la table
SELECT COLUMN_NAME, DATA_TYPE
FROM USER_TAB_COLUMNS
WHERE TABLE_NAME = 'CINE_RESERVATION_FILLE'
ORDER BY COLUMN_ID;

-- ---------------------------------------------------------------------
-- 7. ANNULATION (a decommenter pour revenir en arriere ; refaire ensuite
--    les etapes 1, 3, 4 et 5 avec les anciens noms de colonnes)
-- ---------------------------------------------------------------------
-- DROP INDEX UK_CINE_RF_UNE_REMISE;
-- ALTER TABLE CINE_RESERVATION_FILLE RENAME COLUMN PRIXUNITAIRE TO PRIX_UNITAIRE;
-- ALTER TABLE CINE_RESERVATION_FILLE RENAME COLUMN MONTANTREMISE TO MONTANT_REMISE;