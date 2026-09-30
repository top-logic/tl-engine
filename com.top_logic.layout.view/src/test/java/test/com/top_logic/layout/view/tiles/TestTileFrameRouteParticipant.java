/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.tiles;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.routing.RouteManager;
import com.top_logic.layout.react.routing.RouteMatch;
import com.top_logic.layout.react.routing.RoutePattern;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.view.tiles.FrameParamConfig;
import com.top_logic.layout.view.tiles.FrameRoute;
import com.top_logic.layout.view.tiles.FrameRouteConfig;
import com.top_logic.layout.view.tiles.ScriptedTileLabel;
import com.top_logic.layout.view.tiles.TileFrame;
import com.top_logic.layout.view.tiles.TileFrameRouteParticipant;
import com.top_logic.layout.view.tiles.TileLabelProvider;
import com.top_logic.layout.view.tiles.TileStackElement;
import com.top_logic.layout.view.tiles.TileStackScope;

/**
 * Tests for {@link TileFrameRouteParticipant}.
 *
 * <p>
 * The conversions between a frame parameter and the text of the URL are configured as TL-Script
 * expressions and evaluated by the running application; here they are the equivalent Java
 * functions, so that the participant is tested without a model. The parameter values stand for
 * business objects: a person is the text {@code "1"}, and the URL names it {@code "p-1"}.
 * </p>
 */
public class TestTileFrameRouteParticipant extends TestCase {

	private static final String VIEW = "test-frame-routes.view.xml";

	private static final String PERSON_VIEW = "demo/person-detail.view.xml";

	private static final String ORDER_VIEW = "demo/order-detail.view.xml";

	private static final String NOTE_VIEW = "demo/note.view.xml";

	/**
	 * Tests that the URL names every frame of the path, with the parameter values converted to the
	 * texts naming them.
	 */
	public void testPathIsInUrl() {
		ViewChannel path = path(frame(PERSON_VIEW, "1"), frame(ORDER_VIEW, "7"));
		TileFrameRouteParticipant participant = participant(scope(path));

		assertEquals("person/p-1/order/o-7", participant.activeRouteSegment().path());
	}

	/**
	 * Tests that a stack showing its initial view names nothing in the URL.
	 */
	public void testEmptyPathHasNoSegment() {
		ViewChannel path = path();
		TileFrameRouteParticipant participant = participant(scope(path));

		assertNull("A stack without a drilled-down frame has no address of its own.",
			participant.activeRouteSegment());
	}

	/**
	 * Tests that a URL restores the frame it names, with the parameter value the text resolves to
	 * and the label the stack declares for the view.
	 */
	public void testUrlRestoresFrame() {
		ViewChannel path = path();
		TileStackScope scope = scope(path);
		TileFrameRouteParticipant participant = participant(scope);

		participant.activateRoute(match(participant, "person/p-1"));

		assertEquals(1, scope.getPath().size());
		TileFrame restored = scope.getPath().get(0);
		assertEquals(PERSON_VIEW, restored.getViewRef());
		assertEquals(Map.of("person", "1"), restored.getParams());
		assertEquals("The frame is named by the label the stack declares for its view.",
			ResKey.text("Person 1"), restored.getLabel());
	}

	/**
	 * Tests that a URL naming something the display cannot show restores no frame, so that the
	 * address bar is corrected to the path that could be restored.
	 */
	public void testUnresolvableValueRestoresNothing() {
		ViewChannel path = path();
		TileStackScope scope = scope(path);
		TileFrameRouteParticipant participant = participant(scope);

		// The text does not name any person: the conversion back yields nothing.
		participant.activateRoute(match(participant, "person/deleted"));

		assertEquals("A frame that cannot be displayed is not pushed.", List.of(), scope.getPath());
	}

	/**
	 * Tests that a URL naming something the display cannot show ends the path there, instead of
	 * leaving the frame the session held displayed.
	 */
	public void testUnresolvableValueEmptiesHeldPath() {
		ViewChannel path = path(frame(PERSON_VIEW, "1"));
		TileStackScope scope = scope(path);
		TileFrameRouteParticipant participant = participant(scope);

		participant.activateRoute(match(participant, "person/deleted"));

		assertEquals("The URL is the authority for the path from its first frame on.",
			List.of(), scope.getPath());
	}

	/**
	 * Tests that a URL naming something the display cannot show keeps the frames the same URL
	 * established before it.
	 */
	public void testUnresolvableValueKeepsRestoredFrames() {
		ViewChannel path = path(frame(PERSON_VIEW, "1"), frame(ORDER_VIEW, "7"));
		TileStackScope scope = scope(path);
		TileFrameRouteParticipant participant = participant(scope);

		participant.activateRoute(match(participant, "person/p-1"));
		participant.activateRoute(match(participant, "order/deleted"));

		assertEquals("The path ends where the URL stops describing it.",
			List.of(frame(PERSON_VIEW, "1")), scope.getPath());
	}

