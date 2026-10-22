/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.agent;

import com.top_logic.base.services.CurrencySystem;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.tl.ai.core.agent.environment.EnvironmentSettings;
import com.top_logic.tl.ai.core.agent.environment.UserMemoryConfig;
import com.top_logic.tl.ai.core.cost.CostConversion;
import com.top_logic.tl.ai.core.limits.BudgetConfig;
import com.top_logic.tl.ai.core.limits.BudgetConfig.RoleBudget;
import com.top_logic.tl.ai.core.limits.RunLimitSettings;

/**
 * What an agent run may cost and what an agent may keep about a person: the token and money budget
 * of an account, the memories an agent keeps across conversations, and the preference text an
 * account gives every agent.
 *
 * <p>
 * Set by the administration - and, for what is kept about a person, by data protection. A change
 * takes effect when this service is restarted; nothing else restarts with it.
 * </p>
 *
 * <p>
 * Part of the split of the former {@code LLMService} (Gitea #691).
 * </p>
 */
@Label("Agent policy")
@ServiceDependencies({})
public class AgentPolicyService extends ConfiguredManagedClass<AgentPolicyService.Config<?>> {

	/**
	 * Configuration interface for {@link AgentPolicyService}.
	 */
	@DisplayOrder({
		Config.BUDGET,
		Config.USER_MEMORY,
		Config.USER_PREFERENCES,
		Config.MAX_PREFERENCES_LENGTH,
	})
	public interface Config<I extends AgentPolicyService> extends ConfiguredManagedClass.Config<I> {

		/** Configuration property name for {@link #getBudget()}. */
		String BUDGET = "budget";

		/** Configuration property name for {@link #getUserMemory()}. */
		String USER_MEMORY = "user-memory";

		/** Configuration property name for {@link #getUserPreferences()}. */
		String USER_PREFERENCES = "user-preferences";

		/** Configuration property name for {@link #getMaxPreferencesLength()}. */
		String MAX_PREFERENCES_LENGTH = "max-preferences-length";

		/**
		 * How many tokens and how much money one account may spend in a rolling window, and the
		 * currency this installation states the cost of a run in.
		 *
		 * @see #BUDGET
		 */
		@Name(BUDGET)
		BudgetConfig getBudget();

		/**
		 * What an agent may remember about the accounts of this installation: whether it may at
		 * all, how much of it reaches a model, and the words the agreement check looks for.
		 *
		 * @see #USER_MEMORY
		 */
		@Name(USER_MEMORY)
		UserMemoryConfig getUserMemory();

		/**
		 * Whether the accounts' own preference texts are part of the environment block.
		 *
		 * <p>
		 * Switched off, no preference text reaches any model, whatever the accounts have written. The
		 * texts are kept - this is a switch about what is sent, not about what is stored - so switching
		 * it back on restores them.
		 * </p>
		 *
		 * @see #USER_PREFERENCES
		 */
		@Name(USER_PREFERENCES)
		@Label("User preferences")
		@BooleanDefault(true)
		boolean getUserPreferences();

		/**
		 * How long an account's preference text may be, in characters.
		 *
		 * <p>
		 * The text goes into every single request of that account, so its length is paid for on every
		 * turn of every conversation. A longer text is refused when it is entered, with a message
		 * naming the limit.
		 * </p>
		 *
		 * @see #MAX_PREFERENCES_LENGTH
		 */
		@Name(MAX_PREFERENCES_LENGTH)
		@Label("Maximum preferences length")
		@IntDefault(EnvironmentSettings.DEFAULT_MAX_PREFERENCES_LENGTH)
		int getMaxPreferencesLength();
	}

	private static volatile AgentPolicyService _instance;

	/**
	 * Creates a new {@link AgentPolicyService} from configuration.
	 *
	 * @param context
	 *        The instantiation context for error reporting.
	 * @param config
	 *        The service configuration.
	 */
	@CalledByReflection
	public AgentPolicyService(InstantiationContext context, Config<?> config) {
		super(context, config);
	}

