/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.util;

import junit.framework.Test;

import com.top_logic.element.model.PersistentTLModel;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLModel;
import com.top_logic.util.model.Messages;

/**
 * Utilities for {@link Test}s about the {@link TLModel}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLModelTestUtil {

	/**
	 * Creates a {@link PersistentTLModel} in the given {@link KnowledgeBase}.
	 * 
	 * @return A new {@link PersistentTLModel}. Never null.
	 */
	public static TLModel createTLModelInTransaction(KnowledgeBase knowledgeBase) {
		return createTLModelKO(knowledgeBase);
	}

	private static TLModel createTLModelKO(KnowledgeBase knowledgeBase) {
		Transaction transaction = knowledgeBase.beginTransaction(Messages.CREATING_TL_MODEL);
		try {
			TLModel tlModel = PersistentTLModel.newInstance(knowledgeBase);
			transaction.commit();
			return tlModel;
		} finally {
			transaction.rollback();
		}
	}

}
