package multiBot;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class MultipleRalliesBot extends BaseBot {
	public static final int RALLY_INTERVAL = 150;

	public MultipleRalliesBot(RobotController myRc) {
		super(myRc);
	}

	@Override
	public void run() throws GameActionException {
		if (rc.isActive()) {
			if (rc.senseEncampmentSquare(myLoc)) {
				RobotType encampmentType = chooseEncampmentType(Clock.getRoundNum());
				if (rc.getTeamPower() > rc.senseCaptureCost())
				rc.captureEncampment(encampmentType);
			}
			else {
				MapLocation nearestEnemy = findClosestEnemyRobot();
				if (nearestEnemy != null) {
					moveToLocAndDefuseMine(nearestEnemy);
				}
				else {
					MapLocation rallyPoint = findRallyPoint(Clock.getRoundNum());
					moveToLocAndDefuseMine(rallyPoint);
				}
			}
		}
		
	} 
	
	public static MapLocation findRallyPoint(int clockCycle) {
		int x = 0;
		int y = 0;
		
		if (clockCycle < RALLY_INTERVAL) {
			x = (enemyHQ.x + 3 * homeHQ.x) / 4;
			y = (enemyHQ.y + 3 * homeHQ.y) / 4;
			
		}
		else if (clockCycle < 2 * RALLY_INTERVAL) {
			x = (2 * enemyHQ.x + homeHQ.x) / 3;
			y = (2 * enemyHQ.y + homeHQ.y) / 3;
			
		}
		else if (clockCycle < 3 * RALLY_INTERVAL) {
			x = (2 * enemyHQ.x + homeHQ.x) / 3;
			y = (2 * enemyHQ.y + homeHQ.y) / 3;
			
		}
		else {
			x = enemyHQ.x;
			y = enemyHQ.y;
		}
		
		return new MapLocation(x, y);
	}
	
	public static RobotType chooseEncampmentType(int clockCycle) {
		RobotType encampment = null;
		
		if (clockCycle < RALLY_INTERVAL) {
			encampment = RobotType.SUPPLIER;
			
		}
		else if (clockCycle < 2 * RALLY_INTERVAL) {
			encampment = RobotType.GENERATOR;
		}
		else if (clockCycle < 3 * RALLY_INTERVAL) {
			encampment = RobotType.SHIELDS;
			
		}
		else {
			encampment = RobotType.MEDBAY;
		}
		
		return encampment;
	}
}
