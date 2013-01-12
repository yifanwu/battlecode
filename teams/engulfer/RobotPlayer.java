package engulfer;

import battlecode.common.*;

public class RobotPlayer{
	
	private static RobotController rc;
	private static MapLocation rallyPoint;
	private static final int LOOKAHEAD_SPACES = 8; 
	private static final int SQUARED_RADIUS = 16;
	private static Direction currentDirection;
	private static int engulfBeginCycle = 0;
	private static MapLocation engulfPoint = null;
	
	
	public static void run(RobotController myRC){
		rc = myRC;
		rallyPoint = findRallyPoint();
		while(true){
			try{
				if (rc.getType()==RobotType.SOLDIER){
					
					if (Clock.getRoundNum()<200){
						goToLocation(rallyPoint);
					}else{
						
						int loc = rc.readBroadcast(Clock.getRoundNum());
						
						if (loc > 0) {
							MapLocation mLoc = new MapLocation((loc / 100), (loc % 100));
							engulfBeginCycle = Clock.getRoundNum();
							moveAroundEngulfPoint(mLoc);
							System.out.println("Receiving");

						}
						else {
							currentDirection = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
							engulfPoint = swarmAhead(currentDirection);
							int clocksSinceEngulf = Clock.getRoundNum() - engulfBeginCycle;
							
							if (engulfPoint != null) {	
								rc.broadcast(Clock.getRoundNum() + 1, engulfPoint.x * 100 + engulfPoint.y);
								System.out.println("Broadcasting");
							}
							else if (clocksSinceEngulf < 2) {
								specialtyStep();
								
							}
							else if (clocksSinceEngulf < 4) {
								moveAroundEngulfPoint(new MapLocation(17, 17));

							}
							else {
								moveToLocAndDefuseMine(rc.senseEnemyHQLocation());
							}
						}
					}
				}else{
					hqCode();
				}
			}catch (Exception e){
				System.out.println("caught exception before it killed us:");
				e.printStackTrace();
			}
			rc.yield();
		}
	}
	
	private static void specialtyStep() {
		MapLocation a = new MapLocation(17, 17);
		MapLocation b = rc.senseHQLocation();
		MapLocation c = rc.getLocation();
		
		int relDistToLine = (b.x - a.x) * (c.y - a.y) 
				- (b.y - a.y) * (c.x - a.x);
	
		System.out.println("Rel Dist : " + relDistToLine);
		
	}

	private static boolean isLeft(MapLocation aLine, MapLocation bLine, MapLocation point) {
		return ((bLine.x - aLine.x) * (point.y - aLine.y) 
				- (bLine.y - aLine.y) * (point.x - aLine.x)) > 0;
	}
	
	private static void goToLocation(MapLocation whereToGo) throws GameActionException {
		int dist = rc.getLocation().distanceSquaredTo(whereToGo);
		if (dist>0&&rc.isActive()){
			Direction dir = rc.getLocation().directionTo(whereToGo);
			int[] directionOffsets = {0,1,-1,2,-2};
			Direction lookingAtCurrently = dir;
			lookAround: for (int d:directionOffsets){
				lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
				if(rc.canMove(lookingAtCurrently)){
					break lookAround;
				}
			}
			if (rc.canMove(lookingAtCurrently)) 
				rc.move(lookingAtCurrently);
		}
	}

	private static MapLocation findRallyPoint() {
		MapLocation enemyLoc = rc.senseEnemyHQLocation();
		MapLocation ourLoc = rc.senseHQLocation();
		int x = (enemyLoc.x+3*ourLoc.x)/4;
		int y = (enemyLoc.y+3*ourLoc.y)/4;
		MapLocation rallyPoint = new MapLocation(x,y);
		return rallyPoint;
	}

	public static void hqCode() throws GameActionException{
		if (rc.isActive()) {
			// Spawn a soldier
			Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			if (rc.canMove(dir))
				rc.spawn(dir);
		}
	}
	
	private static void moveAroundEngulfPoint(MapLocation engulfPoint) throws GameActionException {
//		System.out.println("ENGULFING");
		// half walk to left
		if (isLeft(engulfPoint, rc.senseHQLocation(), rc.getLocation())) {
//			System.out.println("Engulfing Left " + rc.getLocation() + " | Round " + Clock.getRoundNum());
			Direction moveDir = currentDirection.rotateLeft();
			if (rc.canMove(moveDir)) {
				rc.move(currentDirection.rotateLeft());
			}
		}
		// half walk to right 
		else {
//			System.out.println("Engulfing right " + rc.getLocation() + " | Round " + Clock.getRoundNum());
			Direction moveDir = currentDirection.rotateRight();
			if (rc.canMove(moveDir)) {
				rc.move(currentDirection.rotateRight());
			}
		}
	}
	
	private static MapLocation swarmAhead(Direction rcDirection) {
		MapLocation lookaheadSquare = rc.getLocation().add(rcDirection, LOOKAHEAD_SPACES);
		Team opponent = rc.getTeam().opponent();
		Robot[] enemiesAhead = rc.senseNearbyGameObjects(Robot.class, lookaheadSquare, SQUARED_RADIUS, opponent);
		if (enemiesAhead.length > 0)
//			System.out.println("Clock : " + Clock.getRoundNum() + " | Loc : " + lookaheadSquare + " | ArrSize : " + enemiesAhead.length);

		if (enemiesAhead.length >= 5) {
//			System.out.println("SWARM AHEAD");
			return lookaheadSquare;
		}
		return null;
	}

	protected static void moveToLocAndDefuseMine(MapLocation destination) throws GameActionException {
		Direction myDir = availableDirection(destination);
		if (myDir != null) {
			MapLocation nextLocation = rc.getLocation().add(myDir);
	
	        if (rc.senseMine(nextLocation) != null) {
	            rc.defuseMine(nextLocation);
	        } else {
	        	if (rc.canMove(myDir))
	        		rc.move(myDir);
	        }
		}
	}	
	
	protected static Direction availableDirection(MapLocation destination) {
		Direction lookingAtCurrently = null;

		int dist = rc.getLocation().distanceSquaredTo(destination);
        if (dist > 0 && rc.isActive()) {
            Direction dir = rc.getLocation().directionTo(destination);
            int[] directionOffsets = {0,1,-1,2,-2};
            lookingAtCurrently = dir;
            for (int d : directionOffsets){
                lookingAtCurrently = Direction.values()[(dir.ordinal()+d+8)%8];
                
                if(rc.canMove(lookingAtCurrently)){
                    break;
                }
            }
        }
        
        return lookingAtCurrently;
	}

	
}