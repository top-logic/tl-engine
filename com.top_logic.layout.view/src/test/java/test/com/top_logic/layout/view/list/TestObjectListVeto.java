/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.list;

import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.ChannelVetoException;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.GridOptions;
import com.top_logic.layout.view.form.StateHandler;
import com.top_logic.layout.view.list.ObjectListElement.Layout;
import com.top_logic.layout.view.list.ObjectListItems;
import com.top_logic.layout.view.list.ObjectListScope;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.annotate.util.AttributeSettings;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.model.CompatibilityService;

/**
 * Tests that the unsaved changes of the new-element form of an {@link ObjectListItems} are reported
 * when an input channel of the list is asked, before that input is written.
 *
 * <p>
 * A change of what the inputs hold discards the draft the new-element form holds, so the user
 * decides about it while the list still displays what the draft was composed for; after a discard,
 * the continuation of the {@link ChannelVetoException} writes the input and the list starts the next
 * draft for what it displays then.
 * </p>
 */
public class TestObjectListVeto extends BasicTestCase {

	/** Name of the channel holding the object whose elements are listed. */
	private static final String CONTAINER_CHANNEL = "ticket";

	/** Name of a second input of the list, narrowing what it displays. */
	private static final String FILTER_CHANNEL = "filter";

	/** Name of the channel publishing an element to the item content. */
	private static final String ELEMENT_CHANNEL = "element";

	/** Name of the channel holding the draft the new-element content edits. */
	private static final String NEW_ELEMENT_CHANNEL = "new-element";

	private TLClass _commentType;

	private TLObject _ticketA;

	private TLObject _ticketB;

	private ViewChannel _container;

	private ViewChannel _filter;

	private ViewContext _context;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.addModule(model, "test");

		TLClass ticketType = TLModelUtil.addClass(module, "Ticket");
		_commentType = TLModelUtil.addClass(module, "Comment");

		_ticketA = TransientObjectFactory.INSTANCE.createObject(ticketType, null);
		_ticketB = TransientObjectFactory.INSTANCE.createObject(ticketType, null);

