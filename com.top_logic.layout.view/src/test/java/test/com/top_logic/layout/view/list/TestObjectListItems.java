/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.list;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestCase;

import test.com.top_logic.ModuleLicenceTestSetup;
import test.com.top_logic.basic.module.ServiceTestSetup;

import com.top_logic.basic.reflect.TypeIndex;
import com.top_logic.basic.thread.ThreadContextManager;
import com.top_logic.layout.react.DefaultReactContext;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactLayoutControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.list.ObjectListItems;
import com.top_logic.layout.view.list.ObjectListScope;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TransientObject;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;

/**
 * Tests what an {@link ObjectListItems} displays for the values its inputs hold.
 *
 * <p>
 * An element is composed to be attached somewhere, so the content for entering one is displayed
 * while every input holds a value, and what is entered is dropped as soon as the inputs hold
 * something else - a deleted object among them being no value at all.
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

	private ReactLayoutControl _display;

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
		assertEquals("The element is displayed, followed by the new-element template.",
			List.of(_itemTemplate.created().get(0), _newElementTemplate.created().get(0)), displayed());
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

		assertEquals("A deleted input is no place to add to.", List.of(), displayed());
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

		assertEquals("An input without a value is no place to add to.",
			List.of(_itemTemplate.created().get(0)), displayed());

		other.set("value");
		items.showElements(List.of("element"));

		assertEquals("Every input holds a value now.",
			List.of(_itemTemplate.created().get(0), _newElementTemplate.created().get(0)), displayed());
	}

	/**
	 * A list without inputs displays its elements and the new-element template: there is nothing
	 * that could be missing.
	 */
	public void testWithoutInputs() {
		ObjectListItems items = list(List.of());
		items.showElements(List.of("element"));

		assertEquals("The elements are displayed, followed by the new-element template.",
			List.of(_itemTemplate.created().get(0), _newElementTemplate.created().get(0)), displayed());
		assertNull("Without an input, the element being composed belongs to nobody.",
			composedElement().tContainer());
	}

	/**
	 * An {@link ObjectListItems} over the given inputs, displaying the item template per element and
	 * the new-element template behind them.
	 */
	private ObjectListItems list(List<ViewChannel> inputs) {
		ObjectListScope scope = new ObjectListScope(inputs, null, null);
		_display = new ReactStackControl(_context, List.of());
		return new ObjectListItems(_context.withScope(ObjectListScope.class, scope), scope, _display, inputs,
			List.of(_itemTemplate), List.of(_newElementTemplate), ELEMENT_CHANNEL, NEW_ELEMENT_CHANNEL,
			_elementType, null);
	}

	/**
	 * The children the list currently displays, in display order.
	 */
	private List<ReactControl> displayed() {
		return _display.displayedChildren();
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
