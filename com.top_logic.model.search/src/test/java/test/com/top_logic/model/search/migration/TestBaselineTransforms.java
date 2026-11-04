/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.search.migration;

import test.com.top_logic.knowledge.service.db2.migration.processor.AbstractBaselineTransformTest;

/**
 * Test of the migrations of module {@value #MODULE} that set the <code>key-attributes</code> of
 * search types in the stored schema baseline.
 */
@SuppressWarnings("javadoc")
public class TestBaselineTransforms extends AbstractBaselineTransformTest {

	private static final String MODULE = "tl-model-search";

	private static final String TICKET_28710 = "Ticket_28710_Added_TLSearch.transform.xslt";

	public void testTicket28710Replace() throws Exception {
		assertReplacesKeyAttributes(MODULE, TICKET_28710, "metaobject", "FlexReport",
			"tType");
	}

	public void testTicket28710Add() throws Exception {
		assertAddsKeyAttributes(MODULE, TICKET_28710, "metaobject", "FlexReport",
			"tType", true);
	}

}
