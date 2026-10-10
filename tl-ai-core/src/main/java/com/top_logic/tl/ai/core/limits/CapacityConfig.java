/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.limits;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.config.order.DisplayOrder;

/**
 * How many agent runs this installation carries at once.
 *
 * <p>
 * The agent runs inside a shared application server on behalf of many users. With nothing
 * configured here every run gets a thread. <b>Every ceiling is off until it is configured</b>: this
 * product runs in applications with a thousand users, and a figure it picked for them would be wrong
 * for most. Where a ceiling is set, a run that meets it waits where waiting is the better answer -
 * see {@link #getChatQueue()} - and is refused where it is not.
 * </p>
 *
 * <p>
 * The per-account cap counts the <em>principal's</em> runs. A delegated sub-agent is part of the run
 * that delegated it, not a run of its own, and is therefore not counted against it.
 * </p>
 *
 * @see RunLimitSettings
 * @see RunLimits
 */
@TagName("capacity")
@DisplayOrder({
	CapacityConfig.CHAT_WORKERS,
	CapacityConfig.CHAT_QUEUE,
	CapacityConfig.CONCURRENT_RUNS_PER_ACCOUNT,
	CapacityConfig.SUBAGENT_WORKERS,
})
public interface CapacityConfig extends ConfigPart {

	/** Configuration property name for {@link #getChatWorkers()}. */
	String CHAT_WORKERS = "chat-workers";

	/** Configuration property name for {@link #getChatQueue()}. */
	String CHAT_QUEUE = "chat-queue";

	/** Configuration property name for {@link #getConcurrentRunsPerAccount()}. */
	String CONCURRENT_RUNS_PER_ACCOUNT = "concurrent-runs-per-account";

	/** Configuration property name for {@link #getSubagentWorkers()}. */
	String SUBAGENT_WORKERS = "subagent-workers";

	/**
	 * How many chat runs this installation carries at the same time.
	 *
	 * <p>
	 * {@code 0}, the default, means <b>no ceiling</b>: every send gets a worker of its own, which is
	 * what this installation did before this section existed. An application serving a thousand users
	 * is not served by a figure this product guessed, so the default is "as many as arrive" and the
	 * number is the administrator's to name.
	 * </p>
	 *
	 * <p>
	 * A positive number is the bound of the thread pool the chat's background runs execute on. A run
	 * thread spends nearly all of its life waiting on a provider, so the bound is not about CPU - it
	 * is about how much memory, how many open conversations and how much provider spend the
	 * installation is willing to have in flight at once. A send that finds every worker busy
	 * {@link #getChatQueue() waits for one} rather than being turned away.
	 * </p>
	 *
	 * <p>
	 * Read once, when the pool is first used. Changing it needs a restart.
	 * </p>
	 *
	 * @see #CHAT_WORKERS
	 */
	@Name(CHAT_WORKERS)
	@Label("Chat worker threads")
	@IntDefault(0)
	int getChatWorkers();

	/**
	 * How many chat runs may wait for a free worker, once {@link #getChatWorkers()} has set a ceiling.
	 *
	 * <p>
	 * Without that ceiling this says nothing: an installation that carries every run as it arrives
	 * never has anybody waiting.
	 * </p>
	 *
	 * <p>
	 * Three readings:
	 * </p>
	 * <ul>
	 * <li>{@code 0}, the default - <b>an unbounded queue</b>. A send that finds every worker busy
	 * waits for one and its answer arrives late. That is what somebody who asked a question wants;
	 * being told to ask again is not. What waits is one small task per send, so a backlog costs the
	 * server a fraction of what carrying those runs would.</li>
	 * <li>A positive number - a queue of that length. A send that arrives at a full queue is refused
	 * with a sentence it can act on.</li>
	 * <li>{@code -1} - <b>no queue at all</b>. Every send that finds the workers busy is refused at
	 * once. For an installation that would rather say "not now" than have a user wait for an answer
	 * whose moment may have passed.</li>
	 * </ul>
	 *
	 * @see #CHAT_QUEUE
	 */
	@Name(CHAT_QUEUE)
	@Label("Chat queue length")
	@IntDefault(0)
	int getChatQueue();

	/**
	 * How many runs one account may have going at the same time.
	 *
	 * <p>
	 * {@code 0}, the default, switches the cap off. A shared installation serving many users wants a
	 * small number here - 3 to 5 is a sensible range - so that one account cannot take every worker
	 * the {@link #getChatWorkers() pool} has, or run up the provider bill on its own.
	 * </p>
	 *
	 * <p>
	 * Counted over <em>every</em> entry point - the chat, the scheduler, TL-Script - because they all
	 * start their runs through the same place. A delegated sub-agent is part of its parent run and is
	 * not counted again.
	 * </p>
	 *
	 * @see #CONCURRENT_RUNS_PER_ACCOUNT
	 */
	@Name(CONCURRENT_RUNS_PER_ACCOUNT)
	@Label("Concurrent runs per account")
	@IntDefault(0)
	int getConcurrentRunsPerAccount();

	/**
	 * How many background subagents this installation runs at the same time, over all chats.
	 *
	 * <p>
	 * {@code 0}, the default, means no installation ceiling. Each chat still runs at most a few
	 * subagents at once (four) and queues the rest; this bounds them all together, for an installation
	 * where many users fan out work at the same moment.
	 * </p>
	 *
	 * <p>
	 * A subagent that finds the ceiling reached <b>waits</b>, queued, and starts when another one ends
	 * - nothing is refused, because a lead that started a subagent expects its result.
	 * </p>
	 *
	 * @see #SUBAGENT_WORKERS
	 */
	@Name(SUBAGENT_WORKERS)
	@Label("Background subagents at once")
	@IntDefault(0)
	int getSubagentWorkers();
}
