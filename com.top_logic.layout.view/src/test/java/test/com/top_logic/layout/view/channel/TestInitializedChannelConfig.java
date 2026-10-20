/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.BufferingProtocol;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelFactory;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.InitializedViewChannel;
import com.top_logic.layout.view.channel.ValueChannelConfig;
import com.top_logic.layout.view.channel.ValueChannelFactory;

/**
 * Tests the configuration of a {@code <channel>} with an initialization function
 * ({@link ValueChannelConfig#getInit()}).
 *
 * <p>
 * The evaluation of the TL-Script function needs the application services; the behavior of the
 * created {@link InitializedViewChannel} is tested with Java functions in
 * {@link TestInitializedViewChannel} and {@link TestInitializedViewChannelObservation}.
 * </p>
 */
public class TestInitializedChannelConfig extends TestCase {

	/**
	 * Tests that {@code inputs}, {@code init} and {@code observed-types} are parsed.
	 */
	public void testParse() throws Exception {
		ViewElement.Config config = parse("test-initialized-channels.view.xml");

		assertEquals(3, config.getChannels().size());

		ValueChannelConfig plain = (ValueChannelConfig) config.getChannels().get(0);
		assertNull("A plain channel has no initialization function.", plain.getInit());
		assertTrue(plain.getInputs().isEmpty());

		ValueChannelConfig budget = (ValueChannelConfig) config.getChannels().get(1);
		assertEquals(1, budget.getInputs().size());
		assertEquals("project", budget.getInputs().get(0).getChannel().getChannelName());
		assertNotNull(budget.getInit());
		assertEquals(1, budget.getObservedTypes().size());
		assertEquals("test.types:B", budget.getObservedTypes().get(0).qualifiedName());

		ValueChannelConfig limit = (ValueChannelConfig) config.getChannels().get(2);
		assertTrue("An initialization function needs no inputs.", limit.getInputs().isEmpty());
		assertNotNull(limit.getInit());
	}

	/**
	 * Tests that the factories are instantiated, and that a plain channel is created as it is
	 * without initialization function.
	 */
	public void testFactories() throws Exception {
		ViewElement.Config config = parse("test-initialized-channels.view.xml");

		DefaultInstantiationContext context = new DefaultInstantiationContext(TestInitializedChannelConfig.class);
		ChannelFactory plain = context.getInstance(config.getChannels().get(0));
		ChannelFactory budget = context.getInstance(config.getChannels().get(1));
		context.checkErrors();

		assertTrue(plain instanceof ValueChannelFactory);
		assertTrue(budget instanceof ValueChannelFactory);
		assertEquals("A plain channel is the value channel it always was.", DefaultViewChannel.class,
			plain.createChannel(new DefaultViewContext(null)).getClass());
	}

	/**
	 * Tests that inputs without an initialization function are reported as a configuration error.
	 */
	public void testInputsWithoutInitFail() throws Exception {
		ViewElement.Config config = parse("test-inputs-without-init.view.xml");

		BufferingProtocol log = new BufferingProtocol();
		DefaultInstantiationContext context = new DefaultInstantiationContext(log);
		context.getInstance(config.getChannels().get(1));
		assertTrue("Inputs without an initialization function must be reported.", log.hasErrors());
	}

	private ViewElement.Config parse(String resource) throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestInitializedChannelConfig.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(new ClassRelativeBinaryContent(TestInitializedChannelConfig.class, resource));
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();
		return config;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestInitializedChannelConfig.class, TypeIndex.Module.INSTANCE);
	}
}
