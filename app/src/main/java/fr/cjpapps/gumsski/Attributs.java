package fr.cjpapps.gumsski;

public enum Attributs {

/*  Les champs doivent être exactement ceux qui figurent comme clés dans le Json de description d'un item.
*   c'est-à-dire les noms des champs dans la base de données de Joomla sur laquelle on travaille
*   Voir l'usage dans AuxReseau.decodeInfosItem(). Nom des champs et hint sont utilisés dans les formulaires de CreateItem
*   et ModifItem.*/

    ATTR01("id", "Id sortie", "123"),
    ATTR02("hotelchauffeurs", "Hotel des chauffeurs", ""),
    ATTR03("tphchauffeurs", "Téléphone Chauffeurs", "+33612345678"),
    ATTR04("dinerretour", "Dîner au retour", "Resto de la Plage, Chamonix"),
    ATTR05("deposes", "Lieux de déposes", ""),
    ATTR06("reprises", "Lieux et heures de reprise", ""),
    ATTR07("coursesprevues", "Courses prévues", ""),
    ATTR08("meteo", "Météo", ""),
    ATTR09("secours", "Secours", "112");

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
