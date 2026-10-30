/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactLayoutControl;
import com.top_logic.layout.react.control.layout.ReactLayoutControl.Item;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackAlign;
import com.top_logic.layout.react.control.layout.ReactStackControl.StackDirection;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ObservingChannels;
import com.top_logic.layout.view.channel.VetoForwarder;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.layout.view.command.ObservableRule;
import com.top_logic.layout.view.command.ViewExecutabilityRule;
import com.top_logic.layout.view.element.GridOptions;
import com.top_logic.layout.view.list.ObjectListElement.Layout;
import com.top_logic.layout.view.model.ItemSelectionBinding;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.table.SelectionMode;
import com.top_logic.util.Resources;

/**
 * What an {@link ObjectListElement} displays: one instance of the item content per list element,
 * followed by the content for entering a new one.
 *
 * <p>
 * The display is built of two controls, because the arrangement belongs to the elements alone: a
 * {@link ReactLayoutControl container} holding the elements - as a column or as a grid, with each
 * element wrapped in an item - and, around it, a column carrying that container, the text shown
 * instead of elements while there are none, and the content for entering a new element. The empty
 * text and the entry content are therefore no elements of the arrangement: they are not placed in a
 * column of a grid, and they carry no item position. While there are no elements, the container is
 * hidden, so that it takes no place - and no gap - beside the empty text.
 * </p>
 *
 * <p>
 * The displayed element set follows the model: the element function is re-evaluated whenever an
 * input channel or an observed object changes, and the elements are updated with keyed reuse - an
 * unchanged element keeps its controls (including transient edit state), only added / removed
 * elements are built / dropped.
 * </p>
 *
 * <p>
 * The content for entering a new element is displayed while every input of the list holds a value:
 * an element is composed to be attached somewhere, and an input that is unset - or that names an
 * object which was deleted meanwhile - is no place to attach it to. A change of what the inputs hold
 * discards the element being composed, because it was composed for what the list displayed before.
 * </p>
 *
 * <p>
 * A list {@link #bindSelection(SelectionMode, ViewChannel) bound to a selection channel} makes its
 * elements selectable: the container receives each element's control together with the element, and
 * the element of the object the channel holds is displayed as selected.
 * </p>
 *
 * <p>
 * This is the one implementation of that display for every {@link ObjectListElement.Layout layout}:
 * what the layout decides is the container the elements are placed in, nothing else.
 * </p>
 */
public class ObjectListItems {

	/** The column carrying the elements, the empty text and the content for entering an element. */
	private final ReactLayoutControl _display;

	/** The container arranging the elements, and nothing else. */
	private final ReactLayoutControl _elements;

	private ViewContext _templateContext;

	private final List<ViewChannel> _inputs;

	/** The instances of the item content, one per list element. */
	private final TemplateInstances _items;

	private final List<UIElement> _newElementContent;

	private final String _newElementChannelName;

	private final TLClass _elementType;

	private final ResKey _emptyText;

	/** The new-element template's controls, created once and kept across refreshes. */
	private final List<ReactControl> _newElementControls = new ArrayList<>();

	/** Channel holding the transient element the new-element template edits. */
	private DefaultViewChannel _newElementChannel;

	/**
	 * The {@link ObjectListScope#linkAccess() link access} of the list, deciding whether the
	 * new-element content is displayed.
	 */
	private ViewExecutabilityRule _linkAccess = ViewExecutabilityRule.ALWAYS_EXECUTABLE;

	/**
	 * Stops following the {@link #_linkAccess link access}, {@code null} while not followed.
	 */
	private Runnable _linkAccessObservation;

	private ReactControl _emptyTextControl;

	/** The input values the children were last built for, to reset the new element on a change. */
	private List<Object> _lastInputValues;

	/** The displayed elements, in display order. */
	private List<Object> _shownElements = List.of();

	/** The binding of the selected elements to a channel, {@code null} for a display-only list. */
	private ItemSelectionBinding _selectionBinding;

