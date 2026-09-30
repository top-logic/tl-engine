/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * The work of a long-running job, written as TL-Script and committed in chunks.
 *
 * <p>
 * A job body runs outside any transaction, and TL-Script opens none. This is the transactional
 * frame around scripted work that produces persistent objects: the work items are walked in chunks
 * of {@link Config#getChunkSize()} items, each chunk committing on its own, so memory stays
 * bounded, a failure late in the run keeps what the earlier chunks produced, and a single item that
 * cannot be processed costs only itself.
 * </p>
 *
 * <p>
 * Every script is called with the monitor of the job as its first argument - the opaque value the
 * reporting functions are called on, so a script says what it is doing with
 * <code>$job.jobMessage('…')</code>:
 * </p>
 *
 * <ul>
 * <li>{@link Config#getInit()} as <code>job -&gt; a -&gt; b -&gt; …</code>, the monitor followed by
 * the values the job was started with,</li>
 * <li>{@link Config#getElements()} as <code>job -&gt; state -&gt; …</code>,</li>
 * <li>{@link Config.Step#getExpr()} as <code>job -&gt; chunk -&gt; state -&gt; …</code>,</li>
 * <li>{@link Config#getFinish()} as <code>job -&gt; state -&gt; …</code>.</li>
 * </ul>
 *
 * <p>
 * The <em>state</em> is what ties the scripts together: it is the value {@link Config#getInit()}
 * produced, and every later script receives it. Where there is no preparation, the state is the
 * first value the job was started with. A state that has to change while the job runs is a
 * transient object, <code>new(`my:State`, transient: true)</code>, whose attributes the passes set;
 * several objects the passes need are a map literal.
 * </p>
 *
 * <pre>
 * &lt;body class="com.top_logic.layout.view.job.ChunkedScriptJobBody" chunk-size="100"&gt;
 *   &lt;init-label&gt;&lt;en&gt;Reading the file&lt;/en&gt;&lt;/init-label&gt;
 *   &lt;init&gt;job -&gt; file -&gt; $file.parse()&lt;/init&gt;
 *   &lt;elements&gt;job -&gt; state -&gt; $state.get(`my:Import#rows`)&lt;/elements&gt;
 *   &lt;steps&gt;
 *     &lt;step&gt;
 *       &lt;label&gt;&lt;en&gt;Creating the records&lt;/en&gt;&lt;/label&gt;
 *       &lt;expr&gt;job -&gt; chunk -&gt; state -&gt; $chunk.foreach(r -&gt; $state.create($r))&lt;/expr&gt;
 *     &lt;/step&gt;
 *   &lt;/steps&gt;
 *   &lt;finish&gt;job -&gt; state -&gt; $state.get(`my:Import#created`)&lt;/finish&gt;
 * &lt;/body&gt;
 * </pre>
 *
 * <p>
 * The result of the job is what {@link Config#getFinish()} produced; a body without a completion
 * ends with the text saying how many items it processed and how many it skipped.
 * </p>
 *
 * @implNote The scripts are compiled with {@link QueryExecutor#compile(Expr)}, and the frame
 *           {@link ChunkedJobBody} is what runs them: the hooks of the frame are bound to the
 *           compiled scripts, and the texts the reader sees fall back to what the frame names its
 *           steps where the configuration leaves them unset. The value of
 *           {@link Config#getElements()} is read as a list: a collection is taken over, nothing at
 *           all is no items, and any other value is the single item it stands for.
 */
@InApp
public class ChunkedScriptJobBody extends ChunkedJobBody {

	/**
	 * Configuration of a {@link ChunkedScriptJobBody}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<ChunkedScriptJobBody> {

		/** Configuration tag of a {@link ChunkedScriptJobBody}. */
		String TAG_NAME = "chunked-script";

		/** Configuration name for {@link #getInit()}. */
		String INIT = "init";

		/** Configuration name for {@link #getInitLabel()}. */
		String INIT_LABEL = "init-label";

		/** Configuration name for {@link #getElements()}. */
		String ELEMENTS = "elements";

		/** Configuration name for {@link #getChunkSize()}. */
		String CHUNK_SIZE = "chunk-size";

		/** Configuration name for {@link #getSteps()}. */
		String STEPS = "steps";

		/** Configuration name for {@link #getFinish()}. */
		String FINISH = "finish";

		/** Configuration name for {@link #getFinishLabel()}. */
		String FINISH_LABEL = "finish-label";

		@Override
		@ClassDefault(ChunkedScriptJobBody.class)
		Class<? extends ChunkedScriptJobBody> getImplementationClass();

		/**
		 * Script producing the state the later scripts work against, run once in a transaction of
		 * its own.
		 *
		 * <p>
		 * Called with the monitor of the job followed by the values the job was started with. It
		 * yields whatever the passes have to reach - typically the container the work items are
		 * created in, which has to be persistent before them. Where it is unset, the first value
		 * the job was started with is the state.
		 * </p>
		 */
		@Name(INIT)
		Expr getInit();

		/**
		 * What the reader sees for the preparation, the step the job begins with.
		 */
		@Name(INIT_LABEL)
		ResKey getInitLabel();

		/**
		 * Script producing the work items, evaluated once against the state, read-only and outside
		 * any transaction.
		 *
		 * <p>
		 * Called with the monitor of the job and the state. A collection is the list of items, any
		 * other value is the single item it stands for.
		 * </p>
		 */
		@Name(ELEMENTS)
		Expr getElements();

		/**
		 * How many work items one transaction takes.
		 */
		@Name(CHUNK_SIZE)
		@IntDefault(200)
		int getChunkSize();

		/**
		 * The passes over the work items, worked through in order.
		 *
		 * <p>
		 * Each pass is applied to successive chunks of {@link #getChunkSize()} items, every chunk
		 * committing on its own. A chunk that fails is retried item by item, so that only the items
		 * that genuinely cannot be processed are skipped, each of them reported and counted,
		 * instead of the whole pass being given up.
		 * </p>
		 *
		 * <p>
		 * A pass begins once the pass before it has committed every chunk. Work that has to see all
		 * the items an earlier pass produced - resolving cross references between them, for
		 * instance - therefore belongs in a later pass, where it is chunked and resilient as well.
		 * </p>
		 */
		@Name(STEPS)
		List<Step> getSteps();

		/**
		 * One pass over the work items.
		 */
		@TagName(Step.TAG_NAME)
		interface Step extends ConfigurationItem {

			/** Configuration tag of a pass. */
			String TAG_NAME = "step";

			/** Configuration name for {@link #getExpr()}. */
			String EXPR = "expr";

			/** Configuration name for {@link #getLabel()}. */
			String LABEL = "label";

			/**
			 * The script of the pass, called with the monitor of the job, the chunk of work items
			 * it is to process and the state.
			 *
			 * <p>
			 * It runs in a transaction that is committed once it returns, and it runs again on a
			 * single item while a failed chunk is retried, so what it does must be repeatable for
			 * an item it already saw.
			 * </p>
			 */
			@Name(EXPR)
			@Mandatory
			Expr getExpr();

			/**
			 * What the reader sees for this pass; its number where none is given.
			 */
			@Name(LABEL)
			ResKey getLabel();
		}

		/**
		 * Script run once after every pass has committed, in a transaction of its own, against the
		 * state.
		 *
		 * <p>
		 * Called with the monitor of the job and the state. The one-shot finalizer that has to see
		 * everything committed - resetting a sequence past what was imported, computing an
		 * aggregate - and what it produces is the result of the job. Work spanning the items
		 * belongs in a later pass, not here.
		 * </p>
		 */
		@Name(FINISH)
		Expr getFinish();

		/**
		 * What the reader sees for the completion, the step the job ends with.
		 */
		@Name(FINISH_LABEL)
		ResKey getFinishLabel();
	}

	private final QueryExecutor _init;

	private final ResKey _initLabel;

	private final QueryExecutor _elements;

	private final List<QueryExecutor> _steps;

	private final List<ResKey> _stepLabels;

	private final QueryExecutor _finish;

	private final ResKey _finishLabel;

	/**
	 * Creates a {@link ChunkedScriptJobBody} from configuration.
	 */
	@CalledByReflection
	public ChunkedScriptJobBody(InstantiationContext context, Config config) {
		super(config.getChunkSize());

		_init = QueryExecutor.compileOptional(config.getInit());
		_initLabel = config.getInitLabel();
		_elements = QueryExecutor.compileOptional(config.getElements());
		_finish = QueryExecutor.compileOptional(config.getFinish());
		_finishLabel = config.getFinishLabel();

		List<QueryExecutor> steps = new ArrayList<>();
		List<ResKey> labels = new ArrayList<>();
		for (Config.Step step : config.getSteps()) {
			steps.add(QueryExecutor.compile(step.getExpr()));
			labels.add(step.getLabel());
		}
		_steps = Collections.unmodifiableList(steps);
		_stepLabels = Collections.unmodifiableList(labels);
	}

	/**
	 * Creates a {@link ChunkedScriptJobBody} for already compiled scripts, with the steps of the job
	 * named as the frame names them.
	 *
	 * @param chunkSize
	 *        How many work items one transaction takes.
	 * @param init
	 *        The preparation producing the state, {@code null} for a body working against the first
	 *        value the job was started with.
	 * @param elements
	 *        The script producing the work items, {@code null} for no items at all.
	 * @param steps
	 *        The passes over the work items, in the order they are worked through.
	 * @param finish
	 *        The completion producing the result of the job, {@code null} for a body ending with
	 *        what it says about its items.
	 */
	public ChunkedScriptJobBody(int chunkSize, QueryExecutor init, QueryExecutor elements,
			List<QueryExecutor> steps, QueryExecutor finish) {
		super(chunkSize);

		_init = init;
		_initLabel = null;
		_elements = elements;
		_finish = finish;
		_finishLabel = null;
		_steps = List.copyOf(steps);
		_stepLabels = List.of();
	}

	@Override
	protected boolean hasInit() {
		return _init != null;
	}

	@Override
	protected Object init(JobMonitor job, List<Object> arguments) {
		Object[] args = new Object[arguments.size() + 1];
		args[0] = job;
		for (int n = 0; n < arguments.size(); n++) {
			args[n + 1] = arguments.get(n);
		}
		return _init.execute(args);
	}

	@Override
	protected List<?> elements(JobMonitor job, Object state) {
		return _elements == null ? null : toList(_elements.execute(job, state));
	}

	@Override
	protected int stepCount() {
		return _steps.size();
	}

	@Override
	protected void step(JobMonitor job, int index, List<?> chunk, Object state) {
		_steps.get(index).execute(job, chunk, state);
	}

	@Override
	protected boolean hasFinish() {
		return _finish != null;
	}

	@Override
	protected Object finish(JobMonitor job, Object state) {
		return _finish.execute(job, state);
	}

	@Override
	protected ResKey initLabel() {
		return _initLabel == null ? super.initLabel() : _initLabel;
	}

	@Override
	protected ResKey stepLabel(int index) {
		ResKey configured = index < _stepLabels.size() ? _stepLabels.get(index) : null;
		return configured == null ? super.stepLabel(index) : configured;
	}

	@Override
	protected ResKey finishLabel() {
		return _finishLabel == null ? super.finishLabel() : _finishLabel;
	}

	/**
	 * The given script value as the list of work items it stands for.
	 */
	private static List<?> toList(Object value) {
		if (value == null) {
			return Collections.emptyList();
		}
		if (value instanceof Collection<?> collection) {
			return new ArrayList<>(collection);
		}
		return Collections.singletonList(value);
	}

}
