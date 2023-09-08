/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.commands;

import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.LayoutStorage;
import com.top_logic.mig.html.layout.TLLayout;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.InDesignModeExecutable;
import com.top_logic.tool.execution.UniqueToolbarCommandRule;

/**
 * {@link CommandHandler} to edit the wrapping layout for the given component instantiated from a
 * typed template.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class EditLayoutCommand extends PreconditionCommandHandler {

	/**
	 * Identifier under which an instance of this class is configured in the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String DEFAULT_COMMAND_ID = "editLayoutCommand";

	/**
	 * Configuration for {@link EditLayoutCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static interface Config extends PreconditionCommandHandler.Config {

		/** Configuration name for the value of {@link #getSupportedTemplates()}. */
		String SUPPORTED_TEMPLATES = "supported-templates";

		/**
		 * The name of the templates for which this command is offered.
		 * 
		 * <p>
		 * This command is only offered when the layout is loaded from a template layout and the
		 * template name is contained in {@link #getSupportedTemplates()}.
		 * </p>
		 */
		@Format(CommaSeparatedStringSet.class)
		@Name(SUPPORTED_TEMPLATES)
		Set<String> getSupportedTemplates();
	}

	/**
	 * Creates a {@link EditLayoutCommand} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	public EditLayoutCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	private Config config() {
		return (Config) getConfig();
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		String myScope = LayoutTemplateUtils.getNonNullNameScope(component);
		LayoutComponent topLevel = component.getMainLayout().getComponentForLayoutKey(myScope);

		if (topLevel == null) {
			return new Failure(I18NConstants.RESOLVE_COMPONENT_ERROR__LAYOUT_KEY.fill(myScope));
		}

		LayoutComponent parent = topLevel.getParent();
		String parentScope = LayoutTemplateUtils.getNonNullNameScope(parent);
		LayoutComponent layout = component.getMainLayout().getComponentForLayoutKey(parentScope);

		if (layout == null) {
			return new Failure(I18NConstants.RESOLVE_COMPONENT_ERROR__LAYOUT_KEY.fill(parentScope));
		}
		TLLayout tlLayout = LayoutStorage.getInstance().getLayout(parentScope);
		if (tlLayout == null) {
			/* No TLLayout found: That may happen, when the in app created layouts are exported to
			 * file system. In this case the generated scopes (e.g.
			 * 770b54cb-217e-44f9-b213-4aa4b2f7d4df.layout.xml) are replaced by user friendly
			 * scopes. The LayoutStorage is a listener at the file system, so that a layout with
			 * generated scope can no longer be found. */
			return new Hide();
		}
		if (!tlLayout.hasTemplate()) {
			// Non template layout includes template using this command. Hide it.
			return new Hide();
		}
		if (!config().getSupportedTemplates().contains(tlLayout.getTemplateName())) {
			// Unsupported template layout includes template using this command. Hide it.
			return new Hide();
		}

		return new Success() {

			@Override
			protected void doExecute(DisplayContext context) {
				EditComponentCommand.openEditComponentDialog(context, layout, parentScope, tlLayout);
			}
		};
	}

	@Override
	protected ExecutabilityRule intrinsicExecutability() {
		return CombinedExecutabilityRule.combine(InDesignModeExecutable.INSTANCE, new UniqueToolbarCommandRule(this),
			super.intrinsicExecutability());
	}

}
