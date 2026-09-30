/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.designer;

import java.io.StringWriter;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationWriter;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.ViewLoader;
import com.top_logic.layout.view.element.TabBarElement;
import com.top_logic.layout.view.element.TabBarElement.TabConfig;

/**
 * Tests that a view configuration edited in the designer is only accepted when it can be read back.
 *
 * <p>
 * The check the designer performs before saving is a round-trip through
 * {@link ViewLoader#parseConfig(List)}, so it covers every constraint the view loader enforces. These
 * tests exercise that round trip for the states an editor can produce.
 * </p>
 */
public class TestSavedViewIsLoadable extends TestCase {

	private static final String PATH = "/WEB-INF/views/test.view.xml";

	private static String serialize(ViewElement.Config view) throws Exception {
		StringWriter buffer = new StringWriter();
		try (StringWriter out = buffer) {
			new ConfigurationWriter(out).write("view", ViewElement.Config.class, view);
		}
		return buffer.toString();
	}

	private static ViewElement.Config parse(String content) throws ConfigurationException {
		return ViewLoader.parseConfig(List.of(CharacterContents.newContent(content, PATH)));
	}

	private static ViewElement.Config viewWithTabs(String... tabIds) {
		ViewElement.Config view = TypedConfiguration.newConfigItem(ViewElement.Config.class);
		TabBarElement.Config tabBar = TypedConfiguration.newConfigItem(TabBarElement.Config.class);
		for (String tabId : tabIds) {
			TabConfig tab = TypedConfiguration.newConfigItem(TabConfig.class);
			tab.update(tab.descriptor().getProperty(TabConfig.ID), tabId);
			tabBar.getTabs().add(tab);
		}
		view.update(view.descriptor().getProperty(ViewElement.Config.CONTENT), tabBar);
		return view;
	}

	/**
	 * A view with distinct tab identifiers survives the round trip.
	 */
	public void testDistinctKeysAreAccepted() throws Exception {
		String content = serialize(viewWithTabs("first", "second"));

		ViewElement.Config reread = parse(content);

		assertNotNull("The written view can be loaded again", reread);
	}

	/**
	 * Two tabs sharing an identifier are rejected, so such a view is never written.
	 *
	 * <p>
	 * Nothing prevents editing a tab identifier to a value another tab already uses; the duplicate is
	 * only detected when the view is read again.
	 * </p>
	 */
	public void testDuplicateKeyIsRejected() throws Exception {
		// The framework rejects adding a duplicate key, so the state is built by editing an
		// identifier afterwards - exactly as it arises in the designer's configuration form.
		TabBarElement.Config tabBar = TypedConfiguration.newConfigItem(TabBarElement.Config.class);
		ViewElement.Config view = TypedConfiguration.newConfigItem(ViewElement.Config.class);
		view.update(view.descriptor().getProperty(ViewElement.Config.CONTENT), tabBar);
		for (String tabId : new String[] { "tab", "tab2" }) {
			TabConfig tab = TypedConfiguration.newConfigItem(TabConfig.class);
			tab.update(tab.descriptor().getProperty(TabConfig.ID), tabId);
			tabBar.getTabs().add(tab);
		}
		TabConfig second = tabBar.getTabs().get(1);
		second.update(second.descriptor().getProperty(TabConfig.ID), "tab");

		String content = serialize(view);
		try {
			parse(content);
			fail("A view with two tabs named 'tab' must not be accepted for saving.");
		} catch (ConfigurationException ex) {
			assertTrue("The duplicated identifier is named: " + ex.getMessage(),
				ex.getMessage().contains("tab"));
		}
	}

	/**
	 * An element that is not filled in is rejected, so such a view is never written.
	 */
	public void testIncompleteElementIsRejected() throws Exception {
		ViewElement.Config view = TypedConfiguration.newConfigItem(ViewElement.Config.class);
		com.top_logic.layout.view.ReferenceElement.Config reference =
			TypedConfiguration.newConfigItem(com.top_logic.layout.view.ReferenceElement.Config.class);
		reference.update(reference.descriptor().getProperty("view"), "other.view.xml");
		// An added but unfilled binding: "channel" and "to" are mandatory.
		reference.getBindings()
			.add(TypedConfiguration.newConfigItem(com.top_logic.layout.view.channel.ChannelBindingConfig.class));
		view.update(view.descriptor().getProperty(ViewElement.Config.CONTENT), reference);

		String content = serialize(view);
		try {
			parse(content);
			fail("A view with an unfilled channel binding must not be accepted for saving.");
		} catch (ConfigurationException ex) {
			assertTrue("The missing setting is named: " + ex.getMessage(),
				ex.getMessage().contains("mandatory"));
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module, used while parsing polymorphic properties.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestSavedViewIsLoadable.class, TypeIndex.Module.INSTANCE);
	}
}
