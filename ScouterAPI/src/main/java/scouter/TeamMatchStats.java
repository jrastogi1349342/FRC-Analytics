package scouter;

public class TeamMatchStats {
	private int team_number;
	private int matchNum;
	private String eventKey;
    private boolean bridgeDockedAuto;
    private boolean bridgeEngagedAuto;
    private boolean bridgeDockedTeleOp;
    private boolean bridgeEngagedTeleOp;
    private boolean didMobility;
    private double calcTotalMatchPts;
    private double calcTotalMatchPtsSD;
    private double calcTeleGPCount;
    private double calcTeleGPCountSD;
    private double calcTeleGPPts;
    private double calcTeleGPPtsSD;
    private String comments;

    public TeamMatchStats() {
        this.bridgeDockedAuto = false;
        this.bridgeEngagedAuto = false;
        this.bridgeDockedTeleOp = false;
        this.bridgeEngagedTeleOp = false;
        this.didMobility = false;
    }
    
    public int getTeamNumber() {
    	return team_number;
    }
    
    public void setTeamNumber(int team_number) {
    	this.team_number = team_number;
    }
    
    public int getMatchNum() {
    	return matchNum;
    }
    
    public void setMatchNum(int matchNum) {
    	this.matchNum = matchNum;
    }
    
    public String getEventKey() {
    	return eventKey;
    }
    
    public void setEventKey(String eventKey) {
    	this.eventKey = eventKey;
    }

    public boolean isBridgeDockedAuto() {
        return bridgeDockedAuto;
    }

    public void setBridgeDockedAuto(boolean bridgeDockedAuto) {
        this.bridgeDockedAuto = bridgeDockedAuto;
    }

    public boolean isBridgeEngagedAuto() {
        return bridgeEngagedAuto;
    }

    public void setBridgeEngagedAuto(boolean bridgeEngagedAuto) {
        this.bridgeEngagedAuto = bridgeEngagedAuto;
    }

    public boolean isBridgeDockedTeleOp() {
        return bridgeDockedTeleOp;
    }

    public void setBridgeDockedTeleOp(boolean bridgeDockedTeleOp) {
        this.bridgeDockedTeleOp = bridgeDockedTeleOp;
    }

    public boolean isBridgeEngagedTeleOp() {
        return bridgeEngagedTeleOp;
    }

    public void setBridgeEngagedTeleOp(boolean bridgeEngagedTeleOp) {
        this.bridgeEngagedTeleOp = bridgeEngagedTeleOp;
    }

    public boolean isDidMobility() {
        return didMobility;
    }

    public void setDidMobility(boolean didMobility) {
        this.didMobility = didMobility;
    }
    
    public double getCalcTeleGPCount() {
    	return calcTeleGPCount;
    }
    
    public void setCalcTeleGPCount(double calcTeleGPCount) {
    	this.calcTeleGPCount = calcTeleGPCount;
    }

    public double getCalcTeleGPCountSD() {
    	return calcTeleGPCountSD;
    }
    
    public void setCalcTeleGPCountSD(double calcTeleGPCountSD) {
    	this.calcTeleGPCountSD = calcTeleGPCountSD;
    }
    
    public double getCalcTeleGPPts() {
    	return calcTeleGPPts;
    }
    
    public void setCalcTeleGPPts(double calcTeleGPPts) {
    	this.calcTeleGPPts = calcTeleGPPts;
    }
    
    public double getCalcTeleGPPtsSD() {
    	return calcTeleGPPtsSD;
    }
    
    public void setCalcTeleGPPtsSD(double calcTeleGPPtsSD) {
    	this.calcTeleGPPtsSD = calcTeleGPPtsSD;
    }
    
    public double getCalcTotalMatchPts() {
    	return calcTotalMatchPts;
    }
    
    public void setCalcTotalMatchPts(double calcTotalMatchPts) {
    	this.calcTotalMatchPts = calcTotalMatchPts;
    }
    
    public double getCalcTotalMatchPtsSD() {
    	return calcTotalMatchPtsSD;
    }
    
    public void setCalcTotalMatchPtsSD(double calcTotalMatchPtsSD) {
    	this.calcTotalMatchPtsSD = calcTotalMatchPtsSD;
    }
    
    public String getComments() {
    	return comments;
    }
    
    public void setComments(String comm) {
    	this.comments = comm;
    }
}
