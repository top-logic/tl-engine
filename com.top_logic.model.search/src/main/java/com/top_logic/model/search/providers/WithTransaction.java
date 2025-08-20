/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.providers;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.NoTransaction;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.component.WithCommitMessage;

/**
 * Mix-in interface for commands that can be configured to operate in transaction context.
 */
public interface WithTransaction<I extends PolymorphicConfiguration<?>> extends ConfiguredInstance<I> {

	/**
	 * Configuration plugin for {@link WithTransaction} implementations.
	 */
	@Abstract
	public interface Config extends WithCommitMessage {

		/**
		 * @see #isInTransaction()
		 */
		String TRANSACTION = "transaction";

		/**
		 * Whether to perform the operation in a transaction.
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

	/**
	 * Starts a (real) transaction, if the argument given is <code>true</code>.
	 */
	default Transaction beginTransaction(ResKey commandLabel, Object model) {
		KnowledgeBase kb = PersistencyLayer.getKnowledgeBase();
		Config config = (Config) getConfig();
		if (config.isInTransaction()) {
			ResKey message = config.buildCommandMessage(commandLabel, model);
			return kb.beginTransaction(message);
		} else {
			return new NoTransaction(kb);
		}
	}

}
