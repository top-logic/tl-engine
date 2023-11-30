/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import java.io.IOException;
import java.util.Map;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.layout.ControlScope;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LinkGenerator;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * Definition of a callback function that receives data from the UI.
 */
@Label("UI callback")
public class UICallback extends AbstractVariableDefinition<UICallback.Config> {

	private QueryExecutor _action;

	/**
	 * Definition of a callback from the UI.
	 * 
	 * <p>
	 * The callback can be invoked from a client-side JavaScript snippet. When defining a UI
	 * callback with name "myhandler", you can invoke the callback from the HTML template by calling
	 * <code>services.form.callback({myhandler}, arg1, arg2,...)"</code> from within an event
	 * handler (such as <code>onclick</code>). The defined action script receives the arguments
	 * passed to the callback as a single argument. The action argument has list type, if there are
	 * more than one arguments in the callback invocation.
	 * </p>
	 * 
	 * <p>
	 * A complete snippet invoking a UI callback named "myhandler" and transmitting a list of
	 * strings <code>['first', 'second']</code> when an image is clicked looks like the following:
	 * </p>
	 * 
	 * <pre>
	 * <img src="..." onclick="services.form.callback({myhandler}, 'first', 'second')"/>
	 * </pre>
	 */
	@DisplayOrder({
		Config.NAME,
		Config.ACTION,
	})
	@TagName("ui-callback")
	public interface Config extends VariableDefinition.Config<UICallback> {

		/**
		 * @see #getAction()
		 */
		String ACTION = "action";

		/**
		 * Script invoked, when a callback from the client-side arrives.
		 * 
		 * <p>
		 * To trigger the action, invoke
		 * <code>services.form.callback({myhandler}, arg1, arg2,...)"</code> from within an event
		 * handler (such as <code>onclick</code>).
		 * </p>
		 * 
		 * <p>
		 * The script expects exactly one argument. If more than one argument is transmitted from
		 * the client, the script receives a list of those arguments. The values transmitted from
		 * the client may be any JSON compliant type (<code>string</code>, <code>number</code>,
		 * <code>boolean</code>, <code>list</code>, <code>map</code>).
		 * </p>
		 */
		@Name(ACTION)
		Expr getAction();

	}

	/**
	 * Creates a {@link UICallback} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UICallback(InstantiationContext context, Config config) {
		super(context, config);

		_action = QueryExecutor.compileOptional(config.getAction());
	}

	@Override
	public Object eval(LayoutComponent component, FormEditorContext editorContext, Object model) {
		return new CallbackFragment(_action, model);
	}

	private static final class CallbackFragment implements HTMLFragment {

		private QueryExecutor _action;

		private Object _model;

		public CallbackFragment(QueryExecutor action, Object model) {
			_action = action;
			_model = model;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			ControlScope scope = context.getExecutionScope();
			if (scope == null) {
				throw new IllegalStateException("Generating callbacks is not possible in command phase.");
			}
			
			LinkGenerator.Callback callback;
			callback = new LinkGenerator.Callback(scope) {
				@Override
				public HandlerResult executeCommand(DisplayContext execContext, String commandName,
						Map<String, Object> arguments) {

					try (var tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
						_action.execute(_model, arguments.get("args"));
						tx.commit();
					}

					return HandlerResult.DEFAULT_RESULT;
				}
			};
			scope.addUpdateListener(callback);
			
			out.writeJsString(callback.getID());
		}
	}

}
