/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.component;

import java.util.List;
import java.util.function.Function;

import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.editor.CreateConfigurationsDialogBuilder;
import com.top_logic.layout.messagebox.CreateConfigurationsDialog.ModelPartDefinition;
import com.top_logic.layout.messagebox.DialogFormBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutUtils;
import com.top_logic.mig.html.layout.tiles.InlinedTile;
import com.top_logic.mig.html.layout.tiles.TileBuilder;
import com.top_logic.mig.html.layout.tiles.TileUtils;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Helper class to configure a new {@link LayoutComponent} based on a {@link TileBuilder}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ConfigureNewTileComponent extends CreateConfigurationsDialogBuilder {

	private final TileBuilder _builder;

	private final String _componentNameScope;

	private final TileContainerComponent _tileContainer;

	/**
	 * Creates a new {@link ConfigureNewTileComponent}.
	 * 
	 * @param tileContainer
	 *        The {@link TileContainerComponent} to create component in.
	 * @param builder
	 *        The {@link TileBuilder} holding informations for the component to create.
	 */
	public ConfigureNewTileComponent(TileContainerComponent tileContainer, TileBuilder builder) {
		super(createConfigurationFinisher(tileContainer, builder));
		_tileContainer = tileContainer;
		_builder = builder;
		_componentNameScope = tileContainer.getName().scope();
		init();
	}

	private void init() {
		setDialogWidth(_builder.getDialogWidth());
		setDialogHeight(_builder.getDialogHeight());
		setDialogTitle(new ResourceText(_builder.getLabel()));
	}

	@Override
	protected void addModelPartDefinitions(List<ModelPartDefinition<? extends ConfigurationItem>> partDefinitions) {
		partDefinitions.add(descriptionPart());
		partDefinitions.add(componentPart());
	}

	private ModelPartDefinition<? extends ConfigurationItem> descriptionPart() {
		ModelPartDefinition<? extends ConfigurationItem> partDefintion;
		if (_builder.getPreview() == null) {
			partDefintion = new ModelPartDefinition<>(StaticPreviewConfiguration.class);
		} else {
			partDefintion = new ModelPartDefinition<>(LabelConfiguration.class);
		}
		partDefintion.setLegendKey(I18NConstants.ADD_COMPONENT_CONFIGURATION__DESCRIPTION_BOX_LABEL);

		return partDefintion;
	}

	private static Function<List<? extends ConfigurationItem>, HandlerResult> createConfigurationFinisher(
			TileContainerComponent tileContainer, TileBuilder builder) {
		return (List<? extends ConfigurationItem> l) -> {
			LabelConfiguration labelConfig = (LabelConfiguration) l.get(0);

			ComponentParameters componentParams = (ComponentParameters) l.get(1);
			String newId = removeMinus(StringServices.randomUUID());
			LayoutComponent.Config layoutConfig = fetchComponentConfig(componentParams, newId);

			InlinedTile inlinedTile = TypedConfiguration.newConfigItem(InlinedTile.class);
			inlinedTile.setId(newId);
			inlinedTile.setComponent(layoutConfig);
			inlinedTile.setLabel(ResKey.text(labelConfig.getLabel()));

			PolymorphicConfiguration<? extends TilePreview> preview = builder.getPreview();
			if (preview == null) {
				StaticPreviewConfiguration item = (StaticPreviewConfiguration) labelConfig;
				preview = StaticPreview.newStaticPreview(item.getImage(), ResKey.text(item.getDescription()));
			}
			inlinedTile.setPreview(preview);
			tileContainer.addNewTile(inlinedTile);
			return HandlerResult.DEFAULT_RESULT;
		};
	}

	private static String removeMinus(String newId) {
		/* Ticket #24549: Component name must not contain '-', because it is eventually used in JS
		 * functions (e.g. displayDialog_<newID>Dialog), which is not allowed. */
		newId = newId.replace('-', '_');
		return newId;
	}

	/**
	 * {@link ModelPartDefinition} for the actual component.
	 * 
	 * @see #addModelPartDefinitions(List)
	 */
	private ModelPartDefinition<? extends ConfigurationItem> componentPart() {
		ModelPartDefinition<ComponentParameters> partDefinition =
			new ModelPartDefinition<>(createComponentParameters());
		DialogFormBuilder<? super ComponentParameters> formBuilder =
			SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(_builder.getFormBuilder());
		partDefinition.setFormBuilder(formBuilder);
		partDefinition.setLegendKey(I18NConstants.ADD_COMPONENT_CONFIGURATION__COMPONENT_BOX_LABEL);
		
		return partDefinition;
	}

	private ComponentParameters createComponentParameters() {
		ComponentParameters parameters = TypedConfiguration.newConfigItem(_builder.getParameters());
		parameters.setTemplate(_builder.getTemplate());
		ConfigurationDescriptor descriptor = parameters.descriptor();
		_builder.getArguments().entrySet().forEach(entry -> {
			PropertyDescriptor property = descriptor.getProperty(entry.getKey());
			updatePropertyValue(parameters, property, entry.getValue());
		});
		return parameters;
	}

	private void updatePropertyValue(ConfigurationItem config, PropertyDescriptor property, String rawValue) {
		ConfigurationValueProvider<?> valueProvider = property.getValueProvider();
		if (valueProvider == null) {
			Logger.error("Unable to set value '" + rawValue + "' to property '" + property
				+ "' because it has no value provider.", CreateConfigurationsDialogBuilder.class);
			return;
		}
		Object parsedValue;
		try {
			parsedValue = valueProvider.getValue(property.getPropertyName(), rawValue);
		} catch (ConfigurationException ex) {
			Logger.error("Unable to parse value '" + rawValue + "'.", ex, CreateConfigurationsDialogBuilder.class);
			return;
		}
		if (parsedValue instanceof ConfigurationItem) {
			LayoutUtils.qualifyComponentNames(_componentNameScope, (ConfigurationItem) parsedValue);
		}
		config.update(property, parsedValue);
	}

	/**
	 * Helper method to create the {@link com.top_logic.mig.html.layout.LayoutComponent.Config} from
	 * the filled {@link ComponentParameters}.
	 * 
	 * @param componentParams
	 *        The {@link ComponentParameters} from the list delivered in
	 *        {@link #createConfigurationFinisher(TileContainerComponent, TileBuilder)}.
	 * @param newId
	 *        A new global wide unique identifier.
	 */
	private static LayoutComponent.Config fetchComponentConfig(ComponentParameters componentParams, String newId) {
		Protocol log = new LogProtocol(CreateConfigurationsDialogBuilder.class);
		LayoutComponent.Config layoutConfig =
			TileUtils.resolveComponentConfiguration(log, componentParams.getTemplate() + "_" + newId,
				componentParams);
		log.checkErrors();
		return layoutConfig;
	}


}
