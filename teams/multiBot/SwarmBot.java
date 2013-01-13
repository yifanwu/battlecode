package multiBot;

import battlecode.common.*;

/*
 * 
 * 
 */
public class SwarmBot extends BaseBot {
	private static final boolean DOREPORT = true;
	private static final int RALLY_TIME = 200;
	private static final int TILE_NUM = 8;	
	private static final int ALLIED_CODE = 1;
	private static final int ENEMY_CODE = 10;
	private static final int MAX_BACK = 3;
	private static int backCounter = 0;
	
	
	private static MapLocation rallyPoint;

	private static int[][] neighborArray;
	//TODO: not sure why this is not {0,0}
	private static int[] self = {2,2};
	private static int[][] surroundingIndices = new int[5][5];

	public SwarmBot(RobotController myRc) {
		super(myRc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() throws GameActionException {
		if (rc.isActive()) {
			// initial settings
			myLoc = rc.getLocation(); 	// always fetch the location fresh

			Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,1000000,rc.getTeam().opponent());			
			MapLocation closestEnemyRobot = nearestBotLocation(enemyRobots, myLoc); 
			rallyPoint = findRallyPoint();
			surroundingIndices = initSurroundingIndices(Direction.NORTH);
			
			if (closestEnemyRobot == null) { 			// no enemies nearby
				if (Clock.getRoundNum() < RALLY_TIME) {	// make sure we convene for a certain number.
					moveToLocAndDefuseMine(rallyPoint);
				} else {								// attack otherwise
					moveToLocAndDefuseMine(rc.senseEnemyHQLocation());
				}
			} else { 									//someone spotted
				
				//TODO: smart is buggy right now --- all bots escapes when hit by enemy...
				//smartCountNeighbors(enemyRobots,closestEnemyRobot);				
				moveToLocAndDefuseMine(closestEnemyRobot);
			}			
		}	
	}

	/**
	 * this is the key of this strategy 
	 */
	private static void smartCountNeighbors(Robot[] enemyRobots, MapLocation closestEnemyRobot) throws GameActionException{
		//build a 5 by 5 array of neighboring units (allied vs enemy)
		neighborArray = populateNeighbors(new int[5][5]);
		//get the total number of enemies and allies adjacent to each of the 8 adjacent tiles
		int[] adj = totalAllAdjacent(neighborArray);
		
		//also check your current position
		int me = totalAdjacent(neighborArray,self);
		
		//display the neighbor information to the indicator strings
		rc.setIndicatorString(0, "adjacent: "+intListToString(adj)+" me: "+me);
		//note: if the indicator string says 23, that means 2 enemies and 3 allies.
		int more = 0;
		Direction moreDir = null;
		int less = ENEMY_CODE;
		Direction lessDir = null;
		
		for (int i = 0; i<adj.length; i++) {
			int info = adj[i];
			int enemyNum = info%ENEMY_CODE;
			if (enemyNum >= more) {
				more = enemyNum;
				moreDir = Direction.values()[i%8];
				assert moreDir != null;
			} else if (enemyNum <= less) {
				less = enemyNum;
				lessDir =  Direction.values()[i%8];
				assert lessDir != null;
			}
		}		
		
		// if the number of allies is large, attack
		int alliedNum = me/10;
		Direction toMove = moreDir;
		assert toMove != null;
		System.out.println(toMove);
		
		if (more > alliedNum) {
			backCounter++;
			if (backCounter < MAX_BACK) {
				toMove = lessDir;
			}
		} else {
			backCounter = 0;			
		}
		
		if (rc.canMove(toMove)) {
			rc.move(toMove);
		} else {
			moveToLocAndDefuseMine(closestEnemyRobot);
		}
	}
	
	/** 
	 * initialize the locations with direction hashed into integers and back
	 * all the 8 directions will be filled with the unit vector going to the certain direction 
	 */
	public static int[][] initSurroundingIndices(Direction forward){
		int[][] indices = new int[8][2];		// 8 for eight directions and 2 for (x,y)
		int startOrdinal = forward.ordinal(); 	// ordinal of this enumeration constant 
		for(int i=0;i<8;i++){			
			// values() Returns an array containing the constants of this enum type, in the order they are declared.
			indices[i] = locToIndex(myLoc,myLoc.add(Direction.values()[(i+startOrdinal)%8]),0);
		}
		return indices;
	}
	
	/**
	 * records the information about robots close by
	 * defined by macro
	 */
	public static int[][] populateNeighbors(int[][] array) throws GameActionException{/*788*/
		MapLocation myCurLoc=rc.getLocation(); //TODO: fetch it fresh again?
		Robot[] nearbyRobots = rc.senseNearbyGameObjects(Robot.class,TILE_NUM);
		
		if (DOREPORT) { // Sets one of this robot's 'indicator strings' for debugging purposes.
			rc.setIndicatorString(2, "number of bots: "+nearbyRobots.length);
		}
		
		for (Robot aRobot:nearbyRobots){
			RobotInfo info = rc.senseRobotInfo(aRobot);
			int[] index = locToIndex(myCurLoc,info.location,2);

			// record the close ones
			if(index[0]>=0 && index[0]<=(TILE_NUM/2) && index[1]>=0 && index[1]<=(TILE_NUM/2)){
				if(info.team==rc.getTeam()){
					array[index[0]][index[1]]=ALLIED_CODE;
				} else {
					array[index[0]][index[1]]=ENEMY_CODE;
				}
			}
		}
		return array;
	}
	
	/**
	 * returns the vector of two vectors with an offset
	 */
	public static int[] locToIndex(MapLocation ref, MapLocation test,int offset){/*40*/
		int[] index = new int[2];
		index[0] = test.y-ref.y+offset;
		index[1] = test.x-ref.x+offset;
		return index;
	}
	
	// encodes an array of ints to a string
	public static String intListToString(int[] intList){
		String sofar = "";
		for(int anInt:intList){
			sofar = sofar+anInt+" ";
		}
		return sofar;
	}
	
	public static int[] totalAllAdjacent(int[][] neighbors){/*2454*/
		//TODO compute only on open spaces (for planned movement)
		int[] allAdjacent = new int[TILE_NUM];
		for(int i=0;i<TILE_NUM;i++){
			allAdjacent[i] = totalAdjacent(neighbors,addPoints(self,surroundingIndices[i]));
		}
		return allAdjacent;
	}
	
	public static int totalAdjacent(int[][] neighbors,int[] index){/*270*/
		int total = 0;
		for(int i=0;i<TILE_NUM;i++){
			// that on the tenth digit is enemy and that on single is allies
			total = total+neighbors[index[0]+surroundingIndices[i][0]][index[1]+surroundingIndices[i][1]];
		}
		return total;
	}
	/**
	 * adds two vector/points
	 * @param vector one
	 * @param vector two
	 * @return the sum
	 */
	public static int[] addPoints(int[] p1, int[] p2){/*30*/
		int[] tot = new int[2];
		tot[0] = p1[0]+p2[0];
		tot[1] = p1[1]+p2[1];
		return tot;
	}
	

		
	private static RobotType chooseEncampmentType() {
		RobotType[] encampmentTypes = {RobotType.SUPPLIER, RobotType.GENERATOR};
		if (Clock.getRoundNum() % 2 == 1) {
			// generator
			return encampmentTypes[1];
		} else {
			return encampmentTypes[0];
		}
	}
	/*
	 * turned out that vanilla rallyPoint was better than 
	 * going to encampments 
	 */
	private static MapLocation findRallyPoint() {
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		MapLocation ourLoc = rc.senseHQLocation();
		int x = (enemyLoc.x+3*ourLoc.x)/4;
		int y = (enemyLoc.y+3*ourLoc.y)/4;
		MapLocation rallyPoint = new MapLocation(x,y);
		return rallyPoint;
	}
}
