/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers.toggle;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;

/**
 * {@link StateHandler} taking the button state from a persistent state.
 */
public class ModelStateHandler extends AbstractConfiguredInstance<ModelStateHandler.Config<?>> implements StateHandler {

	/**
	 * Configuration options for {@link ModelStateHandler}.
	 */
	public interface Config<I extends ModelStateHandler> extends PolymorphicConfiguration<I> {
		/**
		 * @see #getStateLookup()
		 */
		String STATE_LOOKUP = "state-lookup";

		/**
		 * @see #getStateUpdate()
		 */
		String STATE_UPDATE = "state-update";

		/**
		 * @see #isInTransaction()
		 */
		String TRANSACTION = "transaction";

		/**
		 * Function looking up the current state of the button.
		 * 
		 * <p>
		 * A function taking the command's model as single argument and returning the current state
		 * of the button (<code>true</code> means active and <code>false</code> means inactive).
		 * </p>
		 */
		@Mandatory
		@Name(STATE_LOOKUP)
		Expr getStateLookup();

		/**
		 * The operation to perform.
		 * 
		 * <p>
		 * A operation taking the the command's model and the new state (true or false) as arguments
		 * and stores the new state.
		 * </p>
		 */
		@Name(STATE_UPDATE)
		@Mandatory
		Expr getStateUpdate();

		/**
		 * Whether to perform the state update operation in a transaction.
		 * 
		 * <p>
		 * Note: Creating, modifying, or deleting persistent objects require a transaction.
		 * Modification of transient objects or pure service operations do not require a transaction
		 * context.
		 * </p>
		 */
		@Name(TRANSACTION)
		@BooleanDefault(true)
		boolean isInTransaction();
	}

	private final QueryExecutor _stateLookup;

	private final QueryExecutor _stateUpdate;

	private final boolean _transaction;

	/**
	 * Creates a {@link ModelStateHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ModelStateHandler(InstantiationContext context, Config<?> config) {
		super(context, config);

		_transaction = config.isInTransaction();
		_stateUpdate = QueryExecutor.compile(config.getStateUpdate());
		_stateLookup = QueryExecutor.compile(config.getStateLookup());
	}

	@Override
	public boolean getState(LayoutComponent component, Object model) {
		return SearchExpression.isTrue(_stateLookup.execute(model));
	}

	@Override
	public void setState(LayoutComponent component, Object model, boolean state) {
		if (_transaction) {
			try (Transaction tx = PersistencyLayer.getKnowledgeBase().beginTransaction()) {
				_stateUpdate.execute(model, Boolean.valueOf(state));

				tx.commit();
			}
		} else {
			_stateUpdate.execute(model, Boolean.valueOf(state));
		}
	}

}
