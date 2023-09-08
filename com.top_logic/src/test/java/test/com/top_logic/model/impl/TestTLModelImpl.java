/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.impl;

import static com.top_logic.model.TLPrimitive.Kind.*;
import static com.top_logic.model.util.TLModelUtil.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.xml.XMLStreamIndentationWriter;
import com.top_logic.basic.xml.XMLStreamUtil;
import com.top_logic.model.TLAssociation;
import com.top_logic.model.TLAssociationEnd;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLClassPart;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLPrimitive.Kind;
import com.top_logic.model.TLProperty;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.binding.xml.ModelReader;
import com.top_logic.model.binding.xml.ModelWriter;
import com.top_logic.model.builtin.TLCore;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.xref.TypeUsage;
import com.top_logic.util.model.CompatibilityService;

/**
 * Test case for {@link TLModelImpl}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestTLModelImpl extends BasicTestCase {

	protected TLModel model;
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		TLModelImpl tlModelImpl = new TLModelImpl();
		tlModelImpl.addCoreModule();
		this.model = tlModelImpl;
	}
	
	@Override
	protected void tearDown() throws Exception {
		this.model = null;
		
		super.tearDown();
	}
	
	protected void resolve() {
		((TLModelImpl) model).resolve(new AssertProtocol());
	}

	
	public void testExtends() throws FactoryConfigurationError {
		TLModule m1 = addModule(model, "m1");

		TLClass A = addClass(m1, "A");
		TLClass B = addClass(m1, "B");
		TLClass C = addClass(m1, "C");

		assertEquals(list(), A.getGeneralizations());
		assertEquals(set(), A.getSpecializations());
		assertEquals(list(), B.getGeneralizations());
		assertEquals(set(), B.getSpecializations());
		assertEquals(list(), C.getGeneralizations());
		assertEquals(set(), C.getSpecializations());

		A.getGeneralizations().add(B);

		assertEquals(list(B), A.getGeneralizations());
		assertEquals(set(), A.getSpecializations());
		assertEquals(list(), B.getGeneralizations());
		assertEquals(set(A), B.getSpecializations());
		assertEquals(list(), C.getGeneralizations());
		assertEquals(set(), C.getSpecializations());

		A.getGeneralizations().add(C);

		assertEquals(list(B, C), A.getGeneralizations());
		assertEquals(set(), A.getSpecializations());
		assertEquals(list(), B.getGeneralizations());
		assertEquals(set(A), B.getSpecializations());
		assertEquals(list(), C.getGeneralizations());
		assertEquals(set(A), C.getSpecializations());

		try {
			A.getGeneralizations().add(B);
			fail("Must not allow extending the same class twice.");
		} catch (IllegalStateException ex) {
			// Expected.
		}

		A.getGeneralizations().remove(A);

		assertEquals(list(B, C), A.getGeneralizations());
		assertEquals(set(), A.getSpecializations());
		assertEquals(list(), B.getGeneralizations());
		assertEquals(set(A), B.getSpecializations());
		assertEquals(list(), C.getGeneralizations());
		assertEquals(set(A), C.getSpecializations());

		A.getGeneralizations().remove(B);

		assertEquals(list(C), A.getGeneralizations());
		assertEquals(set(), A.getSpecializations());
		assertEquals(list(), B.getGeneralizations());
		assertEquals(set(), B.getSpecializations());
		assertEquals(list(), C.getGeneralizations());
		assertEquals(set(A), C.getSpecializations());

	}

	public void testEnum() throws FactoryConfigurationError {
		TLModule m1 = addModule(model, "enums");
		TLEnumeration c = addEnumeration(m1, "MyClassification");
		addClassifier(c, "C1");
		addClassifier(c, "C2");
		addClassifier(c, "C3");

		assertEquals(m1, c.getModule());
	}

	public void testCreation() throws XMLStreamException, FactoryConfigurationError, IOException {
		TLModule m1 = addModule(model, "m1");
		
		TLClass A = addClass(m1, "A");
		TLProperty a1 = addProperty(A, "a1", getPrimitive(STRING));
		TLProperty a2 = addProperty(A, "a2", getPrimitive(INT));
		
		TLClass B = addClass(m1, "B");
		TLProperty b1 = addProperty(B, "b1", getPrimitive(STRING));
		TLProperty b2 = addProperty(B, "b2", getPrimitive(INT));
		B.getGeneralizations().add(A);
		
		TLClass C = addClass(m1, "C");
		TLProperty c1 = addProperty(C, "c1", getPrimitive(STRING));
		TLProperty c2 = addProperty(C, "c2", getPrimitive(INT));
		C.getGeneralizations().add(B);
		
		TLClass D = addClass(m1, "D");
		TLProperty d1 = addProperty(D, "d1", getPrimitive(STRING));
		TLProperty d2 = addProperty(D, "d2", getPrimitive(INT));
		D.getGeneralizations().add(A);
		
		TLAssociation ab = addAssociation(m1, "ab");
		TLAssociationEnd abSource = addEnd(ab, "source", A);
		TLAssociationEnd abDestination = addEnd(ab, "destination", B);
		TLReference bs = addReference(A, "bs", abDestination);
		TLReference as = addReference(B, "as", abSource);
		
		TLAssociation dc = addReference(D, "cs", C, "ds");
		
		resolve();
		
		// Check D.
		assertTrue(TLModelUtil.getReflexiveTransitiveGeneralizations(D).contains(D));
		assertTrue(TLModelUtil.getReflexiveTransitiveGeneralizations(D).contains(A));
		assertFalse(TLModelUtil.getReflexiveTransitiveGeneralizations(D).contains(B));
		assertFalse(TLModelUtil.getReflexiveTransitiveGeneralizations(D).contains(C));
		
		assertTrue(TLModelUtil.getAllProperties(D).contains(d1));
		assertTrue(TLModelUtil.getAllProperties(D).contains(d2));
		assertTrue(TLModelUtil.getAllProperties(D).contains(a1));
		assertTrue(TLModelUtil.getAllProperties(D).contains(a2));
		assertTrue(TLModelUtil.getAllReferences(D).contains(bs));
		
		assertNotNull(D.getPart("cs"));
		assertNotNull(C.getPart("ds"));
		assertSame(dc, ((TLReference) D.getPart("cs")).getEnd().getOwner());
		assertSame(dc, ((TLReference) C.getPart("ds")).getEnd().getOwner());
		
		// Check C.
		assertTrue(TLModelUtil.getReflexiveTransitiveGeneralizations(C).contains(C));
		assertTrue(TLModelUtil.getReflexiveTransitiveGeneralizations(C).contains(B));
		assertTrue(TLModelUtil.getReflexiveTransitiveGeneralizations(C).contains(A));
		
		assertTrue(TLModelUtil.getAllProperties(C).contains(c1));
		assertTrue(TLModelUtil.getAllProperties(C).contains(c2));
		assertTrue(TLModelUtil.getAllProperties(C).contains(b1));
		assertTrue(TLModelUtil.getAllProperties(C).contains(b2));
		assertTrue(TLModelUtil.getAllProperties(C).contains(a1));
		assertTrue(TLModelUtil.getAllProperties(C).contains(a2));
		assertTrue(TLModelUtil.getAllReferences(C).contains(as));
		assertTrue(TLModelUtil.getAllReferences(C).contains(bs));
		
		// Check ab.
		assertEquals(as, abSource.getReference());
		assertEquals(bs, abDestination.getReference());
		
		// Check bc;
		TLReference cs = (TLReference) D.getPart("cs");
		TLReference ds = (TLReference) C.getPart("ds");
		assertNotNull(cs);
		assertNotNull(ds);
		assertEquals(dc, cs.getEnd().getOwner());
		assertEquals(dc, ds.getEnd().getOwner());
		
		File out1File = BasicTestCase.createTestFile("model-a", ".xml");
		XMLStreamIndentationWriter writer1 = new XMLStreamIndentationWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(new FileOutputStream(out1File)));
		ModelWriter.writeModel(writer1, model);
		writer1.close();
		assertTrue(out1File.length() > 0);
		
		TLModelImpl readModel = new TLModelImpl();
		new ModelReader(XMLStreamUtil.getDefaultInputFactory().createXMLStreamReader(new FileInputStream(out1File)),
			readModel);
		
		// TODO: Assert that original model equals read model.
		
		File out2File = BasicTestCase.createTestFile("model-b", ".xml");
		XMLStreamIndentationWriter writer2 = new XMLStreamIndentationWriter(XMLOutputFactory.newInstance().createXMLStreamWriter(new FileOutputStream(out2File)));
		ModelWriter.writeModel(writer2, model);
		writer2.close();
		
		BasicTestCase.assertEqualsTextStream(new FileInputStream(out2File), new FileInputStream(out1File));
	}
	
	/**
	 * Test case for {@link TypeUsage}.
	 */
	public void testTypeUsage() {
		TLModule m1 = addModule(model, "m1");
		TLModule m2 = addModule(model, "m2");
		
		TLClass A = addClass(m1, "A");
		TLProperty a1 = addProperty(A, "a1", getPrimitive(TLPrimitive.Kind.STRING));
		
		TLClass B = addClass(m1, "B");
		B.getGeneralizations().add(A);
		TLProperty b1 = addProperty(B, "b1", getPrimitive(TLPrimitive.Kind.DATE));
		
		TLClass C = addClass(m1, "C");
		C.getGeneralizations().add(A);
		
		addReference(B, "cs", C, "bs");
		
		TLAssociation ab = addAssociation(m2, "ab");
		TLAssociationEnd abSource = addEnd(ab, "source", A);
		TLAssociationEnd abDestination = addEnd(ab, "destination", B);
		TLProperty ab1 = addProperty(ab, "ab1", getPrimitive(TLPrimitive.Kind.INT));
		resolve();
		
		Map<TLType, Set<TLTypePart>> usage = TypeUsage.buildTypeUsage(model);
		
		assertTrue(usage.get(A).toString(), usage.get(A).contains(abSource));
		
		assertTrue(usage.get(B).toString(), usage.get(B).contains(abDestination));
		assertTrue(usage.get(B).toString(), usage.get(B).contains(C.getPart("bs")));
		assertTrue(usage.get(B).toString(), usage.get(B).contains(((TLReference) C.getPart("bs")).getEnd()));
		
		assertTrue(usage.get(C).toString(), usage.get(C).contains(B.getPart("cs")));
		assertTrue(usage.get(B).toString(), usage.get(C).contains(((TLReference) B.getPart("cs")).getEnd()));
		
		assertTrue(usage.get(getPrimitive(TLPrimitive.Kind.STRING)).contains(a1));
		assertTrue(usage.get(getPrimitive(TLPrimitive.Kind.DATE)).contains(b1));
		assertTrue(usage.get(getPrimitive(TLPrimitive.Kind.INT)).contains(ab1));
	}

	private TLPrimitive getPrimitive(Kind kind) {
		return TLCore.getPrimitiveType(model, kind);
	}

	/**
	 * Test case for stable order of {@link TLClass#getLocalParts()}.
	 */
	public void testStablePartOrder() {
		{
			TLModule module = addModule(model, "m2");
			TLClass clazz = addClass(module, "c1");
			TLProperty property = addProperty(clazz, "p1", getPrimitive(INT));
			TLReference ref = addReference(clazz, "ref1", addEnd(addAssociation(module, "ass"), "ass1", clazz));
			Iterator<TLClassPart> allParts = clazz.getLocalClassParts().iterator();
			assertEquals("Property was inserted before reference.", property, allParts.next());
			assertEquals("Property was inserted before reference.", ref, allParts.next());
		}
		{
			// test with same values but reverse insert order, as current implementation bases on
			// hashCode which may change.
			TLModule module = addModule(model, "m1");
			TLClass clazz = addClass(module, "c1");
			TLReference ref = addReference(clazz, "ref1", addEnd(addAssociation(module, "ass"), "ass1", clazz));
			TLProperty property = addProperty(clazz, "p1", getPrimitive(INT));
			Iterator<TLClassPart> allParts = clazz.getLocalClassParts().iterator();
			assertEquals("Reference was inserted before property.", ref, allParts.next());
			assertEquals("Reference was inserted before property.", property, allParts.next());
		}
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		Test t = new TestSuite(TestTLModelImpl.class);
		t = ServiceTestSetup.createSetup(t, CompatibilityService.Module.INSTANCE, AttributeSettings.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(t);
	}
	
}
