/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.configedit;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyDescriptorImpl;
import com.top_logic.basic.config.PropertyKind;
import com.top_logic.basic.config.container.ConfigPart;

/**
 * The way from the root of a configuration tree down to one of its parts, recorded so it can be
 * replayed on a {@link com.top_logic.basic.config.TypedConfiguration#copy(ConfigurationItem) copy}
 * of that root.
 *
 * <p>
 * A working copy for editing has to be a copy of the whole tree, not of the edited part alone: a
 * part copied on its own has no {@link ConfigPart#container() container} - the container is set
 * only when a part is assigned into a property ({@code AbstractConfigItem#updateDirectly} calls
 * {@link com.top_logic.basic.config.container.ConfigPartUtilInternal#initContainer(Object,
 * ConfigurationItem)}) - so a standalone copy was never assigned anywhere, and anything inside it
 * that navigates upwards or sideways (an option function, a derived property, a constraint) would
 * find nothing. Copying the root keeps all of that intact. This class is what lets the editor still
 * be built over the *part* the caller actually named: {@link #to(ConfigurationItem)} records the
 * way down to that part as a list of steps, and {@link #resolveIn(ConfigurationItem)} replays those
 * steps on a copy of the root to find the corresponding part there.
 * </p>
 *
 * <p>
 * Each step is recorded by property <em>name</em>, not by {@link PropertyDescriptor}: a copy's
 * descriptor hands out its own {@link PropertyDescriptor} instances, so an instance from the
 * original tree would never be found again in the copy, while names survive the copy unchanged.
 * </p>
 */
public final class ConfigItemPath {

	/** One step of the way down: the property, and where in it the child sits. */
	private record Step(String propertyName, int index) {
		// index is -1 for an ITEM property, and the position among the elements otherwise - for a
		// MAP property, the position among its values in iteration order.
	}

	private final ConfigurationItem _root;

	private final List<Step> _steps;

	private ConfigItemPath(ConfigurationItem root, List<Step> steps) {
		_root = root;
		_steps = steps;
	}

	/**
	 * Records the way from the root of the tree containing the given item down to that item.
	 *
	 * <p>
	 * Walks upwards from the given item: while the current item is a {@link ConfigPart} whose
	 * {@link ConfigPart#container()} is not {@code null}, finds which of the container's ITEM,
	 * LIST, ARRAY or MAP properties holds it - by identity, never by equality - and prepends that
	 * step. An item that is not a {@link ConfigPart}, or whose container is {@code null}, is the
	 * root; the way to such an item is empty.
	 * </p>
	 *
	 * @param item
	 *        The item to find the way to. Must not be {@code null}.
	 * @return The way from {@link #root()} down to the given item.
	 */
	public static ConfigItemPath to(ConfigurationItem item) {
		List<Step> steps = new ArrayList<>();
		ConfigurationItem current = item;
		while (current instanceof ConfigPart part && part.container() != null) {
			ConfigurationItem container = part.container();
			steps.add(0, stepTo(container, current));
			current = container;
		}
		return new ConfigItemPath(current, steps);
	}

	/** The root of the tree the recorded way starts from. */
	public ConfigurationItem root() {
		return _root;
	}

	/**
	 * Replays the recorded way on a copy of {@link #root()}, to find the part corresponding to the
	 * one originally passed to {@link #to(ConfigurationItem)}.
	 *
	 * @param rootCopy
	 *        A copy of {@link #root()}.
	 * @return The part of {@code rootCopy} found by replaying the recorded way.
	 */
	public ConfigurationItem resolveIn(ConfigurationItem rootCopy) {
		ConfigurationItem current = rootCopy;
		for (Step step : _steps) {
			current = descend(current, step);
		}
		return current;
	}

	/**
	 * Finds which of the container's ITEM, LIST, ARRAY or MAP properties holds the given child, by
	 * identity, and at which position.
	 */
	private static Step stepTo(ConfigurationItem container, ConfigurationItem child) {
		for (PropertyDescriptor property : container.descriptor().getProperties()) {
			switch (property.kind()) {
				case ITEM: {
					if (container.value(property) == child) {
						return new Step(property.getPropertyName(), -1);
					}
					break;
				}
				case LIST: {
					int index = indexByIdentity((List<?>) container.value(property), child);
					if (index >= 0) {
						return new Step(property.getPropertyName(), index);
					}
					break;
				}
				case ARRAY: {
					Object value = container.value(property);
					int index = value == null ? -1 : indexByIdentity(PropertyDescriptorImpl.arrayAsList(value), child);
					if (index >= 0) {
						return new Step(property.getPropertyName(), index);
					}
					break;
				}
				case MAP: {
					Map<?, ?> map = (Map<?, ?>) container.value(property);
					int index = map == null ? -1 : indexByIdentity(new ArrayList<>(map.values()), child);
					if (index >= 0) {
						return new Step(property.getPropertyName(), index);
					}
					break;
				}
				default:
					// Not a containment property.
			}
		}
		throw new IllegalStateException(
			"No property of '" + container + "' holds '" + child + "' - the tree is not what it claimed to be.");
	}

	/** The position of the given element in the given collection, found by identity, or {@code -1}. */
	private static int indexByIdentity(List<?> elements, Object child) {
		for (int i = 0; i < elements.size(); i++) {
			if (elements.get(i) == child) {
				return i;
			}
		}
		return -1;
	}

	/** Reads the property named by the given step in the given item, and takes its recorded position. */
	private static ConfigurationItem descend(ConfigurationItem item, Step step) {
		PropertyDescriptor property = item.descriptor().getProperty(step.propertyName());
		if (step.index() < 0) {
			return (ConfigurationItem) item.value(property);
		}
		if (property.kind() == PropertyKind.ARRAY) {
			return (ConfigurationItem) PropertyDescriptorImpl.arrayAsList(item.value(property)).get(step.index());
		}
		if (property.kind() == PropertyKind.MAP) {
			Map<?, ?> map = (Map<?, ?>) item.value(property);
			return (ConfigurationItem) new ArrayList<>(map.values()).get(step.index());
		}
		return (ConfigurationItem) ((List<?>) item.value(property)).get(step.index());
	}

}
