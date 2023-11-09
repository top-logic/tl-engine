/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.editor.DynamicComponentDefinition;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.editor.scripting.Identifiers;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutReference;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.mig.html.layout.tiles.GroupTileComponent;
import com.top_logic.mig.html.layout.tiles.RootTileComponent;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;
import com.top_logic.tool.execution.InDesignModeExecutable;
import com.top_logic.util.error.TopLogicException;

/**
 * Adds a tile to a {@link GroupTileComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Add tile")
public class AddTileCommand extends AbstractComponentConfigurationCommandHandler {

	/**
	 * {@link ExecutabilityRule} to determine whether the {@link GroupTileComponent} is currently
	 * displayed. If not, the command is not displayed.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class IsDisplayedComponent implements ExecutabilityRule {

		/** Singleton {@link IsDisplayedComponent} instance. */
		public static final IsDisplayedComponent INSTANCE = new IsDisplayedComponent();

		/**
		 * Creates a new {@link IsDisplayedComponent}.
		 */
		protected IsDisplayedComponent() {
			// singleton instance
		}

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			if (!(aComponent instanceof GroupTileComponent)) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			GroupTileComponent groupTile = (GroupTileComponent) aComponent;
			RootTileComponent container = RootTileComponent.getRootTile(groupTile);
			List<LayoutComponent> displayedPath = container.displayedPath();
			if (displayedPath.isEmpty()) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			LayoutComponent displayComp = displayedPath.get(displayedPath.size() - 1);
			if (displayComp.equals(groupTile.getDisplayedComponent())) {
				return ExecutableState.EXECUTABLE;
			}
			return ExecutableState.NOT_EXEC_HIDDEN;
		}

	}

	/**
	 * Identifier under which an instance of this class is configured in the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String DEFAULT_COMMAND_ID = "addTileCommand";

	/**
	 * Creates a {@link AddTileCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public AddTileCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected HandlerResult createComponent(LayoutComponent component, DynamicComponentDefinition definition,
			ConfigurationItem componentConfig, List<AdditionalComponentDefinition> additional) {
		HandlerResult result = HandlerResult.DEFAULT_RESULT;

		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			Identifiers identifiers = LayoutTemplateUtils.replaceInnerTemplates(componentConfig);

			String tileIdentifier = LayoutTemplateUtils.createNewComponentLayoutKey();
			identifiers.addComponentKey(tileIdentifier);
			LayoutTemplateUtils.recordComponentCreation(ButtonType.OK.getButtonLabelKey(), identifiers, component);
			createTile((GroupTileComponent) component, definition.definitionFile(), componentConfig, tileIdentifier);

			tx.commit();
		}

		return result;
	}

	private void createTile(GroupTileComponent component, String template, ConfigurationItem arguments,
			String layoutKey) {

		LayoutTemplateUtils.storeLayout(layoutKey, template, arguments);

		String componentNameScope = LayoutTemplateUtils.getNonNullNameScope(component);
		TLLayout layout = LayoutTemplateUtils.getOrCreateLayout(componentNameScope);

		String templateName = layout.getTemplateName();
		ConfigurationItem templateArguments = getTemplateArguments(layout, templateName);

		LayoutReference newComponent = LayoutTemplateUtils.createLayoutReference(layoutKey);
		LayoutTemplateUtils.setComponentsProperty(templateArguments, newChildrenConfigs(component, newComponent));
		LayoutTemplateUtils.storeLayout(componentNameScope, templateName, templateArguments);
		LayoutTemplateUtils.replaceComponent(componentNameScope, component);
	}

	private List<LayoutComponent.Config> newChildrenConfigs(GroupTileComponent component,
			LayoutComponent.Config newComponent) {
		List<LayoutComponent.Config> newChildren = new ArrayList<>();
		for (LayoutComponent.Config oldChild : component.getConfig().getChildConfigurations()) {
			newChildren.add(TypedConfiguration.copy(oldChild));
		}
		newChildren.add(newComponent);
		return newChildren;
	}

	private ConfigurationItem getTemplateArguments(TLLayout layout, String template) {
		try {
			return layout.getArguments();
		} catch (ConfigurationException exception) {
			ResKey1 invalidTemplateConfig =
				com.top_logic.layout.editor.I18NConstants.ERROR_LAYOUT_CONFIGURATION_INVALID__LAYOUT;

			throw new TopLogicException(invalidTemplateConfig.fill(template), exception);
		}
	}

	@Override
	protected List<DynamicComponentDefinition> getComponentTemplates(LayoutComponent context) {
		return LayoutTemplateUtils.getTemplates(Collections.singleton("tile"), context);
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(
			super.intrinsicExecutability(),
			InDesignModeExecutable.INSTANCE,
			IsTemplateLayout.INSTANCE,
			IsDisplayedComponent.INSTANCE);
	}

}
