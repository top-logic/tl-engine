/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic;

import java.util.regex.Pattern;

import junit.framework.TestCase;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ApplicationConfig.Module;
import com.top_logic.basic.util.Computation;

/**
 * {@link TestCase} for {@link ConfigLoaderTestUtil}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@SuppressWarnings("javadoc")
public final class TestConfigLoaderTestUtil extends TestCase {

	public void testConfigIsLoadedWithin_runWithLoadedConfig() {
		assertConfigIsNotLoaded();
		ConfigLoaderTestUtil.INSTANCE.runWithLoadedConfig(new Computation<Void>() {

			@Override
			public Void run() {
				assertConfigIsLoaded();
				return null;
			}

		});
		assertConfigIsNotLoaded();
	}

	public void testConfigIsLoadedAfter_loadConfig() {
		assertConfigIsNotLoaded();
		ConfigLoaderTestUtil.INSTANCE.loadConfig();
		try {
			assertConfigIsLoaded();
		} finally {
			if (configModule().isActive()) {
				ConfigLoaderTestUtil.INSTANCE.unloadConfig();
			}
		}
		assertConfigIsNotLoaded();
	}

	public void testNoNesting_loadConfig() {
		assertConfigIsNotLoaded();
		ConfigLoaderTestUtil.INSTANCE.loadConfig();
		try {
			assertConfigIsLoaded();
			assertConfigLoadingIsRejected();
			assertConfigIsLoaded();
		} finally {
			if (configModule().isActive()) {
				ConfigLoaderTestUtil.INSTANCE.unloadConfig();
			}
		}
		assertConfigIsNotLoaded();
	}

	public void testNoNesting_runWithLoadedConfig() {
		assertConfigIsNotLoaded();
		ConfigLoaderTestUtil.INSTANCE.runWithLoadedConfig(new Computation<Void>() {

			@Override
			public Void run() {
				assertConfigIsLoaded();
				assertConfigLoadingIsRejected();
				assertConfigIsLoaded();
				return null;
			}

		});
		assertConfigIsNotLoaded();
	}

	void assertConfigLoadingIsRejected() {
		boolean nestingRejected;
		try {
			ConfigLoaderTestUtil.INSTANCE.loadConfig();
			nestingRejected = false;
			ConfigLoaderTestUtil.INSTANCE.unloadConfig();
		} catch (RuntimeException ex) {
			String message = "Trying to load the configuration failed for the wrong reason.";
			Pattern regex = Pattern.compile("The configuration is already loaded");
			BasicTestCase.assertErrorMessage(message, regex, ex);
			nestingRejected = true;
		}
		assertTrue(nestingRejected);
	}

	void assertConfigIsLoaded() {
		assertTrue(configModule().isActive());
	}

	void assertConfigIsNotLoaded() {
		assertFalse(configModule().isActive());
	}

	Module configModule() {
		return ApplicationConfig.Module.INSTANCE;
	}

}
