/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.control.select;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.ModuleTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.react.control.CommandFailures;
import test.com.top_logic.layout.react.control.InteractionWithoutSession;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.basic.util.ResourcesModule;
import com.top_logic.layout.form.model.SimpleSelectFieldModel;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ModelKeyArguments;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.form.FieldValueArguments;
import com.top_logic.layout.react.control.form.ReactFormFieldControl;
import com.top_logic.layout.react.control.identity.ObjectIdTranslation;
import com.top_logic.layout.react.control.identity.ProjectionMode;
import com.top_logic.layout.react.control.select.ReactDropdownSelectControl;
import com.top_logic.layout.react.control.select.SelectDisplay;
import com.top_logic.layout.react.control.select.SelectValueArguments;
import com.top_logic.layout.react.scripting.ScriptRecorder;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.scripting.ScriptingTreeProjector;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.state.DropdownSelectState;
import com.top_logic.layout.react.state.FieldState;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests the ids a {@link ReactDropdownSelectControl} displays its options under: they stay stable
 * while an option is offered, a recorded value change carries the keys of the options instead, and
 * the recorded step replays in a fresh session by the ids the options have there.
 */
public class TestSelectObjectIds extends TestCase {

	private static final List<String> OPTIONS = List.of("percent", "scale", "angle");

	/**
	 * Runs the test within an interaction of a session, as the naming of an object - which asks
	 * the session for script variables naming it - happens in one.
	 */
	@Override
	protected void runTest() throws Throwable {
		InteractionWithoutSession.inSession().runWithContext((ComputationEx2<Void, Throwable, RuntimeException>) () -> {
			super.runTest();
			return null;
		});
	}

	/**
	 * An option keeps its id across repeated loads of the option list; an option that is offered no
	 * more loses its id.
	 */
	public void testOptionIdsStableAcrossLoads() {
		SimpleSelectFieldModel model = new SimpleSelectFieldModel(null, OPTIONS, true);
		Select select = new Select(context(0), model);
		select.loadOptions();
		String percent = select.optionId("percent");
		String angle = select.optionId("angle");

		select.loadOptions();
		assertEquals(percent, select.optionId("percent"));
		assertEquals(angle, select.optionId("angle"));

		model.setOptions(List.of("percent", "scale"));
		select.loadOptions();
		assertEquals(percent, select.optionId("percent"));
		assertNull("An option offered no more loses its id.", select.objectIdentities().objectOf(angle));
	}

	/**
	 * The value keeps the ids of its options when the option list is loaded after the value was
	 * displayed.
	 */
	public void testValueIdKeptWhenOptionsLoaded() {
		Select select = new Select(context(0), new SimpleSelectFieldModel(List.of("scale"), OPTIONS, true));
		List<String> before = select.valueIds();

		select.loadOptions();
		assertEquals(before, select.valueIds());
		assertEquals(List.of(select.optionId("scale")), before);
	}

	/**
	 * The agent projection identifies the options by the ids the value change takes, the key
	 * projection by their keys.
	 */
	public void testProjectionModes() {
		Select select = new Select(context(0), new SimpleSelectFieldModel(List.of("scale"), OPTIONS, true));
		select.loadOptions();

		Map<String, Object> agent = single(select, ProjectionMode.AGENT, FieldState.VALUE__PROP);
		assertEquals(select.optionId("scale"), agent.get(DropdownSelectState.Option.VALUE__PROP));
		assertEquals("scale", agent.get(DropdownSelectState.Option.LABEL__PROP));
		assertFalse(agent.containsKey(ModelKeyArguments.KEY));

		Map<String, Object> key = single(select, ProjectionMode.KEY, FieldState.VALUE__PROP);
		assertNotNull(key.get(ModelKeyArguments.KEY));
		assertFalse(key.containsKey(DropdownSelectState.Option.VALUE__PROP));
	}

	/**
	 * A value change recorded with option ids carries the keys of the options, replays in a fresh
	 * session without its option list being loaded, and selects the same options there.
	 */
	public void testRecordAndReplayInFreshSession() {
		Select recording = new Select(context(0), new SimpleSelectFieldModel(null, OPTIONS, true));
		recording.loadOptions();
		List<String> ids = List.of(recording.optionId("angle"), recording.optionId("percent"));
		ReactCommand step = record(recording, ids);

		assertEquals(ReactFormFieldControl.CMD_VALUE_CHANGED, step.getName());
		List<String> recorded = ((SelectValueArguments) step).getValue();
		assertEquals(2, recorded.size());
		for (String entry : recorded) {
			assertNotNull("A key in place of an id: " + entry, ObjectIdTranslation.parseKey(entry));
			assertFalse(ids.contains(entry));
		}
		assertEquals(List.of("angle", "percent"), recording.model().getValue());

		Select replay = new Select(context(1000), new SimpleSelectFieldModel(null, OPTIONS, true));
		ScriptingSession.forRoot(replay).replay(step);
		assertEquals(List.of("angle", "percent"), replay.model().getValue());
		assertFalse("The ids differ between the sessions.", ids.equals(replay.valueIds()));

		replay.loadOptions();
		assertEquals(List.of(replay.optionId("angle"), replay.optionId("percent")), replay.valueIds());
		assertEquals("Both sessions assert the same value.",
			ScriptingTreeProjector.nodeState(recording).get(FieldState.VALUE__PROP),
			ScriptingTreeProjector.nodeState(replay).get(FieldState.VALUE__PROP));
	}

