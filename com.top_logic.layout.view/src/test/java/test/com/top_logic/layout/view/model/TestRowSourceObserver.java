/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.listen.ModelChangeEvent;
import com.top_logic.model.util.TLModelUtil;

/**
 * Tests for {@link RowSourceObserver}.
 *
 * <p>
 * The elements are plain strings or transient objects and no type is observed, so the observation
 * needs no {@link com.top_logic.model.listen.ModelScope} of a running model.
 * </p>
 */
public class TestRowSourceObserver extends TestCase {

	/**
	 * The elements delivered to the sink, in the order they were delivered.
	 */
	private final List<List<String>> _delivered = new ArrayList<>();

	/**
	 * Tests that a change of an input channel while the observer observes reaches the sink.
	 */
	public void testChannelChangeIsObserved() {
		ViewChannel filter = new DefaultViewChannel("filter");
		RowSourceObserver<String> observer = observer(filter, List.of("a", "ab", "b"));
		observer.attach(null);
		_delivered.clear();

		filter.set("a");

		assertEquals(List.of(List.of("a", "ab")), _delivered);
	}

	/**
	 * Tests that an input channel written before the observation begins reaches the sink when it
	 * begins - the case of a channel bound to the URL taking up the value a deep link carries while
	 * the display is still being built.
	 */
	public void testChannelChangeBeforeAttachIsCaughtUp() {
		ViewChannel filter = new DefaultViewChannel("filter");
		RowSourceObserver<String> observer = observer(filter, List.of("a", "ab", "b"));

		// Nobody is listening yet: the display the elements belong to is not attached.
		filter.set("b");
		assertEquals("Nothing is delivered before the observation begins.", List.of(), _delivered);

		observer.attach(null);

		assertEquals(List.of(List.of("ab", "b")), _delivered);
	}

	/**
	 * Tests that beginning the observation delivers nothing where the elements are the ones at hand,
	 * so that a display showing them has nothing to rebuild.
	 */
	public void testUnchangedElementsAreNotDelivered() {
		ViewChannel filter = new DefaultViewChannel("filter");
		RowSourceObserver<String> observer = observer(filter, List.of("a", "ab", "b"));

		observer.attach(null);

		assertEquals(List.of(), _delivered);
	}

	/**
	 * Tests that a change missed while the observation was suspended is caught up when it begins
	 * again - a display nobody looked at ignores every change.
	 */
	public void testChangeWhileDetachedIsCaughtUp() {
		ViewChannel filter = new DefaultViewChannel("filter");
		RowSourceObserver<String> observer = observer(filter, List.of("a", "ab", "b"));
		observer.attach(null);
		observer.detach();
		_delivered.clear();

		filter.set("a");
		assertEquals("A suspended observation delivers nothing.", List.of(), _delivered);

		observer.attach(null);

		assertEquals(List.of(List.of("a", "ab")), _delivered);
	}

	/**
	 * Tests that an observation resuming after it was stopped delivers the elements it reads, even
	 * where they are the ones the display holds: what those objects went through while nobody
	 * followed them is unknown, so the display is rebuilt from them.
	 */
	public void testResumedObservationDeliversTheElements() {
		ViewChannel filter = new DefaultViewChannel("filter");
		RowSourceObserver<String> observer = observer(filter, List.of("a", "ab", "b"));
		observer.attach(null);
		observer.detach();
		_delivered.clear();

		observer.attach(null);

		assertEquals("The unchanged elements are delivered once when the observation resumes.",
			List.of(List.of("a", "ab", "b")), _delivered);
	}

