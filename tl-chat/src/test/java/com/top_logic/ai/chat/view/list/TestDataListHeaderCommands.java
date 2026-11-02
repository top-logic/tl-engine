/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view.list;

import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.button.ReactButtonControl;
import com.top_logic.layout.react.state.ButtonState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.TLContext;
import com.top_logic.util.model.ModelService;

/**
 * The header commands of a {@code <data-list>} show the state of their command (#30106).
 *
 * <p>
 * A header command is rendered as the engine's command button, so it is hidden while its rules hide
 * it, disabled with the reason as tooltip while they disable it, and follows a change of its input
 * channel. A click on a command that is not offered runs nothing.
 * </p>
 */
public class TestDataListHeaderCommands extends BasicTestCase {

	/** The input channel the command decides on. */
	private static final String STATE_CHANNEL = "state";

	/** The channel the command clears when it runs. */
	private static final String OUT_CHANNEL = "out";

	/** {@link #STATE_CHANNEL} value under which the command is hidden. */
	private static final String HIDDEN = "hidden";

	/** {@link #STATE_CHANNEL} value under which the command is disabled with {@link #REASON}. */
	private static final String BUSY = "busy";

	/** {@link #STATE_CHANNEL} value under which the command is executable. */
	private static final String READY = "ready";

	/** The reason the command is disabled for. */
	private static final String REASON = "The agent is not available.";

	/** The tooltip of the command while it is executable. */
	private static final String TOOLTIP = "Starts a new conversation.";

	/** The label of the command. */
	private static final String LABEL = "New conversation";

	/** What {@link #OUT_CHANNEL} holds until the command runs. */
	private static final String UNTOUCHED = "untouched";

	private ViewChannel _state;

	private ViewChannel _out;

	private DataListControl _list;

	private Runnable _restoreIdentity;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TLContext context = TLContext.getContext();
		_restoreIdentity = context.captureIdentity();
		context.setPerson(PersonManager.getManager().getRoot());
		ReactWindowRegistry registry = new ReactWindowRegistry("test-session");
		String window = "dataListHeaderWindow";
		DefaultReactContext react =
			new DefaultReactContext("/app", window, registry.getOrCreateQueue(window), registry);
		ViewContext view = new DefaultViewContext(react);
		_state = new DefaultViewChannel(STATE_CHANNEL);
		_out = new DefaultViewChannel(OUT_CHANNEL);
		view.registerChannel(STATE_CHANNEL, _state);
		view.registerChannel(OUT_CHANNEL, _out);

		DataListElement.Config config = (DataListElement.Config) TypedConfiguration.parse(
			CharacterContents.newContent("<data-list xmlns:config='http://www.top-logic.com/ns/config/6.0'"
				+ " config:interface='" + DataListElement.Config.class.getName() + "'>"
				+ "<rows><![CDATA[list()]]></rows>"
				+ "<commands>"
				+ "<generic-command image='css:bi bi-plus-lg' inputs='" + STATE_CHANNEL + "'>"
				+ "<label><en>" + LABEL + "</en></label>"
				+ "<tooltip><en>" + TOOLTIP + "</en></tooltip>"
				+ "<executability>"
				+ "<visible-if expr=\"$" + STATE_CHANNEL + " != '" + HIDDEN + "'\"/>"
				+ "<disabled-if expr=\"if($" + STATE_CHANNEL + " == '" + BUSY + "', '" + REASON + "', null)\"/>"
				+ "</executability>"
				+ "<write-channel name='" + OUT_CHANNEL + "' clear='true'/>"
				+ "</generic-command>"
				+ "</commands>"
				+ "</data-list>"));
		_out.set(UNTOUCHED);
		DataListElement element = new DataListElement(new DefaultInstantiationContext(getClass()), config);
		_list = (DataListControl) element.createControl(view);
	}

	@Override
	protected void tearDown() throws Exception {
		_list.cleanupTree();
		_restoreIdentity.run();
		super.tearDown();
	}

	/** A command its rules hide is not shown, and a click on it runs nothing. */
	public void testHidden() {
		_state.set(HIDDEN);
		_list.attach();

		ReactButtonControl button = button();
		assertTrue("A hidden command is not shown.", button.isHidden());
		assertFalse(click(button).isSuccess());
		assertEquals("A hidden command does not run.", UNTOUCHED, _out.get());
	}

	/** A command its rules disable is shown disabled, with their reason as tooltip. */
	public void testDisabledWithReason() {
		_state.set(BUSY);
		_list.attach();

		ReactButtonControl button = button();
		assertFalse("A disabled command is shown.", button.isHidden());
		assertEquals(Boolean.TRUE, state(button).get(ButtonState.DISABLED__PROP));
		assertEquals("The reason is the tooltip.", REASON, state(button).get(ButtonState.TOOLTIP__PROP));
		assertFalse(click(button).isSuccess());
		assertEquals("A disabled command does not run.", UNTOUCHED, _out.get());
	}

	/** An executable command is shown enabled, with its label and tooltip, and runs on a click. */
	public void testExecutable() {
		_state.set(READY);
		_list.attach();

		ReactButtonControl button = button();
		assertFalse(button.isHidden());
		assertEquals(Boolean.FALSE, state(button).get(ButtonState.DISABLED__PROP));
		assertEquals(LABEL, state(button).get(ButtonState.LABEL__PROP));
		assertEquals(TOOLTIP, state(button).get(ButtonState.TOOLTIP__PROP));
		assertTrue(click(button).isSuccess());
		assertNull("The command ran.", _out.get());
	}

	/** The button follows its command when the input channel changes while the list is shown. */
	public void testStateFollowsInputChannel() {
		_state.set(READY);
		_list.attach();
		ReactButtonControl button = button();
		assertEquals(Boolean.FALSE, state(button).get(ButtonState.DISABLED__PROP));

		_state.set(BUSY);
		assertEquals(Boolean.TRUE, state(button).get(ButtonState.DISABLED__PROP));
		assertEquals(REASON, state(button).get(ButtonState.TOOLTIP__PROP));

		_state.set(HIDDEN);
		assertTrue(button.isHidden());

		_state.set(READY);
		assertFalse(button.isHidden());
		assertEquals(Boolean.FALSE, state(button).get(ButtonState.DISABLED__PROP));
		assertEquals(TOOLTIP, state(button).get(ButtonState.TOOLTIP__PROP));
	}

	/** The header button of the list, the one child it displays. */
	private ReactButtonControl button() {
		List<ReactControl> children = _list.displayedChildren();
		assertEquals("One header command, one button.", 1, children.size());
		return (ReactButtonControl) children.get(0);
	}

	/** The state the button shows; a key the button leaves unset is absent. */
	private static Map<String, Object> state(ReactButtonControl button) {
		return button.scriptingScalarState();
	}

	private static HandlerResult click(ReactButtonControl button) {
		return button.executeCommand(ReactButtonControl.CMD_CLICK, Map.of());
	}

	/** The knowledge base with the chat model. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestDataListHeaderCommands.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
