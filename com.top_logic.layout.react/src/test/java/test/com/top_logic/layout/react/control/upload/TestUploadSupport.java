/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.upload;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.react.control.upload.UploadSupport;

/**
 * Tests the upload size limit an application configures and the size the user is told about.
 */
public class TestUploadSupport extends TestCase {

	/** Without an application-specific setting, uploads are limited to 50 MB. */
	public void testTheDefaultLimitIs50MB() {
		assertEquals(50L * 1024 * 1024, UploadSupport.maxUploadSize());
	}

	/** The limit is reported in the unit the user thinks in, not as a byte count. */
	public void testTheLimitIsNamedInAReadableUnit() {
		assertEquals("50 MB", UploadSupport.sizeLabel(50L * 1024 * 1024));
	}

	/** A limit below a kilobyte is named in bytes. */
	public void testASmallLimitIsNamedInBytes() {
		assertEquals("512 B", UploadSupport.sizeLabel(512));
	}

	/** A field without a limit of its own accepts files up to the application-wide limit. */
	public void testNoFieldLimitTakesTheGlobalLimit() {
		assertEquals(1000, UploadSupport.effectiveMaxUploadSize(0, 1000));
		assertEquals(1000, UploadSupport.effectiveMaxUploadSize(-1, 1000));
		assertEquals(UploadSupport.maxUploadSize(), UploadSupport.effectiveMaxUploadSize(0));
	}

	/** Without an application-wide limit, the limit of the field applies. */
	public void testNoGlobalLimitTakesTheFieldLimit() {
		assertEquals(500, UploadSupport.effectiveMaxUploadSize(500, 0));
		assertEquals(500, UploadSupport.effectiveMaxUploadSize(500, -1));
	}

	/** Where both limits are set, the smaller one applies. */
	public void testBothLimitsTakeTheSmallerOne() {
		assertEquals(500, UploadSupport.effectiveMaxUploadSize(500, 1000));
		assertEquals(1000, UploadSupport.effectiveMaxUploadSize(2000, 1000));
		assertEquals(1024, UploadSupport.effectiveMaxUploadSize(1024));
	}

	/** Without any limit, files of every size are accepted. */
	public void testNoLimitAtAll() {
		assertEquals(0, UploadSupport.effectiveMaxUploadSize(0, 0));
	}

	/**
	 * The test suite, started with the resource bundles the number format of a size label needs.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestUploadSupport.class, ResourcesModule.Module.INSTANCE));
	}

}
