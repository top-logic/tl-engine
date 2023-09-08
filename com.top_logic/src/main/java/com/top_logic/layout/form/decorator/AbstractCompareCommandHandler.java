/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ModelSpec;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InViewModeExecutable;
import com.top_logic.util.Resources;

/**
 * Provide the mechanism to create a second form context and place that in the original one.
 * 
 * <p>
 * This command uses methods from {@link CompareService}.
 * </p>
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public abstract class AbstractCompareCommandHandler<C extends FormComponent> extends AbstractCommandHandler {

	/**
	 * Configuration of the {@link AbstractCompareCommandHandler}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@ListDefault(InViewModeExecutable.class)
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

		@Override
		@FormattedDefault(TARGET_NULL)
		ModelSpec getTarget();
	}

	/**
	 * Creates a new {@link AbstractCompareCommandHandler} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link AbstractCompareCommandHandler}.
	 */
	public AbstractCompareCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Set the compare object to the given component.
	 * 
	 * @param component
	 *        The component to get the information from, must not be <code>null</code>.
	 * @param anotherModel
	 *        The other revision of a model, may be <code>null</code>.
	 */
	protected abstract void setCompareObject(C component, Object anotherModel);

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component,
			Object model, Map<String, Object> someArguments) {
		if (component instanceof FormComponent) {
			@SuppressWarnings("unchecked")
			C form = (C) component;

			Object compareObject;
			if (getCompareObject(form) != null) {
				compareObject = null;
			} else {
				HandlerResult res = openDialog(context, form, someArguments);
				if (res != HandlerResult.DEFAULT_RESULT) {
					return res;
				}
				compareObject = createCompareObject(context, form, someArguments);
			}

			setCompareObject(form, compareObject);

			CommandModel commandModel = getCommandModel(someArguments);
			if (commandModel != null) {
				adaptCommandModel(context, commandModel, component, compareObject == null);
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Returns the comparison object from the given {@link FormComponent}. This is the object
	 * eventually formerly set by {@link #setCompareObject(FormComponent, Object)}.
	 * 
	 * @param component The component on which this command is executed.
	 */
	protected abstract Object getCompareObject(C component);

	/**
	 * Adapts the given {@link CommandModel} (label, tooltip, etc.) due to the new state whether
	 * comparison is active.
	 * 
	 * @param comparisonActive
	 *        Whether the comparison mode is currently active or not.
	 */
	protected void adaptCommandModel(DisplayContext context, CommandModel model, LayoutComponent component,
			boolean comparisonActive) {
		Resources resources = context.getResources();
		String label;
		String tooltip;
		if (comparisonActive) {
			label = createActivatedLabel(resources, component);
			tooltip = getTooltip(resources, component);
		} else {
			label = createDeactivatedLabel(resources, component);
			tooltip = getDeactivatedTooltip(resources, component);
		}
		model.setLabel(label);
		if (tooltip != null) {
			model.setTooltip(TagUtil.encodeXML(tooltip));
		} else {
			model.setTooltip(null);
		}
	}

	private String createDeactivatedLabel(Resources resources, LayoutComponent targetComponent) {
		String deactivated = ".deactivated";
		ResKey key = targetComponent.getResPrefix().key(getID()).suffix(deactivated);
		ResKey defaultKey = getResourceKey(targetComponent).suffix(deactivated);

		return resources.getStringWithDefaultKey(key, defaultKey);
	}

	private String createActivatedLabel(Resources resources, LayoutComponent targetComponent) {
		return LayoutComponent.createCommandLabel(resources, targetComponent, this);
	}

	private String getTooltip(Resources res, LayoutComponent targetComponent) {
		return LayoutComponent.getTooltip(res, this, targetComponent);
	}

	private String getDeactivatedTooltip(Resources res, LayoutComponent targetComponent) {
		String deactivated = ".deactivated";
		ResKey componentInfoKey =
			targetComponent.getResPrefix().append(getID()).key(deactivated).tooltip();
		String componentInfoValue = res.getString(componentInfoKey, null);
		if (componentInfoValue != null) {
			return componentInfoValue;
		}

		ResKey defaultLabelKey = getResourceKey(targetComponent).suffix(deactivated);
		if (defaultLabelKey == null) {
			// No default tooltip specified.
			return null;
		}

		ResKey defaultInfoKey = defaultLabelKey.tooltip();
		return res.getString(defaultInfoKey, null);

	}

	protected HandlerResult openDialog(DisplayContext aContext, LayoutComponent aComponent,
			Map<String, Object> someArguments) {
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * Return the compare object for the given component.
	 * 
	 * @param context
	 *        The execution context
	 * @param component
	 *        The component to create a configuration for.
	 * @param someArguments
	 *        The arguments given in {@link #handleCommand(DisplayContext, LayoutComponent, Object, Map)}.
	 * 
	 * @return The requested compare object.
	 */
	protected abstract Object createCompareObject(DisplayContext context, C component, Map<String, Object> someArguments);

}
