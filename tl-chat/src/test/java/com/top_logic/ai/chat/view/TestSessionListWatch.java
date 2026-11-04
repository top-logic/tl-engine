/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.ai.chat.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.PersonManagerSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.wrap.person.TestPerson;

import com.top_logic.ai.chat.api.ChatTranscripts;
import com.top_logic.ai.chat.api.messages.ChatSessionDto;
import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.AgentSession;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.util.model.ModelService;

/**
 * When a {@code <chat-conversation>} reads the chat list again and sends it to the browser (#596).
 *
 * <p>
 * The browser shows the open chat's title from the list in its state, so a chat renamed in a
 * session list kept its old title in the conversation's header until the list was sent again. The
 * model events of the rename reach this listener in every window of the owner; the question here is
 * which of them count, and that a list read again for nothing is not sent.
 * </p>
 */
public class TestSessionListWatch extends BasicTestCase {

	private static Person owner;

	private static Person someoneElse;

	private final Map<String, Object> _state = new HashMap<>();

	/** How often the watch read the list. */
	private int _reads;

	/** How often the watch sent a list to the browser. */
	private int _sends;

	private SessionListWatch _watch;

	private TLStructuredType _type;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		if (owner == null) {
			owner = TestPerson.createPerson("list-watch-owner");
			someoneElse = TestPerson.createPerson("list-watch-other");
		}
		_type = TlAiExecutionFactory.getAgentSessionType();
		_reads = 0;
		_sends = 0;
		_watch = new SessionListWatch(_type, () -> owner, person -> {
			_reads++;
			return ChatTranscripts.sessionList(person);
		}, (key, value) -> {
			_sends++;
			_state.put(key, value);
		});
	}

	/** A render writes the list as it is, whatever the browser had. */
	public void testARenderWritesTheList() {
		TLObject own = session("rendered", owner);

		_watch.write();

		assertEquals(1, _sends);
		assertTrue(titles().contains("rendered"));
		assertTrue(ChatSessionIds.idOf(own).length() > 0);
	}

	/** A list read again that says what the browser was told is not sent again. */
	public void testAnUnchangedListIsNotSentAgain() {
		_watch.write();
		TLObject foreign = session("deleted elsewhere", someoneElse);

		_watch.notifyChange(event(List.of(), List.of(), List.of(foreign)));

		assertEquals("The deletion counts.", 2, _reads);
		assertEquals("Nobody's list changed: nothing is sent.", 1, _sends);
	}

	/** A rename is sent as the list with the new title. */
	public void testARenameSendsTheNewTitle() {
		TLObject own = session("old title", owner);
		_watch.write();
		_watch.notifyChange(event(List.of(), List.of(own), List.of()));

		rename(own, "new title");
		_watch.notifyChange(event(List.of(), List.of(own), List.of()));

		assertTrue(titles().contains("new title"));
		assertFalse(titles().contains("old title"));
	}

	/**
	 * The first update of a chat the watch has not seen counts: nothing says what its title was
	 * before, and it may be the rename.
	 */
	public void testFirstUpdateOfAnUnseenChatCounts() {
		TLObject own = session("own", owner);

		_watch.notifyChange(event(List.of(), List.of(own), List.of()));

		assertEquals(Integer.valueOf(1), reads());
	}

	/**
	 * A running agent writes its chat's bookkeeping several times per send. Those writes leave the
	 * title alone and cost the browser no further list read.
	 */
	public void testUpdateWithTheSameTitleDoesNotCountAgain() {
		TLObject own = session("busy", owner);

		_watch.notifyChange(event(List.of(), List.of(own), List.of()));
		_watch.notifyChange(event(List.of(), List.of(own), List.of()));
		_watch.notifyChange(event(List.of(), List.of(own), List.of()));

		assertEquals(Integer.valueOf(1), reads());
	}

	/** A rename of a chat the watch has seen counts, and so does every further one. */
	public void testRenameCounts() {
		TLObject own = session("before", owner);
		_watch.notifyChange(event(List.of(), List.of(own), List.of()));

		rename(own, "after");
		_watch.notifyChange(event(List.of(), List.of(own), List.of()));
		rename(own, "after again");
		_watch.notifyChange(event(List.of(), List.of(own), List.of()));

		assertEquals(Integer.valueOf(3), reads());
	}

	/** A chat of the account that comes into being is a new row in the list: it counts. */
	public void testOwnCreationCounts() {
		TLObject own = session("created", owner);

		_watch.notifyChange(event(List.of(own), List.of(), List.of()));

		assertEquals(Integer.valueOf(1), reads());
	}

	/** Somebody else's chats are not in this account's list, and cost it no request. */
	public void testForeignChangeDoesNotCount() {
		TLObject foreign = session("foreign", someoneElse);

		_watch.notifyChange(event(List.of(foreign), List.of(foreign), List.of()));

		assertEquals(Integer.valueOf(0), reads());
	}

	/** A deleted chat can no longer be asked whose it was, so any deletion counts. */
	public void testDeletionCounts() {
		TLObject foreign = session("deleted", someoneElse);

		_watch.notifyChange(event(List.of(), List.of(), List.of(foreign)));

		assertEquals(Integer.valueOf(1), reads());
	}

	/** A chat created here is a new row; its later bookkeeping writes are not. */
	public void testCreationThenBookkeepingCountsOnce() {
		TLObject own = session("created then busy", owner);

		_watch.notifyChange(event(List.of(own), List.of(), List.of()));
		_watch.notifyChange(event(List.of(), List.of(own), List.of()));

		assertEquals(Integer.valueOf(1), reads());
	}

	/** How often a change made the watch read the list. */
	private Integer reads() {
		return Integer.valueOf(_reads);
	}

	private List<String> titles() {
		@SuppressWarnings("unchecked")
		List<ChatSessionDto> list = (List<ChatSessionDto>) _state.get(SessionListWatch.SESSIONS);
		return list.stream().map(ChatSessionDto::getTitle).toList();
	}

	private static void rename(TLObject session, String title) {
		AgentExecutionStore.getInstance().updateTitle((AgentSession) session, title);
		HistoryUtils.updateSessionRevision();
	}

	private static TLObject session(String title, Person person) {
		TLObject session = ChatHistoryService.createSession(title, person);
		HistoryUtils.updateSessionRevision();
		return session;
	}

	/** A model event that created, updated and deleted exactly the given sessions. */
	private ModelChangeEvent event(List<TLObject> created, List<TLObject> updated, List<TLObject> deleted) {
		return new ModelChangeEvent() {
			@Override
			public ChangeType getChange(TLObject existingObject) {
				if (created.contains(existingObject)) {
					return ChangeType.CREATED;
				}
				if (updated.contains(existingObject)) {
					return ChangeType.UPDATED;
				}
				return deleted.contains(existingObject) ? ChangeType.DELETED : ChangeType.NONE;
			}

			@Override
			public Stream<? extends TLObject> getUpdated() {
				return updated.stream();
			}

			@Override
			public Stream<? extends TLObject> getUpdated(TLStructuredType type) {
				return type == _type ? updated.stream() : Stream.empty();
			}

			@Override
			public Stream<? extends TLObject> getCreated() {
				return created.stream();
			}

			@Override
			public Stream<? extends TLObject> getCreated(TLStructuredType type) {
				return type == _type ? created.stream() : Stream.empty();
			}

			@Override
			public Stream<? extends TLObject> getDeleted() {
				return deleted.stream();
			}

			@Override
			public Stream<? extends TLObject> getDeleted(TLStructuredType type) {
				return type == _type ? deleted.stream() : Stream.empty();
			}
		};
	}

	/** The KB setup a chat session needs. */
	public static Test suite() {
		return PersonManagerSetup.createPersonManagerSetup(
			ServiceTestSetup.createSetup(
				new TestSuite(TestSessionListWatch.class),
				ModelService.Module.INSTANCE,
				AgentExecutionStore.Module.INSTANCE));
	}
}
