/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.customization.AnnotationCustomizations;
import com.top_logic.basic.config.customization.ConfiguredAnnotationCustomizations;
import com.top_logic.basic.config.customization.CustomizationContainer;
import com.top_logic.basic.config.customization.CustomizationContainer.TypeCustomizationConfig;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.layout.form.declarative.DeclarativeFormBuilder;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;
import com.top_logic.layout.form.values.edit.initializer.InitializerProvider;
import com.top_logic.layout.messagebox.CreateConfigurationsDialog.ModelPartDefinition;
import com.top_logic.layout.provider.SelectControlProvider;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.component.SimpleFormBuilder;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Base class opening a dialog for configuring a dynamic component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractComponentConfigurationDialogBuilder extends CreateConfigurationsDialogBuilder {

	/**
	 * Global configuration options to configure the {@link AbstractComponentConfigurationDialogBuilder} forms.
	 */
	public interface GlobalConfig extends CustomizationContainer {

		// Pure marker.

		/**
		 * Customizations used for {@link ConfigurationItem} for creation of
		 * {@link LayoutComponent}s.
		 */
		public static ConfiguredAnnotationCustomizations newCustomizations() {
			CustomizationContainer config = ApplicationConfig.getInstance().getConfig(GlobalConfig.class);
			ConfiguredAnnotationCustomizations customizations = new ConfiguredAnnotationCustomizations();
			if (config != null) {
				customizations.addCustomizations(config);
			}
			return customizations;
		}

	}

	/**
	 * Property of the form's {@link InitializerProvider} pointing to the
	 * {@link #contextComponent()}.
	 */
	public static final Property<LayoutComponent> COMPONENT =
		TypedAnnotatable.property(LayoutComponent.class, "component");

	/** The {@link DynamicComponentDefinition} to create the component from. */
	protected final DynamicComponentDefinition _definition;

	private ConfigurationItem _arguments;

	/**
	 * Creates a new {@link AbstractComponentConfigurationDialogBuilder} with the given arguments preset.
	 */
	public AbstractComponentConfigurationDialogBuilder(DynamicComponentDefinition definition, ConfigurationItem arguments,
			HTMLFragment dialogTitle, Function<ConfigurationItem, HandlerResult> createComponentFinisher) {
		super(createComponentConfigurationFinisher(createComponentFinisher));
		_definition = definition;
		_arguments = arguments;

		setDialogTitle(dialogTitle);
	}

	private static Function<List<? extends ConfigurationItem>, HandlerResult> createComponentConfigurationFinisher(
			Function<ConfigurationItem, HandlerResult> createComponentFinisher) {
		return (configs) -> {
			int componentDefIndex = configs.size() - 1;
			ConfigurationItem componentArguments = configs.get(componentDefIndex);
			return createComponentFinisher.apply(componentArguments);
		};
	}

	/**
	 * The template arguments being edited.
	 * 
	 * @return The edited arguments or <code>null</code>, if a new configuration is created in this
	 *         dialog.
	 */
	public ConfigurationItem getArguments() {
		return _arguments;
	}

	@Override
	protected void addModelPartDefinitions(List<ModelPartDefinition<? extends ConfigurationItem>> partDefinitions) {
		partDefinitions.add(componentPart());
	}

	private ModelPartDefinition<ConfigurationItem> componentPart() {
		ConfigurationItem model = createModel();
		ModelPartDefinition<ConfigurationItem> result = new ModelPartDefinition<>(model);
		AnnotationCustomizations customizations = buildCustomizations();
		InitializerProvider initializers = buildInitializers(model);
		result.setFormBuilder(new SimpleFormBuilder<>(customizations, initializers));
		return result;
	}

	/**
	 * Builds the {@link InitializerProvider} for creating the form.
	 */
	protected InitializerProvider buildInitializers(ConfigurationItem model) {
		InitializerProvider initializers = new InitializerIndex();
		initializers.set(COMPONENT, contextComponent());
		initializers.set(DeclarativeFormBuilder.FORM_MODEL, model);
		return initializers;
	}

	/**
	 * Builds all customizations to apply the the displayed form.
	 */
	protected ConfiguredAnnotationCustomizations buildCustomizations() {
		ConfiguredAnnotationCustomizations customizations = GlobalConfig.newCustomizations();

		customizations.addCustomization(createVisibleComponentNameCustomizations());

		return customizations;
	}

	private TypeCustomizationConfig createVisibleComponentNameCustomizations() {
		TypeCustomizationConfig typeConfig = TypedConfiguration.newConfigItem(TypeCustomizationConfig.class);

		typeConfig.setName(ComponentName.class.getName());
		TypedConfigUtil.setProperty(typeConfig, TypeCustomizationConfig.ANNOTATIONS, createVisibleComponentNameAnnotations());

		return typeConfig;
	}

	private Map<Class<? extends Annotation>, Annotation> createVisibleComponentNameAnnotations() {
		Map<Class<? extends Annotation>, Annotation> annotations = new HashMap<>();

		Options options = TypedConfiguration.newAnnotationItem(Options.class);
		TypedConfiguration.updateValue(options, "fun", AllVisibleComponentNames.class);
		annotations.put(Options.class, options);

		ControlProvider controlProvider = TypedConfiguration.newAnnotationItem(ControlProvider.class);
		TypedConfiguration.updateValue(controlProvider, "value", SelectControlProvider.class);
		annotations.put(ControlProvider.class, controlProvider);

		return annotations;
	}

	/**
	 * The {@link LayoutComponent} that opened the configuration dialog.
	 */
	protected abstract LayoutComponent contextComponent();

	private ConfigurationItem createModel() {
		if (_arguments != null) {
			return _arguments;
		}

		return _definition.descriptor().factory().createNew();
	}

}

