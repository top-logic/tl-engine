/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.react.scripting;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;
import test.com.top_logic.layout.react.control.InteractionWithoutSession;

import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.json.JsonBinding;
import com.top_logic.basic.config.json.SimpleJsonListBinding;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ComputationEx2;
import com.top_logic.gui.ThemeFactory;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ModelKeyArguments;
import com.top_logic.layout.react.control.ReactCommand;
import com.top_logic.layout.react.control.ReactCommandHandler;
import com.top_logic.layout.react.control.ReactCommands;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.ScriptingModelKey;
import com.top_logic.layout.react.control.identity.DisplayedObjectId;
import com.top_logic.layout.react.control.identity.ObjectIdIndex;
import com.top_logic.layout.react.control.identity.ObjectIdTranslation;
import com.top_logic.layout.react.control.identity.ObjectIdentities;
import com.top_logic.layout.react.control.identity.ProjectionMode;
import com.top_logic.layout.react.scripting.ScriptRecorder;
import com.top_logic.layout.react.scripting.ScriptingSession;
import com.top_logic.layout.react.scripting.ScriptingTreeProjector;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.scripting.recorder.ref.ModelResolver;
import com.top_logic.util.error.TopLogicException;

/**
 * Tests the translation of the {@link DisplayedObjectId object ids} of a command into business keys
 * when it is recorded, and back into the ids of the replaying control when it is replayed, and the
 * identification of displayed objects in the two {@link ProjectionMode projection modes}.
 */
public class TestDisplayedObjectIds extends TestCase {

	private static final List<Object> ITEMS = List.of("a", "b", "c");

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
	 * A recorded command carries the keys of the objects in its id properties and replays in a
	 * control whose ids differ, receiving the ids of that control.
	 */
	public void testRecordAndReplayInFreshControl() {
		ItemsControl recording = new ItemsControl(context(0), ITEMS, true);
		ReactCommand step = record(recording, "b", List.of("a", "c"));

		PickArguments recorded = (PickArguments) step;
		assertEquals(ScriptingModelKey.toJson(null, "b"), recorded.getItem());
		assertEquals(List.of(ScriptingModelKey.toJson(null, "a"), ScriptingModelKey.toJson(null, "c")),
			recorded.getOthers());
		assertEquals("/testItems[items]", step.getAddress());

		ItemsControl replay = new ItemsControl(context(1000), ITEMS, true);
		assertFalse("The ids differ between the controls.", recording.id("b").equals(replay.id("b")));
		ScriptingSession.forRoot(replay).replay(step);

		assertEquals(replay.id("b"), replay._received.getItem());
		assertEquals(List.of(replay.id("a"), replay.id("c")), replay._received.getOthers());
		assertEquals("The recorded step is not changed by the replay.", ScriptingModelKey.toJson(null, "b"),
			recorded.getItem());
	}

