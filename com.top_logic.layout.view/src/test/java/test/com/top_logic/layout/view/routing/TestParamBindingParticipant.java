/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.routing;

import junit.framework.TestCase;

import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.react.routing.RoutePattern;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DerivedViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.routing.ParamBindingParticipant;

/**
 * Tests for {@link ParamBindingParticipant}.
 */
public class TestParamBindingParticipant extends TestCase {

	/**
	 * Tests that the value the channel holds becomes the participant's segment.
	 */
	public void testChannelValueIsSegment() {
		ViewChannel channel = new DefaultViewChannel("ticketKey");
		ParamBindingParticipant participant = new ParamBindingParticipant("ticket", channel);

		assertNull("An empty channel contributes no segment.", participant.activeRouteSegment());

		channel.set("TL-1234");
		assertEquals("TL-1234", participant.activeRouteSegment().path());

		channel.set(null);
		assertNull("A cleared channel contributes no segment.", participant.activeRouteSegment());
	}

	/**
	 * Tests that a deep link writes its parameter value into the bound channel.
	 */
	public void testRouteActivationWritesChannel() {
		ViewChannel channel = new DefaultViewChannel("ticketKey");
		ParamBindingParticipant participant = new ParamBindingParticipant("ticket", channel);

		participant.activateRoute(match("TL-1234"));

		assertEquals("TL-1234", channel.get());
	}

	/**
	 * Tests that a segment which the binding resolves to nothing contributes no segment, so that the
	 * address bar does not keep an object that is not displayed.
	 */
	public void testUnresolvedSegmentIsDropped() {
		// A binding that resolves a key to an object and maps an object back to its key - here a
		// single known object, standing for the objects a view can display.
		ViewChannel selection = new DefaultViewChannel("ticket");
		DerivedViewChannel key = new DerivedViewChannel("ticketKey");
		key.bind(java.util.List.of(selection),
			args -> "known".equals(args[0]) ? "known" : null,
			value -> "known".equals(value) ? "known" : null);

		ParamBindingParticipant participant = new ParamBindingParticipant("ticket", key);

		participant.activateRoute(match("known"));
		assertEquals("known", participant.activeRouteSegment().path());

		participant.activateRoute(match("deleted"));
		assertNull("A key resolving to no object contributes no segment.",
			participant.activeRouteSegment());
	}

	private static RouteMatch match(String value) {
		return RoutePattern.compile(":ticket", "ticket").match(value);
	}
}
