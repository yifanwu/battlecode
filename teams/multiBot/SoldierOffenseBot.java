/**
 * 
 */
package multiBot;
import battlecode.common.*;

/**
 * TODO: have more types of soldiers!
 */
public class SoldierOffenseBot extends BaseBot{
	
	public SoldierOffenseBot (RobotController rc, GameConst GC) {
		super(rc, GC);
	}
	
	public void run() throws GameActionException {

		rc.yield();
		
		
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
