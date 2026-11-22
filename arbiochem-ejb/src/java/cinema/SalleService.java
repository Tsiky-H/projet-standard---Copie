package cinema;

import java.sql.Connection;
import java.util.Map;
import java.util.TreeMap;
import user.UserEJB;
import utilitaire.UtilDB;

/** Enregistre une salle et le plan de places fourni à sa création. */
public final class SalleService {

    private SalleService() { }

    public static Salle creerAvecPlaces(Salle salle, Map<String, Integer> placesParCategorie,
                                        UserEJB utilisateur) throws Exception {
        if (salle == null) throw new IllegalArgumentException("La salle est obligatoire.");
        if (placesParCategorie == null || placesParCategorie.isEmpty()) {
            throw new IllegalArgumentException("Indiquez le nombre de places par catégorie.");
        }
        if (utilisateur == null) throw new IllegalArgumentException("La session utilisateur a expiré.");

        int total = 0;
        for (Map.Entry<String, Integer> entree : placesParCategorie.entrySet()) {
            if (entree.getKey() == null || entree.getKey().trim().isEmpty()
                    || entree.getValue() == null || entree.getValue() < 0) {
                throw new IllegalArgumentException("Les nombres de places doivent être des entiers positifs ou nuls.");
            }
            total += entree.getValue();
        }
        if (total < 1) throw new IllegalArgumentException("La salle doit contenir au moins une place.");

        Connection c = null;
        try {
            c = new UtilDB().GetConn();
            c.setAutoCommit(false);
            utilisateur.createObject(salle, c);

            int indexRangee = 0;
            for (Map.Entry<String, Integer> entree : new TreeMap<String, Integer>(placesParCategorie).entrySet()) {
                String rangee = codeRangee(indexRangee++);
                for (int numero = 1; numero <= entree.getValue(); numero++) {
                    Place place = new Place();
                    place.setIdSalle(salle.getId());
                    place.setIdCategorie(entree.getKey());
                    place.setNumero(rangee + String.format("%03d", numero));
                    utilisateur.createObject(place, c);
                }
            }
            c.commit();
            return salle;
        } catch (Exception e) {
            if (c != null) c.rollback();
            throw e;
        } finally {
            if (c != null) c.close();
        }
    }

    private static String codeRangee(int index) {
        StringBuilder code = new StringBuilder();
        int valeur = index + 1;
        while (valeur > 0) {
            valeur--;
            code.insert(0, (char) ('A' + valeur % 26));
            valeur /= 26;
        }
        return code.toString();
    }
}
