/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.func.IFunction1;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.ScriptFunction1;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ExecutabilityRule} that disables a command operating on the anonymous account.
 *
 * <p>
 * The anonymous account is allocated by the {@link PersonManager} and represents a visitor that did
 * not log in. It is technical infrastructure without a login of its own, so operating on it is
 * pointless - a changed name alone would silently detach the application from its anonymous account.
 * </p>
 *
 * <p>
 * The account is computed from the target model of the command by the configured
 * {@link Config#getAccount() function}, so that the rule can be used on a component whose model only
 * refers to an account, such as the contact of the account administration.
 * </p>
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class AnonymousAccountDisabled extends AbstractConfiguredInstance<AnonymousAccountDisabled.Config<?>>
		implements ExecutabilityRule {

	/**
	 * Configuration options for {@link AnonymousAccountDisabled}.
	 */
	@TagName("anonymous-account-disabled")
	public interface Config<I extends AnonymousAccountDisabled> extends PolymorphicConfiguration<I> {

		/** @see #getAccount() */
		String ACCOUNT = "account";

		/** @see #getMessage() */
		String MESSAGE = "message";

		/**
		 * Function computing the account from the target model of the command.
		 *
		 * <p>
		 * The function takes the target model as single argument and must deliver the account it
		 * describes, e.g.:
		 * </p>
		 *
		 * <pre>
		 * <code>contact -> $contact.get(`Contacts:PersonContact#account`)</code>
		 * </pre>
		 *
		 * <p>
		 * If no function is specified, the target model itself is expected to be the account. A
		 * result that is not an account allows the execution, since the command then does not operate
		 * on the anonymous account.
		 * </p>
		 */
		@Name(ACCOUNT)
		ScriptFunction1<Object, Object> getAccount();

		/**
		 * Reason displayed to the user, when the command is disabled because it operates on the
		 * anonymous account.
		 *
		 * <p>
		 * If no message is specified, a generic message is shown that states that the command cannot
		 * be executed for the anonymous account. Configure a message describing the denied operation,
		 * e.g. that the account cannot be edited.
		 * </p>
		 */
		@Name(MESSAGE)
		ResKey getMessage();

	}

	private final IFunction1<Object, Object> _account;

	/**
	 * Creates a {@link AnonymousAccountDisabled} from configuration.
	 *
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public AnonymousAccountDisabled(InstantiationContext context, Config<?> config) {
		super(context, config);

		_account = context.getInstance(config.getAccount());
	}

	@Override
	public ExecutableState isExecutable(LayoutComponent component, Object model, Map<String, Object> values) {
		Object account = _account == null ? model : _account.apply(model);
		if (account instanceof Person person && PersonManager.getManager().isAnonymous(person)) {
			return ExecutableState.createDisabledState(message());
		}
		return ExecutableState.EXECUTABLE;
	}

	/**
	 * The reason for the disabled state, see {@link Config#getMessage()}.
	 */
	private ResKey message() {
		ResKey configured = getConfig().getMessage();
		return configured == null ? I18NConstants.ERROR_NOT_EXECUTABLE_FOR_ANONYMOUS_ACCOUNT : configured;
	}

}
