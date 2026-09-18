/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.Inputs;

/**
 * Tests that {@link Inputs#getInputs()} is read from both of its notations.
 */
public class TestInputs extends TestCase {

	/**
	 * Tests the comma-separated attribute notation.
	 */
	public void testAttributeNotation() throws Exception {
		assertEquals(refs("a", "b"), parse("<inputs-config inputs='a,b'/>").getInputs());
	}

	/**
	 * Tests the notation listing one element per channel.
	 */
	public void testElementNotation() throws Exception {
		assertEquals(refs("a", "b"), parse(
			"<inputs-config>"
				+ "<inputs><input channel='a'/><input channel='b'/></inputs>"
				+ "</inputs-config>").getInputs());
	}

	/**
	 * Tests that both notations yield the same references.
	 */
	public void testNotationsAgree() throws Exception {
		List<ChannelRef> fromAttribute = parse("<inputs-config inputs='a,b,c'/>").getInputs();
		List<ChannelRef> fromElements = parse(
			"<inputs-config>"
				+ "<inputs><input channel='a'/><input channel='b'/><input channel='c'/></inputs>"
				+ "</inputs-config>").getInputs();

		assertEquals(fromAttribute, fromElements);
	}

	/**
	 * Tests that the whitespace around the separators of the attribute notation is dropped.
	 */
	public void testWhitespaceAroundSeparators() throws Exception {
		assertEquals(refs("a", "b", "c"), parse("<inputs-config inputs=' a , b ,c '/>").getInputs());
	}

	/**
	 * Tests that a configuration declaring no inputs reads as an empty list in either notation.
	 */
	public void testNoInputs() throws Exception {
		assertEquals(Collections.emptyList(), parse("<inputs-config/>").getInputs());
		assertEquals(Collections.emptyList(), parse("<inputs-config inputs=''/>").getInputs());
		assertEquals(Collections.emptyList(), parse("<inputs-config><inputs/></inputs-config>").getInputs());
	}

	/**
	 * Tests that an entry of the element notation must be an {@code input} element.
	 */
	public void testUnexpectedEntryTag() throws Exception {
		try {
			parse("<inputs-config><inputs><channel channel='a'/></inputs></inputs-config>");
			fail("Expected a configuration error for an entry that is not an 'input' element.");
		} catch (ConfigurationException expected) {
			// Expected.
		}
	}

	private static List<ChannelRef> refs(String... names) {
		return List.of(names).stream().map(ChannelRef::new).toList();
	}

	private static Inputs parse(String xml) throws ConfigurationException {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestInputs.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"inputs-config", TypedConfiguration.getConfigurationDescriptor(Inputs.class));

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(CharacterContents.newContent(xml));
		Inputs result = (Inputs) reader.read();
		context.checkErrors();
		return result;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestInputs.class, TypeIndex.Module.INSTANCE);
	}
}
