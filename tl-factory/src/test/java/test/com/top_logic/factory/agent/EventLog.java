/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.agent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import junit.framework.Assert;

import com.top_logic.tl.ai.core.agent.StopReason;
import com.top_logic.tl.ai.core.agent.event.AgentEvent;
import com.top_logic.tl.ai.core.agent.event.AgentEventListener;

/**
 * {@link AgentEventListener} recording the events of a run, like the chat checking that they
 * arrive on the thread that created it.
 */
public class EventLog implements AgentEventListener {

	private final Thread _owner = Thread.currentThread();

	private final List<AgentEvent> _events = new ArrayList<>();

	private Consumer<AgentEvent> _observer = event -> {
		// Nothing.
	};

	/**
	 * Calls the given observer for every event, after it is recorded.
	 */
	public EventLog observe(Consumer<AgentEvent> observer) {
		_observer = observer;
		return this;
	}

	@Override
	public void onEvent(AgentEvent event) {
		Assert.assertSame("Event on another thread: " + event, _owner, Thread.currentThread());
		_events.add(event);
		_observer.accept(event);
	}

	/**
	 * All events.
	 */
	public List<AgentEvent> events() {
		return _events;
	}

	/**
	 * The events of the given type.
	 */
	public <E extends AgentEvent> List<E> of(Class<E> type) {
		return _events.stream().filter(type::isInstance).map(type::cast).toList();
	}

	/**
	 * The text streamed by the run.
	 */
	public String text() {
		StringBuilder result = new StringBuilder();
		for (AgentEvent.TextDelta delta : of(AgentEvent.TextDelta.class)) {
			result.append(delta.text());
		}
		return result.toString();
	}

	/**
	 * The last event, which must complete the run, and the first, which must start it.
	 */
	public AgentEvent.RunCompleted completion() {
		Assert.assertFalse("No events.", _events.isEmpty());
		Assert.assertTrue("Not started: " + _events, _events.get(0) instanceof AgentEvent.RunStarted);
		AgentEvent last = _events.get(_events.size() - 1);
		Assert.assertTrue("Not completed: " + _events, last instanceof AgentEvent.RunCompleted);
		Assert.assertEquals("Completed more than once: " + _events, 1, of(AgentEvent.RunCompleted.class).size());
		Assert.assertEquals("Started more than once: " + _events, 1, of(AgentEvent.RunStarted.class).size());
		return (AgentEvent.RunCompleted) last;
	}

	/**
	 * The stop reason of the completed run.
	 */
	public StopReason reason() {
		return completion().reason();
	}

}
