package it.albertus.router;

import it.albertus.router.console.RouterLoggerConsole;
import it.albertus.router.gui.RouterLoggerGui;

public class RouterLogger {

	/** Unique entry point */
	public static void main(String args[]) {
		if (args.length != 0) {
			RouterLoggerConsole.start(args);
		}
		else {
			RouterLoggerGui.start();
		}
	}

}