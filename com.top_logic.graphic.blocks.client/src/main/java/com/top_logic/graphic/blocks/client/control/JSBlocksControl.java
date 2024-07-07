/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.client.control;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.vectomatic.dom.svg.OMSVGDocument;
import org.vectomatic.dom.svg.OMSVGElement;
import org.vectomatic.dom.svg.OMSVGGElement;
import org.vectomatic.dom.svg.OMSVGSVGElement;
import org.vectomatic.dom.svg.OMSVGTransform;
import org.vectomatic.dom.svg.OMSVGTransformList;
import org.vectomatic.dom.svg.itf.ISVGTransformable;
import org.vectomatic.dom.svg.utils.OMSVGParser;

import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.MouseDownEvent;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.dom.client.MouseEvent;
import com.google.gwt.event.dom.client.MouseMoveEvent;
import com.google.gwt.event.dom.client.MouseMoveHandler;
import com.google.gwt.event.dom.client.MouseUpEvent;
import com.google.gwt.event.dom.client.MouseUpHandler;
import com.google.gwt.event.shared.HandlerRegistration;

import com.top_logic.ajax.client.control.AbstractJSControl;
import com.top_logic.basic.shared.io.StringR;
import com.top_logic.common.json.gstream.JsonReader;
import com.top_logic.graphic.blocks.client.dom.DOMUtil;
import com.top_logic.graphic.blocks.control.JSBlocksControlCommon;
import com.top_logic.graphic.blocks.model.BlockModel;
import com.top_logic.graphic.blocks.model.BlockSchema;
import com.top_logic.graphic.blocks.model.Identified;
import com.top_logic.graphic.blocks.model.block.Block;
import com.top_logic.graphic.blocks.model.block.BlockCollection;
import com.top_logic.graphic.blocks.model.block.BlockDimensions;
import com.top_logic.graphic.blocks.model.block.BlockList;
import com.top_logic.graphic.blocks.model.block.Connector;
import com.top_logic.graphic.blocks.model.block.ConnectorMatch;
import com.top_logic.graphic.blocks.model.connector.ConnectorKind;
import com.top_logic.graphic.blocks.svg.RenderContext;

