package fr.cjpapps.gumski;

public class MembreGroupe {

    String name;
    String tel;
    String email;
    String autonome;
    String peage;

    MembreGroupe() {}

    void setName(String mName) {this.name = mName;}
    void setTel(String mTel) {this.tel = mTel;}
    void setEmail(String mEmail) {this.email = mEmail;}
    void setAutonome(String mAutonome) {this.autonome = mAutonome;}
    void setPeage(String mPeage) {this.peage = mPeage;}

    String getName() {return name;}
    String getTel() {return tel;}
    String getEmail() {return email;}
    String getAutonome() {return autonome;}
    String getPeage() {return peage;}
}
