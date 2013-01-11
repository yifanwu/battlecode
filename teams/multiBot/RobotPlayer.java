package multiBot;

import battlecode.common.*;

/**
 */
public class RobotPlayer {
	private static GameConst GC;
	public enum SoldierType {
		OFFENSE,DEFENSE
	}
	
	public static void run(RobotController rc) {
		if (Clock.getRoundNum() == 1) {
			setup(rc);
		}
		
		BaseBot br = null;
		switch(rc.getType()) {
		case HQ:
			br = new HQBot(rc, GC);
			break;
		case SOLDIER:
			SoldierType mySoldierType = getSoldierType(rc.getRobot().getID());
			switch(mySoldierType) {
				case DEFENSE:
					br = new SoldierDefenseBot(rc, GC);			
					break;
				case OFFENSE:
					br = new SoldierOffenseBot(rc, GC);	
					break;					
			}					
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
	
	// ratio: 1:1
	// dependency: odd/even split
	public static SoldierType getSoldierType(int ID) {
		if (ID % 2 == 1) {
			return SoldierType.DEFENSE;
		} else {
			return SoldierType.OFFENSE;
		}			
	}
}
