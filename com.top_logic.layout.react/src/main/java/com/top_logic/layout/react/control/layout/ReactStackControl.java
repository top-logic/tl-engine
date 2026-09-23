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

	/** @see #setJustify(StackJustify) */
	private static final String JUSTIFY = "justify";

	/** @see #setWrap(boolean) */
	private static final String WRAP = "wrap";

	/** @see #setGrowFirst(boolean) */
	private static final String GROW_FIRST = "growFirst";

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
	 * Distribution of the children along the main axis.
	 *
	 * <p>
	 * Where the children together are narrower than the stack, this decides what happens with the
	 * space left over: it stays behind them, before them, on both sides, or it is divided between
	 * them.
	 * </p>
	 */
	public enum StackJustify implements ExternallyNamed {

		/** Children placed one after the other from the start, the free space behind them. */
		START("start"),

		/** Children placed as a block in the middle, the free space split between both ends. */
		CENTER("center"),

		/** Children placed one after the other towards the end, the free space before them. */
		END("end"),

		/** First and last child at the ends, the free space divided between the children. */
		SPACE_BETWEEN("space-between"),

		/** Free space divided around the children, each one getting the same share on both sides. */
		SPACE_AROUND("space-around"),

		/** Free space divided into equal gaps before, between and after the children. */
		SPACE_EVENLY("space-evenly");

		private final String _externalName;

		StackJustify(String externalName) {
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
		putState(JUSTIFY, StackJustify.START.getExternalName());
		putState(WRAP, Boolean.valueOf(wrap));
	}

	/**
	 * Distributes the children along the main axis, the space left over behind them by default.
	 *
	 * <p>
	 * Use to spread a row across the width it is given: a title and the actions belonging to it
	 * pushed to the opposite ends, a row of buttons centered under a form.
	 * </p>
	 */
	public void setJustify(StackJustify justify) {
		putState(JUSTIFY, justify.getExternalName());
	}

	/**
	 * Lets the children flow into further lines once they no longer fit next to each other, rather
	 * than shrinking them into one line.
	 *
	 * <p>
	 * Use for a row of items that each keep a readable size - chips, cards, filter buttons - and
	 * that may as well stand below each other where the screen is narrow.
	 * </p>
	 */
	public void setWrap(boolean wrap) {
		putState(WRAP, Boolean.valueOf(wrap));
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

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 *
	 * @implNote A stack is elided from the projection wherever it is a plain child, but a stack
	 *           filling a named slot of its container is kept, and its state is then projected like
	 *           any other. How it arranges its children says nothing about what they display, so it
	 *           declares the arrangement rendering-only either way.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), DIRECTION, GAP, ALIGN, JUSTIFY, WRAP, GROW_FIRST);
	}
}
