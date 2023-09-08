/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.chart.gantt.component;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.component.Selectable;
import com.top_logic.layout.component.Updatable;
import com.top_logic.layout.component.UpdateCommandHandler;
import com.top_logic.layout.form.component.FormComponent;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.CommandRegistry;
import com.top_logic.reporting.chart.gantt.model.GanttChartSettings;
import com.top_logic.reporting.view.component.property.FilterProperty;

/**
 * {@link FormComponent} that sends a new slave model on update containing a
 * {@link GanttChartSettings} that deals with {@link FilterProperty}s.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public abstract class AbstractGanttFilterComponent extends FormComponent implements Selectable, Updatable {

	/**
	 * Configuration options for {@link AbstractGanttFilterComponent}.
	 */
	public interface Config extends FormComponent.Config, Selectable.SelectableConfig {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Name of {@link #getMultipleSettings()}. */
		String MULTIPLE_SETTINGS_ATTR = "multipleSettings";

		/** Name of {@link #getMultipleSettingsKey()}. */
		String MULTIPLE_SETTINGS_KEY_ATTR = "multipleSettingsKey";

		/** Flag whether chart shall be updated automatically on new model. */
		@Name(XML_PARAM_UPDATE_AUTOMATICALLY_ON_NEW_MODEL)
		@BooleanDefault(true)
		boolean getUpdateOnNewModel();

		/**
		 * Whether this component should support storage of multiple filter settings.
		 */
		@Name(MULTIPLE_SETTINGS_ATTR)
		boolean getMultipleSettings();

		/**
		 * If {@link #getMultipleSettings() multiple filter settings} are supported, this property
		 * defines a part of the key identifying the stored keys, i.e. multiple components can share
		 * the stored settings, when they use the same {@link #getMultipleSettingsKey()}.
		 * 
		 * @return If empty the key used for this component is used.
		 */
		@Name(MULTIPLE_SETTINGS_KEY_ATTR)
		String getMultipleSettingsKey();

		@Override
		default void modifyIntrinsicCommands(CommandRegistry registry) {
			FormComponent.Config.super.modifyIntrinsicCommands(registry);
			registry.registerCommand(UpdateCommandHandler.COMMAND_ID);
			if (getMultipleSettings()) {
				registry.registerButton(LoadNamedFilterSetting.COMMAND_ID);
				registry.registerButton(ManageMultipleFilterSettings.COMMAND_ID);
			}
		}

	}

	/** XML parameter for the {@link #_updateAutamaticallyOnNewModel} flag. */
    public static final String XML_PARAM_UPDATE_AUTOMATICALLY_ON_NEW_MODEL = "updateAutamaticallyOnNewModel";

	/** Holds the filter properties. */
	private List<FilterProperty> properties = Collections.emptyList();

	/** @see #isUpdateAutamaticallyOnNewModel() */
	private boolean _updateAutamaticallyOnNewModel;

	/**
	 * Creates a {@link AbstractGanttFilterComponent} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AbstractGanttFilterComponent(InstantiationContext context, Config config) throws ConfigurationException {
		super(context, config);
		_updateAutamaticallyOnNewModel = config.getUpdateOnNewModel();
	}

	@Override
	public FormContext createFormContext() {
		// create new properties when a new FormContext is created
		initProperties();
		FormContext formContext = new FormContext(this);
		for (FilterProperty property : getProperties()) {
			formContext.addMember(property.getFormMember());
		}
		return formContext;
	}

	/**
	 * Initialized the filter properties.
	 */
	private void initProperties() {
		properties = createNewProperties();
	}

	/**
	 * Gets the filter properties.
	 */
	protected List<FilterProperty> getProperties() {
		return properties;
	}

	/**
	 * Gets a new collection with the new FilterProperties used by this filter.
	 */
	protected abstract List<FilterProperty> createNewProperties();

	@Override
	protected void handleNewModel(Object newModel) {
		super.handleNewModel(newModel);
		// drop form context if error values were entered before model was changed
		if (hasFormContext() && getFormContext().hasError()) {
			removeFormContext();
		}
		updateOnModelChange();
	}

	@Override
	public void update() {
		sendNewModel(false);
	}

	/**
	 * Updates only if the {@link #updateOnModelChange()} flag is set
	 */
	public void updateOnModelChange() {
		sendNewModel(true);
	}

	/** Flag whether chart shall be updated automatically on new model. */
	protected boolean isUpdateAutamaticallyOnNewModel() {
		return _updateAutamaticallyOnNewModel;
	}

	/**
	 * Creates the slave component model and sends a model changed event.
	 * 
	 * @param automatic
	 *        flag whether the update was triggered automatically (<code>true</code>) or manual by
	 *        the user (<code>false</code>)
	 */
	protected void sendNewModel(boolean automatic) {
		getFormContext();
		setSelected(createSlaveModel(automatic));
	}

	/**
	 * Creates the slave component model.
	 * 
	 * @param automatic
	 *        flag whether the update was triggered automatically (<code>true</code>) or manual by
	 *        the user (<code>false</code>)
	 */
	protected GanttChartSettings createSlaveModel(boolean automatic) {
		if (automatic && !isUpdateAutamaticallyOnNewModel()) {
			return null;
		}
		
		Object model = getModel();
		if (model instanceof Wrapper) {
			GanttChartSettings config = createGanttChartSettings(model);
			config.setProperties(getProperties());
			return config;
		}
		else if (model != null) {
			Logger.error("Unexpected model type.", this);
		}
		return null;
	}

	/**
	 * Creates the {@link GanttChartSettings} for the given model.
	 * 
	 * @see GanttChartSettings#GanttChartSettings(Object, com.top_logic.tool.boundsec.BoundComponent)
	 */
	protected GanttChartSettings createGanttChartSettings(Object model) {
		return new GanttChartSettings(model, this);
	}

}