	/**
	 * Creates the {@link ObjectListItems} of a list.
	 *
	 * @param templateContext
	 *        The context to derive per-item contexts from; carries the list's
	 *        {@link ObjectListScope}.
	 * @param scope
	 *        The list's runtime scope.
	 * @param layout
	 *        How the elements are arranged.
	 * @param options
	 *        The options of the arrangement; its gap also separates the elements from the content
	 *        displayed behind them.
	 * @param inputs
	 *        The channels the list's functions are applied to, in declaration order.
	 * @param itemContent
	 *        The content instantiated once per list element.
	 * @param newElementContent
	 *        The content instantiated once for entering a new element; empty for a read-only list.
	 * @param elementChannelName
	 *        Name of the per-item channel holding the item template's element.
	 * @param newElementChannelName
	 *        Name of the channel holding the new-element template's transient element.
	 * @param elementType
	 *        The type of transient elements created for the new-element template, or {@code null}
	 *        when the list has no new-element template.
	 * @param emptyText
	 *        Text displayed instead of the elements when the list is empty, or {@code null} for
	 *        none.
	 */
	public ObjectListItems(ViewContext templateContext, ObjectListScope scope, Layout layout, GridOptions options,
			List<ViewChannel> inputs, List<UIElement> itemContent, List<UIElement> newElementContent,
			String elementChannelName, String newElementChannelName, TLClass elementType, ResKey emptyText) {
		_templateContext = templateContext;
		_elements = layout.createContainer(templateContext, options);
		_display = new ReactStackControl(templateContext, StackDirection.COLUMN, options.getGap(),
			StackAlign.STRETCH, false, List.of(_elements));
		_inputs = inputs;
		_items = new TemplateInstances(itemContent, elementChannelName, "item");
		_newElementContent = newElementContent;
		_newElementChannelName = newElementChannelName;
		_elementType = elementType;
		_emptyText = emptyText;
		_lastInputValues = InputValues.of(inputs);

		createNewElementControls(scope);
	}

	/**
	 * The control displaying the list: the elements in their arrangement, and behind them whatever
	 * the list shows in addition.
	 */
	public ReactLayoutControl display() {
		return _display;
	}

	/**
	 * Makes the elements selectable and binds the selected elements to the given channel: selecting
	 * an element writes its object to the channel, and the element of the object the channel holds
	 * is displayed as selected.
	 *
	 * @param mode
	 *        Whether one element or several may be selected.
	 * @param channel
	 *        The channel holding the selection.
	 */
	public void bindSelection(SelectionMode mode, ViewChannel channel) {
		_shownElements = new ArrayList<>(new LinkedHashSet<>(_shownElements));
		_elements.setItems(items(_shownElements));
		_elements.setSelectionMode(mode);
		_selectionBinding = new ItemSelectionBinding(_elements, channel);
		_display.addCleanupAction(_selectionBinding::dispose);
	}

	/**
	 * The items of the given elements, each with the control of its item content.
	 */
	private List<Item> items(List<Object> elements) {
		List<Item> result = new ArrayList<>(elements.size());
		for (Object element : elements) {
			result.add(new Item(element, _items.get(element)));
		}
		return result;
	}

	/**
	 * Instantiates the new-element template once, bound to a channel holding a fresh transient
	 * element.
	 */
	private void createNewElementControls(ObjectListScope scope) {
		if (_newElementContent.isEmpty() || _elementType == null) {
			return;
		}
		_newElementChannel = new DefaultViewChannel(_newElementChannelName);
		resetNewElement();
		scope.initNewElementReset(this::resetNewElement);
		followLinkAccess(scope.linkAccess());

		// A change of the inputs discards the draft, so the unsaved changes of the new-element
		// content are reported when an input channel is asked, before it is written.
		for (ViewChannel input : _inputs) {
			_display.addCleanupAction(VetoForwarder.forward(input, _newElementChannel));
		}

		// Publish the pending new element on the shared template context, so that item content (e.g.
		// a reply button) can reference the draft being composed via the new-element channel - not
		// only the new-element content itself.
		_templateContext = _templateContext.withLocalChannel(_newElementChannelName, _newElementChannel);

		// The channel serves the whole list, not only the new-element content, which leaves the
		// display while an input is missing: it observes while the list is displayed.
		ObservingChannels.observeWhileDisplayed(_display, List.of(_newElementChannel), _templateContext);
		for (int i = 0; i < _newElementContent.size(); i++) {
			ViewContext childContext = _templateContext.withChildSlotPath("new-element-" + i);
			ReactControl control = (ReactControl) _newElementContent.get(i).createControl(childContext);
			_newElementControls.add(control);
		}
	}

