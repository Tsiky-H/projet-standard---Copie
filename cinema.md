# Objectif: gerer un cinema

## Elements de base:
**Film**:id,titre,realisateur,duree(minutes),genre,classification(age)
**Salle**: id,libelle. Le nombre de places est calcule a partir de `CINE_PLACE`.
**Place**:id,#idsalle,#idcategorie,numero
**CategoriePlace**:id,val,prix,rangee?
**Seance**:id,#idfilm,#idsalle,dateheuredebut,dateheurefin
**Reservation**:id,#idclient,#idseance,datereservation,dateexpiration,nbplaces,modepaiement,statut 
**remise**: id,cat_reserve,nb_places,cat_remise,val

## A savoir
Chaque action est enregistree dans une facture et liee a la caisse

Chaque date dans le projet doit fournir l'heure exacte

## Actions qu'on peut faire:
-les CRUD de base pour chaque entite

### pour la resrvation:
-creer une reservation ne genere pas encore de facture, la facture est cree apres validation de la reservation; donc apres paiement

-une reservation ne peut se faire si la seance choisie a deja commencee

-les places choisies dans une reservation meme non validee ne doivent plus etre disponibles pour une nouvelle reservation 

-pas d'accompte exige, paiement toujours total a la validation

-reservation peut etre annulee a tout moment tant que la seance n'a pas commence et que la date d'expiration n'est pas atteinte

-si aucune validation avant date expiration de la reservation, elle est annulee automatiquement

-annulation d'une reservation libere les places choisies immediatement

-les lignes de places d'une reservation annulee ou expiree sont conservees pour l'historique; elles ne bloquent plus les places

-reservation deja validee ne peut etre annulee

-l'expiration d'une reservation est obligatoire et soit saisie a la creation de la reservation, soit correspond au debut de la seance reservee

## achat directe au lieu de reservation possible
-les meme regles des places sont appliquees a l'achat direct et a la reservation

-on ne peut annuler un achat direct

-Achat direct toujours possible tant que la seance n'est pas termine et que des places sont encores disponibles

-l'achat direct utilisera la meme table que reservation mais avec une date expiration null

-l'achat direct genere aussi une facture

## remise
Exemple: 4 places achetees pour la categorie standard donne une remise de 50% pour la categorie premium

-la remise est toujours configurable

-la remise n'est appliquee qu'a une seule place de la categorie remise; achat de 5 places != 50%remise x 5

-la remise n'est pas cumulable; si il faut 4 places achetees pour une remise, l'achat de 100places ne donne pas plusieurs remises

**NB**: apart les tables propres au cinema, les autres tables a utiliser sont deja presentes dans ce projet, je veux implementer mon projet cinema sur celui-ci


### ajouter base cinema a la base du projet:
Depuis la racine du projet, copiez le fichier dans le conteneur :
`docker cp cinema.sql oracle:/tmp/cinema.sql`

Connectez-vous ensuite à Oracle. Cette commande vous demandera le mot de passe sans l’inscrire dans la commande :
`docker exec -it oracle sqlplus -L akoho/akoho@//localhost:1521/EE.oralce.docker`

Dans SQL*Plus, vérifiez d’abord que vous êtes dans le bon schéma et que les tables cinéma n’existent pas déjà :

```
SELECT USER FROM DUAL;
DESC CLIENT;
SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME LIKE 'CINE%';
```

Si le schéma est le bon et qu’aucune table CINE_… n’apparaît, lancez le script : `@/tmp/cinema.sql`

À la fin, le script affiche les objets CINE invalides éventuels. Vérifiez aussi les erreurs Oracle avec :
SELECT NAME, TYPE, LINE, POSITION, TEXT FROM USER_ERRORS WHERE NAME LIKE '%CINE%' ORDER BY NAME, SEQUENCE;

modification en base:
docker cp cinema-renommage-colonnes.sql oracle:/tmp/cinema-renommage-colonnes.sql
docker exec -it oracle sqlplus akoho/akoho@//localhost:1521/EE @/tmp/cinema-renommage-colonnes.sql