	/**
	 * Tests that an element without a persistent identity is displayed and refreshed like any
	 * other: a transient object has no changes anyone could be notified of, so the observation
	 * passes it by instead of asking it for an identity it does not have.
	 */
	public void testTransientElementsAreNotObserved() {
		TLObject transientElement = transientObject();
		List<Object> elements = List.of(transientElement);
		List<List<Object>> delivered = new ArrayList<>();
		ViewChannel hide = new DefaultViewChannel("hide");
		RowSourceObserver<Object> observer = new RowSourceObserver<>(elements,
			args -> args[0] == null ? elements : List.of(), Set.of(), List.of(hide), delivered::add);

		observer.attach(null);

		assertEquals("The elements are the ones at hand, so nothing is delivered.", List.of(), delivered);

		hide.set("all");

		assertEquals("The channel change reaches the sink, the transient element notwithstanding.",
			List.of(List.of()), delivered);
	}

	/**
	 * Tests that a change of a row-independent input does not call the element function, delivers
	 * nothing and runs the inputs-changed handler.
	 */
	public void testRowIndependentInputChangeOnlyRunsTheHandler() {
		ViewChannel filter = new DefaultViewChannel("filter");
		ViewChannel selection = new DefaultViewChannel("selection");
		int[] calls = { 0 };
		int[] handled = { 0 };
		List<String> elements = List.of("a", "ab", "b");
		RowSourceObserver<String> observer = new RowSourceObserver<>(elements, args -> {
			calls[0]++;
			return filtered(elements, args[0]);
		}, Set.of(), List.of(filter, selection), _delivered::add)
			.withRowIndependentInputs(List.of(selection))
			.onInputsChanged(List.of(), () -> handled[0]++);
		observer.attach(null);
		calls[0] = 0;

		selection.set("ab");

		assertEquals("The element function is not called.", 0, calls[0]);
		assertEquals("Nothing is delivered.", List.of(), _delivered);
		assertEquals("The handler runs once.", 1, handled[0]);
	}

	/**
	 * Tests that the element function receives the values of all inputs in their declared order,
	 * the row-independent ones included.
	 */
	public void testElementFunctionReceivesRowIndependentInputs() {
		ViewChannel selection = new DefaultViewChannel("selection");
		ViewChannel filter = new DefaultViewChannel("filter");
		List<Object[]> arguments = new ArrayList<>();
		List<String> elements = List.of("a", "ab", "b");
		RowSourceObserver<String> observer = new RowSourceObserver<>(elements, args -> {
			arguments.add(args);
			return filtered(elements, args[1]);
		}, Set.of(), List.of(selection, filter), _delivered::add)
			.withRowIndependentInputs(List.of(selection))
			.onInputsChanged(List.of(filter), () -> {
				// Nothing to refresh.
			});
		observer.attach(null);
		selection.set("ab");
		arguments.clear();

		filter.set("b");

		assertEquals(1, arguments.size());
		assertEquals(List.of("ab", "b"), List.of(arguments.get(0)));
		assertEquals(List.of(List.of("ab", "b")), _delivered);
	}

	/**
	 * Tests that a change of another input yielding the elements at hand delivers nothing and runs
	 * the inputs-changed handler.
	 */
	public void testEqualElementsRunOnlyTheHandler() {
		ViewChannel filter = new DefaultViewChannel("filter");
		int[] handled = { 0 };
		RowSourceObserver<String> observer =
			observer(filter, List.of("a", "ab", "b")).onInputsChanged(List.of(filter), () -> handled[0]++);
		observer.attach(null);

		// Every element contains the empty term, so the elements stay.
		filter.set("");

		assertEquals("Nothing is delivered.", List.of(), _delivered);
		assertEquals("The handler runs once.", 1, handled[0]);
	}

	/**
	 * Tests that a change of another input yielding other elements delivers them, and does not run
	 * the inputs-changed handler.
	 */
	public void testChangedElementsAreDeliveredWithAHandler() {
		ViewChannel filter = new DefaultViewChannel("filter");
		int[] handled = { 0 };
		RowSourceObserver<String> observer =
			observer(filter, List.of("a", "ab", "b")).onInputsChanged(List.of(filter), () -> handled[0]++);
		observer.attach(null);

		filter.set("a");

		assertEquals(List.of(List.of("a", "ab")), _delivered);
		assertEquals("The handler does not run.", 0, handled[0]);
	}

