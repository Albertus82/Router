package it.albertus.router.gui;

import it.albertus.jface.SwtThreadExecutor;
import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.gui.listener.TextConsoleDisposeListener;
import it.albertus.router.resources.Resources;
import it.albertus.util.Configuration;
import it.albertus.util.NewLine;
import it.albertus.util.SystemConsole;

import java.io.PrintStream;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Text;

public class TextConsole extends SystemConsole {

	protected static final String newLine = NewLine.SYSTEM_LINE_SEPARATOR;

	public interface Defaults {
		int GUI_CONSOLE_MAX_CHARS = 100000;
	}

	protected Scrollable scrollable = null;

	protected TextConsole(final Composite parent, final Object layoutData) {
		if (this.scrollable == null || this.scrollable.isDisposed()) {
			createText(parent);
			scrollable.setLayoutData(layoutData);
			scrollable.setFont(JFaceResources.getTextFont());
			if (SWT.getPlatform().toLowerCase().startsWith("win")) {
				scrollable.setBackground(scrollable.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
			}
			redirectStreams(); // SystemConsole & System.out will print on this SWT Text Widget. 
		}
		else {
			throw new IllegalStateException(Resources.get("err.already.initialized", this.getClass().getSimpleName()));
		}
	}

	protected void createText(final Composite parent) {
		scrollable = new Text(parent, SWT.BORDER | SWT.READ_ONLY | SWT.V_SCROLL | SWT.H_SCROLL);
	}

	protected void redirectStreams() {
		scrollable.addDisposeListener(new TextConsoleDisposeListener(sysout, syserr));
		final PrintStream ps = new PrintStream(this);
		try {
			System.setOut(ps);
			System.setErr(ps);
		}
		catch (final Exception e) {
			e.printStackTrace();
		}
	}

	protected final Configuration configuration = RouterLoggerConfiguration.getInstance();

	private StringBuilder buffer = new StringBuilder();

	@Override
	public synchronized void write(final int b) {
		buffer.append((char) b);
		if (b == newLine.charAt(newLine.length() - 1)) {
			print(buffer.toString(), true);
			buffer = new StringBuilder();
		}
	}

	public void clear() {
		getText().setText("");
	}

	protected void failSafePrint(final String value) {
		System.out.print(value);
		updatePosition(value);
	}

	protected void doPrint(final String value) {
		int maxChars;
		try {
			maxChars = configuration.getInt("gui.console.max.chars");
		}
		catch (final Exception exception) {
			maxChars = Defaults.GUI_CONSOLE_MAX_CHARS;
		}
		if (getText().getCharCount() < maxChars) {
			getText().append(value);
		}
		else {
			getText().setText(value.startsWith(newLine) ? value.substring(newLine.length()) : value);
		}
		getText().setTopIndex(getText().getLineCount() - 1);
		updatePosition(value);
	}

	@Override
	public void print(String value) {
		// Dealing with null argument...
		final String toPrint;
		if (value == null) {
			toPrint = String.valueOf(value);
		}
		else {
			toPrint = value;
		}

		// Actual print... (async avoids deadlocks)
		new SwtThreadExecutor(scrollable, true) {
			@Override
			protected void run() {
				doPrint(toPrint);
			}

			@Override
			protected void onError(final Exception exception) {
				failSafePrint(toPrint);
			}
		}.start();
	}

	@Override
	public void print(char array[]) {
		print(String.valueOf(array));
	}

	@Override
	public void println() {
		print(newLine);
		newLine();
	}

	@Override
	public void println(String value) {
		print(value + newLine);
		newLine();
	}

	@Override
	public void println(Object value) {
		print(String.valueOf(value) + newLine);
		newLine();
	}

	@Override
	public void println(boolean value) {
		print(String.valueOf(value) + newLine);
		newLine();
	}

	@Override
	public void println(char value) {
		print(String.valueOf(value) + newLine);
		newLine();
	}

	@Override
	public void println(int value) {
		print(String.valueOf(value) + newLine);
		newLine();
	}

	@Override
	public void println(long value) {
		print(String.valueOf(value) + newLine);
		newLine();
	}

	@Override
	public void println(float value) {
		print(String.valueOf(value) + newLine);
		newLine();
	}

	@Override
	public void println(double value) {
		print(String.valueOf(value) + newLine);
		newLine();
	}

	@Override
	public void println(char array[]) {
		print(String.valueOf(array) + newLine);
		newLine();
	}

	public Text getText() {
		return (Text) scrollable;
	}

}
