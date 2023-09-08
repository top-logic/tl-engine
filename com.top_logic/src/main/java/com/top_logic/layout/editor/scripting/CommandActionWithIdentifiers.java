/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.editor.scripting;

import java.util.LinkedList;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.editor.LayoutTemplateUtils;
import com.top_logic.layout.scripting.action.CommandAction;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractCommandActionOp;
import com.top_logic.layout.scripting.runtime.action.CommandActionOpBase;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Special {@link CommandAction} that hold an additional list of component identifiers.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface CommandActionWithIdentifiers extends CommandAction, WithIdentifiers {

	@Override
	@ClassDefault(CommandActionOpWithIdentifiers.class)
	Class<CommandActionOpBase<?>> getImplementationClass();

	/**
	 * Default implementation of {@link CommandActionWithIdentifiers}.
	 */
	public class CommandActionOpWithIdentifiers extends AbstractCommandActionOp<CommandActionWithIdentifiers> {

		/**
		 * @param context
		 *        {@link InstantiationContext} context to instantiate sub configurations.
		 * @param config
		 *        {@link CommandActionWithIdentifiers} configuration for this operation.
		 */
		public CommandActionOpWithIdentifiers(InstantiationContext context, CommandActionWithIdentifiers config) {
			super(context, config);
		}

		@Override
		protected HandlerResult executeCommand(ActionContext context, LayoutComponent component) {
			DisplayContext displayContext = context.getDisplayContext();
			displayContext.set(LayoutTemplateUtils.COMPONENT_IDENTIFIERS,
				new LinkedList<>(config.getIdentifiers().getComponentKeys()));
			displayContext.set(LayoutTemplateUtils.UUIDS, new LinkedList<>(config.getIdentifiers().getUUIDs()));
			try {
				return super.executeCommand(context, component);
			} finally {
				displayContext.reset(LayoutTemplateUtils.UUIDS);
				displayContext.reset(LayoutTemplateUtils.COMPONENT_IDENTIFIERS);
			}
		}

	}

}

