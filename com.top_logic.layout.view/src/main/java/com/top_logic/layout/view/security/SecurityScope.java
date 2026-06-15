/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.mig.html.layout.ComponentName;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;
import com.top_logic.tool.boundsec.CommandGroupReference;
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

	private final List<CommandGroupReference> _commandGroupRefs;

	private volatile Collection<BoundCommandGroup> _commandGroups;

	/**
	 * Creates a {@link SecurityScope}.
	 *
	 * @param securityId
	 *        The stable security id, used as the {@link PersBoundComp} lookup key.
	 * @param label
	 *        Human-readable label for the security administration UI, may be {@code null}.
	 * @param commandGroupRefs
	 *        The non-default command groups declared on this scope (in addition to the
	 *        {@link #getDefaultCommandGroup() default} visibility group), against which command-level
	 *        access rules may be configured. Never {@code null}.
	 */
	public SecurityScope(ComponentName securityId, ResKey label, List<CommandGroupReference> commandGroupRefs) {
		super(securityId);
		_label = label;
		_commandGroupRefs = commandGroupRefs;
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

	/**
	 * Whether the current user may execute a command of the given command group in the context of the
	 * given security object.
	 *
	 * <p>
	 * The command-level counterpart of {@link #isVisible()}: it checks the given {@code group}
	 * (instead of the {@link #getDefaultCommandGroup() default} visibility group) against this scope's
	 * persisted role assignment. As with visibility, a scope with no role assigned for the group
	 * grants access to nobody except the technical super user.
	 * </p>
	 *
	 * @param group
	 *        The command group to check.
	 * @param securityObject
	 *        The object the roles are checked on. Pass the {@link SecurityRootObjectProvider security
	 *        root} for a structure-level check, or a concrete model object for a per-object check.
	 *        Must not be {@code null} (a {@code null} object would bypass the check).
	 */
	public boolean allowCommand(BoundCommandGroup group, BoundObject securityObject) {
		return BoundChecker.allowCommandOnSecurityObject(this, group, securityObject);
	}

	@Override
	public BoundObject getSecurityObject(BoundCommandGroup commandGroup, Object potentialModel) {
		return SecurityRootObjectProvider.INSTANCE.getSecurityRoot();
	}

	@Override
	public Collection<BoundCommandGroup> getCommandGroups() {
		Collection<BoundCommandGroup> result = _commandGroups;
		if (result == null) {
			// Resolve lazily: the command group registry may not yet be available when this scope is
			// constructed during service setup.
			Set<BoundCommandGroup> groups = new LinkedHashSet<>();
			groups.add(getDefaultCommandGroup());
			for (CommandGroupReference ref : _commandGroupRefs) {
				BoundCommandGroup group = ref.resolve();
				if (group != null) {
					groups.add(group);
				}
			}
			result = Collections.unmodifiableSet(groups);
			_commandGroups = result;
		}
		return result;
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
