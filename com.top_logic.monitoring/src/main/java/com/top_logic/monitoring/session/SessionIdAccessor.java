/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.session;

import com.top_logic.knowledge.monitor.UserSession;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.model.TLObject;
import com.top_logic.util.TopLogicServlet;

/**
 * Accessor for the {@link UserSession#SESSION_ID id} of a {@link UserSession}.
 * <p>
 * It converts the session id into a form in which it can be logged. See
 * {@link TopLogicServlet#hashSessionIdForLog(String)} for details.
 * </p>
 * 
 * @author <a href=mailto:jst@top-logic.com>Jan Stolzenburg</a>
 */
public class SessionIdAccessor extends ReadOnlyAccessor<TLObject> {

	@Override
	public Object getValue(TLObject object, String property) {
		if (object == null) {
			return null;
		}
		String value = (String) object.tValueByName(property);
		return TopLogicServlet.hashSessionIdForLog(value);
	}

}
