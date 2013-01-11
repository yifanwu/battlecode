package multiBot;

import battlecode.common.*;

/**
 */
public class RobotPlayer {
	private static GameConst GC;
	
	public static void run(RobotController rc) {
		if (Clock.getRoundNum() == 1) {
			setup(rc);
		}
		
		BaseBot br;
		switch(rc.getType()) {
		case HQ:
			br = new HQBot(rc, GC);
			break;
		case SOLDIER:
			br = new SoldierBot(rc, GC);			
			break;
		default:
			br = new EncampBot(rc, GC);
			break;
		}
		
		br.loop();
	}

	public static void setup(RobotController rc) {
		GC = new GameConst(rc.getMapWidth(), rc.senseEnemyHQLocation(), rc.senseHQLocation());
	}
}
