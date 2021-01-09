package fr.cjpapps.gumski;

public enum Attributs {

/*  Les champs doivent être exactement ceux qui figurent comme clés dans le Json de description d'un item.
*   c'est-à-dire les noms des champs dans la base de données de Joomla sur laquelle on travaille
*   Voir l'usage dans GetInfosItem.java. Nom des champs et hint sont utilisés dans les formulaires de CreateItem
*   et ModifItem.*/

    ATTR01("nomlieu", "Nom du Lieu", "Nom"),
    ATTR02("parking", "Parking", "Itinéraire vers parking"),
    ATTR03("latpk", "Latitude P", "48.632"),
    ATTR04("lonpk", "Longitude P", "2.457"),
    ATTR05("rendezvous", "Rendez-vous", "Itinéraire vers rendez-vous"),
    ATTR06("latrv", "Latitude RdV", "48.643"),
    ATTR07("lonrv", "Longitude RdV", "2.468");

    private String champ = "";
    private String nomChamp = "";
    private String hint = "";

    Attributs(String champ, String nomChamp, String hint) {
        this.champ = champ;
        this.nomChamp = nomChamp;
        this.hint = hint;
    }

    public String getChamp() { return champ; }
    public String getNomChamp() { return nomChamp;}
    public String getHint() { return hint;}

}
