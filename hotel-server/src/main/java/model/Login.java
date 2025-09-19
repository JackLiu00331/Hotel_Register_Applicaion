package model;

public class Login {
    private String l_Id;
    private String l_pwd;

    public Login(String l_Id, String l_pwd) {
        this.l_Id = l_Id;
        this.l_pwd = l_pwd;
    }

    public String getL_Id() {
        return l_Id;
    }

    public void setL_Id(String l_Id) {
        this.l_Id = l_Id;
    }

    public String getL_pwd() {
        return l_pwd;
    }

    public void setL_pwd(String l_pwd) {
        this.l_pwd = l_pwd;
    }
}
