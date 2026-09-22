/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.List;
import java.util.Set;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactLayoutControl} that renders a flexbox container via the {@code TLStack} React
 * component.
 */
public class ReactStackControl extends ReactLayoutControl {

	private static final String REACT_MODULE = "TLStack";

	private static final String DIRECTION = "direction";

	private static final String GAP = "gap";

	private static final String ALIGN = "align";

	private static final String WRAP = "wrap";

	private static final String GROW_FIRST = "growFirst";

	/** @see #setCssClass(String) */
	private static final String CSS_CLASS = "cssClass";

	/**
	 * Flex direction.
	 */
	public enum StackDirection implements ExternallyNamed {

		/** Vertical layout. */
		COLUMN("column"),

		/** Horizontal layout. */
		ROW("row");

		private final String _externalName;

		StackDirection(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * Gap between children.
	 */
	public enum StackGap implements ExternallyNamed {

		/** Standard gap. */
		DEFAULT("default"),

		/** Small gap. */
		COMPACT("compact"),

		/** Large gap. */
		LOOSE("loose");

		private final String _externalName;

		StackGap(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * Cross-axis alignment.
	 */
	public enum StackAlign implements ExternallyNamed {

		/** Stretch to fill. */
		STRETCH("stretch"),

		/** Align to start. */
		START("start"),

		/** Center alignment. */
		CENTER("center"),

		/** Align to end. */
		END("end");

		private final String _externalName;

		StackAlign(String externalName) {
			_externalName = externalName;
		}

		@Override
		public String getExternalName() {
			return _externalName;
		}
	}

	/**
	 * Creates a vertical stack with default gap.
	 *
	 * @param children
	 *        The child controls to arrange.
	 */
	public ReactStackControl(ReactContext context, List<? extends ReactControl> children) {
		this(context, StackDirection.COLUMN, StackGap.DEFAULT, StackAlign.STRETCH, false, children);
	}

	/**
	 * Creates a stack with full configuration.
	 *
	 * @param direction
	 *        The flex direction.
	 * @param gap
	 *        The gap between children.
	 * @param align
	 *        The cross-axis alignment.
	 * @param wrap
	 *        Whether to wrap children.
	 * @param children
	 *        The child controls to arrange.
	 */
	public ReactStackControl(ReactContext context, StackDirection direction, StackGap gap, StackAlign align,
			boolean wrap, List<? extends ReactControl> children) {
		super(context, REACT_MODULE, children);
		putState(DIRECTION, direction.getExternalName());
		putState(GAP, gap.getExternalName());
		putState(ALIGN, align.getExternalName());
		putState(WRAP, Boolean.valueOf(wrap));
	}

	/**
	 * Sets an additional CSS class, appended to the layout classes of the stack.
	 *
	 * @param cssClass
	 *        The CSS class, or {@code null} for none.
	 */
	public void setCssClass(String cssClass) {
		putState(CSS_CLASS, cssClass != null ? cssClass : "");
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return Set.of(CSS_CLASS, ITEM_CLASS);
	}

	/**
	 * Lets the first child grow to fill the main axis while trailing children keep their natural
	 * size.
	 *
	 * <p>
	 * Use for an input-with-adornment row (e.g. a full-width text input followed by a fixed-size
	 * icon button), where the leading control should consume the remaining space.
	 * </p>
	 */
	public void setGrowFirst(boolean growFirst) {
		putState(GROW_FIRST, Boolean.valueOf(growFirst));
	}

	/**
	 * Structural: this control is a flex layout container and is elided from the headless projection.
	 */
	@Override
	public boolean scriptingTransparent() {
		return true;
	}
}
