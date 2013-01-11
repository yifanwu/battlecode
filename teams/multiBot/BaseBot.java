package multiBot;

import battlecode.common.*;

public abstract class BaseBot {
	RobotController rc;
	int width;
	
	public BaseBot(RobotController rc) {
		this.rc = rc;
		width = rc.getMapWidth();
	}
	
	public abstract void run() throws GameActionException;
	
	public void loop() {
		while(true) {
			try {
				// Execute turn
				run();
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// End turn
			rc.yield();
		}
	}
}
