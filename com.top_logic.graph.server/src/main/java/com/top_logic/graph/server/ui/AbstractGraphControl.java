/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.server.ui;

import static com.top_logic.ajax.shared.api.NamingConstants.*;
import static com.top_logic.graph.common.util.GraphControlCommon.*;

import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;

import com.top_logic.ajax.server.util.JSControlUtil;
import com.top_logic.base.services.simpleajax.ClientAction;
import com.top_logic.base.services.simpleajax.JSFunctionCall;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.common.json.gstream.JsonWriter;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.common.remote.update.ChangeIO;
import com.top_logic.common.remote.update.Changes;
import com.top_logic.graph.common.model.impl.SharedGraph;
import com.top_logic.graph.common.util.GraphControlCommon;
import com.top_logic.graph.server.model.GraphData;
import com.top_logic.graph.server.model.GraphModelListener;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;

/**
 * Server-side representation of a {@link SharedGraph}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class AbstractGraphControl extends AbstractControlBase implements GraphModelListener {

	private static final String GRAPH_CLASS = "cGraph";

	private static final String GRAPH_DISPLAY_CLASS = "cGraphDisplay";

	/**
	 * General graph commands.
	 */
	protected static final Map<String, ControlCommand> GRAPH_COMMANDS = createCommandMap(new ControlCommand[] {
		UpdateGraphCommand.INSTANCE,
		GraphDropCommand.INSTANCE
	});

	private final GraphData _data;

	/**
	 * Creates a {@link AbstractGraphControl}.
	 *
	 * @param data
	 *        The graph to display.
	 */
	public AbstractGraphControl(GraphData data) {
		this(data, GRAPH_COMMANDS);
	}

	/**
	 * Creates a {@link AbstractGraphControl}.
	 *
	 * @param data
	 *        The graph to display.
	 */
	public AbstractGraphControl(GraphData data, Map<String, ControlCommand> commands) {
		super(commands);

		_data = data;
	}

	@Override
	public GraphData getModel() {
		return _data;
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();
		
		_data.addGraphListener(this);
	}

	@Override
	protected void internalDetach() {
		super.internalDetach();

		_data.removeGraphListener(this);
	}

	@Override
	public void handleGraphChange(GraphData sender, SharedGraph oldValue, SharedGraph newValue) {
		requestRepaint();
	}

	@Override
	protected String getTypeCssClass() {
		return GRAPH_CLASS;
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected boolean hasUpdates() {
		return hasGraph() && getGraphScope().hasUpdates();
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		if (hasGraph()) {
			actions.add(buildUpdateCall());
		}
	}

	private String retrieveChangesAsJSON() {
		Changes changes = getGraphScope().popChanges();
		return ChangeIO.writeChanges(changes);
	}

	private ClientAction buildUpdateCall() {
		String objectPath = SERVICE_NAMESPACE + "." + SERVICE_NAME;
		String methodName = INVOKE;
		String changesAsJSON = retrieveChangesAsJSON();
		String encodedChangeJSON = TagUtil.encodeXML(changesAsJSON);
		logDebug(() -> "Sending graph update: " + changesAsJSON);
		return new JSFunctionCall(getID(), objectPath, methodName,
			GraphControlCommon.UPDATE_CLIENT_GRAPH_COMMAND, encodedChangeJSON);
	}

	/**
	 * Creates the DOM element to display the graph.
	 */
	protected void writeGraphDOMElement(TagWriter out) {
		out.beginBeginTag(DIV);
		out.writeAttribute(ID_ATTR, getGraphId(getID()));
		out.writeAttribute(CLASS_ATTR, GRAPH_DISPLAY_CLASS);
		out.endBeginTag();
		out.endTag(DIV);
	}

	/**
	 * Writes a script to create the client-side control for the given type.
	 */
	protected void writeGraphInitScript(TagWriter out, String type) throws IOException {
		JSControlUtil.writeCreateJSControlScript(out, type, getID(), retrieveStateAsJSON());
	}

	/**
	 * Graph state as json expression.
	 */
	protected String retrieveStateAsJSON() throws IOException {
		if (!hasGraph()) {
			return StringServices.EMPTY_STRING;
		}
		StringBuilder buffer = new StringBuilder();
		try (JsonWriter json = new JsonWriter(buffer)) {
			getGraphScope().writeTo(json);
		}
		return buffer.toString();
	}

	private boolean hasGraph() {
		return getModel().getGraph() != null;
	}

	private ObjectScope getGraphScope() {
		return getModel().getGraph().data().scope();
	}

	private void logDebug(Supplier<String> message) {
		Logger.debug(message, AbstractGraphControl.class);
	}
}
