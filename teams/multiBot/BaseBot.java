package multiBot;

import battlecode.common.*;

public abstract class BaseBot {
	private static final int MAX_SQUARE_RADIUS = 10000;
	protected RobotController rc;
	protected MapLocation myLoc;
	protected GameConst GC;
	
	public BaseBot(RobotController rc, GameConst GC) {
		this.rc = rc;
		this.myLoc = rc.getLocation();
		this.GC = GC;
	}
	
	public abstract void run() throws GameActionException;
	
	public void loop() {
		while(true) {
			try {
				// Execute turn
				run();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			// End turn
			rc.yield();
		}
	}
	
	//determines which location in arr is closest to target, breaking ties by choosing the lowest x and then y
	protected MapLocation nearestMapLocation(MapLocation arr[], MapLocation target) {
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
		if (best == -1) {
			return null;
		} else {
			return arr[best];
		}
	}
	
	protected MapLocation nearestBotLocation(Robot robots[], MapLocation target) throws GameActionException {
		MapLocation[] locArr = new MapLocation[robots.length];  
		
		for (int i = 0; i<robots.length; i++) {
			locArr[i] = rc.senseRobotInfo(robots[i]).location;
		}
		
		return nearestMapLocation(locArr, target);
	}
	
	protected Direction availableDirection(MapLocation destination) {
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
	
	protected MapLocation findClosestEnemyEncampment() throws GameActionException {
		MapLocation[] nearbyEncampments = rc.senseEncampmentSquares(myLoc, MAX_SQUARE_RADIUS, Team.NEUTRAL);        
        MapLocation closestEncampment = nearestMapLocation(nearbyEncampments, myLoc);
        
        return closestEncampment;
    }
	
	protected MapLocation findClosestEnemyRobot() throws GameActionException {
        Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,MAX_SQUARE_RADIUS, rc.getTeam().opponent());        
        MapLocation closestEnemy = nearestBotLocation(enemyRobots, myLoc);
        
        return closestEnemy;
    }
	
}
