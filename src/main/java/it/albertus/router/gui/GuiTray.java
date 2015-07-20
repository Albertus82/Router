package it.albertus.router.gui;

import it.albertus.router.RouterLogger;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tray;
import org.eclipse.swt.widgets.TrayItem;

public class GuiTray {

	public GuiTray(final Shell shell) {
		shell.addShellListener(new ShellAdapter() {
			@Override
			public void shellIconified(ShellEvent e) {
				iconify(shell);
			}
		});
	}

	private void iconify(final Shell shell) {
		Display display = Display.getCurrent();
		Tray tray = display.getSystemTray();
		if (tray != null) {
			shell.setVisible(false);
			final TrayItem trayItem = new TrayItem(tray, SWT.NONE);
			trayItem.setImage(GuiImages.ICONS[12]);
			trayItem.setToolTipText(RouterLogger.class.getSimpleName());
			final Menu menu = new Menu(shell, SWT.POP_UP);
			MenuItem menuItem = new MenuItem(menu, SWT.PUSH);
			menuItem.setText("Open");

			menuItem.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					shell.setVisible(true);
					shell.setMinimized(false);
					trayItem.setVisible(false);
				}
			});

			menuItem = new MenuItem(menu, SWT.SEPARATOR);

			menuItem = new MenuItem(menu, SWT.PUSH);
			menuItem.setText("Exit");

			menuItem.addListener(SWT.Selection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					shell.dispose();
				}
			});

			trayItem.addListener(SWT.MenuDetect, new Listener() {
				@Override
				public void handleEvent(Event event) {
					menu.setVisible(true);
				}
			});

			trayItem.addListener(SWT.DefaultSelection, new Listener() {
				@Override
				public void handleEvent(Event event) {
					shell.setVisible(true);
					shell.setMinimized(false);
					trayItem.setVisible(false);
				}
			});

		}
	}

}