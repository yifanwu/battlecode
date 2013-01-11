package multiBot;

import battlecode.common.*;

public class SoldierDefenseBot extends BaseBot {
	
	public SoldierDefenseBot (RobotController rc, GameConst GC) {
		super(rc, GC);
	}
	
	public void run() throws GameActionException {

		rc.yield();
	}
	
	/** 
	 * decides on the encampment type
	 * currently based on the clock cycles
	 */ 
	private static RobotType chooseEncampmentType() {
		RobotType[] encampmentTypes = {RobotType.SUPPLIER, RobotType.GENERATOR};
		if (Clock.getRoundNum() % 2 == 1) {
			// generator
			return encampmentTypes[1];
		}
		else
			return encampmentTypes[0];
		
	}
}
