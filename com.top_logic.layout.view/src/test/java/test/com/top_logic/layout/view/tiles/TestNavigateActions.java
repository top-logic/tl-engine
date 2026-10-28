/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.tiles;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.view.command.ActionArguments;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ContextDependentRule;
import com.top_logic.layout.view.command.GenericViewCommand;
import com.top_logic.layout.view.command.ViewAction;
import com.top_logic.layout.view.command.ViewActionChain;
import com.top_logic.layout.view.command.ViewCommand;
import com.top_logic.layout.view.element.PanelElement;
import com.top_logic.layout.view.tiles.FrameRoute;
import com.top_logic.layout.view.tiles.NavigatePopAction;
import com.top_logic.layout.view.tiles.NavigatePopCommand;
import com.top_logic.layout.view.tiles.NavigatePopToAction;
import com.top_logic.layout.view.tiles.NavigatePopToCommand;
import com.top_logic.layout.view.tiles.NavigatePushCommand;
import com.top_logic.layout.view.tiles.TileFrame;
import com.top_logic.layout.view.tiles.TileStackElement;
import com.top_logic.layout.view.tiles.TileStackScope;

/**
 * Tests for {@link NavigatePopAction} and {@link NavigatePopToAction}, the commands delegating to
 * them, and the configuration that makes the stack path visible inside a frame.
 */
public class TestNavigateActions extends TestCase {

	private static final String VIEW = "test-navigate-actions.view.xml";

	/** Id of the declaration of the person frame of the {@link #pushScope(ViewChannel) push tests}. */
	private static final String PERSON = "person";

	/** Id of the declaration of the order frame of the {@link #pushScope(ViewChannel) push tests}. */
	private static final String ORDER = "order";

	/**
	 * Tests that a pop leaves the top frame of a two-frame path and hands the input on unchanged.
	 */
	public void testPopLeavesTopFrame() {
		ViewChannel path = path(frame("overview"), frame("detail"));
		Object input = new Object();

		Object result = new NavigatePopAction().execute(frameContext(path), input);

		assertEquals(List.of(frame("overview")), path.get());
		assertSame("The action following the pop still sees the object.", input, result);
	}

	/**
	 * Tests that an action following a pop in a chain receives the chain's object.
	 */
	public void testChainContinuesWithObject() {
		ViewChannel path = path(frame("overview"), frame("detail"));
		Object input = new Object();
		Object[] seen = new Object[1];
		ViewAction record = (context, value) -> seen[0] = value;

		ViewActionChain.run(frameContext(path), List.of(new NavigatePopAction(), record), args(input), null);

		assertEquals(List.of(frame("overview")), path.get());
		assertSame("The object survives the pop.", input, seen[0]);
	}

	/**
	 * Tests that a pop of the only frame empties the path.
	 */
	public void testPopOfLastFrameEmptiesPath() {
		ViewChannel path = path(frame("overview"));

		new NavigatePopAction().execute(frameContext(path), null);

		assertEquals(List.of(), path.get());
	}

	/**
	 * Tests that popping to depth zero empties the path.
	 */
	public void testPopToZeroEmptiesPath() {
		ViewChannel path = path(frame("overview"), frame("detail"), frame("sub-detail"));

		new NavigatePopToAction(0).execute(frameContext(path), null);

		assertEquals(List.of(), path.get());
	}

	/**
	 * Tests that popping to an intermediate depth keeps exactly that many frames.
	 */
	public void testPopToKeepsRequestedDepth() {
		ViewChannel path = path(frame("overview"), frame("detail"), frame("sub-detail"));

		new NavigatePopToAction(1).execute(frameContext(path), null);

		assertEquals(List.of(frame("overview")), path.get());
	}

	/**
	 * Tests that popping to a depth beyond the current one leaves the path alone.
	 */
	public void testPopToBeyondPathKeepsPath() {
		List<TileFrame> frames = List.of(frame("overview"), frame("detail"));
		ViewChannel path = path(frame("overview"), frame("detail"));

		new NavigatePopToAction(5).execute(frameContext(path), null);

		assertEquals(frames, path.get());
	}

	/**
	 * Tests that a pop outside of any tile stack fails, naming the offending tag.
	 */
	public void testPopOutsideStackFails() {
		ViewContext context = new DefaultViewContext(null);

		try {
			new NavigatePopAction().execute(context, null);
			fail("Expected failure without an enclosing tile stack.");
		} catch (IllegalStateException expected) {
			assertContains(NavigatePopAction.Config.TAG_NAME, expected.getMessage());
		}
	}

