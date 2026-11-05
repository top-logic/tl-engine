/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.knowledge.service.db2.migration.processor;

import java.io.File;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.top_logic.basic.core.workspace.ModuleLayoutConstants;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.XsltUtil;
import com.top_logic.knowledge.service.db2.migration.processor.XsltTableDefinitionBaselineProcessor;

/**
 * Base class for tests of {@link XsltTableDefinitionBaselineProcessor} transformations that set
 * the <code>key-attributes</code> annotation of a type, or add an attribute to a type, in the
 * stored schema baseline.
 *
 * <p>
 * The transformation is loaded from the migration directory of the module under test and applied
 * to a small baseline containing the migrated type and an unrelated type. As in the stored
 * baseline, the attributes of a type are direct children of the type element (followed by its
 * <code>index</code>), not wrapped in an <code>attributes</code> element.
 * </p>
 *
 * <p>
 * The checks make sure that the migrated type ends up with exactly one <code>annotations</code>
 * element (its first child) holding exactly one <code>key-attributes</code> entry with the new
 * value, that its other annotations, attributes and index are kept, that an added attribute
 * occurs exactly once after the existing attributes, and that the unrelated type is not touched,
 * regardless of whether the baseline already contained the <code>key-attributes</code> entry or
 * the added attribute.
 * </p>
 */
public abstract class AbstractBaselineTransformTest extends TestCase {

	private static final String CONFIG_NS = "http://www.top-logic.com/ns/config/6.0";

	private static final String MIGRATION_DIR = "WEB-INF/kbase/migration";

	private static final String META_OBJECTS = "meta-objects";

	private static final String METAOBJECT = "metaobject";

	private static final String OBJECT_NAME = "object_name";

	private static final String ANNOTATIONS = "annotations";

	private static final String ANNOTATION = "annotation";

	private static final String INTERFACE = "interface";

	private static final String KEY_ATTRIBUTES = "key-attributes";

	private static final String ATTRIBUTES = "attributes";

	private static final String MO_ATTRIBUTE = "mo_attribute";

	private static final String ATT_NAME = "att_name";

	private static final String INDEX = "index";

	private static final String MO_INDEX = "mo_index";

	private static final String NAME = "name";

	private static final String OTHER_ANNOTATION_INTERFACE =
		"com.top_logic.knowledge.service.xml.annotation.FullLoadAnnotation";

	private static final String STALE_KEY_ATTRIBUTES = "stale";

	private static final String UNRELATED_TYPE = "UnrelatedTestType";

	private static final String ATTRIBUTE_NAME = "testAttribute";

	private static final String INDEX_NAME = "testIndex";

	/**
	 * Checks that the given transformation replaces an existing <code>key-attributes</code> entry
	 * of the given type.
	 *
	 * @param module
	 *        The name of the migration module, the directory of the transformation below
	 *        <code>WEB-INF/kbase/migration</code>.
	 * @param transform
	 *        The file name of the transformation.
	 * @param typeTag
	 *        The tag of the migrated type (<code>metaobject</code> or <code>association</code>).
	 * @param typeName
	 *        The name of the migrated type.
	 * @param expectedKeyAttributes
	 *        The key attributes the transformation sets.
	 */
	protected void assertReplacesKeyAttributes(String module, String transform, String typeTag, String typeName,
			String expectedKeyAttributes) throws Exception {
		assertTransform(module, transform, typeTag, typeName, true, true, null, expectedKeyAttributes, null);
	}

	/**
	 * Checks that the given transformation adds a <code>key-attributes</code> entry to the given
	 * type in a baseline without one.
	 *
	 * @param withAnnotations
	 *        Whether the type has an <code>annotations</code> element (with another annotation) in
	 *        the original baseline.
	 *
	 * @see #assertReplacesKeyAttributes(String, String, String, String, String)
	 */
	protected void assertAddsKeyAttributes(String module, String transform, String typeTag, String typeName,
			String expectedKeyAttributes, boolean withAnnotations) throws Exception {
		assertTransform(module, transform, typeTag, typeName, withAnnotations, false, null, expectedKeyAttributes,
			null);
	}

