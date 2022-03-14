package fr.cjpapps.gumsski;

import java.util.ArrayList;

public class Variables {

    // Global variable used to store network state
    public static boolean isNetworkConnected = false;
    public static boolean monitoringNetwork = false;

    // statut de networkCallback
    public static boolean isRegistered = false;

    // url du backend
    public static String urlActive = "";

    // requête de localisation en cours
    public static boolean requestingLocationUpdates = false;

    //texte saisi pour compléter le SMS au 114
    public static String texteSMSpart1 = "";
    public static String texteSMSpart2 = "";

    // retours de connexion
    public static String errMsg = "";
    public static String errCode = "";

    // liste participants déjà chargée
    public static boolean isListeAvailable = false;

}