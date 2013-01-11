package encampment;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer {
	private static final int MAX_SOLDIERS = 100;
	private static final int RALLY_TIME = 20;
	private static RobotController rc;
	private static MapLocation offenseRallyPoint = null; 
	private static MapLocation defenseRallyPoint = null; 
	private static MapLocation enemyHQ = null;
	private static MapLocation homeHQ = null;
	private static MapLocation closestEnemyNearHome = null;
	private static int totalSoldiers = 0; 
	private static int halfDistBetweenHQ = -1;  //TODO: check if this is actually half
	private static MapLocation[] neutralEncampmentSquares = null;

			
	public static void run(RobotController myRC) {
		rc = myRC;

		offenseRallyPoint = findOffenseRallyPoint();
		defenseRallyPoint = findDefenseRallyPoint();
		
		// only set these values once at the beginning of the game
		if (enemyHQ == null) { 
			enemyHQ = rc.senseEnemyHQLocation();
			homeHQ = rc.senseHQLocation();
//			halfDistBetweenHQ = DistBetweenHQ() / 4;
			try {
				neutralEncampmentSquares = rc.senseEncampmentSquares(homeHQ, 100, Team.NEUTRAL);
			} catch (GameActionException e1) {
				// e1.printStackTrace();
			}	
		}
		
		while(true) {
			try{
				if (rc.getType() == RobotType.SOLDIER && rc.isActive()) {
					int curID = rc.getRobot().getID();
					if (Clock.getRoundNum() < RALLY_TIME) { 
						// capture encampment if on it
						if (rc.senseEncampmentSquare(rc.getLocation())) {
							rc.captureEncampment(chooseEncampmentType());
							rc.yield();
						}
						// go to nearbyEncampmentSquare if nearby
						int neutralEncampmentLen = neutralEncampmentSquares.length;
						if (neutralEncampmentLen > 0) {
							MapLocation anEncampment = neutralEncampmentSquares[curID % neutralEncampmentLen];
							goToLocation(anEncampment);	
						}
						// else go to a rally point
						else {
							// odd is offense, even is defense
							if (curID % 2 == 1) 
								goToLocation(offenseRallyPoint);
							else
								goToLocation(defenseRallyPoint);
						}
					}
					
					else {
						if (curID % 3 != 0) {
							offense(curID / 2);
							rc.yield();
						}
						else 
							defense(curID / 2);
					}
				} 
				else if (rc.getType() == RobotType.HQ){
					hqCode();
					rc.yield();
				}
			}

			catch (Exception e) {
				System.out.println("caught exception before it killed us:");
				System.out.println("round " + Clock.getRoundNum());
				e.printStackTrace();
			}
			rc.yield();

			// necessary? 
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
			goToLocation(defenseRallyPoint);

		}
	}

	private static RobotType chooseEncampmentType() {
		RobotType[] encampmentTypes = {RobotType.SUPPLIER, RobotType.GENERATOR};
		if (Clock.getRoundNum() % 2 == 1) {
			// generator
			return encampmentTypes[1];
		}
		else
			return encampmentTypes[0];
		
	}
	
	private static void offense(int idNum) throws GameActionException { 
		if (rc.senseEncampmentSquare(rc.getLocation())) {
			RobotType encampmentType = chooseEncampmentType();
			rc.captureEncampment(encampmentType);
		}
		else {
			MapLocation closestEncampment = findClosestEnemyEncampment();
			MapLocation closestEnemyRobot = findClosestEnemyRobot();
			if (closestEncampment != null) {
				goToLocation(closestEncampment);
			}
			else if (closestEnemyRobot != null) {
				goToLocation(closestEnemyRobot);
			}
			else {
				goToLocation(enemyHQ);
			}
		}
		rc.yield(); 		
	}
	
//	private static int DistBetweenHQ() {
//		int dist = enemyHQ.distanceSquaredTo(homeHQ);
//		System.out.println("Distance between HQs " + dist);
//		return dist;
//	}
	
	private static MapLocation findClosestRobot(Robot[] robots, MapLocation loc) throws GameActionException {
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
	
	private static void hqCode() throws GameActionException{
		if (rc.isActive()) {
			// Spawn a soldier
			Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			if (rc.canMove(dir) && (totalSoldiers < MAX_SOLDIERS)) {
				rc.spawn(dir);
				totalSoldiers++;
			}
		}
	}
	
	/* goToLocation moves in the direction of a location 
	 * and defuses a mine if its in the way 
	 * */
	private static void goToLocation(MapLocation whereToGo) throws GameActionException {
		int dist = rc.getLocation().distanceSquaredTo(whereToGo);
		if (dist > 0 && rc.isActive()) {
			Direction dir = rc.getLocation().directionTo(whereToGo);
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction lookingAtCurrently = dir;
			for (int d : directionOffsets){
				lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				
				if(rc.canMove(lookingAtCurrently)){
					break;
				}
			}
			
			MapLocation nextLocation = rc.getLocation().add(lookingAtCurrently);

			if (rc.senseMine(nextLocation) != null) {
				rc.defuseMine(nextLocation);
			}
			else {
				rc.move(lookingAtCurrently);
			}
		}
	}
	
	//determines which location in arr is closest to target, breaking ties by choosing the lowest x and then y
	private static MapLocation nearestToLoc(MapLocation arr[], MapLocation target) {
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
		
		if (best != -1) {
			return arr[best];
		}
		return null;
	}
	
	private static MapLocation findClosestEnemyEncampment() throws GameActionException {
		MapLocation rcLocation = rc.getLocation();
		MapLocation[] nearbyEncampments = rc.senseEncampmentSquares(rcLocation, 10000, Team.NEUTRAL);
		
		MapLocation closestEncampment = nearestToLoc(nearbyEncampments, rcLocation);
		return closestEncampment;
	}

	private static MapLocation findClosestEnemyRobot() throws GameActionException {
		MapLocation rcLocation = rc.getLocation();
		Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,1000000,rc.getTeam().opponent());
		
		MapLocation closestEnemy = findClosestRobot(enemyRobots, rcLocation);
		return closestEnemy;
	}
	/*
	 * make sure that we change direction if not movable 
	 */
	private static MapLocation findOffenseRallyPoint() {
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		MapLocation ourLoc = rc.senseHQLocation();
		int x = (enemyLoc.x*ourLoc.x)/2;
		int y = (enemyLoc.y*ourLoc.y)/2;
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

