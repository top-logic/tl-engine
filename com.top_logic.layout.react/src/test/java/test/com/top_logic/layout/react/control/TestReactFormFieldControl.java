/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.form.model.AbstractFieldModel;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.protocol.PatchEvent;
import com.top_logic.layout.react.protocol.SSEEvent;
import com.top_logic.layout.react.protocol.StateEvent;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.model.listen.ModelScope;

/**
 * Tests the dispatch-origin-dependent echo behavior of {@link ReactFormFieldControl}.
 *
 * <p>
 * A {@code valueChanged} dispatched by the browser client is not echoed back — the client holds
 * the value optimistically, and a late echo would overwrite newer keystrokes. The same command
 * dispatched programmatically (script replay, headless agent) must be pushed, since no browser
 * has anticipated it. In both cases the server-side state must reflect the applied value, so
 * later renders and agent observations see it.
 * </p>
 */
public class TestReactFormFieldControl extends TestCase {

	private CapturingQueue _queue;

	private AbstractFieldModel _model;

	private TextControl _field;

	@Override
	protected void setUp() throws Exception {
		_queue = new CapturingQueue();
		_model = new AbstractFieldModel("");
		_field = new TextControl(new TestReactContext(_queue), _model);
		// Render, so state changes produce patch events instead of pre-render state updates.
		_field.write(new TagWriter());
		_queue.clear();
	}

	/**
	 * A replayed or agent-dispatched value change reaches the client: the handler records it as a
	 * silent change (the client-already-knows assumption), and the framework compensates by
	 * resending the control state after the command — the UI update gap that made a replayed text
	 * edit change only the server-side value.
	 */
	public void testProgrammaticValueChangeIsPushed() {
		_field.executeCommand("valueChanged", Map.of("value", "replayed"));

		assertEquals("replayed", _model.getValue());
		assertEquals("replayed", _field.state("value"));
		StateEvent state = _queue.singleEvent(StateEvent.class);
		assertEquals(_field.getID(), state.getControlId());
		assertTrue(state.getState(), state.getState().contains("replayed"));
	}

	/**
	 * A client-dispatched value change is not echoed back, but the server-side state still
	 * reflects the applied value.
	 */
	public void testClientValueChangeIsRecordedButNotEchoed() {
		_field.executeClientCommand("valueChanged", Map.of("value", "typed"));

		assertEquals("typed", _model.getValue());
		assertEquals("The server-side state must reflect the applied client value.",
			"typed", _field.state("value"));
		assertEquals("No echo of the value the client already holds.",
			List.of(), _queue.events());
	}

	/**
	 * A model change during client dispatch that differs from the sent value (a server coercion or
	 * a dependent change) is still pushed.
	 */
	public void testDivergingModelValueIsEchoedToClient() {
		_model.addListener(new com.top_logic.layout.form.model.FieldModelListener() {
			@Override
			public void onValueChanged(com.top_logic.layout.form.model.FieldModel source,
					Object oldValue, Object newValue) {
				if ("raw".equals(newValue)) {
					source.setValue("coerced");
				}
			}

			@Override
			public void onValidationChanged(com.top_logic.layout.form.model.FieldModel source) {
				// Value coercion only.
			}

			@Override
			public void onEditabilityChanged(com.top_logic.layout.form.model.FieldModel source,
					boolean editable) {
				// Value coercion only.
			}
		});

		_field.executeClientCommand("valueChanged", Map.of("value", "raw"));

		assertEquals("coerced", _model.getValue());
		assertEquals("coerced", _field.state("value"));
		assertTrue("The coerced value differs from the sent one and must reach the client.",
			_queue.singleEvent(PatchEvent.class).getPatch().contains("coerced"));
	}

	/**
	 * A text-field control exposing its server-side state for assertions.
	 */
	private static final class TextControl extends ReactFormFieldControl {

		TextControl(ReactContext context, AbstractFieldModel model) {
			super(context, model, "TLTextInput");
		}

		Object state(String key) {
			return getState(key);
		}
	}

	/**
	 * An {@link SSEUpdateQueue} that captures enqueued events for assertions instead of flushing
	 * them to a connection.
	 */
	private static final class CapturingQueue extends SSEUpdateQueue {

		private final List<SSEEvent> _events = new ArrayList<>();

		@Override
		public void enqueue(SSEEvent event) {
			_events.add(event);
		}

		List<SSEEvent> events() {
			return _events;
		}

		void clear() {
			_events.clear();
		}

		<T extends SSEEvent> T singleEvent(Class<T> type) {
			assertEquals("Expected exactly one event: " + _events, 1, _events.size());
			return type.cast(_events.get(0));
		}
	}

	/**
	 * A minimal {@link ReactContext} backed only by an {@link SSEUpdateQueue}, with no application
	 * services.
	 */
	private static final class TestReactContext implements ReactContext {

		private final SSEUpdateQueue _sseQueue;

		TestReactContext(SSEUpdateQueue queue) {
			_sseQueue = queue;
		}

		@Override
		public String allocateId() {
			return _sseQueue.allocateId();
		}

		@Override
		public String getWindowName() {
			return "testWindow";
		}

		@Override
		public String getContextPath() {
			return "";
		}

		@Override
		public SSEUpdateQueue getSSEQueue() {
			return _sseQueue;
		}

		@Override
		public ReactWindowRegistry getWindowRegistry() {
			return null;
		}

		@Override
		public ModelScope getModelScope() {
			return null;
		}
	}
}
