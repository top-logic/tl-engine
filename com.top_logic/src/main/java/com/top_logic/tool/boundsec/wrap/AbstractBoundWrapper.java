/*
 * SPDX-FileCopyrightText: 2003 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.wrap;

import java.util.Collection;
import java.util.Collections;

import com.top_logic.basic.TLID;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.tool.boundsec.BoundHelper;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.BoundRole;

/**
 * Persistent {@link com.top_logic.tool.boundsec.BoundObject}.
 *
 * @author <a href="mailto:dro@top-logic.com">Dieter Rothb&auml;cher</a>
 */
public abstract class AbstractBoundWrapper extends AbstractWrapper implements BoundObject {

	/** Constructor */
    public AbstractBoundWrapper(KnowledgeObject ko) {
        super(ko);
    }

    @Override
	public final TLID getID() {
		return KBUtils.getWrappedObjectName(this);
    }

    @Override
	public final Collection<BoundRole> getRoles(Person aPerson) {
		return BoundedRole.getRoles(this, toGroup(aPerson));
    }

	private static Group toGroup(Person aPerson) {
		return aPerson == null ? null : aPerson.getRepresentativeGroup();
	}

	@Override
	public final Collection<BoundRole> getRoles(Group aGroup) {
		return BoundedRole.getRoles(this, aGroup);
    }

    @Override
	public final Collection<BoundRole> getLocalAndGlobalRoles(Person aPerson) {
		return BoundedRole.getLocalAndGlobalRoles(this, aPerson);
    }

    @Override
	public final Collection<BoundRole> getLocalAndGlobalAndGroupRoles(Person aPerson) {
		return BoundedRole.getLocalAndGlobalAndGroupRoles(this, aPerson);
    }

    @Override
	public final boolean hasAnyRole(Person aPerson) {
		return BoundedRole.hasLocalOrGlobalOrGroupRole(this, aPerson);
    }

    @Override
	public final Collection<BoundRole> getRoles() {
		return BoundedRole.getRoles(this, null);
    }

    @Override
	public Collection<? extends BoundObject> getSecurityChildren() {
		return Collections.emptyList();
    }

    @Override
	public BoundObject getSecurityParent() {
		BoundHelper boundHelper = BoundHelper.getInstance();
		if (boundHelper.useDefaultObject()) {
			BoundObject securityRoot = boundHelper.getDefaultObject();
			if (securityRoot != this) {
				return securityRoot;
			}
        }

        return null;
    }

}
