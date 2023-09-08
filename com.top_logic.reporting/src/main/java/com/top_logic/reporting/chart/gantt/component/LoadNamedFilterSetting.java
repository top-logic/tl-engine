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
import com.top_logic.layout.table.model.LoadNamedSetting;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.reporting.view.component.property.FilterProperty;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.util.TLContext;

/**
 * {@link AbstractCommandHandler} loading one of the formerly stored filter settings.
 * 
 * @see ManageMultipleFilterSettings
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class LoadNamedFilterSetting extends AbstractCommandHandler {

	private static final ExecutabilityRule NAMED_FILTERSETTING_RULE = new ExecutabilityRule() {

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			AbstractGanttFilterComponent gantt = (AbstractGanttFilterComponent) aComponent;
			ConfigKey key = ManageMultipleFilterSettings.key(gantt);
			if (TLContext.getContext().getPersonalConfiguration().getValue(key.get()) == null) {
				return ExecutableState.createDisabledState(I18NConstants.ERROR_NO_NAMED_FILTER_SETTING);
			}
			return ExecutableState.EXECUTABLE;
		}
	};

	/**
	 * Command ID of this {@link LoadNamedFilterSetting} to be found in the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String COMMAND_ID = "loadFilterSettings";

	/**
	 * Creates a new {@link LoadNamedFilterSetting}.
	 */
	public LoadNamedFilterSetting(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model,
			Map<String, Object> someArguments) {
		AbstractGanttFilterComponent gantt = (AbstractGanttFilterComponent) aComponent;
		List<FilterProperty> properties = gantt.getProperties();
		Consumer<ConfigKey> configLoader = key -> properties.forEach(prop -> prop.loadConfiguredValue(key.get()));
		return new LoadNamedSetting(ManageMultipleFilterSettings.key(gantt), configLoader).executeCommand(aContext);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(super.intrinsicExecutability(), NAMED_FILTERSETTING_RULE);
	}

}

