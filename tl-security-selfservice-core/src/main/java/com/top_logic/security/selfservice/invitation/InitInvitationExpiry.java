/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.security.selfservice.invitation;

import java.util.Date;

import com.top_logic.basic.Logger;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.service.migration.StartupAction;
import com.top_logic.security.selfservice.model.Invitation;
import com.top_logic.security.selfservice.model.TlSecuritySelfserviceFactory;

/**
 * {@link StartupAction} giving every {@link Invitation} without {@link Invitation#getExpires()
 * expiry date} the expiry date of an invitation created at the time of the update.
 *
 * <p>
 * The expiry date is the time of the update plus the
 * {@link InvitationModule.Config#getInvitationValidity() configured validity}.
 * </p>
 */
public class InitInvitationExpiry implements StartupAction {

	@Override
	public void perform() {
		Date expires = InvitationModule.expiryFromNow();
		int updated = 0;
		for (Invitation invitation : MetaElementUtil
			.getAllInstancesOf(TlSecuritySelfserviceFactory.getInvitationType(), Invitation.class)) {
			if (invitation.getExpires() == null) {
				invitation.setExpires(expires);
				updated++;
			}
		}
		Logger.info("Set the expiry date of " + updated + " invitation(s) to " + expires + ".",
			InitInvitationExpiry.class);
	}

}
