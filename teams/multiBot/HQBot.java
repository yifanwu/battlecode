package multiBot;
import battlecode.common.*;

public class HQBot extends BaseBot{

	//declare local variables
	
	public HQBot(RobotController rc, GameConst GC) {
		super(rc, GC);
		//code to execute one time
	}
	
	public void run() throws GameActionException {
		//code to execute for the whole match
		//TODO: dummy righ now
		if (rc.isActive()) {
			// Spawn a soldier
			Direction dir = rc.getLocation().directionTo(rc.senseEnemyHQLocation());
			if (rc.canMove(dir)) {
				rc.spawn(dir);
			}
		}
		
		rc.yield();
	}
	
	
}
