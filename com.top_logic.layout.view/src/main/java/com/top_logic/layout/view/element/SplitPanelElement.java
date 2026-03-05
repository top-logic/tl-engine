/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.view.element;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.config.annotation.defaults.FloatDefault;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.layout.react.ViewControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl.ChildConstraint;
import com.top_logic.layout.react.control.layout.ReactStackControl;
import com.top_logic.layout.structure.OrientationAware.Orientation;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.view.UIElement;
import com.top_logic.layout.view.ViewContext;

/**
 * UIElement that wraps {@link ReactSplitPanelControl}.
 *
 * <p>
 * Renders a split panel with draggable splitters between child panes. Each {@code <pane>} defines
 * its size, unit, and content elements.
 * </p>
 */
public class SplitPanelElement implements UIElement {

	/**
	 * Configuration for {@link SplitPanelElement}.
	 */
	@TagName("split-panel")
	public interface Config extends UIElement.Config {

		@Override
		@ClassDefault(SplitPanelElement.class)
		Class<? extends UIElement> getImplementationClass();

		/** Configuration name for {@link #getOrientation()}. */
		String ORIENTATION = "orientation";

		/** Configuration name for {@link #getResizable()}. */
		String RESIZABLE = "resizable";

		/** Configuration name for {@link #getPanes()}. */
		String PANES = "panes";

		/**
		 * The layout orientation: "horizontal" or "vertical".
		 */
		@Name(ORIENTATION)
		@StringDefault("horizontal")
		String getOrientation();

		/**
		 * Whether draggable splitters are shown between panes.
		 */
		@Name(RESIZABLE)
		@BooleanDefault(true)
		boolean getResizable();

		/**
		 * The child pane definitions.
		 */
		@Name(PANES)
		List<PaneConfig> getPanes();
	}

	/**
	 * Configuration for a single pane in the split panel.
	 */
	@TagName("pane")
	public interface PaneConfig extends com.top_logic.basic.config.ConfigurationItem {

		/** Configuration name for {@link #getSize()}. */
		String SIZE = "size";

		/** Configuration name for {@link #getUnit()}. */
		String UNIT = "unit";

		/** Configuration name for {@link #getMinSize()}. */
		String MIN_SIZE = "min-size";

		/** Configuration name for {@link #getChildren()}. */
		String CHILDREN = "children";

		/**
		 * The pane size in the given unit.
		 */
		@Name(SIZE)
		@FloatDefault(50f)
		float getSize();

		/**
		 * The size unit: "%" or "px".
		 */
		@Name(UNIT)
		@StringDefault("%")
		String getUnit();

		/**
		 * The minimum size in pixels (0 for no minimum).
		 */
		@Name(MIN_SIZE)
		@IntDefault(0)
		int getMinSize();

		/**
		 * The content elements in this pane.
		 */
		@Name(CHILDREN)
		List<PolymorphicConfiguration<? extends UIElement>> getChildren();
	}

	private final String _orientation;

	private final boolean _resizable;

	private final List<PaneEntry> _panes;

	/**
	 * Creates a new {@link SplitPanelElement} from configuration.
	 */
	@CalledByReflection
	public SplitPanelElement(InstantiationContext context, Config config) {
		_orientation = config.getOrientation();
		_resizable = config.getResizable();
		_panes = config.getPanes().stream()
			.map(paneConfig -> {
				List<UIElement> children = paneConfig.getChildren().stream()
					.map(context::getInstance)
					.collect(Collectors.toList());
				return new PaneEntry(paneConfig.getSize(), paneConfig.getUnit(),
					paneConfig.getMinSize(), children);
			})
			.collect(Collectors.toList());
	}

