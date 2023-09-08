/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.form.values.edit.initializer;

import junit.framework.Test;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.form.values.edit.initializer.Initializer;
import com.top_logic.layout.form.values.edit.initializer.InitializerIndex;

/**
 * Test of {@link InitializerIndex}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestInitializerIndex extends BasicTestCase {

	public interface Config1 extends ConfigurationItem {

		String STRING_NAME = "string";

		@Name(STRING_NAME)
		String getString();
	}

	public interface Config2 extends Config1 {
		// Config2 has additional initializer
	}

	public void testCaching() {
		InitializerIndex initializerIndex = new InitializerIndex();
		class Testinitializer implements Initializer {

			@Override
			public void init(ConfigurationItem model, PropertyDescriptor property, Object value) {
				// ignore
			}
		}

		initializerIndex.add(Config1.class, Config1.STRING_NAME, new Testinitializer());
		initializerIndex.add(Config2.class, Config1.STRING_NAME, new Testinitializer());
		ConfigurationDescriptor descr = TypedConfiguration.getConfigurationDescriptor(Config2.class);
		PropertyDescriptor property = descr.getProperty(Config2.STRING_NAME);
		Initializer combinedInitializer = initializerIndex.getInitializer(descr, property);
		Initializer cachedInitializer = initializerIndex.getInitializer(descr, property);
		assertSame("Ticket #19470: Cache not used", combinedInitializer, cachedInitializer);

	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestInitializerIndex}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestInitializerIndex.class);
	}

}
