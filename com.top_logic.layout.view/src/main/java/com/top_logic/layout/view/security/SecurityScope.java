/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityRootObjectProvider;
import com.top_logic.tool.boundsec.simple.AbstractBoundChecker;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;

/**
 * A named access control unit of the declarative view system.
 *
 * <p>
 * A {@link SecurityScope} is the new-UI counterpart of a legacy
 * {@link com.top_logic.tool.boundsec.compound.CompoundSecurityLayout}: it carries a stable
 * {@link #getSecurityId() security id} that keys the persistent role assignment
 * ({@link PersBoundComp}) and decides — via the shared {@link BoundChecker} machinery — whether the
 * current user may see the UI unit guarded by it.
 * </p>
 *
 * <p>
 * For now the security object is always the security root (structure-level access), so a scope
 * answers the question "may this user see this part of the application". Per-object roles can be
 * added later without changing the call sites.
 * </p>
 *
 * @see SecurityScopeService
 */
public class SecurityScope extends AbstractBoundChecker {

	private final ResKey _label;

	/**
	 * Creates a {@link SecurityScope}.
	 *
	 * @param securityId
	 *        The stable security id, used as the {@link PersBoundComp} lookup key.
	 * @param label
	 *        Human-readable label for the security administration UI, may be {@code null}.
	 */
	public SecurityScope(ComponentName securityId, ResKey label) {
		super(securityId);
		_label = label;
	}

	/**
	 * Human-readable label for the security administration UI.
	 *
	 * @return May be {@code null}.
	 */
	public ResKey getLabel() {
		return _label;
	}

	/**
	 * Whether the current user may see a UI unit guarded by this scope.
	 *
	 * <p>
	 * Equivalent to the legacy "can show" check with the {@link #getDefaultCommandGroup() default
	 * command group}. A scope with no persisted role assignment grants access to nobody (except the
	 * technical super user) — protecting a unit is an explicit opt-in to access control.
	 * </p>
	 */
	public boolean isVisible() {
		return BoundChecker.allowShowModel(this, null);
	}

	@Override
	public BoundObject getSecurityObject(BoundCommandGroup commandGroup, Object potentialModel) {
		return SecurityRootObjectProvider.INSTANCE.getSecurityRoot();
	}

	@Override
	public Collection<BoundCommandGroup> getCommandGroups() {
		return Collections.singletonList(getDefaultCommandGroup());
	}

	@Override
	public Set<? extends BoundRole> getRolesForCommandGroup(BoundCommandGroup commandGroup) {
		PersBoundComp persBoundComp = persBoundComp();
		if (persBoundComp == null) {
			return Collections.emptySet();
		}
		return persBoundComp.rolesForCommandGroup(commandGroup);
	}

	@Override
	public boolean isDefaultCheckerFor(String type, BoundCommandGroup aBCG) {
		return false;
	}

	private PersBoundComp persBoundComp() {
		return PersBoundComp.getInstance(PersistencyLayer.getKnowledgeBase(), getSecurityId());
	}

}
