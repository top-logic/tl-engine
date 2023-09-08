/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.basic.config.internal;

import java.io.File;
import java.util.Collections;
import java.util.List;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.basic.BasicTestSetup;
import test.com.top_logic.basic.config.TestTypedConfiguration;

import com.top_logic.basic.Logger.LogEntry;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.internal.CompiledItemFactoryBuilder;
import com.top_logic.basic.config.internal.FactoryBuilder;
import com.top_logic.basic.config.internal.gen.ConfigItemGenerator;
import com.top_logic.basic.io.FileUtilities;
import com.top_logic.basic.logging.Level;
import com.top_logic.basic.tools.CollectingLogListener;

/**
 * Test case for {@link ConfigItemGenerator} using compiled items for the test
 * {@link TestTypedConfiguration}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestConfigItemGenerator extends TestTypedConfiguration {

	public static class Setup extends TestSetup {

		public Setup(Test test) {
			super(test);
		}

		static FactoryBuilder _builder;

		private CollectingLogListener _listener = new CollectingLogListener(Collections.singleton(Level.ERROR), false);

		@Override
		protected void setUp() throws Exception {
			File generationDir = File.createTempFile("test-generated", "");
			FileUtilities.deleteR(generationDir);
			generationDir.mkdirs();

			_listener.activate();
			_builder = new CompiledItemFactoryBuilder(generationDir);

			super.setUp();
		}

		@Override
		protected void tearDown() throws Exception {
			super.tearDown();

			_builder = null;
			List<LogEntry> errors = _listener.getAndClearLogEntries();
			assertTrue(errors.toString(), errors.isEmpty());
		}
	}


	@Override
	public void testGetInstanceListFailure() throws ConfigurationException {
		// Ignore, willfully logs an error.
	}

	@Override
	public void testReadInstanceListFailure() throws ConfigurationException {
		// Ignore, willfully logs an error.
	}

	@Override
	public void testGetInstanceMapFailure() throws ConfigurationException {
		// Ignore, willfully logs an error.
	}

	@Override
	public void testReadInstanceMapFailure() throws ConfigurationException {
		// Ignore, willfully logs an error.
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <T extends ConfigurationItem> T newConfigItem(Class<T> type) {
		return (T) Setup._builder
			.createFactory(TypedConfiguration.getConfigurationDescriptor(type))
			.createNew();
	}

	public static Test suite() {
		return BasicTestSetup.createBasicTestSetup(new Setup(new TestSuite(TestConfigItemGenerator.class)));
	}

}
