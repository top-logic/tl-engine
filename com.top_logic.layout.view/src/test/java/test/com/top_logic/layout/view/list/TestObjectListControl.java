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
import com.top_logic.layout.react.servlet.SSEUpdateQueue;
import com.top_logic.layout.react.window.ReactWindowRegistry;
import com.top_logic.layout.view.DefaultViewContext;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.ChannelRef;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.list.ObjectListControl;
import com.top_logic.layout.view.list.ObjectListScope;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLObject;
import com.top_logic.model.TransientObject;
import com.top_logic.model.impl.TLModelImpl;
import com.top_logic.model.util.TLModelUtil;

/**
 * Tests what an {@link ObjectListControl} displays for a container that was deleted.
 *
 * <p>
 * A deleted container is no container: the list displays no element of it, and the element being
 * composed for it is replaced by a fresh one that belongs to nobody.
 * </p>
 */
public class TestObjectListControl extends TestCase {

	/** Name of the channel holding the element an item template displays. */
	private static final String ELEMENT_CHANNEL = "element";

	/** Name of the channel holding the element being composed. */
	private static final String NEW_ELEMENT_CHANNEL = "newElement";

	private MockContainer _container;

	private DefaultViewChannel _containerChannel;

	private Template _itemTemplate;

	private Template _newElementTemplate;

	private RecordingList _list;

	@Override
	protected void setUp() throws Exception {
		super.setUp();

		TLModelImpl model = new TLModelImpl();
		model.addCoreModule();
		TLModule module = TLModelUtil.addModule(model, "test.list");
		TLClass elementType = TLModelUtil.addClass(module, "Element");

		_container = new MockContainer();
		_containerChannel = new DefaultViewChannel("container");
		_containerChannel.set(_container);

		ViewContext context = new DefaultViewContext(new DefaultReactContext("", "test", new SSEUpdateQueue(),
			new ReactWindowRegistry("test")));
		_itemTemplate = new Template(ELEMENT_CHANNEL);
		_newElementTemplate = new Template(NEW_ELEMENT_CHANNEL);
		_list = new RecordingList(context, new ObjectListScope(_containerChannel, null, null), _containerChannel,
			List.of(_itemTemplate), List.of(_newElementTemplate), elementType);
	}

	/**
	 * A list of an alive container displays its elements, followed by the new-element template.
	 */
	public void testAliveContainer() {
		_list.showElements(List.of("element"));

		assertEquals("The item template was instantiated for the element.", 1, _itemTemplate.created().size());
		assertEquals("The element is displayed, followed by the new-element template.",
			List.of(_itemTemplate.created().get(0), _newElementTemplate.created().get(0)), _list.displayed());
		assertSame("The element being composed belongs to the container.", _container,
			composedElement().tContainer());
	}

	/**
	 * A list of a deleted container displays nothing, and composes for nobody.
	 */
	public void testDeletedContainer() {
		_list.showElements(List.of("element"));
		TLObject composedBefore = composedElement();

		_container.setValid(false);
		_list.showElements(List.of("element"));

		assertEquals("The deleted container has no element to display.", List.of(), _list.displayed());
		assertEquals("No item template was instantiated for the deleted container.", 1,
			_itemTemplate.created().size());
		assertNotSame("The element composed for the deleted container is dropped.", composedBefore,
			composedElement());
		assertNull("The element being composed belongs to nobody.", composedElement().tContainer());
	}

	/**
	 * The element currently held by the new-element channel.
	 */
	private TLObject composedElement() {
		return (TLObject) _newElementTemplate.channel().get();
	}

	/**
	 * {@link ObjectListControl} recording the children it displays.
	 */
	private static class RecordingList extends ObjectListControl {

		private List<ReactControl> _displayed = List.of();

		/**
		 * Creates a {@link RecordingList}.
		 */
		public RecordingList(ViewContext context, ObjectListScope scope, ViewChannel container,
				List<UIElement> itemContent, List<UIElement> newElementContent, TLClass elementType) {
			super(context, scope, container, itemContent, newElementContent, ELEMENT_CHANNEL, NEW_ELEMENT_CHANNEL,
				elementType, null);
		}

		@Override
		public void setChildren(List<? extends ReactControl> children) {
			super.setChildren(children);

			_displayed = new ArrayList<>(children);
		}

		/**
		 * The children of the last {@link #setChildren(List)} call.
		 */
		public List<ReactControl> displayed() {
			return _displayed;
		}
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
	 * Container of the list; its validity is what these tests switch.
	 */
	private static class MockContainer extends TransientObject {

		private boolean _valid = true;

		@Override
		public boolean tValid() {
			return _valid;
		}

		/**
		 * Sets whether this container still exists.
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
			ServiceTestSetup.createSetup(TestObjectListControl.class,
				ThreadContextManager.Module.INSTANCE, TypeIndex.Module.INSTANCE));
	}

}
