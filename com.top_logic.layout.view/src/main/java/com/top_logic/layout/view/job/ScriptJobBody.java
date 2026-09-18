/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.job;

import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * The work of a job written as a TL-Script function.
 *
 * <p>
 * The function is called with the {@link JobMonitor} of the job as its first argument, followed by
 * the values the job was started with, in the order the caller declared them. The monitor is an
 * opaque object to the script: it is handed to the functions that report a phase, a progress or a
 * message, and is not otherwise looked into.
 * </p>
 *
 * <pre>
 * job -&gt; customers -&gt; x -&gt; {
 *   $customers.forEach(c -&gt; $c.doSomething());
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
	 * Configuration for {@link ScriptJobBody}.
	 */
	public interface Config extends PolymorphicConfiguration<ScriptJobBody> {

		/** Configuration name for {@link #getFunction()}. */
		String FUNCTION = "function";

		@Override
		@ClassDefault(ScriptJobBody.class)
		Class<? extends ScriptJobBody> getImplementationClass();

		/**
		 * TL-Script function doing the work of the job.
		 *
		 * <p>
		 * Called with the monitor of the job as its first argument, followed by the values the job
		 * was started with.
		 * </p>
		 */
		@Name(FUNCTION)
		@Mandatory
		Expr getFunction();

	}

	private final QueryExecutor _function;

	/**
	 * Creates a {@link ScriptJobBody} from configuration.
	 */
	@CalledByReflection
	public ScriptJobBody(InstantiationContext context, Config config) {
		this(QueryExecutor.compile(config.getFunction()));
	}

	/**
	 * Creates a {@link ScriptJobBody} for the given TL-Script function.
	 *
	 * @param function
	 *        The function doing the work of the job.
	 */
	public ScriptJobBody(Expr function) {
		this(QueryExecutor.compile(function));
	}

	/**
	 * Creates a {@link ScriptJobBody} for an already compiled function.
	 *
	 * @param function
	 *        The compiled function doing the work of the job.
	 */
	public ScriptJobBody(QueryExecutor function) {
		_function = function;
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
