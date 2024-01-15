/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts.template;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.LinkGenerator;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.CommandHandler.ExecutabilityConfig;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.CombinedExecutabilityRule;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * Definition of a {@link PostCreateAction} to be invoked by an <code>onclick</code> handler.
 */
public class UIAction extends AbstractVariableDefinition<UIAction.Config> {

	private final QueryExecutor _targetObject;

	private final List<PostCreateAction> _actions;

	private final ExecutabilityRule _executability;

	/**
	 * Definition of an <code>onclick</code> script that jumps to an object or opens a dialog for an
	 * object.
	 * 
	 * <p>
	 * The variable that is defined by a link computation must be expanded within an
	 * <code>onclick</code> attribute of some element that should trigger the link.
	 * </p>
	 */
	@DisplayOrder({
		Config.NAME,
		Config.TARGET_OBJECT,
		Config.EXECUTABILITY_PROPERTY,
		Config.POST_CREATE_ACTIONS
	})
	@TagName("ui-action")
	public interface Config
			extends VariableDefinition.Config<UIAction>, ExecutabilityConfig, WithPostCreateActions.Config {

		/**
		 * @see #getTargetObject()
		 */
		String TARGET_OBJECT = "target-object";

		/**
		 * Function taking the rendered object as argument and computing the object that should be
		 * displayed when clicking.
		 * 
		 * <p>
		 * By default, the rendered object is displayed.
		 * </p>
		 */
		@Name(TARGET_OBJECT)
		Expr getTargetObject();

	}

	/**
	 * Creates a {@link UIAction} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public UIAction(InstantiationContext context, Config config) {
		super(context, config);

		_executability =
			CombinedExecutabilityRule.combine(TypedConfiguration.getInstanceList(context, config.getExecutability()));
		_targetObject = QueryExecutor.compileOptional(config.getTargetObject());
		_actions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
	}

	@Override
	public Object eval(LayoutComponent component, FormEditorContext editorContext, Object model) {
		Object target = _targetObject != null ? _targetObject.execute(model) : model;
		return new LinkFragment(component, target);
	}

	private final class LinkFragment implements HTMLFragment, Command {
		private final LayoutComponent _component;

		private final Object _target;

		/**
		 * Creates a {@link LinkFragment}.
		 */
		private LinkFragment(LayoutComponent component, Object target) {
			_component = component;
			_target = target;
		}

		@Override
		public void write(DisplayContext context, TagWriter out) throws IOException {
			LinkGenerator.renderLink(context, out, this);
		}

		@Override
		public HandlerResult executeCommand(DisplayContext context) {
			ExecutableState state = _executability.isExecutable(_component, _target, Collections.emptyMap());
			// Simply ignore non-executable callbacks, since there is no way to disable them at the
			// UI.
			if (state.isExecutable()) {
				WithPostCreateActions.processCreateActions(_actions, _component, _target);
			}
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}
