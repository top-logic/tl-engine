/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.component.AbstractApplyAttributedCommandHandler;
import com.top_logic.element.meta.form.component.DefaultApplyAttributedCommandHandler;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.layout.form.FormHandler;
import com.top_logic.layout.form.component.PostCreateAction;
import com.top_logic.layout.form.component.WithPostCreateActions;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.tool.boundsec.CommandHandlerUtil;

/**
 * Apply command handler that can be extended with an operation in TL-Script.
 */
@InApp(classifiers = "form")
public class ApplyCommandByExpression extends DefaultApplyAttributedCommandHandler {

	/**
	 * Configuration options for {@link DefaultApplyAttributedCommandHandler}.
	 */
	public interface Config
			extends AbstractApplyAttributedCommandHandler.Config, WithPostCreateActions.Config, UIOptions {

		@Override
		@StringDefault("storeAttributes")
		String getId();
	}

	/**
	 * Options for {@link ApplyCommandByExpression} that can be directly configured from wihin the
	 * layout template.
	 */
	public interface UIOptions extends ConfigurationItem {
		/**
		 * Operation that is executed in the same transaction that stores the edited attribute
		 * values.
		 * 
		 * <p>
		 * The function receives the component's model as first and the current form data as second
		 * argument. When this operation is called, values have already been transfered from the
		 * form data to the model object.
		 * </p>
		 * 
		 * <pre>
		 * <code>model -> form -> ...</code>
		 * </pre>
		 */
		@Nullable
		@Name("additional-operation")
		Expr getAdditionalOperation();

		/**
		 * Function computing a confirm message.
		 * 
		 * <p>
		 * The function receives the component's model as first and the current form data as second
		 * argument.
		 * </p>
		 * 
		 * <p>
		 * The function is expected to return a confirm message. If the result is <code>null</code>,
		 * no confirmation is required. The confirm message must either be a string or an
		 * internationalized text.
		 * </p>
		 * 
		 * <pre>
		 * <code>model -> form -> ...</code>
		 * </pre>
		 */
		@Name("dynamic-confirm")
		Expr getDynamicConfirm();
	}

	private final QueryExecutor _operation;

	private final List<PostCreateAction> _actions;

	private final QueryExecutor _confirm;

	/**
	 * Creates a {@link ApplyCommandByExpression}.
	 */
	public ApplyCommandByExpression(InstantiationContext context, Config config) {
		super(context, config);

		_actions = TypedConfiguration.getInstanceList(context, config.getPostCreateActions());
		_operation = QueryExecutor.compileOptional(config.getAdditionalOperation());
		_confirm = QueryExecutor.compileOptional(config.getDynamicConfirm());
	}

	@Override
	public ResKey getConfirmKey(LayoutComponent component, Map<String, Object> arguments) {
		if (_confirm != null) {
			Object model = CommandHandlerUtil.getTargetModel(this, component, arguments);
			TLFormObject form = formOverlay(component, model);
			ResKey confirmKey = SearchExpression.asResKey(_confirm.execute(model, form));
			if (confirmKey != null) {
				return confirmKey;
			}
		}

		return super.getConfirmKey(component, arguments);
	}

	@Override
	protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
		boolean result = super.storeChanges(component, formContext, model);

		if (_operation != null) {
			TLFormObject form = formOverlay(component, model);
			_operation.execute(model, form);
		}

		return result;
	}

	private TLFormObject formOverlay(LayoutComponent component, Object model) {
		return ((AttributeFormContext) ((FormHandler) component).getFormContext())
			.getAttributeUpdateContainer().editObject((TLObject) model);
	}

	@Override
	protected void updateComponent(LayoutComponent component, FormContext formContext, Object model) {
		super.updateComponent(component, formContext, model);

		WithPostCreateActions.processCreateActions(_actions, component, model);
	}
}
