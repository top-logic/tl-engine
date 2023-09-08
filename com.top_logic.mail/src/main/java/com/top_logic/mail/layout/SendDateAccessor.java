/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mail.layout;

import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.mail.base.Mail;

/**
 * Access to {@link Mail#getSentDate()}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SendDateAccessor extends ReadOnlyAccessor<Object> {

	public static final String SEND_DATE = "sendDate";

	@Override
	public Object getValue(Object object, String property) {
		if (object instanceof Mail) {
			return ((Mail) object).getSentDate();
		}
		return null;
	}

}

