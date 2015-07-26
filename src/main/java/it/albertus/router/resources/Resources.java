package it.albertus.router.resources;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Resources {

	private static final ResourceBundle resources = ResourceBundle.getBundle(Resources.class.getName().toLowerCase());

	public static String get(final String key, final Object... params) {
		final List<String> stringParams = new ArrayList<String>(params.length);
		for (Object param : params) {
			stringParams.add(param != null ? param.toString() : "");
		}
		final String message = MessageFormat.format(resources.getString(key), stringParams.toArray());
		return message != null ? message.trim() : "";
	}

}
