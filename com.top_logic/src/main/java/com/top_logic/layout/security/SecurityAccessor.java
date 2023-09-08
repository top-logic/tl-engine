/*
 * SPDX-FileCopyrightText: 2007 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.layout.Accessor;
import com.top_logic.tool.boundsec.IGroup;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;

/**
 * {@link Accessor} wrapping values as {@link ProtectedValue}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SecurityAccessor implements Accessor {
	
	private static final ProtectedValue ACCESS_DENIED = new ProtectedValue(Collections.EMPTY_SET, null);

	private final IGroup currentGroup;
	private boolean isSuperUser = false;

	private final Accessor valueAccessor;
	private final SecurityProvider securityProvider;


	public SecurityAccessor(IGroup currentGroup, SecurityProvider securityProvider, Accessor valueAccessor) {
	    this.currentGroup = currentGroup;
		this.valueAccessor = valueAccessor;
		this.securityProvider = securityProvider;
		this.isSuperUser = ThreadContext.isSuperUser();
	}

	@Override
	public Object getValue(Object object, String property) {
		if (!isSuperUser && securityProvider.isRestricted(object, property)) {
			return getProtectedValue(object, property);
		} else {
			return valueAccessor.getValue(object, property);
		}
	}

	public final ProtectedValue getProtectedValue(Object object, String property) {
		Set accessRights = securityProvider.getAccessRights(object, property, currentGroup);
		if (accessRights.isEmpty()) {
			// Short-cut that suppresses getting the value, if there are no
			// access rights.
			return ACCESS_DENIED;
		}
		return new ProtectedValue(accessRights, valueAccessor.getValue(object, property));
	}

	@Override
	public void setValue(Object object, String property, Object value) {
		if (!isSuperUser && !securityProvider.hasAccessRight(object, property, currentGroup, SimpleBoundCommandGroup.WRITE)) {
			throw new SecurityException("Write access to property '" + property + "' denied for object '" + object + "'");
		}
		valueAccessor.setValue(object, property, value);
	}

    public Accessor getInnerSecurityProvider() {
        return (this.valueAccessor);
    }

}
