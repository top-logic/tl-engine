/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.knowledge.service.AfterCommitAction;
import com.top_logic.knowledge.service.CommitActions;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Reaction to a change of an attribute value of a persistent object, executed within the commit
 * of the change.
 *
 * <p>
 * A handler is annotated to an attribute with {@link TLChangeHandlers}. It is called when a
 * transaction that changes the value of the annotated attribute of an existing object is
 * committed, independent of the source of the change (user interface, service, import, script).
 * The call happens within the commit, before the commit checks (e.g. the model constraints) run.
 * Changes the handler makes become part of the same commit: if the commit fails, they are rolled
 * back together with the change that triggered the handler.
 * </p>
 *
 * <p>
 * A handler is not called for an object that is created or deleted in the committed transaction.
 * Changes made by handlers do not trigger handlers again.
 * </p>
 *
 * <p>
 * A handler that must react after the commit succeeded (e.g. to inform the user) registers an
 * {@link AfterCommitAction} with the given {@link CommitActions}.
 * </p>
 *
 * @see TLChangeHandlers
 */
@FunctionalInterface
public interface TLChangeHandler {

	/**
	 * Informs this handler about a value change of the given attribute in the transaction being
	 * committed.
	 *
	 * @param object
	 *        The changed object. It existed before the transaction and is not deleted by it.
	 * @param attribute
	 *        The attribute whose value has changed. This is the attribute of the object's type,
	 *        which may be an override of the annotated attribute.
	 * @param oldValue
	 *        The committed value, i.e. the value before the transaction.
	 * @param newValue
	 *        The value in the transaction being committed.
	 * @param commitActions
	 *        Registration of actions to execute after the commit.
	 */
	void changed(TLObject object, TLStructuredTypePart attribute, Object oldValue, Object newValue,
			CommitActions commitActions);

}
