/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.basic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.TableViewModel;

/**
 * Control whose vertical size (height) could be adjusted by the user.
 * 
 * @see SaveVerticalSizeAction
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class VerticalSizableControl extends AbstractVisibleControl {

	/**
	 * The current version of the format, used to store the vertical size into the personal
	 * configuration.
	 */
	private static final double VERTICAL_SIZE_FORMAT_VERSION = 1.0d;

	private HTMLFragment _contentControl;

	private ConfigKey _configKey;

	/**
	 * Creates a {@link VerticalSizableControl} for the given control.
	 * 
	 * The created {@link Control} represents the given control, but its vertical size can be
	 * adjusted by the user.
	 * 
	 * @param contentControl
	 *        Control thats wrapped into a vertical resizable control.
	 * @param key
	 *        Personal configuration key to save the vertical size to or load from.
	 */
	public VerticalSizableControl(Control contentControl, ConfigKey key) {
		super(createCommandMap(new ControlCommand[] { SaveVerticalSizeAction.INSTANCE }));

		_contentControl = contentControl;
		_configKey = ConfigKey.derived(key, "verticalSize");
	}

	@Override
	public Object getModel() {
		return null;
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		Icons.VERTICAL_SIZABLE_CONTROL_TEMPLATE.get().write(context, out, this);
	}

	/**
	 * Writes the control content.
	 */
	@TemplateVariable("content")
	public void writeContent(DisplayContext context, TagWriter out) throws IOException {
		_contentControl.write(context, out);
	}

	/**
	 * Returns the controls maximal height.
	 */
	@TemplateVariable("maxHeight")
	public double getMaxHeight() {
		return loadSize();
	}

	private double loadSize() {
		String configKey = _configKey.get();

		if (configKey != null) {
			PersonalConfiguration personalConfiguration = PersonalConfiguration.getPersonalConfiguration();

			try {
				List<?> sizeConfig = (List<?>) personalConfiguration.getJSONValue(configKey);

			if (sizeConfig != null) {
<<<<<<< Upstream, based on origin/master
				double configFormatVersion = ((List<Number>) ((List<Object>) sizeConfig).get(0)).get(0).doubleValue();
				
				if (sizeConfig != null) {
					double configFormatVersion = ((Number) ((List<?>) sizeConfig.get(0)).get(0)).doubleValue();

=======
				double configFormatVersion = ((List<Double>) ((List<Object>) sizeConfig).get(0)).get(0);

				if (Logger.isDebugEnabled(TableViewModel.class)) {
					Logger.debug("Load tables height configuration from personal configuration. " +
						"Configuration format version '" + configFormatVersion +
						"' found.", TableViewModel.class);
				}
>>>>>>> 9a673af Ticket #27199: Changes to avoid merge conflict

				if (configFormatVersion == VERTICAL_SIZE_FORMAT_VERSION) {
					return ((List<Double>) ((List<Object>) sizeConfig).get(1)).get(0);
				} else {
				if (configFormatVersion == VERTICAL_SIZE_FORMAT_VERSION) {
					return ((List<Number>) ((List<Object>) sizeConfig).get(1)).get(0).doubleValue();
				} else {
					if (Logger.isDebugEnabled(TableViewModel.class)) {
						Logger.debug("Load tables height configuration from personal configuration. " +
							"Configuration format version '" + configFormatVersion +
							"' found.", TableViewModel.class);
					}

					if (configFormatVersion == VERTICAL_SIZE_FORMAT_VERSION) {
						return ((Number) ((List<?>) sizeConfig.get(1)).get(0)).doubleValue();
					} else {
						Logger.debug("Incompatible personal configuration format: " + configFormatVersion,
							VerticalSizableControl.class);
					}
				}
			} catch (RuntimeException ex) {
				Logger.warn("Invalid personal configuration.", ex, VerticalSizableControl.class);
			}

			// Reset configuration.
			personalConfiguration.setValue(configKey, null);
		}

		return 300.;
	}

	/**
	 * Saves the given vertical size into the users personal configuration.
	 */
	public void saveSize(double size) {
		String configKey = _configKey.get();

		if (configKey != null) {
			List<Object> config = new ArrayList<>(2);
			List<Double> formatVersion = Collections.singletonList(VERTICAL_SIZE_FORMAT_VERSION);

			config.add(formatVersion);
			config.add(Collections.singletonList(size));

			PersonalConfiguration.getPersonalConfiguration().setJSONValue(configKey, config);
		}
	}
}
