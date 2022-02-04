package fr.cjpapps.gumsski;

import java.util.ArrayList;

public class Variables {

    // Global variable used to store network state
    public static boolean isNetworkConnected = false;

    // url du backend
    public static String urlActive = "";

    // requête de localisation en cours
    public static boolean requestingLocationUpdates = false;

    // retours de connexion
    public static String errMsg = "";
    public static String errCode = "";

    // liste participants déjà chargée
    public static boolean isListeAvailable = false;

}