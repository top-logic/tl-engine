/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.demo;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;

import com.top_logic.ai.chat.view.ChatConversationElement;
import com.top_logic.ai.chat.view.ChatRailElement;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.layout.view.ViewElement;

/**
 * What the chat page shows for the objects an agent <code>present</code>s (Issues #493, #616).
 *
 * <p>
 * Since #616 the rail's pane renders whatever stands on its channel - one object as its form, several
 * as a table with the columns of their type, a chart - for any type. What this application decides
 * is only <em>which</em> channel: the one its conversation lets the agent write. So the test pins
 * that the two name the same channel.
 * </p>
 *
 * <p>
 * The test reads the shipped view file rather than a fixture, so it also fails when the
 * configuration stops parsing.
 * </p>
 */
public class TestPresentedObjectsPane extends TestCase {

	/** The chat page of this demo. */
	private static final String VIEW = "/WEB-INF/views/tl-ai-demo/chat-grouped.view.xml";

	/** What the agent presents is what the rail's pane shows. */
	public void testThePaneShowsWhatTheAgentPresents() throws Exception {
		ViewElement.Config view = view();
		ChatRailElement.Config rail = search(view, ChatRailElement.Config.class);
		ChatConversationElement.Config conversation = search(view, ChatConversationElement.Config.class);
		assertNotNull("The chat page has a rail.", rail);
		assertNotNull("The chat page has a conversation.", conversation);

		assertEquals("paneObject", rail.getInput().getChannelName());
		assertTrue("The agent may write the channel the pane shows: " + conversation.getPresent(),
			conversation.getPresent().stream().anyMatch(ref -> "paneObject".equals(ref.getChannelName())));
	}

	/** The shipped view file, parsed. */
	private static ViewElement.Config view() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestPresentedObjectsPane.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = FileManager.getInstance().getData(VIEW);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();

		context.checkErrors();
		return config;
	}

	/**
	 * The first configuration of the given type in the tree below the given one.
	 *
	 * <p>
	 * Only the properties that hold configurations are followed ({@link PropertyKind#ITEM},
	 * {@link PropertyKind#LIST}, {@link PropertyKind#ARRAY}, {@link PropertyKind#MAP}); a
	 * {@link PropertyKind#REF reference} would lead out of the tree and a
	 * {@link PropertyKind#DERIVED derived} value is computed rather than written.
	 * </p>
	 */
	private static <T extends ConfigurationItem> T search(ConfigurationItem root, Class<T> type) {
		if (type.isInstance(root)) {
			return type.cast(root);
		}
		for (PropertyDescriptor property : root.descriptor().getProperties()) {
			switch (property.kind()) {
				case ITEM:
				case LIST:
				case ARRAY:
				case MAP: {
					T found = searchIn(root.value(property), type);
					if (found != null) {
						return found;
					}
					break;
				}
				default:
					break;
			}
		}
		return null;
	}

	private static <T extends ConfigurationItem> T searchIn(Object value, Class<T> type) {
		if (value instanceof ConfigurationItem item) {
			return search(item, type);
		}
		if (value instanceof Collection<?> collection) {
			for (Object entry : collection) {
				T found = searchIn(entry, type);
				if (found != null) {
					return found;
				}
			}
		}
		if (value instanceof Map<?, ?> map) {
			for (Object entry : map.values()) {
				T found = searchIn(entry, type);
				if (found != null) {
					return found;
				}
			}
		}
		if (value instanceof Object[] array) {
			for (Object entry : array) {
				T found = searchIn(entry, type);
				if (found != null) {
					return found;
				}
			}
		}
		return null;
	}

	/**
	 * All this test needs is the {@link FileManager} over the web application and the
	 * {@code TypeIndex} that typed configuration resolves the nested element tags through.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(TestPresentedObjectsPane.class);
	}

}
