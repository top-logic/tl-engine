/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.list;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactLayoutControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.element.GridOptions;
import com.top_logic.layout.view.list.ObjectListElement;
import com.top_logic.layout.view.list.ObjectListElement.Layout;
import com.top_logic.layout.view.list.ObjectListItems;
import com.top_logic.layout.view.list.ObjectListScope;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TransientObject;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;

/**
 * Tests what an {@link ObjectListItems} displays for the values its inputs hold, and where it
 * displays it.
 *
 * <p>
 * An element is composed to be attached somewhere, so the content for entering one is displayed
 * while every input holds a value, and what is entered is dropped as soon as the inputs hold
 * something else - a deleted object among them being no value at all.
 * </p>
 *
 * <p>
 * The arrangement holds the repeated elements alone: the empty text and the content for entering an
 * element are displayed behind it, not as elements of it.
 * </p>
 */
public class TestObjectListItems extends TestCase {

	/** Name of the channel holding the element an item template displays. */
	private static final String ELEMENT_CHANNEL = "element";

	/** Name of the channel holding the element being composed. */
	private static final String NEW_ELEMENT_CHANNEL = "newElement";

	private MockObject _container;

	private DefaultViewChannel _containerChannel;

	private Template _itemTemplate;

	private Template _newElementTemplate;

	private TLClass _elementType;

	private ViewContext _context;

	private ObjectListItems _list;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.addModule(model, "test.list");
		_elementType = TLModelUtil.addClass(module, "Element");

		_container = new MockObject();
		_containerChannel = new DefaultViewChannel("container");
		_containerChannel.set(_container);

