/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodHandles;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.TreeProperty;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.CommandErrors;
import com.top_logic.layout.react.control.button.ButtonTone;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.InputRef;
import com.top_logic.layout.view.channel.NamedInputs;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.view.channel.ViewScripts;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScopeDefinition;
import com.top_logic.layout.view.command.ActionScopes;
import com.top_logic.layout.view.command.ArgumentsViewAction;
import com.top_logic.layout.view.command.Continuation;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.command.ViewActions;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.command.WithResult;
import com.top_logic.layout.view.command.WithResultActions;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;

/**
 * {@link ViewAction} that hands its work to a worker thread and lets the command wait for it.
 *
 * <p>
 * Work that takes longer than a request may take does not belong in the request: the action starts
 * it, publishes what it reports as a {@link JobState} on the configured channel, and suspends the
 * command until the work has ended. A display bound to that channel follows the job while it runs -
 * the step it is in, how far it has come, what it is doing right now - and may ask it to stop.
 * </p>
 *
 * <p>
 * The command continues where it left off once the job has completed: the actions of
 * {@link Config#getWithResult() with-result} run over the job's result, and the chain goes on with
 * the actions following this one. The work runs outside any transaction: persisting its result is
 * the business of the follow-up actions, unless the job commits the parts of its work itself.
 * </p>
 *
 * <p>
 * A job that fails aborts the command, so the compensations of the actions before it run and the
 * failure stays visible in the last state of the job. So does a cancelled job, unless the action
 * declares {@link Config#getOnCancel() on-cancel} actions: then these run over what the cancelled
 * job got done - the {@link JobState#result() result} its body returned after the cancel, or
 * nothing - instead, and the chain goes on with the actions following this one, exactly as after
 * a completed job. That is for a job whose parts stand once done, e.g. an import committing file
 * by file: the files imported before the cancel are there, and the command reports them rather
 * than pretending nothing happened.
 * </p>
 *
 * <pre>
 * &lt;start-job job="importState" cancelable="true"&gt;
 *   &lt;phases&gt;
 *     &lt;phase name="read"&gt;&lt;label&gt;&lt;en&gt;Reading&lt;/en&gt;&lt;/label&gt;&lt;/phase&gt;
 *     &lt;phase name="check"&gt;&lt;label&gt;&lt;en&gt;Checking&lt;/en&gt;&lt;/label&gt;&lt;/phase&gt;
 *   &lt;/phases&gt;
 *   &lt;body class="com.acme.ImportJob"/&gt;
 *   &lt;with-result as="report"&gt;...&lt;/with-result&gt;
 *   &lt;on-cancel as="report"&gt;...&lt;/on-cancel&gt;
 * &lt;/start-job&gt;
 * </pre>
 */
@InApp
public class StartJobAction extends ArgumentsViewAction {

	/**
	 * Configuration for {@link StartJobAction}.
	 */
	@TagName(Config.TAG_NAME)
	public interface Config extends PolymorphicConfiguration<StartJobAction>, NamedInputs, ActionScopeDefinition {

		/** Access to the default methods of this interface for the configuration proxy. */
		Lookup LOOKUP = MethodHandles.lookup();

		/** Configuration tag of a {@link StartJobAction}. */
		String TAG_NAME = "start-job";

		/** Configuration name for {@link #getJob()}. */
		String JOB = "job";

		/** Configuration name for {@link #isCancelable()}. */
		String CANCELABLE = "cancelable";

		/** Configuration name for {@link #getFunction()}. */
		String FUNCTION = "function";

		/** Configuration name for {@link #getBody()}. */
		String BODY = "body";

		/** Configuration name for {@link #getPhases()}. */
		String PHASES = "phases";

		/** Configuration name for {@link #getUpdateInterval()}. */
		String UPDATE_INTERVAL = "update-interval";

		/** Configuration name for {@link #getWithResult()}. */
		String WITH_RESULT = WithResultActions.WITH_RESULT;

