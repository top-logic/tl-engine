/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.HashMap;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.util.model.ModelService;

/**
 * What the browser is told after it reports a session onto the {@code input} channel of a
 * {@code <chat-conversation>} (Issue #576).
 *
 * <p>
 * The host here binds one conversation to the page, as BOS/Consulting binds one per project: a
 * session the browser reports that is not the page's own is refused, and the page's own is written
 * back. The browser restored the conversation of the page it came from, so the report is exactly
 * such a session.
 * </p>
 */
public class TestSessionSelection extends BasicTestCase {

	private static Person user;

	private final Map<String, Object> _state = new HashMap<>();

	private ViewChannel _channel;

	private SessionSelection _selection;

	private ViewChannel.ChannelListener _follower;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (user == null) {
			user = TestPerson.createPerson("selection-owner");
		}
		_channel = new DefaultViewChannel("chatSession");
		_selection = new SessionSelection(_channel, _state::put, () -> user);
		// The conversation's own listener first, as it is on a page: the element that shows the
		// conversation is built before the helper that corrects it.
		_follower = _selection.listener();
		_channel.addListener(_follower);
	}

	/**
	 * A page with no conversation of its own refuses the one the browser restored, and the browser
	 * is told to start a fresh one - although {@code selected} reads {@code ""} before and after.
	 */
	public void testARefusedSessionLeavesTheChannelEmptyAndSaysSo() {
		TLObject restored = session("the previous page's conversation");
		hostOwns(null);
		_selection.apply();
		assertEquals("", _state.get(SessionSelection.SELECTED));

		_selection.publish(ChatSessionIds.idOf(restored), restored);

		assertNull("The host's push-back stands.", _channel.get());
		assertEquals("The browser is not told to keep the refused session.", "",
			_state.get(SessionSelection.SELECTED));
		assertEquals("Nothing changed in 'selected', so the correction is what the browser sees.",
			Integer.valueOf(1), _state.get(SessionSelection.CORRECTIONS));
	}

	/** A page that has a conversation of its own puts that one back, and the browser opens it. */
	public void testARefusedSessionIsReplacedByTheHostsOwn() {
		TLObject restored = session("restored from elsewhere");
		TLObject own = session("this page's conversation");
		hostOwns(own);

		_selection.publish(ChatSessionIds.idOf(restored), restored);

		assertSame(own, _channel.get());
		assertEquals(ChatSessionIds.idOf(own), _state.get(SessionSelection.SELECTED));
		assertEquals(Integer.valueOf(1), _state.get(SessionSelection.CORRECTIONS));
	}

	/** A session the host takes - a first send creates one - is simply the selection. */
	public void testAnAcceptedSessionIsNoCorrection() {
		TLObject created = session("created by the first send");
		_channel.addListener((sender, oldValue, newValue) -> {
			// A host that claims whatever it is given.
		});

		_selection.publish(ChatSessionIds.idOf(created), created);

		assertSame(created, _channel.get());
		assertEquals(ChatSessionIds.idOf(created), _state.get(SessionSelection.SELECTED));
		assertNull(_state.get(SessionSelection.CORRECTIONS));
	}

	/**
	 * A write that the host answers, with the host's listener in front of the conversation's: the
	 * conversation's listener is told of the host's answer first and of the overwritten write last,
	 * so it must read the channel rather than the value it is handed.
	 */
	public void testAHostWriteFollowsTheValueThatStands() {
		TLObject picked = session("picked from another project's list row");
		TLObject own = session("the open project's conversation");
		_channel.removeListener(_follower);
		hostOwns(own);
		_channel.addListener(_follower);

		_channel.set(picked);

		assertSame(own, _channel.get());
		assertEquals(ChatSessionIds.idOf(own), _state.get(SessionSelection.SELECTED));
	}

	/**
	 * Installs a host that keeps the channel on the given conversation, the way Consulting's
	 * {@code ProjectConversationControl} pushes back a conversation of another project.
	 */
	private void hostOwns(TLObject own) {
		_channel.addListener((sender, oldValue, newValue) -> {
			if (newValue != null && newValue != own) {
				sender.set(own);
			}
		});
	}

	private static TLObject session(String title) {
		TLObject session = ChatHistoryService.createSession(title, user);
		HistoryUtils.updateSessionRevision();
		return session;
	}

	/** The KB setup the owned-session lookup needs. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestSessionSelection.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
