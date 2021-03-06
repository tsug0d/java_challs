package rmi;

import java.io.IOException;

public class Player implements java.io.Serializable
{
    // change the serialVersionUID based on the error return
    private static final long serialVersionUID = 5558077863197230219L;
    private String name;
    private boolean isAdmin;
    private String logCommand = "echo \"ADMIN LOGGED IN\" > /tmp/log.txt";

    public String getName()
    {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isAdmin() {
        return this.isAdmin;
    }

    public void setAdmin(boolean admin) {
        this.isAdmin = admin;
    }

    public String toString()
    {
        if (isAdmin()) {
            try {
                Runtime.getRuntime().exec(this.logCommand);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "ADMIN LOGGED IN";
        }
        return "USER LOGGED IN";
    }
}