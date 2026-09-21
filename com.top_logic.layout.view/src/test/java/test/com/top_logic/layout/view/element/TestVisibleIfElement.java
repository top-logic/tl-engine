/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import junit.framework.Test;

import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.knowledge.KBSetup;

import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.DefaultInstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.io.BinaryContent;
import com.top_logic.basic.io.binary.ClassRelativeBinaryContent;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactInsetControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.ViewElement;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.InsetElement;
import com.top_logic.layout.view.element.VisibleIfElement;
import com.top_logic.model.search.expr.config.ExprFormat;
import com.top_logic.model.search.expr.config.SearchBuilder;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.util.model.ModelService;

/**
 * Tests what a {@code <visible-if>} shows: its content while the condition over the input channel
 * holds, and nothing while it does not.
 *
 * <p>
 * The content is built and disposed as the condition changes, so what a hidden condition leaves
 * behind is tested as well - the control of the content it showed is gone, not merely undisplayed.
 * </p>
 */
public class TestVisibleIfElement extends BasicTestCase {

	/** Name of the channel the condition is evaluated over. */
	private static final String STATE_CHANNEL = "state";

	/** The channel value the condition of the tests holds for. */
	private static final String SHOWN = "shown";

	/** A channel value the condition of the tests does not hold for. */
	private static final String HIDDEN = "hidden";

	private ViewChannel _state;

	private SSEUpdateQueue _queue;

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		_state = new DefaultViewChannel(STATE_CHANNEL);
		_queue = new SSEUpdateQueue();
		_context = new DefaultViewContext(
			new DefaultReactContext("", "test", _queue, new ReactWindowRegistry("test")));
		_context.registerChannel(STATE_CHANNEL, _state);
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_queue = null;
		_state = null;

		super.tearDown();
	}

	/**
	 * Tests that the content is built while the condition holds.
	 */
	public void testContentIsShownWhileTheConditionHolds() {
		_state.set(SHOWN);

		ReactControl visibleIf = createControl();

		assertTrue("The content is displayed while the condition holds.",
			shownContent(visibleIf) instanceof ReactInsetControl);
	}

	/**
	 * Tests that no content is built while the condition does not hold.
	 */
	public void testNothingIsShownWhileTheConditionDoesNotHold() {
		_state.set(HIDDEN);

		ReactControl visibleIf = createControl();

		assertNull("Nothing is displayed while the condition does not hold.", shownContent(visibleIf));
	}

	/**
	 * Tests that a channel value ending the condition takes the content away and disposes it, and
	 * that a value bringing the condition back builds content anew.
	 */
	public void testTheContentFollowsTheChannelValue() {
		_state.set(SHOWN);
		ReactControl visibleIf = createControl();
		visibleIf.attach();
		ReactControl shown = shownContent(visibleIf);

		_state.set(HIDDEN);

		assertNull("The content is taken away when the condition stops holding.", shownContent(visibleIf));
		assertFalse("The content that was taken away is not displayed any more.", shown.isAttached());
		assertNull("The content that was taken away is disposed, not kept alive.",
			_queue.getControl(shown.getID()));

		_state.set(SHOWN);
		ReactControl again = shownContent(visibleIf);

		assertTrue("The condition holding again builds the content anew.", again instanceof ReactInsetControl);
		assertNotSame("The content is built anew rather than resurrected.", shown, again);
	}

	/**
	 * Tests that a {@code <visible-if>} with its content written directly inside the tag parses and
	 * instantiates.
	 */
	public void testParseVisibleIfView() throws Exception {
		DefaultInstantiationContext context = new DefaultInstantiationContext(TestVisibleIfElement.class);

		Map<String, ConfigurationDescriptor> descriptors = Collections.singletonMap(
			"view", TypedConfiguration.getConfigurationDescriptor(ViewElement.Config.class));

		BinaryContent source = new ClassRelativeBinaryContent(TestVisibleIfElement.class, "test-visible-if.view.xml");

		ConfigurationReader reader = new ConfigurationReader(context, descriptors);
		reader.setSource(source);
		ViewElement.Config config = (ViewElement.Config) reader.read();
		context.checkErrors();

		assertTrue("The content of the view is a <visible-if>.",
			config.getContent() instanceof VisibleIfElement.Config);
		VisibleIfElement.Config visibleIf = (VisibleIfElement.Config) config.getContent();

		assertEquals("The condition is evaluated over the configured channel.",
			STATE_CHANNEL, visibleIf.getInput().getChannelName());
		assertNotNull("The condition is parsed.", visibleIf.getExpr());

		List<PolymorphicConfiguration<? extends UIElement>> content = visibleIf.getContent();
		assertEquals("The children of the tag are its content.", 1, content.size());
		assertTrue("The child of the tag is the <inset> written inside it.",
			content.get(0) instanceof InsetElement.Config);

		UIElement element = context.getInstance(config);
		context.checkErrors();
		assertTrue("The configuration builds a view.", element instanceof ViewElement);
	}

	/**
	 * The control of a {@code <visible-if>} over {@link #STATE_CHANNEL} showing an {@code <inset>}
	 * while the channel holds {@link #SHOWN}.
	 */
	private ReactControl createControl() {
		VisibleIfElement.Config config = TypedConfiguration.newConfigItem(VisibleIfElement.Config.class);
		update(config, VisibleIfElement.Config.INPUT, new ChannelRef(STATE_CHANNEL));
		update(config, VisibleIfElement.Config.EXPR, expr("x -> $x == '" + SHOWN + "'"));
		config.getContent().add(TypedConfiguration.newConfigItem(InsetElement.Config.class));

		DefaultInstantiationContext instantiation = new DefaultInstantiationContext(TestVisibleIfElement.class);
		UIElement element = instantiation.getInstance(config);
		IReactControl control = element.createControl(_context);
		return (ReactControl) control;
	}

	/**
	 * The content the given {@code <visible-if>} control currently displays, or {@code null} when it
	 * displays none.
	 *
	 * @implNote Content that is hidden is an empty {@link ReactStackControl}: the deck always renders
	 *           an active child, and the one standing for "nothing" has nothing in it.
	 */
	private static ReactControl shownContent(ReactControl visibleIf) {
		List<ReactControl> children = visibleIf.displayedChildren();
		assertEquals("A deck displays exactly one child.", 1, children.size());
		ReactControl child = children.get(0);
		if (child instanceof ReactStackControl && child.displayedChildren().isEmpty()) {
			return null;
		}
		return child;
	}

	private static void update(VisibleIfElement.Config config, String propertyName, Object value) {
		PropertyDescriptor property = config.descriptor().getProperty(propertyName);
		config.update(property, value);
	}

	/** The given TL-Script source as the configuration reads it. */
	private static Expr expr(String source) {
		try {
			return ExprFormat.INSTANCE.getValue(VisibleIfElement.Config.EXPR, source);
		} catch (ConfigurationException ex) {
			throw new AssertionError("Not a TL-Script expression: " + source, ex);
		}
	}

	/**
	 * The suite of tests.
	 *
	 * @implNote The condition is a TL-Script expression, whose compilation and evaluation need the
	 *           application model and the {@link com.top_logic.knowledge.service.KnowledgeBase} it
	 *           is executed against.
	 */
	public static Test suite() {
		return KBSetup.getSingleKBTest(TestVisibleIfElement.class,
			ServiceTestSetup.createStarterFactoryForModules(
				TypeIndex.Module.INSTANCE, SearchBuilder.Module.INSTANCE, ModelService.Module.INSTANCE));
	}

}