		/** Configuration name for {@link #getOnCancel()}. */
		String ON_CANCEL = "on-cancel";

		@Override
		@ClassDefault(StartJobAction.class)
		Class<? extends StartJobAction> getImplementationClass();

		/**
		 * The channel the job publishes its state on.
		 *
		 * <p>
		 * It holds a {@link JobState} from the moment the job starts, and a new one on every report
		 * of the job. Whatever displays the job reads it from there.
		 * </p>
		 */
		@Name(JOB)
		@Mandatory
		@Format(ChannelRefFormat.class)
		ChannelRef getJob();

		/**
		 * Whether the reader may ask this job to stop.
		 *
		 * <p>
		 * Only for work that may be given up half-done: the job is asked to stop and ends at the
		 * next point it reports from, having done part of what it was started for.
		 * </p>
		 */
		@Name(CANCELABLE)
		boolean isCancelable();

		/**
		 * TL-Script doing the work of the job.
		 *
		 * <p>
		 * Uses the monitor of the job as variable {@value ScriptJobBody#MONITOR}, the arguments of
		 * the chain and the values of the {@link #getInputs() inputs} as variables named after them,
		 * without declaring them.
		 * </p>
		 *
		 * <p>
		 * Exactly one of this and a body of its own states what the job does.
		 * </p>
		 */
		@Name(FUNCTION)
		@Nullable
		@ScriptContextVariables(JobScriptVariables.class)
		Expr getFunction();

		/**
		 * The work of the job, written in Java.
		 *
		 * <p>
		 * The alternative to a TL-Script function for work that is not expressed as an expression.
		 * Exactly one of this and a function states what the job does. The body receives the values
		 * of the arguments of the chain followed by the values of the {@link #getInputs() inputs}.
		 * </p>
		 */
		@Name(BODY)
		@Nullable
		PolymorphicConfiguration<? extends JobBody> getBody();

		/**
		 * The steps the job goes through, in order.
		 *
		 * <p>
		 * Declared here so the display knows the whole way before the job has walked it. The job
		 * marks the step it enters by its name; a job that does not name its steps declares none.
		 * </p>
		 */
		@Name(PHASES)
		@Key(PhaseConfig.NAME)
		List<PhaseConfig> getPhases();

		/**
		 * The shortest time in milliseconds between two states published by a job.
		 *
		 * <p>
		 * A job reporting faster than this is followed at this pace, so that a job counting
		 * thousands of items does not flood the browser with updates.
		 * </p>
		 */
		@Name(UPDATE_INTERVAL)
		@LongDefault(250)
		long getUpdateInterval();

		/**
		 * The actions to run over the result of the job once it has completed.
		 *
		 * <p>
		 * They run with the arguments of the chain, extended by the result of the job under the name
		 * given as {@link WithResult#getAs() as}, {@value WithResultActions#RESULT} without one, as
		 * the follow-up actions of any other action with a result do (see
		 * {@link WithResultActions}). The chain then goes on with the actions following this one.
		 * </p>
		 */
		@Name(WITH_RESULT)
		@TreeProperty
		WithResult getWithResult();

		/**
		 * The actions to run when the job was cancelled, instead of aborting the command.
		 *
		 * <p>
		 * They run with the arguments of the chain, extended by what the cancelled job got done
		 * under the name given as {@link WithResult#getAs() as}, {@value WithResultActions#RESULT}
		 * without one: the {@link JobState#result() result} its body returned after the cancel, or
		 * nothing when it returned none. The chain then goes on with the actions following this one,
		 * as after a completed job; the compensations of the actions before this one do not run,
		 * since the part of the work the job got done stands.
		 * </p>
		 *
		 * <p>
		 * Without them, a cancelled job aborts the command. A job that fails aborts it either way.
		 * </p>
		 */
		@Name(ON_CANCEL)
		@TreeProperty
		WithResult getOnCancel();