	/**
	 * Tests that a change of an input not declared to the inputs-changed handler delivers the
	 * elements even where they equal the ones at hand: what the display builds from them may read
	 * that input.
	 */
	public void testEqualElementsOfAnUndeclaredInputAreDelivered() {
		ViewChannel filter = new DefaultViewChannel("filter");
		ViewChannel other = new DefaultViewChannel("other");
		int[] handled = { 0 };
		List<String> elements = List.of("a", "ab", "b");
		RowSourceObserver<String> observer = new RowSourceObserver<>(elements,
			args -> filtered(elements, args[0]), Set.of(), List.of(filter, other), _delivered::add)
				.onInputsChanged(List.of(filter), () -> handled[0]++);
		observer.attach(null);

		other.set("x");

		assertEquals(List.of(List.of("a", "ab", "b")), _delivered);
		assertEquals("The handler does not run.", 0, handled[0]);
	}

	/**
	 * Tests that without an inputs-changed handler a change of an input delivers the elements even
	 * where they equal the ones at hand.
	 */
	public void testEqualElementsAreDeliveredWithoutAHandler() {
		ViewChannel filter = new DefaultViewChannel("filter");
		RowSourceObserver<String> observer = observer(filter, List.of("a", "ab", "b"));
		observer.attach(null);

		filter.set("");

		assertEquals(List.of(List.of("a", "ab", "b")), _delivered);
	}

	/**
	 * Tests that a model change delivers the elements even where they equal the ones at hand: the
	 * displayed objects themselves may have changed.
	 */
	public void testModelChangeDeliversEqualElements() {
		ViewChannel filter = new DefaultViewChannel("filter");
		int[] handled = { 0 };
		RowSourceObserver<String> observer =
			observer(filter, List.of("a", "ab", "b")).onInputsChanged(List.of(filter), () -> handled[0]++);
		observer.attach(null);

		observer.notifyChange(NoChange.INSTANCE);

		assertEquals(List.of(List.of("a", "ab", "b")), _delivered);
		assertEquals("The handler does not run.", 0, handled[0]);
	}

	/**
	 * A model change reporting no object, as the observer receives it from its scope: the observer
	 * re-evaluates on every notification.
	 */
	private enum NoChange implements ModelChangeEvent {

		/** The sole instance. */
		INSTANCE;

		@Override
		public ChangeType getChange(TLObject existingObject) {
			return ChangeType.NONE;
		}

		@Override
		public Stream<? extends TLObject> getUpdated() {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getUpdated(TLStructuredType type) {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getCreated() {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getCreated(TLStructuredType type) {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getDeleted() {
			return Stream.empty();
		}

		@Override
		public Stream<? extends TLObject> getDeleted(TLStructuredType type) {
			return Stream.empty();
		}
	}

	/**
	 * An object that lives only in the display holding it.
	 */
	private static TLObject transientObject() {
		TLModelImpl model = new TLModelImpl();
		TLModule module = TLModelUtil.addModule(model, "test.rowSourceObserver");
		TLClass type = TLModelUtil.addClass(module, "Row");
		return TransientObjectFactory.INSTANCE.createObject(type, null);
	}

	/**
	 * An observer over the given elements, keeping those that contain the value of the given channel,
	 * delivering into {@link #_delivered}.
	 */
	private RowSourceObserver<String> observer(ViewChannel filter, List<String> elements) {
		return new RowSourceObserver<>(elements, args -> filtered(elements, args[0]), Set.of(),
			List.of(filter), _delivered::add);
	}

	/**
	 * The given elements containing the given term, all of them for no term.
	 */
	private static List<String> filtered(List<String> elements, Object term) {
		if (term == null || term.toString().isEmpty()) {
			return elements;
		}
		return elements.stream().filter(element -> element.contains(term.toString())).toList();
	}

}
