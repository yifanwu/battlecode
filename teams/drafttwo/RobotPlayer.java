package drafttwo;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.logging.*;
import battlecode.common.*;

public class RobotPlayer {
	private static final int MAX_SOLDIERS = 10000;
	private static final int MOVEAWAY = 1;
	private static final int MAXMOVERANGE = 10; //TODO: try to tune this
	private static RobotController rc;
	private static MapLocation offenseRallyPoint = null; 
	private static MapLocation defenseRallyPoint = null; 
	private static MapLocation enemyHQ = null;
	private static MapLocation homeHQ = null;
	//	// private static Robot[] enemiesNearHome = {}; //TODO: needed?
	private static MapLocation closestEnemyNearHome = null;
	private static MapLocation closestFarEnemy = null;
	private static int totalSoldiers = 0; 
	private static int halfDistBetweenHQ = -1;  //TODO: check if this is actually half
	private static boolean isFarEnemyFound = false;

	//private static lazyCycle //TODO: lazy!

	public static void run(RobotController myRC) throws IOException {
		rc = myRC;
		offenseRallyPoint = findOffenseRallyPoint();
		defenseRallyPoint = findDefenseRallyPoint();
		
		if (enemyHQ == null) { 
			enemyHQ = rc.senseEnemyHQLocation();
			// if enemy is null so is home
			homeHQ = rc.senseHQLocation();
			halfDistBetweenHQ = DistBetweenHQ() / 4;
		}
		System.out.println(enemyHQ);
		
		closestEnemyNearHome = enemyHQ;

		//TODO: figure out if code is needed for encampments 
		
		while(true) {

			try{
				if (!rc.isActive()) continue; //Don't execute anything if robot is not active
				if (rc.getType() == RobotType.SOLDIER) {
					int curID = rc.getRobot().getID();					
					MapLocation curLoc = rc.getLocation();
/*
					switch(rc.readBroadcast(curID)){
						case MOVEAWAY:
							rc.move(rc.getLocation().directionTo(homeHQ).opposite());
							break;						
					}
	*/				
					
					if (Clock.getRoundNum() < 50){
						if (curID % 2 == 1) 
							goToLocation(offenseRallyPoint);
						else
							goToLocation(defenseRallyPoint);
					} else {
						if (curID % 2 == 1) {
							offense(curID / 2, curLoc);
						}
						else { 
							defense(curID / 2);
						}
					}
				} 
				else if (rc.getType() == RobotType.HQ){
					Robot[] enemiesNearHome = rc.senseNearbyGameObjects(Robot.class,1000000,rc.getTeam().opponent());
					MapLocation n = findClosest(homeHQ, enemiesNearHome);
					if (n != null)
						closestEnemyNearHome = n;
					hqCode();
				}
			}

			catch (Exception e) {
				System.out.println("caught exception before it killed us:");
				e.printStackTrace();
			}
			
			// CRUCIAL 
			rc.yield();
		}
		
		//logger.close();
		
	}

	private static int encodeLoc(MapLocation loc) {
		// assuming maximum is capped at 1000
		return (loc.x * 1000 + loc.y);		
	}
	
	private static MapLocation decodeMsg (int msg) {
		int y = msg % 1000;
		int x = msg/1000;
		MapLocation result = new MapLocation(x,y);
		return result;
	}

	private static void hqCode() throws GameActionException{
		if (rc.isActive()) {
			// Spawn a soldier
			Direction dir = getDirForSpawn(enemyHQ);
			if (rc.canMove(dir) && (totalSoldiers < MAX_SOLDIERS)) {
				rc.spawn(dir);
				totalSoldiers++;
			}
			/*
			 * Broad casting scheme
			 * 1: 
			 * 2: offense
			 */
			isFarEnemyFound = false;
			int encoded = encodeLoc(closestEnemyNearHome);
			rc.broadcast(2, encoded);
			//rc.broadcast(1, ) TODO: messaging for defense
			//broadcast(data)
			/* make sure that the hq is not blocked by yielding the robots in front */
			Robot[] blockingRobots = rc.senseNearbyGameObjects(Robot.class,2,rc.getTeam());
			System.out.println(blockingRobots.length);
			
			if (blockingRobots.length > 0) {
				
				for (Robot r:blockingRobots) {
					rc.broadcast(r.getID(), MOVEAWAY);
				}
			}
		}
		
		
	}

