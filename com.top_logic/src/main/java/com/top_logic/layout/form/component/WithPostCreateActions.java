/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.List;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Plug-in interface for {@link CommandHandler}s executing {@link PostCreateAction}s.
 * 
 * <p>
 * Note: {@link CommandHandler}s using this interface must also add {@link Config} to their
 * configuration interface.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface WithPostCreateActions {

	/**
	 * Configuration options for {@link WithPostCreateActions}.
	 */
	public interface Config extends ConfigurationItem {

		/**
		 * @see #getPostCreateActions()
		 */
		String POST_CREATE_ACTIONS = "postCreateActions";

		/**
		 * Actions in the user interface to be executed after a model operation.
		 * 
		 * <p>
		 * If applicable, actions operate on the result of the model operation.
		 * </p>
		 * 
		 * @implNote This is a configurable way of enriching a create handler with common actions
		 *           that update the UI after a create operation. Instead,
		 *           {@link AbstractCreateCommandHandler#afterCommit(LayoutComponent, Object)} could
		 *           be overridden to produce the same effect.
		 */
		@Name(POST_CREATE_ACTIONS)
		@Label("User interface actions")
		@Options(fun = AllInAppImplementations.class)
		List<PolymorphicConfiguration<PostCreateAction>> getPostCreateActions();

	}

	/**
	 * Utility for invoking {@link PostCreateAction}s in a context of a component.
	 *
	 * @param postCreateActions
	 *        The actions to invoke.
	 * @param component
	 *        The context component.
	 * @param newObject
	 *        The newly created object.
	 */
	static void processCreateActions(List<PostCreateAction> postCreateActions, LayoutComponent component,
			Object newObject) {
		{
			// Ensure that components have received the model
			// created event, before the new object is used in
			// explicit communication with the components.
			// Otherwise, selection would not work, if the new
			// object was not included into component models.
			// See also Ticket #669.
			component.getMainLayout().processGlobalEvents();
		}

		if (postCreateActions.isEmpty()) {
			// Legacy handling.
			if (component instanceof AbstractCreateComponent) {
				((AbstractCreateComponent) component).handleNew(newObject);
			}
		} else {
			postCreateActions.forEach(action -> action.handleNew(component, newObject));
		}
	}

}
