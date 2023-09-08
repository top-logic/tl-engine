/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.util;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.dsa.util.MimeTypes;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.util.TLMimeTypes;

/**
 * Test case for {@link TLMimeTypes}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestTLMimeTypes extends BasicTestCase {

	/** Testing implemetnation of Reloadeable */
	public void testTypeImages() {
		TLMimeTypes mt = TLMimeTypes.getInstance();

		assertEquals("/mimetypes/unknown.png", mt.getMimeTypeImage("type.foobar").resolve().toEncodedForm());
		assertEquals("/mimetypes/binaryContent.png",
			mt.getMimeTypeImage(mt.getMimeType("file.foobar")).resolve().toEncodedForm());
		assertEquals("/mimetypes/large/binaryContent.png",
			mt.getMimeTypeImageLarge(mt.getMimeType("file.foobar")).resolve().toEncodedForm());
		assertEquals("/mimetypes/doc.png", mt.getMimeTypeImage(mt.getMimeType("file.doc")).resolve().toEncodedForm());
		assertEquals("/mimetypes/doc.png", mt.getMimeTypeImage("application/msword").resolve().toEncodedForm());
	}

	/**
	 * Suite of tests.
	 */
	public static Test suite() {
		Test suite = ServiceTestSetup.createSetup(TestTLMimeTypes.class, ThemeFactory.Module.INSTANCE,
			MimeTypes.Module.INSTANCE);
		return ModuleTestSetup.setupModule(suite);
	}

}
