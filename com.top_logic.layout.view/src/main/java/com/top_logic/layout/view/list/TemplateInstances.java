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

import com.top_logic.layout.react.control.IReactControl;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;
import com.top_logic.layout.view.channel.DefaultViewChannel;

/**
 * The instances of a content template, one per element of a displayed list, kept by element.
 *
 * <p>
 * Each instance is the template content instantiated with its element published on a local
 * channel, under a slot path of its own. Bringing the instances in line with a changed list
 * {@link #update(ViewContext, List) reuses} the instance of an element that stays, so its controls
 * keep their state (including transient edit state); only the instances of added elements are
 * built, and those of removed ones are cleaned up.
 * </p>
 *
 * <p>
 * This is the per-element mechanism of every element repeating a template over a computed list,
 * whatever arrangement it places the instances in: the items of an {@link ObjectListElement} as
 * well as the cards of a {@link KanbanBoardElement}.
 * </p>
 */
public class TemplateInstances {

	private final List<UIElement> _content;

	private final String _channelName;

	private final String _slotPrefix;

	/** Instances by element, in the order of the last update. */
	private final Map<Object, ReactControl> _instances = new LinkedHashMap<>();

	/** Allocates stable slot-path segments for the instances. */
	private int _counter;

	/**
	 * Creates {@link TemplateInstances}.
	 *
	 * @param content
	 *        The template content instantiated once per element; multiple entries are stacked
	 *        vertically.
	 * @param channelName
	 *        Name of the local channel holding an instance's element.
	 * @param slotPrefix
	 *        Prefix of the slot-path segment of each instance, which is followed by a number unique
	 *        within these instances.
	 */
	public TemplateInstances(List<UIElement> content, String channelName, String slotPrefix) {
		_content = content;
		_channelName = channelName;
		_slotPrefix = slotPrefix;
	}

	/**
	 * Brings the instances in line with the given elements.
	 *
	 * @param context
	 *        The context new instances are derived from.
	 * @param elements
	 *        The displayed elements, in display order.
	 * @return The instances of the given elements, in their order.
	 */
	public List<ReactControl> update(ViewContext context, List<?> elements) {
		Map<Object, ReactControl> retained = new LinkedHashMap<>();
		List<ReactControl> result = new ArrayList<>(elements.size());
		for (Object element : elements) {
			ReactControl control = retained.get(element);
			if (control == null) {
				control = _instances.remove(element);
			}
			if (control == null) {
				control = createInstance(context, element);
			}
			retained.put(element, control);
			result.add(control);
		}
		for (ReactControl dropped : _instances.values()) {
			dropped.cleanupTree();
		}
		_instances.clear();
		_instances.putAll(retained);
		return result;
	}

	/**
	 * The instance of the given element, as of the last {@link #update(ViewContext, List)};
	 * {@code null} if the element was not given there.
	 */
	public ReactControl get(Object element) {
		return _instances.get(element);
	}

	/**
	 * Instantiates the template content for one element.
	 */
	private ReactControl createInstance(ViewContext context, Object element) {
		DefaultViewChannel elementChannel = new DefaultViewChannel(_channelName);
		elementChannel.set(element);
		ViewContext instanceContext = context.withLocalChannel(_channelName, elementChannel);

		String segment = _slotPrefix + "-" + (_counter++);
		List<ReactControl> controls = new ArrayList<>(_content.size());
		for (int i = 0; i < _content.size(); i++) {
			ViewContext childContext = instanceContext.withChildSlotPath(segment + "." + i);
			IReactControl control = _content.get(i).createControl(childContext);
			controls.add((ReactControl) control);
		}

		return controls.size() == 1 ? controls.get(0) : new ReactStackControl(instanceContext, controls);
	}

}
