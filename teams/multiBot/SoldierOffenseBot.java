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
			
			if (rc.getTeamPower() > rc.senseCaptureCost()) {
				rc.captureEncampment(encampmentType);
			}
		} else {
			
			MapLocation closestEncampment = findClosestEnemyEncampment();
			MapLocation closestEnemyRobot = findClosestEnemyRobot();
			
			if (closestEncampment != null) {
				moveToLocAndDiffuseMine(closestEncampment);
			} else if (closestEnemyRobot != null) {
				moveToLocAndDiffuseMine(closestEnemyRobot);
			} else {
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
		mineListen();

		for (MapLocation l: enemyMines) {
			if (l != null && l.equals(nextLocation)) {
				rc.defuseMine(nextLocation);
				while (!rc.isActive()) {
					rc.yield();
				}
				mineDefuseReport(myLoc);
				return;
			}
		}			
		if (rc.senseMine(nextLocation) != null) {
			rc.defuseMine(nextLocation);
		} else {
			if (rc.canMove(myDir)) {
				rc.move(myDir);
			}
		}
	}		
	
}