	@Override
	public ViewControl createControl(ViewContext context) {
		String key = resolveKey(context, "split-panel");

		Map<Integer, Float> persistedSizes = loadPaneSizes(key);

		Orientation orientation = "vertical".equals(_orientation) ? Orientation.VERTICAL : Orientation.HORIZONTAL;

		// TODO: Restore persisted collapse states per child. Currently ReactSplitPanelControl
		// does not offer a public API to set initial collapse state per-child at construction time.
		ReactSplitPanelControl splitPanel = new ReactSplitPanelControl(orientation, _resizable,
			sizes -> savePaneSizes(key, sizes),
			(idx, collapsed) -> saveCollapseState(key, idx, collapsed));

		for (int i = 0; i < _panes.size(); i++) {
			PaneEntry pane = _panes.get(i);
			float size = persistedSizes.containsKey(Integer.valueOf(i))
				? persistedSizes.get(Integer.valueOf(i)).floatValue() : pane._size;
			DisplayUnit unit = persistedSizes.containsKey(Integer.valueOf(i))
				? DisplayUnit.PIXEL : ("%".equals(pane._unit) ? DisplayUnit.PERCENT : DisplayUnit.PIXEL);
			ChildConstraint constraint = new ChildConstraint(size, unit, pane._minSize, Scrolling.AUTO);
			ReactControl content = createContent(pane._children, context);
			splitPanel.addChild(content, constraint);
		}

		return splitPanel;
	}

	private static String resolveKey(ViewContext context, String defaultSegment) {
		return context.getPersonalizationKey() + "." + defaultSegment;
	}

	@SuppressWarnings("unchecked")
	private static Map<Integer, Float> loadPaneSizes(String key) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return Map.of();
		}
		Object value = pc.getJSONValue(key + ".sizes");
		if (value instanceof Map) {
			Map<String, Object> raw = (Map<String, Object>) value;
			Map<Integer, Float> result = new HashMap<>();
			for (Map.Entry<String, Object> entry : raw.entrySet()) {
				try {
					int index = Integer.parseInt(entry.getKey());
					if (entry.getValue() instanceof Number) {
						result.put(Integer.valueOf(index), Float.valueOf(((Number) entry.getValue()).floatValue()));
					}
				} catch (NumberFormatException e) {
					// Skip corrupt entries.
				}
			}
			return result;
		}
		return Map.of();
	}

	private static void savePaneSizes(String key, Map<String, Float> controlIdToSize) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}
		Map<String, Object> indexed = new HashMap<>();
		int i = 0;
		for (Map.Entry<String, Float> entry : controlIdToSize.entrySet()) {
			indexed.put(String.valueOf(i), entry.getValue());
			i++;
		}
		pc.setJSONValue(key + ".sizes", indexed);
	}

	@SuppressWarnings("unchecked")
	private static Map<Integer, Boolean> loadCollapseStates(String key) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return Map.of();
		}
		Object value = pc.getJSONValue(key + ".collapse");
		if (value instanceof Map) {
			Map<String, Object> raw = (Map<String, Object>) value;
			Map<Integer, Boolean> result = new HashMap<>();
			for (Map.Entry<String, Object> entry : raw.entrySet()) {
				try {
					int index = Integer.parseInt(entry.getKey());
					if (entry.getValue() instanceof Boolean) {
						result.put(Integer.valueOf(index), (Boolean) entry.getValue());
					}
				} catch (NumberFormatException e) {
					// Skip corrupt entries.
				}
			}
			return result;
		}
		return Map.of();
	}

	private static void saveCollapseState(String key, int childIndex, boolean collapsed) {
		PersonalConfiguration pc = PersonalConfiguration.getPersonalConfiguration();
		if (pc == null) {
			return;
		}
		Map<Integer, Boolean> states = loadCollapseStates(key);
		Map<String, Object> store = new HashMap<>();
		for (Map.Entry<Integer, Boolean> entry : states.entrySet()) {
			store.put(entry.getKey().toString(), entry.getValue());
		}
		if (collapsed) {
			store.put(String.valueOf(childIndex), Boolean.TRUE);
		} else {
			store.remove(String.valueOf(childIndex));
		}
		if (store.isEmpty()) {
			pc.setJSONValue(key + ".collapse", null);
		} else {
			pc.setJSONValue(key + ".collapse", store);
		}
	}

	private static ReactControl createContent(List<UIElement> elements, ViewContext context) {
		if (elements.size() == 1) {
			return (ReactControl) elements.get(0).createControl(context);
		}
		List<ReactControl> children = elements.stream()
			.map(e -> (ReactControl) e.createControl(context))
			.collect(Collectors.toList());
		return new ReactStackControl(children);
	}

	private record PaneEntry(float _size, String _unit, int _minSize, List<UIElement> _children) {
	}
}
