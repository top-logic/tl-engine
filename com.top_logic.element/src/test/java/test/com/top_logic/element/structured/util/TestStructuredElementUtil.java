/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.structured.util;

import static com.top_logic.element.structured.util.StructuredElementUtil.*;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.element.structured.model.ANode;
import test.com.top_logic.element.structured.model.TestTypesFactory;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.element.structured.StructuredElement;
import com.top_logic.element.structured.util.StructuredElementUtil;
import com.top_logic.knowledge.service.KBUtils;
import com.top_logic.model.TLClass;

/**
 * {@link Test} for {@link StructuredElementUtil}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public class TestStructuredElementUtil extends BasicTestCase {

	public void testGetParentsNull() {
		try {
			getParents(null);
			fail("getParent(null) did not throw an Exception.");
		} catch (NullPointerException exception) {
			// This is the wanted behavior.
			return;
		}
	}

	public void testGetParentsRoot() {
		assertEquals(list(), getParents(getRoot()));
	}

	public void testGetParentsChild() {
		KBUtils.inTransaction(() -> {
			StructuredElement child = createChild(getRoot(), "testGetParentsChild");
			assertEquals(list(getRoot()), getParents(child));
			/* No need to commit: The test data is only needed during the test. */
		});
	}

	public void testGetParentsGrandChild() {
		KBUtils.inTransaction(() -> {
			StructuredElement child = createChild(getRoot(), "testGetParentsGrandChild_1");
			StructuredElement grandChild = createChild(child, "testGetParentsGrandChild_2");
			assertEquals(list(child, getRoot()), getParents(grandChild));
			/* Dito. */
		});
	}

	private StructuredElement createChild(StructuredElement parent, String name) {
		return parent.createChild(getElementNamePrefix() + name, getChildrenType());
	}

	private String getElementNamePrefix() {
		return getClass().getSimpleName();
	}

	private TLClass getChildrenType() {
		return TestTypesFactory.getANodeType();
	}

	private ANode getRoot() {
		return TestTypesFactory.getInstance().getRootSingleton();
	}

	public static Test suite() {
		return KBSetup.getSingleKBTest(TestStructuredElementUtil.class);
	}

}
