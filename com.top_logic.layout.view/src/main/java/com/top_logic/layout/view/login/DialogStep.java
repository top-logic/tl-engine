/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.login;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link LoginStep} showing a dialog view to the account logging in, e.g. to tell it something it
 * has to acknowledge before it gets into the application.
 *
 * <p>
 * The dialog receives the account on its {@link LoginAction#ACCOUNT_CHANNEL} channel and derives
 * what it shows from it. It ends with the action {@link ContinueLoginAction
 * <code>&lt;continue-login/&gt;</code>}, which finishes this step; closing it cancels the login.
 * Whether the step applies is the TL-Script {@link Config#getCondition() condition} over the
 * account.
 * </p>
 *
 * <p>
 * This step asks for nothing but confirmation. A step that has the account prove something must
 * not be a {@link DialogStep}: whatever dialog is due, the general
 * {@link ContinueLoginAction} finishes a step of this kind only.
 * </p>
 */
public class DialogStep extends AbstractConfiguredInstance<DialogStep.Config<?>> implements LoginStep {

	/**
	 * Configuration of a {@link DialogStep}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config<I extends DialogStep> extends LoginStep.Config<I> {

		/** Configuration tag of a {@link DialogStep}. */
		String TAG_NAME = "dialog-step";

		/** Configuration name for {@link #getView()}. */
		String VIEW = "view";

		/** Configuration name for {@link #getCondition()}. */
		String CONDITION = "condition";

		@Override
		@ClassDefault(DialogStep.class)
		Class<? extends I> getImplementationClass();

		/**
		 * The dialog view, relative to {@link ViewLoader#VIEW_BASE_PATH}.
		 *
		 * <p>
		 * The view declares the channel {@link LoginAction#ACCOUNT_CHANNEL}, which carries the
		 * account logging in, and ends with the action <code>&lt;continue-login/&gt;</code>.
		 * </p>
		 */
		@Name(VIEW)
		@Mandatory
		String getView();

		/**
		 * Function taking the account logging in and telling whether the step applies to it, e.g.
		 * <code>account -> $account.get(`...`) != null</code>.
		 *
		 * <p>
		 * Without a condition, the step applies to every account.
		 * </p>
		 *
		 * <p>
		 * The condition is evaluated without access checks: it is part of the application
		 * configuration and runs before the account has a session, on behalf of the anonymous user,
		 * who may read nothing of the model. It decides only whether the step applies; what the
		 * dialog shows runs with the checks of the anonymous user.
		 * </p>
		 */
		@Name(CONDITION)
		Expr getCondition();
	}

	private final QueryExecutor _condition;

	/**
	 * Creates a {@link DialogStep} from configuration.
	 */
	@CalledByReflection
	public DialogStep(InstantiationContext context, Config<?> config) {
		super(context, config);
		_condition = QueryExecutor.compileOptional(config.getCondition());
		if (_condition != null) {
			// Trusted configuration deciding before the account has a session, see Config#getCondition().
			_condition.disableSecurity();
		}
	}

	@Override
	public boolean appliesTo(LoginFlow flow) {
		return _condition == null || SearchExpression.isTrue(_condition.execute(flow.getAccount()));
	}

	@Override
	public void start(ReactContext context, LoginFlow flow) {
		LoginFlow.replaceDialog(context, getConfig().getView(),
			Map.of(LoginAction.ACCOUNT_CHANNEL, flow.getAccount()));
	}

}
