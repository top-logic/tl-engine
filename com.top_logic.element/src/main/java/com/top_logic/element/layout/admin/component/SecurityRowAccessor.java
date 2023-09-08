/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.admin.component;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.security.SecurityStorage;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.tool.boundsec.BoundObject;
import com.top_logic.tool.boundsec.wrap.BoundedRole;

/**
 * @author    <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class SecurityRowAccessor extends ReadOnlyAccessor<SecurityRow> {

	private static final String TARGET = "target";

	private static final String WRAPPER = "wrapper";

	private static final String ROLE = "role";

	private static final String REASON = "reason";

	private static final String SEC_PARENT = "secParent";

	private static final String SEC_PARENTS = "secParents";

	private static final String TYPE = "type";

	private static final String MO_TYPE = "moType";

	private static final String ME_TYPE = "meType";

	private static final String BUS_OBJ = "bus_obj";

	/** Columns provided by this accessor. */
	public static final String[] ALL_COLUMNS = {
		TARGET, WRAPPER, ROLE, REASON,
		TYPE, ME_TYPE, MO_TYPE, BUS_OBJ,
		SEC_PARENT, SEC_PARENTS
	};

	@Override
	public Object getValue(SecurityRow aRow, String aKey) {
		if (aKey.equals(SecurityRowAccessor.TARGET)) {
			return aRow.getTarget();
		}
		else if (aKey.equals(SecurityRowAccessor.WRAPPER)) {
			return aRow.getBO();
		}
		else if (aKey.equals(SecurityRowAccessor.ROLE)) {
			return aRow.getRole();
		}
		else if (aKey.equals(SecurityRowAccessor.REASON)) {
			Object reason = aRow.getReason();
			if (SecurityStorage.REASON_HAS_ROLE.equals(reason)) {
				return BoundedRole.HAS_ROLE_ASSOCIATION;
			}
			return reason;
		}
		else if (aKey.equals(SecurityRowAccessor.TYPE)) {
			Object theBO = aRow.getBO();

			return (theBO != null) ? theBO.getClass().getSimpleName() : "?";
		}
		else if (aKey.equals(SecurityRowAccessor.ME_TYPE)) {
			Object theBO = aRow.getBO();

			return (theBO instanceof Wrapper) ? ((Wrapper) theBO).tType() : null;
		}
		else if (aKey.equals(SecurityRowAccessor.MO_TYPE)) {
			Object theBO = aRow.getBO();

			return (theBO instanceof Wrapper) ? ((Wrapper) theBO).tTable() : null;
		}
		else if (aKey.equals(SecurityRowAccessor.BUS_OBJ)) {
			Object theBO = aRow.getBO();

			return (theBO instanceof Wrapper) ? KBUtils.getWrappedObjectName((Wrapper) theBO) : null;
		}
		else if (aKey.equals(SecurityRowAccessor.SEC_PARENT)) {
			Object theBO = aRow.getBO();

			return (theBO instanceof BoundObject) ? ((BoundObject) theBO).getSecurityParent() : null;
		}
		else if (aKey.equals(SecurityRowAccessor.SEC_PARENTS)) {
			List<BoundObject> theList   = new ArrayList<>();
			Object            theBO     = aRow.getBO();
			BoundObject       theParent = (theBO instanceof BoundObject) ? ((BoundObject) theBO).getSecurityParent() : null;

            while (theParent != null) {
                theList.add(theParent);
                theParent = theParent.getSecurityParent();
            }

            return theList;
		}
		else {
			return null;
		}
	}
}
