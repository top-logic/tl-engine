/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.routing;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.layout.react.routing.RouteSegment;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.routing.QueryBindingParticipant;

/**
 * Tests for {@link QueryBindingParticipant}.
 */
public class TestQueryBindingParticipant extends TestCase {

	/**
	 * Tests that the value the channel holds becomes the value of the query parameter, while the
	 * binding occupies no path segment.
	 */
	public void testChannelValueIsQueryParameter() {
		ViewChannel channel = new DefaultViewChannel("filter");
		QueryBindingParticipant participant = new QueryBindingParticipant("q", channel);

		assertNull("An empty channel contributes no parameter.", participant.activeRouteSegment());

		channel.set("urgent");
		RouteSegment segment = participant.activeRouteSegment();
		assertEquals("The binding occupies no path segment.", "", segment.path());
		assertEquals(Map.of("q", "urgent"), segment.queryParams());
	}

	/**
	 * Tests that a channel holding nothing to describe leaves the parameter out of the URL.
	 */
	public void testEmptyValueContributesNothing() {
		ViewChannel channel = new DefaultViewChannel("filter");
		QueryBindingParticipant participant = new QueryBindingParticipant("q", channel);

		channel.set("urgent");
		channel.set("");
		assertNull("An emptied filter is no filter.", participant.activeRouteSegment());

		channel.set("urgent");
		channel.set(null);
		assertNull("A cleared channel contributes no parameter.", participant.activeRouteSegment());
	}

	/**
	 * Tests that the binding declares no route, so that nothing of the path is consumed by it.
	 */
	public void testNoRouteIsDeclared() {
		ViewChannel channel = new DefaultViewChannel("filter");
		QueryBindingParticipant participant = new QueryBindingParticipant("q", channel);

		assertEquals(List.of(), participant.declaredRoutes());
	}

	/**
	 * Tests that the query of an opened URL writes the value of the bound parameter into the channel
	 * and ignores the parameters of everything else the URL carries.
	 */
	public void testQueryActivationWritesChannel() {
		ViewChannel channel = new DefaultViewChannel("filter");
		QueryBindingParticipant participant = new QueryBindingParticipant("q", channel);

		participant.activateQuery(Map.of("q", "urgent", "sort", "name"));

		assertEquals("urgent", channel.get());
	}

	/**
	 * Tests that a URL carrying no value for the parameter leaves the value the view established, so
	 * that a link without the parameter says nothing about the filter.
	 */
	public void testAbsentParameterKeepsValue() {
		ViewChannel channel = new DefaultViewChannel("filter");
		channel.set("established");
		QueryBindingParticipant participant = new QueryBindingParticipant("q", channel);

		participant.activateQuery(Map.of("sort", "name"));

		assertEquals("established", channel.get());
		assertEquals(Map.of("q", "established"), participant.activeRouteSegment().queryParams());
	}

	/**
	 * Tests that a change of the bound channel is reported to the listeners of the participant, so
	 * that the address bar follows the value.
	 */
	public void testChannelChangeIsReported() {
		ViewChannel channel = new DefaultViewChannel("filter");
		QueryBindingParticipant participant = new QueryBindingParticipant("q", channel);

		List<RouteSegment> reported = new ArrayList<>();
		participant.addRouteChangeListener((sender, segment) -> reported.add(segment));

		channel.set("urgent");
		assertEquals(1, reported.size());
		assertEquals(Map.of("q", "urgent"), reported.get(0).queryParams());

		channel.set(null);
		assertEquals("A cleared value is reported as well, so the parameter leaves the URL.", 2,
			reported.size());
		assertEquals(Map.of(), reported.get(1).queryParams());
	}

}
