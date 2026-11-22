package cinema;

import cinema.Salle;

/** Modèle de lecture de la vue CINE_SALLE_LIB. */
public class SalleLib extends Salle {
    private int capacite;

    public SalleLib() { setNomTable("CINE_SALLE_LIB"); }

    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
}
