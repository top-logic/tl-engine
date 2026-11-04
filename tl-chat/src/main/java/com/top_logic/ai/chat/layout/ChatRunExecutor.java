/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.layout;

import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import com.top_logic.tl.ai.core.limits.BoundedWorkerPool;
import com.top_logic.tl.ai.core.limits.CapacityConfig;
import com.top_logic.tl.ai.core.limits.RunLimitSettings;

/**
 * The threads the chat's agent runs execute on, and how many of them there are.
 *
 * <p>
 * As many as arrive, unless an administrator says otherwise. That is what
 * {@code Executors.newCachedThreadPool()} in {@code ChatProcessor} did, and what an installation
 * upgrading past Gitea #343 must still find: this product is deployed into applications with a
 * thousand users, and any ceiling it picked for them would be wrong for most. So
 * {@link RunLimitSettings#chatWorkers()} is {@code 0} until somebody names a number.
 * </p>
 *
 * <p>
 * When a number is named, a send that finds every worker busy <b>waits</b>: the queue is of no
 * fixed length unless {@link RunLimitSettings#chatQueue()} gives it one. Somebody who asked a
 * question wants the answer, late rather than not at all, and what waits is one task object rather
 * than a run. An installation that would rather say "not now" gives the queue a length it accepts,
 * or sets it to {@link RunLimitSettings#NO_QUEUE} for no waiting room at all; a send that arrives
 * at a full queue is then refused with {@link NoRoom}, which the caller turns into a sentence the
 * user can act on.
 * </p>
 *
 * <p>
 * <b>Why this is a class and not a TopLogic service.</b> A pool has no lifecycle of its own beyond
 * the application's, nothing to inject and nothing for another component to look up; making it a
 * {@code ConfiguredManagedClass} would add a service every application has to register in order to
 * get a thread pool it cannot do without. What it does need is a <em>configured</em> size, and that
 * lives where the installation's other ceilings live - the {@code <capacity>} section of
 * {@code AgentRuntimeService} - so an administrator sets all of them in one place. What this replaces is the
 * {@code static} field in {@code ChatProcessor}, a layout class, which is where the pool did not
 * belong.
 * </p>
 *
 * <p>
 * The pool itself is a {@link BoundedWorkerPool}, the same kind the parallel tool calls of an agent
 * run execute on.
 * </p>
 */
final class ChatRunExecutor {

	/** Prefix of the names of the pool's threads, followed by a running number. */
	private static final String THREAD_NAME_PREFIX = "ChatProcessor-AI-Worker-";

	/** The pool, built on first use so that the configuration is read after the services are up. */
	private static final BoundedWorkerPool POOL = new BoundedWorkerPool(ChatRunExecutor.class,
		"Chat agent runs", "a send", CapacityConfig.CHAT_WORKERS, n -> THREAD_NAME_PREFIX + n,
		RunLimitSettings::chatWorkers, RunLimitSettings::chatQueue);

	private ChatRunExecutor() {
		// Static utility.
	}

	/** What a run that found no room answers with. */
	static final class NoRoom extends RuntimeException {

		NoRoom(String message) {
			super(message);
		}
	}

	/**
	 * Runs the given task on a chat worker, or queues it for the next free one.
	 *
	 * @return The task's future, for cancellation.
	 * @throws NoRoom
	 *         If every worker is busy and the queue has no room either - which only an installation
	 *         that configured both a ceiling and a bounded queue can reach. The caller answers the
	 *         user; nothing is started.
	 */
	static Future<?> submit(Runnable task) {
		try {
			return POOL.submit(task);
		} catch (RejectedExecutionException ex) {
			throw new NoRoom(noRoomDescription());
		}
	}

	/**
	 * Why a send found no room, for the log.
	 *
	 * <p>
	 * Names the ceilings as they are configured, because that is what an administrator reading the
	 * line has to decide about.
	 * </p>
	 */
	static String noRoomDescription() {
		int queue = RunLimitSettings.chatQueue();
		return "all " + workers() + " chat workers are busy and "
			+ (queue > 0 ? "the queue of " + queue + " is full" : "this installation keeps no queue")
			+ ".";
	}

	/**
	 * Whether a send would find room right now - a free worker, or a place to wait.
	 *
	 * <p>
	 * Always {@code true} unless this installation configured a ceiling <em>and</em> bounded the
	 * queue behind it. Where it can say no, it is advisory and deliberately so: it is asked before
	 * the request has changed anything, so that the ordinary "we are full" answer costs the user
	 * nothing more than a sentence. Between this answer and the submit another request can take the
	 * last place, which {@link #submit(Runnable)} then reports - the rare path, where the user's
	 * message is already in the conversation.
	 * </p>
	 */
	static boolean hasRoom() {
		return POOL.hasRoom();
	}

	/**
	 * How many runs this installation carries at the same time; {@code 0} when it carries as many as
	 * arrive.
	 */
	static int workers() {
		return POOL.workers();
	}

	/**
	 * Throws the pool away, interrupting the runs on it, so that the next submit starts from an empty
	 * pool built from the configuration as it is now.
	 *
	 * <p>
	 * <b>A test seam.</b> A changed configuration alone builds a new pool on the next use (see
	 * {@link BoundedWorkerPool}), but lets the runs of the previous one end; a test wants the
	 * blocked tasks of the test before gone. Nothing in production calls it.
	 * </p>
	 */
	static void resetForTest() {
		POOL.reset();
	}
}
