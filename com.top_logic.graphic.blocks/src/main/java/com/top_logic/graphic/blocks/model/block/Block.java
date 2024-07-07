/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.model.block;

import static com.top_logic.graphic.blocks.math.Vec.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.json.JsonSerializable;
import com.top_logic.graphic.blocks.math.Rect;
import com.top_logic.graphic.blocks.math.Vec;
import com.top_logic.graphic.blocks.model.AbstractBlockModel;
import com.top_logic.graphic.blocks.model.BlockModel;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.BlockType;
import com.top_logic.graphic.blocks.model.Connected;
import com.top_logic.graphic.blocks.model.connector.ConnectorKind;
import com.top_logic.graphic.blocks.model.connector.ConnectorType;
import com.top_logic.graphic.blocks.model.content.BlockContent;
import com.top_logic.graphic.blocks.model.content.BlockContentType;
import com.top_logic.graphic.blocks.model.content.mouth.Mouth;
import com.top_logic.graphic.blocks.model.visit.BlockVisitor;
import com.top_logic.graphic.blocks.svg.BevelBorderWriter;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * Model of a single block in a block puzzle.
 * 
 * <p>
 * A {@link Block} consists of vertically stacked {@link BlockContent}.
 * </p>
 * 
 * @see #getParts()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Block extends AbstractBlockModel implements BlockGroup, Connected {

	private final BlockType _type;

	private final List<BlockContent> _parts = new ArrayList<>();

	private BlockCollection _outer;

	private Block _previous;

	private Block _next;

	private double _width;

	private double _height;

	private double _offsetX;

	private double _offsetY;

	private Connector _topConnector = new Connector() {

		@Override
		public BlockModel getOwner() {
			return Block.this;
		}

		@Override
		public String getId() {
			return Block.this.getId() + "-top";
		}

		@Override
		public Vec getStart() {
			return vec(BlockDimensions.CONNECTOR_OFFSET, BlockDimensions.CONNECTOR_HIGHLIGHT_OFFSET_Y);
		}

		@Override
		public Rect getSensitiveArea() {
			double x = getX() + BlockDimensions.CONNECTOR_OFFSET;
			double y = getY();
			return Rect.rect(
				x - BlockDimensions.CONNECTOR_SENSITIVE_PADDING_X, y - BlockDimensions.CONNECTOR_SENSITIVE_HEIGHT,
				x + BlockDimensions.CONNECTOR_WIDTH + BlockDimensions.CONNECTOR_SENSITIVE_PADDING_X, y);
		}

		@Override
		public Vec getCenter() {
			return getTopLeft().plus(BlockDimensions.CONNECTOR_OFFSET + BlockDimensions.CONNECTOR_WIDTH / 2, 0.0);
		}

		@Override
		public ConnectorType getType() {
			return Block.this.getType().getTopType();
		}

		@Override
		public ConnectorKind getKind() {
			return ConnectorKind.TOP_OUTER;
		}

		@Override
		public Connector connected() {
			Block previous = getPrevious();
			if (previous != null) {
				return previous.getBottomConnector();
			}

			BlockCollection outer = Block.this.getOwner();
			if (outer instanceof Mouth) {
				return ((Mouth) outer).getTopConnector();
			} else {
				return null;
			}
		}
	};

	private Connector _bottomConnector = new Connector() {

		@Override
		public BlockModel getOwner() {
			return Block.this;
		}

		@Override
		public String getId() {
			return Block.this.getId() + "-bottom";
		}

		@Override
		public Vec getStart() {
			return vec(BlockDimensions.CONNECTOR_OFFSET + BlockDimensions.CONNECTOR_WIDTH, getHeight() - BlockDimensions.CONNECTOR_HIGHLIGHT_OFFSET_Y);
		}

		@Override
		public Rect getSensitiveArea() {
			double y = getY() + getHeight();
			double x = getX() + BlockDimensions.CONNECTOR_OFFSET;
			return Rect.rect(
				x - BlockDimensions.CONNECTOR_SENSITIVE_PADDING_X, y,
				x + BlockDimensions.CONNECTOR_WIDTH + BlockDimensions.CONNECTOR_SENSITIVE_PADDING_X, y + BlockDimensions.CONNECTOR_SENSITIVE_HEIGHT);
		}

		@Override
		public Vec getCenter() {
			return getBottomLeft().plus(BlockDimensions.CONNECTOR_OFFSET + BlockDimensions.CONNECTOR_WIDTH / 2, 0.0);
		}

		@Override
		public ConnectorType getType() {
			return Block.this.getType().getBottomType();
		}

		@Override
		public ConnectorKind getKind() {
			return ConnectorKind.BOTTOM_OUTER;
		}

		@Override
		public Connector connected() {
			Block next = getNext();
			if (next != null) {
				return next.getTopConnector();
			}

			// Note: A bottom connector is never connected to a mouth part's bottom connector, at
			// least not for checking block insertions. To a block collection within a mouth all
			// blocks matching the last block's bottom connector can be appended. This is required
			// to ensure that the expected return type can be constructed by appending more blocks.
			return null;
		}
	};

	/**
	 * Creates a {@link Block}.
	 */
	public Block(BlockType type) {
		_type = type;
		for (BlockContentType partType : _type.getPartTypes()) {
			_parts.add(partType.newInstance(this));
		}
	}

	/**
	 * Description of a block's structure.
	 */
	public BlockType getType() {
		return _type;
	}

	/**
	 * The vertically stacked contents of the {@link Block}.
	 */
	public List<BlockContent> getParts() {
		return _parts;
	}

	/**
	 * The {@link Block} linked to the {@link #getBottomConnector()} of this {@link Block},
	 * <code>null</code> if this is the {@link BlockCollection#getLast()} {@link Block} of a
	 * {@link BlockCollection}.
	 */
	public Block getPrevious() {
		return _previous;
	}

	/**
	 * Whether a {@link #getPrevious()} {@link Block} exists.
	 */
	public boolean hasPrevious() {
		return getPrevious() != null;
	}

	/**
	 * The {@link Block} linked to the {@link #getTopConnector()} of this {@link Block},
	 * <code>null</code> if this is the {@link BlockCollection#getFirst()} {@link Block} of a
	 * {@link BlockCollection}.
	 */
	public Block getNext() {
		return _next;
	}

	/**
	 * Whether a {@link #getNext()} {@link Block} exists.
	 */
	public boolean hasNext() {
		return getNext() != null;
	}

	/**
	 * The last {@link Block} within it's {@link #getOwner() owning} {@link BlockCollection}.
	 * 
	 * <p>
	 * For a {@link Block} not currently owned by any {@link BlockCollection}, its the last
	 * {@link Block} in the chain of {@link Block}s iteratively following the {@link #getNext()}
	 * pointer.
	 * </p>
	 */
	public Block last() {
		if (_next != null) {
			return _next.last();
		}
		return this;
	}

	void linkOuter(BlockCollection outer) {
		assert outer != null;
	
		checkInitOuter(outer);
		doInitOuter(outer);
	}

	void unlinkOuter() {
		_outer = null;
		if (_next != null) {
			_next.unlinkOuter();
		}
	}

	void linkNext(Block other) {
		assert _next == null;
		assert other != null;
		assert other.getPrevious() == null;
	
		_next = other;
		other.linkPrevious(this);
	}

	private void linkPrevious(Block block) {
		_previous = block;
	}

	Block unlinkNext() {
		Block result = _next;
		_next = null;
	
		if (result != null) {
			result.unlinkPrevious();
		}
		return result;
	}

	private void unlinkPrevious() {
		_previous = null;
	}

	/**
	 * The {@link Connector} on the top border of this {@link Block}.
	 */
	public Connector getTopConnector() {
		return _topConnector;
	}

	/**
	 * The {@link Connector} on the bottom border of this {@link Block}.
	 */
	public Connector getBottomConnector() {
		return _bottomConnector;
	}

	/**
	 * The {@link BlockCollection} this {@link Block} is part of.
	 * 
	 * @see BlockCollection#contents()
	 */
	@Override
	public BlockCollection getOwner() {
		return _outer;
	}

	/**
	 * The width of the bounding box of this {@link Block}.
	 */
	public double getWidth() {
		return _width;
	}

	/**
	 * The height of the bounding box of this {@link Block} (excluding potential contents of
	 * mouthes).
	 */
	@Override
	public double getHeight() {
		return _height;
	}

	@Override
	public void updateDimensions(RenderContext context, double offsetX, double offsetY) {
		_offsetX = offsetX;
		_offsetY = offsetY;

		double height = paddingTop() + BlockDimensions.BLOCK_PADDING_BOTTOM;
		double width = BlockDimensions.BLOCK_MINIMUM_CONTENT_WIDTH;

		double y = paddingTop();
		for (BlockContent part : _parts) {
			part.updateDimensions(context, 0.0, y);

			double partHeight = part.getHeight();
			y += partHeight;

			height += partHeight;
			width = Math.max(width, part.getWidth());
		}
		_height = height;

		_width = BlockDimensions.BLOCK_PADDING_LEFT + BlockDimensions.BLOCK_PADDING_RIGHT + width;
	}

	@Override
	public void draw(SvgWriter out) {
		out.beginGroup();
		out.writeId(getId());
		out.writeCssClass(getType().getCssClass());
		out.translate(_offsetX, _offsetY);
		{
			out.beginPath();
			out.writeCssClass(BlockCss.SHAPE_CLASS);
			out.beginData();
			outline(out);
			out.endData();
			out.endPath();
	
			out.beginPath();
			out.writeCssClass(BlockCss.BORDER_CLASS);
			out.beginData();
			outline(new BevelBorderWriter(out));
			out.endData();
			out.endPath();
		}
	
		for (BlockContent part : _parts) {
			part.draw(out);
		}
		out.endGroup();
	}

	private void outline(SvgWriter out) {
		// Assume, origin of coordinate system is at the top-left corner of the block.
		out.moveToAbs(0, BlockDimensions.BLOCK_LEFT_ROUND_Y);
		boolean hasPrevious = hasPrevious();
		if (hasPrevious) {
			out.lineToVerticalRel(-BlockDimensions.BLOCK_LEFT_ROUND_Y);
			out.lineToHorizontalRel(BlockDimensions.BLOCK_LEFT_ROUND_X);
		} else {
			out.roundedCorner(false, BlockDimensions.BLOCK_LEFT_ROUND_X, -BlockDimensions.BLOCK_LEFT_ROUND_Y);
		}

		out.lineToHorizontalAbs(BlockDimensions.CONNECTOR_OFFSET);
		getTopConnector().outline(out);
		out.lineToHorizontalAbs(_width - BlockDimensions.BLOCK_RIGHT_ROUND_X);

		out.roundedCorner(false, BlockDimensions.BLOCK_RIGHT_ROUND_X, BlockDimensions.BLOCK_RIGHT_ROUND_Y);

		out.lineToVerticalAbs(paddingTop());
		for (BlockContent part : _parts) {
			part.outlineRight(out);
		}
		out.lineToVerticalAbs(_height - BlockDimensions.BLOCK_RIGHT_ROUND_Y);

		boolean hasNext = hasNext();
		out.roundedCorner(false, -BlockDimensions.BLOCK_RIGHT_ROUND_X, BlockDimensions.BLOCK_RIGHT_ROUND_Y);

		out.lineToHorizontalAbs(BlockDimensions.CONNECTOR_OFFSET + BlockDimensions.CONNECTOR_WIDTH);
		getBottomConnector().outline(out);
		out.lineToHorizontalAbs(BlockDimensions.BLOCK_LEFT_ROUND_X);

		if (hasNext) {
			out.lineToHorizontalRel(-BlockDimensions.BLOCK_LEFT_ROUND_X);
			out.lineToVerticalRel(-BlockDimensions.BLOCK_LEFT_ROUND_Y);
		} else {
			out.roundedCorner(false, -BlockDimensions.BLOCK_LEFT_ROUND_X, -BlockDimensions.BLOCK_LEFT_ROUND_Y);
		}
		out.lineToVerticalAbs(BlockDimensions.BLOCK_LEFT_ROUND_Y);
		out.closePath();
	}

	private double paddingTop() {
		return Math.max(BlockDimensions.BLOCK_MIN_PADDING_TOP, getType().getTopType().getHeight() + 1);
	}

	private void doInitOuter(BlockCollection outer) {
		_outer = outer;
		if (_next != null) {
			_next.doInitOuter(outer);
		}
	}

	private void checkInitOuter(BlockCollection outer) {
		assert _outer == null;
		if (_next != null) {
			_next.checkInitOuter(outer);
		}
	}

	/**
	 * The i-th {@link Mouth} in {@link #getParts()}.
	 */
	public Mouth mouth(int index) {
		for (BlockContent x : getParts()) {
			if (x instanceof Mouth) {
				if (index == 0) {
					return (Mouth) x;
				} else {
					index--;
				}
			}
		}
		return null;
	}

	/**
	 * The offset of this {@link Block}'s left border from the left border of its
	 * {@link #getOwner()}.
	 */
	public double getOffsetX() {
		return _offsetX;
	}

	/**
	 * The offset of this {@link Block}'s top border from the top border of its {@link #getOwner()}.
	 */
	public double getOffsetY() {
		return _offsetY;
	}

	@Override
	public double getX() {
		return getOwner().getX() + getOffsetX();
	}

	@Override
	public double getY() {
		return getOwner().getY() + getOffsetY();
	}

	/**
	 * The top-left corner of this {@link Block} in absolute coordinates.
	 */
	public Vec getTopLeft() {
		return vec(getX(), getY());
	}

	/**
	 * The top-right corner of this {@link Block} in absolute coordinates.
	 */
	public Vec getTopRight() {
		return vec(getX() + getWidth(), getY());
	}

	/**
	 * The bottom-left corner of this {@link Block} in absolute coordinates.
	 */
	public Vec getBottomLeft() {
		return vec(getX(), getY() + getHeight());
	}

	/**
	 * The bottom-right corner of this {@link Block} in absolute coordinates.
	 */
	public Vec getBottomRight() {
		return vec(getX() + getWidth(), getY() + getHeight());
	}

	@Override
	public BlockList top() {
		return getOwner().top();
	}

	@Override
	public void forEachConnector(Consumer<Connector> consumer) {
		consumer.accept(getTopConnector());
		consumer.accept(getBottomConnector());
		for (BlockContent part : getParts()) {
			part.forEachConnector(consumer);
		}
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		super.writePropertiesTo(json);
	
		json.name("parts");
		JsonSerializable.writeArray(json, getParts());
	}

	@Override
	public void writeTo(JsonWriter json) throws IOException {
		json.beginArray();
		{
			json.value(getType().getId());
	
			super.writeTo(json);
		}
		json.endArray();
	}

	/**
	 * Reads a {@link Block} from the given {@link JsonReader}.
	 */
	public static Block read(BlockSchema schema, JsonReader json) throws IOException {
		Block result;
		json.beginArray();
		{
			String typeId = json.nextString();
	
			result = schema.block(typeId);
			result.readFrom(schema, json);
		}
		json.endArray();
		return result;
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "parts":
				json.beginArray();
				for (BlockContent part : getParts()) {
					part.readFrom(context, json);
				}
				json.endArray();
				break;
	
			default:
				super.readPropertyFrom(context, json, name);
		}
	}

	@Override
	public <R, A> R visit(BlockVisitor<R, A> v, A arg) {
		return v.visit(this, arg);
	}

}