	/* 
	 * restricted to the home half
	 */
	// do we need to pass Id to this?
	private static void defense(int idNum) throws GameActionException {
		if (closestEnemyNearHome != null && 
				closestEnemyNearHome.distanceSquaredTo(homeHQ) < halfDistBetweenHQ) {
			// attack the closest enemy if enemy cross the line
			goToLocation(closestEnemyNearHome);
		} 
		else {
			MapLocation[] nearbyEncampments = rc.senseEncampmentSquares(homeHQ,
					halfDistBetweenHQ, Team.NEUTRAL);
			
			//TODO: permanently save these neutral encampments
			MapLocation[] defenders = getNearbyDefenders(homeHQ, halfDistBetweenHQ);
			
			boolean closest = false;
			MapLocation dest = null;
			// go to encampment if closest
			if (defenders.length > 0) {
				for (MapLocation eloc: nearbyEncampments) {
					int i = nearestToLoc(defenders, eloc);
					if (defenders[i].distanceSquaredTo(eloc) > rc.getLocation().distanceSquaredTo(eloc)) {
						closest = true;
						dest = eloc;
						break;
						//this soldier is closest -- needs to go to destination
					}
				}
			}
			if(closest) {
				goToLocation(dest); //TODO: maybe make this move until reached destination
				
				//move encampment logic to top
				if (rc.senseEncampmentSquare(rc.getLocation())) { //bug if already moved this turn
					rc.captureEncampment(RobotType.SUPPLIER); // make encampment a supplier
				}
			}
			else if (rc.senseMine(rc.getLocation())==null) // place mine if possible
				rc.layMine();
			else { //move in random direction
				Direction dir = Direction.values()[(int)(Math.random()*8)];
				if(rc.canMove(dir)) {
					rc.move(dir);
            	}
        	}
		}
	}
	
	//determines which x in arr is closest to target, breaking ties by choosing the lowest x and then y
	private static int nearestToLoc(MapLocation arr[], MapLocation target) {
		int best = -1;
		int bestDist = -1;
		for (int i=0;i<arr.length;i++) {
			int dist = arr[i].distanceSquaredTo(target);
			if ((bestDist == -1) || (bestDist > dist) || (bestDist == dist && arr[best].x > arr[i].x) ||
					(bestDist == dist && arr[best].x == arr[i].x && arr[best].y > arr[i].y)) {
				bestDist = dist;
				best = i;
			}
		}
		return best;
	}
	
	private static boolean isDefender(int x) {
		return (x % 2 == 0) ? true : false;
	}
	
	private static void Log(String msg)
	{
		System.out.println("Turn " + Clock.getRoundNum() + ": " + msg);
	}
	
	// gets defenders within radiusSquared of center using defender IDs
	// may have a problem with bytecodes if there are too many allied robots
	// does not give location of self
	private static MapLocation[] getNearbyDefenders(MapLocation center, int radiusSquared) throws GameActionException {
		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, center, radiusSquared, rc.getTeam());
		List<MapLocation> defenders = new ArrayList<MapLocation>();
		int numDefenders = 0;
	
		for (Robot x: allies) {
			//canSense costs 15 bytecodes, but is safer?
			if(rc.canSenseObject(x)) {
				RobotInfo info = rc.senseRobotInfo(x);
				if (info.type == RobotType.SOLDIER && isDefender(x.getID())) {	
					defenders.add(info.location);
					numDefenders++;
				}
			}
		}
		
