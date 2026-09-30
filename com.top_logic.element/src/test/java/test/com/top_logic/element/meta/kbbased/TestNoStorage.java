/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.meta.kbbased;

import junit.framework.Test;

import test.com.top_logic.element.model.util.TLModelTest;

import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.factory.TLFactory;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests for {@link com.top_logic.element.meta.kbbased.NoStorage}.
 *
 * <p>
 * Tests that abstract attributes are inaccessible on transient objects.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestNoStorage extends TLModelTest {

	@Override
	protected TLModel setUpModel() {
		return new TLModelImpl();
	}

	@Override
	protected TLFactory setUpFactory() {
		return TransientObjectFactory.INSTANCE;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		extendModel(ClassRelativeBinaryContent.withSuffix(TestNoStorage.class, "model.xml"));
	}

	@Override
	protected void tearDownModel() {
		// Nothing to do for transient model.
	}

	/**
	 * Tests that accessing an abstract attribute on a transient object throws a
	 * {@link TopLogicException}.
	 */
	public void testAbstractAttributeAccessOnTransientObject() {
		TLClass baseClass = type("testNoStorage:Base");
		TLStructuredTypePart abstractAttr = baseClass.getPart("abstractAttr");
		assertNotNull("Abstract attribute should exist", abstractAttr);
		assertTrue("Attribute should be abstract", abstractAttr.isAbstract());

		// Create a transient object of type Base (which has the abstract attribute)
		TLObject transientObject = getFactory().createObject(baseClass, null, null);
		assertNotNull("Transient object should be created", transientObject);

		// Try to read the abstract attribute - should throw TopLogicException
		try {
			transientObject.tValue(abstractAttr);
			fail("Reading abstract attribute should throw TopLogicException");
		} catch (TopLogicException ex) {
			// Expected - abstract attributes are inaccessible
			assertContains("testNoStorage:Base#abstractAttr", ex.getMessage());
		}

		// Try to write the abstract attribute - should also throw TopLogicException
		try {
			transientObject.tUpdate(abstractAttr, "some value");
			fail("Writing abstract attribute should throw TopLogicException");
		} catch (TopLogicException ex) {
			// Expected - abstract attributes are inaccessible
			assertContains("testNoStorage:Base#abstractAttr", ex.getMessage());
		}
	}

	/**
	 * Tests that a concrete (overridden) attribute can be accessed on a transient object of the
	 * subtype.
	 */
	public void testConcreteAttributeAccessOnTransientObject() {
		TLClass extClass = type("testNoStorage:Ext");
		TLStructuredTypePart concreteAttr = extClass.getPart("abstractAttr");
		assertNotNull("Concrete attribute should exist in subclass", concreteAttr);
		assertFalse("Attribute should not be abstract in subclass", concreteAttr.isAbstract());

		// Create a transient object of type Ext (which has the concrete override)
		TLObject transientObject = getFactory().createObject(extClass, null, null);
		assertNotNull("Transient object should be created", transientObject);

		// Should be able to read and write the concrete attribute without exception
		assertNull("Initial value should be null", transientObject.tValue(concreteAttr));

		transientObject.tUpdate(concreteAttr, "test value");
		assertEquals("Value should be updated", "test value", transientObject.tValue(concreteAttr));
	}

	/**
	 * Tests that accessing an Ext object through the Base class's abstract attribute definition
	 * still works because the concrete Ext type provides the override.
	 */
	public void testExtObjectAccessThroughBaseAttribute() {
		TLClass baseClass = type("testNoStorage:Base");
		TLClass extClass = type("testNoStorage:Ext");

		// Get the abstract attribute from the Base class
		TLStructuredTypePart baseAbstractAttr = baseClass.getPart("abstractAttr");
		assertNotNull("Abstract attribute should exist in base class", baseAbstractAttr);
		assertTrue("Base attribute should be abstract", baseAbstractAttr.isAbstract());

		// Create an Ext object (which has the concrete override)
		TLObject extObject = getFactory().createObject(extClass, null, null);
		assertNotNull("Ext object should be created", extObject);

		// Accessing through the Base class's abstract attribute should work for Ext objects
		// because Ext provides the concrete implementation
		assertNull("Initial value should be null", extObject.tValue(baseAbstractAttr));

		extObject.tUpdate(baseAbstractAttr, "value via base attr");
		assertEquals("Value should be updated", "value via base attr", extObject.tValue(baseAbstractAttr));
	}

	/**
	 * Test suite.
	 */
	public static Test suite() {
		return suiteTransient(TestNoStorage.class);
	}

}
