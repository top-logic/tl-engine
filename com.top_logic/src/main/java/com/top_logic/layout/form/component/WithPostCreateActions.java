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
 * A handler that opts into post-create actions must:
 * </p>
 * <ol>
 * <li>Implement this interface.</li>
 * <li>Extend {@link WithPostCreateActions.Config} in its own configuration interface.</li>
 * <li>Instantiate the configured actions in its constructor and hold them in a field.</li>
 * <li>Invoke {@link #processCreateActions(List, LayoutComponent, Object)} after the model
 * operation has been committed, passing the object the actions should act on (the "result" of
 * the operation — {@code null} if the command produces nothing meaningful).</li>
 * </ol>
 *
 * <p>
 * Minimal implementation skeleton:
 * </p>
 *
 * <pre>
 * public class MyHandler extends AbstractCommandHandler implements WithPostCreateActions {
 *
 *     public interface Config extends AbstractCommandHandler.Config, WithPostCreateActions.Config {
 *         // ...
 *     }
 *
 *     private final List&lt;PostCreateAction&gt; _postCreateActions;
 *
 *     public MyHandler(InstantiationContext context, Config config) {
 *         super(context, config);
 *         _postCreateActions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
 *     }
 *
 *     public HandlerResult handleCommand(...) {
 *         Object result = doOperation(...); // commits the transaction
 *         WithPostCreateActions.processCreateActions(_postCreateActions, component, result);
 *         return HandlerResult.DEFAULT_RESULT;
 *     }
 * }
 * </pre>
 *
 * <p>
 * Call {@link #processCreateActions(List, LayoutComponent, Object)} <em>after</em> the
 * transaction has been committed — global model events from the commit must have been processed
 * before the actions operate on the result.
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
	 * Utility for invoking {@link PostCreateAction}s in the context of a component.
	 *
	 * <p>
	 * Must be called after the commit of the model operation: this method first drains pending
	 * global model events so the actions can rely on component state being up to date before they
	 * touch the passed result.
	 * </p>
	 *
	 * @param postCreateActions
	 *        The actions to invoke.
	 * @param component
	 *        The context component.
	 * @param newObject
	 *        The object the actions should act on — conventionally the "result" of the command.
	 *        May be {@code null} if the command produces nothing meaningful.
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
			for (PostCreateAction action : postCreateActions) {
				action.handleNew(component, newObject);
			}
		}
	}

}
