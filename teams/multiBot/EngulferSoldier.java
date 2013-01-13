package multiBot;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.Robot;
import battlecode.common.RobotController;
import battlecode.common.Team;

public class EngulferSoldier extends BaseBot {


	private static final int LOOKAHEAD_SPACES = 10; 
	private static final int SQUARED_RADIUS = 9;
	private Direction currentDirection;
	
	public EngulferSoldier(RobotController myRc) {
		super(myRc);
		myLoc = rc.getLocation();
	}

	@Override
	public void run() throws GameActionException {
		currentDirection = myLoc.directionTo(enemyHQ);
						
		MapLocation engulfPoint = swarmAhead(currentDirection);
		if (engulfPoint != null) {			
			moveAroundEngulfPoint(engulfPoint);
		}
		else {
			moveToLocAndDefuseMine(enemyHQ);
		}
	}
	
	private void moveAroundEngulfPoint(MapLocation engulfPoint) throws GameActionException {
		// half walk to left
		if (rc.getRobot().getID() % 2 == 0) {
			rc.move(currentDirection.rotateLeft());
		}
		// half walk to right 
		else {
			rc.move(currentDirection.rotateRight());
		}
	}
	
	private MapLocation swarmAhead(Direction rcDirection) {
		MapLocation lookaheadSquare = myLoc.add(rcDirection, LOOKAHEAD_SPACES);
		Team opponent = rc.getTeam().opponent();
		Robot[] enemiesAhead = rc.senseNearbyGameObjects(Robot.class, lookaheadSquare, SQUARED_RADIUS, opponent);
		if (enemiesAhead.length >= SQUARED_RADIUS * 3) {
			return lookaheadSquare;
		}
		return null;
	}


}
