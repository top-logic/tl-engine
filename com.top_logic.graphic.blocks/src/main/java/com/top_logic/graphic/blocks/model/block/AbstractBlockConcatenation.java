/*
 * Copyright (c) 2020 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.graphic.blocks.model.block;

import java.io.IOException;
import java.util.Iterator;

import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.graphic.blocks.json.JsonSerializable;
import com.top_logic.graphic.blocks.model.AbstractBlockModel;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.svg.RenderContext;
import com.top_logic.graphic.blocks.svg.SvgWriter;

/**
 * Concatenation of {@link Block}s.
 * 
 * @see #getFirst()
 * @see #getLast()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractBlockConcatenation extends AbstractBlockModel implements BlockCollection {

	private Block _first;

	private Block _last;

	private double _height;

	private Iterable<Block> _contents = new Iterable<Block>() {
		@Override
		public Iterator<Block> iterator() {
			return new Iterator<Block>() {
				private Block _current = getFirst();

				@Override
				public boolean hasNext() {
					return _current != null;
				}

				@Override
				public Block next() {
					Block result = _current;
					_current = _current.getNext();
					return result;
				}
			};
		}
	};

	@Override
	public Block getFirst() {
		return _first;
	}

	@Override
	public Block getLast() {
		return _last;
	}

	@Override
	public void insertBefore(Block before, Block newBlock) {
		if (newBlock == null) {
			// Nothing to insert.
			return;
		}

		assert newBlock.getPrevious() == null;

		Block after;
		Block last = newBlock.last();
		if (before == null) {
			// At the end.
			if (_last == null) {
				// Empty.
				_first = newBlock;
				_last = last;
				newBlock.linkOuter(this);
				return;
			}

			after = _last;
		} else {
			assert before.getOwner() == this;

			after = before.getPrevious();
		}

		newBlock.linkOuter(this);

		Block tail;
		if (after == null) {
			tail = _first;
			_first = newBlock;
		} else {
			tail = after.unlinkNext();
			after.linkNext(newBlock);
		}

		if (tail == null) {
			_last = last;
		} else {
			last.linkNext(tail);
		}
	}

	@Override
	public Block removeFrom(Block start) {
		assert start != null;
		assert start.getOwner() == this;

		Block previous = start.getPrevious();
		if (previous == null) {
			_first = _last = null;
		} else {
			_last = previous;
			_last.unlinkNext();
		}

		start.unlinkOuter();
		return start;
	}

	@Override
	public Iterable<Block> contents() {
		return _contents;
	}

	@Override
	public double getHeight() {
		return _height;
	}

	@Override
	public void updateDimensions(RenderContext context, double offsetX, double offsetY) {
		double contentHeight = 0.0;
		boolean first = true;
		for (Block content : contents()) {
			if (first) {
				first = false;
			} else {
				contentHeight += BlockDimensions.BLOCKLIST_SPACING_Y;
			}
			content.updateDimensions(context, offsetX, offsetY);

			double localHeight = content.getHeight();
			contentHeight += localHeight;
			offsetY += localHeight + BlockDimensions.BLOCKLIST_SPACING_Y;
		}
		_height = contentHeight;
	}

	@Override
	public void draw(SvgWriter out) {
		for (Block current : contents()) {
			current.draw(out);
		}
	}

	@Override
	public void writePropertiesTo(JsonWriter json) throws IOException {
		super.writePropertiesTo(json);

		json.name("contents");
		JsonSerializable.writeArray(json, contents());
	}

	@Override
	public void readPropertyFrom(BlockSchema context, JsonReader json, String name) throws IOException {
		switch (name) {
			case "contents":
				json.beginArray();
				while (json.hasNext()) {
					append(Block.read(context, json));
				}
				json.endArray();
				break;

			default:
				super.readPropertyFrom(context, json, name);
		}
	}

}
