package scouter;

public class Alliance {
    private int score;
    private String [] surrogate_team_keys;
    private String [] team_keys;

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String[] getSurrogate_team_keys() {
        return surrogate_team_keys;
    }

    public void setSurrogate_team_keys(String[] surrogate_team_keys) {
        this.surrogate_team_keys = surrogate_team_keys;
    }

    public String[] getTeam_keys() {
        return team_keys;
    }

    public void setTeam_keys(String[] team_keys) {
        this.team_keys = team_keys;
    }
}
