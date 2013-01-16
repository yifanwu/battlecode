package sprintBot;
import battlecode.common.*;

public class HQBot {

	private static MapLocation enemyHQ;
	private static RobotController rc;
	
	public static void run(RobotController myRC) throws GameActionException {
		rc = myRC;
		enemyHQ = rc.senseEnemyHQLocation();
		if (rc.isActive() &&rc.getTeamPower() > 50) {
			Direction dir = rc.getLocation().directionTo(enemyHQ);
			if (rc.canMove(dir)) {
				rc.spawn(dir);
			}
		}
		rc.yield();
	}
}