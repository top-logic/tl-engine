/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.server.ui;

import java.util.Map;
import java.util.function.Supplier;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.common.remote.update.ChangeIO;
import com.top_logic.common.remote.update.Changes;
import com.top_logic.graph.common.model.GraphModel;
import com.top_logic.graph.common.model.impl.SharedGraph;
import com.top_logic.graph.common.util.GraphControlCommon;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ControlCommand} for receiving updates for the {@link GraphModel} from the client.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class UpdateGraphCommand extends ControlCommand {

	/** The name of the parameter storing the update information. */
	public static final String PARAMETER_GRAPH_UPDATE = "graphUpdate";

	/** The instance of the {@link UpdateGraphCommand}. */
	public static final UpdateGraphCommand INSTANCE = new UpdateGraphCommand();

	private UpdateGraphCommand() {
		super(GraphControlCommon.UPDATE_SERVER_GRAPH_COMMAND);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.UPDATE_GRAPH_COMMAND;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		SharedGraph graphModel = getGraphModel(control);
		String update = getUpdate(arguments);
		logDebug(() -> "Receiving graph update: " + update);
		Changes changes = ChangeIO.readChanges(update);
		applyChanges(graphModel, changes);
		return HandlerResult.DEFAULT_RESULT;
	}

	private SharedGraph getGraphModel(Control control) {
		AbstractGraphControl graphControl = (AbstractGraphControl) control;
		return graphControl.getModel().getGraph();
	}

	private String getUpdate(Map<String, Object> arguments) {
		return (String) arguments.get(PARAMETER_GRAPH_UPDATE);
	}

	private void applyChanges(SharedGraph graphModel, Changes changes) {
		graphModel.data().scope().update(changes);
	}

	private void logDebug(Supplier<String> message) {
		Logger.debug(message, UpdateGraphCommand.class);
	}

	@Override
	protected boolean executeCommandIfViewDisabled() {
		return true;
	}

}
