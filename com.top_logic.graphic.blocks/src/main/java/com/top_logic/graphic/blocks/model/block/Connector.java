/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model.block;

import com.top_logic.graphic.blocks.math.Rect;
import com.top_logic.graphic.blocks.math.Vec;
import com.top_logic.graphic.blocks.model.BlockModel;
import com.top_logic.graphic.blocks.model.Connected;
import com.top_logic.graphic.blocks.model.Drawable;
import com.top_logic.graphic.blocks.model.Identified;
import com.top_logic.graphic.blocks.model.connector.ConnectorKind;
import com.top_logic.graphic.blocks.model.connector.ConnectorType;
import com.top_logic.graphic.blocks.model.content.mouth.Mouth;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * Description of a {@link Block}'s or {@link Mouth}'s connectors.
 * 
 * @see Connected#connectors()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Connector extends Identified, Drawable {

	/**
	 * Start position from where to draw {@link ConnectorType#outline(SvgWriter, ConnectorKind)}.
	 */
	Vec getStart();

	/**
	 * The center point of this {@link Connector} for finding the best match for an other connector
	 */
	Vec getCenter();

	/**
	 * The sensitive area of this {@link Connector}.
	 * 
	 * <p>
	 * Two connectors can be connected by dropping a currently dragged {@link Block}, if their
	 * sensitive areas overlap.
	 * </p>
	 * 
	 * @see #match(Connector)
	 */
	Rect getSensitiveArea();

	/**
	 * The {@link ConnectorType} that defines the outline of this {@link Connector}.
	 */
	ConnectorType getType();

	/**
	 * The {@link BlockModel} that defines/owns this {@link Connector}.
	 */
	BlockModel getOwner();

	/**
	 * The position of this {@link Connector} within its {@link #getOwner()}.
	 */
	ConnectorKind getKind();

	@Override
	default void draw(SvgWriter out) {
		out.beginPath();
		out.writeId(getId());
		out.writeCssClass(BlockCss.CONNECTOR_CLASS);
		out.beginData();
		out.moveToAbs(getStart());
		outline(out);
		out.endData();
	}

	/**
	 * Creates outline path segments for this connector.
	 */
	default void outline(SvgWriter out) {
		getType().outline(out, getKind());
	}

	/**
	 * Whether the given connector's {@link #getSensitiveArea()} overlaps with this
	 * {@link Connector}'s {@link #getSensitiveArea()}.
	 */
	default ConnectorMatch match(Connector sensitive) {
		if (!getSensitiveArea().intersects(sensitive.getSensitiveArea())) {
			return ConnectorMatch.NONE;
		}
		return ConnectorMatch.match(getCenter().minus(sensitive.getCenter()).length(), this, sensitive);
	}

	/**
	 * If this {@link Connector} is connected, the {@link Connector} at the top side of the
	 * connection, <code>null</code> otherwise.
	 */
	default Connector connectionTop() {
		switch (getKind()) {
			case TOP_OUTER:
			case BOTTOM_INNER:
				return connected();

			default:
				return this;
		}
	}

	/**
	 * If this {@link Connector} is connected, the {@link Connector} at the bottom side of the
	 * connection, <code>null</code> otherwise.
	 */
	default Connector connectionBottom() {
		switch (getKind()) {
			case BOTTOM_OUTER:
			case TOP_INNER:
				return connected();

			default:
				return this;
		}
	}

	/**
	 * The other {@link Connector} connected to this one, <code>null</code> if this
	 * {@link Connector} is not connected.
	 */
	Connector connected();

}
