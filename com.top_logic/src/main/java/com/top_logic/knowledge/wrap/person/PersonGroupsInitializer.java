/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.wrap.person;

import java.util.Set;

import com.top_logic.base.services.InitialGroupManager;
import com.top_logic.model.TLObject;
import com.top_logic.model.initializer.TLObjectInitializer;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * {@link TLObjectInitializer} that initializes the {@link Group} relations of a new {@link Person}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class PersonGroupsInitializer implements TLObjectInitializer {

	/** Singleton {@link PersonGroupsInitializer} instance. */
	public static final PersonGroupsInitializer INSTANCE = new PersonGroupsInitializer();

	/**
	 * Creates a new {@link PersonGroupsInitializer}.
	 */
	protected PersonGroupsInitializer() {
		// singleton instance
	}

	@Override
	public void initializeObject(TLObject object) {
		Person account = (Person) object;

		Set<Group> eagerlyAssignedGroups = account.getGroups();

		Group representativeGroup = Group.createRepresentativeGroup(account);

		Group defaultGroup = InitialGroupManager.getInstance().getDefaultGroup();
		if (defaultGroup != null) {
			defaultGroup.addMember(account);
		}
		// Fix group assignments. If the new account was already added to some groups, the
		// representative group must also be assigned (which could not be done before this
		// initializer is run).
		for (Group group : eagerlyAssignedGroups) {
			group.addMember(representativeGroup);
		}

	}

}
