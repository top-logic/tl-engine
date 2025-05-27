/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model.content.mouth;

import static com.top_logic.graphic.blocks.math.Rect.*;
import static com.top_logic.graphic.blocks.math.Vec.*;

import java.util.function.Consumer;

import com.top_logic.graphic.blocks.math.Rect;
import com.top_logic.graphic.blocks.math.Vec;
import com.top_logic.graphic.blocks.model.BlockModel;
import com.top_logic.graphic.blocks.model.block.AbstractBlockConcatenation;
import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.model.block.BlockDimensions;
import com.top_logic.graphic.blocks.model.block.BlockList;
import com.top_logic.graphic.blocks.model.block.Connector;
import com.top_logic.graphic.blocks.model.connector.ConnectorKind;
import com.top_logic.graphic.blocks.model.connector.ConnectorType;
import com.top_logic.graphic.blocks.model.content.BlockContent;
import com.top_logic.graphic.blocks.model.visit.BlockVisitor;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * A mouth {@link BlockContent}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Mouth extends AbstractBlockConcatenation implements BlockContent {

	private MouthType _type;

	private double _contentHeight;

	private final Block _owner;

	private final Connector _topConnector = new Connector() {

		@Override
		public BlockModel getOwner() {
			return Mouth.this;
		}

		@Override
		public String getId() {
			return Mouth.this.getId() + "-top";
		}

		@Override
		public ConnectorKind getKind() {
			return ConnectorKind.TOP_INNER;
		}

		@Override
		public ConnectorType getType() {
			return Mouth.this.getType().getTopType();
		}

		@Override
		public Vec getStart() {
			return vec(BlockDimensions.CONNECTOR_OFFSET + BlockDimensions.CONNECTOR_WIDTH, BlockDimensions.CONNECTOR_HIGHLIGHT_OFFSET_Y);
		}

		@Override
		public Rect getSensitiveArea() {
			return rect(left() - BlockDimensions.CONNECTOR_SENSITIVE_PADDING_X, getY(),
				right() + BlockDimensions.CONNECTOR_SENSITIVE_PADDING_X, getY() + BlockDimensions.CONNECTOR_SENSITIVE_HEIGHT);
		}

		private double left() {
			return getX() + BlockDimensions.CONNECTOR_OFFSET;
		}

		private double right() {
			return getX() + BlockDimensions.CONNECTOR_OFFSET + BlockDimensions.CONNECTOR_WIDTH;
		}

		@Override
		public Vec getCenter() {
			return vec(getX() + BlockDimensions.CONNECTOR_OFFSET + BlockDimensions.CONNECTOR_WIDTH / 2.0, getY());
		}

		@Override
		public Connector connected() {
			Block first = getFirst();
			return first == null ? null : first.getTopConnector();
		}
	};

	private final Connector _bottomConnector = new Connector() {

		@Override
		public BlockModel getOwner() {
			return Mouth.this;
		}

		@Override
		public String getId() {
			return Mouth.this.getId() + "-bottom";
		}

		@Override
		public ConnectorKind getKind() {
			return ConnectorKind.BOTTOM_INNER;
		}

		@Override
		public ConnectorType getType() {
			return Mouth.this.getType().getBottomType();
		}

		@Override
		public Vec getStart() {
			return vec(BlockDimensions.CONNECTOR_OFFSET, getHeight() - BlockDimensions.CONNECTOR_HIGHLIGHT_OFFSET_Y);
		}

		@Override
		public Rect getSensitiveArea() {
			return rect(left() - BlockDimensions.CONNECTOR_SENSITIVE_PADDING_X, getY() - BlockDimensions.CONNECTOR_SENSITIVE_HEIGHT,
				right() + BlockDimensions.CONNECTOR_SENSITIVE_PADDING_X, getY());
		}

		private double left() {
			return getX() + BlockDimensions.CONNECTOR_OFFSET;
		}

		private double right() {
			return getX() + BlockDimensions.CONNECTOR_OFFSET + BlockDimensions.CONNECTOR_WIDTH;
		}

		@Override
		public Vec getCenter() {
			return vec(getX() + BlockDimensions.CONNECTOR_OFFSET + BlockDimensions.CONNECTOR_WIDTH / 2.0, getY());
		}

		@Override
		public Connector connected() {
			// Note: A bottom connector is never connected to a mouth part's bottom connector, at
			// least not for checking block insertions. To a block collection within a mouth all
			// blocks matching the last block's bottom connector can be appended. This is required
			// to ensure that the expected return type can be constructed by appending more blocks.
			return null;
		}
	};

	private double _offsetX;

	private double _offsetY;

	/**
	 * Creates a {@link Mouth}.
	 */
	public Mouth(Block owner, MouthType type) {
		_owner = owner;
		_type = type;
	}

	@Override
	public Block getOwner() {
		return _owner;
	}

	/**
	 * The {@link MouthType} of this {@link Mouth}.
	 */
	public MouthType getType() {
		return _type;
	}

	@Override
	public BlockList top() {
		return _owner.top();
	}

	@Override
	public void updateDimensions(RenderContext context, double offsetX, double offsetY) {
		_offsetX = offsetX + BlockDimensions.MOUTH_PADDING_LEFT;
		_offsetY = offsetY + BlockDimensions.MOUTH_PADDING_TOP;

		double contentHeight;
		if (isEmpty()) {
			contentHeight = BlockDimensions.MOUTH_EMPTY_CONTENT_HEIGHT;
		} else {
			double x = BlockDimensions.MOUTH_SPACING_X;
			double y = BlockDimensions.BLOCKLIST_SPACING_Y;

			contentHeight = BlockDimensions.BLOCKLIST_SPACING_Y;
			for (Block content : contents()) {
				content.updateDimensions(context, x, y);
				double dy = content.getHeight() + BlockDimensions.BLOCKLIST_SPACING_Y;
				contentHeight += dy;
				y += dy;
			}

			ConnectorType bottomType = getLast().getType().getBottomType();
			if (!_type.getBottomType().canConnectTo(bottomType)) {
				contentHeight += bottomType.getHeight();
			}
		}
		_contentHeight = contentHeight;
	}

	@Override
	public double getX() {
		return getOwner().getX() + _offsetX;
	}

	@Override
	public double getY() {
		return getOwner().getY() + _offsetY;
	}

	@Override
	public void draw(SvgWriter out) {
		out.beginGroup();
		out.writeId(getId());
		out.translate(_offsetX, _offsetY);
		super.draw(out);
		out.endGroup();
	}

	@Override
	public double getWidth() {
		return 0;
	}

	@Override
	public double getHeight() {
		return BlockDimensions.MOUTH_PADDING_TOP + BlockDimensions.MOUTH_PADDING_BOTTOM + _contentHeight;
	}

	@Override
	public void outlineRight(SvgWriter out) {
		double connectorOffset = BlockDimensions.CONNECTOR_OFFSET + BlockDimensions.MOUTH_SPACING_X;
		double leftInnerWidth =
			getOwner().getWidth() - BlockDimensions.MOUTH_PADDING_LEFT - connectorOffset - BlockDimensions.CONNECTOR_WIDTH
				- BlockDimensions.MOUTH_OUTER_ROUND_X;

		out.lineToVerticalRel(BlockDimensions.MOUTH_PADDING_TOP - BlockDimensions.MOUTH_OUTER_ROUND_Y);
		out.roundedCorner(false, -BlockDimensions.MOUTH_OUTER_ROUND_X, BlockDimensions.MOUTH_OUTER_ROUND_Y);

		out.lineToHorizontalRel(-(leftInnerWidth));
		getTopConnector().outline(out);
		out.lineToHorizontalRel(-(connectorOffset - BlockDimensions.MOUTH_INNER_ROUND_X));

		out.roundedCorner(true, -BlockDimensions.MOUTH_INNER_ROUND_X, BlockDimensions.MOUTH_INNER_ROUND_Y);
		out.lineToVerticalRel(_contentHeight - 2 * BlockDimensions.MOUTH_INNER_ROUND_Y);
		out.roundedCorner(true, BlockDimensions.MOUTH_INNER_ROUND_X, BlockDimensions.MOUTH_INNER_ROUND_Y);

		out.lineToHorizontalRel(connectorOffset - BlockDimensions.MOUTH_INNER_ROUND_X);
		getBottomConnector().outline(out);
		out.lineToHorizontalRel(leftInnerWidth);

		out.roundedCorner(false, BlockDimensions.MOUTH_OUTER_ROUND_X, BlockDimensions.MOUTH_OUTER_ROUND_Y);
		out.lineToVerticalRel(BlockDimensions.MOUTH_PADDING_BOTTOM - BlockDimensions.MOUTH_OUTER_ROUND_Y);
	}

	/**
	 * Appends all given {@link Block}s to this {@link Mouth}.
	 * 
	 * @return The {@link #getOwner()} for call-chaining in Block composition expressions.
	 */
	public Block init(Block... blocks) {
		for (Block block : blocks) {
			append(block);
		}
		return getOwner();
	}

	@Override
	public void forEachConnector(Consumer<Connector> consumer) {
		consumer.accept(getTopConnector());
		consumer.accept(getBottomConnector());
	}

	@Override
	public <R, A> R visit(BlockVisitor<R, A> v, A arg) {
		return v.visit(this, arg);
	}

	/**
	 * The top {@link Connector} of this {@link Mouth}.
	 */
	public Connector getTopConnector() {
		return _topConnector;
	}

	/**
	 * The bottom {@link Connector} of this {@link Mouth}.
	 */
	public Connector getBottomConnector() {
		return _bottomConnector;
	}

}