		_context = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue(), new ReactWindowRegistry("test")));
		_container = new DefaultViewChannel(CONTAINER_CHANNEL);
		_context.registerChannel(CONTAINER_CHANNEL, _container);
		_container.set(_ticketA);

		_filter = new DefaultViewChannel(FILTER_CHANNEL);
		_context.registerChannel(FILTER_CHANNEL, _filter);
		_filter.set("open");
	}

	@Override
	protected void tearDown() throws Exception {
		_context = null;
		_container = null;
		_filter = null;
		_commentType = null;
		_ticketA = null;
		_ticketB = null;

		super.tearDown();
	}

	/**
	 * Tests that the write of an input is blocked by the unsaved changes of the new-element form,
	 * and that the continuation completes the switch including the fresh draft.
	 */
	public void testInputSwitchAsksTheDraftForm() {
		DraftForm form = new DraftForm();
		form.enterText();
		list(form, List.of(_container));

		TLObject draft = draft(form);
		assertSame("The draft belongs to the container the list displays.", _ticketA, draft.tContainer());
		assertEquals("The unsaved changes of the draft must be visible on the container channel.",
			List.of(form), _container.dirtyHandlers());

		ChannelVetoException caught = null;
		try {
			_container.set(_ticketB);
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException ex) {
			caught = ex;
		}

		assertEquals("The form holding the draft must block the switch.", List.of(form),
			caught.getDirtyHandlers());
		assertSame("The container must not be written while the user decides.", _ticketA, _container.get());
		assertSame("The draft must be kept while the user decides.", draft, draft(form));

		form.executeDiscard();
		caught.getContinuation().run();

		assertSame("The continuation must complete the switch.", _ticketB, _container.get());
		TLObject nextDraft = draft(form);
		assertNotSame("The draft of the container left behind must be dropped.", draft, nextDraft);
		assertSame("The next draft belongs to the container now displayed.", _ticketB, nextDraft.tContainer());
	}

	/**
	 * Tests that a new-element form without unsaved changes lets the container switch happen.
	 */
	public void testCleanDraftFormDoesNotBlock() {
		DraftForm form = new DraftForm();
		list(form, List.of(_container));

		assertEquals("A form without unsaved changes has nothing to report.", List.of(),
			_container.dirtyHandlers());
		assertTrue("The switch must happen.", _container.set(_ticketB));
		assertSame(_ticketB, _container.get());
		assertSame("The next draft belongs to the container now displayed.", _ticketB,
			draft(form).tContainer());
	}

	/**
	 * Tests that a list without a new-element template leaves the container channel unquestioned.
	 */
	public void testReadOnlyListAsksNothing() {
		List<ViewChannel> inputs = List.of(_container);
		ObjectListScope scope = new ObjectListScope(inputs, null, null);
		new ObjectListItems(_context.withScope(ObjectListScope.class, scope), scope, Layout.LIST, gridOptions(),
			inputs, List.of(), List.of(), ELEMENT_CHANNEL, NEW_ELEMENT_CHANNEL, null, null);

		assertEquals("A list without a draft has nothing to report.", List.of(), _container.dirtyHandlers());
		assertTrue("The switch must happen.", _container.set(_ticketB));
	}

	/**
	 * Tests that every input of the list reports the unsaved changes of the draft, because the draft
	 * is composed for what all of them hold together.
	 */
	public void testEveryInputAsksTheDraftForm() {
		DraftForm form = new DraftForm();
		form.enterText();
		list(form, List.of(_container, _filter));

		assertEquals("The unsaved changes of the draft must be visible on the first input.",
			List.of(form), _container.dirtyHandlers());
		assertEquals("The unsaved changes of the draft must be visible on the second input.",
			List.of(form), _filter.dirtyHandlers());

		try {
			_filter.set("closed");
			fail("Expected ChannelVetoException");
		} catch (ChannelVetoException expected) {
			assertEquals("The form holding the draft must block the switch.", List.of(form),
				expected.getDirtyHandlers());
		}
	}

	/**
	 * An object list over the given inputs whose new-element content is the given form.
	 *
	 * <p>
	 * The listeners refreshing the display stand in for the
	 * {@link com.top_logic.layout.view.model.RowSourceObserver} the
	 * {@link com.top_logic.layout.view.list.ObjectListElement} attaches to the input channels.
	 * </p>
	 */
	private ObjectListItems list(DraftForm form, List<ViewChannel> inputs) {
		ObjectListScope scope = new ObjectListScope(inputs, null, null);
		ObjectListItems items =
			new ObjectListItems(_context.withScope(ObjectListScope.class, scope), scope, Layout.LIST, gridOptions(),
				inputs, List.of(), List.of(form), ELEMENT_CHANNEL, NEW_ELEMENT_CHANNEL, _commentType, null);
		items.showElements(List.of());
		for (ViewChannel input : inputs) {
			input.addListener((sender, oldValue, newValue) -> items.showElements(List.of()));
		}
		return items;
	}

	/**
	 * The arrangement options of a list that displays its elements as a plain column.
	 */
	private static GridOptions gridOptions() {
		return TypedConfiguration.newConfigItem(GridOptions.class);
	}

	/**
	 * The element the new-element content of the given form currently edits.
	 */
	private static TLObject draft(DraftForm form) {
		return (TLObject) form.draftChannel().get();
	}

	/**
	 * Stand-in for the new-element content: a form over the channel holding the draft, which
	 * registers its unsaved changes there, exactly where a {@code <form>} bound to that channel
	 * registers them.
	 */
	private static class DraftForm implements UIElement, StateHandler {

		private boolean _dirty;

		private ViewChannel _draftChannel;

		@Override
		public IReactControl createControl(ViewContext context) {
			_draftChannel = context.resolveChannel(new ChannelRef(NEW_ELEMENT_CHANNEL));
			_draftChannel.addVetoListener(
				(sender, oldValue, newValue) -> _dirty ? List.of(this) : List.<StateHandler> of());
			return new ReactTextControl(context, "draft");
		}

		/**
		 * The channel this form edits.
		 */
		ViewChannel draftChannel() {
			return _draftChannel;
		}

		/**
		 * Simulates input the user has not stored yet.
		 */
		void enterText() {
			_dirty = true;
		}

		@Override
		public boolean isDirty() {
			return _dirty;
		}

		@Override
		public boolean hasErrors() {
			return false;
		}

		@Override
		public void executeSave() {
			_dirty = false;
		}

		@Override
		public void executeDiscard() {
			_dirty = false;
		}

		@Override
		public String getDescription() {
			return "new comment";
		}
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		Test test = new TestSuite(TestObjectListVeto.class);
		test = ServiceTestSetup.createSetup(test, CompatibilityService.Module.INSTANCE,
			AttributeSettings.Module.INSTANCE);
		return TLTestSetup.createTLTestSetup(test);
	}

}
