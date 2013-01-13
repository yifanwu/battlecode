package multiBot;

import java.util.Random;

import battlecode.common.*;

public class RobotPlayer {
	
	protected final static boolean VERBOSE = true; 
	
	public enum SoldierType {
		OFFENSE, DEFENSE, ENGULF
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
			mySoldierType = SoldierType.OFFENSE;
			
			switch(mySoldierType) {
				case DEFENSE:
					br = new SoldierDefenseBot(rc);
					//System.out.println("defense bot called");
					break;
				case OFFENSE:
					br = new SoldierOffenseBot(rc);	
					//System.out.println("offense bot called");
					break;
				case ENGULF:
					br = new EngulferSoldier(rc);	
					//System.out.println("engulf bot called");
					break;
				default:
					br = new SoldierOffenseBot(rc);	
					//System.out.println("other bot called");
			}					
			break;
		default:
			br = new EncampBot(rc);
			break;
		}
		
		br.loop();
	}

	public static void setup(RobotController rc) {
		//TODO: add stuff
	}
	
	// ratio: 1:1
	// dependency: odd/even split
	public static SoldierType getSoldierType(int ID) {
//		Random rg = new Random();
//		double rand1 = rg.nextDouble();
//		System.out.println(rand1);
//		
//		if (rand < 0.2) {
//			return SoldierType.DEFENSE;
//		} else if (rand < 0.7) {
//			return SoldierType.OFFENSE;
//		} else {
//			return SoldierType.ENGULF;
//		}
		
		int modded = (Clock.getRoundNum()/10)%5;
		//System.out.println(modded);
		//int modded = ID % 5;		
		if (modded == 1) {
			return SoldierType.DEFENSE;
		} else if (modded == 2 || modded == 3) {
			return SoldierType.OFFENSE;
		} else {
			return SoldierType.ENGULF;
		}
	}
}
