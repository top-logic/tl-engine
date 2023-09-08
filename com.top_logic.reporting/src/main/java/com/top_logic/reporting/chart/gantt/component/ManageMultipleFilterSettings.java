/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.layout.table.model.ManageMultipleTableSettings;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.view.component.property.FilterProperty;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link AbstractCommandHandler} to manage multiple filter settings.
 * 
 * @see LoadNamedFilterSetting
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ManageMultipleFilterSettings extends AbstractCommandHandler {

	private static final String KEY_PREFIX = "multipleFilterSettings.";

	/**
	 * Command ID of this {@link ManageMultipleFilterSettings} to be found in the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String COMMAND_ID = "manageFilterSettings";

	/**
	 * Creates a new {@link ManageMultipleFilterSettings}.
	 */
	public ManageMultipleFilterSettings(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		AbstractGanttFilterComponent gantt = (AbstractGanttFilterComponent) aComponent;
		List<FilterProperty> properties = gantt.getProperties();

		Consumer<ConfigKey> configSaver = key -> properties.forEach(prop -> prop.saveValueToConfiguration(key.get()));
		Consumer<ConfigKey> configRemover = key -> properties.forEach(prop -> prop.deleteConfiguration(key.get()));
		return new ManageMultipleTableSettings(key(gantt), configSaver, configRemover).executeCommand(aContext);
	}

	static ConfigKey key(AbstractGanttFilterComponent gantt) {
		String multipleSettingsKey = ((AbstractGanttFilterComponent.Config) gantt.getConfig()).getMultipleSettingsKey();
		if (multipleSettingsKey.isEmpty()) {
			return ConfigKey.component(gantt);
		} else {
			return ConfigKey.named(KEY_PREFIX + multipleSettingsKey);
		}
	}

}
