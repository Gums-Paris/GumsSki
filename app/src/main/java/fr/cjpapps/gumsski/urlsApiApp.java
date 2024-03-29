package fr.cjpapps.gumsski;

public enum urlsApiApp {

    API_LOCAL("http://10.0.2.2:8081/index.php?option=com_api&"),
    API_GUMS_v2("https://v2.gumsparis.asso.fr/index.php?option=com_api&"),
    API_GUMS("https://www.gumsparis.asso.fr/index.php?option=com_api&"),
    API_GUMS_v3("https://v3.gumsparis.asso.fr/index.php?option=com_api&"),
    API_GUMS_v4("https://v4.gumsparis.asso.fr/index.php?option=com_api&");

    private String url = "";

    urlsApiApp(String url) {
        this.url = url;
    }

    public String getUrl(){
        return url;
    }

}
