/**
 * 
 */
package multiBot;
import java.util.HashSet;

import battlecode.common.*;

/**
 * TODO: have more types of soldiers!
 */
public class SoldierOffenseBot extends BaseBot{	
	public SoldierOffenseBot (RobotController rc, GameConst GC) {
		super(rc, GC);
	}
	
	public void run(){
		if (super.VERBOSE) {
			//System.out.println("Offense run called");
		}
		if (rc.isActive()) {
			try {
				offense();
			} catch (GameActionException e) {
				e.printStackTrace();
			}
		}		
	}
	
	private void offense() throws GameActionException {
		
		if (rc.senseEncampmentSquare(rc.getLocation())) {		
			RobotType encampmentType = chooseEncampmentType();
			rc.captureEncampment(encampmentType);		
		} else {
			
			MapLocation closestEncampment = findClosestEnemyEncampment();
			MapLocation closestEnemyRobot = findClosestEnemyRobot();
			
			if (closestEncampment != null) {
				moveToLocAndDefuseMine(closestEncampment);
			} else if (closestEnemyRobot != null) {
				moveToLocAndDefuseMine(closestEnemyRobot);
			} else {
				//
				moveToLocAndDefuseMine(GC.enemyHQ);//rc.senseEnemyHQLocation());
			}
		}
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
		} else {
			return encampmentTypes[0];
		}
	}
	
	/**
	 * Defusing mines 
	 * @param destination
	 * @throws GameActionException
	 */
	protected void moveToLocAndDefuseMine(MapLocation destination) throws GameActionException {
		Direction myDir = super.availableDirection(destination);
		MapLocation nextLocation = rc.getLocation().add(myDir);

        if (rc.senseMine(nextLocation) != null) {
            rc.defuseMine(nextLocation);
        } else {
            rc.move(myDir);
        }
	}		
	
}
