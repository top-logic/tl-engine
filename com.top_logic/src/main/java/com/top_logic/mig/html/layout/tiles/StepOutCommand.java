/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ItemDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.check.CheckScopeProvider;
import com.top_logic.layout.basic.check.ChildrenCheckScopeProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.tiles.component.StepOutTileCommand;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.PreconditionCommandHandler;
import com.top_logic.tool.boundsec.conditional.Success;

/**
 * Command to display the "tile parent" of a component.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class StepOutCommand extends PreconditionCommandHandler {

	/**
	 * Typed configuration interface definition for {@link StepOutCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PreconditionCommandHandler.Config {

		@Override
		@ItemDefault(ChildrenCheckScopeProvider.class)
		PolymorphicConfiguration<CheckScopeProvider> getCheckScopeProvider();

	}

	/**
	 * Command id to find the instance of the {@link StepOutCommand} within the
	 * {@link CommandHandlerFactory}.
	 */
	public static final String COMMAND_ID = "stepOutTileCommand";

	/**
	 * Creates a new {@link StepOutTileCommand}.
	 */
	public StepOutCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected CommandStep prepare(LayoutComponent component, Object model, Map<String, Object> arguments) {
		RootTileComponent rootTileComponent = (RootTileComponent) component;
		List<LayoutComponent> layoutPath = rootTileComponent.displayedPath();
		LayoutComponent parent = layoutPath.size() > 1 ? layoutPath.get(layoutPath.size() - 2) : null;
		if (parent == null) {
			// Current layout is root. Therefore no step out possible
			return new Hide();
		}
		return new Success() {

			@Override
			protected void doExecute(DisplayContext context) {
				rootTileComponent.displayTileLayout(parent);
			}
		};
	}
}

