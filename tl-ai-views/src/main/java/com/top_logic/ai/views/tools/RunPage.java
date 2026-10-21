/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.views.tools;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.top_logic.ai.views.WindowPage;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.Logger;
import com.top_logic.basic.sched.SchedulerService;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.tl.ai.core.agent.RunWindow;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentExecution;
import com.top_logic.tl.ai.tool.api.RunInfo;
import com.top_logic.util.TLContextManager;

/**
 * The page of the window an agent run belongs to, for the tools of this package.
 */
final class RunPage {

	/** How long a rebuild of the start page waits for the run that wrote it, in seconds. */
	private static final long WAIT_LIMIT = 30 * 60;

	/** What the model is told when the run's window shows no built page. */
	static final String NO_PAGE = "The user's window shows no built page for this run.";

	/** The views changed in a window whose rebuild waits for the end of the run. */
	private static final Map<TLSubSessionContext, Set<String>> PENDING = new ConcurrentHashMap<>();

	private RunPage() {
		// Static utility.
	}

	/**
	 * Computes something in the run's window, see {@link RunWindow#inWindow}.
	 *
	 * @return The result, empty when the run has no window or its page is not built.
	 */
	static <T> Optional<T> inWindow(Function<ReactControl, T> action) {
		return inWindow(TLContextManager.getSubSession(), action);
	}

	private static <T> Optional<T> inWindow(TLSubSessionContext subSession, Function<ReactControl, T> action) {
		AtomicReference<T> result = new AtomicReference<>();
		RunWindow.get().inWindow(subSession, root -> {
			// A window whose page is not built (yet, or any more) shows nothing to read or rebuild.
			if (root != null) {
				result.set(action.apply((ReactControl) root));
			}
		});
		return Optional.ofNullable(result.get());
	}

	/**
	 * Brings changed views onto the run's window.
	 *
	 * <p>
	 * The parts of the page that show a changed view are rebuilt at once, which leaves the rest of
	 * the page - the chat the run answers in - as it is. A change of the start page itself rebuilds
	 * the whole page, chat included, so that waits until the run has ended.
	 *
	 * @param run
	 *        The run that wrote.
	 * </p>
	 *
	 * @return A sentence for the model about what the user sees.
	 */
	static String show(Collection<String> resources, RunInfo run) {
		Set<String> changed = new LinkedHashSet<>(resources);
		TLSubSessionContext subSession = TLContextManager.getSubSession();
		try {
			Optional<Boolean> deferred = inWindow(subSession, page -> {
				if (changed.contains(WindowPage.startView(page))) {
					return Boolean.TRUE;
				}
				WindowPage.rebuild(page, changed);
				return Boolean.FALSE;
			});
			if (deferred.isEmpty()) {
				return NO_PAGE + " The change shows when the view is opened next.";
			}
			if (!deferred.get()) {
				return "The user's page shows the change already.";
			}
			rebuildAfterRun(subSession, changed, topRunId(run));
			return "The change touches the start page, so the user's page is rebuilt when this answer is finished.";
		} catch (RuntimeException ex) {
			// The change is written; only bringing it onto the open page failed.
			Logger.warn("Could not show the change of " + changed + " on the open page.", ex, RunPage.class);
			return "The user's page shows the change after a reload.";
		}
	}

	/**
	 * Rebuilds the window once the run that wrote has ended. Changes made meanwhile join the
	 * rebuild already waiting for the window.
	 */
	private static void rebuildAfterRun(TLSubSessionContext subSession, Set<String> changed, String runId) {
		Set<String> waiting = PENDING.computeIfAbsent(subSession, x -> ConcurrentHashMap.newKeySet());
		boolean first = waiting.isEmpty();
		waiting.addAll(changed);
		if (!first) {
			return;
		}
		long until = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(WAIT_LIMIT);
		AtomicReference<ScheduledFuture<?>> task = new AtomicReference<>();
		task.set(SchedulerService.getInstance().scheduleWithFixedDelay(() -> {
			if (running(runId) && System.currentTimeMillis() < until) {
				return;
			}
			task.get().cancel(false);
			Set<String> resources = PENDING.remove(subSession);
			try {
				inWindow(subSession, root -> {
					WindowPage.rebuild(root, resources);
					return null;
				});
			} catch (RuntimeException ex) {
				Logger.warn("Could not rebuild the page after a change of " + resources + ".", ex, RunPage.class);
			}
		}, 1, 1, TimeUnit.SECONDS));
	}

	/** The run a delegated run belongs to: the one whose answer the user is reading. */
	private static String topRunId(RunInfo run) {
		RunInfo top = run;
		while (top.parent() != null) {
			top = top.parent();
		}
		return top.runId();
	}

	/** Whether the run is recorded and has not ended. */
	private static boolean running(String runId) {
		if (runId == null) {
			return false;
		}
		return ThreadContext.<Boolean, RuntimeException, RuntimeException> inSystemContext(RunPage.class, () -> {
			AgentExecution execution = AgentExecutionStore.getInstance().findExecutionByRunId(runId);
			return execution != null && execution.getEndedAt() == null;
		});
	}
}
