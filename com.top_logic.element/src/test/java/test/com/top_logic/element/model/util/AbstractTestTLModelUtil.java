/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.util;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static com.top_logic.model.util.TLModelUtil.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import junit.framework.Test;

import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link Test}s for {@link TLModelUtil}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public abstract class AbstractTestTLModelUtil extends TLModelTest {

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		/* For now, the same TLModel file is used for all tests. */
		extendModel(getModelXml());
	}

	private ClassRelativeBinaryContent getModelXml() {
		return ClassRelativeBinaryContent.withSuffix(AbstractTestTLModelUtil.class, "model.xml");
	}

	public void testCommonGeneralizations() {
		assertEquals(set(), TLModelUtil.getCommonGeneralizations(list(type("test1:A"), type("test1:X"))));

		assertEquals(set(type("test1:A")), TLModelUtil.getCommonGeneralizations(list(type("test1:A"))));
		assertEquals(set(type("test1:B")), TLModelUtil.getCommonGeneralizations(list(type("test1:B"))));
		assertEquals(set(type("test1:C")), TLModelUtil.getCommonGeneralizations(list(type("test1:C"))));
		assertEquals(set(type("test1:F")), TLModelUtil.getCommonGeneralizations(list(type("test1:F"))));
		assertEquals(set(type("test1:H")), TLModelUtil.getCommonGeneralizations(list(type("test1:H"))));

		assertEquals(set(type("test1:B")), TLModelUtil.getCommonGeneralizations(list(type("test1:B"), type("test1:C"))));
		assertEquals(set(type("test1:B")), TLModelUtil.getCommonGeneralizations(list(type("test1:C"), type("test1:D"))));
		assertEquals(set(type("test1:D")), TLModelUtil.getCommonGeneralizations(list(type("test1:F"), type("test1:G"))));

		assertEquals(set(type("test1:B")),
			TLModelUtil.getCommonGeneralizations(list(type("test1:C"), type("test1:G"), type("test1:H"))));
	}

	public void testGetClassParts_A() {
		List<?> actualParts = type("testGetClassParts:A").getAllClassParts();
		List<?> expectedParts = list();
		assertEquals(expectedParts, actualParts);
	}

	public void testGetClassParts_B() {
		List<?> actualParts = type("testGetClassParts:B").getAllClassParts();
		List<?> expectedParts = list(part("testGetClassParts:B#bottomProperty"));
		assertEquals(expectedParts, actualParts);
	}

	public void testGetClassParts_C() {
		List<?> actualParts = type("testGetClassParts:C").getAllClassParts();
		List<?> expectedParts = list(part("testGetClassParts:B#bottomProperty"));
		assertEquals(expectedParts, actualParts);
	}

	public void testGetAllParts() {
		List<?> actual = type("ComplexTypeHierarchy:C").getAllClassParts();
		List<?> expected = list(
			part("ComplexTypeHierarchy:C#overriddenProperty"),
			part("ComplexTypeHierarchy:C#overriddenReference"));
		assertEquals(expected, actual);
	}

	public void testGetAllProperties() {
		List<?> actual = getAllProperties(type("ComplexTypeHierarchy:C"));
		List<?> expected = list(part("ComplexTypeHierarchy:C#overriddenProperty"));
		assertEquals(expected, actual);
	}

	public void testGetAllReferences() {
		List<?> actual = getAllReferences(type("ComplexTypeHierarchy:C"));
		List<?> expected = list(part("ComplexTypeHierarchy:C#overriddenReference"));
		assertEquals(expected, actual);
	}

	public void testGetDefinition_B() {
		TLTypePart part = part("ComplexTypeHierarchy:B#overriddenReference");
		TLTypePart actual = part.getDefinition();
		TLTypePart expected = part("ComplexTypeHierarchy:B#overriddenReference");
		assertEquals(expected, actual);
	}

	public void testGetDefinition_C() {
		TLTypePart part = part("ComplexTypeHierarchy:C#overriddenReference");
		TLTypePart actual = part.getDefinition();
		TLTypePart expected = part("ComplexTypeHierarchy:B#overriddenReference");
		assertEquals(expected, actual);
	}

	public void testGetDefinition_D() {
		TLTypePart part = part("ComplexTypeHierarchy:D#overriddenReference");
		TLTypePart actual = part.getDefinition();
		TLTypePart expected = part("ComplexTypeHierarchy:B#overriddenReference");
		assertEquals(expected, actual);
	}

	public void testGetDefinition_E() {
		TLTypePart part = part("ComplexTypeHierarchy:E#overriddenReference");
		TLTypePart actual = part.getDefinition();
		TLTypePart expected = part("ComplexTypeHierarchy:B#overriddenReference");
		assertEquals(expected, actual);
	}

	public void testGetDefinition_F() {
		TLTypePart part = part("ComplexTypeHierarchy:F#overriddenReference");
		TLTypePart actual = part.getDefinition();
		TLTypePart expected = part("ComplexTypeHierarchy:B#overriddenReference");
		assertEquals(expected, actual);
	}

	public void testGetDefinition_G() {
		TLTypePart part = part("ComplexTypeHierarchy:G#overriddenReference");
		TLTypePart actual = part.getDefinition();
		TLTypePart expected = part("ComplexTypeHierarchy:B#overriddenReference");
		assertEquals(expected, actual);
	}

	public void testGetSuperClasses_A() {
		Set<?> actual = TLModelUtil.getReflexiveTransitiveGeneralizations(type("ComplexTypeHierarchy:A"));
		Set<?> expected = set(
			type("ComplexTypeHierarchy:A"));
		assertEquals(expected, actual);
	}

	public void testGetTransitiveGeneralizations_A() {
		LinkedHashSet<?> actual = getTransitiveGeneralizations(type("ComplexTypeHierarchy:A"));
		LinkedHashSet<?> expected = linkedSet();
		assertEquals(expected, actual);
	}

	public void testGetSuperClasses_B() {
		Set<?> actual = TLModelUtil.getReflexiveTransitiveGeneralizations(type("ComplexTypeHierarchy:B"));
		Set<?> expected = set(
			type("ComplexTypeHierarchy:B"),
			type("ComplexTypeHierarchy:A"));
		assertEquals(expected, actual);
	}

	public void testGetTransitiveGeneralizations_B() {
		LinkedHashSet<?> actual = getTransitiveGeneralizations(type("ComplexTypeHierarchy:B"));
		LinkedHashSet<?> expected = linkedSet(
			type("ComplexTypeHierarchy:A"));
		assertEquals(expected, actual);
	}

	public void testGetSuperClasses_C() {
		Set<?> actual = TLModelUtil.getReflexiveTransitiveGeneralizations(type("ComplexTypeHierarchy:C"));
		Set<?> expected = set(
			type("ComplexTypeHierarchy:C"),
			type("ComplexTypeHierarchy:B"),
			type("ComplexTypeHierarchy:A"));
		assertEquals(expected, actual);
	}

	public void testGetTransitiveGeneralizations_C() {
		LinkedHashSet<?> actual = getTransitiveGeneralizations(type("ComplexTypeHierarchy:C"));
		LinkedHashSet<?> expected = linkedSet(
			type("ComplexTypeHierarchy:B"),
			type("ComplexTypeHierarchy:A"));
		assertEquals(expected, actual);
	}

	public void testGetSuperClasses_D() {
		Set<?> actual = TLModelUtil.getReflexiveTransitiveGeneralizations(type("ComplexTypeHierarchy:D"));
		Set<?> expected = set(
			type("ComplexTypeHierarchy:D"),
			type("ComplexTypeHierarchy:A"),
			type("ComplexTypeHierarchy:B"));
		assertEquals(expected, actual);
	}

	public void testGetTransitiveGeneralizations_D() {
		LinkedHashSet<?> actual = getTransitiveGeneralizations(type("ComplexTypeHierarchy:D"));
		LinkedHashSet<?> expected = linkedSet(
			type("ComplexTypeHierarchy:A"),
			type("ComplexTypeHierarchy:B"));
		assertEquals(expected, actual);
	}

	public void testGetSuperClasses_E() {
		Set<?> actual = TLModelUtil.getReflexiveTransitiveGeneralizations(type("ComplexTypeHierarchy:E"));
		Set<?> expected = set(
			type("ComplexTypeHierarchy:E"),
			type("ComplexTypeHierarchy:B"),
			type("ComplexTypeHierarchy:A"));
		assertEquals(expected, actual);
	}

	public void testGetTransitiveGeneralizations_E() {
		LinkedHashSet<?> actual = getTransitiveGeneralizations(type("ComplexTypeHierarchy:E"));
		LinkedHashSet<?> expected = linkedSet(
			type("ComplexTypeHierarchy:B"),
			type("ComplexTypeHierarchy:A"));
		assertEquals(expected, actual);
	}

	public void testGetSuperClasses_F() {
		Set<?> actual = TLModelUtil.getReflexiveTransitiveGeneralizations(type("ComplexTypeHierarchy:F"));
		Set<?> expected = set(
			type("ComplexTypeHierarchy:F"),
			type("ComplexTypeHierarchy:D"),
			type("ComplexTypeHierarchy:A"),
			type("ComplexTypeHierarchy:B"),
			type("ComplexTypeHierarchy:E"));
		assertEquals(expected, actual);
	}

	public void testGetTransitiveGeneralizations_F() {
		LinkedHashSet<?> actual = getTransitiveGeneralizations(type("ComplexTypeHierarchy:F"));
		LinkedHashSet<?> expected = linkedSet(
			type("ComplexTypeHierarchy:D"),
			type("ComplexTypeHierarchy:A"),
			type("ComplexTypeHierarchy:B"),
			type("ComplexTypeHierarchy:E"));
		assertEquals(expected, actual);
	}

	public void testGetSuperClasses_G() {
		Set<?> actual = TLModelUtil.getReflexiveTransitiveGeneralizations(type("ComplexTypeHierarchy:G"));
		Set<?> expected = set(
			type("ComplexTypeHierarchy:G"),
			type("ComplexTypeHierarchy:E"),
			type("ComplexTypeHierarchy:B"),
			type("ComplexTypeHierarchy:A"),
			type("ComplexTypeHierarchy:D"));
		assertEquals(expected, actual);
	}

	public void testGetTransitiveGeneralizations_G() {
		LinkedHashSet<?> actual = getTransitiveGeneralizations(type("ComplexTypeHierarchy:G"));
		LinkedHashSet<?> expected = linkedSet(
			type("ComplexTypeHierarchy:E"),
			type("ComplexTypeHierarchy:B"),
			type("ComplexTypeHierarchy:A"),
			type("ComplexTypeHierarchy:D"));
		assertEquals(expected, actual);
	}

	public void testEnums() {
		TLModule module = module("enums");
		TLEnumeration type = enumeration("enums:Classification");
		assertNotNull(type);
		assertEquals(3, type.getClassifiers().size());
		assertEquals(3, type.getClassifiers().size());
		assertEquals(module, type.getModule());
		List<?> expected = list(
			resolve("enums:Classification#C1"),
			resolve("enums:Classification#C2"),
			resolve("enums:Classification#C3"));
		assertEquals(expected, type.getClassifiers());
	}

	public void testEnumClassifierAsPart() {
		TLModule module = module("enums");
		TLEnumeration type = enumeration("enums:Classification");

		assertEquals(type.getClassifiers().get(0), part("enums:Classification#C1"));
	}

	public void testIsCompatibleType() {
		assertTrue("Class is assignment compatible to itself.",
			TLModelUtil.isCompatibleType(type("ComplexTypeHierarchy:A"), type("ComplexTypeHierarchy:A")));
		assertTrue("Class is assignment compatible to direct supertype.",
			TLModelUtil.isCompatibleType(type("ComplexTypeHierarchy:A"), type("ComplexTypeHierarchy:B")));
		assertTrue("Class is assignment compatible to supertype of supertype.",
			TLModelUtil.isCompatibleType(type("ComplexTypeHierarchy:A"), type("ComplexTypeHierarchy:C")));
		assertFalse("Class is not compatible to subtype.",
			TLModelUtil.isCompatibleType(type("ComplexTypeHierarchy:C"), type("ComplexTypeHierarchy:A")));
	}

}
