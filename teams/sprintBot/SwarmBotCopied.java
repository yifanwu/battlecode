package sprintBot;

import battlecode.common.*;

public class SwarmBotCopied {
	private static final int RALLY_TIME = 200;
	private static MapLocation rallyPoint;
	private static RobotController rc;

	public static void run(RobotController myRC) throws GameActionException {
		rc = myRC;
		if (rc.isActive()) {
			rc.getLocation(); 			
			rallyPoint = findRallyPoint();
			
			Robot[] enemyRobots = rc.senseNearbyGameObjects(Robot.class,1000000,rc.getTeam().opponent());
			if(enemyRobots.length==0){
				if (Clock.getRoundNum() < RALLY_TIME){
					goToLocation(rallyPoint);
				}else{
					goToLocation(rc.senseEnemyHQLocation());
				}
			}else{
				MapLocation closestEnemy = findClosest(enemyRobots);
				goToLocation(closestEnemy);
			}
		
		}	
	}
		
	private static MapLocation findClosest(Robot[] enemyRobots) throws GameActionException {
		int closestDist = 1000000;
		MapLocation closestEnemy=null;
		for (int i=0;i<enemyRobots.length;i++){
			Robot arobot = enemyRobots[i];
			RobotInfo arobotInfo = rc.senseRobotInfo(arobot);
			int dist = arobotInfo.location.distanceSquaredTo(rc.getLocation());
			if (dist<closestDist){
				closestDist = dist;
				closestEnemy = arobotInfo.location;
			}
		}
		return closestEnemy;
	}
	
	private static void goToLocation(MapLocation whereToGo) throws GameActionException {
		int dist = rc.getLocation().distanceSquaredTo(whereToGo);
		if (dist>0&&rc.isActive()){
			Direction dir = rc.getLocation().directionTo(whereToGo);
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction lookingAtCurrently = null;
			lookAround: for (int d:directionOffsets){
				lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					moveOrDefuse(lookingAtCurrently);
					break lookAround;
				}
			}
		}
	}
	
	private static void moveOrDefuse(Direction dir) throws GameActionException{
		MapLocation ahead = rc.getLocation().add(dir);
		if(rc.senseMine(ahead)!= null){
			rc.defuseMine(ahead);
		}else{
			rc.move(dir);			
		}
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
