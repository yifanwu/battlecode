package multiBot;
import battlecode.common.*;

public class EncampBot extends BaseBot{
	
	public EncampBot(RobotController rc, GameConst GC) {
		super(rc, GC);
		// TODO Auto-generated constructor stub

	}
	
	public void run() throws GameActionException {
		if (rc.isActive() && rc.getType() == RobotType.ARTILLERY) {
			//check enemy HQ first
			
			
			
			Robot enemies[] =
				rc.senseNearbyGameObjects(Robot.class, RobotType.ARTILLERY.attackRadiusMaxSquared, rc.getTeam().opponent());
			/*
			MapLocation loc;
			if(rc.canAttackSquare(loc)) {
				
			}
			*/
			
		}
		else {
			sweepAndJam();
		}
		
		
	}
}
