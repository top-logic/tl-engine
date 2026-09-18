/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.job;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import junit.framework.TestCase;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ErrorSink;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.job.JobBody;
import com.top_logic.layout.view.job.JobPhase;
import com.top_logic.layout.view.job.JobState;
import com.top_logic.layout.view.job.StartJobAction;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.interpreter.DefResolver;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * What a test of a long-running job needs: a view that is not displayed in a window, the channel
 * the job reports on, and the record of what it published there.
 *
 * <p>
 * A job is started by running a command chain over the channels registered here, and is awaited by
 * both the last state it published and the settling of the command it stood in, because the state
 * reaches the display before the command runs on.
 * </p>
 */
public abstract class AbstractJobTest extends TestCase {

	/** Name of the channel the job reports on. */
	protected static final String JOB = "job";

	/** Name of the channel handed to the job as an input. */
	protected static final String CONTEXT = "context";

	/** The value the command chain of a test is run with. */
	protected static final String INPUT = "in";

	/** How long a test waits for a job before it counts as hanging. */
	protected static final long TIMEOUT = 20_000;

	/** The view the job runs in. */
	protected ViewContext _context;

	/** The channel the job reports on. */
	protected ViewChannel _job;

	/** Every state the job published, in the order it published them. */
	protected final List<JobState> _states = Collections.synchronizedList(new ArrayList<>());

	/** The values the command chain settled with. */
	protected final List<Object> _completions = Collections.synchronizedList(new ArrayList<>());

	/** Counted down by the state that ends the job. */
	protected final CountDownLatch _finished = new CountDownLatch(1);

	/** Counted down once the command the job stands in has settled. */
	protected final CountDownLatch _settled = new CountDownLatch(1);

	/** What the view was asked to show as a failure. */
	protected final List<HTMLFragment> _shownErrors = Collections.synchronizedList(new ArrayList<>());

	/** Counted down by the first failure shown. */
	protected final CountDownLatch _errorShown = new CountDownLatch(1);

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(new TestReactContext(new RecordingErrorSink()));
		_job = new DefaultViewChannel(JOB);
		_context.registerChannel(JOB, _job);
		_context.registerChannel(CONTEXT, new DefaultViewChannel(CONTEXT));
		_job.addListener((sender, oldValue, newValue) -> {
			JobState state = (JobState) newValue;
			_states.add(state);
			if (state.isFinished()) {
				_finished.countDown();
			}
		});
	}

	/** The state the channel currently holds. */
	protected JobState current() {
		return (JobState) _job.get();
	}

	/**
	 * Waits until the job has published its last state and the command it stands in has settled.
	 */
	protected JobState awaitFinished() throws InterruptedException {
		assertTrue("The job must finish within " + TIMEOUT + " ms.",
			_finished.await(TIMEOUT, TimeUnit.MILLISECONDS));
		assertTrue("The command must settle within " + TIMEOUT + " ms.",
			_settled.await(TIMEOUT, TimeUnit.MILLISECONDS));
		return current();
	}

	/** Runs a command chain consisting of the given job alone. */
	protected void start(JobBody body, boolean cancelable, List<JobPhase> phases) {
		run(List.of(job(body, cancelable, phases)));
	}

	/** An action starting the given work as a job on the {@link #JOB} channel. */
	protected StartJobAction job(JobBody body, boolean cancelable, List<JobPhase> phases) {
		return new StartJobAction(new ChannelRef(JOB), List.of(), phases, cancelable, 0, body);
	}

	/** Runs the given actions as one command over the channels of this test. */
	protected void run(List<ViewAction> actions) {
		ViewActionChain.run(_context, actions, INPUT, value -> {
			_completions.add(value);
			_settled.countDown();
		});
	}

	/**
	 * The given TL-Script source, compiled for interpretation without an application model and
	 * without a knowledge base, which a script about a job touches neither of.
	 *
	 * <p>
	 * Only the variables are resolved; the types of the expressions are not, because that
	 * resolution looks the primitive types up in the model of a running application.
	 * </p>
	 *
	 * @param source
	 *        The script to compile.
	 * @return The compiled script, ready to be run as the body of a job.
	 */
	protected static QueryExecutor compile(String source) {
		SearchExpression search = SearchBuilder.toSearchExpression(null, expr(source));
		search.visit(new DefResolver(), null);
		return QueryExecutor.executor(null, null, search);
	}

	/** The given TL-Script source as the configuration reads it. */
	protected static Expr expr(String source) {
		try {
			return ExprFormat.INSTANCE.getValue("expr", source);
		} catch (ConfigurationException ex) {
			throw new AssertionError("Not a TL-Script expression: " + source, ex);
		}
	}

	/** The given work, written where the compiler sees which interface it implements. */
	protected static JobBody body(JobBody body) {
		return body;
	}

	/**
	 * Waits for the given latch, ignoring the interrupt a cancellation sends, so that a body ends
	 * by what it does rather than by being woken.
	 */
	protected static void awaitQuietly(CountDownLatch latch) {
		long end = System.currentTimeMillis() + TIMEOUT;
		while (System.currentTimeMillis() < end) {
			try {
				if (latch.await(TIMEOUT, TimeUnit.MILLISECONDS)) {
					return;
				}
			} catch (InterruptedException ex) {
				// The job was cancelled; this body reports what it produces all the same.
			}
		}
	}

	/**
	 * {@link ErrorSink} remembering what it was asked to show.
	 */
	private class RecordingErrorSink implements ErrorSink {

		@Override
		public void showError(HTMLFragment content) {
			_shownErrors.add(content);
			_errorShown.countDown();
		}

		@Override
		public void showWarning(HTMLFragment content) {
			// Not of interest here.
		}

		@Override
		public void showInfo(HTMLFragment content) {
			// Not of interest here.
		}
	}

	/**
	 * Minimal {@link ReactContext} for a view that is not displayed in a window.
	 */
	private static class TestReactContext implements ReactContext {

		private final ErrorSink _errorSink;

		private int _nextId;

		TestReactContext(ErrorSink errorSink) {
			_errorSink = errorSink;
		}

		@Override
		public ErrorSink getErrorSink() {
			return _errorSink;
		}

		@Override
		public String allocateId() {
			return "id-" + (_nextId++);
		}

		@Override
		public String getWindowName() {
			return "test-window";
		}

		@Override
		public String getContextPath() {
			return "/test";
		}

		@Override
		public SSEUpdateQueue getSSEQueue() {
			return null;
		}

		@Override
		public ReactWindowRegistry getWindowRegistry() {
			return null;
		}

		@Override
		public ModelScope getModelScope() {
			return null;
		}
	}

}