	/**
	 * The running instance, or {@code null} while the service is not started.
	 */
	public static AgentPolicyService getInstance() {
		return _instance;
	}

	/**
	 * Whether the service is up; a run is not started while it is not, because its account's budget
	 * could not be asked.
	 */
	public static boolean isRunning() {
		return _instance != null;
	}

	@Override
	protected void startUp() {
		super.startUp();
		warnWithoutRate(getConfig().getBudget());
		refuseEmptyRoleBudgets(getConfig().getBudget());
		_instance = this;
	}

	/**
	 * Stops the start when a role budget names neither tokens nor money.
	 *
	 * <p>
	 * Such an entry says nothing: its holders get the section's budget. Before the two figures could
	 * be left empty (#747), {@code <role-budget name="..."/>} gave the role an unlimited token budget,
	 * and an installation that exempted a role that way would find it limited after the upgrade.
	 * Refusing the entry makes the administrator write what is meant (#825).
	 * </p>
	 */
	private static void refuseEmptyRoleBudgets(BudgetConfig budget) {
		if (budget == null) {
			return;
		}
		for (RoleBudget role : budget.getRoleBudgets().values()) {
			if (role.getTokens() == null && role.getMoney() == null) {
				throw new RuntimeException("The <role-budget name=\"" + role.getName()
					+ "\"/> of AgentPolicyService names neither tokens nor money, so it changes nothing for"
					+ " the role. Write tokens=\"0\" (and money=\"0\") to exempt the role from the budget,"
					+ " give it the figures it may spend, or delete the entry.");
			}
		}
	}

	/**
	 * Tells the log at the start when a money budget is configured that cannot be checked today.
	 *
	 * <p>
	 * The money budget is stated in the system currency and runs record US dollars; without a rate of
	 * the currency "USD" valid today the budget is not checked (see {@link CostConversion}). The
	 * start does not fail over it - nobody is to be blocked for a rate nobody entered - but the
	 * administrator learns it here and not only from the first run. The currency system is asked
	 * only when it is already running; otherwise the first budget check tells the log.
	 * </p>
	 */
	private static void warnWithoutRate(BudgetConfig budget) {
		if (budget == null || !RunLimitSettings.namesMoney(budget) || !CurrencySystem.Module.INSTANCE.isActive()) {
			return;
		}
		if (CostConversion.usdRate(null).isEmpty()) {
			CostConversion.warnNoRate("the money budget of AgentPolicyService is not checked");
		}
	}

	@Override
	protected void shutDown() {
		try {
			_instance = null;
		} finally {
			super.shutDown();
		}
	}

	/**
	 * The budget section; <code>null</code> when nothing is configured.
	 */
	public BudgetConfig getBudget() {
		return getConfig().getBudget();
	}

	/**
	 * What an agent may remember about the accounts of this installation; <code>null</code> when
	 * nothing is configured.
	 */
	public UserMemoryConfig getUserMemory() {
		return getConfig().getUserMemory();
	}

	/**
	 * Whether the accounts' preference texts are sent at all.
	 */
	public boolean getUserPreferences() {
		return getConfig().getUserPreferences();
	}

	/**
	 * How long an account's preference text may be, in characters.
	 */
	public int getMaxPreferencesLength() {
		return getConfig().getMaxPreferencesLength();
	}

	/**
	 * Module for {@link AgentPolicyService}.
	 */
	public static final class Module extends TypedRuntimeModule<AgentPolicyService> {

		/**
		 * Singleton {@link AgentPolicyService.Module} instance.
		 */
		public static final AgentPolicyService.Module INSTANCE = new AgentPolicyService.Module();

		private Module() {
			// Singleton constructor
		}

		@Override
		public Class<AgentPolicyService> getImplementation() {
			return AgentPolicyService.class;
		}
	}
}
