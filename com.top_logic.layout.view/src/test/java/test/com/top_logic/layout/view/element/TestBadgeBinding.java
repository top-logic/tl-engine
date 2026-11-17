/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;
import test.com.top_logic.layout.view.form.IdentifiedObject;
import test.com.top_logic.layout.view.form.RecordingScope;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.common.BadgeDisplay;
import com.top_logic.layout.react.control.common.BadgeRole;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.element.BadgeBinding;
import com.top_logic.layout.view.element.ValueLabel;
import com.top_logic.layout.view.element.WithBadge;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.listen.ModelScope;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.CompatibilityService;

/**
 * Tests {@link BadgeBinding}: which badge a value of the badge channel shows, and that the badge
 * shown by a control follows the channel and the object on it for as long as the control exists.
 */
@SuppressWarnings("javadoc")
public class TestBadgeBinding extends BasicTestCase {

	/** Name of the channel the badge reads. */
	private static final String COUNT_CHANNEL = "count";

	/** Name of the attribute naming a ticket. */
	private static final String NAME_ATTRIBUTE = "name";

	/** The accessible name given to the badges built here. */
	private static final String LABEL = "New entries";

	private TLClass _ticketType;

	private DefaultViewChannel _count;

	private RecordingScope _scope;

	private ViewContext _context;

	/** The badges handed to the control under test, in order. */
	private List<BadgeDisplay> _shown;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.addModule(model, "test");
		_ticketType = TLModelUtil.addClass(module, "Ticket");
		TLPrimitive string = TLModelUtil.addDatatype(module, module, "String", TLPrimitive.Kind.STRING);
		TLModelUtil.addProperty(_ticketType, NAME_ATTRIBUTE, string);

