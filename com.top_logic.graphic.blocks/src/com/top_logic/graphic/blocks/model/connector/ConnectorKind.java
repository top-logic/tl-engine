/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.connector;

import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.model.block.Connector;
import com.top_logic.graphic.blocks.model.content.mouth.Mouth;

/**
 * Description of a {@link Connector} position within its {@link Connector#getOwner()}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public enum ConnectorKind {

	/**
	 * A connector at the top outer border of a {@link Block}.
	 */
	TOP_OUTER,

	/**
	 * A connector at the top border of a {@link Mouth}.
	 */
	TOP_INNER,

	/**
	 * A connector at the bottom outer of a {@link Block}.
	 */
	BOTTOM_OUTER,

	/**
	 * A connector at the bottom of a {@link Mouth}.
	 */
	BOTTOM_INNER;

	/**
	 * Whether a {@link Connector} of this {@link ConnectorKind} can theoretically be connected to a
	 * {@link Connector} of the given {@link ConnectorKind}.
	 */
	public boolean canConnectTo(ConnectorKind other) {
		switch (this) {
			case TOP_OUTER:
				return other == BOTTOM_OUTER || other == TOP_INNER;
			case TOP_INNER:
				return other == TOP_OUTER;
			case BOTTOM_INNER:
				return other == BOTTOM_OUTER;
			case BOTTOM_OUTER:
				return other == TOP_OUTER || other == BOTTOM_INNER;
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * Whether the outline is drawn horizontally from right to left.
	 */
	public boolean isRightToLeft() {
		switch (this) {
			case TOP_INNER:
			case BOTTOM_OUTER:
				return true;
			case TOP_OUTER:
			case BOTTOM_INNER:
				return false;
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * Whether this is an inner ({@link Mouth}) connector.
	 */
	public boolean isInner() {
		switch (this) {
			case TOP_INNER:
			case BOTTOM_INNER:
				return true;
			case TOP_OUTER:
			case BOTTOM_OUTER:
				return false;
		}
		throw new UnsupportedOperationException();
	}

	/**
	 * Whether this connector's notch wraps "around" the other conector's notch when connecting.
	 */
	public boolean isFemale() {
		switch (this) {
			case TOP_OUTER:
			case BOTTOM_INNER:
				return true;
			case TOP_INNER:
			case BOTTOM_OUTER:
				return false;
		}
		throw new UnsupportedOperationException();
	}

}