		_context = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")));
		_itemTemplate = new Template(ELEMENT_CHANNEL);
		_newElementTemplate = new Template(NEW_ELEMENT_CHANNEL);
	}

	/**
	 * A list of an alive input displays its elements, followed by the new-element template, and
	 * composes for the object that input holds.
	 */
	public void testAliveInput() {
		ObjectListItems items = list(List.of(_containerChannel));
		items.showElements(List.of("element"));

		assertEquals("The item template was instantiated for the element.", 1, _itemTemplate.created().size());
		assertEquals("The element is placed in the arrangement.",
			List.of(_itemTemplate.created().get(0)), arranged());
		assertEquals("The arrangement is followed by the new-element template.",
			List.of(elements(), _newElementTemplate.created().get(0)), displayed());
		assertSame("The element being composed belongs to the object the input holds.", _container,
			composedElement().tContainer());
	}

	/**
	 * A list whose input holds a deleted object composes for nobody and offers no place to enter a
	 * new element.
	 */
	public void testDeletedInput() {
		ObjectListItems items = list(List.of(_containerChannel));
		items.showElements(List.of("element"));
		TLObject composedBefore = composedElement();

		_container.setValid(false);
		items.showElements(List.of());

		assertEquals("A deleted input has no element to display.", List.of(), arranged());
		assertEquals("A deleted input is no place to add to.", List.of(elements()), displayed());
		assertEquals("No item template was instantiated for the deleted input.", 1,
			_itemTemplate.created().size());
		assertNotSame("The element composed for the deleted input is dropped.", composedBefore,
			composedElement());
		assertNull("The element being composed belongs to nobody.", composedElement().tContainer());
	}

	/**
	 * A change of what the inputs hold starts a fresh element, composed for what the list displays
	 * now.
	 */
	public void testInputChange() {
		DefaultViewChannel other = new DefaultViewChannel("other");
		other.set("first");

		ObjectListItems items = list(List.of(_containerChannel, other));
		items.showElements(List.of());
		TLObject composedBefore = composedElement();

		other.set("second");
		items.showElements(List.of());

		assertNotSame("The element composed for the previous input values is dropped.", composedBefore,
			composedElement());
		assertSame("The fresh element belongs to the object the first input holds.", _container,
			composedElement().tContainer());
	}

	/**
	 * A list that unset input values leave incomplete offers no place to enter a new element.
	 */
	public void testUnsetInput() {
		DefaultViewChannel other = new DefaultViewChannel("other");

		ObjectListItems items = list(List.of(_containerChannel, other));
		items.showElements(List.of("element"));

		assertEquals("The element is displayed.", List.of(_itemTemplate.created().get(0)), arranged());
		assertEquals("An input without a value is no place to add to.", List.of(elements()), displayed());

		other.set("value");
		items.showElements(List.of("element"));

		assertEquals("Every input holds a value now.",
			List.of(elements(), _newElementTemplate.created().get(0)), displayed());
	}

	/**
	 * A list without inputs displays its elements and the new-element template: there is nothing
	 * that could be missing.
	 */
	public void testWithoutInputs() {
		ObjectListItems items = list(List.of());
		items.showElements(List.of("element"));

		assertEquals("The element is placed in the arrangement.",
			List.of(_itemTemplate.created().get(0)), arranged());
		assertEquals("The arrangement is followed by the new-element template.",
			List.of(elements(), _newElementTemplate.created().get(0)), displayed());
		assertNull("Without an input, the element being composed belongs to nobody.",
			composedElement().tContainer());
	}

	/**
	 * The empty text takes the place of the elements, not a place among them.
	 */
	public void testEmptyTextBesideTheArrangement() {
		ObjectListItems items = list(List.of(_containerChannel), Layout.LIST, ResKey.text("Nothing here."));
		items.showElements(List.of());

		assertEquals("Nothing is arranged while the list is empty.", List.of(), arranged());
		assertEquals("The empty text and the new-element template follow the arrangement.", 3,
			displayed().size());
		assertSame("The arrangement leads the display.", elements(), displayed().get(0));
		assertEquals("The new-element template is displayed last.", _newElementTemplate.created().get(0),
			displayed().get(2));

		items.showElements(List.of("element"));

		assertEquals("The element replaces the empty text.",
			List.of(elements(), _newElementTemplate.created().get(0)), displayed());
		assertEquals("The element is placed in the arrangement.",
			List.of(_itemTemplate.created().get(0)), arranged());
	}

	/**
	 * A grid list arranges its elements in a grid, while the content displayed in addition to them
	 * stays out of it.
	 */
	public void testGridArrangement() throws IOException {
		ObjectListItems items = list(List.of(_containerChannel), Layout.GRID, ResKey.text("Nothing here."));
		items.showElements(List.of("element"));

		assertEquals("The list as a whole is displayed as a column.", "TLStack",
			_list.display().getReactModule());
		assertEquals("The elements are arranged in a grid.", "TLGrid", elements().getReactModule());
		assertEquals("Only the element is placed in the grid.",
			List.of(_itemTemplate.created().get(0)), arranged());
		assertEquals("The new-element template is no cell of the grid.",
			List.of(elements(), _newElementTemplate.created().get(0)), displayed());

		items.showElements(List.of());

		assertEquals("The empty text is no cell of the grid.", List.of(), arranged());
		assertTrue("The children of the grid - and only they - are wrapped in items.",
			state(elements()).contains("\"itemClass\":\"" + ObjectListElement.ITEM_CSS_CLASS + "\""));
	}

	/**
	 * The state the given control hands to the client.
	 */
	private static String state(ReactControl control) throws IOException {
		TagWriter out = new TagWriter();
		control.write(out);

		// The state is serialized into an HTML attribute.
		return out.toString().replace("&quot;", "\"");
	}

	/**
	 * An {@link ObjectListItems} over the given inputs, displaying the item template per element and
	 * the new-element template behind them.
	 */
	private ObjectListItems list(List<ViewChannel> inputs) {
		return list(inputs, Layout.LIST, null);
	}

	/**
	 * An {@link ObjectListItems} over the given inputs, arranged as given.
	 *
	 * @param emptyText
	 *        Text displayed while there are no elements, or {@code null} for none.
	 */
	private ObjectListItems list(List<ViewChannel> inputs, Layout layout, ResKey emptyText) {
		ObjectListScope scope = new ObjectListScope(inputs, null, null);
		GridOptions options = TypedConfiguration.newConfigItem(GridOptions.class);
		_list = new ObjectListItems(_context.withScope(ObjectListScope.class, scope), scope, layout, options, inputs,
			List.of(_itemTemplate), List.of(_newElementTemplate), ELEMENT_CHANNEL, NEW_ELEMENT_CHANNEL,
			_elementType, emptyText);
		return _list;
	}

	/**
	 * The children of the control the list displays as a whole, in display order: the arrangement of
	 * the elements, followed by what is shown in addition to them.
	 */
	private List<ReactControl> displayed() {
		return _list.display().displayedChildren();
	}

	/**
	 * The container arranging the elements: the first child of the displayed control.
	 */
	private ReactLayoutControl elements() {
		return (ReactLayoutControl) displayed().get(0);
	}

	/**
	 * The item controls placed in the arrangement, in display order.
	 */
	private List<ReactControl> arranged() {
		return elements().displayedChildren();
	}

	/**
	 * The element currently held by the new-element channel.
	 */
	private TLObject composedElement() {
		return (TLObject) _newElementTemplate.channel().get();
	}

	/**
	 * Template creating a bare control per instantiation, remembering the channel it is bound to.
	 */
	private static class Template implements UIElement {

		private final String _channelName;

		private final List<IReactControl> _created = new ArrayList<>();

		private ViewChannel _channel;

		/**
		 * Creates a {@link Template}.
		 *
		 * @param channelName
		 *        Name of the channel the enclosing list binds this template to.
		 */
		public Template(String channelName) {
			_channelName = channelName;
		}

		@Override
		public IReactControl createControl(ViewContext context) {
			_channel = context.resolveChannel(new ChannelRef(_channelName));

			ReactControl control = new ReactControl(context, null, "TLPanel");
			_created.add(control);
			return control;
		}

		/**
		 * The controls created so far, in creation order.
		 */
		public List<IReactControl> created() {
			return _created;
		}

		/**
		 * The channel the last created control is bound to.
		 */
		public ViewChannel channel() {
			return _channel;
		}
	}

	/**
	 * Object an input of the list holds; its validity is what these tests switch.
	 */
	private static class MockObject extends TransientObject {

		private boolean _valid = true;

		@Override
		public boolean tValid() {
			return _valid;
		}

		/**
		 * Sets whether this object still exists.
		 */
		public void setValid(boolean valid) {
			_valid = valid;
		}
	}

	/**
	 * The suite of tests.
	 */
	public static Test suite() {
		return ModuleLicenceTestSetup.setupModule(
			ServiceTestSetup.createSetup(TestObjectListItems.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE));
	}

}
