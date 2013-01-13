package multiBot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class SwarmBot extends BaseBot {
	private static final int RALLY_TIME = 150;
	private static MapLocation rallyPoint;

	public SwarmBot(RobotController myRc) {
		super(myRc);
		// TODO Auto-generated constructor stub
	}

	@Override
	public void run() throws GameActionException {
		if (rc.isActive()) {
			MapLocation closestEncampment = findClosestEnemyEncampment();

			if (rc.senseEncampmentSquare(rc.getLocation())) {		
				RobotType encampmentType = chooseEncampmentType();

				if (rc.getTeamPower() > rc.senseCaptureCost()) {
					rc.captureEncampment(encampmentType);
				}
			}
			else if (Clock.getRoundNum() < RALLY_TIME) {
				
//				if (closestEncampment != null) {
//					moveToLocAndDefuseMine(closestEncampment);
//				} 
//				else {
					rallyPoint = findRallyPoint();
					moveToLocAndDefuseMine(rallyPoint);
//				}
			}
			else {
				MapLocation closestEnemyRobot = findClosestEnemyRobot();

				if (closestEnemyRobot != null) {
					moveToLocAndDefuseMine(closestEnemyRobot);
				}
				else {
					moveToLocAndDefuseMine(enemyHQ); //rc.senseEnemyHQLocation());
				}
			}
		}	
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
	
	private static MapLocation findRallyPoint() {
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		MapLocation ourLoc = rc.senseHQLocation();
		int x = (enemyLoc.x+3*ourLoc.x)/4;
		int y = (enemyLoc.y+3*ourLoc.y)/4;
		MapLocation rallyPoint = new MapLocation(x,y);
		return rallyPoint;
	}
}
