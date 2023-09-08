/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.contact.orgunit;

import com.top_logic.basic.TLID;
import com.top_logic.element.core.CreateElementException;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.util.NumberHandler;
import com.top_logic.element.structured.util.NumberHandlerFactory;
import com.top_logic.model.TLClass;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link OrgUnitFactory} with post processing of newly created {@link OrgUnitAll}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DefaultOrgUnitFactory extends OrgUnitFactory {

	@Override
	public StructuredElement createChild(String aStructureName, StructuredElement aWrapper, TLID anID,
			String aName, TLClass type) throws CreateElementException {
		StructuredElement newChild = super.createChild(aStructureName, aWrapper, anID, aName, type);
		initOrgUnitAll((OrgUnitAll) newChild);
		return newChild;
	}

	/**
	 * Initialises the given new {@link OrgUnitAll}.
	 */
	protected void initOrgUnitAll(OrgUnitAll newOrgUnitAll) {
		if (newOrgUnitAll instanceof OrgUnitBase) {
			OrgUnitBase orgUnitBase = (OrgUnitBase) newOrgUnitAll;
			initOrgUnitBase(orgUnitBase);
		}
	}

	/**
	 * Initialises the given new {@link OrgUnitBase}.
	 */
	protected void initOrgUnitBase(OrgUnitBase newOrgUnitBase) {
		DefaultOrgUnitFactory.createOrgUnitID(newOrgUnitBase);
	}

	/**
	 * Create a new unique ID and append this to the given {@link OrgUnitBase}.
	 * 
	 * @param orgUnitBase
	 *        The {@link OrgUnitBase} element to create the new ID for.
	 * @throws TopLogicException
	 *         If creating the ID fails for a reason.
	 */
	public static void createOrgUnitID(OrgUnitBase orgUnitBase) {
		NumberHandler handler;
		try {
			handler = NumberHandlerFactory.getInstance().getNumberHandler("OrgUnit");
		} catch (Exception ex) {
			throw new TopLogicException(DefaultOrgUnitFactory.class, "create.number.handler.get",
				new Object[] { orgUnitBase.getName() }, ex);
	    }
		if (handler == null) {
			return;
		}
		{
			try {
				orgUnitBase.setValue(OrgUnitBase.ATTRIBUTE_ORG_ID, handler.generateId(orgUnitBase));
			} catch (Exception ex) {
				throw new TopLogicException(DefaultOrgUnitFactory.class, "create.number",
					new Object[] { orgUnitBase.getName() }, ex);
			}
		}
	}

}

