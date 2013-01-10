package draftone;

import battlecode.common.*;

public class RobotPlayer {
	private static final int MAX_SOLDIERS = 10;
	private static RobotController rc;
	private static MapLocation offenseRallyPoint = null; 
	private static MapLocation defenseRallyPoint = null; 
	private static MapLocation enemyHQ = null;
	private static MapLocation homeHQ = null;
//	// private static Robot[] enemiesNearHome = {}; //TODO: needed?
	private static MapLocation closestEnemyNearHome = null;
	private static int totalSoldiers = 0; 
	private static int halfDistBetweenHQ = -1;  //TODO: check if this is actually half
		
	//private static lazyCycle //TODO: lazy!
	
	public static void run(RobotController myRC) {
		rc = myRC;

		offenseRallyPoint = findOffenseRallyPoint();
		defenseRallyPoint = findDefenseRallyPoint();
		if (enemyHQ == null) { 
			enemyHQ = rc.senseEnemyHQLocation();
			// if enemy is null so is home
			homeHQ = rc.senseHQLocation();
			halfDistBetweenHQ = DistBetweenHQ() / 4;
		}

		while(true) {
			
			try{
				if (rc.getType() == RobotType.SOLDIER) {
					int curID = rc.getRobot().getID();
					
					if (Clock.getRoundNum() < 50){
						if (curID % 2 == 1) 
							goToLocation(offenseRallyPoint);
						else
							goToLocation(defenseRallyPoint);
					}
					else {
						if (curID % 2 == 1) 
							offense(curID / 2);
						else 
							defense(curID / 2);
					}
				} 
				else {
					Robot[] enemiesNearHome = rc.senseNearbyGameObjects(Robot.class,1000000,rc.getTeam().opponent());
					closestEnemyNearHome = findClosest(homeHQ, enemiesNearHome);
					hqCode();
				}
			}

			catch (Exception e) {
				System.out.println("caught exception before it killed us:");
				e.printStackTrace();
			}
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
			// else with probability lay mine 
			if (Math.random() < 0.083) {
				rc.layMine();
			}
		}
	}

	private static void offense(int idNum) throws GameActionException { 
		// pass divided by 2
		// stay in enemy half
		if (idNum % 2 == 1) {
			// attack HQ
			goToLocation(enemyHQ); //TODO: optimize			
		} else {
			// gang up
			// findClosest used to find closest enemy to home and closest enemy to offense??
			Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,1000000,rc.getTeam().opponent());
			if (enemyRobots.length > 0) {
				goToLocation(findClosest(rc.getLocation(), enemyRobots)); // TODO: add messaging
			}
			else {
				goToLocation(enemyHQ);
			}
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
	
	private static void goToLocation(MapLocation whereToGo) throws GameActionException {
		int dist = rc.getLocation().distanceSquaredTo(whereToGo);
		if (dist>0&&rc.isActive()){
			Direction dir = rc.getLocation().directionTo(whereToGo);
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction lookingAtCurrently = dir;
			lookAround: for (int d:directionOffsets){
				lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					break lookAround;
				}
			}
			rc.move(lookingAtCurrently);
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
