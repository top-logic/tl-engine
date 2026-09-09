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

/**
 * Tests for {@link RowSourceObserver}.
 *
 * <p>
 * The elements are plain strings and no type is observed, so the observation needs no
 * {@link com.top_logic.model.listen.ModelScope} of a running model.
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
