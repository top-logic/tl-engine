/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.table;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.component.ObjectRevealer;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Test command that reveals the current selection of the component it is placed on.
 *
 * <p>
 * This command exists to manually verify the "show object in component" feature (ticket #29009):
 * select a node, collapse an ancestor so that the (still selected) node becomes invisible, then
 * press this button to make the selection visible again. It is a thin trigger for
 * {@link ObjectRevealer#revealObject(Object)} on the current selection; the reusable, composable
 * variant of the feature is the {@code reveal} {@link com.top_logic.layout.form.component.PostCreateAction}.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class RevealSelectionTestCommand extends AbstractCommandHandler {

	/**
	 * Configuration options for {@link RevealSelectionTestCommand}.
	 */
	public interface Config extends AbstractCommandHandler.Config {

		/** The default value for {@link #getId()}. */
		String ID = "revealSelectionTest";

		@Override
		@StringDefault(ID)
		String getId();

	}

	/**
	 * Creates a {@link RevealSelectionTestCommand} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration for this command.
	 */
	public RevealSelectionTestCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext context, LayoutComponent component, Object model,
			Map<String, Object> someArguments) {
		if (component instanceof ObjectRevealer) {
			((ObjectRevealer) component).revealObject(leafObject(model));
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	/**
	 * The single object to reveal for the command's target model.
	 *
	 * <p>
	 * The selection of a tree component is a path of business objects from the root to the selected
	 * node, and a multiple selection is a collection of such values. This reduces the target model
	 * to the leaf object that should be revealed.
	 * </p>
	 */
	private static Object leafObject(Object model) {
		Object result = model;
		if (result instanceof Collection<?> && !(result instanceof List<?>)) {
			Collection<?> selection = (Collection<?>) result;
			if (selection.isEmpty()) {
				return null;
			}
			result = selection.iterator().next();
		}
		if (result instanceof List<?>) {
			List<?> path = (List<?>) result;
			if (path.isEmpty()) {
				return null;
			}
			result = path.get(path.size() - 1);
		}
		return result;
	}

}
