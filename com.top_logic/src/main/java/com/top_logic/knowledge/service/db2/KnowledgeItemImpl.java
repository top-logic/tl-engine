/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2;

import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.db2.DBKnowledgeItem.AliveState;
import com.top_logic.knowledge.wrap.ImplementationFactory;
import com.top_logic.model.TLObject;



/**
 * Static mix-in implementation of the {@link KnowledgeItemInternal} interface.
 * 
 * <p>
 * This class (and its subclasses) contain all implementations that are shared between
 * {@link DBKnowledgeItem original objects} and their immutable variants.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class KnowledgeItemImpl {

	/**
	 * Whether the given object is valid in the current context.
	 */
	protected static void checkAccess(KnowledgeItemInternal self) {
		DBContext context = self.getKnowledgeBase().getCurrentDBContext();
		checkAccess(self, context);
	}

	/**
	 * Whether the given object is valid in the given context.
	 */
	protected static void checkAccess(KnowledgeItemInternal self, DBContext context) {
		self.checkAlive(context);
	}

	/**
	 * Implementation of {@link DBKnowledgeItem#initWrapper()}.
	 */
	public static TLObject createWrapper(KnowledgeItem dbKnowledgeItem) {
		KnowledgeBase kb = dbKnowledgeItem.getKnowledgeBase();
		ImplementationFactory implementationFactory = ((DBKnowledgeBase) kb).getImplementationFactory();
		return implementationFactory.createBinding(dbKnowledgeItem);
	}

	static String toString(KnowledgeItem item) {
		return String.valueOf(item.tId());
	}

	/**
	 * Appends a string representation for the given not alive item.
	 * 
	 * @see KnowledgeItem#isAlive()
	 */
	static void toStringInvalid(StringBuilder out, KnowledgeItem item) {
		if (item instanceof DBKnowledgeItem) {
			DBKnowledgeItem dbki = (DBKnowledgeItem) item;
			long sessionRevision = DBKnowledgeItem.IN_SESSION_REVISION;
			AliveState aliveState = dbki.aliveState(dbki.getKnowledgeBase().getCurrentDBContext(), sessionRevision);
			dbki.appendInvalidToString(out, aliveState, sessionRevision);
		} else {
			out.append(item.tId());
		}
	}

}
