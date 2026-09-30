/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
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

import com.top_logic.ai.chat.history.ChatHistoryService;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.tl.ai.core.execution.AgentExecutionStore;
import com.top_logic.tl.ai.core.execution.model.TlAiExecutionFactory;
import com.top_logic.util.model.ModelService;

/**
 * When a {@code <chat-conversation>} tells the browser to read the chat list again (#596).
 *
 * <p>
 * The browser shows the open chat's title from its own copy of that list, so a chat renamed in a
 * session list kept its old title in the conversation's header. The model events of the rename
 * reach this listener in every window of the owner; the question here is which of them count.
 * </p>
 */
public class TestSessionListWatch extends BasicTestCase {

	private static Person owner;

	private static Person someoneElse;

	private final Map<String, Object> _state = new HashMap<>();

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
		_watch = new SessionListWatch(_type, () -> owner, _state::put);
	}

	/** The browser is told a count from the start, so that the first change is a change. */
	public void testStartsAtZero() {
		assertEquals(Integer.valueOf(0), revision());
	}

	/** A rename of one of the account's chats is an update of that chat: it counts. */
	public void testOwnUpdateCounts() {
		TLObject own = session("own", owner);

		_watch.notifyChange(event(List.of(), List.of(own), List.of()));

		assertEquals(Integer.valueOf(1), revision());
	}

	/** A chat of the account that comes into being is a new row in the list: it counts. */
	public void testOwnCreationCounts() {
		TLObject own = session("created", owner);

		_watch.notifyChange(event(List.of(own), List.of(), List.of()));

		assertEquals(Integer.valueOf(1), revision());
	}

	/** Somebody else's chats are not in this account's list, and cost it no request. */
	public void testForeignChangeDoesNotCount() {
		TLObject foreign = session("foreign", someoneElse);

		_watch.notifyChange(event(List.of(foreign), List.of(foreign), List.of()));

		assertEquals(Integer.valueOf(0), revision());
	}

	/** A deleted chat can no longer be asked whose it was, so any deletion counts. */
	public void testDeletionCounts() {
		TLObject foreign = session("deleted", someoneElse);

		_watch.notifyChange(event(List.of(), List.of(), List.of(foreign)));

		assertEquals(Integer.valueOf(1), revision());
	}

	/** Every counted change moves the count, so two renames in a row are two news. */
	public void testEveryChangeMovesTheCount() {
		TLObject own = session("twice", owner);

		_watch.notifyChange(event(List.of(), List.of(own), List.of()));
		_watch.notifyChange(event(List.of(), List.of(own), List.of()));

		assertEquals(Integer.valueOf(2), revision());
	}

	private Object revision() {
		return _state.get(SessionListWatch.REVISION);
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
