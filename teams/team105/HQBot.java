package team105;
import battlecode.common.*;

public class HQBot {

	private static MapLocation enemyHQ;
	private static RobotController rc;
	
	public static void run(RobotController myRC) throws GameActionException {
		rc = myRC;
		enemyHQ = rc.senseEnemyHQLocation();
		if (rc.isActive() &&rc.getTeamPower() > 50) {
			Direction dir = getDirForSpawn(enemyHQ);
			if (rc.canMove(dir)) {
				rc.spawn(dir);
			}
		}
		rc.yield();
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
	
}