/**
 * Client-side logic of a {@link JSBlocksControlCommon}.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class JSBlocksControl extends AbstractJSControl
		implements JSBlocksControlCommon, ClickHandler, MouseDownHandler, MouseMoveHandler, MouseUpHandler {

	private static final String SELECTED_CLASS = "tlbSelected";

	private BlockSchema _schema;

	private Block _selected;

	private OMSVGDocument _svgDoc;

	private OMSVGSVGElement _svg;

	private BlockIndex _blockById = new BlockIndex();

	private HandlerRegistration _moveHandler;

	private HandlerRegistration _upHandler;

	private int _dragStartX;

	private int _dragStartY;

	private BlockList _dragElement;

	private double _dragOffsetX;

	private double _dragOffsetY;

	private RenderContext _renderContext = new JSRenderContext();

	private int _nextId = 1;

	private List<Connector> _activeConnectors;

	private Connector _activeConnector;

	/**
	 * Creates a {@link JSBlocksControl}.
	 *
	 * @param id
	 *        See {@link #getId()}.
	 */
	public JSBlocksControl(String id) {
		super(id);
	}

	@Override
	public void init(Object[] args) {
		super.init(args);

		try {
			_schema = BlockSchema.read(new JsonReader(new StringR(controlElement().getAttribute(TL_BLOCK_SCHEMA))));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		BlockList blocks;
		try {
			blocks = BlockList.read(_schema, new JsonReader(new StringR(controlElement().getAttribute(TL_BLOCK_DATA))));
		} catch (IOException ex) {
			throw new RuntimeException(ex);
		}

		_svgDoc = OMSVGParser.currentDocument();
		_svg = _svgDoc.getElementById(controlElement().getId());

		_blockById.addAll(blocks);

		blocks.updateDimensions(_renderContext, 0.0, 0.0);
		blocks.draw(new SVGBuilder(_svgDoc, _svg));

		_svg.addMouseDownHandler(this);
		_svg.addClickHandler(this);
	}

	@Override
	public void onMouseDown(MouseDownEvent event) {
		Element target = event.getNativeEvent().getEventTarget().cast();
		if (select(target)) {
			_dragStartX = getEventX(event);
			_dragStartY = getEventY(event);
			_moveHandler = _svg.addMouseMoveHandler(this);
			_upHandler = _svg.addMouseUpHandler(this);
		}
	}

	private int getEventY(MouseEvent<?> event) {
		return event.getRelativeY(_svg.getElement());
	}

	private int getEventX(MouseEvent<?> event) {
		return event.getRelativeX(_svg.getElement());
	}

	@Override
	public void onMouseMove(MouseMoveEvent event) {
		int x = getEventX(event);
		int y = getEventY(event);

		if (_dragElement == null) {
			if (Math.abs(x - _dragStartX) + Math.abs(y - _dragStartY) > 10) {
				startDrag();
			}
		}

		if (_dragElement != null) {
			updateDrag(x, y);
		}
	}

	private void startDrag() {
		// Vector from the mouse position where the drag started to the origin of the
		// coordinate system of the dragged block.
		_dragOffsetX = _selected.getX() - _dragStartX;
		_dragOffsetY = _selected.getY() - _dragStartY;
	
		SVGBuilder out = new SVGBuilder(_svgDoc, _svg);
	
		BlockCollection outer = _selected.getOwner();
		if (outer instanceof BlockList && _selected.getPrevious() == null) {
			// The whole list is being dragged.
			_dragElement = (BlockList) outer;
	
			// Bring the dragged element to front.
			OMSVGElement display = removeDisplay(_dragElement);
			_svg.appendChild(display);
		} else {
			Block dragged = outer.removeFrom(_selected);
	
			BlockList newList = new BlockList();
			newList.setId(newId());
			newList.append(dragged);
			_dragElement = newList;
	
			BlockList top = outer.top();
			removeDisplay(top);
	
			top.updateDimensions(_renderContext, 0.0, 0.0);
			_dragElement.updateDimensions(_renderContext, 0.0, 0.0);
	
			top.draw(out);
			_dragElement.draw(out);
		}

		BlockIndex dragged = new BlockIndex();
		dragged.addAll(_dragElement);

		_activeConnectors = new ArrayList<>();
		for (Block block : _blockById.getBlocks()) {
			if (dragged.getModel(block.getId()) != null) {
				continue;
			}

			block.forEachConnector(_activeConnectors::add);
		}
	}

	private void updateDrag(int x, int y) {
		_dragElement.setX(x + _dragOffsetX);
		_dragElement.setY(y + _dragOffsetY);

		updatePosition(_dragElement);

		ConnectorMatch match =
			findMatch(_dragElement.getFirst().getTopConnector(), _dragElement.getLast().getBottomConnector());
		if (match.hasMatch()) {
			Connector active = match.getActive();
			if (active != _activeConnector) {
				removeActiveConnector();

				_activeConnector = active;
				String connectableId = _activeConnector.getOwner().getId();
				OMSVGGElement outerElement = _svgDoc.getElementById(connectableId);
				_activeConnector.draw(new SVGBuilder(_svgDoc, _svg, outerElement));
			}
		} else {
			removeActiveConnector();
		}
	}

	private ConnectorMatch findMatch(Connector... sensitives) {
		ConnectorMatch match = ConnectorMatch.NONE;
		for (Connector sensitive : sensitives) {
			for (Connector active : _activeConnectors) {
				if (!sensitive.getKind().canConnectTo(active.getKind())) {
					// Puzzle geometry does not match.
					continue;
				}

				// With a dragged bottom connector only a connector of the first block of a
				// top-level list can be connected.
				if (sensitive.getKind() == ConnectorKind.BOTTOM_OUTER) {
					if (active.getKind() != ConnectorKind.TOP_OUTER) {
						continue;
					}

					Block activeBlock = (Block) active.getOwner();
					if (activeBlock.getPrevious() != null) {
						continue;
					}

					if (!(activeBlock.getOwner() instanceof BlockList)) {
						continue;
					}
				}

				BlockCollection sensitiveList = ((Block) sensitive.getOwner()).getOwner();

				Connector connectionTop = active.connectionTop();
				if (connectionTop != null) {
					if (!connectionTop.getType().canConnectTo(sensitiveList.getFirst().getTopConnector().getType())) {
						continue;
					}
				}

				Connector connectionBottom = active.connectionBottom();
				if (connectionBottom != null) {
					if (!sensitiveList.getLast().getBottomConnector().getType()
						.canConnectTo(connectionBottom.getType())) {
						continue;
					}
				}

				match = match.best(active.match(sensitive));
			}
		}
		return match;
	}

	private OMSVGElement removeDisplay(Identified model) {
		OMSVGElement result = _svgDoc.getElementById(model.getId());
		result.getParentNode().removeChild(result);
		return result;
	}

	private String newId() {
		return getId() + "-" + (_nextId++);
	}

	private void updatePosition(BlockList model) {
		ISVGTransformable node = _svgDoc.getElementById(model.getId());
		OMSVGTransformList txList = node.getTransform().getBaseVal();
		OMSVGTransform tx;
		if (txList.getNumberOfItems() != 1) {
			tx = _svg.createSVGTransform();

			txList.clear();
			txList.appendItem(tx);
		} else {
			tx = txList.getItem(0);
		}
		tx.setTranslate((float) model.getX(), (float) model.getY());
	}

	@Override
	public void onMouseUp(MouseUpEvent event) {
		if (_dragElement != null && _activeConnector != null) {
			Block inserted = _dragElement.getFirst();
			_dragElement.removeFrom(inserted);

			BlockList top;
			if (_activeConnector.getKind() == ConnectorKind.BOTTOM_OUTER) {
				// Append the dragged blocks to the block owning active connector.
				Block appendTo = (Block) _activeConnector.getOwner();
				appendTo.getOwner().insertBefore(appendTo.getNext(), inserted);
				top = appendTo.top();
			} else {
				// Prepend the dragged blocks to the container owning the active connector. This
				// container may either be a block mouth or a top-level block list.
				boolean placeBefore = _activeConnector.getKind() == ConnectorKind.TOP_OUTER;
				BlockCollection prependTo = placeBefore
					// Start block of a top-level block list.
					? ((Block) _activeConnector.getOwner()).getOwner()
					// Top connector of a mouth.
					: (BlockCollection) _activeConnector.getOwner();
				prependTo.insertBefore(prependTo.getFirst(), inserted);
				top = prependTo.top();

				if (placeBefore) {
					// Move the resulting list so that the contents in the list does not visually
					// move, when prepending the dragged blocks.
					top.setY(top.getY() - _dragElement.getHeight() - BlockDimensions.BLOCKLIST_SPACING_Y);
				}
			}


			removeDisplay(_dragElement);
			removeActiveConnector();
			removeDisplay(top);
			top.updateDimensions(_renderContext, 0.0, 0.0);
			SVGBuilder out = new SVGBuilder(_svgDoc, _svg);
			top.draw(out);
		} else {
			removeActiveConnector();
		}

		resetDrag();
	}

	private void removeActiveConnector() {
		if (_activeConnector != null) {
			removeDisplay(_activeConnector);
			_activeConnector = null;
		}
	}

	private void resetDrag() {
		if (_moveHandler != null) {
			_moveHandler.removeHandler();
			_moveHandler = null;
		}
		if (_upHandler != null) {
			_upHandler.removeHandler();
			_upHandler = null;
		}
		_dragElement = null;
		_dragStartX = 0;
		_dragStartY = 0;
	}

	@Override
	public void onClick(ClickEvent event) {
		// Ignore.
	}

	private boolean select(Element element) {
		while (element != null) {
			String id = element.getId();
			BlockModel model = _blockById.getModel(id);
			if (model instanceof Block) {
				setSelected((Block) model);
				return true;
			}
	
			if (element == controlElement()) {
				// No block found.
				setSelected(null);
				break;
			}

			element = element.getParentElement();
		}
		return false;
	}

	/**
	 * The currently selected Block.
	 */
	public Block getSelected() {
		return _selected;
	}

	/**
	 * @see #getSelected()
	 */
	public void setSelected(Block newSelection) {
		if (newSelection == _selected) {
			return;
		}
	
		Document ownerDocument = controlElement().getOwnerDocument();
		if (_selected != null) {
			Element oldElement = ownerDocument.getElementById(_selected.getId());
			if (oldElement != null) {
				DOMUtil.removeClassName(oldElement, SELECTED_CLASS);
			}
			_selected = null;
		}
	
		if (newSelection != null) {
			Element newElement = ownerDocument.getElementById(newSelection.getId());
			if (newElement != null) {
				_selected = newSelection;
				DOMUtil.addClassName(newElement, SELECTED_CLASS);
			}
		}
	}

}
