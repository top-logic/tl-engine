/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.values.edit.editor;

import java.util.Collections;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.mime.MimeTypesModule;
import com.top_logic.layout.form.values.edit.editor.AcceptedTypesChecker;

/**
 * Test case for {@link AcceptedTypesChecker}.
 */
@SuppressWarnings("javadoc")
public class TestAcceptedTypesChecker extends TestCase {

	public void testConcreteMimetype() {
		AcceptedTypesChecker jpg = new AcceptedTypesChecker(Collections.singleton("image/jpeg"));
		assertNull(jpg.checkFileName("foobar.jpg"));
		assertNull(jpg.checkFileName("foobar.jpeg"));
		assertNull(jpg.checkFileName("foobar.JPG"));
		assertNull(jpg.checkFileName("foobar.JPEG"));
		assertNotNull(jpg.checkFileName("foobar.png"));
		assertNotNull(jpg.checkFileName("foobar.PNG"));
	}

	public void testMimetypePattern() {
		AcceptedTypesChecker jpg = new AcceptedTypesChecker(Collections.singleton("image/*"));
		assertNull(jpg.checkFileName("foobar.jpg"));
		assertNull(jpg.checkFileName("foobar.jpeg"));
		assertNull(jpg.checkFileName("foobar.JPG"));
		assertNull(jpg.checkFileName("foobar.JPEG"));
		assertNull(jpg.checkFileName("foobar.png"));
		assertNull(jpg.checkFileName("foobar.PNG"));
	}

	public void testSimpleExt() {
		AcceptedTypesChecker jpg = new AcceptedTypesChecker(Collections.singleton("*.xml"));
		assertNull(jpg.checkFileName("foo.bar.xml"));
		assertNull(jpg.checkFileName("foo.bar.XML"));
		assertNotNull(jpg.checkFileName("foo.bar.JPG"));
	}

	public void testLongExt() {
		AcceptedTypesChecker jpg = new AcceptedTypesChecker(Collections.singleton("*.bar.xml"));
		assertNull(jpg.checkFileName("foo.bar.xml"));
		assertNull(jpg.checkFileName("foo.BAR.XML"));
		assertNotNull(jpg.checkFileName("foo.other.xml"));
		assertNotNull(jpg.checkFileName("foo.bar.JPG"));
	}

	public void testCustomExt() {
		AcceptedTypesChecker jpg = new AcceptedTypesChecker(Collections.singleton("*.myext"));
		assertNull(jpg.checkFileName("foo.myext"));
		assertNull(jpg.checkFileName("foo.MYEXT"));
		assertNotNull(jpg.checkFileName("foo.ext"));
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(
				ServiceTestSetup.createSetup(TestAcceptedTypesChecker.class, MimeTypesModule.Module.INSTANCE));
	}
}
