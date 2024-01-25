package scouter;

public class AllianceScoreDetails {
    private String autoBridgeState;
    private int autoChargeStationPoints;
    private String autoChargeStationRobot1;
    private String autoChargeStationRobot2;
    private String autoChargeStationRobot3;
    private String [] autoChargeStation;
    private int autoMobilityPoints;
    private String mobilityRobot1;
    private String mobilityRobot2;
    private String mobilityRobot3;
    private String [] autoMobility;
    private int autoPoints;
    private String endGameBridgeState;
    private int endGameChargeStationPoints;
    private String endGameChargeStationRobot1;
    private String endGameChargeStationRobot2;
    private String endGameChargeStationRobot3;
    private String [] endGameChargeStation;
    private int endGameParkPoints;
    private int foulPoints;
    private int linkPoints;
    private int teleopGamePieceCount;
    private int teleopGamePiecePoints;

    public String getAutoBridgeState() {
        return autoBridgeState;
    }

    public void setAutoBridgeState(String autoBridgeState) {
        this.autoBridgeState = autoBridgeState;
    }

    public int getAutoChargeStationPoints() {
        return autoChargeStationPoints;
    }

    public void setAutoChargeStationPoints(int autoChargeStationPoints) {
        this.autoChargeStationPoints = autoChargeStationPoints;
    }

    public String getAutoChargeStationRobot1() {
        return autoChargeStationRobot1;
    }

    public void setAutoChargeStationRobot1(String autoChargeStationRobot1) {
        this.autoChargeStationRobot1 = autoChargeStationRobot1;
    }

    public String getAutoChargeStationRobot2() {
        return autoChargeStationRobot2;
    }

    public void setAutoChargeStationRobot2(String autoChargeStationRobot2) {
        this.autoChargeStationRobot2 = autoChargeStationRobot2;
    }

    public String getAutoChargeStationRobot3() {
        return autoChargeStationRobot3;
    }

    public void setAutoChargeStationRobot3(String autoChargeStationRobot3) {
        this.autoChargeStationRobot3 = autoChargeStationRobot3;
    }

    public String [] getAutoChargeStation() {
        return autoChargeStation;
    }

    public void setAutoChargeStation() {
        this.autoChargeStation = new String[3];
        autoChargeStation[0] = autoChargeStationRobot1;
        autoChargeStation[1] = autoChargeStationRobot2;
        autoChargeStation[2] = autoChargeStationRobot3;
    }

    public int getAutoMobilityPoints() {
        return autoMobilityPoints;
    }

    public void setAutoMobilityPoints(int autoMobilityPoints) {
        this.autoMobilityPoints = autoMobilityPoints;
    }

    public String getMobilityRobot1() {
        return mobilityRobot1;
    }

    public void setMobilityRobot1(String mobilityRobot1) {
        this.mobilityRobot1 = mobilityRobot1;
    }

    public String getMobilityRobot2() {
        return mobilityRobot2;
    }

    public void setMobilityRobot2(String mobilityRobot2) {
        this.mobilityRobot2 = mobilityRobot2;
    }

    public String getMobilityRobot3() {
        return mobilityRobot3;
    }

    public void setMobilityRobot3(String mobilityRobot3) {
        this.mobilityRobot3 = mobilityRobot3;
    }

    public String[] getAutoMobility() {
        return autoMobility;
    }

    public void setAutoMobility() {
        this.autoMobility = new String[3];
        autoMobility[0] = mobilityRobot1;
        autoMobility[1] = mobilityRobot2;
        autoMobility[2] = mobilityRobot3;
    }

    public int getAutoPoints() {
        return autoPoints;
    }

    public void setAutoPoints(int autoPoints) {
        this.autoPoints = autoPoints;
    }

    public String getEndGameBridgeState() {
        return endGameBridgeState;
    }

    public void setEndGameBridgeState(String endGameBridgeState) {
        this.endGameBridgeState = endGameBridgeState;
    }

    public int getEndGameChargeStationPoints() {
        return endGameChargeStationPoints;
    }

    public void setEndGameChargeStationPoints(int endGameChargeStationPoints) {
        this.endGameChargeStationPoints = endGameChargeStationPoints;
    }

    public String getEndGameChargeStationRobot1() {
        return endGameChargeStationRobot1;
    }

    public void setEndGameChargeStationRobot1(String endGameChargeStationRobot1) {
        this.endGameChargeStationRobot1 = endGameChargeStationRobot1;
    }

    public String getEndGameChargeStationRobot2() {
        return endGameChargeStationRobot2;
    }

    public void setEndGameChargeStationRobot2(String endGameChargeStationRobot2) {
        this.endGameChargeStationRobot2 = endGameChargeStationRobot2;
    }

    public String getEndGameChargeStationRobot3() {
        return endGameChargeStationRobot3;
    }

    public void setEndGameChargeStationRobot3(String endGameChargeStationRobot3) {
        this.endGameChargeStationRobot3 = endGameChargeStationRobot3;
    }

    public String[] getEndGameChargeStation() {
        return endGameChargeStation;
    }

    public void setEndGameChargeStation() {
        this.endGameChargeStation = new String[3];
        endGameChargeStation[0] = endGameChargeStationRobot1;
        endGameChargeStation[1] = endGameChargeStationRobot2;
        endGameChargeStation[2] = endGameChargeStationRobot3;
    }

    public int getEndGameParkPoints() {
        return endGameParkPoints;
    }

    public void setEndGameParkPoints(int endGameParkPoints) {
        this.endGameParkPoints = endGameParkPoints;
    }

    public int getFoulPoints() {
        return foulPoints;
    }

    public void setFoulPoints(int foulPoints) {
        this.foulPoints = foulPoints;
    }

    public int getLinkPoints() {
        return linkPoints;
    }

    public void setLinkPoints(int linkPoints) {
        this.linkPoints = linkPoints;
    }

    public int getTeleopGamePieceCount() {
        return teleopGamePieceCount;
    }

    public void setTeleopGamePieceCount(int teleopGamePieceCount) {
        this.teleopGamePieceCount = teleopGamePieceCount;
    }

    public int getTeleopGamePiecePoints() {
        return teleopGamePiecePoints;
    }

    public void setTeleopGamePiecePoints(int teleopGamePiecePoints) {
        this.teleopGamePiecePoints = teleopGamePiecePoints;
    }
}
