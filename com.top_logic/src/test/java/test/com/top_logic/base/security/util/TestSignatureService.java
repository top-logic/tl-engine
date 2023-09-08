/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.security.util;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.base.security.util.SignatureService;
import com.top_logic.basic.Logger;

/**
 * Test case for {@link SignatureService}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestSignatureService extends TestCase {

	public void testSign() {
		String message = "Orwell war ein Optimist";
		SignatureService tool = SignatureService.getInstance();
		String signature = tool.sign(message);

		Logger.info("signed message: " + signature, this);

		assertNotNull(signature);
		assertTrue(tool.verify(message, signature));
		assertFalse(tool.verify(message + "x", signature));
	}

	public void testVerifyNull() {
		String theName = "Orwell war ein Optimist";
		SignatureService tool = SignatureService.getInstance();
		String signature = tool.sign(theName);

		Logger.info("signed message: " + signature, this);

		assertNotNull(signature);
		assertFalse(tool.verify((String) null, signature));
	}

	public static Test suite() {
		return TLTestSetup.createTLTestSetup(ServiceTestSetup.createSetup(TestSignatureService.class,
			SignatureService.Module.INSTANCE));
	}

}
