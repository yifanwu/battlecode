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
	protected static double AllyWeight = -0.5;
	protected static double EncampmentWeight = 4;
	
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
			//vectorMove();
			MapLocation closestEncampment = findClosestEnemyEncampment();
			MapLocation closestEnemyRobot = findClosestEnemyRobot();
			
			if (closestEncampment != null) {
				moveToLocAndDefuseMine(closestEncampment);
			} else if (closestEnemyRobot != null) {
				moveToLocAndDefuseMine(closestEnemyRobot);
			} else {
				moveToLocAndDefuseMine(enemyHQ); //rc.senseEnemyHQLocation());
			}
		}
		
		// report the mine to HQ
		if(rc.senseMine(rc.getLocation()) == rc.getTeam().opponent()) {
			mineReport(rc.getLocation());
		}
	}
	
	//TODO: offensive algorithm
	
	
	//Moves based on the encampments and allied units seen, as well as their distances
	protected static void vectorMove() throws GameActionException {
		MapLocation currentLoc = rc.getLocation();
		MapLocation dest = slopeFieldDirection();
		Direction dir;
		if (dest.equals(currentLoc)) {
            dir = Direction.values()[(int)(Math.random()*8)];
            rc.setIndicatorString(0, "Random move");
		}
		else {
			dir = currentLoc.directionTo(dest);
			rc.setIndicatorString(0, "Moved: " +dir.toString());
		}
		if (!defuseMineIfThere(dir) && rc.canMove(dir)) {
			rc.move(dir);
		}
	}
	
	protected static MapLocation slopeFieldDirection() throws GameActionException {
		MapLocation currentLoc = rc.getLocation();

		MapLocation[] encampments = rc.senseEncampmentSquares(currentLoc, 10000, Team.NEUTRAL);
		Robot[] allies = rc.senseNearbyGameObjects(Robot.class, 10000, rc.getTeam());

		double sumX = 0;
		double sumY = 0;
		
		for (Robot ally: allies) {
			RobotInfo info = rc.senseRobotInfo(ally);
			if (info.type != RobotType.SOLDIER)
				continue;
			MapLocation loc = info.location;
			point p = computeWeightedVector(currentLoc.x, currentLoc.y, loc.x, loc.y, AllyWeight);
			sumX += p.x;
			sumY += p.y;
		}
		
		for (MapLocation loc: encampments) {
			point p = computeWeightedVector(currentLoc.x, currentLoc.y, loc.x, loc.y, EncampmentWeight);
			sumX += p.x;
			sumY += p.y;
		}
		
		MapLocation enemyHQ = rc.senseEnemyHQLocation();
		point p = computeWeightedVector(currentLoc.x, currentLoc.y, enemyHQ.x, enemyHQ.y, 5);
		sumX += p.x;
		sumY += p.y;
		//Get Direction
		sumX *= 100;
		sumY *= 100;
		
		MapLocation dest = new MapLocation((int)sumX, (int)sumY);
		System.out.println(dest.toString());
		return dest;
	}
	
	protected static point computeWeightedVector(int centerX, int centerY, int x, int y, double weight) {
		vector v = new vector(centerX, centerY, x, y);
		point p = new point(weight*(x-centerX)/v.normsq, weight*(y-centerY)/v.normsq);
		return p;
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
	
}