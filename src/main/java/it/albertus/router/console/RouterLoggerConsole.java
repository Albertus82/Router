package it.albertus.router.console;

import java.io.IOException;
import java.util.Map;

import it.albertus.router.engine.RouterData;
import it.albertus.router.engine.RouterLoggerEngine;
import it.albertus.router.engine.Threshold;
import it.albertus.router.resources.Messages;
import it.albertus.router.util.Logger;
import it.albertus.router.util.Logger.Destination;
import it.albertus.router.util.LoggerFactory;
import it.albertus.util.Version;

public class RouterLoggerConsole extends RouterLoggerEngine {

	private static final Logger logger = LoggerFactory.getLogger(RouterLoggerConsole.class);

	public static class Defaults extends RouterLoggerEngine.Defaults {
		public static final boolean CONSOLE_ANIMATION = true;

		private Defaults() {
			throw new IllegalAccessError("Constants class");
		}
	}

	private static final String ARG_HELP = "--help";
	private static final String ARG_CONSOLE = "-c";

	private static final char[] ANIMATION = { '-', '\\', '|', '/' };

	private static Thread uiThread;

	private int lastLogLength = 0;

	/** Entry point for console version */
	public static void start(final String[] args) {
		// Check arguments...
		if (args[0].trim().equalsIgnoreCase(ARG_HELP)) {
			final Version version = Version.getInstance();
			System.out.println(Messages.get("msg.welcome", Messages.get("msg.application.name"), Messages.get("msg.version", version.getNumber(), version.getDate()), Messages.get("msg.website")));
			System.out.println();
			System.out.println(Messages.get("msg.help.usage", ARG_CONSOLE, ARG_HELP));
			System.out.println();
			System.out.println("  " + Messages.get("msg.help.option.console", ARG_CONSOLE));
			System.out.println("  " + Messages.get("msg.help.option.help", ARG_HELP));
		}
		else if (args.length > 1) {
			System.err.println(Messages.get("err.too.many.parameters", args[1]));
			System.out.println(Messages.get("err.try.help", ARG_HELP));
		}
		else if (args[0].trim().equalsIgnoreCase(ARG_CONSOLE)) {
			uiThread = Thread.currentThread();
			// Start RouterLogger in console...
			final RouterLoggerConsole routerLogger = new RouterLoggerConsole();
			try {
				routerLogger.addShutdownHook();
				routerLogger.beforeConnect();
				routerLogger.connect();
				Thread.sleep(Long.MAX_VALUE);
			}
			catch (final InterruptedException ie) {
				logger.debug(ie);
				Thread.currentThread().interrupt();
			}
			catch (final Exception exception) {
				logger.error(exception);
			}
			finally {
				routerLogger.disconnect(true);
				routerLogger.joinPollingThread();
				routerLogger.stopNetworkServices();
				routerLogger.removeShutdownHook();
			}
			routerLogger.printGoodbye();
		}
		else {
			System.err.println(Messages.get("err.unrecognized.option", args[0]));
			System.out.println(Messages.get("err.try.help", ARG_HELP));
		}
	}

	@Override
	protected void showInfo(final RouterData info, final Map<Threshold, String> thresholdsReached) {
		// Scrittura indice dell'iterazione in console...
		final StringBuilder clean = new StringBuilder();
		while (lastLogLength-- > 0) {
			clean.append('\b').append(' ').append('\b');
		}
		final StringBuilder log = new StringBuilder();
		final boolean animate = configuration.getBoolean("console.animation", Defaults.CONSOLE_ANIMATION);
		if (animate) {
			log.append(ANIMATION[getIteration() & 3]).append(' ');
		}
		log.append(getIteration());
		final int iterations = configuration.getInt("logger.iterations", Defaults.ITERATIONS);
		if (iterations > 0) {
			log.append('/').append(iterations);
		}
		log.append(' ');
		if (animate) {
			log.append(ANIMATION[getIteration() & 3]).append(' ');
		}
		// Fine scrittura indice.

		// Scrittura informazioni aggiuntive richieste...
		if (info != null && info.getData() != null && !info.getData().isEmpty()) {
			final StringBuilder infoToShow = new StringBuilder();
			for (String keyToShow : configuration.getConsoleKeysToShow()) {
				if (keyToShow != null && !"".equals(keyToShow.trim())) {
					keyToShow = keyToShow.trim();
					for (final String key : info.getData().keySet()) {
						if (key != null && key.trim().equals(keyToShow)) {
							if (infoToShow.length() == 0) {
								infoToShow.append('[');
							}
							else {
								infoToShow.append(", ");
							}
							infoToShow.append(keyToShow + ": " + info.getData().get(key));
						}
					}
				}
			}
			if (infoToShow.length() != 0) {
				infoToShow.append("] ");
			}
			log.append(infoToShow);
		}
		// Fine scrittura informazioni aggiuntive.

		lastLogLength = log.length();
		out.print(clean.toString() + log.toString());
	}

	@Override
	public void connect() {
		// Avvia thread di interrogazione router...
		if (getReader() != null && getWriter() != null) {
			boolean connect;
			try {
				connect = canConnect();
			}
			catch (final Exception exception) {
				logger.error(exception);
				return;
			}
			if (connect) {
				exit = false;
				pollingThread = new Thread("pollingThread") {
					@Override
					public void run() {
						try {
							outerLoop();
						}
						catch (final Exception exception) {
							logger.error(exception);
							try {
								getReader().disconnect();
							}
							catch (final Exception e) {
								logger.debug(e);
							}
							release();
						}
					}
				};
				pollingThread.start();
			}
			else {
				logger.info(Messages.get("err.operation.not.allowed", getCurrentStatus().getStatus().getDescription()), Destination.CONSOLE);
			}
		}
	}

	@Override
	public void restart() {
		disconnect(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				httpServer.stop();
				joinPollingThread();
				try {
					configuration.reload();
				}
				catch (final IOException ioe) {
					logger.error(ioe);
				}
				mqttClient.disconnect();
				setIteration(FIRST_ITERATION);
				out.println();
				beforeConnect();
				connect();
			}
		}, "resetThread").start();
	}

	@Override
	public void close() {
		if (uiThread != null) {
			uiThread.interrupt();
		}
	}

}
