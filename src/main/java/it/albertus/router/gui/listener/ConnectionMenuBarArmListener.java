package it.albertus.router.gui.listener;

import it.albertus.router.gui.RouterLoggerGui;
import it.albertus.util.ConfigurationException;

import org.eclipse.swt.events.ArmEvent;
import org.eclipse.swt.events.ArmListener;
import org.eclipse.swt.widgets.MenuItem;

/**
 * Attenzione: disabilitando gli elementi dei menu, vengono automaticamente
 * disabilitati anche i relativi acceleratori.
 */
public class ConnectionMenuBarArmListener implements ArmListener {

	private final RouterLoggerGui gui;

	public ConnectionMenuBarArmListener(final RouterLoggerGui gui) {
		this.gui = gui;
	}

	@Override
	public void widgetArmed(ArmEvent e) {
		final MenuItem connectMenuItem = gui.getMenuBar().getConnectionConnectItem();
		boolean connectEnabled;
		try {
			connectEnabled = gui.canConnect();
		}
		catch (final ConfigurationException ce) {
			connectEnabled = false;
		}
		connectMenuItem.setEnabled(connectEnabled);

		final MenuItem disconnectMenuItem = gui.getMenuBar().getConnectionDisconnectItem();
		disconnectMenuItem.setEnabled(gui.canDisconnect());
	}

}
