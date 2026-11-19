/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.view.ChatRailElement.SectionConfig;
import com.top_logic.ai.core.execution.AgentExecutionStore;
import com.top_logic.base.accesscontrol.SessionService;
import com.top_logic.base.context.TLSubSessionContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.common.BadgeRole;
import com.top_logic.layout.react.state.Badge;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.BadgeBinding;
import com.top_logic.layout.view.element.WithBadge;
import com.top_logic.layout.view.form.FieldControlService;
import com.top_logic.layout.view.table.ColumnProviderService;
import com.top_logic.util.TLContextManager;
import com.top_logic.util.model.ModelService;

/**
 * The badges of the sections of a {@code <chat-rail>}: per section the state of the badge its
 * {@link WithBadge declaration} shows, following the section's badge channel.
 */
public class TestChatRailBadges extends BasicTestCase {

	/** Name of the section counting with the configured role and label. */
	private static final String COUNTED = "counted";

	/** Name of the section showing a plain count. */
	private static final String PLAIN = "plain";

	/** Name of the section without a badge channel. */
	private static final String SILENT = "silent";

	/** Badge channel of {@link #COUNTED}. */
	private static final String COUNT_CHANNEL = "count";

	/** Badge channel of {@link #PLAIN}. */
	private static final String PLAIN_CHANNEL = "plainCount";

	/** The accessible name of the badge of {@link #COUNTED}. */
	private static final String LABEL = "New entries";

	private static Person user;

	private LiveWindow _window;

	private Person _previousAccount;

	private DefaultViewChannel _count;

	private DefaultViewChannel _plain;

	private ChatRailControl _rail;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_previousAccount = subSession().getPerson();
		subSession().setPerson(PersonManager.getManager().getRoot());
		if (user == null) {
			user = TestPerson.createPerson("railer");
		}
		String window = "railWindow";
		_window = LiveWindow.open(user, window, subSession());
		ReactWindowRegistry registry = _window.registry();

		DefaultViewContext view = new DefaultViewContext(
			new DefaultReactContext("/app", window, registry.getOrCreateQueue(window), registry));
		_count = new DefaultViewChannel(COUNT_CHANNEL);
		_count.set(Integer.valueOf(3));
		view.registerChannel(COUNT_CHANNEL, _count);
		_plain = new DefaultViewChannel(PLAIN_CHANNEL);
		_plain.set(Integer.valueOf(0));
		view.registerChannel(PLAIN_CHANNEL, _plain);

		ChatRailElement.Config config = (ChatRailElement.Config) TypedConfiguration.parse(CharacterContents.newContent(
			"<chat-rail xmlns:config='http://www.top-logic.com/ns/config/6.0'"
				+ " config:interface='" + ChatRailElement.Config.class.getName() + "'>"
				+ "<section name='" + COUNTED + "' " + WithBadge.BADGE + "='" + COUNT_CHANNEL + "' "
				+ WithBadge.BADGE_ROLE + "='" + BadgeRole.ERROR.getExternalName() + "'/>"
				+ "<section name='" + PLAIN + "' " + WithBadge.BADGE + "='" + PLAIN_CHANNEL + "'/>"
				+ "<section name='" + SILENT + "'/>"
				+ "</chat-rail>"));
		// A literal text, which needs no resource bundle to be resolved.
		SectionConfig counted = config.getSections().get(0);
		counted.update(counted.descriptor().getProperty(WithBadge.BADGE_LABEL), ResKey.text(LABEL));
		ChatRailElement element =
			(ChatRailElement) SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
		_rail = (ChatRailControl) element.createControl(view);
		_rail.attach();
	}

	@Override
	protected void tearDown() throws Exception {
		_rail.cleanupTree();
		if (_window != null) {
			_window.close();
			_window = null;
		}
		subSession().setPerson(_previousAccount);
		super.tearDown();
	}

	/**
	 * Tests that a section sends the state of its badge with the configured role and label, and
	 * that a section counting nothing or without a badge channel sends none.
	 */
	public void testInitialBadges() {
		assertEquals(Map.of(COUNTED, counted("3")), badges());
	}

	/** Tests that the badge follows the channel, with the rules of {@link BadgeBinding}. */
	public void testBadgesFollowTheChannel() {
		_plain.set(Integer.valueOf(5));
		assertEquals(Map.of(Badge.CONTENT__PROP, "5"), badges().get(PLAIN));

		_count.set(Integer.valueOf(150));
		assertEquals(counted(BadgeBinding.OVERFLOW), badges().get(COUNTED));

		_count.set(Boolean.TRUE);
		assertEquals(Map.of(Badge.DOT__PROP, Boolean.TRUE, Badge.ROLE__PROP, BadgeRole.ERROR.getExternalName(),
			Badge.LABEL__PROP, LABEL), badges().get(COUNTED));

		_count.set(Integer.valueOf(0));
		assertEquals("A count dropping to zero removes the badge.",
			Map.of(PLAIN, Map.of(Badge.CONTENT__PROP, "5")), badges());
	}

	/** Tests that a detached rail catches up with what changed meanwhile when attached again. */
	public void testDetachedRailCatchesUp() {
		_rail.detach();
		_plain.set(Integer.valueOf(7));
		_rail.attach();
		assertEquals(Map.of(Badge.CONTENT__PROP, "7"), badges().get(PLAIN));
	}

	private static Map<String, Object> counted(String content) {
		return Map.of(Badge.CONTENT__PROP, content, Badge.ROLE__PROP, BadgeRole.ERROR.getExternalName(),
			Badge.LABEL__PROP, LABEL);
	}

	@SuppressWarnings("unchecked")
	private Map<String, Object> badges() {
		return (Map<String, Object>) _rail.scriptingScalarState().get(ChatRailControl.SECTION_BADGES);
	}

	private static TLSubSessionContext subSession() {
		return TLContextManager.getSubSession();
	}

	/**
	 * The services a chat control needs, with a person manager for the window's login.
	 */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatRailBadges.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE,
				ChatWindowService.Module.INSTANCE,
				ColumnProviderService.Module.INSTANCE,
				FieldControlService.Module.INSTANCE,
				SessionService.Module.INSTANCE));
	}
}
