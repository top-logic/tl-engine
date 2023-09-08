/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.wrap;

import static test.com.top_logic.knowledge.service.db2.KnowledgeBaseTestScenarioImpl.*;

import java.util.Arrays;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.knowledge.dummy.DummyKnowledgeObject;

import com.top_logic.dob.DataObjectException;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.attr.MOAttributeImpl;
import com.top_logic.dob.attr.MOPrimitive;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.meta.DefaultTypeSystem;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.dob.meta.MOClassImpl;
import com.top_logic.dob.meta.TypeContext;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.wrap.AbstractWrapper;
import com.top_logic.knowledge.wrap.ImplementationFactory;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.knowledge.wrap.binding.PolymorphicBinding.Config;
import com.top_logic.model.TLObject;

/**
 * Test case for {@link ImplementationFactory}
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestImplementationFactory extends TestCase {

	/**
	 * The root type.
	 */
	private MOClassImpl _classObject;

	/**
	 * Extends {@link #_classObject}.
	 */
	private MOClassImpl _classA;

	/**
	 * extends {@link #_classA}.
	 */
	private MOClassImpl _classB;

	/**
	 * Extends {@link #_classObject}.
	 */
	private MOClassImpl _classC;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_classObject = new MOClassImpl("Object");

		_classA = new MOClassImpl("A");
		_classA.setSuperclass(_classObject);
		setApplicationType(_classA, TestImplementationFactory.TestWrapperAImpl.class);

		_classB = new MOClassImpl("B");
		_classB.setSuperclass(_classA);
		setApplicationType(_classB, TestImplementationFactory.TestWrapperSubAImpl.class);

		_classC = new MOClassImpl("C");
		_classC.setSuperclass(_classObject);
		_classC.addAttribute(new MOAttributeImpl("dynamicType", MOPrimitive.STRING));

		Config polyBinding = polyBinding("dynamicType");
		addBinding(polyBinding, "C1", TestImplementationFactory.TestWrapperAImpl.class);
		addBinding(polyBinding, "C2", TestImplementationFactory.TestWrapperBImpl.class);
		setBinding(_classC, polyBinding);

		_classObject.freeze();
		_classA.freeze();
		_classB.freeze();
		_classC.freeze();
	}

	@Override
	protected void tearDown() throws Exception {
		_classObject = null;
		_classA = null;
		_classB = null;
		_classC = null;

		super.tearDown();
	}

	/**
	 * Test the correct handling of wrapper registration.
	 */
	public void testWrapperRegistration() throws Exception {
		ImplementationFactory theFactory = createImplementationFactory();

		// check the registration
		assertEquals(
			"Wrong wrapper impl for " + _classA,
			TestImplementationFactory.TestWrapperAImpl.class,
			TestImplementationFactory.getImplementationClassForType(theFactory, _classA));
		assertEquals(
			"Wrong wrapper impl for " + _classB,
			TestImplementationFactory.TestWrapperSubAImpl.class,
			TestImplementationFactory.getImplementationClassForType(theFactory, _classB));
	}

	public void testResolver() throws DataObjectException {
		ImplementationFactory factory = createImplementationFactory();
		KnowledgeItem c1 = DummyKnowledgeObject.item("foo", _classC);
		c1.setAttributeValue("dynamicType", "C1");
		KnowledgeItem c2 = DummyKnowledgeObject.item("bar", _classC);
		c2.setAttributeValue("dynamicType", "C2");
		assertEquals(TestImplementationFactory.TestWrapperAImpl.class, factory.createBinding(c1).getClass());
		assertEquals(TestImplementationFactory.TestWrapperBImpl.class, factory.createBinding(c2).getClass());
	}

	private ImplementationFactory createImplementationFactory() {
		ImplementationFactory factory = new ImplementationFactory();
		Iterable<? extends MetaObject> types = Arrays.asList(_classObject, _classA, _classB, _classC);
		TypeContext typeContext = new DefaultTypeSystem(types);
		factory.init(typeContext);
		return factory;
	}
	
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestImplementationFactory.class);
	}

	private static Class<? extends TLObject> getImplementationClassForType(ImplementationFactory factory,
			MOClass type) throws UnknownTypeException {

		return factory.createBinding(DummyKnowledgeObject.item("foobar", type)).getClass();
	}

	public static class TestWrapperAImpl extends AbstractWrapper {
	    public TestWrapperAImpl(KnowledgeObject ko) {
	        super(ko);
	    }
	    public static Wrapper getInstance(KnowledgeObject aKO) {
	        return WrapperFactory.getWrapper(aKO);
	    }
	}

	public static class TestWrapperSubAImpl extends TestWrapperAImpl {
	    public TestWrapperSubAImpl(KnowledgeObject ko) {
	        super(ko);
	    }
	    public static Wrapper getInstance(KnowledgeObject aKO) {
	            return WrapperFactory.getWrapper(aKO);
	    }
	}

	public static class TestWrapperBImpl extends AbstractWrapper {
	    public TestWrapperBImpl(KnowledgeObject ko) {
	        super(ko);
	    }
	    public static Wrapper getInstance(KnowledgeObject aKO) {
	            return WrapperFactory.getWrapper(aKO);
	    }
	}
}
