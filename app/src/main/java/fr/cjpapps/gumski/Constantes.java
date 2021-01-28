package fr.cjpapps.gumski;

public class Constantes {

    final static int MODIF_REQUEST = 1;
    final static int SUPPR_REQUEST = 2;
    final static int CREATE_REQUEST = 3;
    final static int AUTH_ACTIV = 4;
    final static int AUTH_CHANGE = 5;

/* pour caractériser les points d'accès sur le serveur joomla
    (ne pas toucher à users et login)
  ****************************************************************/
    final static String JOOMLA_USERS = "users";
    final static String JOOMLA_RESOURCE_LOGIN = "login";
// *****************************************************************
// définir ci-dessous les ressources à utiliser par l'appli (plugins de com_api)
    final static String JOOMLA_APP = "gski";
    final static String JOOMLA_RESOURCE_1 = "paramsortie";
    final static String JOOMLA_RESOURCE_2 = "inscrits";


    private Constantes(){}

}
