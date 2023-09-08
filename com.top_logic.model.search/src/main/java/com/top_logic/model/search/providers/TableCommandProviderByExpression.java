/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import java.util.List;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.table.TableData;
import com.top_logic.layout.table.command.AbstractTableCommandProvider;
import com.top_logic.layout.table.command.TableCommand;
import com.top_logic.layout.table.command.TableCommandProvider;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.search.providers.TableCommandProviderByExpression.Config.ExecutabilityExpression;
import com.top_logic.tool.boundsec.conditional.CommandStep;
import com.top_logic.tool.boundsec.conditional.Failure;
import com.top_logic.tool.boundsec.conditional.Hide;
import com.top_logic.tool.boundsec.conditional.Success;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link TableCommandProvider} that can be parameterized with TL-Script expressions.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
public class TableCommandProviderByExpression<C extends TableCommandProviderByExpression.Config<?>>
		extends AbstractTableCommandProvider<C> implements TableCommand {

	private List<ExecutabilityQuery> _executability;

	private QueryExecutor _implementation;

	/**
	 * Configuration options for {@link TableCommandProviderByExpression}.
	 */
	public interface Config<I extends TableCommandProviderByExpression<?>>
			extends AbstractTableCommandProvider.Config<I> {

		/**
		 * Whether the command can be executed.
		 * 
		 * <p>
		 * All {@link ExecutabilityExpression}s are executed before the
		 * {@link #getImplementation()}. If any of them return a non-<code>true</code> result, the
		 * command is not allowed to execute.
		 * </p>
		 */
		List<ExecutabilityExpression> getExecutability();

		/**
		 * The command script.
		 * 
		 * <p>
		 * The script is evaluated in a transaction context. The script expects two arguments. The
		 * first argument is the list of all rows displayed in the table. The second argument is the
		 * set of selected rows.
		 * </p>
		 */
		Expr getImplementation();

		/**
		 * Configuration options for a TL-Script executability function.
		 */
		public interface ExecutabilityExpression extends ConfigurationItem {

			/**
			 * Whether a non-executable command is displayed in disabled state.
			 * 
			 * <p>
			 * A value of <code>false</code> means to hide a non-executable command. If a disabled
			 * command is shown, {@link #getDecision()} should provide a reason for the
			 * non-executability.
			 * </p>
			 */
			boolean getShowDisabled();

			/**
			 * Function deciding whether a command can currently be executed.
			 * 
			 * <p>
			 * The function takes two arguments: A list of all table rows as first argument and the
			 * current selection set as second argument.
			 * </p>
			 * 
			 * <p>
			 * To allow execution, <code>true</code> must be returned. Any other value denies
			 * execution. If {@link #getShowDisabled()} is configured to be <code>true</code>, a
			 * non-<code>null</code> function result is used as reason for the disabled state show
			 * to the user. It's best to return a {@link ResKey} with an internationalized reason. A
			 * string result is used as not internationalized text. Any other value produces no
			 * reason.
			 * </p>
			 */
			Expr getDecision();
		}
	}

	/**
	 * Creates a {@link TableCommandProviderByExpression} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TableCommandProviderByExpression(InstantiationContext context, C config) {
		super(context, config);

		_executability = config.getExecutability().stream().map(ExecutabilityQuery::new).collect(Collectors.toList());
		_implementation = QueryExecutor.compile(config.getImplementation());
	}

	@Override
	protected TableCommand getTableCommand() {
		return this;
	}

	@Override
	public CommandStep prepareCommand(DisplayContext context, TableData table, CommandModel button) {
		for (ExecutabilityQuery query : _executability) {
			ExecutableState result =
				query.eval(table.getTableModel().getAllRows(), table.getSelectionModel().getSelection());
			if (!result.isExecutable()) {
				if (result.isHidden()) {
					return new Hide();
				} else {
					return new Failure(result.getI18NReasonKey());
				}
			}
		}

		QueryExecutor implementation = _implementation;
		return new Success() {
			@Override
			protected void doExecute(DisplayContext executionContext) {
				try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
					implementation.execute(table.getTableModel().getAllRows(),
						table.getSelectionModel().getSelection());
					tx.commit();
				}
			}
		};
	}

	private static class ExecutabilityQuery {

		private QueryExecutor _query;

		private boolean _showDisabled;

		/**
		 * Creates a {@link ExecutabilityQuery}.
		 */
		public ExecutabilityQuery(ExecutabilityExpression executability) {
			_query = QueryExecutor.compile(executability.getDecision());
			_showDisabled = executability.getShowDisabled();
		}

		public ExecutableState eval(Object... args) {
			Object result = _query.execute(args);
			return ExecutabilityByExpression.toExecutabilityState(_showDisabled, result);
		}

	}
}
