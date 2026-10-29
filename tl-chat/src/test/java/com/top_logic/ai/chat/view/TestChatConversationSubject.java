/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.List;
import java.util.Set;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.basic.col.LazyTypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLObject;
import com.top_logic.tl.ai.core.agent.approval.ApprovalMode;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.util.model.ModelService;

/**
 * What {@code <chat-conversation subject="…">} stamps a conversation with (Issue #508).
 *
 * <p>
 * The element offers its channel to its chat while it is displayed
 * ({@link ChatViewBinding#offerSubject(ViewChannel)}); the request creating a conversation reads it
 * there ({@link ChatViewBinding#subjectOf(TypedAnnotatable, String)}) and creates the conversation
 * with it ({@link ChatHistoryService#createSession(String, Person, ApprovalMode, TLObject)}), before
 * the first message is sent. Together they are the page's promise: a conversation started beside a
 * project is found again from that project, and nothing else is moved there.
 * </p>
 */
public class TestChatConversationSubject extends BasicTestCase {

	private static final String CHAT = "subject-chat";

	private static Person user;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (user == null) {
			user = TestPerson.createPerson("subject-owner");
		}
	}

	/** One object on the channel is the subject. */
	public void testAnObjectIsTheSubject() {
		TLObject project = session("a project stands in for the host's object");

		assertSame(project, ChatViewBinding.singleObject(project));
	}

	/** A one-row selection names one object, which is what a selection channel usually holds. */
	public void testASingleSelectionIsTheObject() {
		TLObject project = session("the only selected row");

		assertSame(project, ChatViewBinding.singleObject(List.of(project)));
	}

	/** Nothing on the channel means the page was showing nothing, which stamps nothing. */
	public void testAnEmptyChannelStampsNothing() {
		assertNull(ChatViewBinding.singleObject(null));
		assertNull(ChatViewBinding.singleObject(List.of()));
	}

	/** Several objects name no single thing the conversation could be about. */
	public void testAMultipleSelectionStampsNothing() {
		TLObject one = session("first of a multi-selection");
		TLObject two = session("second of a multi-selection");

		assertNull(ChatViewBinding.singleObject(List.of(one, two)));
	}

	/** Anything that is not a model object is not a subject either. */
	public void testANonObjectStampsNothing() {
		assertNull(ChatViewBinding.singleObject("Project 4711"));
		assertNull(ChatViewBinding.singleObject(List.of("Project 4711")));
	}

	/**
	 * The chat names the subject of its displayed element, whatever stands on the channel when the
	 * conversation is created, and nothing once the element is gone.
	 */
	public void testTheChatOffersWhatItsElementShows() {
		TLObject first = session("shown first");
		TLObject second = session("shown later");
		TypedAnnotatable window = new LazyTypedAnnotatable();
		ChatViewBinding binding = ChatViewBinding.bind(window, "w1", "/app", CHAT);
		ViewChannel channel = new DefaultViewChannel("project");

		assertNull("No element offers a subject.", ChatViewBinding.subjectOf(window, CHAT));

		binding.offerSubject(channel);
		assertNull("The page shows nothing.", ChatViewBinding.subjectOf(window, CHAT));
		channel.set(first);
		assertSame(first, ChatViewBinding.subjectOf(window, CHAT));
		channel.set(second);
		assertSame(second, ChatViewBinding.subjectOf(window, CHAT));
		assertNull("Another chat of the window is about nothing.", ChatViewBinding.subjectOf(window, "other"));

		binding.withdrawSubject(new DefaultViewChannel("another element's"));
		assertSame("Another element's withdrawal is not this one's.", second,
			ChatViewBinding.subjectOf(window, CHAT));
		binding.withdrawSubject(channel);
		assertNull(ChatViewBinding.subjectOf(window, CHAT));
	}

	/**
	 * End to end: a conversation created about the offered object is about it as soon as it exists,
	 * the scoped list finds it, and a conversation started without one is not in it.
	 */
	public void testTheCreatedConversationIsWhatAScopedListFinds() {
		TLObject project = session("the object this page is about");
		TypedAnnotatable window = new LazyTypedAnnotatable();
		ViewChannel channel = new DefaultViewChannel("project");
		channel.set(project);
		ChatViewBinding.bind(window, "w1", "/app", CHAT).offerSubject(channel);

		AgentSession stamped = (AgentSession) ChatHistoryService.createSession("about that object", user,
			ApprovalMode.MANUAL, ChatViewBinding.subjectOf(window, CHAT));
		AgentSession unstamped = (AgentSession) ChatHistoryService.createSession("about nothing", user,
			ApprovalMode.MANUAL, null);
		HistoryUtils.updateSessionRevision();

		assertEquals(project, stamped.getSubject());
		assertNull(unstamped.getSubject());
		Set<? extends TLObject> scoped =
			project.tReferers(TlAiExecutionFactory.getSubjectAgentSessionAttr());
		assertEquals("The conversation that was started here, and no other.", Set.of(stamped), scoped);
	}

	/**
	 * Any persisted object will do as the host's own: what is under test knows nothing about
	 * projects.
	 */
	private static TLObject session(String title) {
		TLObject session = ChatHistoryService.createSession(title, user);
		HistoryUtils.updateSessionRevision();
		return session;
	}

	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestChatConversationSubject.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
