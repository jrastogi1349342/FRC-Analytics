package scouter;

public class Match implements Comparable<Match> {
    private String comp_level;
    private String key;
    private int match_number;
    private int time;
    private Alliances alliances;
    private ScoreBreakdown score_breakdown;

    public String getComp_level() {
        return comp_level;
    }

    public void setComp_level(String comp_level) {
        this.comp_level = comp_level;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getMatch_number() {
        return match_number;
    }

    public void setMatch_number(int match_number) {
        this.match_number = match_number;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public Alliances getAlliances() {
        return alliances;
    }

    public void setAlliances(Alliances alliances) {
        this.alliances = alliances;
    }

    public ScoreBreakdown getScore_breakdown() {
        return score_breakdown;
    }

    public void setScore_breakdown(ScoreBreakdown score_breakdown) {
        this.score_breakdown = score_breakdown;
    }

    @Override
    public int compareTo(Match o) {
        return Integer.compare(time, o.getTime());
    }

    @Override
    public String toString() {
        return "Match{" +
                "key='" + key + '\'' +
                ", match_number=" + match_number +
                '}';
    }
}
