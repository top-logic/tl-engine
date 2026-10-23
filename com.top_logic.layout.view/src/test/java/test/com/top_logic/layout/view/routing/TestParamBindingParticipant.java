/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.routing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.listener.Registration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.routing.RouteChangeListener;
import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.react.routing.RoutePattern;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.DerivedViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.routing.ParamBindingConfig;
import com.top_logic.layout.view.routing.ParamBindingParticipant;
import com.top_logic.layout.view.tiles.ScriptedTileLabel;
import com.top_logic.layout.view.tiles.TileLabelProvider;

/**
 * Tests for {@link ParamBindingParticipant}.
 */
public class TestParamBindingParticipant extends TestCase {

	private static final String LABEL_VIEW = "test-param-binding-label.view.xml";

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

	/**
	 * Tests that a binding without a label names no page, whatever value it holds.
	 */
	public void testBindingWithoutLabelNamesNoPage() {
		ViewChannel channel = new DefaultViewChannel("ticketKey");
		channel.set("TL-1234");
		ParamBindingParticipant participant = new ParamBindingParticipant("detail", "ticket", channel);

		assertNull("An identifier in the URL is no title.", participant.activeRouteLabel());
	}

	/**
	 * Tests that a binding with a label names the page while it contributes its segment, and names
	 * nothing while it holds no value.
	 */
	public void testLabelNamesPageWhileValueIsBound() {
		LabelFixture fixture = new LabelFixture();

		assertNull("Without a value there is nothing to name.", fixture._participant.activeRouteLabel());

		fixture._ticket.set("Login broken");
		fixture._key.set("TL-1234");
		assertEquals(ResKey.text("Ticket Login broken"), fixture._participant.activeRouteLabel());

		fixture._key.set(null);
		assertNull("A cleared value names nothing.", fixture._participant.activeRouteLabel());
	}

	/**
	 * Tests that a change of a channel the label is computed from is reported to the route change
	 * listeners, so that the title follows a rename, while the value stays the same.
	 */
	public void testLabelChangeIsReported() {
		LabelFixture fixture = new LabelFixture();
		fixture._ticket.set("Login broken");
		fixture._key.set("TL-1234");

		List<ResKey> reported = new ArrayList<>();
		RouteChangeListener listener = (sender, segment) -> reported.add(sender.activeRouteLabel());
		fixture._participant.addRouteChangeListener(listener);

		fixture._ticket.set("Login fails");
		assertEquals(List.of(ResKey.text("Ticket Login fails")), reported);

		fixture._participant.removeRouteChangeListener(listener);
		fixture._ticket.set("Login works");
		assertEquals("A participant without listeners reports nothing.", 1, reported.size());
	}

	/**
	 * Tests that a change of the label's channels while the binding holds no value is not reported:
	 * the binding names no page then.
	 */
	public void testLabelChangeWithoutValueIsNotReported() {
		LabelFixture fixture = new LabelFixture();

		List<ResKey> reported = new ArrayList<>();
		fixture._participant.addRouteChangeListener(
			(sender, segment) -> reported.add(sender.activeRouteLabel()));

		fixture._ticket.set("Login broken");
		assertEquals(List.of(), reported);
	}

	/**
	 * Tests that the channels of the label are observed exactly while the participant has listeners,
	 * so that a participant leaving the display does not stay subscribed to channels of an
	 * enclosing view.
	 */
	public void testLabelChannelsObservedWhileListened() {
		LabelFixture fixture = new LabelFixture();

		assertEquals("Nobody listens, nothing is observed.", 0, fixture._ticket._subscriptions);

		RouteChangeListener first = (sender, segment) -> {
			// Ignored.
		};
		RouteChangeListener second = (sender, segment) -> {
			// Ignored.
		};
		fixture._participant.addRouteChangeListener(first);
		fixture._participant.addRouteChangeListener(second);
		assertEquals(1, fixture._ticket._subscriptions);

		fixture._participant.removeRouteChangeListener(first);
		assertEquals(1, fixture._ticket._subscriptions);

		fixture._participant.removeRouteChangeListener(second);
		assertEquals(0, fixture._ticket._subscriptions);

		fixture._participant.addRouteChangeListener(first);
		assertEquals("A participant entering the display again observes again.", 1,
			fixture._ticket._subscriptions);
	}

	/**
	 * Tests that a {@code <bind>} declares its label as a nested {@code <label>}, and that the label
	 * is optional.
	 */
	public void testLabelIsConfigured() throws Exception {
		DefaultInstantiationContext context =
			new DefaultInstantiationContext(TestParamBindingParticipant.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(new ClassRelativeBinaryContent(TestParamBindingParticipant.class, LABEL_VIEW));
		ViewElement.Config view = (ViewElement.Config) reader.read();
		context.checkErrors();

		List<ParamBindingConfig> bindings = view.getParamBindings();
		assertEquals(2, bindings.size());
		assertEquals(ScriptedTileLabel.class, bindings.get(0).getLabel().getImplementationClass());
		assertNull("A binding may name no page.", bindings.get(1).getLabel());
	}

	/**
	 * A binding of a ticket identifier, named by the ticket's name through a label computed from
	 * the {@code ticket} channel of the view - the way a {@link ScriptedTileLabel} reads its
	 * inputs.
	 */
	private static class LabelFixture {

		final CountingChannel _ticket = new CountingChannel("ticket");

		final ViewChannel _key = new DefaultViewChannel("ticketKey");

		final ParamBindingParticipant _participant;

		LabelFixture() {
			ViewContext context = new DefaultViewContext(null);
			context.registerChannel("ticket", _ticket);
			TileLabelProvider label = new TileLabelProvider() {
				@Override
				public ResKey compute(ViewContext labelContext) {
					Object ticket = labelContext.resolveChannel(new ChannelRef("ticket")).get();
					return ticket == null ? null : ResKey.text("Ticket " + ticket);
				}

				@Override
				public List<ViewChannel> observedChannels(ViewContext labelContext) {
					return List.of(labelContext.resolveChannel(new ChannelRef("ticket")));
				}
			};
			_participant = new ParamBindingParticipant("ticket", "ticket", _key, label, context);
		}
	}

	/**
	 * A channel counting the listeners subscribed to it.
	 */
	private static class CountingChannel extends DefaultViewChannel {

		int _subscriptions;

		CountingChannel(String name) {
			super(name);
		}

		@Override
		public Registration addListener(ViewChannel.ChannelListener listener) {
			Registration registration = super.addListener(listener);
			_subscriptions++;
			return Registration.onDispose(() -> {
				registration.dispose();
				_subscriptions--;
			});
		}
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestParamBindingParticipant.class, TypeIndex.Module.INSTANCE);
	}

	private static RouteMatch match(String value) {
		return RoutePattern.compile(":ticket", "ticket").match(value);
	}
}
