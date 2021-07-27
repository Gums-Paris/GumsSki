package fr.cjpapps.gumsski;

import java.util.ArrayList;
import java.util.Arrays;

public class Constantes {

// les id des admins autorisés à modifier le contenu des logistiques
    final static ArrayList<String> listeAdmins = new ArrayList<>(Arrays.asList("62", "66"));

/* pour caractériser les points d'accès sur le serveur joomla
    (ne pas toucher à users et login)
  ****************************************************************/
    final static String JOOMLA_USERS = "users";
    final static String JOOMLA_RESOURCE_LOGIN = "login";
// *****************************************************************
// définir ci-dessous les ressources à utiliser par l'appli (plugins de com_api)
    final static String JOOMLA_APP = "gski";
    final static String JOOMLA_RESOURCE_1 = "logistique";
    final static String JOOMLA_RESOURCE_2 = "inscrits";
    final static String JOOMLA_RESOURCE_3 = "listesorties";


    private Constantes(){}

}