		/**
		 * The {@link #getBody() body} works on the arguments of the chain together with the values
		 * of the {@link #getInputs() inputs}, see {@link StartJobAction#jobScope(ActionScope, List)}.
		 */
		@Override
		default ActionScope actionScope(ConfigurationItem part, ActionScope outer) {
			if (part != null && part == getBody()) {
				return jobScope(outer, getInputs());
			}
			return outer;
		}
	}

	/**
	 * {@link ScriptContextVariablesProvider} for the {@link Config#getFunction() function} of a
	 * {@link StartJobAction}: the names of its {@link StartJobAction#jobScope(ActionScope, List)
	 * arguments} followed by the monitor of the job, see {@link ScriptJobBody#variables(ActionScope)}.
	 */
	public static class JobScriptVariables implements ScriptContextVariablesProvider {

		@Override
		public List<String> getVariables(ValueModel valueModel) {
			if (!(valueModel.getModel() instanceof Config config)) {
				return List.of();
			}
			return ScriptJobBody.variables(jobScope(ActionScopes.scopeAt(config), config.getInputs()));
		}
	}

	/**
	 * Configuration of one step a job announces it goes through.
	 */
	@TagName(PhaseConfig.TAG_NAME)
	public interface PhaseConfig extends ConfigurationItem {

		/** Configuration tag of a phase. */
		String TAG_NAME = "phase";

		/** Configuration name for {@link #getName()}. */
		String NAME = "name";

		/** Configuration name for {@link #getLabel()}. */
		String LABEL = "label";

		/**
		 * The identifier the job names this step by; unique among the steps of one job.
		 */
		@Name(NAME)
		@Mandatory
		String getName();

		/**
		 * What the reader sees for this step; its name where none is given.
		 */
		@Name(LABEL)
		ResKey getLabel();
	}

	private final ChannelRef _job;

	private final ActionScope _scope;

	private final List<InputRef> _inputs;

	private final List<JobPhase> _phases;

	private final boolean _cancelable;

	private final long _updateInterval;

	private final JobBody _body;

	/** The name under which the {@link #_withResult} actions see the result of the job. */
	private final String _resultName;

	/** The actions run over the result of a completed job; empty for none. */
	private final List<ViewAction> _withResult;

	/** The name under which the {@link #_onCancel} actions see what a cancelled job got done. */
	private final String _cancelResultName;

	/**
	 * The actions run over what a cancelled job got done; {@code null} to abort the command when
	 * the job is cancelled.
	 */
	private final List<ViewAction> _onCancel;

	/**
	 * Names the operation a failure is reported for: the command taken up again after the job has
	 * ended.
	 */
	private static final String SETTLE_DESCRIPTION = "Continuing the command after the job";

	/**
	 * Creates a {@link StartJobAction} from configuration.
	 */
	@CalledByReflection
	public StartJobAction(InstantiationContext context, Config config) {
		_job = config.getJob();
		_inputs = config.getInputs();
		_scope = jobScope(ActionScope.of(context), _inputs);
		_phases = phases(config.getPhases());
		_cancelable = config.isCancelable();
		_updateInterval = config.getUpdateInterval();
		_body = body(context, config, _scope);

		ActionScope scope = ActionScope.of(context);
		WithResult withResult = config.getWithResult();
		_resultName = withResult == null ? WithResultActions.RESULT : WithResult.resultName(withResult);
		_withResult = withResult == null ? List.of()
			: ViewActions.instantiate(context, withResult.getActions(), scope.with(_resultName));
		WithResult onCancel = config.getOnCancel();
		_cancelResultName = onCancel == null ? WithResultActions.RESULT : WithResult.resultName(onCancel);
		_onCancel = onCancel == null ? null
			: ViewActions.instantiate(context, onCancel.getActions(), scope.with(_cancelResultName));
	}

