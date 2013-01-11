/**
 * 
 */
package multiBot;
import battlecode.common.*;

/**
 * TODO: have more types of soldiers!
 */
public class SoldierBot extends BaseBot{
	
	public SoldierBot (RobotController rc, GameConst GC) {
		super(rc, GC);
		// TODO Auto-generated constructor stub
	}
	
	public void run() throws GameActionException {
		
	}
	
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