	/**
	 * Tests that popping to a depth outside of any tile stack fails, naming the offending tag.
	 */
	public void testPopToOutsideStackFails() {
		ViewContext context = new DefaultViewContext(null);

		try {
			new NavigatePopToAction(0).execute(context, null);
			fail("Expected failure without an enclosing tile stack.");
		} catch (IllegalStateException expected) {
			assertContains(NavigatePopToAction.Config.TAG_NAME, expected.getMessage());
		}
	}

	/**
	 * Tests that a pop outside of the view layer fails.
	 */
	public void testPopOutsideViewContextFails() {
		ReactContext context = new DefaultReactContext("", "test", new SSEUpdateQueue(),
				new ReactWindowRegistry("test"));

		try {
			new NavigatePopAction().execute(context, null);
			fail("Expected failure without a ViewContext.");
		} catch (IllegalStateException expected) {
			assertContains(ViewContext.class.getSimpleName(), expected.getMessage());
		}
	}

	/**
	 * Tests that the commands perform the same navigation as the actions they delegate to.
	 */
	public void testCommandsNavigateLikeActions() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestNavigateActions.class);
		Map<String, ViewCommand> commands = commands(context, parse(context));

		ViewCommand pop = commands.get("popCommand");
		assertTrue("The command list resolves the tag to the command.", pop instanceof NavigatePopCommand);
		ViewChannel popPath = path(frame("overview"), frame("detail"));
		pop.execute(frameContext(popPath), null);
		assertEquals(List.of(frame("overview")), popPath.get());

		ViewCommand popTo = commands.get("popToCommand");
		assertTrue("The command list resolves the tag to the command.", popTo instanceof NavigatePopToCommand);
		ViewChannel popToPath = path(frame("overview"), frame("detail"), frame("sub-detail"));
		popTo.execute(frameContext(popToPath), null);
		assertEquals(List.of(frame("overview"), frame("detail")), popToPath.get());
	}

	/**
	 * Tests that the actions are reachable by their tag inside a command chain, even though the
	 * commands claim the same tags in the command list.
	 */
	public void testActionsRunInsideGenericCommand() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestNavigateActions.class);
		Map<String, ViewCommand> commands = commands(context, parse(context));

		ViewCommand back = commands.get("back");
		assertTrue("A <navigate-pop> in a chain is an action of a generic command.",
			back instanceof GenericViewCommand);
		ViewChannel backPath = path(frame("overview"), frame("detail"));
		back.execute(frameContext(backPath), null);
		assertEquals(List.of(frame("overview")), backPath.get());

		ViewChannel homePath = path(frame("overview"), frame("detail"), frame("sub-detail"));
		commands.get("home").execute(frameContext(homePath), null);
		assertEquals(List.of(frame("overview")), homePath.get());
	}

	/**
	 * Tests that a push names the frame by the id of its declaration, which supplies the view of the
	 * pushed frame.
	 */
	public void testPushByIdBuildsFrameOfDeclaration() {
		ViewChannel path = path();
		TileStackScope scope = pushScope(path);
		ViewContext initial = displayedIn(scope, scope.root());
		NavigatePushCommand push = push(PERSON, PERSON);

		check(push, initial);
		push.execute(initial, "1");

		assertEquals(List.of(new TileFrame(PERSON, PERSON + ".view.xml", null, Map.of(PERSON, "1"))), path.get());

		ViewContext personFrame = displayedIn(scope, scope.declaration(PERSON));
		NavigatePushCommand order = push(ORDER, ORDER);
		check(order, personFrame);
		order.execute(personFrame, "7");
		assertEquals(ORDER, ((TileFrame) ((List<?>) path.get()).get(1)).getFrameId());
	}

	/**
	 * Tests that a push naming a frame that does not follow the frame it is displayed in is
	 * rejected when that frame is built.
	 */
	public void testPushOfNonSuccessorRejected() {
		TileStackScope scope = pushScope(path());
		NavigatePushCommand order = push(ORDER, ORDER);
		try {
			check(order, displayedIn(scope, scope.root()));
			fail("An order follows a person, not the initial view.");
		} catch (ConfigurationError expected) {
			assertContains(ORDER, expected.getMessage());
			assertContains("initial view", expected.getMessage());
		}

		try {
			check(push("missing", ORDER), displayedIn(scope, scope.root()));
			fail("The stack declares no such frame.");
		} catch (ConfigurationError expected) {
			assertContains("missing", expected.getMessage());
		}
	}

	/**
	 * Tests that a push handing over other values than the parameters of the frame it names is
	 * rejected when the frame displaying it is built.
	 */
	public void testPushWithOtherParametersRejected() {
		TileStackScope scope = pushScope(path());
		try {
			check(push(PERSON, "selected"), displayedIn(scope, scope.root()));
			fail("The person frame holds the parameter person.");
		} catch (ConfigurationError expected) {
			assertContains(PERSON, expected.getMessage());
			assertContains("selected", expected.getMessage());
		}
	}

	/**
	 * Tests that a tile stack names the channel under which its frames see the path.
	 */
	public void testStackBindsPath() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestNavigateActions.class);
		PanelElement.Config panel = (PanelElement.Config) parse(context).getContent();

		TileStackElement.Config stack = panel.getChildren().stream()
			.filter(TileStackElement.Config.class::isInstance)
			.map(TileStackElement.Config.class::cast)
			.findFirst()
			.orElse(null);

		assertNotNull("The panel contains a tile stack.", stack);
		assertEquals("navPath", stack.getBindPathTo());
	}

	private ViewElement.Config parse(DefaultInstantiationContext context) throws Exception {
		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestNavigateActions.class, VIEW);
		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();
		return config;
	}

	private Map<String, ViewCommand> commands(DefaultInstantiationContext context, ViewElement.Config view)
			throws Exception {
		PanelElement.Config panel = (PanelElement.Config) view.getContent();
		Map<String, ViewCommand> result = new HashMap<>();
		for (PolymorphicConfiguration<? extends ViewCommand> config : panel.getCommands()) {
			result.put(((ViewCommand.Config) config).getName(), context.getInstance(config));
		}
		context.checkErrors();
		return result;
	}

	/**
	 * A stack whose person frame follows the initial view and whose order frame follows a person
	 * frame, each holding a parameter named like it.
	 */
	private static TileStackScope pushScope(ViewChannel path) {
		FrameRoute order = new FrameRoute(ORDER, ORDER + ".view.xml", "order/:order", Map.of(), Map.of(), null);
		FrameRoute person = new FrameRoute(PERSON, PERSON + ".view.xml", "person/:person", Map.of(), Map.of(), null);
		person.initSuccessors(List.of(order));
		return new TileStackScope(path, List.of(person));
	}

	/**
	 * The context of a frame of the given declaration displayed in the given stack.
	 */
	private static ViewContext displayedIn(TileStackScope scope, FrameRoute declaration) {
		return new DefaultViewContext(null).withScope(TileStackScope.class, scope)
			.withScope(FrameRoute.class, declaration);
	}

	/**
	 * A push of the frame of the given declaration, handing its input over as the given parameter.
	 */
	private static NavigatePushCommand push(String frame, String bindInputTo) {
		NavigatePushCommand.Config config = TypedConfiguration.newConfigItem(NavigatePushCommand.Config.class);
		config.update(config.descriptor().getProperty(NavigatePushCommand.Config.FRAME), frame);
		config.update(config.descriptor().getProperty(NavigatePushCommand.Config.BIND_INPUT_TO), bindInputTo);
		return (NavigatePushCommand) new DefaultInstantiationContext(TestNavigateActions.class).getInstance(config);
	}

	/**
	 * Checks the push as building the frame displaying it in the given context does.
	 */
	private static void check(NavigatePushCommand push, ViewContext context) {
		((ContextDependentRule) push.getIntrinsicRule()).bind(context);
	}

	private static ViewChannel path(TileFrame... frames) {
		DefaultViewChannel channel = new DefaultViewChannel("navPath");
		channel.set(List.of(frames));
		return channel;
	}

	private static ViewContext frameContext(ViewChannel pathChannel) {
		return new DefaultViewContext(null).withScope(TileStackScope.class, new TileStackScope(pathChannel, List.of()));
	}

	private static TileFrame frame(String name) {
		return new TileFrame(name, name + ".view.xml", null, Map.of());
	}

	private static void assertContains(String expected, String message) {
		assertTrue("Message '" + message + "' should mention '" + expected + "'.",
			message.contains(expected));
	}

	/**
	 * Test suite requiring the {@link TypeIndex} module.
	 */
	public static Test suite() {
		return ServiceTestSetup.createSetup(TestNavigateActions.class, TypeIndex.Module.INSTANCE);
	}

	/** The given value as the single argument of a chain. */
	private static ActionArguments args(Object value) {
		return ActionArguments.of("input", value);
	}
}
