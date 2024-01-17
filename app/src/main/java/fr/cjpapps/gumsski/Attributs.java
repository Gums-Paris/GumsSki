package fr.cjpapps.gumsski;

public enum Attributs {

/*  Les champs doivent être exactement ceux qui figurent comme clés dans le Json de description d'un item.
*   c'est-à-dire les noms des champs dans la base de données de Joomla sur laquelle on travaille
*   Voir l'usage dans AuxReseau.decodeInfosItem(). Nom des champs et hint sont utilisés dans les formulaires de CreateItem
*   et ModifItem.*/

    ATTR01("id", "Id logistique", "123"),
    ATTR02("rdv_depart", "Rendez-vous départ", "Denfert"),
    ATTR03("hotelchauffeurs", "Hotel des chauffeurs", ""),
    ATTR04("tphchauffeurs", "Téléphone Chauffeurs", "+33612345678"),
    ATTR05("dinerretour", "Dîner au retour", "Resto de la Plage, Chamonix"),
    ATTR06("deposes", "Lieux de déposes", ""),
    ATTR07("reprises", "Lieux et heures de reprise", ""),
    ATTR08("coursesprevues", "Courses prévues", ""),
    ATTR09("meteo", "Météo", ""),
    ATTR10("secours", "Secours", "112"),
    ATTR11("checked_out", "checked_out", "" ),
    ATTR12("checked_out_time","checked_out_time", ""),
    ATTR13("verrou", "verrou", ""),
    ATTR14("canedit", "canedit", "");


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
