/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.gui.profile;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
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
				BoundedRole role = member.get(EditSecurityProfileComponent.ROLES);
				configNode.storeRight(role, node.group(), asBoolean);
				layouts.computeIfAbsent(configNode, unused -> new HashSet<>()).add(role);
			}
			for (ConfigNode node : layouts.keySet()) {
				ensureRead(node, layouts.get(node));
			}
		}
		return true;
	}

	private void ensureRead(ConfigNode node, Set<BoundedRole> roles) {
		for (BoundedRole role : roles) {
			ImplicitReadRights.updateImplicitReadRights(node.layoutNode(), role);
		}
	}

}