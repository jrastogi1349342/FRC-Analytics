package scouter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;

public class Team implements Comparable<Team> {
    private int team_number;
    private String eventKey;
    private ArrayList<Double> totalPts = new ArrayList<>();
    private ArrayList<TeamMatchStats> matchStats = new ArrayList<>();
    private int numTimesBridgeDockedAuto;
    private int numTimesBridgeEngagedAuto;
    private int numTimesBridgeDockedTeleOp;
    private int numTimesBridgeEngagedTeleOp;
    private int numTimesMobility;
    private ArrayList<Double> teleGPCounts = new ArrayList<>();
    private ArrayList<Double> teleGPPoints = new ArrayList<>();
    private double ptsDataSum;
    private double teleGPCtsSum;
    private double teleGPPtsSum;

    public int getTeam_number() {
        return team_number;
    }

    public void setTeam_number(int team_number) {
        this.team_number = team_number;
    }
    
    public String getEventKey() {
    	return eventKey;
    }
    
    public void setEventKey(String eventKey) {
    	this.eventKey = eventKey;
    }

    public ArrayList<Double> getTotalPts() {
        return totalPts;
    }

    public void addToTotalPts(Connection connection, PreparedStatement statement, 
    		double newMean, int teamNumber, String eventKey, int matchNumber) throws Exception {
        this.totalPts.add(newMean);

        //Add new mean points per match calculated for the first k + 1 matches into the database
        String addToDoubleValueTable = "INSERT IGNORE INTO doublevalue "
				+ "(teamNumber, eventKey, matchNumber, doubleValue, totalPts, teleGPCts, teleGPPts) "
				+ "values (?, ?, ?, ?, ?, ?, ?)";
		statement = connection.prepareStatement(addToDoubleValueTable);
		statement.setInt(1, teamNumber);
		statement.setString(2, eventKey);
		statement.setInt(3, matchNumber);
		statement.setDouble(4, newMean);
		statement.setBoolean(5, true);
		statement.setBoolean(6, false);
		statement.setBoolean(7, false);
		statement.executeUpdate();
		
		String updateTeamMatchStatsTable = "UPDATE IGNORE teammatchstats "
				+ "SET calcTotalMatchPts = ?, calcTotalMatchPtsSD = ? "
				+ "WHERE teamNumber = ? AND eventKey = ? AND matchNumber = ?";
		statement = connection.prepareStatement(updateTeamMatchStatsTable);
    	statement.setInt(3, teamNumber);
    	statement.setString(4, eventKey);
    	statement.setInt(5, matchNumber);
        
        if (matchStats.size() == 1) {
        	matchStats.get(0).setCalcTotalMatchPts(newMean);
        	matchStats.get(0).setCalcTotalMatchPtsSD(0);
        	
        	statement.setDouble(1, newMean);
        	statement.setDouble(2, 0);
        } else {
        	double matchTotalPts = newMean * matchStats.size() - ptsDataSum;
        	
        	matchStats.get(matchStats.size() - 1).setCalcTotalMatchPts(matchTotalPts);
        	statement.setDouble(1, matchTotalPts);
        	
        	double matchTotalPtsSD;
        	
        	/*Reason: there's a bug where the total number of points attributed 
        	 * to a team can be in the hundreds (infeasible) or negative (impossible) 
        	 * around their third and/or fourth match played, so I did not count 
        	 * those unusual values when calculating the SD. It is used in the following 
        	 * matches, so the SD ends up being too high, but the mean number of 
        	 * points scored seems to converge. */
        	if (matchStats.size() == 3 || matchStats.size() == 4) 
        		matchTotalPtsSD = matchStats.get(1).getCalcTotalMatchPtsSD();
        	else 
        		matchTotalPtsSD = getSD(newMean, 1);
        	
        	
        	matchStats.get(matchStats.size() - 1).setCalcTotalMatchPtsSD(matchTotalPtsSD);
        	statement.setDouble(2, matchTotalPtsSD);
        }
        
    	statement.executeUpdate();
        
        this.ptsDataSum = newMean * matchStats.size();
        
        String updateTeamTable = "UPDATE IGNORE team "
    			+ "SET ptsDataSum = ? "
    			+ "WHERE teamNumber = ? AND eventKey = ?";
    	statement = connection.prepareStatement(updateTeamTable);
    	statement.setDouble(1, ptsDataSum);
    	statement.setInt(2, teamNumber);
    	statement.setString(3, eventKey);
    	statement.executeUpdate();
    }
    
    public ArrayList<TeamMatchStats> getMatchStats() {
        return matchStats;
    }

    public void addMatchToMatchStats(TeamMatchStats stats) {
        this.matchStats.add(stats);
    }

    public int getNumTimesBridgeDockedAuto() {
        return numTimesBridgeDockedAuto;
    }

