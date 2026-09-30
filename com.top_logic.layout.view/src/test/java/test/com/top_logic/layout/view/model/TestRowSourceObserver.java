/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.model.RowSourceObserver;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
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
