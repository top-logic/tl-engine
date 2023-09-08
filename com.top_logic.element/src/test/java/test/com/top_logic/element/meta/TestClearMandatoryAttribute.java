/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta;

import junit.framework.Test;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.element.model.DynamicModelService;
import com.top_logic.element.model.ModelFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test that it is possible to temporarily setting a mandatory attribute to <code>null</code>.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestClearMandatoryAttribute extends TestPersistentModelPart {

	public void testClearAttribute() throws ConfigurationException {
		extendApplicationModel(TestClearMandatoryAttribute.class, "clear-attribute.model.xml");

		TLModule testModule = TLModelUtil.findModule("test.com.top_logic.element.meta.TestClearMandatoryAttribute");
		ModelFactory factory =
			DynamicModelService.getFactoryFor("test.com.top_logic.element.meta.TestClearMandatoryAttribute");

		TLObject src;
		TLObject dst;
		try (Transaction tx = _kb.beginTransaction()) {
			src = factory.createObject((TLClass) TLModelUtil.findType(testModule, "Src"));
			dst = factory.createObject((TLClass) TLModelUtil.findType(testModule, "Dst"));
			src.tUpdateByName("dest", dst);
			tx.commit();
		}

		assertTrue(dst.tValid());

		try (Transaction tx = _kb.beginTransaction()) {
			dst = (TLObject) src.tValueByName("dest");

			// Clear composite reference (prevent deletion of dest together with src).
			src.tUpdateByName("dest", null);

			// Delete the (now empty) container.
			src.tDelete();

			tx.commit();
		}

		assertTrue(dst.tValid());
	}

	public static Test suite() {
		return suite(TestClearMandatoryAttribute.class);
	}
}