    public void setNumTimesBridgeDockedAuto(int numTimesBridgeDockedAuto) {
        this.numTimesBridgeDockedAuto = numTimesBridgeDockedAuto;
    }

    public int getNumTimesBridgeEngagedAuto() {
        return numTimesBridgeEngagedAuto;
    }

    public void setNumTimesBridgeEngagedAuto(int numTimesBridgeEngagedAuto) {
        this.numTimesBridgeEngagedAuto = numTimesBridgeEngagedAuto;
    }

    public int getNumTimesBridgeDockedTeleOp() {
        return numTimesBridgeDockedTeleOp;
    }

    public void setNumTimesBridgeDockedTeleOp(int numTimesBridgeDockedTeleOp) {
        this.numTimesBridgeDockedTeleOp = numTimesBridgeDockedTeleOp;
    }

    public int getNumTimesBridgeEngagedTeleOp() {
        return numTimesBridgeEngagedTeleOp;
    }

    public void setNumTimesBridgeEngagedTeleOp(int numTimesBridgeEngagedTeleOp) {
        this.numTimesBridgeEngagedTeleOp = numTimesBridgeEngagedTeleOp;
    }

    public int getNumTimesMobility() {
        return numTimesMobility;
    }

    public void setNumTimesMobility(int numTimesMobility) {
        this.numTimesMobility = numTimesMobility;
    }

    public ArrayList<Double> getTeleGPCounts() {
        return teleGPCounts;
    }

    public void addToTeleGPCounts(Connection connection, PreparedStatement statement, 
    		double teleGPCount, int teamNumber, String eventKey, int matchNumber) throws Exception {
        this.teleGPCounts.add(teleGPCount);
        
        //Add new mean points per match calculated for the first k + 1 matches into the database
        String addToDoubleValueTable = "INSERT IGNORE INTO doublevalue "
				+ "(teamNumber, eventKey, matchNumber, doubleValue, totalPts, teleGPCts, teleGPPts) "
				+ "values (?, ?, ?, ?, ?, ?, ?)";
		statement = connection.prepareStatement(addToDoubleValueTable);
		statement.setInt(1, teamNumber);
		statement.setString(2, eventKey);
		statement.setInt(3, matchNumber);
		statement.setDouble(4, teleGPCount);
		statement.setBoolean(5, false);
		statement.setBoolean(6, true);
		statement.setBoolean(7, false);
		statement.executeUpdate();
		
		String updateTeamMatchStatsTable = "UPDATE IGNORE teammatchstats "
				+ "SET calcTeleGPCount = ?, calcTeleGPCountSD = ? "
				+ "WHERE teamNumber = ? AND eventKey = ? AND matchNumber = ?";
		statement = connection.prepareStatement(updateTeamMatchStatsTable);
    	statement.setInt(3, teamNumber);
    	statement.setString(4, eventKey);
    	statement.setInt(5, matchNumber);
                
        if (matchStats.size() == 1) {
        	matchStats.get(0).setCalcTeleGPCount(teleGPCount);
        	matchStats.get(0).setCalcTeleGPCountSD(0);
        	
        	statement.setDouble(1, teleGPCount);
        	statement.setDouble(2, 0);
        } else {
        	double matchTotalGPCts = teleGPCount * matchStats.size() - teleGPCtsSum;
        	
        	matchStats.get(matchStats.size() - 1).setCalcTeleGPCount(matchTotalGPCts);
        	statement.setDouble(1, matchTotalGPCts);
        	
        	double matchTotalGPCtsSD;
        	
        	if (matchStats.size() == 3 || matchStats.size() == 4)
        		matchTotalGPCtsSD = matchStats.get(1).getCalcTeleGPCountSD();
        	else 
        		matchTotalGPCtsSD = getSD(teleGPCount, 2);
        	
        	matchStats.get(matchStats.size() - 1).setCalcTeleGPCountSD(matchTotalGPCtsSD);
        	statement.setDouble(2, matchTotalGPCtsSD);
        }
        
        statement.executeUpdate();

        this.teleGPCtsSum = teleGPCount * matchStats.size();
        
        String updateTeamTable = "UPDATE IGNORE team "
    			+ "SET teleGPCtsSum = ? "
    			+ "WHERE teamNumber = ? AND eventKey = ?";
    	statement = connection.prepareStatement(updateTeamTable);
    	statement.setDouble(1, teleGPCtsSum);
    	statement.setInt(2, teamNumber);
    	statement.setString(3, eventKey);
    	statement.executeUpdate();
    }

    public ArrayList<Double> getTeleGPPoints() {
        return teleGPPoints;
    }

