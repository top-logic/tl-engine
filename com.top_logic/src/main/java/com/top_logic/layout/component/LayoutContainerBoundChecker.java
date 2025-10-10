/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import java.util.Collection;
import java.util.List;

import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.BoundChecker;
import com.top_logic.tool.boundsec.BoundCheckerComponent;
import com.top_logic.tool.boundsec.BoundCheckerMixin;
import com.top_logic.tool.boundsec.BoundCommandGroup;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.NullSecurityObjectProvider;
import com.top_logic.tool.boundsec.wrap.PersBoundComp;

/**
 * {@link BoundCheckerComponent} that is a container for other components.
 */
public interface LayoutContainerBoundChecker extends BoundCheckerComponent {

	/**
	 * The inner components, if this component is a container, the empty list otherwise.
	 */
	List<? extends LayoutComponent> getChildList();

	/**
	 * Ask our children if given Person has access to aModel for given CommandGroup This should
	 * normally not be called as it tends to be very expensive.
	 */
	@Override
	default boolean allow(Person aPerson, BoundObject aModel, BoundCommandGroup aCmdGroup) {
		for (LayoutComponent theChild : getChildList()) {
			if (theChild instanceof BoundChecker) {
				if (((BoundChecker) theChild).allow(aPerson, aModel, aCmdGroup))
					return true; // one child allows, fine
			}
			// Children that are no BoundCheckers cannot allow this.
		}
		return false;
	}

	@Override
	default Collection<BoundChecker> getChildCheckers() {
		return BoundCheckerMixin.getChildCheckers(getChildList(), getDialogs());
	}

	@Override
	default void initPersBoundComp(PersBoundComp persBoundComp) {
		// Store.
		BoundCheckerComponent.super.initPersBoundComp(persBoundComp);

		// Distribute.
		for (LayoutComponent theChild : getChildList()) {
			if (theChild instanceof BoundCheckerComponent childChecker) {
				childChecker.initPersBoundComp(persBoundComp);
			}
		}
	}

	@Override
	default SecurityObjectProvider getSecurityObjectProvider() {
		return NullSecurityObjectProvider.INSTANCE;
	}

}