	/**
	 * Replaying a recorded key of an object that is no option in the replaying session fails, and
	 * the value stays as it is.
	 */
	public void testReplayOfKeyNotAmongOptionsFails() {
		Select recording = new Select(context(0), new SimpleSelectFieldModel(null, OPTIONS, true));
		recording.loadOptions();
		ReactCommand step = record(recording, List.of(recording.optionId("angle")));

		Select replay = new Select(context(1000),
			new SimpleSelectFieldModel(List.of("scale"), List.of("percent", "scale"), true));
		TopLogicException failure = CommandFailures.failure("A key of an object that is no option must fail.",
			() -> ScriptingSession.forRoot(replay).replay(step));
		assertEquals(I18NConstants.ERROR_OBJECT_NOT_DISPLAYED__KEY.getKey(), failure.getErrorKey().plain().getKey());
		assertEquals(List.of("scale"), replay.model().getValue());
	}

	/**
	 * Dispatches a value change with the given option ids as the browser does and returns the
	 * recorded step.
	 */
	private static ReactCommand record(Select select, List<String> ids) {
		ScriptRecorder recorder = new ScriptRecorder();
		recorder.start();
		Map<String, Object> arguments = Map.of(FieldValueArguments.VALUE, ids);
		recorder.capture(select, select, ReactFormFieldControl.CMD_VALUE_CHANGED, arguments);
		select.executeClientCommand(ReactFormFieldControl.CMD_VALUE_CHANGED, arguments);
		recorder.stop();

		List<ReactCommand> steps = recorder.steps();
		assertEquals(1, steps.size());
		return steps.get(0);
	}

	@SuppressWarnings("unchecked")
	private static Map<String, Object> single(Select select, ProjectionMode mode, String property) {
		List<Map<String, Object>> entries =
			(List<Map<String, Object>>) ScriptingTreeProjector.nodeState(select, mode).get(property);
		assertEquals(1, entries.size());
		return entries.get(0);
	}

	/** A context of a session that has allocated the given number of ids before. */
	private static ReactContext context(int allocated) {
		ReactContext result =
			new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
		for (int n = 0; n < allocated; n++) {
			result.allocateId();
		}
		return result;
	}

	/**
	 * A {@link ReactDropdownSelectControl} over string options, exposing the ids of its state.
	 */
	private static final class Select extends ReactDropdownSelectControl {

		private final SimpleSelectFieldModel _model;

		Select(ReactContext context, SimpleSelectFieldModel model) {
			super(context, model, String::valueOf, null, false, SelectDisplay.DROPDOWN);
			_model = model;
		}

		SimpleSelectFieldModel model() {
			return _model;
		}

		void loadOptions() {
			executeClientCommand(CMD_LOAD_OPTIONS, Map.of());
		}

		/** The ids of the descriptors the state sends as the value. */
		List<String> valueIds() {
			List<String> ids = new ArrayList<>();
			for (Map<?, ?> descriptor : descriptors(FieldState.VALUE__PROP)) {
				ids.add((String) descriptor.get(DropdownSelectState.Option.VALUE__PROP));
			}
			return ids;
		}

		/** The id of the option with the given label. */
		String optionId(String label) {
			for (Map<?, ?> descriptor : descriptors(DropdownSelectState.OPTIONS__PROP)) {
				if (label.equals(descriptor.get(DropdownSelectState.Option.LABEL__PROP))) {
					return (String) descriptor.get(DropdownSelectState.Option.VALUE__PROP);
				}
			}
			throw new AssertionError("No option labelled '" + label + "'.");
		}

		@SuppressWarnings("unchecked")
		private List<Map<?, ?>> descriptors(String key) {
			Object state = getState(key);
			return state == null ? List.of() : (List<Map<?, ?>>) state;
		}
	}

	/**
	 * The test suite, started with the resource bundles the controls need and the
	 * {@link ModelResolver} naming the options.
	 */
	public static Test suite() {
		return ModuleTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestSelectObjectIds.class, TypeIndex.Module.INSTANCE,
				ThreadContextManager.Module.INSTANCE, ResourcesModule.Module.INSTANCE, ModelResolver.Module.INSTANCE));
	}

}
