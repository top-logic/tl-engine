/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.view.ViewConfig;
import com.top_logic.layout.view.ViewServlet;
import com.top_logic.mig.html.HTMLConstants;

/**
 * Tests that the page body names the {@link ViewConfig#getBusyDelay() busy delay} to the client.
 *
 * @see ViewServlet#writeBusyDelay(TagWriter, ViewConfig)
 */
public class TestBusyDelay extends TestCase {

	/**
	 * Without a setting, a command locks the page after half a second.
	 */
	public void testDefault() {
		ViewConfig config = TypedConfiguration.newConfigItem(ViewConfig.class);

		assertEquals(500L, config.getBusyDelay());
		assertEquals("<body " + ViewServlet.BUSY_DELAY_ATTR + "=\"500\">", bodyTag(config));
	}

	/**
	 * A configured delay is written in milliseconds.
	 */
	public void testConfigured() {
		ViewConfig config = TypedConfiguration.newConfigItem(ViewConfig.class);
		config.update(
			TypedConfiguration.getConfigurationDescriptor(ViewConfig.class).getProperty(ViewConfig.BUSY_DELAY),
			Long.valueOf(1000));

		assertEquals("<body " + ViewServlet.BUSY_DELAY_ATTR + "=\"1000\">", bodyTag(config));
	}

	/**
	 * The delay accepts a unit.
	 */
	public void testParsedWithUnit() throws Exception {
		ViewConfig config = TypedConfiguration.parse("config", ViewConfig.class,
			CharacterContents.newContent("<config " + ViewConfig.BUSY_DELAY + "=\"2s\"/>"));

		assertEquals(2000L, config.getBusyDelay());
	}

	private static String bodyTag(ViewConfig config) {
		TagWriter out = new TagWriter();
		out.beginBeginTag(HTMLConstants.BODY);
		ViewServlet.writeBusyDelay(out, config);
		out.endBeginTag();
		return out.toString();
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestBusyDelay.class, TypeIndex.Module.INSTANCE);
	}
}
