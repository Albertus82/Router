package it.albertus.router.gui.preference.page;

import it.albertus.router.engine.RouterLoggerConfiguration;
import it.albertus.router.gui.preference.Preference;
import it.albertus.router.resources.Resources;
import it.albertus.router.util.Logger;
import it.albertus.util.NewLine;

import java.io.IOException;
import java.io.OutputStream;
import java.util.EnumMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.PreferenceStore;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

public abstract class BasePreferencePage extends FieldEditorPreferencePage {

	private final Map<Preference, FieldEditor> fieldEditorMap = new EnumMap<Preference, FieldEditor>(Preference.class);

	private Control header;

	public BasePreferencePage() {
		super(GRID);
	}

	protected BasePreferencePage(final int style) {
		super(style);
	}

	public Page getPage() {
		return Page.forClass(getClass());
	}

	@Override
	protected void performApply() {
		super.performApply();

		// Save configuration file...
		final RouterLoggerConfiguration configuration = RouterLoggerConfiguration.getInstance();

		OutputStream configurationOutputStream = null;
		try {
			configurationOutputStream = configuration.openConfigurationOutputStream();
			((PreferenceStore) getPreferenceStore()).save(configurationOutputStream, null);
		}
		catch (IOException ioe) {
			Logger.getInstance().log(ioe);
		}
		finally {
			try {
				configurationOutputStream.close();
			}
			catch (final Exception exception) {}
		}

		// Reload RouterLogger configuration...
		try {
			configuration.reload();
		}
		catch (final Exception exception) {
			Logger.getInstance().log(exception);
		}
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);

		final Button defaultsButton = getDefaultsButton();
		defaultsButton.setText(Resources.get("lbl.preferences.button.defaults"));
		defaultsButton.setToolTipText(Resources.get("lbl.preferences.button.defaults.tooltip"));

		final Button applyButton = getApplyButton();
		applyButton.setText(Resources.get("lbl.button.apply"));
	}

	@Override
	protected void createFieldEditors() {
		// Header
		header = createHeader();
		if (header != null) {
			GridDataFactory.fillDefaults().span(Short.MAX_VALUE, 1).applyTo(header);
			addSeparator();
		}

		// Fields
		for (final Preference preference : Preference.values()) {
			if (getPage().equals(preference.getPage())) {
				final FieldEditor fieldEditor = preference.createFieldEditor(getFieldEditorParent());
				addField(fieldEditor);
				fieldEditorMap.put(preference, fieldEditor);
			}
		}
	}

	@Override
	public void propertyChange(final PropertyChangeEvent event) {
		super.propertyChange(event);
		if (event.getSource() instanceof BooleanFieldEditor) {
			final BooleanFieldEditor changedBooleanFieldEditor = (BooleanFieldEditor) event.getSource();
			final Boolean parentEnabled = (Boolean) event.getNewValue();
			for (final Entry<Preference, FieldEditor> entry : fieldEditorMap.entrySet()) {
				if (entry.getValue().equals(changedBooleanFieldEditor)) {
					// Found!
					for (final Preference childPreference : entry.getKey().getChildren()) {
						updateChildrenStatus(childPreference, parentEnabled);
					}
					break; // Done!
				}
			}
		}
	}

	@Override
	protected void initialize() {
		super.initialize();
		updateFieldsStatus();
	}

	@Override
	protected void performDefaults() {
		super.performDefaults();
		updateFieldsStatus();
	}

	protected void updateChildrenStatus(final Preference childPreference, final Boolean parentEnabled) {
		final FieldEditor childFieldEditor = fieldEditorMap.get(childPreference);
		updateChildStatus(childFieldEditor, parentEnabled);
		// Recurse descendants...
		if (childFieldEditor instanceof BooleanFieldEditor) {
			final BooleanFieldEditor childBooleanFieldEditor = (BooleanFieldEditor) childFieldEditor;
			for (final Preference descendantPreference : childPreference.getChildren()) { // Exit condition
				final boolean childEnabled = childBooleanFieldEditor.getBooleanValue();
				updateChildrenStatus(descendantPreference, childEnabled && parentEnabled);
			}
		}
	}

	protected void updateFieldsStatus() {
		for (final Entry<Preference, FieldEditor> entry : fieldEditorMap.entrySet()) {
			if (entry.getValue() instanceof BooleanFieldEditor) {
				final BooleanFieldEditor booleanFieldEditor = (BooleanFieldEditor) entry.getValue();
				final boolean parentEnabled;
				if (fieldEditorMap.get(entry.getKey().getParent()) instanceof BooleanFieldEditor) {
					final BooleanFieldEditor parentBooleanFieldEditor = (BooleanFieldEditor) fieldEditorMap.get(entry.getKey().getParent());
					parentEnabled = booleanFieldEditor.getBooleanValue() && parentBooleanFieldEditor.getBooleanValue();
				}
				else {
					parentEnabled = booleanFieldEditor.getBooleanValue();
				}
				for (final Preference childPreference : entry.getKey().getChildren()) {
					updateChildStatus(fieldEditorMap.get(childPreference), parentEnabled);
				}
			}
		}
	}

	protected void updateChildStatus(final FieldEditor childFieldEditor, final boolean parentEnabled) {
		childFieldEditor.setEnabled(parentEnabled, getFieldEditorParent());
		if (!parentEnabled && !childFieldEditor.isValid()) {
			childFieldEditor.loadDefault(); // Fix invalid value
			checkState(); // Enable OK & Apply buttons
		}
	}

	/** Viene aggiunto automaticamente un separatore tra il testo e i campi. */
	protected Control createHeader() {
		return null;
	}

	protected void addSeparator() {
		final Label separator = new Label(getFieldEditorParent(), SWT.HORIZONTAL | SWT.SEPARATOR);
		GridDataFactory.fillDefaults().span(Short.MAX_VALUE, 1).grab(true, false).applyTo(separator);
	}

	public static String[][] getNewLineComboOptions() {
		final int length = NewLine.values().length;
		final String[][] options = new String[length][2];
		for (int index = 0; index < length; index++) {
			options[index][0] = options[index][1] = NewLine.values()[index].name();
		}
		return options;
	}

	public Control getHeader() {
		return header;
	}

}
