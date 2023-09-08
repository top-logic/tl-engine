/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import static com.top_logic.layout.DisplayDimension.*;

import java.util.List;
import java.util.function.Function;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.customization.ConfiguredAnnotationCustomizations;
import com.top_logic.basic.config.customization.CustomizationContainer;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.component.TabComponent;
import com.top_logic.layout.form.model.FieldMode;
import com.top_logic.layout.form.values.edit.initializer.InitializerProvider;
import com.top_logic.layout.form.values.edit.mode.CustomMode;
import com.top_logic.layout.messagebox.CreateConfigurationsDialog;
import com.top_logic.layout.messagebox.CreateConfigurationsDialog.ModelPartDefinition;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.component.I18NConstants;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Utility to open a dialog for configuring a dynamic component.
 * 
 * <p>
 * The configuration options are the parameters read from the corresponding typed layout template.
 * </p>
 * 
 * @see DynamicComponentDefinition
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ComponentConfigurationDialogBuilder extends AbstractComponentConfigurationDialogBuilder {

	private LayoutComponent _component;

	/**
	 * Global configuration options for {@link ComponentConfigurationDialogBuilder} in edit mode.
	 * 
	 * @see ApplicationConfig#getConfig(Class) This is a marker configuration to be configured in
	 *      the application configuration.
	 * 
	 * @see com.top_logic.layout.editor.AbstractComponentConfigurationDialogBuilder.GlobalConfig
	 *      Inherited options used in view mode.
	 */
	public interface GlobalEditConfig extends CustomizationContainer {
		// Pure marker.
	}

	/**
	 * Creates a new {@link ComponentConfigurationDialogBuilder}.
	 */
	public ComponentConfigurationDialogBuilder(LayoutComponent component, DynamicComponentDefinition definition,
			Function<ConfigurationItem, HandlerResult> createComponentFinisher) {
		this(component, definition, null, createComponentFinisher);
	}

	/**
	 * Creates a new {@link ComponentConfigurationDialogBuilder}.
	 */
	public ComponentConfigurationDialogBuilder(LayoutComponent component, DynamicComponentDefinition definition,
			ConfigurationItem arguments, Function<ConfigurationItem, HandlerResult> createComponentFinisher) {
		this(component, definition, arguments, new ResourceText(definition.label()), createComponentFinisher);
	}

	/**
	 * Creates a new {@link ComponentConfigurationDialogBuilder}.
	 * 
	 * @param component
	 *        {@link TabComponent} in which the new component should be integrated.
	 * @param definition
	 *        Definition of the component to create.
	 * @param arguments
	 *        Values for the component definition.
	 * @param dialogTitle
	 *        Title of the dialog to edit the values (arguments) used by the component definition.
	 */
	public ComponentConfigurationDialogBuilder(LayoutComponent component, DynamicComponentDefinition definition,
			ConfigurationItem arguments, HTMLFragment dialogTitle,
			Function<ConfigurationItem, HandlerResult> createComponentFinisher) {
		super(definition, arguments, dialogTitle, createComponentFinisher);
		_component = component;

		init();
	}

	@Override
	protected LayoutComponent contextComponent() {
		return _component;
	}

	private void init() {
		setDialogWidth(dim(750, DisplayUnit.PIXEL));
		int numberProperties = _definition.descriptor().getProperties().size();
		setDialogHeight(dim(415 + numberProperties * 65, DisplayUnit.PIXEL));
	}

	@Override
	protected InitializerProvider buildInitializers(ConfigurationItem model) {
		InitializerProvider result = super.buildInitializers(model);
		result.set(CustomMode.MODE_PROPERTY, inEditMode() ? FieldMode.INVISIBLE : FieldMode.ACTIVE);
		return result;
	}

	@Override
	protected ConfiguredAnnotationCustomizations buildCustomizations() {
		ConfiguredAnnotationCustomizations result = super.buildCustomizations();

		if (inEditMode()) {
			GlobalEditConfig config = ApplicationConfig.getInstance().getConfig(GlobalEditConfig.class);
			if (config != null) {
				result.addCustomizations(config);
			}
		}

		return result;
	}

	private boolean inEditMode() {
		return getArguments() != null;
	}

	@Override
	protected CreateConfigurationsDialog createConfigurationsDialog(
			List<ModelPartDefinition<? extends ConfigurationItem>> partDefinitions) {
		return new CreateComponentConfigurationDialog(
			partDefinitions,
			I18NConstants.TILE_BUILDER_CONFIGURATION_DIALOG.key("title"),
					dialogWidth(), dialogHeight());
	}

}