		MapLocation[] temp = new MapLocation[numDefenders]; //used to call toArray
		return defenders.toArray(temp);
	}

	private static void offense(int idNum, MapLocation curLoc) throws GameActionException { 
		// pass divided by 2
		// stay in enemy half		
		if (idNum % 2 == 1) {
			// attack HQ
			goToLocation(enemyHQ); //TODO: optimize			
		} else {
			// gang up			
			// for broadcasting purposes
			if (!isFarEnemyFound) {				
				Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,1000000,rc.getTeam().opponent());
				if (enemyRobots.length > 0) {
					closestFarEnemy = findClosest(curLoc, enemyRobots);
				} else {
					closestFarEnemy = enemyHQ;
				}
				isFarEnemyFound = true;
			}						
			// then get over there
			goToLocation(decodeMsg(rc.readBroadcast(2)));
		}

	}

	private static int DistBetweenHQ() {
		int dist = enemyHQ.distanceSquaredTo(homeHQ);
		System.out.println("Distance between HQs " + dist);
		return dist;
	}

	private static MapLocation findClosest(MapLocation loc, Robot[] robots) throws GameActionException {
		int closestDist = 1000000;
		MapLocation closestEnemy = null;

		for (Robot r : robots) {
			RobotInfo aRobotInfo = rc.senseRobotInfo(r);
			int dist = aRobotInfo.location.distanceSquaredTo(rc.getLocation());

			if (dist < closestDist) {
				closestDist = dist;
				closestEnemy = aRobotInfo.location;	
			}
		}

		return closestEnemy;
	}

	private static Direction getDirForSpawn(MapLocation enemyHQ) {
		// randomize the direction
		double rand = Math.random();
		Direction dir = rc.getLocation().directionTo(enemyHQ);
		if (rand < 0.25) {
			dir = dir.rotateRight();
		} else if (rand < 0.5) {
			dir = dir.rotateLeft();							
		}
		return dir;
	}

	/*
	 * TANGENT direction finding algorithm
	 */
	private static void goToLocation(MapLocation whereToGo) throws GameActionException {
		int dist = rc.getLocation().distanceSquaredTo(whereToGo);
		if (dist>0 && rc.isActive()){
			int midX = (rc.getLocation().x + whereToGo.x)/2;
			int midY = (rc.getLocation().y + whereToGo.y)/2;

			MapLocation midLoc = new MapLocation(midX,midY); 
			
			if (dist < MAXMOVERANGE) {
				MapLocation[] mines = rc.senseMineLocations(midLoc, MAXMOVERANGE, rc.getTeam().opponent());
				if (mines.length > 0) {
					// hard coded to move left
					// not strictly tangent
					whereToGo = new MapLocation(midX+MAXMOVERANGE, midY);
				}								
			} else {
				whereToGo = midLoc;
			}		
			
			Direction dir = rc.getLocation().directionTo(whereToGo);
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction lookingAtCurrently = dir;
			lookAround: for (int d:directionOffsets){
				lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					rc.move(lookingAtCurrently);
					break lookAround;
				}
			}						
		}
	}

	/*
	 * make sure that we change direction if not movable 
	 */

	private static MapLocation findOffenseRallyPoint() {
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		MapLocation ourLoc = rc.senseHQLocation();
		int x = (enemyLoc.x+3*ourLoc.x)/4;
		int y = (enemyLoc.y+3*ourLoc.y)/4;
		MapLocation rallyPoint = new MapLocation(x,y);
		return rallyPoint;
	}

	private static MapLocation findDefenseRallyPoint() {
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		MapLocation ourLoc = rc.senseHQLocation();
		int x = (enemyLoc.x+2*ourLoc.x)/3;
		int y = (enemyLoc.y+2*ourLoc.y)/3;
		MapLocation rallyPoint = new MapLocation(x,y);
		return rallyPoint;
	}
}