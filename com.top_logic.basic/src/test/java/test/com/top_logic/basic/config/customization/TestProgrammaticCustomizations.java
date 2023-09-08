/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.customization;

import java.lang.annotation.Annotation;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.BasicTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Id;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.customization.ProgrammaticCustomizations;

/**
 * Test for {@link ProgrammaticCustomizations}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestProgrammaticCustomizations extends BasicTestCase {

	public void testTypeAnnotations() {
		ProgrammaticCustomizations customizations = new ProgrammaticCustomizations();
		Annotation mandatory = mandatory();
		customizations.addAnnotation(Object.class, mandatory);
		assertEquals(mandatory, customizations.getAnnotation(Object.class, Mandatory.class));

		Annotation hidden = hidden();
		customizations.addAnnotation(Object.class, hidden);
		assertEquals(hidden, customizations.getAnnotation(Object.class, Hidden.class));

		Annotation id = id();
		customizations.addAnnotation(Object.class, id);
		assertEquals(id, customizations.getAnnotation(Object.class, Id.class));

		Annotation id2 = id();
		customizations.addAnnotation(Object.class, id2);
		assertEquals("Annotation of same typeis overridden", id2, customizations.getAnnotation(Object.class, Id.class));

		Annotation intHidden = hidden();
		customizations.addAnnotation(String.class, intHidden);
		assertEquals(intHidden, customizations.getAnnotation(String.class, Hidden.class));
		assertEquals("No mandatory customization for String set.", null,
			customizations.getAnnotation(String.class, Mandatory.class));
		assertEquals("No customization for Integer set.", null,
			customizations.getAnnotation(Integer.class, Mandatory.class));
	}

	private Annotation mandatory() {
		return TypedConfiguration.newAnnotationItem(Mandatory.class);
	}

	private Annotation hidden() {
		return TypedConfiguration.newAnnotationItem(Hidden.class);
	}

	private Annotation id() {
		return TypedConfiguration.newAnnotationItem(Id.class);
	}

	/**
	 * @return a cumulative {@link Test} for all Tests in {@link TestProgrammaticCustomizations}.
	 */
	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(TestProgrammaticCustomizations.class);
	}

}

