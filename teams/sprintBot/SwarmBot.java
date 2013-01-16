package sprintBot;

import battlecode.common.*;

public class SwarmBot {
	private static MapLocation rallyPoint;
	private static MapLocation myLoc;
	private static RobotController rc;
	private static final int MAX_SQUARE_RADIUS = 10;

	public static void run(RobotController myRC) throws GameActionException {
		rc = myRC;
		if (rc.isActive()) {
			myLoc = rc.getLocation(); 			

			MapLocation closestEnemyRobot = findClosestEnemyRobot(); 
			rallyPoint = findRallyPoint();
			
			if (rc.senseEncampmentSquare(myLoc) && rc.getTeamPower() > rc.senseCaptureCost()) {
				if (Clock.getRoundNum() < 200) 
					rc.captureEncampment(RobotType.SUPPLIER);
				else 
					rc.captureEncampment(RobotType.GENERATOR);
			}
			else if (closestEnemyRobot != null) {
				moveToLocAndDefuseMine(closestEnemyRobot);
			}
			else if (Clock.getRoundNum() < 500 || Clock.getRoundNum() > 1300 && Clock.getRoundNum() < 1600) {
				if (rc.getLocation().distanceSquaredTo(rallyPoint) < 12) 
					rc.layMine();
 
				moveToLocAndDefuseMine(rallyPoint);
			}
			else {
				moveToLocAndDefuseMine(rc.senseEnemyHQLocation());
			}
		}	
	}
	
	protected static Direction availableDirection(MapLocation destination) {
		Direction lookingAtCurrently = null;
		
		int dist = rc.getLocation().distanceSquaredTo(destination);
        if (dist > 0 && rc.isActive()) {
            Direction dir = rc.getLocation().directionTo(destination);
            int[] directionOffsets = {0,1,-1,2,-2};
            lookingAtCurrently = dir;
            for (int d : directionOffsets){
                lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
                
                if(rc.canMove(lookingAtCurrently)){
                    break;
                }
            }
        }
        
        return lookingAtCurrently;
	}
	protected static void moveToLocAndDefuseMine(MapLocation destination) throws GameActionException {
		Direction myDir = availableDirection(destination);
		if (myDir == null)
			return; 
		
		if(!defuseMineIfThere(myDir) && rc.canMove(myDir)) {
			rc.move(myDir);
		}
	}
	
	protected static boolean defuseMineIfThere(Direction dir) throws GameActionException {
		MapLocation nextLocation = rc.getLocation().add(dir);
		
		if (rc.senseMine(nextLocation) == Team.NEUTRAL) {
			rc.defuseMine(nextLocation);
			return true;
		}
		
		return false;
	}
	
	protected static MapLocation findClosestEnemyRobot() throws GameActionException {
        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,MAX_SQUARE_RADIUS, rc.getTeam().opponent());        
        if (enemyRobots.length == 0) {
        	return null;
        }
        
        MapLocation closestEnemy = nearestBotLocation(enemyRobots, myLoc);
        return closestEnemy;
    }
	
	
	protected static MapLocation nearestMapLocation(MapLocation arr[], MapLocation target) {
		int best = -1;
		int bestDist = -1;
		for (int i = 0; i < arr.length; i++) {
			int dist = arr[i].distanceSquaredTo(target);
			if ((bestDist == -1) || (bestDist > dist) || (bestDist == dist && arr[best].x > arr[i].x) ||
					(bestDist == dist && arr[best].x == arr[i].x && arr[best].y > arr[i].y)) {
				bestDist = dist;
				best = i;
			}
		}
		if (best == -1) {
			return null;
		} else {
			return arr[best];
		}
	}
	
	protected static MapLocation nearestBotLocation(Robot robots[], MapLocation target) throws GameActionException {
		MapLocation[] locArr = new MapLocation[robots.length];  
		
		for (int i = 0; i<robots.length; i++) {
			if (rc.canSenseObject(robots[i])) {
				locArr[i] = rc.senseRobotInfo(robots[i]).location;
			}
		}
		
		return nearestMapLocation(locArr, target);
	}

	private static MapLocation findRallyPoint() {
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		MapLocation ourLoc = rc.senseHQLocation();
		int x = (enemyLoc.x+3*ourLoc.x)/4;
		int y = (enemyLoc.y+3*ourLoc.y)/4;
		MapLocation rallyPoint = new MapLocation(x,y);
		return rallyPoint;
	}
}
