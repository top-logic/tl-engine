/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.model.provider;

import java.util.Collections;
import java.util.Map;
import java.util.TimeZone;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.model.annotate.TLDefaultValue;
import com.top_logic.model.provider.ConfiguredConstantDefaultProvider;
import com.top_logic.model.provider.CountryDefaultProvider;
import com.top_logic.model.provider.DefaultProvider;
import com.top_logic.model.provider.StringDefaultProvider;
import com.top_logic.model.provider.TimeZoneDefaultProvider;
import com.top_logic.util.Country;

/**
 * Test for the {@link ConfiguredConstantDefaultProvider}s configured as
 * {@link TLDefaultValue default-value} annotation of an attribute.
 */
@SuppressWarnings("javadoc")
public class TestConfiguredConstantDefaultProvider extends BasicTestCase {

	public void testCountry() throws ConfigurationException {
		DefaultProvider provider = provider("<default-value><country value='DE'/></default-value>");

		assertInstanceof(provider, CountryDefaultProvider.class);
		assertEquals(new Country("DE"), provider.createDefault(null, null));
	}

	public void testTimeZone() throws ConfigurationException {
		DefaultProvider provider =
			provider("<default-value><time-zone value='Europe/Berlin'/></default-value>");

		assertInstanceof(provider, TimeZoneDefaultProvider.class);
		assertEquals(TimeZone.getTimeZone("Europe/Berlin"), provider.createDefault(null, null));
	}

	public void testString() throws ConfigurationException {
		DefaultProvider provider = provider("<default-value><string value='abc'/></default-value>");

		assertInstanceof(provider, StringDefaultProvider.class);
		assertEquals("abc", provider.createDefault(null, null));
	}

	private static DefaultProvider provider(String xml) throws ConfigurationException {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap("default-value",
			TypedConfiguration.getConfigurationDescriptor(TLDefaultValue.class));
		Protocol log = new AssertProtocol();
		InstantiationContext context = new DefaultInstantiationContext(log);
		TLDefaultValue annotation = (TLDefaultValue) new ConfigurationReader(context, descriptors)
			.setSource(CharacterContents.newContent(xml)).read();
		log.checkErrors();

		DefaultProvider result = context.getInstance(annotation.getProvider());
		log.checkErrors();
		return result;
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(new TestSuite(TestConfiguredConstantDefaultProvider.class));
	}

}