    public void addToTeleGPPoints(Connection connection, PreparedStatement statement, 
    		double teleGPPoint, int teamNumber, String eventKey, int matchNumber) throws Exception {
        this.teleGPPoints.add(teleGPPoint);
        
      //Add new mean points per match calculated for the first k + 1 matches into the database
        String addToDoubleValueTable = "INSERT IGNORE INTO doublevalue "
				+ "(teamNumber, eventKey, matchNumber, doubleValue, totalPts, teleGPCts, teleGPPts) "
				+ "values (?, ?, ?, ?, ?, ?, ?)";
		statement = connection.prepareStatement(addToDoubleValueTable);
		statement.setInt(1, teamNumber);
		statement.setString(2, eventKey);
		statement.setInt(3, matchNumber);
		statement.setDouble(4, teleGPPoint);
		statement.setBoolean(5, false);
		statement.setBoolean(6, false);
		statement.setBoolean(7, true);
		statement.executeUpdate();
		
		String updateTeamMatchStatsTable = "UPDATE IGNORE teammatchstats "
				+ "SET calcTeleGPPts = ?, calcTeleGPPtsSD = ? "
				+ "WHERE teamNumber = ? AND eventKey = ? AND matchNumber = ?";
		statement = connection.prepareStatement(updateTeamMatchStatsTable);
    	statement.setInt(3, teamNumber);
    	statement.setString(4, eventKey);
    	statement.setInt(5, matchNumber);
                
        if (matchStats.size() == 1) {
        	matchStats.get(0).setCalcTeleGPPts(teleGPPoint);
        	matchStats.get(0).setCalcTeleGPPtsSD(0);
        	
        	statement.setDouble(1, teleGPPoint);
        	statement.setDouble(2, 0);
        } else {
        	double matchGPPts = teleGPPoint * matchStats.size() - teleGPPtsSum;
        	
        	matchStats.get(matchStats.size() - 1).setCalcTeleGPPts(matchGPPts);
        	statement.setDouble(1, matchGPPts);
        	
        	double matchGPPtsSD;
        	
        	/*Reason: there's a bug where the total number of points attributed 
        	 * to a team can be in the hundreds (infeasible) or negative (impossible) 
        	 * around their third and/or fourth match played, so I did not count 
        	 * those unusual values when calculating the SD. It is used in the following 
        	 * matches, so the SD ends up being too high, but the mean number of 
        	 * points scored seems to converge. */
        	if (matchStats.size() == 3 || matchStats.size() == 4) 
        		matchGPPtsSD = matchStats.get(1).getCalcTeleGPPtsSD();
        	else 
        		matchGPPtsSD = getSD(teleGPPoint, 3);

        	matchStats.get(matchStats.size() - 1).setCalcTeleGPPtsSD(matchGPPtsSD);
        	statement.setDouble(2, matchGPPtsSD);
        }
        
        this.teleGPPtsSum = teleGPPoint * matchStats.size();
        
        String updateTeamTable = "UPDATE IGNORE team "
    			+ "SET teleGPPtsSum = ? "
    			+ "WHERE teamNumber = ? AND eventKey = ?";
    	statement = connection.prepareStatement(updateTeamTable);
    	statement.setDouble(1, teleGPPtsSum);
    	statement.setInt(2, teamNumber);
    	statement.setString(3, eventKey);
    	statement.executeUpdate();
    }
    
    public double getPtsDataSum() {
    	return ptsDataSum;
    }
    
    public double getTeleGPCtsSum() {
    	return teleGPCtsSum;
    }
    
    public double getTeleGPPtsSum() {
    	return teleGPPtsSum;
    }

    private double getSD(double mean, int state) {
    	if (state <= 0 || state > 3) throw new IllegalArgumentException("Invalid state");
    	
    	double standardDev = 0;
    	
    	for (int i = 0; i < matchStats.size(); i++) {
    		if (i != 2 && i != 3) {
    			if (state == 1) standardDev += Math.pow(matchStats.get(i).getCalcTotalMatchPts() - mean, 2);
    			else if (state == 2) standardDev += Math.pow(matchStats.get(i).getCalcTeleGPCount() - mean, 2);
    			else if (state == 3) standardDev += Math.pow(matchStats.get(i).getCalcTeleGPPts() - mean, 2);
    		}
    	}
    	
    	if (matchStats.size() <= 2) return Math.sqrt(standardDev / matchStats.size());
    	else if (matchStats.size() <= 4) return Math.sqrt(standardDev / 2);
    	else return Math.sqrt(standardDev / (matchStats.size() - 2));
    }
    
    @Override
    public int compareTo(Team o) {
        return Integer.compare(team_number, o.getTeam_number());
    }

    @Override
    public String toString() {
        return "Team{" +
                "team_number=" + team_number +
                ", currentAvgPts=" + totalPts +
                '}';
    }
}