	/**
	 * Creates a {@link StartJobAction}.
	 *
	 * @param job
	 *        The channel the job publishes its state on.
	 * @param scope
	 *        The scope the action runs in.
	 * @param inputs
	 *        The channels whose values are handed to the job behind the arguments of the chain.
	 * @param phases
	 *        The steps the job announces it goes through; empty for a job that does not name them.
	 * @param cancelable
	 *        Whether the reader may ask the job to stop.
	 * @param updateInterval
	 *        The shortest time in milliseconds between two published states.
	 * @param body
	 *        The work of the job.
	 */
	public StartJobAction(ChannelRef job, ActionScope scope, List<InputRef> inputs, List<JobPhase> phases,
			boolean cancelable, long updateInterval, JobBody body) {
		this(job, scope, inputs, phases, cancelable, updateInterval, body, WithResultActions.RESULT, List.of(),
			WithResultActions.RESULT, null);
	}

	/**
	 * Creates a {@link StartJobAction} with follow-up actions.
	 *
	 * @param job
	 *        The channel the job publishes its state on.
	 * @param scope
	 *        The scope the action runs in.
	 * @param inputs
	 *        The channels whose values are handed to the job behind the arguments of the chain.
	 * @param phases
	 *        The steps the job announces it goes through; empty for a job that does not name them.
	 * @param cancelable
	 *        Whether the reader may ask the job to stop.
	 * @param updateInterval
	 *        The shortest time in milliseconds between two published states.
	 * @param body
	 *        The work of the job.
	 * @param resultName
	 *        The name under which the actions run over the result of a completed job see it.
	 * @param withResult
	 *        The actions run over the result of a completed job, see {@link Config#getWithResult()}.
	 * @param cancelResultName
	 *        The name under which the actions run for a cancelled job see what it got done.
	 * @param onCancel
	 *        The actions run for a cancelled job, see {@link Config#getOnCancel()}; {@code null} to
	 *        abort the command when the job is cancelled.
	 */
	public StartJobAction(ChannelRef job, ActionScope scope, List<InputRef> inputs, List<JobPhase> phases,
			boolean cancelable, long updateInterval, JobBody body, String resultName, List<ViewAction> withResult,
			String cancelResultName, List<ViewAction> onCancel) {
		_resultName = resultName;
		_withResult = List.copyOf(withResult);
		_cancelResultName = cancelResultName;
		_onCancel = onCancel == null ? null : List.copyOf(onCancel);
		_job = job;
		_inputs = inputs;
		_scope = jobScope(scope, inputs);
		_phases = List.copyOf(phases);
		_cancelable = cancelable;
		_updateInterval = updateInterval;
		_body = body;
	}

	/**
	 * The names of the values a job is started with: the arguments of the chain, followed by the
	 * values of the inputs of the action; a later name shadows an equal earlier one.
	 *
	 * @param scope
	 *        The scope of the action.
	 * @param inputs
	 *        The inputs of the action.
	 */
	public static ActionScope jobScope(ActionScope scope, List<InputRef> inputs) {
		List<String> names = new ArrayList<>(scope.names());
		names.addAll(InputRef.names(inputs));
		String defaultName = scope.defaultName();
		if (defaultName == null && !inputs.isEmpty()) {
			defaultName = inputs.get(0).getName();
		}
		return ActionScope.of(names, defaultName);
	}

	private static JobBody body(InstantiationContext context, Config config, ActionScope scope) {
		Expr function = config.getFunction();
		PolymorphicConfiguration<? extends JobBody> body = config.getBody();
		if (function != null) {
			if (body != null) {
				context.error("A <" + Config.TAG_NAME + "> states its work either as '" + Config.FUNCTION
					+ "' or as '" + Config.BODY + "', not both.");
			}
			return new ScriptJobBody(ViewScripts.compile(function, ScriptJobBody.variables(scope)));
		}
		if (body == null) {
			context.error("A <" + Config.TAG_NAME + "> requires either '" + Config.FUNCTION + "' or '"
				+ Config.BODY + "' to state its work.");
			return null;
		}
		return scope.bind(context).getInstance(body);
	}

