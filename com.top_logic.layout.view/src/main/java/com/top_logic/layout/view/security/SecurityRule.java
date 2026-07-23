/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.security;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelRefFormat;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ContextDependentRule;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityRootObjectProvider;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link ViewExecutabilityRule} that hides a command unless the current user is granted its command
 * group on a {@link SecurityScope}.
 *
 * <p>
 * The command-level counterpart of the visibility {@link AccessControl} on removable units: where
 * visibility asks "may this user see this part of the app", this rule asks "may this user run
 * <em>this action</em> here". The decision goes through the same {@link SecurityScope} (and thus the
 * same persistent role assignment) as visibility, so the new UI agrees with legacy semantics. Since
 * a command's server-side dispatch re-evaluates its executability rule, this rule is enforced both
 * on the button (disabled/hidden) and at execution time.
 * </p>
 *
 * <p>
 * Two things are resolved when the rule is {@link #bind(ViewContext) bound}:
 * </p>
 * <ul>
 * <li>The {@link SecurityScope} providing the role mapping: the explicitly configured
 * {@link Config#getScope() scope} if set, otherwise the
 * {@link ViewContext#getScope(Class) enclosing unit's scope}. A guarded command that resolves to
 * no scope fails closed.</li>
 * <li>The {@link BoundObject security object} the roles are checked on: the value of the configured
 * {@link Config#getSecurityObject() security-object channel} if it is a {@link BoundObject},
 * otherwise the security root (structure-level check).</li>
 * </ul>
 *
 * @see SecurityScope#allowCommand(BoundCommandGroup, BoundObject)
 */
public class SecurityRule implements ViewExecutabilityRule, ContextDependentRule {

	/**
	 * Configuration for {@link SecurityRule}.
	 */
	@TagName("security")
	public interface Config extends ViewExecutabilityRule.Config {

		/** Configuration name for {@link #getScope()}. */
		String SCOPE = "scope";

		/** Configuration name for {@link #getGroup()}. */
		String GROUP = "group";

		/** Configuration name for {@link #getSecurityObject()}. */
		String SECURITY_OBJECT = "security-object";

		@Override
		@ClassDefault(SecurityRule.class)
		Class<? extends ViewExecutabilityRule> getImplementationClass();

		/**
		 * The id of the security scope providing the role mapping for this command.
		 *
		 * <p>
		 * When unset, the scope of the nearest enclosing removable unit (the {@code access-control}
		 * gating the surrounding nav-item, tab or tile) is used, so the command shares the role
		 * mapping that already gates its visibility. A guarded command that is neither given a scope
		 * nor enclosed by one denies access to everyone.
		 * </p>
		 */
		@Name(SCOPE)
		@Nullable
		String getScope();

		/**
		 * The command group required to execute the command.
		 *
		 * <p>
		 * The group (e.g. {@code Write}, {@code Delete}) must be declared on the referenced scope so
		 * that roles can be assigned to it.
		 * </p>
		 */
		@Name(GROUP)
		@Mandatory
		CommandGroupReference getGroup();

		/**
		 * Channel whose value is the object the access is checked on.
		 *
		 * <p>
		 * When unset, the access is checked on the security root (a structure-level "may this user
		 * run this action here" check). When set, the channel's current value is used as the security
		 * object, enabling per-object access checks. A channel value that is not a security object
		 * falls back to the security root.
		 * </p>
		 */
		@Name(SECURITY_OBJECT)
		@Nullable
		@Format(ChannelRefFormat.class)
		ChannelRef getSecurityObject();
	}

	private final String _scopeId;

	private final CommandGroupReference _groupRef;

	private final ChannelRef _securityObjectRef;

	private SecurityScope _scope;

	private BoundCommandGroup _group;

	private ViewChannel _securityObjectChannel;

	/**
	 * Creates a {@link SecurityRule} from configuration.
	 */
	@CalledByReflection
	public SecurityRule(InstantiationContext context, Config config) {
		_scopeId = config.getScope();
		_groupRef = config.getGroup();
		_securityObjectRef = config.getSecurityObject();
	}

	@Override
	public void bind(ViewContext context) {
		if (_scopeId != null && !_scopeId.isEmpty()) {
			_scope = SecurityScopeService.getInstance().getScope(_scopeId);
			if (_scope == null) {
				Logger.error("Reference to undefined security scope '" + _scopeId + "'.", SecurityRule.class);
			}
		} else {
			_scope = context.getScope(SecurityScope.class);
		}
		_group = _groupRef.resolve();
		_securityObjectChannel = _securityObjectRef != null ? context.resolveChannel(_securityObjectRef) : null;

		if (_scope != null && _group != null && !_scope.getCommandGroups().contains(_group)) {
			Logger.warn("Command group '" + _group.getID() + "' is not declared on security scope '"
				+ _scope.getSecurityId() + "'; no role can be assigned to it, so the command is denied for"
				+ " everyone except the technical super user.", SecurityRule.class);
		}
	}

	@Override
	public ExecutableState isExecutable(Object input) {
		if (_scope == null) {
			// Guarded command without a resolvable scope: fail closed.
			return ExecutableState.NOT_EXEC_HIDDEN;
		}
		return _scope.allowCommand(_group, securityObject()) ? ExecutableState.EXECUTABLE
			: ExecutableState.NOT_EXEC_HIDDEN;
	}

	private BoundObject securityObject() {
		Object value = _securityObjectChannel != null ? _securityObjectChannel.get() : null;
		if (value instanceof BoundObject boundObject) {
			return boundObject;
		}
		// No (suitable) per-object value: check on the security root (structure-level).
		return SecurityRootObjectProvider.INSTANCE.getSecurityRoot();
	}
}
