/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.simple;

import java.util.Objects;

import com.top_logic.basic.util.ResKey;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;
import com.top_logic.tool.execution.I18NConstants;

/**
 * Abstract implementation of {@link BoundChecker} implementing convenience methods.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractBoundChecker implements BoundChecker {

	private final ComponentName _securityId;

	/**
	 * Creates a new {@link AbstractBoundChecker}.
	 */
	public AbstractBoundChecker(ComponentName securityId) {
		_securityId = Objects.requireNonNull(securityId, "securityId must not be null");
	}

	@Override
	public ComponentName getSecurityId() {
		return _securityId;
	}

	@Override
	public ResKey hideReason(Object potentialModel) {
		return securityReason(potentialModel);
	}

	/**
	 * Utility to compute {@link #hideReason(Object)} based on access restrictions.
	 */
	protected final ResKey securityReason(Object potentialModel) {
		BoundCommandGroup group = getDefaultCommandGroup();
		if (!this.allow(group, getCurrentObject(group, potentialModel))) {
			return I18NConstants.ERROR_NO_PERMISSION;
		}
		return null;
	}

	@Override
	public BoundObject getCurrentObject(BoundCommandGroup aBCG, Object potentialModel) {
		return getSecurityObject(aBCG, potentialModel);
	}

	@Override
	public PersBoundComp getPersBoundComp() {
		return null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (_securityId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractBoundChecker other = (AbstractBoundChecker) obj;
		if (!_securityId.equals(other._securityId))
			return false;
		return true;
	}

}

