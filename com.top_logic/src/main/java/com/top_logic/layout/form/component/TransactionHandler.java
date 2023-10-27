/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * Mix-in interface for {@link CommandHandler}s performing transaction handling.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface TransactionHandler {

	/**
	 * Starts a {@link Transaction} for the given model object.
	 * 
	 * @see KnowledgeBase#beginTransaction()
	 */
	default Transaction beginTransaction(Object model) {
		KnowledgeBase kb;
		if (model instanceof TLObject) {
			kb = ((TLObject) model).tKnowledgeBase();
		} else {
			kb = PersistencyLayer.getKnowledgeBase();
		}
		if (kb == null) {
			return null;
		} else {
			return kb.beginTransaction();
		}
	}

	/**
	 * Commits the given {@link Transaction}.
	 * 
	 * <p>
	 * If the commit fails, an exception is thrown that reports the problem.
	 * </p>
	 * 
	 * @param model
	 *        The model object also passed to {@link #beginTransaction(Object)}.
	 * 
	 * @see Transaction#commit()
	 * 
	 * @throws KnowledgeBaseException
	 *         If the commit fails.
	 */
	default void commit(Transaction tx, Object model) throws KnowledgeBaseException {
		if (tx != null) {
			tx.commit();
		}
	}

}
