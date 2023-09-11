package scouter;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

@Path("/")
public class Main {
    private static LinkedHashMap<Integer, Team> teamHashMap = new LinkedHashMap<>();
    private static ArrayList<Team> teams;
    private static Gson gson = new Gson();
    private static MatchMatrix matrix;
    private static int firstMatchToAdd = 0; //Will change
    private static int lastMatchToAdd = 119; //Will change
    private static ArrayList<Match> matches;
    private static String eventKey = "2023mrcmp"; //Will change
    private static Connection connection;

    public static void main(String[] args) throws Exception {
        mainMethod();
    }

	private static void mainMethod() throws Exception {
		connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/scouterdb", Constants.DB_USER, Constants.DB_PASS);
		
		String addToTournamentTable = "INSERT IGNORE INTO tournament "
				+ "(eventKey) "
				+ "values (?)";
		PreparedStatement statementSQL = connection.prepareStatement(addToTournamentTable);
		statementSQL.setString(1, eventKey);
		statementSQL.executeUpdate();
		
        HttpClient httpClient = HttpClient.newHttpClient();

        HttpRequest getTeams = HttpRequest.newBuilder()
                .uri(new URI("https://www.thebluealliance.com/api/v3/event/" + eventKey + "/teams"))
                .header("X-TBA-Auth-Key", Constants.TBA_API_KEY)
                .build();

        HttpResponse<String> getTeamsResponse = httpClient.send(getTeams,
                    HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status code of GET for teams: " + getTeamsResponse.statusCode());

        teams = gson.fromJson(getTeamsResponse.body(), new TypeToken<ArrayList<Team>>(){}.getType());
        Collections.sort(teams);

        matrix = new MatchMatrix(teams);
        
        for (Team team : teams) {
        	String addToTeamTable = "INSERT IGNORE INTO team "
        			+ "(teamNumber, eventKey) "
        			+ "values (?, ?)";
        	statementSQL = connection.prepareStatement(addToTeamTable);
        	statementSQL.setInt(1, team.getTeam_number());
        	statementSQL.setString(2, eventKey);
        	statementSQL.executeUpdate();
        	
        	team.setEventKey(eventKey);
        }
        
        //This is my data storage in memory
        for (Team team : teams) {
            teamHashMap.put(team.getTeam_number(), team);
        }

        HttpRequest getMatches = HttpRequest.newBuilder()
                .uri(new URI("https://www.thebluealliance.com/api/v3/event/" + eventKey + "/matches"))
                .header("X-TBA-Auth-Key", Constants.TBA_API_KEY)
                .build();

        HttpResponse<String> getMatchesResponse = httpClient.send(getMatches,
                HttpResponse.BodyHandlers.ofString());
        
        System.out.println("Status code of GET for matches: " + getMatchesResponse.statusCode());

        matches = gson.fromJson(getMatchesResponse.body(), new TypeToken<ArrayList<Match>>(){}.getType());
        matches = sortMatchesChronological(matches);

        for (int matchNum = firstMatchToAdd; matchNum < lastMatchToAdd; matchNum++) {
            int matchNumber = matches.get(matchNum).getMatch_number();

            int blueOne = getTeamNum(matches, "BLUE", 0, matchNum);
            int blueTwo = getTeamNum(matches, "BLUE", 1, matchNum);
            int blueThree = getTeamNum(matches, "BLUE", 2, matchNum);

            int redOne = getTeamNum(matches, "RED", 0, matchNum);
            int redTwo = getTeamNum(matches, "RED", 1, matchNum);
            int redThree = getTeamNum(matches, "RED", 2, matchNum);

            matrix.addTeamToMatch(blueOne, matchNumber, true);
            matrix.addTeamToMatch(blueTwo, matchNumber, true);
            matrix.addTeamToMatch(blueThree, matchNumber, true);

            matrix.addTeamToMatch(redOne, matchNumber, false);
            matrix.addTeamToMatch(redTwo, matchNumber, false);
            matrix.addTeamToMatch(redThree, matchNumber, false);

            matrix.addTotalScoreToMatch(matches.get(matchNum).getAlliances().getBlue().getScore(), matchNumber, true);
            matrix.addTotalScoreToMatch(matches.get(matchNum).getAlliances().getRed().getScore(), matchNumber, false);
            
            matrix.addTeleGamePieceNumToMatch(matches.get(matchNum).getScore_breakdown().getBlue().getTeleopGamePieceCount(), matchNumber, true);
            matrix.addTeleGamePieceNumToMatch(matches.get(matchNum).getScore_breakdown().getRed().getTeleopGamePieceCount(), matchNumber, false);

            matrix.addTeleGamePieceScoreToMatch(matches.get(matchNum).getScore_breakdown().getBlue().getTeleopGamePiecePoints(), matchNumber, true);
            matrix.addTeleGamePieceScoreToMatch(matches.get(matchNum).getScore_breakdown().getRed().getTeleopGamePiecePoints(), matchNumber, false);

            matches.get(matchNum).getScore_breakdown().getRed().setAutoChargeStation();
            matches.get(matchNum).getScore_breakdown().getRed().setAutoMobility();
            matches.get(matchNum).getScore_breakdown().getRed().setEndGameChargeStation();

            matches.get(matchNum).getScore_breakdown().getBlue().setAutoChargeStation();
            matches.get(matchNum).getScore_breakdown().getBlue().setAutoMobility();
            matches.get(matchNum).getScore_breakdown().getBlue().setEndGameChargeStation();

            addMatchStatsToTeams(statementSQL, "BLUE", blueOne, matchNum, matches, 1);
            addMatchStatsToTeams(statementSQL, "BLUE", blueTwo, matchNum, matches, 2);
            addMatchStatsToTeams(statementSQL, "BLUE", blueThree, matchNum, matches, 3);

            addMatchStatsToTeams(statementSQL, "RED", redOne, matchNum, matches, 1);
            addMatchStatsToTeams(statementSQL, "RED", redTwo, matchNum, matches, 2);
            addMatchStatsToTeams(statementSQL, "RED", redThree, matchNum, matches, 3);
            
            addCurrentAvgPts(statementSQL, matrix.solveLeastSquares(matrix.getTotalScoresMtx()),
                    matrix.solveLeastSquares(matrix.getTeleGPCountMtx()),
                    matrix.solveLeastSquares(matrix.getTeleGPPointsMtx()), eventKey, matchNumber);

			/*
			 * addCurrentAvgPtsDoubleMtx(connection, statementSQL,
			 * matrix.solveLeastSquaresDoubleMtx(matrix.getTotalScoresMtx()),
			 * matrix.solveLeastSquaresDoubleMtx(matrix.getTeleGPCountMtx()),
			 * matrix.solveLeastSquaresDoubleMtx(matrix.getTeleGPPointsMtx()), eventKey,
			 * matchNumber);
			 */
        }

//        System.out.println("Team num: " + teams.get(18).getTeam_number() + "\n");
//        for (int i = 0; i < teams.get(18).getMatchStats().size(); i++) {
//            System.out.println("Match num for this team: " + i + "\tGuess of total points scored in that match: " + 
//            		teams.get(18).getMatchStats().get(i).getCalcTotalMatchPts());
//        }
        
//        removeDuplicatesFromDB(statementSQL, "teammatchstats");
//        removeDuplicatesFromDB(statementSQL, "doublevalue");
        
        System.out.println(predictWinner(statementSQL, 2539, 222, 484, 11, 4373, 7045));
        
        //TODO debug queries
		/*
		 * String getTotalPts = "SELECT teamNumber, matchNumber, MAX(doubleValue) " +
		 * "FROM doublevalue " // + "JOIN " // +
		 * "(SELECT teamNumber, MAX(matchNumber) FROM doublevalue GROUP BY teamNumber) AS mx ON dv.teamNumber = mx.teamNumber "
		 * + "WHERE eventKey = ? AND totalPts = 1 " +
		 * "GROUP BY teamNumber, matchNumber " + "ORDER BY doubleValue DESC";
		 * statementSQL = connection.prepareStatement(getTotalPts);
		 * statementSQL.setString(1, eventKey); ResultSet set =
		 * statementSQL.executeQuery();
		 * 
		 * while (set.next()) { System.out.println("Team num: " +
		 * set.getInt("teamNumber") + "\tMatch num: " + set.getInt("matchNumber") +
		 * "\tValue: " + set.getInt("doubleValue")); }
		 */        
//        sortByDescendingPts(teams);

//        sortNumTeleOpBalances(teams);
        
	}
	
	//TODO: debug this - works in MySQL server but not here
	private static void removeDuplicatesFromDB(PreparedStatement statement, String dbName) throws Exception {
		String getExtraData = "select max('id') as 'idVal' from scouterdb.teammatchstats where 'id' >= 0 group by 'teamNumber'";
		statement = connection.prepareStatement(getExtraData);
//		statement.setString(1, "scouterdb." + dbName);
        ResultSet result = statement.executeQuery();
        
        //Format IDs
        StringBuffer formattedIDs = new StringBuffer();
        
        while (result.next()) {
        	formattedIDs.append(result.getInt("idVal")).append(", ");
        }
        
        formattedIDs.delete(formattedIDs.length() - 2, formattedIDs.length());
        String formattedIDStr = formattedIDs.toString();
        String deleteExtraData = "delete from ? where id not in (?)";
        statement = connection.prepareStatement(deleteExtraData);
        statement.setString(1, "scouterdb." + dbName);
        statement.setString(2, formattedIDStr);
        statement.execute();
	}
	
	private static void addCurrentAvgPts(PreparedStatement statement,  double [][] totalPts, 
			double [][] teleGPCounts, double [][] teleGPPoints,  String eventKey, 
			int matchNum) throws Exception {
        for (int j = 0; j < totalPts.length; j++) {
        	//If a team played in this match, add point data to their stat sheet
        	if (matrix.getPresenceMtx()[2 * matchNum - 2][j] == 1 || 
        			matrix.getPresenceMtx()[2 * matchNum - 1][j] == 1) {
        		teams.get(j).addToTotalPts(connection, statement, totalPts[j][0], 
        				teams.get(j).getTeam_number(), eventKey, matchNum);
        		
                teams.get(j).addToTeleGPCounts(connection, statement, teleGPCounts[j][0], 
                		teams.get(j).getTeam_number(), eventKey, matchNum);
                
                teams.get(j).addToTeleGPPoints(connection, statement, teleGPPoints[j][0], 
                		teams.get(j).getTeam_number(), eventKey, matchNum);
        	}
        }
    }

//    private static void addCurrentAvgPtsDoubleMtx(Connection connection, PreparedStatement statement, 
//    		DoubleMatrix totalPts, DoubleMatrix teleGPCounts, DoubleMatrix teleGPPoints, 
//    		String eventKey, int matchNum) throws Exception {
//        for (int j = 0; j < totalPts.length; j++) {
//        	if (matrix.getPresenceMtx()[2 * matchNum - 2][j] == 1 || 
//        			matrix.getPresenceMtx()[2 * matchNum - 1][j] == 1) {
//        		teams.get(j).addToTotalPts(connection, statement, totalPts.get(j, 0), 
//        				teams.get(j).getTeam_number(), eventKey, matchNum);
//                teams.get(j).addToTeleGPCounts(connection, statement, teleGPCounts.get(j, 0), 
//                		teams.get(j).getTeam_number(), eventKey, matchNum);
//                teams.get(j).addToTeleGPPoints(connection, statement, teleGPPoints.get(j, 0), 
//                		teams.get(j).getTeam_number(), eventKey, matchNum);
//        	}
//        }
//    }

    private static int getTeamNum(ArrayList<Match> matches, String allianceColor,
                                  int allianceNumber, int i) {
        if (!allianceColor.equals("RED") && !allianceColor.equals("BLUE"))
            throw new IllegalArgumentException("Wrong color");

        if (allianceColor.equals("RED"))
            return Integer.parseInt(matches.get(i).getAlliances().getRed().getTeam_keys()[allianceNumber].substring(3));
        else return Integer.parseInt(matches.get(i).getAlliances().getBlue().getTeam_keys()[allianceNumber].substring(3));
    }
    
    //Note: will only be accurate after the 5th or 6th match or so
    //TODO debug this
    public static String predictWinner(PreparedStatement statement, int blueOne, int blueTwo, 
    		int blueThree, int redOne, int redTwo, int redThree) throws Exception {
    	String queryValue = "select max(matchNumber) as maxNum, doubleValue from "
    			+ "scouterdb.doublevalue where teamNumber = ? and totalPts = 1 group by doublevalue "
    			+ "order by maxNum";
    	
    	int blueScore = getMostRecentAvg(statement, blueOne, queryValue) + getMostRecentAvg(statement, blueTwo, queryValue) + 
    			getMostRecentAvg(statement, blueThree, queryValue);
    	
    	int redScore = getMostRecentAvg(statement, redOne, queryValue) + getMostRecentAvg(statement, redTwo, queryValue) + 
    			getMostRecentAvg(statement, redThree, queryValue);
    	
    	if (blueScore > redScore) return "BLUE";
    	else if (redScore > blueScore) return "RED";
    	else return "DRAW";
    }
    
    public static int getMostRecentAvg(PreparedStatement statement, int teamNum, String query) throws Exception {
    	statement = connection.prepareStatement(query);
    	statement.setInt(1, teamNum);
    	ResultSet set = statement.executeQuery();
    	
    	while (set.next()) {
    		System.out.println("maxNum: " + set.getInt("maxNum") + "\tValue: " + set.getDouble("doubleValue"));
    	}
    	
    	set.last();
    	
    	return set.getInt("doubleValue");
    }

    private static void addMatchStatsToTeams(PreparedStatement statement, String allianceColor,
                                             int teamNum, int matchNum, ArrayList<Match> matches,
                                             int teamNumInAlliance) throws Exception {
        if (!allianceColor.equals("RED") && !allianceColor.equals("BLUE"))
            throw new IllegalArgumentException("Wrong color");

        if (teamNumInAlliance < 1 || teamNumInAlliance > 3)
            throw new IllegalArgumentException("Team num in alliance out of range (1-3)");

        AllianceScoreDetails breakdown;
        Team team = teamHashMap.get(teamNum);
        TeamMatchStats matchStats = new TeamMatchStats();
        
        matchStats.setTeamNumber(teamNum);
        matchStats.setMatchNum(matchNum);
        matchStats.setEventKey(eventKey);
        
        String addToTeamMatchStatsTable = "INSERT IGNORE INTO teammatchstats "
        		+ "(teamNumber, eventKey, matchNumber, bridgeDockAuto, "
        		+ "bridgeEngageAuto, bridgeDockTele, bridgeEngageTele, didMobility) "
        		+ "values (?, ?, ?, ?, ?, ?, ?, ?)";
        statement = connection.prepareStatement(addToTeamMatchStatsTable);
        statement.setInt(1, team.getTeam_number());
    	statement.setString(2, eventKey);
    	statement.setInt(3, (matchNum + 1));
    	
    	//Known bug: mobility is 13 in a few cases where teams only played 12 times (ex. 11, 203, 341)
    	String updateTeamTable = "UPDATE IGNORE team "
    			+ "SET numBridgeDockAuto = ?, numBridgeEngageAuto = ?, "
    			+ "numBridgeDockTele = ?, numBridgeEngageTele = ?, "
    			+ "numMobility = ? "
    			+ "WHERE teamNumber = ? AND eventKey = ?";
    	PreparedStatement secStatement = connection.prepareStatement(updateTeamTable);
    	secStatement.setInt(6, teamNum);
    	secStatement.setString(7, eventKey);
    	
        if (allianceColor.equals("RED")) {
            breakdown = matches.get(matchNum).getScore_breakdown().getRed();
        } else {
            breakdown = matches.get(matchNum).getScore_breakdown().getBlue();
        }
        
        if (breakdown.getAutoMobility()[teamNumInAlliance - 1].equals("Yes")) {
            team.setNumTimesMobility(team.getNumTimesMobility() + 1);
            matchStats.setDidMobility(true);
            
            secStatement.setInt(5, team.getNumTimesMobility() + 1);
            statement.setBoolean(8, true);
        } else {
            secStatement.setInt(5, team.getNumTimesMobility());
        	statement.setBoolean(8, false);
        }

        if (breakdown.getAutoChargeStation()[teamNumInAlliance - 1].equals("Docked")) {
            if (breakdown.getAutoChargeStationPoints() == 8) {
                team.setNumTimesBridgeDockedAuto(team.getNumTimesBridgeDockedAuto() + 1);
                matchStats.setBridgeDockedAuto(true);
                
                secStatement.setInt(1, team.getNumTimesBridgeDockedAuto() + 1);
                secStatement.setInt(2, team.getNumTimesBridgeEngagedAuto());
                
                statement.setBoolean(4, true);
                statement.setBoolean(5, false);
            } else {
                team.setNumTimesBridgeEngagedAuto(team.getNumTimesBridgeEngagedAuto() + 1);
                matchStats.setBridgeEngagedAuto(true);
                
                secStatement.setInt(1, team.getNumTimesBridgeDockedAuto());
                secStatement.setInt(2, team.getNumTimesBridgeEngagedAuto() + 1);
                
                statement.setBoolean(4, false);
                statement.setBoolean(5, true);
            }
        } else {
            secStatement.setInt(1, team.getNumTimesBridgeDockedAuto());
        	secStatement.setInt(2, team.getNumTimesBridgeEngagedAuto());
        	
            statement.setBoolean(4, false);
            statement.setBoolean(5, false);
        }

        if (breakdown.getEndGameChargeStation()[teamNumInAlliance - 1].equals("Docked")) {
            if (breakdown.getEndGameBridgeState().equals("NotLevel")) {
                team.setNumTimesBridgeDockedTeleOp(team.getNumTimesBridgeDockedTeleOp() + 1);
                matchStats.setBridgeDockedTeleOp(true);
                
                secStatement.setInt(3, team.getNumTimesBridgeDockedTeleOp() + 1);
                secStatement.setInt(4, team.getNumTimesBridgeEngagedTeleOp());
                
                statement.setBoolean(6, true);
                statement.setBoolean(7, false);
            } else {
                team.setNumTimesBridgeEngagedTeleOp(team.getNumTimesBridgeEngagedTeleOp() + 1);
                matchStats.setBridgeEngagedTeleOp(true);

                secStatement.setInt(3, team.getNumTimesBridgeDockedTeleOp());
                secStatement.setInt(4, team.getNumTimesBridgeEngagedTeleOp() + 1);
                
                statement.setBoolean(6, false);
                statement.setBoolean(7, true);
            }
        } else {
            secStatement.setInt(3, team.getNumTimesBridgeDockedTeleOp());
            secStatement.setInt(4, team.getNumTimesBridgeEngagedTeleOp());
            
            statement.setBoolean(6, false);
            statement.setBoolean(7, false);
        }

        team.addMatchToMatchStats(matchStats);
        
        statement.executeUpdate();
        secStatement.executeUpdate();
    }

    private static ArrayList<Match> sortMatchesChronological(ArrayList<Match> matches) {
        ArrayList<Match> qualMatches = new ArrayList<>();
        ArrayList<Match> playoffMatches = new ArrayList<>();
        ArrayList<Match> finalMatches = new ArrayList<>();

        for (Match match : matches) {
            if (match.getComp_level().equals("qm")) qualMatches.add(match);
            if (match.getComp_level().equals("sf")) playoffMatches.add(match);
            if (match.getComp_level().equals("f")) finalMatches.add(match);
        }

        Collections.sort(qualMatches);
        Collections.sort(playoffMatches);
        Collections.sort(finalMatches);

        for (int i = 1; i <= playoffMatches.size(); i++) {
            playoffMatches.get(i - 1).setMatch_number(qualMatches.size() + i);
        }

        for (int i = 1; i <= finalMatches.size(); i++) {
            finalMatches.get(i - 1).setMatch_number(qualMatches.size() + playoffMatches.size() + i);
        }

        ArrayList<Match> matchOrder = new ArrayList<>(qualMatches);
        matchOrder.addAll(playoffMatches);
        matchOrder.addAll(finalMatches);

        return matchOrder;
    }
    
    @Path("sort/descending-points")
    public SortDescendingPts sortByDescendingPts(@QueryParam("comp") String eventKey) {
    	Main.eventKey = eventKey;
    	return new SortDescendingPts();
    }
    
    public class SortDescendingPts {
    	@GET
    	@Produces(MediaType.APPLICATION_JSON)
    	public static String sortByDescendingPts() throws Exception {
    		mainMethod();
    		    		
    		String getTotalPts = "SELECT teamNumber, doubleValue "
    				+ "FROM doublevalue "
    				+ "WHERE eventKey = ? AND totalPts = 1 AND matchNumber = (SELECT MAX(matchNumber) FROM doublevalue GROUP BY teamNumber) "
    				+ "GROUP BY teamNumber "
    				+ "ORDER BY doubleValue DESC";
    		PreparedStatement statementSQL = connection.prepareStatement(getTotalPts);
    		statementSQL.setString(1, eventKey);
    		ResultSet set = statementSQL.executeQuery();
    		
    		ObjectMapper mapper = new ObjectMapper();
    		ArrayNode rootNode = mapper.createArrayNode();
    		
    		while (set.next()) {
    			JsonNode childNode = mapper.createObjectNode();
    			((ObjectNode) childNode).put(String.valueOf(set.getInt("teamNumber")), set.getDouble("doubleValue"));
    			rootNode.add(childNode);
    		}
    		
//    		LinkedHashMap<Integer, Double> teamAvgPts = new LinkedHashMap<>();
//    		
//    		for (Team team : teams) teamAvgPts.put(team.getTeam_number(), team.getTotalPts().get(team.getTotalPts().size() - 1));
//    		
//    		LinkedHashMap<Integer, Double> sortedMap = 
//    				teamAvgPts
//    				.entrySet()
//    				.stream()
//    				.sorted((a, b) -> b.getValue().compareTo(a.getValue()))
//    				.collect(Collectors.toMap(Map.Entry::getKey, 
//    						Map.Entry::getValue, 
//    						(c, d) -> c, LinkedHashMap::new));
//    		
//    		ObjectMapper mapper = new ObjectMapper();
//    		ArrayNode rootNode = mapper.createArrayNode();
//    		
//    		for (Map.Entry<Integer, Double> entry : sortedMap.entrySet()) {
//    			JsonNode childNode = mapper.createObjectNode();
//    			((ObjectNode) childNode).put(String.valueOf(entry.getKey()), entry.getValue());
//    			rootNode.add(childNode);
//    		}
    		
    		return mapper.writeValueAsString(rootNode);
    	}
    }

    @Path("sort/descending-tele-gp-count")
    public SortDescendingTeleGPCount sortByDescendingTeleGPCount() {
    	return new SortDescendingTeleGPCount();
    }
    
    public class SortDescendingTeleGPCount {
    	@GET
    	@Produces(MediaType.APPLICATION_JSON)
    	public static String sortByDescendingTeleGPCount() throws Exception {
    		mainMethod();
    		
    		LinkedHashMap<Integer, Double> teamAvgPts = new LinkedHashMap<>();
    		
    		for (Team team : teams) teamAvgPts.put(team.getTeam_number(), team.getTeleGPCounts().get(team.getTeleGPCounts().size() - 1));
    		
    		LinkedHashMap<Integer, Double> sortedMap = 
    				teamAvgPts
    				.entrySet()
    				.stream()
    				.sorted((a, b) -> b.getValue().compareTo(a.getValue()))
    				.collect(Collectors.toMap(Map.Entry::getKey, 
    						Map.Entry::getValue, 
    						(c, d) -> c, LinkedHashMap::new));
    		
    		ObjectMapper mapper = new ObjectMapper();
    		ArrayNode rootNode = mapper.createArrayNode();
    		
    		for (Map.Entry<Integer, Double> entry : sortedMap.entrySet()) {
    			JsonNode childNode = mapper.createObjectNode();
    			((ObjectNode) childNode).put(String.valueOf(entry.getKey()), entry.getValue());
    			rootNode.add(childNode);
    		}
    		
    		return mapper.writeValueAsString(rootNode);
    	}
    }
    
    @Path("sort/descending-tele-gp-points")
    public SortDescendingTeleGPPoints sortByDescendingTeleGPPoints() {
    	return new SortDescendingTeleGPPoints();
    }
    
    public class SortDescendingTeleGPPoints {
    	@GET
    	@Produces(MediaType.APPLICATION_JSON)
    	public static String sortByDescendingTeleGPPts() throws Exception {
    		mainMethod();
    		
    		LinkedHashMap<Integer, Double> teamAvgPts = new LinkedHashMap<>();
    		
    		for (Team team : teams) teamAvgPts.put(team.getTeam_number(), team.getTeleGPPoints().get(team.getTeleGPPoints().size() - 1));
    		
    		LinkedHashMap<Integer, Double> sortedMap = 
    				teamAvgPts
    				.entrySet()
    				.stream()
    				.sorted((a, b) -> b.getValue().compareTo(a.getValue()))
    				.collect(Collectors.toMap(Map.Entry::getKey, 
    						Map.Entry::getValue, 
    						(c, d) -> c, LinkedHashMap::new));
    		
    		ObjectMapper mapper = new ObjectMapper();
    		ArrayNode rootNode = mapper.createArrayNode();
    		
    		for (Map.Entry<Integer, Double> entry : sortedMap.entrySet()) {
    			JsonNode childNode = mapper.createObjectNode();
    			((ObjectNode) childNode).put(String.valueOf(entry.getKey()), entry.getValue());
    			rootNode.add(childNode);
    		}
    		
    		return mapper.writeValueAsString(rootNode);
    	}
    }
    
    @Path("sort/descending-auto-balances")
    public SortAutoBalances sortByNumAutoBalances(@QueryParam("comp") String eventKey, @DefaultValue("0") @QueryParam("first") int firstMatch, 
    			@DefaultValue("0") @QueryParam("last") int lastMatch) {
    	Main.eventKey = eventKey;
    	firstMatchToAdd = firstMatch;
    	lastMatchToAdd = lastMatch;
    	return new SortAutoBalances();
    }
    
    public class SortAutoBalances {
    	@GET
    	@Produces(MediaType.APPLICATION_JSON)
    	public static String sortByDescendingAutoBalances() throws Exception {
    		mainMethod();
    		
    		LinkedHashMap<Integer, Integer> teamAvgPts = new LinkedHashMap<>();
    		
            for (Team team : teams) teamAvgPts.put(team.getTeam_number(), team.getNumTimesBridgeEngagedAuto());
    		
    		LinkedHashMap<Integer, Integer> sortedMap = 
    				teamAvgPts
    				.entrySet()
    				.stream()
    				.sorted((a, b) -> b.getValue().compareTo(a.getValue()))
    				.collect(Collectors.toMap(Map.Entry::getKey, 
    						Map.Entry::getValue, 
    						(c, d) -> c, LinkedHashMap::new));
    		
    		ObjectMapper mapper = new ObjectMapper();
    		ArrayNode rootNode = mapper.createArrayNode();
    		
    		for (Map.Entry<Integer, Integer> entry : sortedMap.entrySet()) {
    			JsonNode childNode = mapper.createObjectNode();
    			((ObjectNode) childNode).put(String.valueOf(entry.getKey()), entry.getValue());
    			rootNode.add(childNode);
    		}
    		
    		return mapper.writeValueAsString(rootNode);
    	}
    }
    
    @Path("sort/descending-tele-balances")
    public SortTeleBalances sortByNumTeleBalances(@QueryParam("comp") String eventKey, @DefaultValue("0") @QueryParam("first") int firstMatch, 
			@DefaultValue("0") @QueryParam("last") int lastMatch) {
    	Main.eventKey = eventKey;
    	firstMatchToAdd = firstMatch;
    	lastMatchToAdd = lastMatch;
    	return new SortTeleBalances();
    }
    
    public class SortTeleBalances {
    	@GET
    	@Produces(MediaType.APPLICATION_JSON)
    	public static String sortByDescendingTeleBalances() throws Exception {
    		mainMethod();
    		
    		LinkedHashMap<Integer, Integer> teamAvgPts = new LinkedHashMap<>();
    		
            for (Team team : teams) teamAvgPts.put(team.getTeam_number(), team.getNumTimesBridgeEngagedTeleOp());
    		
    		LinkedHashMap<Integer, Integer> sortedMap = 
    				teamAvgPts
    				.entrySet()
    				.stream()
    				.sorted((a, b) -> b.getValue().compareTo(a.getValue()))
    				.collect(Collectors.toMap(Map.Entry::getKey, 
    						Map.Entry::getValue, 
    						(c, d) -> c, LinkedHashMap::new));
    		
    		ObjectMapper mapper = new ObjectMapper();
    		ArrayNode rootNode = mapper.createArrayNode();
    		
    		for (Map.Entry<Integer, Integer> entry : sortedMap.entrySet()) {
    			JsonNode childNode = mapper.createObjectNode();
    			((ObjectNode) childNode).put(String.valueOf(entry.getKey()), entry.getValue());
    			rootNode.add(childNode);
    		}
    		
    		return mapper.writeValueAsString(rootNode);
    	}
    }
    
    @Path("team/{team}")
    public GetTeam getTeam(@PathParam("team") String team, @QueryParam("comp") String eventKey) {
    	Main.eventKey = eventKey;
    	return new GetTeam(team);
    }
    
    public class GetTeam {
    	private int teamNum;
    	
    	public GetTeam(String team) {
    		teamNum = Integer.parseInt(team);
    	}
    	
    	@GET
    	@Produces(MediaType.APPLICATION_JSON)
    	public String getTeamData() throws Exception {
    		mainMethod();
    		
    		return gson.toJson(teamHashMap.get(teamNum));
    	}
    }
    
	/*
	 * private static void sortNumAutoBalances() { HashMap<Integer, Integer>
	 * teamAvgPts = new HashMap<>(); for (Team team : teams)
	 * teamAvgPts.put(team.getTeam_number(), team.getNumTimesBridgeEngagedAuto());
	 * teamAvgPts.entrySet().stream()
	 * .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
	 * .forEach(System.out::println); }
	 * 
	 * private static void sortNumTeleOpBalances() { HashMap<Integer, Integer>
	 * teamAvgPts = new HashMap<>(); for (Team team : teams)
	 * teamAvgPts.put(team.getTeam_number(), team.getNumTimesBridgeEngagedTeleOp());
	 * teamAvgPts.entrySet().stream()
	 * .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
	 * .forEach(System.out::println); }
	 */
}