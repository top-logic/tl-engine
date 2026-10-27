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

	private static final String PROJECT_VIEW = "demo/project.view.xml";

	private static final String MILESTONES_VIEW = "demo/milestone-list.view.xml";

	private static final String PROJECT = "project";

	private static final String TICKETS_VIEW = "demo/tickets.view.xml";

	private static final String TICKET_VIEW = "demo/ticket-detail.view.xml";

	private static final String FOLDER_VIEW = "demo/folder.view.xml";

	private static final String FILE_VIEW = "demo/file.view.xml";

	/** Id of the declaration of the person frame. */
	private static final String PERSON = "person";

	/** Id of the declaration of the order frame. */
	private static final String ORDER = "order";

	/** Id of the declaration of the note frame. */
	private static final String NOTE = "note";

	/** Id of the declaration of the milestone list frame. */
	private static final String MILESTONES = "milestones";

	/** Id of the declaration of the frame of the tickets of a milestone. */
	private static final String MILESTONE = "milestone";

	/** Id of the declaration of the ticket frame. */
	private static final String TICKET = "ticket";

	/** Id of the declaration of the folder frame. */
	private static final String FOLDER = "folder";

	/** Id of the declaration of the file frame. */
	private static final String FILE = "file";

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

		routeManager.adoptUrl("person/p-2");
		participant.activateRoute(match(participant, "person/p-2"));

		assertEquals("The URL describes the path from its first frame.",
			List.of(frame(PERSON_VIEW, "2")), scope.getPath());
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

		scope.push(PERSON, null, Map.of("person", "1"));

		assertEquals("person/p-1", reported[0]);
	}

	/**
	 * Tests that the page is named by the innermost frame of the path.
	 */
	public void testInnermostFrameNamesPage() {
		ViewChannel path = path(frame(PERSON_VIEW, "1"), frame(ORDER_VIEW, "7"));
		TileFrameRouteParticipant participant = participant(scope(path));

		assertEquals(ResKey.text("Order 7"), participant.activeRouteLabel());
	}

	/**
	 * Tests that a stack showing its initial view leaves the title to the routes around it.
	 */
	public void testEmptyPathNamesNoPage() {
		assertNull(participant(scope(path())).activeRouteLabel());
	}

	/**
	 * Tests that a frame without a label leaves the title to the frame it was drilled into from.
	 */
	public void testUnlabeledFrameKeepsOuterLabel() {
		ViewChannel path = path(frame(PERSON_VIEW, "1"),
			new TileFrame(ORDER, ORDER_VIEW, null, Map.of("order", "7")));

		assertEquals(ResKey.text("Person 1"), participant(scope(path)).activeRouteLabel());
	}

	/**
	 * Tests that a frame beyond the address names no page: the title names what the URL leads back
	 * to.
	 */
	public void testFrameWithoutAddressNamesNoPage() {
		ViewChannel path = path(frame(PERSON_VIEW, "1"),
			new TileFrame(NOTE, NOTE_VIEW, ResKey.text("Note"), Map.of()));
		assertEquals(ResKey.text("Person 1"), participant(scope(path)).activeRouteLabel());

		ViewChannel below = path(new TileFrame(NOTE, NOTE_VIEW, ResKey.text("Note"), Map.of()), frame(PERSON_VIEW, "1"));
		assertNull("Nothing drilled into from a frame without an address has an address.",
			participant(scope(below)).activeRouteLabel());
	}

	/**
	 * Tests that the label of a frame pushed onto the path is the one reported with the route
	 * change, so that the title follows a drill-down.
	 */
	public void testPushedFrameNamesPage() {
		ViewChannel path = path();
		TileStackScope scope = scope(path);
		TileFrameRouteParticipant participant = participant(scope);

		ResKey[] reported = new ResKey[1];
		participant.addRouteChangeListener((sender, segment) -> reported[0] = sender.activeRouteLabel());

		scope.push(PERSON, null, Map.of("person", "1"));
		assertEquals(ResKey.text("Person 1"), reported[0]);

		scope.popTo(0);
		assertNull(reported[0]);
	}

	/**
	 * Tests that a frame pushed without a label of its own is named by the label the stack declares
	 * for its view.
	 */
	public void testPushedFrameUsesDeclaredLabel() {
		ViewChannel path = path();
		TileStackScope scope = scope(path);

		scope.push(PERSON, null, Map.of("person", "1"));

		assertEquals(ResKey.text("Person 1"), scope.getPath().get(0).getLabel());
	}

	/**
	 * Tests that the content of a frame keeps out of the title exactly while the frame naming the
	 * page has a route declared the final title.
	 */
	public void testFinalTitleFrameKeepsContentOutOfTitle() {
		assertTrue("A stack showing its initial view names no page and keeps nothing out of the title.",
			participant(finalTitleScope(path())).titlesContent());

		ViewChannel person = path(frame(PERSON_VIEW, "1"));
		assertTrue("By default, what a frame displays refines the title.",
			participant(scope(person)).titlesContent());
		assertFalse("The content of a frame declared the final title renamed the page.",
			participant(finalTitleScope(person)).titlesContent());

		ViewChannel order = path(frame(PERSON_VIEW, "1"), frame(ORDER_VIEW, "7"));
		assertTrue("The order frame names the page, and its route is not declared the final title.",
			participant(finalTitleScope(order)).titlesContent());

		ViewChannel unlabeled = path(frame(PERSON_VIEW, "1"),
			new TileFrame(ORDER, ORDER_VIEW, null, Map.of("order", "7")));
		assertFalse("The person frame names the page, the unlabeled frame above leaves it to it.",
			participant(finalTitleScope(unlabeled)).titlesContent());
	}

	/**
	 * Tests that a frame pushed with a label of its own keeps it.
	 */
	public void testPushedLabelWinsOverDeclaration() {
		ViewChannel path = path();
		TileStackScope scope = scope(path);

		scope.push(PERSON, ResKey.text("The one"), Map.of("person", "1"));

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
		assertTrue("The label of the person frame is the final title.", personFrame.getFinalTitle());

		FrameRouteConfig noteFrame = stack.getFrames().get(1);
		assertEquals("note", noteFrame.getRoute());
		assertEquals("A frame without a parameter is named by static segments alone.",
			List.of(), noteFrame.getParams());
		assertNull("A frame may be named by whoever pushes it.", noteFrame.getLabel());
		assertFalse("By default, what a frame displays refines the title.", noteFrame.getFinalTitle());
	}

	/**
	 * Tests that a frame restored from a URL takes a parameter its route does not carry from the
	 * frame below it, and then holds the same parameters as the frame a push hands the value to.
	 */
	public void testPathParamTakenFromFrameBelow() {
		ViewChannel path = path();
		TileStackScope scope = projectScope(path);
		TileFrameRouteParticipant participant = participant(scope);

		participant.activateRoute(match(participant, "project/pr-1"));
		participant.activateRoute(match(participant, "milestones"));

		assertEquals(2, scope.getPath().size());
		TileFrame milestones = scope.getPath().get(1);
		assertEquals(MILESTONES_VIEW, milestones.getViewRef());
		assertEquals("The project is taken from the frame below.", Map.of(PROJECT, "1"), milestones.getParams());
		assertEquals("The label is computed from the parameter taken from the path.",
			ResKey.text("Milestones of 1"), milestones.getLabel());

		TileStackScope pushed = projectScope(path(scope.getPath().get(0)));
		pushed.push(MILESTONES, null, Map.of(PROJECT, "1"));
		assertTrue("The restored frame is the one a push from the project frame puts on the path.",
			pushed.getPath().get(1).showsSame(milestones));
		assertEquals(pushed.getPath(), scope.getPath());
	}

	/**
	 * Tests that a parameter taken from the path does not reach the URL, so that the URL names the
	 * value once, in the route of the frame below.
	 */
	public void testPathParamNotInUrl() {
		ViewChannel path = path(projectFrame("1"), milestonesFrame("1"));
		TileFrameRouteParticipant participant = participant(projectScope(path));

		assertEquals("project/pr-1/milestones", participant.activeRouteSegment().path());
	}

	/**
	 * Tests that a frame whose path parameter no frame below holds is not restored, and the path ends
	 * at the frames established before it.
	 */
	public void testPathParamWithoutValueBelowEndsPath() {
		ViewChannel path = path();
		TileStackScope scope = new TileStackScope(path, List.of(
			new FrameRoute(MILESTONES, MILESTONES_VIEW, "milestones", Map.of(), Map.of(), List.of(PROJECT),
				label("Milestones of ", PROJECT), false)));
		TileFrameRouteParticipant participant = participant(scope);

		participant.activateRoute(match(participant, "milestones"));
		assertEquals("Without a frame below, the list has no project.", List.of(), scope.getPath());
		assertEquals("The path ended, the rest of the address is not taken up.", List.of(),
			participant.declaredRoutes());
	}

	/**
	 * Tests that the frames of a nested path are restored one on top of the other and the address
	 * is written back as it was read, every object named once.
	 */
	public void testNestedPathRestoredAndWritten() {
		TileStackScope scope = projectScope(path());
		TileFrameRouteParticipant participant = participant(scope);

		String address = "project/pr-1/milestones/milestone/m-5/ticket/t-9";
		adopt(participant, address);

		assertEquals(List.of(PROJECT, MILESTONES, MILESTONE, TICKET), frameIds(scope));
		assertEquals(address, participant.activeRouteSegment().path());
	}

	/**
	 * Tests that a route naming a frame that does not follow the frame below it ends the path
	 * there, and nothing after it is taken up.
	 */
	public void testMisplacedSegmentEndsPath() {
		TileStackScope direct = projectScope(path(projectFrame("1")));
		TileFrameRouteParticipant directParticipant = participant(direct);
		assertEquals("A ticket does not follow the initial view.", 1, adopt(directParticipant, "ticket/t-9"));
		assertEquals(List.of(), direct.getPath());

		TileStackScope reversed = projectScope(path());
		assertEquals("A ticket before its project ends the path at the ticket.", 1,
			adopt(participant(reversed), "ticket/t-9/project/pr-1"));
		assertEquals(List.of(), reversed.getPath());

		TileStackScope skipping = projectScope(path());
		assertEquals("A ticket does not follow the project directly.", 2,
			adopt(participant(skipping), "project/pr-1/ticket/t-9/milestones"));
		assertEquals(List.of(PROJECT), frameIds(skipping));
	}

	/**
	 * Tests that a declaration referenced by a {@code <frame-ref>} follows itself, so that a
	 * recursive structure is restored and written back level by level.
	 */
	public void testRecursionViaFrameRef() {
		TileStackScope scope = folderScope(path());
		TileFrameRouteParticipant participant = participant(scope);

		String address = "folder/1/folder/7/file/3";
		adopt(participant, address);

		assertEquals(List.of(FOLDER, FOLDER, FILE), frameIds(scope));
		assertEquals(Map.of(FOLDER, "7"), scope.getPath().get(1).getParams());
		assertEquals(address, participant.activeRouteSegment().path());
	}

	/**
	 * Tests that the same view declared below two frames is written with the route of the place it
	 * is pushed at.
	 */
	public void testSameViewUnderTwoParentsWritesRouteOfItsPlace() {
		FrameRoute personNote = new FrameRoute("personNote", NOTE_VIEW, "person-note", Map.of(), Map.of(), null);
		FrameRoute orderNote = new FrameRoute("orderNote", NOTE_VIEW, "order-note", Map.of(), Map.of(), null);
		FrameRoute order = route(ORDER, ORDER_VIEW, "o-");
		order.initSuccessors(List.of(orderNote));
		FrameRoute person = route(PERSON, PERSON_VIEW, "p-");
		person.initSuccessors(List.of(order, personNote));

		TileStackScope scope = new TileStackScope(path(), List.of(person));
		TileFrameRouteParticipant participant = participant(scope);
		scope.push(PERSON, null, Map.of(PERSON, "1"));
		scope.push("personNote", null, Map.of());
		assertEquals("person/p-1/person-note", participant.activeRouteSegment().path());

		scope.popTo(1);
		scope.push(ORDER, null, Map.of(ORDER, "7"));
		scope.push("orderNote", null, Map.of());
		assertEquals("person/p-1/order/o-7/order-note", participant.activeRouteSegment().path());

		scope.popTo(1);
		scope.push("orderNote", null, Map.of());
		assertEquals("A frame pushed where its declaration does not follow has no address.",
			"person/p-1", participant.activeRouteSegment().path());
	}

	/**
	 * The stack of the path parameter and nesting tests: a project frame carrying its project in
	 * the URL, a milestone list taking the project from the frame below, the tickets of a milestone
	 * and a ticket.
	 */
	private static TileStackScope projectScope(ViewChannel path) {
		FrameRoute ticket = route(TICKET, TICKET_VIEW, "t-");
		FrameRoute milestone = route(MILESTONE, TICKETS_VIEW, "m-");
		milestone.initSuccessors(List.of(ticket));
		FrameRoute milestones = new FrameRoute(MILESTONES, MILESTONES_VIEW, "milestones", Map.of(), Map.of(),
			List.of(PROJECT), label("Milestones of ", PROJECT), false);
		milestones.initSuccessors(List.of(milestone));
		FrameRoute project = new FrameRoute(PROJECT, PROJECT_VIEW, "project/:project",
			toUrl(PROJECT, value -> value == null ? null : "pr-" + value),
			fromUrl(PROJECT, text -> known(text, "pr-")),
			label("Project ", PROJECT));
		project.initSuccessors(List.of(milestones));
		return new TileStackScope(path, List.of(project));
	}

	/**
	 * A stack of folders: a folder may follow a folder, and a file may follow a folder.
	 */
	private static TileStackScope folderScope(ViewChannel path) {
		FrameRoute file = route(FILE, FILE_VIEW, "");
		FrameRoute folder = route(FOLDER, FOLDER_VIEW, "");
		folder.initSuccessors(List.of(folder, file));
		return new TileStackScope(path, List.of(folder));
	}

	/**
	 * A declaration of a frame holding one parameter named like the declaration, which the URL
	 * names by the given prefix in front of its value.
	 */
	private static FrameRoute route(String id, String viewRef, String prefix) {
		return new FrameRoute(id, viewRef, id + "/:" + id,
			toUrl(id, value -> value == null ? null : prefix + value),
			fromUrl(id, text -> known(text, prefix)),
			null);
	}

	/**
	 * Offers the given address to the participant route by route, as the {@link RouteManager} does
	 * while a URL is adopted.
	 *
	 * @return The number of routes the participant took up.
	 */
	private static int adopt(TileFrameRouteParticipant participant, String address) {
		String rest = address;
		int taken = 0;
		while (!rest.isEmpty()) {
			RouteMatch match = null;
			for (RoutePattern pattern : participant.declaredRoutes()) {
				match = pattern.match(rest);
				if (match != null) {
					break;
				}
			}
			if (match == null) {
				break;
			}
			taken++;
			rest = match.remainingPath();
			participant.activateRoute(match);
		}
		return taken;
	}

	private static List<String> frameIds(TileStackScope scope) {
		return scope.getPath().stream().map(TileFrame::getFrameId).toList();
	}

	private static TileFrame projectFrame(String project) {
		return new TileFrame(PROJECT, PROJECT_VIEW, ResKey.text("Project " + project), Map.of(PROJECT, project));
	}

	private static TileFrame milestonesFrame(String project) {
		return new TileFrame(MILESTONES, MILESTONES_VIEW, ResKey.text("Milestones of " + project),
			Map.of(PROJECT, project));
	}

	/**
	 * The stack of the tests: a person frame follows the initial view, an order frame follows a
	 * person frame, and a note frame without an address follows either the initial view or a person
	 * frame.
	 */
	private static TileStackScope scope(ViewChannel path) {
		return scope(path, false);
	}

	/**
	 * The {@link #scope(ViewChannel) stack of the tests} with the route of the person frame declared
	 * the final title.
	 */
	private static TileStackScope finalTitleScope(ViewChannel path) {
		return scope(path, true);
	}

	private static TileStackScope scope(ViewChannel path, boolean personFinalTitle) {
		FrameRoute note = new FrameRoute(NOTE, NOTE_VIEW, null, Map.of(), Map.of(), null);
		FrameRoute order = new FrameRoute(ORDER, ORDER_VIEW, "order/:order",
			toUrl("order", value -> value == null ? null : "o-" + value),
			fromUrl("order", text -> known(text, "o-")),
			label("Order ", "order"));
		FrameRoute person = new FrameRoute(PERSON, PERSON_VIEW, "person/:person",
			toUrl("person", value -> value == null ? null : "p-" + value),
			fromUrl("person", text -> known(text, "p-")),
			List.of(), label("Person ", "person"), personFinalTitle);
		person.initSuccessors(List.of(order, note));
		return new TileStackScope(path, List.of(person, note));
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
		String param = PERSON_VIEW.equals(viewRef) ? PERSON : ORDER_VIEW.equals(viewRef) ? ORDER : NOTE;
		Map<String, Object> params = value == null ? Map.of() : Map.of(param, value);
		String name = PERSON_VIEW.equals(viewRef) ? "Person " : ORDER_VIEW.equals(viewRef) ? "Order " : null;
		ResKey label = name == null || value == null ? null : ResKey.text(name + value);
		return new TileFrame(param, viewRef, label, params);
	}
}