	/**
	 * Tests that the frames of one URL are restored one on top of the other.
	 */
	public void testUrlRestoresFramesInSequence() {
		ViewChannel path = path();
		TileStackScope scope = scope(path);
		TileFrameRouteParticipant participant = participant(scope);

		participant.activateRoute(match(participant, "person/p-1"));
		participant.activateRoute(match(participant, "order/o-7"));

		assertEquals(List.of(frame(PERSON_VIEW, "1"), frame(ORDER_VIEW, "7")), scope.getPath());
		assertTrue("A drill-down path is named one route per frame.", participant.acceptsRouteSequence());
	}

	/**
	 * Tests that the frames of a URL replace the path an earlier one established rather than
	 * extending it.
	 */
	public void testUrlReplacesEarlierPath() {
		ViewChannel path = path();
		TileStackScope scope = scope(path);
		RouteManager routeManager = new RouteManager();
		TileFrameRouteParticipant participant = new TileFrameRouteParticipant(scope, routeManager);

		routeManager.adoptUrl("person/p-1");
		participant.activateRoute(match(participant, "person/p-1"));

		routeManager.adoptUrl("order/o-7");
		participant.activateRoute(match(participant, "order/o-7"));

		assertEquals("The URL describes the path from its first frame.",
			List.of(frame(ORDER_VIEW, "7")), scope.getPath());
	}

	/**
	 * Tests that a URL naming no frame of the stack returns it to its initial view.
	 */
	public void testResetEmptiesPath() {
		ViewChannel path = path(frame(PERSON_VIEW, "1"), frame(ORDER_VIEW, "7"));
		TileStackScope scope = scope(path);
		TileFrameRouteParticipant participant = participant(scope);

		participant.resetRoute();

		assertEquals("A URL out of the drill-down returns to the view it started in.",
			List.of(), scope.getPath());
	}

	/**
	 * Tests that a frame view the stack declares no route for has no address, and neither has
	 * anything drilled into from it.
	 */
	public void testFrameWithoutRouteHasNoAddress() {
		ViewChannel undeclaredOnly = path(frame(NOTE_VIEW, "1"));
		assertNull("A frame without a declared route has no address.",
			participant(scope(undeclaredOnly)).activeRouteSegment());

		ViewChannel undeclaredOnTop = path(frame(PERSON_VIEW, "1"), frame(NOTE_VIEW, "2"));
		assertEquals("The address describes the path up to the frame that has none.",
			"person/p-1", participant(scope(undeclaredOnTop)).activeRouteSegment().path());

		ViewChannel undeclaredBelow = path(frame(NOTE_VIEW, "2"), frame(PERSON_VIEW, "1"));
		assertNull("A frame drilled into from one without an address has none either.",
			participant(scope(undeclaredBelow)).activeRouteSegment());
	}

	/**
	 * Tests that a frame whose parameter value has no name in a URL contributes no address.
	 */
	public void testFrameWithUnnamableValueHasNoAddress() {
		ViewChannel path = path(frame(PERSON_VIEW, null));
		assertNull("A value that names nothing leaves the frame without an address.",
			participant(scope(path)).activeRouteSegment());
	}

	/**
	 * Tests that a change of the path is reported to the listeners of the participant, so that the
	 * address bar follows a drill-down.
	 */
	public void testPathChangeIsReported() {
		ViewChannel path = path();
		TileStackScope scope = scope(path);
		TileFrameRouteParticipant participant = participant(scope);

		String[] reported = new String[1];
		participant.addRouteChangeListener((sender, segment) -> reported[0] = segment.path());

		scope.push(PERSON_VIEW, null, Map.of("person", "1"));

		assertEquals("person/p-1", reported[0]);
	}

	/**
	 * Tests that a frame pushed without a label of its own is named by the label the stack declares
	 * for its view.
	 */
	public void testPushedFrameUsesDeclaredLabel() {
		ViewChannel path = path();
		TileStackScope scope = scope(path);

		scope.push(PERSON_VIEW, null, Map.of("person", "1"));

		assertEquals(ResKey.text("Person 1"), scope.getPath().get(0).getLabel());
	}

	/**
	 * Tests that a frame pushed with a label of its own keeps it.
	 */
	public void testPushedLabelWinsOverDeclaration() {
		ViewChannel path = path();
		TileStackScope scope = scope(path);

		scope.push(PERSON_VIEW, ResKey.text("The one"), Map.of("person", "1"));

		assertEquals(ResKey.text("The one"), scope.getPath().get(0).getLabel());
	}

