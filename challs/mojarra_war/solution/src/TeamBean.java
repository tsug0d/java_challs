//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//
package ascis;

import java.io.Serializable;
import java.util.ArrayList;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;


public class TeamBean implements Serializable {
    private static final long serialVersionUID = -1333713373713373737L;
    private int team_id;
    private String team_name;
    private String team_country;
    private boolean team_secret_status = false;
    private String team_secret_message;
    private String template = "var message = \"SECRET MESSAGE WAS LEAKED!  \";";
    public ArrayList teamsListFromDB;

    public TeamBean() {
    }

    public int getTeam_id() {
        return this.team_id;
    }

    public void setTeam_id(int team_id) {
        this.team_id = team_id;
    }

    public String getTeam_name() {
        return this.team_name;
    }

    public void setTeam_name(String team_name) {
        this.team_name = team_name;
    }

    public String getTeam_country() {
        return this.team_country;
    }

    public void setTeam_country(String team_country) {
        this.team_country = team_country;
    }

    public Boolean getTeam_secret_status() {
        return this.team_secret_status;
    }

    public void setTeam_secret_status(Boolean team_secret_status) {
        this.team_secret_status = team_secret_status;
    }

    public String getTeam_secret_message() {
        return this.team_secret_message;
    }

    public void setTeam_secret_message(String team_secret_message) {
        this.team_secret_message = team_secret_message;
    }

    public String toString() {
        if (this.getTeam_secret_status()) {
            try {
                ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
                ScriptEngine scriptEngine = scriptEngineManager.getEngineByName("JavaScript");
                scriptEngine.eval(this.template);
                return (String)scriptEngine.get("message") + this.getTeamSecretMessage(this.team_id);
            } catch (ScriptException var3) {
                var3.printStackTrace();
                return "";
            }
        } else {
            return "SECRET MESSAGE STILL FINE !";
        }
    }

    public String getTeamSecretMessage(int teamID) {
        TeamBean team = new TeamBean();
        return team != null ? team.getTeam_secret_message() : "";
    }
}
