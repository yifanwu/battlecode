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
				moveToLocAndDefuseMine(closestEncampment);
			} else if (closestEnemyRobot != null) {
				moveToLocAndDefuseMine(closestEnemyRobot);
			} else {
				moveToLocAndDefuseMine(enemyHQ);//rc.senseEnemyHQLocation());
			}
		}
		
		// report the mine to HQ
		if(rc.senseMine(rc.getLocation()) == rc.getTeam().opponent()) {
			mineReport(rc.getLocation());
		}
	}
	
	
	protected static void slopeFieldGenerate() throws GameActionException {
		MapLocation[] encampments = rc.senseEncampmentSquares(rc.getLocation(), 10000, Team.NEUTRAL);
		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam());
		//MapLocation[] enemies = rc.senseNearbyGameObjects(Robot.class, rc.getTeam());
		
		//rc.senseNearbyObjects();
	}
	
	//TODO: method to update encampments
	
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
	
}
