/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.config.ExternallyNamed;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that renders a flexbox container via the {@code TLStack} React component.
 */
public class ReactStackControl extends ReactControl {

	private static final String REACT_MODULE = "TLStack";

	private static final String DIRECTION = "direction";

	private static final String GAP = "gap";

	private static final String ALIGN = "align";

	private static final String WRAP = "wrap";

	private static final String CHILDREN = "children";

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

	private final List<ReactControl> _children;

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
		super(context, null, REACT_MODULE);
		_children = new ArrayList<>(children);
		putState(DIRECTION, direction.getExternalName());
		putState(GAP, gap.getExternalName());
		putState(ALIGN, align.getExternalName());
		putState(WRAP, Boolean.valueOf(wrap));
		putState(CHILDREN, _children);
	}

	@Override
	protected void cleanupChildren() {
		for (ReactControl child : _children) {
			child.cleanupTree();
		}
	}

}