	/**
	 * Checks the result of applying the given transformation to a baseline with the given content.
	 *
	 * @param withAnnotations
	 *        Whether the type has an <code>annotations</code> element (with another annotation) in
	 *        the original baseline.
	 * @param withKeyAttributes
	 *        Whether the <code>annotations</code> of the type already contain a
	 *        <code>key-attributes</code> entry in the original baseline.
	 * @param presentAttribute
	 *        Name of an attribute that the type already has in the original baseline in addition
	 *        to its test attribute, <code>null</code> for none.
	 * @param expectedAddedAttribute
	 *        Name of the attribute the transformation adds to the type, <code>null</code> if the
	 *        transformation does not change the attributes of the type.
	 *
	 * @see #assertReplacesKeyAttributes(String, String, String, String, String)
	 */
	protected void assertTransform(String module, String transform, String typeTag, String typeName,
			boolean withAnnotations, boolean withKeyAttributes, String presentAttribute,
			String expectedKeyAttributes, String expectedAddedAttribute) throws Exception {
		StringBuilder baseline = new StringBuilder();
		baseline.append("<config xmlns:config=\"" + CONFIG_NS + "\">\n");
		baseline.append("\t<" + META_OBJECTS + ">\n");
		appendType(baseline, typeTag, typeName, withAnnotations, withKeyAttributes, presentAttribute);
		appendType(baseline, METAOBJECT, UNRELATED_TYPE, true, true, null);
		baseline.append("\t</" + META_OBJECTS + ">\n");
		baseline.append("</config>\n");

		Document result = transform(module, transform, baseline.toString());
		Element metaObjects = singleChild(result.getDocumentElement(), META_OBJECTS);

		List<String> expectedContents = new ArrayList<>();
		expectedContents.add(ANNOTATIONS);
		expectedContents.add(MO_ATTRIBUTE + ":" + ATTRIBUTE_NAME);
		if (presentAttribute != null && !presentAttribute.equals(expectedAddedAttribute)) {
			expectedContents.add(MO_ATTRIBUTE + ":" + presentAttribute);
		}
		if (expectedAddedAttribute != null) {
			expectedContents.add(MO_ATTRIBUTE + ":" + expectedAddedAttribute);
		}
		expectedContents.add(INDEX);

		Element type = type(metaObjects, typeTag, typeName);
		List<Element> typeContents = elements(type);
		List<String> contents = new ArrayList<>();
		for (Element content : typeContents) {
			String tag = content.getTagName();
			contents.add(tag.equals(MO_ATTRIBUTE) ? tag + ":" + content.getAttribute(ATT_NAME) : tag);
		}
		assertEquals("Children of '" + typeName + "'.", expectedContents, contents);

		Element index = typeContents.get(typeContents.size() - 1);
		assertEquals(INDEX_NAME, singleChild(index, MO_INDEX).getAttribute(NAME));

		List<Element> annotationList = elements(typeContents.get(0));
		assertEquals("Annotations of '" + typeName + "'.", withAnnotations ? 2 : 1, annotationList.size());
		if (withAnnotations) {
			Element other = annotationList.get(0);
			assertEquals(ANNOTATION, other.getTagName());
			assertEquals(OTHER_ANNOTATION_INTERFACE, other.getAttributeNS(CONFIG_NS, INTERFACE));
		}
		Element keyAttributes = annotationList.get(annotationList.size() - 1);
		assertEquals(KEY_ATTRIBUTES, keyAttributes.getTagName());
		assertEquals(expectedKeyAttributes, keyAttributes.getAttribute(ATTRIBUTES));

		Element unrelated = type(metaObjects, METAOBJECT, UNRELATED_TYPE);
		List<Element> unrelatedContents = elements(unrelated);
		assertEquals(3, unrelatedContents.size());
		List<Element> unrelatedAnnotations = elements(unrelatedContents.get(0));
		assertEquals(2, unrelatedAnnotations.size());
		assertEquals(STALE_KEY_ATTRIBUTES, unrelatedAnnotations.get(1).getAttribute(ATTRIBUTES));
	}

	private static void appendType(StringBuilder out, String typeTag, String typeName, boolean withAnnotations,
			boolean withKeyAttributes, String presentAttribute) {
		out.append("\t\t<" + typeTag + " " + OBJECT_NAME + "=\"" + typeName + "\">\n");
		if (withAnnotations) {
			out.append("\t\t\t<" + ANNOTATIONS + ">\n");
			out.append("\t\t\t\t<" + ANNOTATION + " config:" + INTERFACE + "=\"" + OTHER_ANNOTATION_INTERFACE
				+ "\" full-load=\"true\"/>\n");
			if (withKeyAttributes) {
				out.append("\t\t\t\t<" + KEY_ATTRIBUTES + " " + ATTRIBUTES + "=\"" + STALE_KEY_ATTRIBUTES + "\"/>\n");
			}
			out.append("\t\t\t</" + ANNOTATIONS + ">\n");
		}
		appendAttribute(out, ATTRIBUTE_NAME);
		if (presentAttribute != null) {
			appendAttribute(out, presentAttribute);
		}
		out.append("\t\t\t<" + INDEX + ">\n");
		out.append("\t\t\t\t<" + MO_INDEX + " " + NAME + "=\"" + INDEX_NAME + "\"/>\n");
		out.append("\t\t\t</" + INDEX + ">\n");
		out.append("\t\t</" + typeTag + ">\n");
	}

	private static void appendAttribute(StringBuilder out, String name) {
		out.append("\t\t\t<" + MO_ATTRIBUTE + " " + ATT_NAME + "=\"" + name + "\" att_type=\"String\"/>\n");
	}

	private static Document transform(String module, String transform, String baseline) throws Exception {
		File file = new File(ModuleLayoutConstants.WEBAPP_DIR, MIGRATION_DIR + "/" + module + "/" + transform);
		assertTrue("Transformation not found: " + file.getAbsolutePath(), file.isFile());

		Transformer transformer = XsltUtil.createTransformer(new StreamSource(file), false);
		Document result = DOMUtil.newDocument();
		transformer.transform(new StreamSource(new StringReader(baseline)), new DOMResult(result));
		return result;
	}

	private static Element type(Element metaObjects, String typeTag, String typeName) {
		Element found = null;
		for (Element type : elements(metaObjects)) {
			if (type.getTagName().equals(typeTag) && type.getAttribute(OBJECT_NAME).equals(typeName)) {
				assertNull("Multiple types '" + typeName + "'.", found);
				found = type;
			}
		}
		assertNotNull("Type '" + typeName + "' not found.", found);
		return found;
	}

	private static Element singleChild(Element parent, String tag) {
		Element found = null;
		for (Element child : elements(parent)) {
			if (child.getTagName().equals(tag)) {
				assertNull("Multiple '" + tag + "' in '" + parent.getTagName() + "'.", found);
				found = child;
			}
		}
		assertNotNull("No '" + tag + "' in '" + parent.getTagName() + "'.", found);
		return found;
	}

	private static List<Element> elements(Element parent) {
		List<Element> result = new ArrayList<>();
		for (Node child = parent.getFirstChild(); child != null; child = child.getNextSibling()) {
			if (child instanceof Element) {
				result.add((Element) child);
			}
		}
		return result;
	}

}
