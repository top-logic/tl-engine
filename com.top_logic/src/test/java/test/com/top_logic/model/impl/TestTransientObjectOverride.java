/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.impl;

import junit.framework.Test;

import test.com.top_logic.knowledge.service.db2.AbstractDBKnowledgeBaseTest;

import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.builtin.TLCore;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.impl.TransientTLObjectImpl;
import com.top_logic.model.util.TLModelUtil;

/**
 * Test for a {@link TransientTLObjectImpl} of a type overriding an attribute: the attribute and its
 * override share one value, whichever of them it is written or read through.
 *
 * <p>
 * The interface <code>Base</code> defines the attribute <code>value</code>, which the class
 * <code>Derived</code> overrides. The attribute <code>other</code> of <code>Derived</code> has no
 * override.
 * </p>
 */
@SuppressWarnings("javadoc")
public class TestTransientObjectOverride extends AbstractDBKnowledgeBaseTest {

	private TLStructuredTypePart _baseValue;

	private TLStructuredTypePart _derivedValue;

	private TLStructuredTypePart _other;

	private TLClass _derived;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLPrimitive string = TLCore.getPrimitiveType(model, TLPrimitive.Kind.STRING);
		TLModule module = TLModelUtil.addModule(model, "test.override");

		TLClass base = TLModelUtil.addClass(module, "Base");
		base.setAbstract(true);
		TLProperty baseValue = TLModelUtil.addProperty(base, "value", string);

		_derived = TLModelUtil.addClass(module, "Derived");
		_derived.getGeneralizations().add(base);
		TLProperty derivedValue = TLModelUtil.addProperty(_derived, "value", string);
		_other = TLModelUtil.addProperty(_derived, "other", string);

		_baseValue = baseValue;
		_derivedValue = derivedValue;
		assertSame("The attribute of the class overrides the attribute of the interface.", _baseValue,
			_derivedValue.getDefinition());
	}

	@Override
	protected void tearDown() throws Exception {
		_baseValue = null;
		_derivedValue = null;
		_other = null;
		_derived = null;

		super.tearDown();
	}

	/**
	 * A value written through the override is read through the overridden attribute.
	 */
	public void testWriteOverrideReadDefinition() {
		TLObject object = newTransient();

		object.tUpdate(_derivedValue, "v1");

		assertEquals("v1", object.tValue(_baseValue));
		assertEquals("v1", object.tValue(_derivedValue));
	}

	/**
	 * A value written through the overridden attribute is read through the override.
	 */
	public void testWriteDefinitionReadOverride() {
		TLObject object = newTransient();

		object.tUpdate(_baseValue, "v1");

		assertEquals("v1", object.tValue(_derivedValue));
		assertEquals("v1", object.tValue(_baseValue));
	}

	/**
	 * The last write wins, whichever attribute it went through.
	 */
	public void testAlternatingWrites() {
		TLObject object = newTransient();

		object.tUpdate(_derivedValue, "v1");
		object.tUpdate(_baseValue, "v2");
		assertEquals("v2", object.tValue(_derivedValue));

		object.tUpdate(_derivedValue, "v3");
		assertEquals("v3", object.tValue(_baseValue));
	}

	/**
	 * An attribute without override keeps its own value.
	 */
	public void testAttributeWithoutOverride() {
		TLObject object = newTransient();

		object.tUpdate(_derivedValue, "v1");
		object.tUpdate(_other, "o1");

		assertEquals("o1", object.tValue(_other));
		assertEquals("v1", object.tValue(_baseValue));
	}

	private TLObject newTransient() {
		return TransientObjectFactory.INSTANCE.createObject(_derived, null);
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return suite(TestTransientObjectOverride.class);
	}

}
