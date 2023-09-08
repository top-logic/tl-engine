/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.client.service.event;

import java.util.Arrays;
import java.util.Collections;

import com.google.gwt.core.client.JavaScriptObject;

import com.top_logic.client.diagramjs.event.Event;
import com.top_logic.client.diagramjs.event.EventHandler;
import com.top_logic.client.diagramjs.model.Base;
import com.top_logic.client.diagramjs.util.DiagramJSObjectUtil;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.diagramjs.model.DiagramJSGraphModel;

/**
 * {@link EventHandler} for clicking diagramJS components. Updates the selected {@link GraphPart}
 * property of the {@link DiagramJSGraphModel}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public final class ClickEventHandler implements EventHandler {

	private DiagramJSGraphModel _graphModel;

	/**
	 * Creates a {@link ClickEventHandler} for the given {@link GraphModel}.
	 */
	public ClickEventHandler(DiagramJSGraphModel graphModel) {
		_graphModel = graphModel;
	}

	@Override
	public void call(Event event) {
		JavaScriptObject elementFromEvent = event.getElement();

		Base element = elementFromEvent.cast();
		GraphPart sharedGraphPart = (GraphPart) element.getSharedGraphPart();

		if (sharedGraphPart != null) {
			_graphModel.setSelectedGraphParts(Arrays.asList(sharedGraphPart));
		} else {
			if (DiagramJSObjectUtil.isLabel(element)) {
				sharedGraphPart = (GraphPart) element.getParent().getSharedGraphPart();

				_graphModel.setSelectedGraphParts(Arrays.asList(sharedGraphPart));
			} else {
				_graphModel.setSelectedGraphParts(Collections.emptyList());
			}
		}
	}
}