	/**
	 * A recorded list of keys survives the wire form of a step - its arguments as JSON, bound back
	 * into the argument interface - also keys containing commas, and replays.
	 */
	public void testListOfKeysRoundTripsThroughWireForm() throws JSON.ParseException {
		List<Object> items = List.of("a", "x, y", "b,c");
		ReactCommand step = record(new ItemsControl(context(0), items, true), "a", List.of("x, y", "b,c"));
		List<String> keys = ((PickArguments) step).getOthers();
		assertEquals(List.of(ScriptingModelKey.toJson(null, "x, y"), ScriptingModelKey.toJson(null, "b,c")), keys);
		for (String key : keys) {
			assertTrue("The key contains a comma: " + key, key.contains(","));
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> wire =
			(Map<String, Object>) JSON.fromString(JSON.toString(ReactCommands.arguments(step)));
		ItemsControl replay = new ItemsControl(context(1000), items, true);
		ReactCommand rebound = replay.commandItem(ItemsControl.PICK, wire);
		rebound.setAddress(step.getAddress());
		assertEquals(keys, ((PickArguments) rebound).getOthers());

		ScriptingSession.forRoot(replay).replay(rebound);
		assertEquals(List.of(replay.id("x, y"), replay.id("b,c")), replay._received.getOthers());
	}

	/**
	 * A key designating an object the replaying control does not display fails the step.
	 */
	public void testReplayOfObjectNotAtHandFails() {
		ReactCommand step = record(new ItemsControl(context(0), ITEMS, true), "b", List.of());

		ItemsControl withoutB = new ItemsControl(context(1000), List.of("a", "c"), true);
		try {
			ScriptingSession.forRoot(withoutB).replay(step);
			fail("A key of an object the control does not display must fail.");
		} catch (TopLogicException expected) {
			// Expected.
		}
		assertNull("The command is not dispatched.", withoutB._received);
	}

	/**
	 * An object without a key is recorded by its id, which replays as it was recorded.
	 */
	public void testObjectWithoutKeyRecordedById() {
		Object nameless = new Object();
		ItemsControl control = new ItemsControl(context(0), List.of("a", nameless), true);
		ReactCommand step = record(control, nameless, List.of());

		assertEquals(control.id(nameless), ((PickArguments) step).getItem());
		ScriptingSession.forRoot(control).replay(step);
		assertEquals(control.id(nameless), control._received.getItem());
	}

	/**
	 * A control without object identities records and replays its arguments as they are.
	 */
	public void testControlWithoutIdentitiesRecordsVerbatim() {
		ItemsControl control = new ItemsControl(context(0), ITEMS, false);
		ReactCommand step = record(control, "b", List.of());

		assertEquals(control.id("b"), ((PickArguments) step).getItem());
		ScriptingSession.forRoot(control).replay(step);
		assertEquals(control.id("b"), control._received.getItem());
	}

	/**
	 * The description of a recorded step renders a key through the label of its object, an id as
	 * it is.
	 */
	public void testKeyLabels() {
		ReactCommand step = record(new ItemsControl(context(0), ITEMS, true), "b", List.of("a"));

		PickArguments labeled =
			(PickArguments) ObjectIdTranslation.withKeyLabels(step, key -> "label of " + key.getClass().getSimpleName());
		assertTrue(labeled.getItem(), labeled.getItem().startsWith("label of "));
		assertTrue(labeled.getOthers().get(0).startsWith("label of "));
		assertEquals("The recorded step is not changed.", ScriptingModelKey.toJson(null, "b"),
			((PickArguments) step).getItem());
	}

	/**
	 * The agent projection identifies the objects of a control offering object identities by their
	 * ids, the key projection by their keys; a control without identities is projected by keys in
	 * both modes.
	 */
	public void testProjectionModes() {
		ItemsControl control = new ItemsControl(context(0), ITEMS, true);

		Map<String, Object> agent = item(control, ProjectionMode.AGENT, 1);
		assertEquals(control.id("b"), agent.get(ItemsControl.ID));
		assertFalse(agent.containsKey(ModelKeyArguments.KEY));

		Map<String, Object> key = item(control, ProjectionMode.KEY, 1);
		assertEquals(ScriptingModelKey.toKey(null, "b"), key.get(ModelKeyArguments.KEY));
		assertFalse(key.containsKey(ItemsControl.ID));
		assertEquals("Assertions are captured in the key form.", key,
			items(ScriptingTreeProjector.nodeState(control)).get(1));

		assertTrue(ScriptingSession.forRoot(control).observeJson().contains(control.id("b")));
		assertFalse(ScriptingSession.forRoot(control).observeJson(ProjectionMode.KEY).contains(control.id("b")));

		ItemsControl unconverted = new ItemsControl(context(0), ITEMS, false);
		assertEquals(ScriptingModelKey.toKey(null, "b"),
			item(unconverted, ProjectionMode.AGENT, 1).get(ModelKeyArguments.KEY));
	}

	private static Map<String, Object> item(ItemsControl control, ProjectionMode mode, int index) {
		return items(ScriptingTreeProjector.nodeState(control, mode)).get(index);
	}

	@SuppressWarnings("unchecked")
	private static List<Map<String, Object>> items(Map<String, Object> state) {
		return (List<Map<String, Object>>) state.get(ItemsControl.ITEMS);
	}

	/**
	 * Dispatches the pick of the given objects as the browser does and returns the recorded step.
	 */
	private static ReactCommand record(ItemsControl control, Object item, List<Object> others) {
		ScriptRecorder recorder = new ScriptRecorder();
		recorder.start();
		Map<String, Object> arguments = new LinkedHashMap<>();
		arguments.put(PickArguments.ITEM, control.id(item));
		List<String> otherIds = new ArrayList<>();
		for (Object other : others) {
			otherIds.add(control.id(other));
		}
		arguments.put(PickArguments.OTHERS, otherIds);
		recorder.capture(control, control, ItemsControl.PICK, arguments);
		control.executeClientCommand(ItemsControl.PICK, arguments);
		recorder.stop();

		List<ReactCommand> steps = recorder.steps();
		assertEquals(1, steps.size());
		return steps.get(0);
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
	 * Arguments of {@link ItemsControl#PICK}: the ids of displayed objects.
	 */
	public interface PickArguments extends ReactCommand {

		/** @see #getItem() */
		String ITEM = "item";

		/** @see #getOthers() */
		String OTHERS = "others";

		/**
		 * The id of the picked object.
		 */
		@Name(ITEM)
		@Nullable
		@DisplayedObjectId
		String getItem();

		/**
		 * The ids of further objects.
		 */
		@Name(OTHERS)
		@JsonBinding(SimpleJsonListBinding.class)
		@DisplayedObjectId
		List<String> getOthers();
	}

	/**
	 * A control displaying objects under ids of an {@link ObjectIdIndex}, taking them by id.
	 */
	private static final class ItemsControl extends ReactControl {

		static final String PICK = "pick";

		static final String ITEMS = "items";

		static final String ID = "id";

		private final List<Object> _items;

		private final ObjectIdIndex _index;

		private final boolean _offersIdentities;

		PickArguments _received;

		ItemsControl(ReactContext context, List<Object> items, boolean offersIdentities) {
			super(context, null, "TLTestItems");
			_items = items;
			_index = new ObjectIdIndex(context::allocateId);
			_index.setObjects(items);
			_offersIdentities = offersIdentities;
		}

		@Override
		public String scriptingName() {
			return ITEMS;
		}

		@Override
		public ObjectIdentities objectIdentities() {
			return _offersIdentities ? _index : null;
		}

		@ReactCommandHandler(PICK)
		void pick(PickArguments arguments) {
			_received = arguments;
		}

		@Override
		public Map<String, Object> scriptingScalarState(ProjectionMode mode) {
			Map<String, Object> result = super.scriptingScalarState(mode);
			List<Map<String, Object>> entries = new ArrayList<>();
			for (Object item : _items) {
				Map<String, Object> entry = new LinkedHashMap<>();
				ScriptingModelKey.putIdentity(entry, mode, objectIdentities(), null, item, ID, _index.idOf(item));
				entries.add(entry);
			}
			result.put(ITEMS, entries);
			return result;
		}

		String id(Object item) {
			return _index.idOf(item);
		}
	}

	/**
	 * The test suite, run on a knowledge base, with the {@link ModelResolver} naming the displayed
	 * objects.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestDisplayedObjectIds.class,
			ServiceTestSetup.createStarterFactoryForModules(ThreadContextManager.Module.INSTANCE,
				ThemeFactory.Module.INSTANCE, ModelResolver.Module.INSTANCE));
	}

}
