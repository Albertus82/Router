package it.albertus.router.gui.preference.field;

import it.albertus.router.gui.TextFormatter;
import it.albertus.router.resources.Resources;

import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;

public abstract class ValidatedComboFieldEditor extends EditableComboFieldEditor {

	private boolean keyListenerAdded; // becomes true only after super constructors!
	private boolean valid = true;
	private String errorMessage;

	public ValidatedComboFieldEditor(final String name, final String labelText, final String[][] entryNamesAndValues, final Composite parent) {
		super(name, labelText, entryNamesAndValues, parent);
		keyListenerAdded = true;
	}

	protected abstract boolean checkState();

	@Override
	protected void updateValue() {
		cleanComboText();
		super.updateValue();
		boolean oldValue = valid;
		refreshValidState();
		updateFontStyle();
		fireValueChanged(IS_VALID, oldValue, valid);
	}

	@Override
	protected void refreshValidState() {
		setValid(checkState());
		final String errorMessage = getErrorMessage();
		if (errorMessage != null && !errorMessage.isEmpty()) {
			if (isValid()) {
				clearErrorMessage();
			}
			else {
				showErrorMessage(errorMessage);
			}
		}
	}

	@Override
	protected void doLoad() {
		super.doLoad();
		setToolTipText(getNameForValue(getDefaultValue()));
		updateFontStyle();
		cleanComboText();
	}

	@Override
	protected void doLoadDefault() {
		super.doLoadDefault();
		updateFontStyle();
	}

	@Override
	protected void updateComboForValue(final String value) {
		super.updateComboForValue(cleanValue(value));
	}

	@Override
	public boolean isValid() {
		return valid;
	}

	@Override
	protected Combo getComboBoxControl(final Composite parent) {
		final Combo combo = super.getComboBoxControl(parent);
		if (!keyListenerAdded) { // enters only when called from super constructors!
			combo.addKeyListener(new KeyAdapter() {
				@Override
				public void keyReleased(final KeyEvent ke) {
					ValidatedComboFieldEditor.super.updateValue();
					boolean oldValue = valid;
					refreshValidState();
					fireValueChanged(IS_VALID, oldValue, valid);
				}
			});
		}
		return combo;
	}

	protected void setValid(final boolean valid) {
		this.valid = valid;
	}

	protected String getDefaultValue() {
		return getPreferenceStore().getDefaultString(getPreferenceName());
	}

	protected void setToolTipText(final String defaultValue) {
		if (getComboBoxControl() != null && !getComboBoxControl().isDisposed() && defaultValue != null && !defaultValue.isEmpty()) {
			getComboBoxControl().setToolTipText(Resources.get("lbl.preferences.default.value", defaultValue));
		}
	}

	protected void updateFontStyle() {
		final String defaultValue = getDefaultValue();
		if (defaultValue != null && !defaultValue.isEmpty()) {
			TextFormatter.updateFontStyle(getComboBoxControl(), defaultValue, getValue());
		}
	}

	/** Trims value (from configuration file) to empty. */
	protected String cleanValue(final String value) {
		return value != null ? value.trim() : "";
	}

	/** Trims combo text and tries to associate it with an existing entry. */
	protected void cleanComboText() {
		final String oldText = getComboBoxControl().getText();
		final String newText = getNameForValue(oldText.trim());
		if (!newText.equals(oldText)) {
			getComboBoxControl().setText(newText);
		}
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(final String message) {
		this.errorMessage = message;
	}

}
