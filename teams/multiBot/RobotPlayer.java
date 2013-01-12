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
			br = new HQBot(rc);
			break;
		case SOLDIER:
			SoldierType mySoldierType = getSoldierType(rc.getRobot().getID());
			switch(mySoldierType) {
				case DEFENSE:
					//DEFENSE IS STILL BUGG
					br = new SoldierOffenseBot(rc);
					System.out.println("defense bot called");
					break;
				case OFFENSE:
					//TODO: hack
					br = new SoldierOffenseBot(rc);	
					System.out.println("offense bot called");
					break;
				default:
					br = new SoldierOffenseBot(rc);	
					System.out.println("other bot called");

			}					
			break;
		default:
			br = new EncampBot(rc);
			break;
		}
		
		br.loop();
	}

	public static void setup(RobotController rc) {
		//TODO add stuff
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
