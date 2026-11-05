/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.migration;

import test.com.top_logic.knowledge.service.db2.migration.processor.AbstractBaselineTransformTest;

/**
 * Test of the migrations of module {@value #MODULE} that set the <code>key-attributes</code> of
 * element types, or add attributes to them, in the stored schema baseline.
 */
@SuppressWarnings("javadoc")
public class TestBaselineTransforms extends AbstractBaselineTransformTest {

	private static final String MODULE = "tl-element";

	private static final String TICKET_27205 = "Ticket_27205_KeyAttributes_for_Association.transform.xslt";

	private static final String TICKET_28604 = "Ticket_28604_changelog.transform.xslt";

	private static final String TICKET_29088 = "Ticket_29088_Add_PersistentView.transform.db.xslt";

	private static final String PERS_BOUND_COMP = "PersBoundComp";

	private static final String T_TYPE = "tType";

	public void testTicket27205Replace() throws Exception {
		assertReplacesKeyAttributes(MODULE, TICKET_27205, "association", "hasWrapperAttValueBaseAssociation",
			"source,dest,metaAttribute");
	}

	public void testTicket27205AddToAnnotations() throws Exception {
		assertAddsKeyAttributes(MODULE, TICKET_27205, "association", "hasWrapperAttValueBaseAssociation",
			"source,dest,metaAttribute", true);
	}

	public void testTicket27205AddWithoutAnnotations() throws Exception {
		assertAddsKeyAttributes(MODULE, TICKET_27205, "association", "hasWrapperAttValueBaseAssociation",
			"source,dest,metaAttribute", false);
	}

	public void testTicket28604GeneralizationsReplace() throws Exception {
		assertReplacesKeyAttributes(MODULE, TICKET_28604, "association", "MetaElement_generalizations",
			"source,dest");
	}

	public void testTicket28604GeneralizationsAdd() throws Exception {
		assertAddsKeyAttributes(MODULE, TICKET_28604, "association", "MetaElement_generalizations",
			"source,dest", true);
	}

	public void testTicket28604HistoricValueReplace() throws Exception {
		assertReplacesKeyAttributes(MODULE, TICKET_28604, "metaobject", "hasHistoricValue",
			"source,dest,metaAttribute");
	}

	public void testTicket28604HistoricValueAdd() throws Exception {
		assertAddsKeyAttributes(MODULE, TICKET_28604, "metaobject", "hasHistoricValue",
			"source,dest,metaAttribute", true);
	}

	public void testTicket28604I18NAttributeStorageReplace() throws Exception {
		assertReplacesKeyAttributes(MODULE, TICKET_28604, "metaobject", "I18NAttributeStorage",
			"object, metaAttribute");
	}

	public void testTicket29088Replace() throws Exception {
		assertTransform(MODULE, TICKET_29088, "metaobject", PERS_BOUND_COMP,
			true, true, null, T_TYPE, T_TYPE);
	}

	public void testTicket29088Add() throws Exception {
		assertTransform(MODULE, TICKET_29088, "metaobject", PERS_BOUND_COMP,
			true, false, null, T_TYPE, T_TYPE);
	}

	public void testTicket29088ReplaceAttribute() throws Exception {
		assertTransform(MODULE, TICKET_29088, "metaobject", PERS_BOUND_COMP,
			true, true, T_TYPE, T_TYPE, T_TYPE);
	}

}
