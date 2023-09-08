/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.server.ui;

import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.event.infoservice.InfoService;
import com.top_logic.graph.common.util.GraphControlCommon;
import com.top_logic.graph.server.model.GraphData;
import com.top_logic.graph.server.model.GraphDropEvent;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.dnd.DnD;
import com.top_logic.layout.dnd.DndData;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * {@link ControlCommand} accepting client-side drop events over the graph.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class GraphDropCommand extends ControlCommand {

	/**
	 * Singleton {@link GraphDropCommand} instance.
	 */
	public static final GraphDropCommand INSTANCE = new GraphDropCommand();

	private GraphDropCommand() {
		super(GraphControlCommon.GRAPH_DROP_COMMAND);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.GRAPH_DROP_COMMAND;
	}

	@Override
	protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
		if ((String) arguments.get(DnD.DATA_PARAM) != null) {
			DndData data = DnD.getDndData(context, arguments);
			if (data != null) {
				double x = ((Number) arguments.get("x")).doubleValue();
				double y = ((Number) arguments.get("y")).doubleValue();
				GraphData model = (GraphData) control.getModel();
				model.getDropTarget().handleDrop(new GraphDropEvent(model, data, x, y));
			} else {
				InfoService.showWarning(com.top_logic.layout.dnd.I18NConstants.DROP_NOT_POSSIBLE);
			}
		}

		return HandlerResult.DEFAULT_RESULT;
	}

}
