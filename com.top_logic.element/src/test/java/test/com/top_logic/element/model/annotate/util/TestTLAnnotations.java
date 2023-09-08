/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.element.model.annotate.util;

import test.com.top_logic.basic.ExpectedFailure;
import test.com.top_logic.element.model.util.TLModelTest;

import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.model.TLClass;
import com.top_logic.model.annotate.AnnotationInheritance;
import com.top_logic.model.annotate.AnnotationInheritance.Policy;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.config.annotation.TableName;

/**
 * Test for {@link TLAnnotations}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public abstract class TestTLAnnotations extends TLModelTest {

	@AnnotationInheritance(Policy.FINAL)
	@TargetType(value = TLTypeKind.REF, name = "testCanNotOverride:X")
	public interface PreventOverrideAnnotation extends TLAttributeAnnotation {
		// Marker annotation
	}

	public void testTable() {
		TLClass clazz = addClass("module", "class");
		assertEquals(TLAnnotations.GENERIC_TABLE_NAME, TLAnnotations.getTable(clazz));

		TLAnnotations.setTable(clazz, "tableName");
		assertEquals("tableName", TLAnnotations.getTable(clazz));

		// Set same table name again.
		TLAnnotations.setTable(clazz, "tableName");
		assertEquals("tableName", TLAnnotations.getTable(clazz));

		TLAnnotations.setTable(clazz, "newTableName");
		assertEquals("newTableName", TLAnnotations.getTable(clazz));

		TLAnnotations.setTable(clazz, null);
		assertEquals("Setting table to null resets to default table.", TLAnnotations.GENERIC_TABLE_NAME,
			TLAnnotations.getTable(clazz));
	}

	public void testTableAnnotation() {
		TableName annotation = TLAnnotations.newTableAnnotation("tableName");
		assertEquals("tableName", annotation.getName());
	}

	public void testTargetType() {
		try {
			extendModelExpectFailure(
				ClassRelativeBinaryContent.withSuffix(TestTLAnnotations.class, "invalidTargetType1.model.xml"));
			fail("Invalid model definition.");
		} catch (ExpectedFailure ex) {
			Throwable e = ex;
			while (true) {
				if (e.getMessage()
					.contains("test1:X#invalidTypeKind is not allowed for kind 'REF' of target type test1:X")) {
					break;
				}
				e = e.getCause();
				if (e == null) {
					throw ex;
				}
			}
		}
		try {
			extendModelExpectFailure(
				ClassRelativeBinaryContent.withSuffix(TestTLAnnotations.class, "invalidTargetType2.model.xml"));
			fail("Invalid model definition.");
		} catch (ExpectedFailure ex) {
			Throwable e = ex;
			while (true) {
				if (e.getMessage()
					.contains(
						"test1:X#invalidSubType is not allowed for target type test1:X. Only subtypes of [tl.folder:WebFolder] allowed")) {
					break;
				}
				e = e.getCause();
				if (e == null) {
					throw ex;
				}
			}
		}
	}

	public void testCanNotOverrideAnnotation() {
		try {
			extendModelExpectFailure(
				ClassRelativeBinaryContent.withSuffix(TestTLAnnotations.class, "canNotOverride.model.xml"));
			fail("Invalid model definition.");
		} catch (ExpectedFailure ex) {
			Throwable e = ex;
			while (true) {
				if (e.getMessage()
					.contains(
						"testCanNotOverride:XOverride#reference is not allowed, because part overrides another part.")) {
					break;
				}
				e = e.getCause();
				if (e == null) {
					throw ex;
				}
			}
		}
	}

}
