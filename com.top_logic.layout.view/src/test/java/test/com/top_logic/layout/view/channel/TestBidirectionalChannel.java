/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.channel;

import java.util.List;
import java.util.function.Function;

import junit.framework.TestCase;

import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DerivedViewChannel;

/**
 * Tests for bidirectional {@link DerivedViewChannel} with reverse propagation.
 */
public class TestBidirectionalChannel extends TestCase {

	/**
	 * Tests that forward propagation works as before: setting the source updates the derived
	 * channel.
	 */
	public void testForwardPropagation() {
		DefaultViewChannel source = new DefaultViewChannel("source");
		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(
			List.of(source),
			args -> args[0] != null ? "id-" + args[0] : null,
			value -> value != null ? ((String) value).substring(3) : null
		);
		source.set("42");
		assertEquals("id-42", derived.get());
	}

	/**
	 * Tests that setting a value on a bidirectional derived channel propagates it back to the
	 * source via the reverse function.
	 */
	public void testReversePropagation() {
		DefaultViewChannel source = new DefaultViewChannel("source");
		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(
			List.of(source),
			args -> args[0] != null ? "id-" + args[0] : null,
			value -> value != null ? ((String) value).substring(3) : null
		);
		derived.set("id-99");
		assertEquals("99", source.get());
		assertEquals("id-99", derived.get());
	}

	/**
	 * Tests that setting a value on a read-only derived channel (no reverse function) throws
	 * {@link UnsupportedOperationException}.
	 */
	public void testReverseWithoutReverseFunction() {
		DefaultViewChannel source = new DefaultViewChannel("source");
		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(source), args -> args[0] != null ? "id-" + args[0] : null);
		try {
			derived.set("id-99");
			fail("Expected UnsupportedOperationException");
		} catch (UnsupportedOperationException expected) {
			// OK
		}
	}

	/**
	 * Tests that source listeners fire when the derived channel writes back via reverse
	 * propagation.
	 */
	public void testListenerOnReverse() {
		DefaultViewChannel source = new DefaultViewChannel("source");
		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(
			List.of(source),
			args -> args[0] != null ? "id-" + args[0] : null,
			value -> value != null ? ((String) value).substring(3) : null
		);
		boolean[] sourceChanged = {false};
		source.addListener((sender, oldVal, newVal) -> sourceChanged[0] = true);
		derived.set("id-77");
		assertTrue("Source listener must fire on reverse propagation", sourceChanged[0]);
		assertEquals("77", source.get());
	}

	/**
	 * Tests that reverse propagation with multiple inputs targets the first input.
	 */
	public void testReverseWithMultipleInputs() {
		DefaultViewChannel source1 = new DefaultViewChannel("source1");
		DefaultViewChannel source2 = new DefaultViewChannel("source2");
		source1.set("a");
		source2.set("b");

		Function<Object[], Object> forward = args -> args[0] + "-" + args[1];
		Function<Object, Object> reverse = value -> ((String) value).split("-")[0];

		DerivedViewChannel derived = new DerivedViewChannel("derived");
		derived.bind(List.of(source1, source2), forward, reverse);

		assertEquals("a-b", derived.get());

		derived.set("x-b");
		assertEquals("x", source1.get());
		assertEquals("b", source2.get());
		assertEquals("x-b", derived.get());
	}
}
