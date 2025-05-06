/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.component;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
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
	 * Command argument to pass a custom commit message of type {@link ResKey}.
	 */
	String CUSTOM_COMMIT_MESSAGE = "customCommitMessage";

	/**
	 * Starts a {@link Transaction} for the given model object.
	 * 
	 * @param message
	 *        The commit message to mark the change with.
	 * 
	 * @see KnowledgeBase#beginTransaction()
	 */
	default Transaction beginTransaction(Object model, ResKey message) {
		KnowledgeBase kb;
		if (model instanceof TLObject object) {
			kb = object.tKnowledgeBase();
		} else {
			kb = PersistencyLayer.getKnowledgeBase();
		}
		if (kb == null) {
			return null;
		} else {
			return kb.beginTransaction(message);
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
	 *        The model object also passed to {@link #beginTransaction(Object, ResKey)}.
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

	/**
	 * Retrieves an optional custom commit message from the command's arguments.
	 *
	 * @param arguments
	 *        The arguments of
	 *        {@link CommandHandler#handleCommand(com.top_logic.layout.DisplayContext, com.top_logic.mig.html.layout.LayoutComponent, Object, Map)}.
	 * @return A custom commit message to use, or <code>null</code> to choose a generic default
	 *         commit message.
	 */
	default ResKey getCustomCommitMessage(Map<String, Object> arguments) {
		return (ResKey) arguments.get(TransactionHandler.CUSTOM_COMMIT_MESSAGE);
	}

}