		_scope = new RecordingScope();
		_count = new DefaultViewChannel(COUNT_CHANNEL);
		_context = new DefaultViewContext(new ObservingReactContext(_scope));
		_context.registerChannel(COUNT_CHANNEL, _count);
		_shown = new ArrayList<>();
	}

	@Override
	protected void tearDown() throws Exception {
		_shown = null;
		_context = null;
		_count = null;
		_scope = null;
		_ticketType = null;

		super.tearDown();
	}

	/**
	 * Tests that a count is shown with its digits, and that nothing to count shows no badge.
	 */
	public void testCounts() {
		assertNull("Nothing to count is worth no badge.", display(Integer.valueOf(0)));
		assertNull(display(Long.valueOf(0)));
		assertEquals(count("1"), display(Integer.valueOf(1)));
		assertEquals(count("99"), display(Integer.valueOf(99)));
	}

	/**
	 * Tests that a count beyond two digits is shown as {@link BadgeBinding#OVERFLOW}.
	 */
	public void testOverflow() {
		assertEquals("99+", BadgeBinding.OVERFLOW);
		assertEquals(count(BadgeBinding.OVERFLOW), display(Integer.valueOf(100)));
		assertEquals(count(BadgeBinding.OVERFLOW), display(Integer.valueOf(150)));
		assertEquals(count(BadgeBinding.OVERFLOW), display(Long.valueOf(100000)));
	}

	/**
	 * Tests that a short text is shown as it is, and an empty text shows no badge.
	 */
	public void testTexts() {
		assertEquals(count("neu"), display("neu"));
		assertNull("An empty text says nothing.", display(""));
	}

	/**
	 * Tests that {@code true} marks the element with a dot, and that {@code false} and no value at
	 * all show no badge.
	 */
	public void testFlags() {
		assertEquals(new BadgeDisplay(null, true, BadgeRole.ERROR, LABEL), display(Boolean.TRUE));
		assertNull(display(Boolean.FALSE));
		assertNull(display(null));
	}

	/**
	 * Tests that a value that is neither a number, a text nor a flag is shown by its label.
	 */
	public void testOtherValuesAreShownByTheirLabel() {
		TLObject ticket = ticket("T-17");

		assertEquals(count(ValueLabel.label(ticket)), display(ticket));
	}

	/**
	 * Tests that the configured role and accessible name reach the badge, and that the role
	 * defaults to the brand.
	 */
	public void testTheDeclarationReachesTheBadge() {
		_count.set(Integer.valueOf(3));

		assertNull("An element declared without a badge has none.", BadgeBinding.create(_context, declaration()));

		WithBadge config = declaration();
		config.update(config.descriptor().getProperty(WithBadge.BADGE), new ChannelRef(COUNT_CHANNEL));
		assertEquals(new BadgeDisplay("3", false, BadgeRole.BRAND, null),
			BadgeBinding.create(_context, config).current());
	}

	/**
	 * Tests that the shown badge follows a new value on the channel.
	 */
	public void testANewChannelValueIsShown() {
		_count.set(Integer.valueOf(3));
		ReactTextControl control = attachedControl();

		_count.set(Integer.valueOf(150));
		assertEquals(count(BadgeBinding.OVERFLOW), lastShown());

		_count.set(Integer.valueOf(0));
		assertNull("A count dropping to zero removes the badge.", lastShown());
		assertEquals(2, _shown.size());

		control.cleanupTree();
	}

	/**
	 * Tests that the shown badge follows a change of the object on the channel, although the
	 * channel keeps pointing to the same object.
	 */
	public void testAChangedObjectIsShown() {
		IdentifiedObject ticket = ticket("T-17");
		_count.set(ticket);
		ReactTextControl control = attachedControl();
		String before = ValueLabel.label(ticket);

		ticket.tUpdateByName(NAME_ATTRIBUTE, "T-18");
		_scope.reportUpdate(ticket);

		assertEquals("The edited object is shown anew.", 1, _shown.size());
		String after = ValueLabel.label(ticket);
		assertEquals(count(after), lastShown());
		assertEquals("T-17", before);
		assertEquals("The label of the ticket follows its name.", "T-18", after);

		control.cleanupTree();
	}

	/**
	 * Tests that a detached control follows no object change, and shows the object as it is when it
	 * is attached again.
	 */
	public void testADetachedControlCatchesUpWhenAttached() {
		IdentifiedObject ticket = ticket("T-17");
		_count.set(ticket);
		ReactTextControl control = attachedControl();
		control.detach();

		ticket.tUpdateByName(NAME_ATTRIBUTE, "T-18");
		_scope.reportUpdate(ticket);
		assertTrue("A detached control follows no object: " + _shown, _shown.isEmpty());

		control.attach();
		assertEquals("The control returning on screen shows the object as it is now.",
			count(ValueLabel.label(ticket)), lastShown());

		control.cleanupTree();
	}

	/**
	 * Tests that disposing the control ends the binding: neither a new channel value nor an object
	 * change reaches it, and no listener is left behind.
	 */
	public void testDisposalEndsTheBinding() {
		IdentifiedObject ticket = ticket("T-17");
		_count.set(ticket);
		ReactTextControl control = attachedControl();
		assertEquals("The attached control observes the object on the channel.", 1, _scope.listeners(ticket).size());

		control.cleanupTree();

		assertTrue("The disposed control leaves no object listener behind.", _scope.listeners(ticket).isEmpty());
		_scope.reportUpdate(ticket);
		_count.set(Integer.valueOf(5));
		assertTrue("Nothing reaches a disposed control: " + _shown, _shown.isEmpty());
	}

	private BadgeDisplay display(Object value) {
		return BadgeBinding.display(value, BadgeRole.ERROR, LABEL);
	}

	private static BadgeDisplay count(String content) {
		return new BadgeDisplay(content, false, BadgeRole.ERROR, LABEL);
	}

	private BadgeDisplay lastShown() {
		assertFalse("No badge was shown.", _shown.isEmpty());
		return _shown.get(_shown.size() - 1);
	}

	/**
	 * A control showing the badge of {@link #COUNT_CHANNEL} in the role
	 * {@link BadgeRole#ERROR} named {@link #LABEL}, attached as a displayed control is.
	 */
	private ReactTextControl attachedControl() {
		WithBadge config = declaration();
		config.update(config.descriptor().getProperty(WithBadge.BADGE), new ChannelRef(COUNT_CHANNEL));
		config.update(config.descriptor().getProperty(WithBadge.BADGE_ROLE), BadgeRole.ERROR);
		config.update(config.descriptor().getProperty(WithBadge.BADGE_LABEL), ResKey.text(LABEL));
		BadgeBinding binding = BadgeBinding.create(_context, config);

		ReactTextControl control = new ReactTextControl(_context, "");
		binding.bind(control, _shown::add);
		control.attach();
		return control;
	}

	private static WithBadge declaration() {
		return TypedConfiguration.newConfigItem(WithBadge.class);
	}

	private IdentifiedObject ticket(String name) {
		IdentifiedObject result = new Ticket(_ticketType);
		result.tUpdateByName(NAME_ATTRIBUTE, name);
		return result;
	}

	/**
	 * A ticket named by its {@link #NAME_ATTRIBUTE}, so that its label follows an edit of it.
	 */
	private static final class Ticket extends IdentifiedObject {

		Ticket(TLClass type) {
			super(type);
		}

		@Override
		public String toString() {
			return (String) tValueByName(NAME_ATTRIBUTE);
		}
	}

	/**
	 * React context whose {@link ModelScope} is the one the test reports changes through.
	 */
	private static final class ObservingReactContext extends DefaultReactContext {

		private final ModelScope _scope;

		ObservingReactContext(ModelScope scope) {
			super("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test"));
			_scope = scope;
		}

		@Override
		public ModelScope getModelScope() {
			return _scope;
		}
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestBadgeBinding.class);
		test = ServiceTestSetup.createSetup(test, CompatibilityService.Module.INSTANCE,
			AttributeSettings.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(test);
	}

}