	private static List<JobPhase> phases(List<PhaseConfig> configs) {
		List<JobPhase> result = new ArrayList<>(configs.size());
		for (PhaseConfig phase : configs) {
			ResKey label = phase.getLabel();
			result.add(label == null ? JobPhase.named(phase.getName()) : new JobPhase(phase.getName(), label));
		}
		return result;
	}

	@Override
	public void execute(ReactContext context, ActionArguments arguments, Continuation continuation) {
		ViewContext viewContext = (ViewContext) context;
		ViewChannel target = viewContext.resolveChannel(_job);
		ActionArguments jobArguments = arguments;
		for (InputRef input : _inputs) {
			jobArguments = jobArguments.with(input.getName(), viewContext.resolveChannel(input.getChannel()).get());
		}
		List<Object> values = Collections.unmodifiableList(jobArguments.values(_scope.names()));

		JobRunner runner = new JobRunner(context, target, _phases, _cancelable, _updateInterval,
			state -> settle(context, arguments, state, continuation));
		runner.start(_body, values);
	}

	/**
	 * Takes the command up again where the job left it: a completed job hands its result to the
	 * {@link #_withResult} actions, a cancelled one what it got done to the {@link #_onCancel}
	 * actions - both resume the chain afterwards with that value - and a failed one, or a cancelled
	 * one without {@link #_onCancel} actions, ends the command.
	 *
	 * <p>
	 * What the rest of the command throws is reported the way a failed command reports it - an
	 * internal error logged as one and shown generically, a user-level failure carrying its own
	 * message into the window's snackbar - because nothing else answers for it: the job has already
	 * ended, and the thread taking the command up serves no request.
	 * </p>
	 */
	private void settle(ReactContext context, ActionArguments arguments, JobState state,
			Continuation continuation) {
		try {
			Object result = state.result();
			if (state.status() == JobStatus.COMPLETED) {
				followUp(context, _withResult, arguments.with(_resultName, result), result, continuation);
			} else if (state.status() == JobStatus.CANCELLED && _onCancel != null) {
				followUp(context, _onCancel, arguments.with(_cancelResultName, result), result, continuation);
			} else {
				continuation.abort();
			}
		} catch (Throwable ex) {
			CommandErrors.report(context.getErrorSink(), ex, SETTLE_DESCRIPTION, StartJobAction.class);
		}
	}

	/**
	 * Runs the given follow-up actions over the given arguments and resumes the chain with the given
	 * result; resumes at once without follow-up actions.
	 */
	private static void followUp(ReactContext context, List<ViewAction> followUps, ActionArguments arguments,
			Object result, Continuation continuation) {
		if (followUps.isEmpty()) {
			continuation.resume(result);
		} else {
			ViewActionChain.followUp(context, followUps, arguments, result, continuation);
		}
	}

	/**
	 * Whether one of the follow-up actions applies the values entered into the enclosing form.
	 */
	/**
	 * The rules of the follow-up actions, which run when the job ends.
	 */
	@Override
	public ViewExecutabilityRule getIntrinsicRule() {
		return ViewActions.intrinsicRule(followUps());
	}

	/**
	 * Destructive as soon as one of the follow-up actions is.
	 */
	@Override
	public ButtonTone getTone() {
		return ViewActions.tone(followUps());
	}

	/**
	 * All actions that may run after the job: the {@link #_withResult} and the {@link #_onCancel}
	 * ones.
	 */
	private List<ViewAction> followUps() {
		if (_onCancel == null || _onCancel.isEmpty()) {
			return _withResult;
		}
		List<ViewAction> result = new ArrayList<>(_withResult);
		result.addAll(_onCancel);
		return result;
	}

	/**
	 * Writes the job channel and lets the command wait for the job.
	 */
	@Override
	public boolean changesViewState() {
		return true;
	}
}