	/**
	 * Follows the link access of the list while it is displayed: a change of what it answers shows
	 * or withdraws the new-element content.
	 */
	private void followLinkAccess(ViewExecutabilityRule linkAccess) {
		_linkAccess = linkAccess;
		if (!(linkAccess instanceof ObservableRule observable)) {
			return;
		}
		_display.addAttachListener(() -> {
			if (_linkAccessObservation == null) {
				_linkAccessObservation = observable.observe(this::updateDisplay);
			}
			updateDisplay();
		});
		Runnable stop = () -> {
			if (_linkAccessObservation != null) {
				_linkAccessObservation.run();
				_linkAccessObservation = null;
			}
		};
		_display.addDetachListener(stop);
		_display.addCleanupAction(stop);
	}

	/**
	 * Whether the new-element content is displayed: every input holds a value, and the
	 * {@link ObjectListElement.Config#getLinkAccess() link access} allows linking the element
	 * being composed.
	 *
	 * <p>
	 * A user who may not link an element in the current context does not see the content composing
	 * one, as a UI unit the user may not see is hidden: there is nothing the content could submit.
	 * </p>
	 */
	private boolean showsNewElement() {
		if (_newElementChannel == null || !InputValues.complete(InputValues.of(_inputs))) {
			return false;
		}
		return _linkAccess.isExecutable(_newElementChannel.get()).isExecutable();
	}

	/**
	 * Fills the new-element channel with a fresh transient element.
	 *
	 * <p>
	 * The element is created with the value of the first input as its {@link TLObject#tContainer()
	 * container} where that value is an object, so that option providers or default-value
	 * expressions of the new element can navigate to it (e.g. restrict a reference to siblings
	 * within the same container).
	 * </p>
	 */
	private void resetNewElement() {
		_newElementChannel.set(TransientObjectFactory.INSTANCE.createObject(_elementType, containerObject()));
	}

	/**
	 * The object a composed element belongs to: the value of the first input where it is an object,
	 * nobody otherwise.
	 */
	private TLObject containerObject() {
		if (_inputs.isEmpty()) {
			return null;
		}
		Object value = InputValues.aliveOrNull(_inputs.get(0).get());
		return value instanceof TLObject object ? object : null;
	}

	/**
	 * Rebuilds the display for the given list elements with keyed reuse.
	 *
	 * @param elements
	 *        The current list elements, in display order.
	 */
	public void showElements(List<Object> elements) {
		List<Object> inputValues = InputValues.of(_inputs);
		if (!inputValues.equals(_lastInputValues)) {
			_lastInputValues = inputValues;
			if (_newElementChannel != null) {
				// Entered content was composed for what the list displayed before - start fresh.
				resetNewElement();
			}
		}

		boolean selectable = _elements.isSelectable();
		// A selectable container displays each object once.
		_shownElements = selectable ? new ArrayList<>(new LinkedHashSet<>(elements)) : elements;
		List<ReactControl> items = _items.update(_templateContext, _shownElements);
		if (selectable) {
			_elements.setItems(items(_shownElements));
		} else {
			_elements.setChildren(items);
		}
		// An empty arrangement takes no place beside the empty text: no gap of the display on
		// either side of it.
		_elements.setHidden(_shownElements.isEmpty());

		if (items.isEmpty() && _emptyText != null) {
			if (_emptyTextControl == null) {
				_emptyTextControl =
					new ReactTextControl(_templateContext, Resources.getInstance().getString(_emptyText));
			}
		} else if (_emptyTextControl != null) {
			_emptyTextControl.cleanupTree();
			_emptyTextControl = null;
		}

		updateDisplay();

		if (_selectionBinding != null) {
			_selectionBinding.refreshed();
		}
	}

	/**
	 * Arranges the display: the elements, the empty text, and the new-element content where it is
	 * {@link #showsNewElement() shown}.
	 */
	private void updateDisplay() {
		// Only the elements are arranged; what is shown in addition to them follows the arrangement
		// as a whole.
		List<ReactControl> displayed = new ArrayList<>();
		displayed.add(_elements);
		if (_emptyTextControl != null) {
			displayed.add(_emptyTextControl);
		}
		if (showsNewElement()) {
			displayed.addAll(_newElementControls);
		}
		_display.setChildren(displayed);
	}

}
