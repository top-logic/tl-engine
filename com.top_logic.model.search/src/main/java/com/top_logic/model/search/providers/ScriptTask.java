/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.exception.ErrorSeverity;
import com.top_logic.basic.exception.I18NRuntimeException;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.util.TLContext;
import com.top_logic.util.sched.task.impl.TaskImpl;
import com.top_logic.util.sched.task.result.TaskResult.ResultType;

/**
 * Task that allows to periodically execute custom scripts.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class ScriptTask<C extends ScriptTask.Config<?>> extends TaskImpl<C> {

	/**
	 * Configuration options for {@link ScriptTask}.
	 */
	public interface Config<I extends ScriptTask<?>> extends TaskImpl.Config<I> {

		/**
		 * Configuration name for {@link #isNodeLocal()}.
		 */
		String NODE_LOCAL = "node-local";

		/**
		 * Script to execute with the configured {@link #getSchedules()}.
		 */
		@Mandatory
		Expr getScript();

		/**
		 * Whether the script runs on every cluster node.
		 * 
		 * <p>
		 * If not set, the script runs only once in the cluster at a time: One node executes it and
		 * records the result, the other nodes skip the scheduled run. This is the right choice for a
		 * script that modifies persistent data or has external effects such as sending mails.
		 * </p>
		 * 
		 * <p>
		 * If set, every cluster node executes the script independently. This is only suitable for a
		 * script whose effects are local to the node, since a modification of persistent data would
		 * be repeated by every node.
		 * </p>
		 */
		@Name(NODE_LOCAL)
		boolean isNodeLocal();

	}

	private QueryExecutor _script;

	private final boolean _nodeLocal;

	/**
	 * Creates a {@link ScriptTask}.
	 */
	public ScriptTask(InstantiationContext context, C config) {
		super(context, config);

		_script = QueryExecutor.compile(config.getScript());
		_nodeLocal = config.isNodeLocal();

		// The task runs periodically in a system context (see run()) without a logged-in user. It
		// is backend logic that must operate on all data and must not be subject to a user's access
		// rights; with security enabled it would even be denied, as there is no current user.
		_script.disableSecurity();
	}

	@Override
	public void run() {
		ThreadContext.inSystemContext(ScriptTask.class, () -> {
			TLContext context = TLContext.getContext();

			// Add the task name to the context ID to better identify this task in potential log
			// messages it produces.
			context.setContextId(context.getContextId() + "-" + getName());

			getLog().taskStarted();

			try (Transaction tx =
				PersistencyLayer.getKnowledgeBase().beginTransaction(I18NConstants.SCRIPT_TASK__TASK.fill(getName()))) {
				Object result = _script.execute();
				tx.commit();

				getLog().taskEnded(ResultType.SUCCESS,
					result == null ? ResultType.SUCCESS.getMessageI18N()
						: I18NConstants.TASK_MESSAGE__VALUE.fill(result));
			} catch (I18NRuntimeException ex) {
				getLog().taskEnded(resultType(ex.getSeverity()), ex.getErrorKey(), ex);
			} catch (Throwable ex) {
				getLog().taskEnded(ResultType.ERROR, ResultType.ERROR.getMessageI18N(), ex);
			}
		});

		super.run();
	}

	private ResultType resultType(ErrorSeverity severity) {
		switch (severity) {
			case INFO:
				return ResultType.SUCCESS;
			case WARNING:
				return ResultType.WARNING;
			case ERROR:
				return ResultType.FAILURE;
			case SYSTEM_FAILURE:
				return ResultType.ERROR;
		}
		return ResultType.UNKNOWN;
	}

	@Override
	public boolean isNodeLocal() {
		return _nodeLocal;
	}

}
