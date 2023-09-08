/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.encryption;

import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.encryption.EncryptionService;

/**
 * Test case for {@link EncryptionService}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TestEncryptionService extends TestCase {

	public void testSignature() {
		byte[] plainText = plainText();
		byte[] signature = signature();
		
		assertTrue(service().checkSignature(plainText, signature));
	}

	public void testModifiedSignature() {
		byte[] plainText = plainText();
		byte[] modifiedSignature = signature();
		modifiedSignature[0]++;
		
		assertFalse(service().checkSignature(plainText, modifiedSignature));
	}
	
	public void testModifiedPlainText() {
		byte[] modifiedPlainText = plainText();
		modifiedPlainText[0]++;
		byte[] signature = signature();
		
		assertFalse(service().checkSignature(modifiedPlainText, signature));
	}

	private byte[] plainText() {
		byte[] plainText = new byte[42];
		new Random(42).nextBytes(plainText);
		return plainText;
	}
	
	private byte[] signature() {
		return service().createSignature(plainText());
	}
	
	private EncryptionService service() {
		return EncryptionService.getInstance();
	}

	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(
				TestEncryptionService.class, EncryptionService.Module.INSTANCE));
	}

}
