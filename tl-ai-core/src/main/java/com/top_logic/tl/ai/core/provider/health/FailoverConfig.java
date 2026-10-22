/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.provider.health;

import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.EntryTag;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.tl.ai.core.agent.AgentTuning;

/**
 * Where a run goes when the model it was configured with cannot answer.
 *
 * <p>
 * Empty by default, and an empty section changes nothing: a run that loses its model fails exactly
 * as it did before. An installation that lists a fallback buys the other behaviour - the run
 * continues on the next healthy model of the chain, and the switch is recorded on the run so an
 * operator sees why it ended up somewhere else than configured.
 * </p>
 *
 * <p>
 * The chain is looked up by model <em>name</em>, whichever provider serves it: a fallback is about
 * the model an agent asked for, and the agents that ask for the same model want the same way out.
 * A fallback entry may name its own provider, which is how the same model served by two accounts -
 * or by Anthropic and by Bedrock - becomes a fallback for itself.
 * </p>
 *
 * @see ModelAvailability
 */
@TagName("failover")
@DisplayOrder({
	FailoverConfig.QUOTA_TTL_SECONDS,
	FailoverConfig.CAPACITY_TTL_SECONDS,
	FailoverConfig.TRANSIENT_TTL_SECONDS,
	FailoverConfig.RATE_LIMIT_WAIT_SECONDS,
	FailoverConfig.CHAINS,
})
public interface FailoverConfig extends ConfigPart {

	/** Configuration property name for {@link #getQuotaTtlSeconds()}. */
	String QUOTA_TTL_SECONDS = "quota-ttl-seconds";

	/** Configuration property name for {@link #getCapacityTtlSeconds()}. */
	String CAPACITY_TTL_SECONDS = "capacity-ttl-seconds";

	/** Configuration property name for {@link #getTransientTtlSeconds()}. */
	String TRANSIENT_TTL_SECONDS = "transient-ttl-seconds";

	/** Configuration property name for {@link #getRateLimitWaitSeconds()}. */
	String RATE_LIMIT_WAIT_SECONDS = "rate-limit-wait-seconds";

	/** Configuration property name for {@link #getChains()}. */
	String CHAINS = "chains";

	/**
	 * How long a {@link HealthCause#QUOTA} mark holds, in seconds.
	 *
	 * <p>
	 * An exhausted budget or a rejected key does not heal by itself, so this is the long one: a day
	 * by default, which outlives the daily windows the providers reset on. {@code 0} makes the mark
	 * hold until an administrator resets it, which is the right setting where somebody watches.
	 * </p>
	 *
	 * @see #QUOTA_TTL_SECONDS
	 */
	@Name(QUOTA_TTL_SECONDS)
	@Label("Quota mark lifetime (s)")
	@IntDefault(86_400)
	int getQuotaTtlSeconds();

	/**
	 * How long a {@link HealthCause#CAPACITY} mark holds, in seconds.
	 *
	 * <p>
	 * Rate limiting and overload pass within seconds, and the run's own backoff has already waited
	 * out the short ones by the time a failure reaches here. Long enough to get the rest of the run
	 * onto another model, short enough that the next conversation starts where it was configured.
	 * </p>
	 *
	 * @see #CAPACITY_TTL_SECONDS
	 */
	@Name(CAPACITY_TTL_SECONDS)
	@Label("Capacity mark lifetime (s)")
	@IntDefault(30)
	int getCapacityTtlSeconds();

	/**
	 * How long a {@link HealthCause#TRANSIENT} mark holds, in seconds.
	 *
	 * <p>
	 * A mark this weak never blocks a model outright - it costs the model one attempt per turn -
	 * so its lifetime only decides how long the fault is remembered.
	 * </p>
	 *
	 * @see #TRANSIENT_TTL_SECONDS
	 */
	@Name(TRANSIENT_TTL_SECONDS)
	@Label("Transient mark lifetime (s)")
	@IntDefault(300)
	int getTransientTtlSeconds();

	/**
	 * How long, in seconds, an agent waits in total for a model that answers "too many requests"
	 * (HTTP 429) before its turn fails.
	 *
	 * <p>
	 * The agent asks again with growing pauses, never sooner than the model's service asks for,
	 * and the chat shows the user that it is waiting. Set it to at least the time one request of
	 * the slowest model takes: what holds the model is usually another request of the same key. A
	 * run whose model has a fallback chain does not wait this long - it moves on to the fallback
	 * after its retries.
	 * </p>
	 *
	 * @see #RATE_LIMIT_WAIT_SECONDS
	 */
	@Name(RATE_LIMIT_WAIT_SECONDS)
	@Label("Rate-limit wait (s)")
	@IntDefault(AgentTuning.DEFAULT_RATE_LIMIT_WAIT_SECONDS)
	int getRateLimitWaitSeconds();

	/**
	 * The fallback chains, keyed by the model they are the way out of.
	 *
	 * <p>
	 * A model that is not listed has no fallback, which is the default for every model.
	 * </p>
	 *
	 * @see #CHAINS
	 */
	@Name(CHAINS)
	@EntryTag("model")
	@Key(NamedConfigMandatory.NAME_ATTRIBUTE)
	Map<String, Chain> getChains();

	/**
	 * Where runs of one model go when it cannot answer, in order.
	 */
	@TagName("model")
	interface Chain extends NamedConfigMandatory, ConfigPart {

		/** Configuration property name for {@link #getFallbacks()}. */
		String FALLBACKS = "fallbacks";

		/**
		 * The model this chain is the way out of.
		 */
		@Override
		@Label("Model")
		String getName();

		/**
		 * The models to try instead, in order.
		 *
		 * <p>
		 * Tried from the first onwards, skipping the ones that are themselves marked. A chain
		 * whose entries are all marked fails the run with the error of the last model that was
		 * actually called.
		 * </p>
		 *
		 * @see #FALLBACKS
		 */
		@Name(FALLBACKS)
		@EntryTag("fallback")
		List<Target> getFallbacks();
	}

	/**
	 * One model in a fallback chain.
	 */
	@TagName("fallback")
	interface Target extends ConfigurationItem {

		/** Configuration property name for {@link #getModel()}. */
		String MODEL = "model";

		/** Configuration property name for {@link #getProvider()}. */
		String PROVIDER = "provider";

		/**
		 * The model to run on instead.
		 *
		 * <p>
		 * Cross-provider is the point: the tool schemas, the response format and the sampling
		 * options are rebuilt for whatever provider serves this model.
		 * </p>
		 *
		 * @see #MODEL
		 */
		@Name(MODEL)
		@Label("Model")
		@com.top_logic.basic.config.annotation.Mandatory
		String getModel();

		/**
		 * The configured provider that must serve {@link #getModel()}, or empty when the model's
		 * only provider serves it.
		 *
		 * <p>
		 * Needed exactly where {@code agent-execution} needs it: a model two provider entries
		 * expose has to say which one it means.
		 * </p>
		 *
		 * @see #PROVIDER
		 */
		@Name(PROVIDER)
		@Label("Provider")
		String getProvider();
	}
}
