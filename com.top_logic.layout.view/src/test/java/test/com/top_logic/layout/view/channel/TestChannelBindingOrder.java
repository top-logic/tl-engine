/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.Collections;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelInputBinding;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.util.model.ModelService;

/**
 * Tests that the channels of a view may read channels declared after them, and channels reading
 * them in turn, since {@link ViewElement} creates all its channels before binding them.
 */
public class TestChannelBindingOrder extends BasicTestCase {

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		super.tearDown();
	}

	/**
	 * Tests that derived channels reading channels declared after them start with the value
	 * computed from those channels, and follow their changes.
	 */
	public void testForwardReference() throws Exception {
		build(
			"<derived-channel name='quadrupled' inputs='doubled' expr='$doubled * 2'/>"
				+ "<derived-channel name='doubled' inputs='base' expr='$base * 2'/>"
				+ "<channel name='base' init='3'/>");

		assertEquals(12, number("quadrupled"));
		assertEquals(6, number("doubled"));

		channel("base").set(5);
		assertEquals(20, number("quadrupled"));
		assertEquals(10, number("doubled"));
	}

	/**
	 * Tests a cycle of channels reading each other, declared with the reset channel first.
	 */
	public void testSettlingCycleResetFirst() throws Exception {
		build(RESET + SELECTION + CHOICE);
		checkSettlingCycle();
	}

	/**
	 * Tests a cycle of channels reading each other, declared with the reset channel last.
	 */
	public void testSettlingCycleResetLast() throws Exception {
		build(CHOICE + SELECTION + RESET);
		checkSettlingCycle();
	}

	/**
	 * A writable channel emptied whenever the channel {@code selection} takes a new value, which in
	 * turn reads it.
	 */
	private static final String RESET = "<channel name='pinned' inputs='selection' init='null'/>";

	/**
	 * A channel computed from the choice and the {@code pinned} channel; the latter does not change
	 * its value.
	 */
	private static final String SELECTION =
		"<derived-channel name='selection' inputs='choice, pinned' expr='$choice'/>";

	/**
	 * The channel the user chooses in.
	 */
	private static final String CHOICE = "<channel name='choice' init='1'/>";

	private void checkSettlingCycle() {
		assertEquals(1, number("selection"));
		assertNull(channel("pinned").get());

		channel("pinned").set("kept");
		assertEquals("A value of the reset channel that leaves its input as it is stays.", "kept",
			channel("pinned").get());
		assertEquals(1, number("selection"));

		channel("choice").set(2);
		assertEquals(2, number("selection"));
		assertNull("A new value of the input empties the reset channel.", channel("pinned").get());

		channel("pinned").set("kept again");
		assertEquals("kept again", channel("pinned").get());
		assertEquals(2, number("selection"));
	}

	/**
	 * Tests that a cycle of channels that does not settle fails with a message naming its channels,
	 * after {@link ChannelInputBinding#MAX_NESTING} rounds.
	 */
	public void testCycleNotSettlingFails() throws Exception {
		try {
			build(
				"<derived-channel name='x' inputs='y' expr=\"$y + 'x'\"/>"
					+ "<derived-channel name='y' inputs='x' expr=\"$x + 'y'\"/>");
			fail("A cycle that does not settle must fail.");
		} catch (IllegalStateException ex) {
			String message = ex.getMessage();
			assertTrue("The message names channel 'x': " + message, message.contains("'x'"));
			assertTrue("The message names channel 'y': " + message, message.contains("'y'"));
		}
	}

	private void build(String channels) throws ConfigurationException {
		String xml = "<view><channels>" + channels + "</channels><stack direction='column'/></view>";
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestChannelBindingOrder.class);
		ConfigurationReader reader = new ConfigurationReader(context,
			Collections.singletonMap("view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class)));
		reader.setSource(CharacterContents.newContent(xml));
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();
		ViewElement view = (ViewElement) context.getInstance(config);
		context.checkErrors();
		view.createControl(_context);
	}

	private ViewChannel channel(String name) {
		return _context.resolveChannel(new ChannelRef(name));
	}

	private int number(String name) {
		return ((Number) channel(name).get()).intValue();
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The scripts of the channels are compiled and evaluated against the application
	 *           model and the knowledge base.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestChannelBindingOrder.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}
}
