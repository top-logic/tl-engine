/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation;

import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.security.selfservice.model.Invitation;

/**
 * {@link DefaultProvider} for {@link Invitation#getExpires()}: the current time plus the
 * {@link InvitationModule.Config#getInvitationValidity() validity of an invitation}.
 */
@TargetType(TLTypeKind.DATE)
public class InvitationExpiryDefault implements DefaultProvider {

	/**
	 * Singleton {@link InvitationExpiryDefault} instance.
	 */
	public static final InvitationExpiryDefault INSTANCE = new InvitationExpiryDefault();

	private InvitationExpiryDefault() {
		// Singleton constructor.
	}

	@Override
	public Object createDefault(Object context, TLStructuredTypePart attribute) {
		return InvitationModule.expiryFromNow();
	}

}
