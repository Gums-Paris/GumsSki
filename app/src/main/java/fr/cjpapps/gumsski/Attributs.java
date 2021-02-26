package fr.cjpapps.gumsski;

public enum Attributs {

/*  Les champs doivent être exactement ceux qui figurent comme clés dans le Json de description d'un item.
*   c'est-à-dire les noms des champs dans la base de données de Joomla sur laquelle on travaille
*   Voir l'usage dans GetInfosItem.java. Nom des champs et hint sont utilisés dans les formulaires de CreateItem
*   et ModifItem.*/

// en vrai ceci ne sert pas parce qu'on édite pasces paramètres on a pas besoin d'un objet. on s'en servira quand on aur
// la logistique
    ATTR01("date_bdh", "Dates sortie", "Sam 1 -Dim 2 mai 2021"),
    ATTR02("id", "Id sortie", "162"),
    ATTR03("titre", "Titre", "Névache"),
    ATTR04("date", "Date début", "2021-05-01"),
    ATTR05("jours", "Durée", "2"),
    ATTR06("publier_groupes", "Groupes", "2"),
    ATTR07("responsable", "Responsable", "Pierre TRUC");

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
