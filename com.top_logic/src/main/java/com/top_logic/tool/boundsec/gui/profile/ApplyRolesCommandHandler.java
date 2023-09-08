/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * {@link AbstractApplyCommandHandler} to apply changes made in the
 * {@link EditSecurityProfileComponent}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ApplyRolesCommandHandler extends AbstractApplyCommandHandler {

	/**
	 * {@link CommandHandler#getID() Id} that is used to register the
	 * {@link ApplyRolesCommandHandler} in the {@link CommandHandlerFactory}.
	 * 
	 * @see CommandHandlerFactory#getHandler(String)
	 */
	public static final String COMMAND_ID = "applyRoleRights";

	/**
	 * Creates a new {@link ApplyRolesCommandHandler}.
	 */
	public ApplyRolesCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	private DisplayContext displayContext() {
		return DefaultDisplayContext.getDisplayContext();
	}

	@Override
	protected boolean storeChanges(LayoutComponent component, FormContext aContext, Object aModel) {
		Collection<FormMember> changedMembers = aContext.getChangedMembers();
		if (changedMembers.isEmpty()) {
			return false;
		}
		{
			Map<ConfigNode, Set<BoundedRole>> layouts = new HashMap<>();
			for (FormMember member : changedMembers) {
				CommandNode node = member.get(EditSecurityProfileComponent.COMMAND_NODE);
				ConfigNode configNode = node.configNode();
				boolean asBoolean = ((BooleanField) member).getAsBoolean();
				Set<BoundedRole> roles = ((EditSecurityProfileComponent) component)._rolesMap.get(member.getName());
				configNode.storeRight(roles, node.group(), asBoolean);
				Set<BoundedRole> set = layouts.get(configNode);
				if (set == null) {
					set = new HashSet<>();
					layouts.put(configNode, set);
				}
				set.addAll(roles);
			}
			for (ConfigNode node : layouts.keySet()) {
				ensureRead(node, layouts.get(node));
			}
		}
		return true;
	}

	private void ensureRead(ConfigNode node, Set<BoundedRole> roles) {
		List<BoundCommandGroup> commands = node.getCommandGroups();
		boolean hasAnyRight = false;
		for (BoundedRole role : roles) {
			Set<BoundedRole> roleSet = Collections.singleton(role);
			for (BoundCommandGroup cgn : commands) {
				hasAnyRight = node.hasRight(roleSet, cgn);
				if (hasAnyRight) {
					break;
				}
			}
			ConfigNode csl = node.getParent();
			while (csl != null) {
				if (ensureRead(csl, roleSet, hasAnyRight)) {
					csl = csl.getParent();
				} else {
					break;
				}
			}
		}
	}

	private boolean ensureRead(ConfigNode csl, Set<BoundedRole> roles, boolean shouldHaveRight) {
		boolean hasRight = csl.hasRight(roles, SimpleBoundCommandGroup.READ);
		boolean hasChange = hasRight != shouldHaveRight;
		if (hasChange) {
			if (!shouldHaveRight) {
				if (anySubNeedsRight(csl, roles)) {
					return false;
				}
			}
			csl.storeRight(roles, SimpleBoundCommandGroup.READ, shouldHaveRight);
		}
		return hasChange;
	}

	private boolean anySubNeedsRight(ConfigNode csl, Set<BoundedRole> roles) {
		List<SecurityNode> children = csl.getChildren();
		for (SecurityNode child : children) {
			if (child instanceof ConfigNode) {
				ConfigNode childConfigNode = (ConfigNode) child;
				List<BoundCommandGroup> commands = childConfigNode.getCommandGroups();
				for (BoundCommandGroup cgn : commands) {
					if (childConfigNode.hasRight(roles, cgn)) {
						return true;
					}
				}
				if (anySubNeedsRight(childConfigNode, roles)) {
					return true;
				}
			}
		}
		return false;
	}

}