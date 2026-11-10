/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.base.accesscontrol.ExternalIdentity;
import com.top_logic.base.accesscontrol.ExternalLoginListener;
import com.top_logic.base.accesscontrol.Login.LoginDeniedException;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.search.expr.ScriptAbort;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.DeferredQueryExecutor;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link ExternalLoginListener} that executes TL-Script expressions.
 *
 * <p>
 * The scripts run in the transaction of the login, without security: no user is logged in yet,
 * and the scripts are part of the configuration of the application. They may modify the account
 * (or its contact) and create objects. A script that fails refuses the login. A message that a
 * script raises with <code>throw(...)</code> is shown to the user as the reason of the refusal.
 * </p>
 */
@InApp
@Label("Login listener with TL-Script")
public class ExternalLoginListenerByExpression
		extends AbstractConfiguredInstance<ExternalLoginListenerByExpression.Config>
		implements ExternalLoginListener {

	/**
	 * Typed configuration interface definition for {@link ExternalLoginListenerByExpression}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<ExternalLoginListenerByExpression> {

		/** Tag name of an {@link ExternalLoginListenerByExpression} in a list of listeners. */
		String TAG_NAME = "login-script";

		/** Configuration name of {@link #getOnCreate()}. */
		String ON_CREATE = "on-create";

		/** Configuration name of {@link #getOnLogin()}. */
		String ON_LOGIN = "on-login";

		/**
		 * TL-Script executed when a login has created an account.
		 *
		 * <p>
		 * The function gets three arguments: the created account, the claims of the identity as
		 * dictionary from claim name to value, and the name of the client through which the user
		 * has logged in. Its result is ignored. The script may modify the account and its contact
		 * and create objects.
		 * </p>
		 *
		 * <p>
		 * Example taking the first name of an account from the claim <code>given_name</code>,
		 * without a prefix <code>Last, </code> that the identity provider erroneously delivers:
		 * </p>
		 *
		 * <pre>
		 * account -&gt; claims -&gt; client -&gt;
		 *   $account.set(`tl.accounts:Person#firstName`,
		 *     regex("^[^,]*, *").regexReplace($claims["given_name"], ""))
		 * </pre>
		 *
		 * @see ExternalLoginListener#onCreate(Person, ExternalIdentity)
		 */
		@Name(ON_CREATE)
		@Nullable
		Expr getOnCreate();

		/**
		 * TL-Script executed at every login of an account.
		 *
		 * <p>
		 * The function gets the same arguments as {@link #getOnCreate()}: the account, the claims
		 * of the identity as dictionary from claim name to value, and the name of the client. Its
		 * result is ignored. For a created account, it runs after {@link #getOnCreate()}.
		 * </p>
		 *
		 * <p>
		 * Example refusing the login of a user without the claim <code>department</code>:
		 * </p>
		 *
		 * <pre>
		 * account -&gt; claims -&gt; client -&gt;
		 *   if($claims["department"] == null, throw("No department assigned."))
		 * </pre>
		 *
		 * @see ExternalLoginListener#onLogin(Person, ExternalIdentity)
		 */
		@Name(ON_LOGIN)
		@Nullable
		Expr getOnLogin();

	}

	private final QueryExecutor _onCreate;

	private final QueryExecutor _onLogin;

	/**
	 * Creates a {@link ExternalLoginListenerByExpression}.
	 *
	 * <p>
	 * The scripts are compiled at their first execution: the listener is created with the
	 * authentication configuration, possibly before the model and TL-Script services are started.
	 * </p>
	 *
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public ExternalLoginListenerByExpression(InstantiationContext context, Config config) {
		super(context, config);
		_onCreate = executor(config.getOnCreate());
		_onLogin = executor(config.getOnLogin());
	}

	private static QueryExecutor executor(Expr expr) {
		if (expr == null) {
			return null;
		}
		QueryExecutor result = new DeferredQueryExecutor(expr);
		result.disableSecurity();
		return result;
	}

	@Override
	public void onCreate(Person account, ExternalIdentity identity) {
		run(_onCreate, account, identity);
	}

	@Override
	public void onLogin(Person account, ExternalIdentity identity) {
		run(_onLogin, account, identity);
	}

	private static void run(QueryExecutor script, Person account, ExternalIdentity identity) {
		if (script == null) {
			return;
		}
		try {
			script.execute(account, identity.getClaims(), identity.getClientName());
		} catch (ScriptAbort ex) {
			LoginDeniedException denied = new LoginDeniedException(ex.getErrorKey(),
				"Login script refused " + identity + ": " + ex.getMessage());
			denied.initCause(ex);
			throw denied;
		}
	}

}
