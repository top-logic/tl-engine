/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.security.password.hashing;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.security.password.hashing.Argon2Hashing;
import com.top_logic.base.security.password.hashing.Argon2Hashing.Argon2Type;
import com.top_logic.base.security.password.hashing.Argon2Hashing.Config;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;

/**
 * Test for {@link Argon2Hashing}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestArgon2Hashing extends BasicTestCase {

	public void testHashVerify() {
		Argon2Hashing hashing = newInstance();
		char[] pwd = "My funny pwd".toCharArray();
		String hash = hashing.createHash(pwd);
		assertTrue(hashing.verify(pwd, hash));
		assertTrue("Duplicate verifying works.", hashing.verify(pwd, hash));
	}

	public void testHashVerifyAgainstDifferentParameters() {
		Argon2Hashing hashing = newInstance();
		char[] pwd = "My funny pwd".toCharArray();
		String hash = hashing.createHash(pwd);
		assertTrue(hashing.verify(pwd, hash));
		assertTrue("Duplicate verifying works.", hashing.verify(pwd, hash));
	}

	public void testDifferentHashes() {
		Argon2Hashing hashing = newInstance(Argon2Type.ARGON2D, 5, 6000);
		char[] pwd = "My funny pwd".toCharArray();
		String hash = hashing.createHash(pwd);
		Argon2Hashing otherHashing = newInstance(Argon2Type.ARGON2I, 3, 65000);
		assertTrue(hashing.verify(pwd, hash));
		assertTrue(otherHashing.verify(pwd, hash));
	}

	public void testPepper() {
		Argon2Hashing hashing = newInstance(Argon2Type.ARGON2D, 5, 6000, "pepper1");
		Argon2Hashing hashing2 = newInstance(Argon2Type.ARGON2D, 5, 6000, "pepper2");
		char[] pwd = "My funny pwd".toCharArray();
		String hash = hashing.createHash(pwd);
		assertTrue(hashing.verify(pwd, hash));
		assertFalse(hashing2.verify(pwd, hash));
	}

	private Argon2Hashing newInstance() {
		Config config = newConfig();
		return TypedConfigUtil.createInstance(config);
	}

	private Config newConfig() {
		Config config = TypedConfiguration.newConfigItem(Argon2Hashing.Config.class);
		config.setImplementationClass(Argon2Hashing.class);
		return config;
	}

	private Argon2Hashing newInstance(Argon2Type type, int iterations, int memory) {
		return newInstance(type, iterations, memory, null);
	}

	private Argon2Hashing newInstance(Argon2Type type, int iterations, int memory, String pepper) {
		Config config = newConfig();
		config.setType(type);
		config.setIterations(iterations);
		config.setMemory(memory);
		config.setPepper(pepper);
		return TypedConfigUtil.createInstance(config);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestArgon2Hashing}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestArgon2Hashing.class);
	}

}
