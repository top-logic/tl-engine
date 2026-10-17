/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.view.element;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.react.control.layout.ReactSplitPanelControl.ChildConstraint;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.layout.view.element.SplitSizePersonalization;

/**
 * Tests {@link SplitSizePersonalization} against an in-memory {@link SplitSizePersonalization.Store}.
 */
public class TestSplitSizePersonalization extends TestCase {

	private static final String KEY = "view.adaptive-detail";

	private final Map<String, Object> _values = new HashMap<>();

	private final SplitSizePersonalization.Store _store = new SplitSizePersonalization.Store() {
		@Override
		public Object load(String key) {
			return _values.get(key);
		}

		@Override
		public void save(String key, Object value) {
			if (value == null) {
				_values.remove(key);
			} else {
				_values.put(key, value);
			}
		}
	};

	private static List<ChildConstraint> configured(float selectorSize) {
		return List.of(
			new ChildConstraint(selectorSize, DisplayUnit.PERCENT, 240, Scrolling.AUTO),
			new ChildConstraint(68, DisplayUnit.PERCENT, 320, Scrolling.AUTO));
	}

	private SplitSizePersonalization create(List<ChildConstraint> configured) {
		return new SplitSizePersonalization(KEY, configured, _store);
	}

	private static Map<String, Float> dragged(float selector, float detail) {
		Map<String, Float> sizes = new LinkedHashMap<>();
		sizes.put("c1", Float.valueOf(selector));
		sizes.put("c2", Float.valueOf(detail));
		return sizes;
	}

	private static void assertConstraint(ChildConstraint expected, ChildConstraint actual) {
		assertEquals(expected.getSize(), actual.getSize());
		assertEquals(expected.getUnit(), actual.getUnit());
		assertEquals(expected.getMinSize(), actual.getMinSize());
		assertEquals(expected.getScrolling(), actual.getScrolling());
	}

	/**
	 * Without stored values the configured constraints apply.
	 */
	public void testConfiguredWithoutStoredValues() {
		List<ChildConstraint> configured = configured(32);
		SplitSizePersonalization personalization = create(configured);

		assertConstraint(configured.get(0), personalization.constraint(0));
		assertConstraint(configured.get(1), personalization.constraint(1));
		assertNotSame("A split panel modifies the constraints of its panes.", configured.get(0),
			personalization.constraint(0));
		assertFalse(personalization.isCollapsed(0));
		assertFalse(personalization.isCollapsed(1));
	}

	/**
	 * Dragged sizes stored for the same configuration apply in pixels, keeping the configured
	 * minimum size.
	 */
	public void testStoredSizesApply() {
		create(configured(32)).saveSizes(dragged(400, 900));

		SplitSizePersonalization personalization = create(configured(32));
		ChildConstraint selector = personalization.constraint(0);
		assertEquals(400f, selector.getSize());
		assertEquals(DisplayUnit.PIXEL, selector.getUnit());
		assertEquals(240, selector.getMinSize());
		ChildConstraint detail = personalization.constraint(1);
		assertEquals(900f, detail.getSize());
		assertEquals(DisplayUnit.PIXEL, detail.getUnit());
		assertEquals(320, detail.getMinSize());
	}

	/**
	 * Sizes stored for another configuration are discarded.
	 */
	public void testChangedConfigurationDiscardsStoredSizes() {
		create(configured(32)).saveSizes(dragged(400, 900));

		List<ChildConstraint> changed = configured(40);
		SplitSizePersonalization personalization = create(changed);
		assertConstraint(changed.get(0), personalization.constraint(0));
		assertConstraint(changed.get(1), personalization.constraint(1));
	}

	/**
	 * Values read back after a round trip through JSON (numbers of another type) still apply.
	 */
	public void testStoredNumbersOfOtherType() {
		_values.put(KEY + SplitSizePersonalization.SIZES_SUFFIX, Map.of("0", Double.valueOf(400), "1", Long.valueOf(900)));
		_values.put(KEY + SplitSizePersonalization.CONFIG_SIGNATURE_SUFFIX,
			SplitSizePersonalization.configSignature(configured(32)));

		SplitSizePersonalization personalization = create(configured(32));
		assertEquals(400f, personalization.constraint(0).getSize());
		assertEquals(900f, personalization.constraint(1).getSize());
	}

	/**
	 * A collapsed pane is restored collapsed, and expanding it removes the stored state.
	 */
	public void testCollapseStateRestored() {
		create(configured(32)).saveCollapsed(1, true);

		SplitSizePersonalization personalization = create(configured(32));
		assertFalse(personalization.isCollapsed(0));
		assertTrue(personalization.isCollapsed(1));

		personalization.saveCollapsed(1, false);
		assertFalse(create(configured(32)).isCollapsed(1));
		assertNull(_values.get(KEY + SplitSizePersonalization.COLLAPSE_SUFFIX));
	}

	/**
	 * The signature follows size, unit and minimum size of each pane.
	 */
	public void testConfigSignature() {
		String base = SplitSizePersonalization.configSignature(configured(32));
		assertEquals(base, SplitSizePersonalization.configSignature(configured(32)));
		assertFalse(base.equals(SplitSizePersonalization.configSignature(configured(33))));
		assertFalse(base.equals(SplitSizePersonalization.configSignature(List.of(
			new ChildConstraint(32, DisplayUnit.PIXEL, 240, Scrolling.AUTO),
			new ChildConstraint(68, DisplayUnit.PERCENT, 320, Scrolling.AUTO)))));
		assertFalse(base.equals(SplitSizePersonalization.configSignature(List.of(
			new ChildConstraint(32, DisplayUnit.PERCENT, 200, Scrolling.AUTO),
			new ChildConstraint(68, DisplayUnit.PERCENT, 320, Scrolling.AUTO)))));
	}

	/**
	 * Without a current user (no session), nothing is stored and the configured sizes apply.
	 */
	public void testWithoutPersonalConfiguration() {
		List<ChildConstraint> configured = configured(32);
		SplitSizePersonalization personalization = new SplitSizePersonalization(KEY, configured);
		personalization.saveSizes(dragged(400, 900));
		personalization.saveCollapsed(0, true);

		SplitSizePersonalization reread = new SplitSizePersonalization(KEY, configured);
		assertConstraint(configured.get(0), reread.constraint(0));
		assertFalse(reread.isCollapsed(0));
	}

	/**
	 * Forgetting the sizes gives the configured constraints back, also to a personalization created later.
	 */
	public void testForgetSizes() {
		List<ChildConstraint> configured = configured(32);
		SplitSizePersonalization personalization = create(configured);
		personalization.saveSizes(dragged(400, 600));

		personalization.forgetSizes();

		assertConstraint(configured.get(0), personalization.constraint(0));
		assertConstraint(configured.get(0), create(configured).constraint(0));
	}
}
