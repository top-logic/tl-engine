/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.container.ConfigPart;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.view.command.ActionScope;
import com.top_logic.layout.view.command.ActionScopes;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.ui.ScriptContextVariables;
import com.top_logic.model.search.ui.ScriptContextVariablesProvider;

/**
 * The work of a job written as a TL-Script function.
 *
 * <p>
 * The script uses the {@link JobMonitor} of the job as variable {@value #MONITOR} and the values
 * the job was started with as variables named after them, without declaring them. The monitor is an
 * opaque object to the script: it is handed to the functions that report a phase, a progress or a
 * message, and is not otherwise looked into.
 * </p>
 *
 * <pre>
 * {
 *   $customers.forEach(c -&gt; $c.doSomething());
 *   $job.jobMessage('Done.');
 *   $customers.size();
 * }
 * </pre>
 *
 * <p>
 * What the function returns is the result of the job.
 * </p>
 */
public class ScriptJobBody implements JobBody {

	/**
	 * Name of the variable holding the {@link JobMonitor} of the job.
	 */
	public static final String MONITOR = "job";

	/**
	 * Configuration for {@link ScriptJobBody}.
	 */
	public interface Config extends PolymorphicConfiguration<ScriptJobBody>, ConfigPart {

		/** Configuration name for {@link #getFunction()}. */
		String FUNCTION = "function";

		@Override
		@ClassDefault(ScriptJobBody.class)
		Class<? extends ScriptJobBody> getImplementationClass();

		/**
		 * TL-Script doing the work of the job.
		 *
		 * <p>
		 * Uses the monitor of the job as variable {@value ScriptJobBody#MONITOR} and the values the
		 * job was started with as variables named after them, without declaring them.
		 * </p>
		 */
		@Name(FUNCTION)
		@Mandatory
		@ScriptContextVariables(BodyVariables.class)
		Expr getFunction();

	}

	private final QueryExecutor _function;

	/**
	 * Creates a {@link ScriptJobBody} from configuration.
	 */
	@CalledByReflection
	public ScriptJobBody(InstantiationContext context, Config config) {
		this(QueryExecutor.compile(config.getFunction(), variables(ActionScope.of(context))));
	}

	/**
	 * Creates a {@link ScriptJobBody} for an already compiled function.
	 *
	 * @param function
	 *        The compiled function doing the work of the job, taking the monitor followed by the
	 *        values the job is started with.
	 */
	public ScriptJobBody(QueryExecutor function) {
		_function = function;
	}

	/**
	 * The variables of a script of a job started with arguments of the given scope: the
	 * {@link #MONITOR} followed by the names of the arguments.
	 */
	public static List<String> variables(ActionScope scope) {
		List<String> result = new ArrayList<>(scope.names().size() + 1);
		result.add(MONITOR);
		result.addAll(scope.names());
		return result;
	}

	/**
	 * {@link ScriptContextVariablesProvider} for the {@link Config#getFunction() function} of a
	 * {@link ScriptJobBody}.
	 */
	public static class BodyVariables implements ScriptContextVariablesProvider {

		@Override
		public List<String> getVariables(ValueModel valueModel) {
			return variables(ActionScopes.scopeAt(valueModel.getModel()));
		}
	}

	@Override
	public Object run(JobMonitor job, List<Object> arguments) {
		Object[] args = new Object[arguments.size() + 1];
		args[0] = job;
		for (int n = 0; n < arguments.size(); n++) {
			args[n + 1] = arguments.get(n);
		}
		return _function.execute(args);
	}

}
