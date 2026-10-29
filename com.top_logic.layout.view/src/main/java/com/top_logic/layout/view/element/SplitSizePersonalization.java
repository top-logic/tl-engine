/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl.ChildConstraint;
import com.top_logic.layout.structure.OrientationAware.Orientation;

/**
 * The pane sizes and collapse states a user gave a {@link ReactSplitPanelControl}, remembered per
 * user under a personalization key.
 *
 * <p>
 * A split is configured with one {@link ChildConstraint} per pane. When the user drags a splitter,
 * the resulting sizes of all panes are stored together with a {@link #configSignature(List)
 * signature} of the configured constraints, each size in the configured unit of its pane: pixels for
 * a pane sized in pixels, a weight among the percent panes for a pane sized in percent. When the user
 * collapses or expands a pane, its collapse state is stored. The next time the split is built, the stored sizes replace the configured ones -
 * unless the signature has changed in the meantime: then the configuration was edited after the user
 * personalized the split, and the configured sizes take effect again.
 * </p>
 *
 * <p>
 * The values are kept in a {@link Store}, by default the {@link PersonalConfiguration} of the
 * current user: under {@link #SIZES_SUFFIX}, {@link #CONFIG_SIGNATURE_SUFFIX} and
 * {@link #COLLAPSE_SUFFIX} appended to the personalization key. Without a current user nothing is
 * stored, and the configured sizes apply.
 * </p>
 *
 * <p>
 * An instance reads the stored values when it is created, so a control rebuilding its split creates
 * a fresh instance for each build.
 * </p>
 */
public class SplitSizePersonalization {

	/** Suffix of the key the pane sizes are stored under. */
	public static final String SIZES_SUFFIX = ".sizes";

	/** Suffix of the key the {@link #configSignature(List) signature} is stored under. */
	public static final String CONFIG_SIGNATURE_SUFFIX = ".configSignature";

	/** Suffix of the key the collapse states are stored under. */
	public static final String COLLAPSE_SUFFIX = ".collapse";

	/**
	 * Storage of JSON values by key.
	 */
	public interface Store {

		/**
		 * The value stored under the given key, or {@code null} if there is none.
		 */
		Object load(String key);

		/**
		 * Stores the given value under the given key; {@code null} removes the key.
		 */
		void save(String key, Object value);
	}

	/**
	 * {@link Store} keeping the values in the {@link PersonalConfiguration} of the current user.
	 */
	public static final Store PERSONAL_CONFIGURATION = new Store() {
		@Override
		public Object load(String key) {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			return pc == null ? null : pc.getJSONValue(key);
		}

		@Override
		public void save(String key, Object value) {
			PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
			if (pc != null) {
				pc.setJSONValue(key, value);
			}
		}
	};

	private final String _key;

	private final List<ChildConstraint> _configured;

	private final String _configSignature;

	private final Store _store;

	private Map<Integer, Float> _sizes;

	private final Map<Integer, Boolean> _collapsed;

	/**
	 * Creates a {@link SplitSizePersonalization} stored in the {@link #PERSONAL_CONFIGURATION}.
	 *
	 * @see #SplitSizePersonalization(String, List, Store)
	 */
	public SplitSizePersonalization(String key, List<ChildConstraint> configured) {
		this(key, configured, PERSONAL_CONFIGURATION);
	}

	/**
	 * Creates a {@link SplitSizePersonalization} and reads the values stored for it.
	 *
	 * @param key
	 *        The personalization key the values are stored under.
	 * @param configured
	 *        The configured constraint of each pane, in pane order.
	 * @param store
	 *        Where the values are kept.
	 */
	public SplitSizePersonalization(String key, List<ChildConstraint> configured, Store store) {
		_key = key;
		_configured = List.copyOf(configured);
		_configSignature = configSignature(_configured);
		_store = store;
		_sizes = _configSignature.equals(_store.load(_key + CONFIG_SIGNATURE_SUFFIX))
			? loadIndexed(SIZES_SUFFIX, Float.class) : Map.of();
		_collapsed = loadIndexed(COLLAPSE_SUFFIX, Boolean.class);
	}

	/**
	 * A signature of the given configured constraints, which changes whenever one of their size,
	 * unit or minimum size changes.
	 */
	public static String configSignature(List<ChildConstraint> configured) {
		StringBuilder result = new StringBuilder();
		for (ChildConstraint constraint : configured) {
			DisplayUnit unit = constraint.getUnit();
			result.append(constraint.getSize());
			result.append(unit == null ? "" : unit.getExternalName());
			result.append(':');
			result.append(constraint.getMinSize());
			result.append(';');
		}
		return result.toString();
	}

