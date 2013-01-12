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
	public SoldierOffenseBot (RobotController rc) {
		super(rc);
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
				moveToLocAndDiffuseMine(closestEncampment);
			} else if (closestEnemyRobot != null) {
				moveToLocAndDiffuseMine(closestEnemyRobot);
			} else {
				//
				moveToLocAndDiffuseMine(enemyHQ);//rc.senseEnemyHQLocation());
			}
		}
		
		// report the mine to HQ
		if(rc.senseMine(myLoc) == rc.getTeam().opponent()) {
			mineReport(myLoc);
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
	 * Diffuse mines 
	 * Update: also avoid if next to enemy location
	 * @param destination
	 * @throws GameActionException
	 */
	protected void moveToLocAndDiffuseMine(MapLocation destination) throws GameActionException {
		Direction myDir = availableDirection(destination);
		MapLocation nextLocation = rc.getLocation().add(myDir);
		MapLocation[] enemyMines = mineListen();

		//int mineCounter = 0;
		// avoid stepping into known buggy places 
		//enemyLookup: 
		for (MapLocation l:enemyMines) {
			if (l.equals(nextLocation)) {
				rc.defuseMine(nextLocation);
				return;
				// shouldn't break the loop just to make sure
				//myDir = myDir.rotateRight(); 
				// update nextLocation
				//nextLocation = rc.getLocation().add(myDir);
				//mineCounter++;
			}
			//else {
				//mineCounter = 0;
				//break enemyLookup;
			//}			
		}
		
		// check if surrounded by enemy mines
	/*	if (mineCounter != 0) {
			rc.suicide();
			return;
		} else*/ 
			
		if (rc.senseMine(nextLocation) != null) {
			//report if diffused
			rc.defuseMine(nextLocation);
			//TODO: is there an issue if there needs time to diffuse?
			mineDefuseReport(myLoc);
		} else {
			rc.move(myDir);
		}
	}		
	
}
