/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.component.model;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.AssertProtocol;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.NamedConstant;
import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.layout.component.model.StaticMethodModelProvider;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.mig.html.layout.SimpleComponent;

/**
 * Test case for {@link StaticMethodModelProvider}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@SuppressWarnings("javadoc")
public class TestStaticMethodModelProvider extends TestCase {
	
	private static final NamedConstant MODEL = new NamedConstant("theModel");

	public void testGetModel() throws ConfigurationException {
		String xml = 
			"<component class='"  + SimpleComponent.class.getName() + "' " + 
				"model='provider(" + TestStaticMethodModelProvider.class.getName() + "#getModel())' " +
			"/>";
		LayoutComponent.Config config = (LayoutComponent.Config) read(xml);

		LayoutComponent component = SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		component.linkChannels(new AssertProtocol());
		assertEquals(MODEL, component.getModel());
	}

	public static Object getModel() {
		return MODEL;
	}

	private ConfigurationItem read(String xml) throws ConfigurationException {
		return reader().setSource(CharacterContents.newContent(xml)).read();
	}

	private ConfigurationReader reader() {
		return new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, descriptors());
	}

	private Map<String, ConfigurationDescriptor> descriptors() {
		return Collections.singletonMap(LayoutComponent.Config.COMPONENT, TypedConfiguration.getConfigurationDescriptor(LayoutComponent.Config.class));
	}

	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(ServiceTestSetup.createSetup(TestStaticMethodModelProvider.class,
			ApplicationConfig.Module.INSTANCE));
	}

}
