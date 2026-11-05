/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.migration.processor;

/**
 * Test of the migrations of module {@value #MODULE} that set the <code>key-attributes</code> of
 * core types in the stored schema baseline.
 */
@SuppressWarnings("javadoc")
public class TestBaselineTransforms extends AbstractBaselineTransformTest {

	private static final String MODULE = "tl";

	private static final String TICKET_27205 = "Ticket_27205_KeyAttributes_for_Association.transform.xslt";

	public void testTicket27205Replace() throws Exception {
		assertReplacesKeyAttributes(MODULE, TICKET_27205, "association", "Association",
			"source,dest");
	}

	public void testTicket27205Add() throws Exception {
		assertAddsKeyAttributes(MODULE, TICKET_27205, "association", "Association",
			"source,dest", true);
	}

}
