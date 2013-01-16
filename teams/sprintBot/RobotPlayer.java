package sprintBot;

import battlecode.common.*;

public class RobotPlayer {

	public static void run(RobotController rc) {
		while (true) {
			try {
				if (rc.getType() == RobotType.SOLDIER) {
					SwarmBot.run(rc);

				}
				else if (rc.getType() == RobotType.HQ) {
					HQBot.run(rc);
				}
			}
			catch (GameActionException e) {
				e.printStackTrace();
			}

			rc.yield();
		}
	}
}
