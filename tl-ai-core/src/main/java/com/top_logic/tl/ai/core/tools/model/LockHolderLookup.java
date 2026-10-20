/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.model;

import com.top_logic.base.locking.token.LockInfo;
import com.top_logic.base.locking.token.Token;
import com.top_logic.base.locking.token.TokenService;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;

/**
 * Looks up the current lock holder for a given object via {@link TokenService}.
 *
 * <p>Shared by {@link LockTool} (lock status queries) and
 * {@link com.top_logic.tl.ai.core.agent.TransactionScope} (lock conflict diagnostics)
 * to avoid duplicating the token-scanning logic.</p>
 */
public final class LockHolderLookup {

	private LockHolderLookup() {
		// Utility class
	}

	/**
	 * Find the display name of the user holding a lock on the given object.
	 *
	 * @param target the object to check
	 * @return the holder's display label, or {@code null} if the object is not locked
	 */
	public static String findLockHolder(TLObject target) {
		try {
			for (LockInfo info : TokenService.getInstance().getAllLocks()) {
				for (Token token : info.getTokens()) {
					if (target.equals(token.getObject())) {
						return resolveOwnerLabel(info.getOwner());
					}
				}
			}
		} catch (Exception e) {
			// Best effort — don't fail the caller because of holder lookup
		}
		return null;
	}

	private static String resolveOwnerLabel(TLObject owner) {
		if (owner == null) {
			return "unknown";
		}
		String label = MetaLabelProvider.INSTANCE.getLabel(owner);
		return label != null ? label : owner.toString();
	}
}
