/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.knowledge.service.AfterCommitAction;
import com.top_logic.knowledge.service.CommitActions;
import com.top_logic.knowledge.service.event.Modification;
import com.top_logic.model.TLObject;
import com.top_logic.model.initializer.TLObjectInitializer;

/**
 * Reaction to the creation and deletion of a persistent object, executed within the transaction
 * that creates or deletes the object.
 *
 * <p>
 * A handler is annotated to a type with {@link TLLifecycleHandlers}. It is informed about objects
 * of the annotated type and its specializations, independent of the source of the change (user
 * interface, service, import, script). Changes the handler makes become part of the transaction it
 * is called in: if the transaction fails or is rolled back, they are rolled back together with the
 * change that triggered the handler.
 * </p>
 *
 * <p>
 * The creation is reported once in the commit (see {@link #created(TLObject, CommitActions)}),
 * the deletion immediately with each delete (see {@link #deleting(TLObject, CommitActions)}).
 * </p>
 *
 * <p>
 * A handler that must react after the commit succeeded (e.g. to inform the user) registers an
 * {@link AfterCommitAction} with the given {@link CommitActions}.
 * </p>
 *
 * <p>
 * All methods do nothing by default, a handler implements only the notifications it needs.
 * </p>
 *
 * @see TLLifecycleHandlers
 */
public interface TLLifecycleHandler {

	/**
	 * Informs this handler about the creation of the given object.
	 *
	 * <p>
	 * The call happens in the commit of the transaction that created the object, after the
	 * {@link TLObjectInitializer}s of all objects created in this transaction have run and before
	 * the commit checks (e.g. the model constraints). An object that is deleted in the same
	 * transaction that created it is not reported.
	 * </p>
	 *
	 * <p>
	 * Objects created by a handler (or by any other reaction running within the commit) are not
	 * reported to the handlers of their types. In contrast, an object deleted by this handler is
	 * reported to {@link #deleting(TLObject, CommitActions)} of the handlers of its type, since
	 * deletions are reported with each delete.
	 * </p>
	 *
	 * @param object
	 *        The created object.
	 * @param commitActions
	 *        Registration of actions to execute after the commit.
	 */
	default void created(TLObject object, CommitActions commitActions) {
		// No reaction by default.
	}

	/**
	 * Informs this handler about the upcoming deletion of the given object.
	 *
	 * <p>
	 * The call happens when the object is deleted within the deleting transaction, before the
	 * deletion is executed. At this time the transaction is locked: the object and its values can
	 * still be read, but nothing may be modified. Changes are made by the returned
	 * {@link Modification}. It runs after the object and the objects deleted with it (e.g. the
	 * parts of a composition) have been removed, still within the same transaction. Its changes
	 * become part of that transaction and are rolled back with it.
	 * </p>
	 *
	 * <p>
	 * An object that is deleted in the same transaction that created it is not reported (its
	 * creation is not reported either).
	 * </p>
	 *
	 * <p>
	 * The deletion is reported with each delete, not once in the commit: An object deleted by the
	 * returned {@link Modification}, by {@link #created(TLObject, CommitActions)}, or by any other
	 * reaction is reported to the handlers of its type as well.
	 * </p>
	 *
	 * @param object
	 *        The object being deleted.
	 * @param commitActions
	 *        Registration of actions to execute after the commit of the deleting transaction.
	 * @return The changes to make after the deletion, {@link Modification#NONE} for none.
	 */
	default Modification deleting(TLObject object, CommitActions commitActions) {
		return Modification.NONE;
	}

}
