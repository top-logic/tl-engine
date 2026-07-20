/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.list;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.common.ReactTextControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;
import com.top_logic.layout.view.channel.ViewChannel;
import com.top_logic.model.TLClass;
import com.top_logic.model.impl.TransientObjectFactory;
import com.top_logic.util.Resources;

/**
 * {@link ReactControl} of an {@link ObjectListElement}: a vertical stack holding one instance of
 * the item template per list element, followed by the new-element template.
 *
 * <p>
 * The displayed element set follows the model: the element function is re-evaluated whenever the
 * container channel or an observed object changes, and the children are updated with keyed reuse -
 * an unchanged element keeps its controls (including transient edit state), only added / removed
 * elements are built / dropped.
 * </p>
 */
public class ObjectListControl extends ReactStackControl {

	private ViewContext _templateContext;

	private final ViewChannel _container;

	private final List<UIElement> _itemContent;

	private final List<UIElement> _newElementContent;

	private final String _elementChannelName;

	private final String _newElementChannelName;

	private final TLClass _elementType;

	private final ResKey _emptyText;

	/** Item controls by list element, in display order. */
	private final Map<Object, ReactControl> _itemControls = new LinkedHashMap<>();

	/** The new-element template's controls, created once and kept across refreshes. */
	private final List<ReactControl> _newElementControls = new ArrayList<>();

	/** Channel holding the transient element the new-element template edits. */
	private DefaultViewChannel _newElementChannel;

	private ReactControl _emptyTextControl;

	/** The container the children were last built for, to reset the new element on a switch. */
	private Object _lastContainer;

	/** Allocates stable slot-path segments for dynamically created item controls. */
	private int _itemCounter;

	/**
	 * Creates an {@link ObjectListControl}.
	 *
	 * @param templateContext
	 *        The context to derive per-item contexts from; carries the list's
	 *        {@link ObjectListScope}.
	 * @param scope
	 *        The list's runtime scope.
	 * @param container
	 *        The channel providing the container object.
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
	 *        Text displayed instead of items when the list is empty, or {@code null} for none.
	 */
	public ObjectListControl(ViewContext templateContext, ObjectListScope scope, ViewChannel container,
			List<UIElement> itemContent, List<UIElement> newElementContent,
			String elementChannelName, String newElementChannelName, TLClass elementType, ResKey emptyText) {
		super(templateContext, List.of());
		_templateContext = templateContext;
		_container = container;
		_itemContent = itemContent;
		_newElementContent = newElementContent;
		_elementChannelName = elementChannelName;
		_newElementChannelName = newElementChannelName;
		_elementType = elementType;
		_emptyText = emptyText;
		_lastContainer = container.get();

		createNewElementControls(scope);
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

		// Publish the pending new element on the shared template context, so that item content (e.g.
		// a reply button) can reference the draft being composed via the new-element channel - not
		// only the new-element content itself.
		_templateContext = _templateContext.withLocalChannel(_newElementChannelName, _newElementChannel);
		for (int i = 0; i < _newElementContent.size(); i++) {
			ViewContext childContext = _templateContext.withChildSlotPath("new-element-" + i);
			ReactControl control = (ReactControl) _newElementContent.get(i).createControl(childContext);
			registerChildControl(control);
			_newElementControls.add(control);
		}
	}

	/**
	 * Fills the new-element channel with a fresh transient element.
	 */
	private void resetNewElement() {
		_newElementChannel.set(TransientObjectFactory.INSTANCE.createObject(_elementType));
	}

	/**
	 * Rebuilds the children for the given list elements with keyed reuse.
	 *
	 * @param elements
	 *        The current list elements, in display order.
	 */
	public void showElements(List<Object> elements) {
		Object container = _container.get();
		if (!Objects.equals(container, _lastContainer)) {
			_lastContainer = container;
			if (_newElementChannel != null) {
				// Entered content belongs to the previous container - start fresh.
				resetNewElement();
			}
		}

		Map<Object, ReactControl> retained = new LinkedHashMap<>();
		List<ReactControl> children = new ArrayList<>();
		if (container != null) {
			for (Object element : elements) {
				ReactControl control = _itemControls.remove(element);
				if (control == null) {
					control = createItemControl(element);
				}
				retained.put(element, control);
				children.add(control);
			}
		}
		for (ReactControl dropped : _itemControls.values()) {
			dropped.cleanupTree();
		}
		_itemControls.clear();
		_itemControls.putAll(retained);

		if (children.isEmpty() && _emptyText != null) {
			if (_emptyTextControl == null) {
				_emptyTextControl =
					new ReactTextControl(_templateContext, Resources.getInstance().getString(_emptyText));
				registerChildControl(_emptyTextControl);
			}
			children.add(_emptyTextControl);
		} else if (_emptyTextControl != null) {
			_emptyTextControl.cleanupTree();
			_emptyTextControl = null;
		}

		if (container != null) {
			children.addAll(_newElementControls);
		}

		setChildren(children);
	}

	/**
	 * Instantiates the item content for one list element.
	 */
	private ReactControl createItemControl(Object element) {
		DefaultViewChannel elementChannel = new DefaultViewChannel(_elementChannelName);
		elementChannel.set(element);
		ViewContext itemContext = _templateContext.withLocalChannel(_elementChannelName, elementChannel);

		String itemSegment = "item-" + (_itemCounter++);
		List<ReactControl> controls = new ArrayList<>(_itemContent.size());
		for (int i = 0; i < _itemContent.size(); i++) {
			ViewContext childContext = itemContext.withChildSlotPath(itemSegment + "." + i);
			IReactControl control = _itemContent.get(i).createControl(childContext);
			controls.add((ReactControl) control);
		}

		ReactControl itemControl =
			controls.size() == 1 ? controls.get(0) : new ReactStackControl(itemContext, controls);
		registerChildControl(itemControl);
		return itemControl;
	}

}
