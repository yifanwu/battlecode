package multiBot;

import battlecode.common.*;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class RobotPlayer {
	public static void run(RobotController rc) {
		BaseBot br;
		switch(rc.getType()) {
		case HQ:
			br = new HQBot(rc);
			break;
		case SOLDIER:
			br = new SoldierBot(rc);
			break;
		default:
			br = new EncampBot(rc);
			break;
		}
		
		br.loop();
	}
	
	public static RobotInfo nearestEnemy(RobotController rc, int distThreshold) throws GameActionException {
		// functinos written here will be available to each player file
		return null;
	}
}
