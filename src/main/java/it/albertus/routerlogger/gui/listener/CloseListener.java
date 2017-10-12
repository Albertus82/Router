package it.albertus.routerlogger.gui.listener;

import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.events.ShellListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import it.albertus.routerlogger.gui.CloseDialog;

public class CloseListener implements Listener, SelectionListener, ShellListener {

	private final IShellProvider provider;

	public CloseListener(final IShellProvider provider) {
		this.provider = provider;
	}

	private boolean canClose() {
		return !CloseDialog.mustShow() || CloseDialog.open(provider.getShell()) == SWT.YES;
	}

	private void disposeShellAndDisplay() {
		provider.getShell().dispose();
		final Display display = Display.getCurrent();
		if (display != null) {
			display.dispose(); // Fix close not working on Windows 10 when iconified
		}
	}

	/* OS X Menu */
	@Override
	public void handleEvent(final Event event) {
		if (canClose()) {
			disposeShellAndDisplay();
		}
		else if (event != null) {
			event.doit = false;

		}
	}

	/* Menu */
	@Override
	public void widgetSelected(final SelectionEvent event) {
		if (canClose()) {
			disposeShellAndDisplay();
		}
		else if (event != null) {
			event.doit = false;
		}
	}

	/* Shell close command */
	@Override
	public void shellClosed(final ShellEvent event) {
		if (canClose()) {
			disposeShellAndDisplay();
		}
		else if (event != null) {
			event.doit = false;
		}
	}

	@Override
	public void shellActivated(final ShellEvent event) {/* Ignore */}

	@Override
	public void shellDeactivated(final ShellEvent event) {/* Ignore */}

	@Override
	public void shellDeiconified(final ShellEvent event) {/* Ignore */}

	@Override
	public void shellIconified(final ShellEvent event) {/* Ignore */}

	@Override
	public void widgetDefaultSelected(final SelectionEvent event) {/* Ignore */}

}
