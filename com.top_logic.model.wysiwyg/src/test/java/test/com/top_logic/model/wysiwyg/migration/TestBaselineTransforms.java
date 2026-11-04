/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.wysiwyg.migration;

import test.com.top_logic.knowledge.service.db2.migration.processor.AbstractBaselineTransformTest;

/**
 * Test of the migrations of module {@value #MODULE} that set the <code>key-attributes</code> of
 * wysiwyg types in the stored schema baseline.
 */
@SuppressWarnings("javadoc")
public class TestBaselineTransforms extends AbstractBaselineTransformTest {

	private static final String MODULE = "tl-model-wysiwyg";

	private static final String TICKET_28604 = "Ticket_28604_changelog.transform.xslt";

	public void testTicket28604Replace() throws Exception {
		assertReplacesKeyAttributes(MODULE, TICKET_28604, "metaobject", "HTMLAttributeStorage",
			"object, metaAttribute");
	}

	public void testTicket28604Add() throws Exception {
		assertAddsKeyAttributes(MODULE, TICKET_28604, "metaobject", "HTMLAttributeStorage",
			"object, metaAttribute", true);
	}

}
