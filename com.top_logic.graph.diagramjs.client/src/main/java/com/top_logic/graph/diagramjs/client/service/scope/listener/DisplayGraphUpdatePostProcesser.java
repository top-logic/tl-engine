/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service.scope.listener;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.core.Canvas;
import com.top_logic.client.diagramjs.core.Diagram;
import com.top_logic.client.diagramjs.core.Selection;
import com.top_logic.client.diagramjs.model.Base;
import com.top_logic.client.diagramjs.model.Connection;
import com.top_logic.client.diagramjs.model.Label;
import com.top_logic.client.diagramjs.model.Root;
import com.top_logic.client.diagramjs.model.Shape;
import com.top_logic.client.diagramjs.model.util.Dimension;
import com.top_logic.client.diagramjs.model.util.Position;
import com.top_logic.client.diagramjs.util.DiagramJSObjectUtil;
import com.top_logic.client.diagramjs.util.JavaScriptObjectUtil;
import com.top_logic.graph.common.model.GraphPart;

/**
 * Finish the display graph part update.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DisplayGraphUpdatePostProcesser {

	private Diagram _diagram;

	/**
	 * Creates an {@link DisplayGraphUpdatePostProcesser}.
	 */
	public DisplayGraphUpdatePostProcesser(Diagram diagram) {
		_diagram = diagram;
	}

	/**
	 * Draw the given graph parts and apply selection.
	 */
	public void finish(Set<Base> drawingGraphParts, Collection<? extends GraphPart> selectedGraphParts) {
		Canvas canvas = _diagram.getCanvas();

		addGraphPartsToCanvas(canvas, drawingGraphParts);
		selectGraphParts(canvas, selectedGraphParts);
	}

	private void addGraphPartsToCanvas(Canvas canvas, Set<Base> drawingGraphParts) {
		Root root = canvas.getRootElement();

		for (Base displayObject : drawingGraphParts) {
			if (DiagramJSObjectUtil.isShape(displayObject)) {
				Shape shape = (Shape) displayObject;

				canvas.addShape(shape, root);

				List<Label> labels = Arrays.asList(shape.getLabels());

				labels.forEach(label -> canvas.addShape(label, shape));
			} else if (DiagramJSObjectUtil.isConnection(displayObject)) {
				Connection connection = (Connection) displayObject;

				canvas.addConnection(connection, root);

				List<Label> labels = Arrays.asList(connection.getLabels());

				labels.forEach(label -> canvas.addShape(label, connection));
			} else if (DiagramJSObjectUtil.isLabel(displayObject)) {
				canvas.addShape((Label) displayObject, root);
			}
		}
	}

	private void selectGraphParts(Canvas canvas, Collection<? extends GraphPart> selectedGraphParts) {
		Selection selection = _diagram.getSelection();

		if (!selectedGraphParts.isEmpty()) {
			List<Base> selectedDiagrammParts = selectedGraphParts
				.stream()
				.map(graphPart -> (Base) graphPart.getTag())
				.collect(Collectors.toList());

			boolean isVisible = canvas.inViewbox(selectedDiagrammParts);

			if (!isVisible) {
				Dimension size = canvas.getSize();

				Position center = DiagramJSObjectUtil.getCanvasCenter(selectedDiagrammParts, canvas);

				canvas.setViewbox(DiagramJSObjectUtil.createBounds(center, size));
			}

			selection.select(selectedDiagrammParts, false);
		} else {
			Collection<Base> selectedElements = selection.getSelection();

			for (Base selectedElement : selectedElements) {
				selection.deselect(selectedElement);
			}
		}
	}

	JavaScriptObject getConnectionContext(Connection connection) {
		JavaScriptObject context = JavaScriptObject.createObject();

		JavaScriptObjectUtil.put(context, "connection", connection);

		return context;
	}
}
