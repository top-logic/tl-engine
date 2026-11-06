/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation;

import com.top_logic.element.model.copy.CopyOperation;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.security.selfservice.model.Invitation;

/**
 * {@link DefaultProvider} for {@link Invitation#getExpires()}: the current time plus the
 * {@link InvitationModule.Config#getInvitationValidity() validity of an invitation}.
 *
 * <p>
 * The validity counts from the moment the invitation is stored, not from the moment a draft of
 * it was created: the default is {@link #isComputedInTransaction() computed in the transaction}
 * creating the persistent invitation. A transient draft, e.g. the invitation edited in a create
 * dialog, has no expiry date, and storing the draft keeps the default computed for the persistent
 * invitation instead of copying the draft's empty value (see
 * {@link CopyOperation#skipTransactionDefaults(boolean)}).
 * </p>
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

	@Override
	public boolean isComputedInTransaction() {
		return true;
	}

}