	/**
	 * Tests that a stack declares the routes of its frames as {@code <frame>} entries, with the
	 * parameter conversions and the label of a frame.
	 */
	public void testFrameRoutesAreConfigured() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(
			TestTileFrameRouteParticipant.class);
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(new ClassRelativeBinaryContent(TestTileFrameRouteParticipant.class, VIEW));
		ViewElement.Config view = (ViewElement.Config) reader.read();
		context.checkErrors();

		PanelElement.Config panel = (PanelElement.Config) view.getContent();
		TileStackElement.Config stack = panel.getChildren().stream()
			.filter(TileStackElement.Config.class::isInstance)
			.map(TileStackElement.Config.class::cast)
			.findFirst()
			.orElseThrow();

		assertEquals(2, stack.getFrames().size());

		FrameRouteConfig personFrame = stack.getFrames().get(0);
		assertEquals("tiles/person-detail.view.xml", personFrame.getView());
		assertEquals("person/:person", personFrame.getRoute());
		assertEquals(1, personFrame.getParams().size());
		FrameParamConfig param = personFrame.getParams().get(0);
		assertEquals("person", param.getName());
		assertNotNull("The value is named in the URL by an expression.", param.getExpr());
		assertNotNull("The name in the URL is resolved back by an expression.", param.getReverse());
		assertEquals(ScriptedTileLabel.class, personFrame.getLabel().getImplementationClass());

		FrameRouteConfig noteFrame = stack.getFrames().get(1);
		assertEquals("note", noteFrame.getRoute());
		assertEquals("A frame without a parameter is named by static segments alone.",
			List.of(), noteFrame.getParams());
		assertNull("A frame may be named by whoever pushes it.", noteFrame.getLabel());
	}

	/**
	 * The stack of the tests: a person frame and an order frame are addressable, a note frame is
	 * not.
	 */
	private static TileStackScope scope(ViewChannel path) {
		return new TileStackScope(path, List.of(
			new FrameRoute(PERSON_VIEW, "person/:person",
				toUrl("person", value -> value == null ? null : "p-" + value),
				fromUrl("person", text -> known(text, "p-")),
				label("Person ", "person")),
			new FrameRoute(ORDER_VIEW, "order/:order",
				toUrl("order", value -> value == null ? null : "o-" + value),
				fromUrl("order", text -> known(text, "o-")),
				label("Order ", "order"))));
	}

	private static TileFrameRouteParticipant participant(TileStackScope scope) {
		return new TileFrameRouteParticipant(scope, new RouteManager());
	}

	/**
	 * The match of the first route of the participant that the given URL names.
	 */
	private static RouteMatch match(TileFrameRouteParticipant participant, String url) {
		for (RoutePattern pattern : participant.declaredRoutes()) {
			RouteMatch match = pattern.match(url);
			if (match != null) {
				return match;
			}
		}
		throw new AssertionError("No route of the participant matches '" + url + "'.");
	}

	/**
	 * The value the given text names, or {@code null} if it names none - a deleted object, a
	 * mistyped identifier.
	 */
	private static Object known(String text, String prefix) {
		return text.startsWith(prefix) ? text.substring(prefix.length()) : null;
	}

	private static Map<String, Function<Object, String>> toUrl(String name, Function<Object, String> conversion) {
		return Map.of(name, conversion);
	}

	private static Map<String, Function<String, Object>> fromUrl(String name, Function<String, Object> conversion) {
		return Map.of(name, conversion);
	}

	/**
	 * A label naming a frame by the value of the given parameter, as a
	 * {@link com.top_logic.layout.view.tiles.ScriptedTileLabel scripted label} does.
	 */
	private static TileLabelProvider label(String prefix, String param) {
		return context -> ResKey.text(prefix + context.resolveChannel(new ChannelRef(param)).get());
	}

	private static ViewChannel path(TileFrame... frames) {
		DefaultViewChannel channel = new DefaultViewChannel("navPath");
		channel.set(List.of(frames));
		return channel;
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestTileFrameRouteParticipant.class, TypeIndex.Module.INSTANCE);
	}

	private static TileFrame frame(String viewRef, String value) {
		String param = PERSON_VIEW.equals(viewRef) ? "person" : ORDER_VIEW.equals(viewRef) ? "order" : "note";
		Map<String, Object> params = value == null ? Map.of() : Map.of(param, value);
		String name = PERSON_VIEW.equals(viewRef) ? "Person " : ORDER_VIEW.equals(viewRef) ? "Order " : null;
		ResKey label = name == null || value == null ? null : ResKey.text(name + value);
		return new TileFrame(viewRef, label, params);
	}
}
