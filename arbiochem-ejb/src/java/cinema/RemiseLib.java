package cinema;

import cinema.Remise;

/** Modèle de lecture de la vue CINE_REMISE_LIB. */
public class RemiseLib extends Remise {
    private String catReserveLib;
    private String catRemiseLib;

    public RemiseLib() { setNomTable("CINE_REMISE_LIB"); }

    public String getCatReserveLib() { return catReserveLib; }
    public void setCatReserveLib(String catReserveLib) { this.catReserveLib = catReserveLib; }
    public String getCatRemiseLib() { return catRemiseLib; }
    public void setCatRemiseLib(String catRemiseLib) { this.catRemiseLib = catRemiseLib; }
}
