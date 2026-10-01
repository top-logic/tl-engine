/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.monitor;

import java.util.HashSet;
import java.util.Set;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.knowledge.monitor.StoreUserEventListener;
import com.top_logic.knowledge.monitor.UserSession;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.I18NConstants;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.Transaction;

/**
 * Removes the {@link UserSession} records a test creates.
 *
 * <p>
 * Logging in a user in a test records a {@link UserSession} (through the
 * {@link StoreUserEventListener}), which stays in the knowledge base after the test. Tests that log
 * in users take a {@link #snapshot(KnowledgeBase)} before and call {@link #deleteCreated()}
 * afterwards, so that later tests sharing the knowledge base do not see these records.
 * </p>
 */
public final class UserSessionCleanup {

	private final KnowledgeBase _kb;

	private final Set<ObjectKey> _existing;

	private UserSessionCleanup(KnowledgeBase kb, Set<ObjectKey> existing) {
		_kb = kb;
		_existing = existing;
	}

	/**
	 * Records the {@link UserSession}s currently present in the given {@link KnowledgeBase}.
	 */
	public static UserSessionCleanup snapshot(KnowledgeBase kb) {
		Set<ObjectKey> existing = new HashSet<>();
		for (KnowledgeObject userSession : kb.getAllKnowledgeObjects(UserSession.OBJECT_NAME)) {
			existing.add(userSession.tId());
		}
		return new UserSessionCleanup(kb, existing);
	}

	/**
	 * Deletes all {@link UserSession}s created since the {@link #snapshot(KnowledgeBase)}.
	 */
	public void deleteCreated() {
		try (Transaction tx = _kb.beginTransaction(I18NConstants.NO_COMMIT_MESSAGE)) {
			for (KnowledgeObject userSession : _kb.getAllKnowledgeObjects(UserSession.OBJECT_NAME)) {
				if (!_existing.contains(userSession.tId())) {
					userSession.delete();
				}
			}
			tx.commit();
		}
	}

}