	/**
	 * Creates a split panel reporting size and collapse changes to this instance.
	 *
	 * @param context
	 *        The context of the split panel.
	 * @param orientation
	 *        The layout orientation.
	 * @param resizable
	 *        Whether draggable splitters are shown between the panes.
	 */
	public ReactSplitPanelControl createSplitPanel(ReactContext context, Orientation orientation,
			boolean resizable) {
		return new ReactSplitPanelControl(context, orientation, resizable, this::saveSizes,
			(index, collapsed) -> saveCollapsed(index.intValue(), collapsed.booleanValue()), this::forgetSizes);
	}

	/**
	 * Adds the given content as pane with the given index to the given split panel, with the size and
	 * collapse state the user gave it.
	 *
	 * @param split
	 *        The split panel, see {@link #createSplitPanel(ReactContext, Orientation, boolean)}.
	 * @param index
	 *        The index of the pane in the configured constraints; panes are added in this order.
	 * @param content
	 *        The content of the pane.
	 */
	public void addPane(ReactSplitPanelControl split, int index, ReactControl content) {
		ChildConstraint constraint = constraint(index);
		ChildConstraint configured = _configured.get(index);
		if (isCollapsed(index)) {
			// The pane expands to its configured size.
			split.addChild(content, constraint, true, configured.getSize(), configured.getUnit());
		} else {
			split.addChild(content, constraint);
		}
		split.setDefaultSize(content, configured.getSize(), configured.getUnit());
	}

	/**
	 * The constraint the pane with the given index is laid out with: the size the user dragged it to,
	 * if stored for the current configuration, otherwise the configured size. The pane keeps its
	 * configured unit in both cases.
	 *
	 * <p>
	 * The result is a fresh instance on each call, never one of the configured constraints: a split
	 * panel updates the constraints of its panes while the user drags or collapses them.
	 * </p>
	 */
	public ChildConstraint constraint(int index) {
		ChildConstraint configured = _configured.get(index);
		Float size = _sizes.get(Integer.valueOf(index));
		if (size == null) {
			return new ChildConstraint(configured.getSize(), configured.getUnit(), configured.getMinSize(),
				configured.getScrolling());
		}
		return new ChildConstraint(size.floatValue(), configured.getUnit(), configured.getMinSize(),
			configured.getScrolling());
	}

	/**
	 * Whether the user collapsed the pane with the given index.
	 */
	public boolean isCollapsed(int index) {
		return _collapsed.getOrDefault(Integer.valueOf(index), Boolean.FALSE).booleanValue();
	}

	/**
	 * Stores the pane sizes reported by a split panel after a drag.
	 *
	 * @param controlIdToSize
	 *        The size of each pane in pane order in the unit of the pane, keyed by the ID of the
	 *        pane's control.
	 */
	public void saveSizes(Map<String, Float> controlIdToSize) {
		Map<String, Object> indexed = new HashMap<>();
		int i = 0;
		for (Float size : controlIdToSize.values()) {
			indexed.put(String.valueOf(i), size);
			i++;
		}
		_store.save(_key + SIZES_SUFFIX, indexed);
		_store.save(_key + CONFIG_SIGNATURE_SUFFIX, _configSignature);
	}

	/**
	 * Forgets the pane sizes the user dragged, after the panes were reset to their configured sizes.
	 */
	public void forgetSizes() {
		_sizes = Map.of();
		_store.save(_key + SIZES_SUFFIX, null);
		_store.save(_key + CONFIG_SIGNATURE_SUFFIX, null);
	}

	/**
	 * Stores the collapse state of the pane with the given index.
	 */
	public void saveCollapsed(int index, boolean collapsed) {
		Map<String, Object> stored = new HashMap<>();
		for (Map.Entry<Integer, Boolean> entry : loadIndexed(COLLAPSE_SUFFIX, Boolean.class).entrySet()) {
			stored.put(entry.getKey().toString(), entry.getValue());
		}
		if (collapsed) {
			stored.put(String.valueOf(index), Boolean.TRUE);
		} else {
			stored.remove(String.valueOf(index));
		}
		_store.save(_key + COLLAPSE_SUFFIX, stored.isEmpty() ? null : stored);
	}

	/**
	 * Reads a map from pane index to value of the given type, skipping corrupt entries.
	 */
	private <T> Map<Integer, T> loadIndexed(String suffix, Class<T> valueType) {
		Object value = _store.load(_key + suffix);
		if (!(value instanceof Map<?, ?> raw)) {
			return Map.of();
		}
		Map<Integer, T> result = new HashMap<>();
		for (Map.Entry<?, ?> entry : raw.entrySet()) {
			Object entryValue = entry.getValue();
			if (valueType == Float.class && entryValue instanceof Number number) {
				entryValue = Float.valueOf(number.floatValue());
			}
			if (!valueType.isInstance(entryValue)) {
				continue;
			}
			try {
				result.put(Integer.valueOf(String.valueOf(entry.getKey())), valueType.cast(entryValue));
			} catch (NumberFormatException ex) {
				// Skip corrupt entries.
			}
		}
		return result;
	}

}
