/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person.infouser;

import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.constraints.AbstractConstraint;
import com.top_logic.util.Resources;
import com.top_logic.util.license.LicenseTool;
import com.top_logic.util.license.TLLicense;

/**
 * The {@link InfoUserConstraint} check that the {@link TLLicense} allows to change a user from
 * restricted user to an full user.
 * 
 * @author <a href="mailto:tri@top-logic.com">tri</a>
 */
public class InfoUserConstraint extends AbstractConstraint {

	/** Sole instance of {@link InfoUserConstraint} */
	public static final InfoUserConstraint INSTANCE = new InfoUserConstraint();

	@Override
	public boolean check(Object value) throws CheckException {
		return internalCheck(value);
	}

	private boolean internalCheck(Object value) throws CheckException {
		if (Boolean.FALSE.equals(value)) {
			if (!LicenseTool.moreUsersAllowed(LicenseTool.getInstance().getLicense(), false)) {
				Resources resources = Resources.getInstance();
				String error = resources.getString(I18NConstants.ERROR_FULL_USER_LIMIT_REACHED);
				throw new CheckException(error);
			}
		} else {
			if (!LicenseTool.moreUsersAllowed(LicenseTool.getInstance().getLicense(), Boolean.TRUE)) {
				Resources resources = Resources.getInstance();
				String error = resources.getString(I18NConstants.ERROR_RESTRICTED_USER_LIMIT_REACHED);
				throw new CheckException(error);
			}
		}
		return true;
	}

}
