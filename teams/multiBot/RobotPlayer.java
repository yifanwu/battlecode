package multiBot;

import battlecode.common.*;
import java.util.HashSet;
import java.util.Set;

/**
 */
public class RobotPlayer {
	private static GameConst GC;
	
	public static void run(RobotController rc) {
		
		if (Clock.getRoundNum() == 1) {
			// do the setup
			setup(rc);
		}
		
		BaseBot br;
		switch(rc.getType()) {
		case HQ:
			br = new HQBot(rc, GC);
			break;
		case SOLDIER:
			//br = new SoldierBot(rc, GC);			

			//TODO: TEMP
			br = new EncampBot(rc, GC);
			
			break;
		default:
			br = new EncampBot(rc, GC);
			break;
		}
		
		br.loop();
	}
	
	public static RobotInfo nearestEnemy(RobotController rc, int distThreshold) throws GameActionException {
		// functinos written here will be available to each player file
		return null;
	}
	
	public static void setup(RobotController rc) {
		GameConst GC = new GameConst(rc.getMapWidth(), rc.senseEnemyHQLocation(), rc.senseHQLocation());
	}
}
