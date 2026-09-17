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
	 * Tests that a URL carrying no value for the parameter leaves the value the view established, so
	 * that a default selection survives being deep-linked to.
	 */
	public void testAbsentParameterKeepsDefault() {
		ViewChannel channel = new DefaultViewChannel("ticketKey");
		channel.set("default-ticket");
		ParamBindingParticipant participant = new ParamBindingParticipant("ticket", channel);

		// A URL without the segment matches no route of the binding, so nothing is activated on it,
		assertNull(participant.declaredRoutes().get(0).match(""));

		// and the value the view established stands - and names itself in the address bar.
		assertEquals("default-ticket", participant.activeRouteSegment().path());
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

	/**
	 * Tests that a prefix precedes the value in the segment the participant contributes.
	 */
	public void testPrefixPrecedesValue() {
		ViewChannel channel = new DefaultViewChannel("ticketKey");
		ParamBindingParticipant participant = new ParamBindingParticipant("detail", "ticket", channel);

		assertNull("A prefix without a value describes nothing.", participant.activeRouteSegment());

		channel.set("TL-1234");
		assertEquals("detail/TL-1234", participant.activeRouteSegment().path());
	}

	/**
	 * Tests that a URL naming the prefix and a value writes that value into the bound channel.
	 */
	public void testPrefixedUrlWritesChannel() {
		ViewChannel channel = new DefaultViewChannel("ticketKey");
		ParamBindingParticipant participant = new ParamBindingParticipant("detail", "ticket", channel);

		RoutePattern route = participant.declaredRoutes().get(0);
		RouteMatch match = route.match("detail/TL-1234");
		assertNotNull(match);
		participant.activateRoute(match);

		assertEquals("TL-1234", channel.get());
	}

	/**
	 * Tests that a URL carrying the prefix without a value behind it matches nothing, so that it
	 * leaves the value the view established.
	 */
	public void testPrefixWithoutValueMatchesNothing() {
		ViewChannel channel = new DefaultViewChannel("ticketKey");
		channel.set("default-ticket");
		ParamBindingParticipant participant = new ParamBindingParticipant("detail", "ticket", channel);

		assertNull(participant.declaredRoutes().get(0).match("detail"));
		assertEquals("default-ticket", channel.get());
	}

	/**
	 * Tests that a value whose characters have a meaning in a URL survives the way into the address
	 * bar and back.
	 */
	public void testValueRoundTripThroughUrl() {
		ViewChannel channel = new DefaultViewChannel("ticketKey");
		ParamBindingParticipant participant = new ParamBindingParticipant("detail", "ticket", channel);

		channel.set("a/b c");
		String path = participant.activeRouteSegment().path();
		assertEquals("detail/a%2Fb%20c", path);

		ViewChannel opened = new DefaultViewChannel("ticketKey");
		ParamBindingParticipant reopened = new ParamBindingParticipant("detail", "ticket", opened);
		RouteMatch match = reopened.declaredRoutes().get(0).match(path);
		assertNotNull(match);
		reopened.activateRoute(match);

		assertEquals("a/b c", opened.get());
	}

	private static RouteMatch match(String value) {
		return RoutePattern.compile(":ticket", "ticket").match(value);
	}
}
