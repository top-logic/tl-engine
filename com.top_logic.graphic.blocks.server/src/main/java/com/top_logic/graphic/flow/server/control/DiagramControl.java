/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.flow.server.control;

import static com.top_logic.graphic.blocks.svg.SvgConstants.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.ajax.server.util.JSControlUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.graphic.flow.callback.ClickHandler;
import com.top_logic.graphic.flow.control.JSDiagramControlCommon;
import com.top_logic.graphic.flow.data.ClickTarget;
import com.top_logic.graphic.flow.data.Diagram;
import com.top_logic.graphic.flow.data.MouseButton;
import com.top_logic.graphic.flow.data.SelectableBox;
import com.top_logic.graphic.flow.data.Widget;
import com.top_logic.layout.ContentHandler;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.URLParser;
import com.top_logic.layout.UpdateQueue;
import com.top_logic.layout.basic.AbstractControlBase;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.contextmenu.ContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.NoContextMenuProvider;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOpener;
import com.top_logic.layout.basic.contextmenu.control.ContextMenuOwner;
import com.top_logic.layout.basic.contextmenu.menu.Menu;
import com.top_logic.tool.boundsec.HandlerResult;

import de.haumacher.msgbuf.graph.DefaultScope;
import de.haumacher.msgbuf.graph.SharedGraphNode;
import de.haumacher.msgbuf.io.StringR;
import de.haumacher.msgbuf.io.StringW;
import de.haumacher.msgbuf.json.JsonReader;
import de.haumacher.msgbuf.json.JsonWriter;
import de.haumacher.msgbuf.server.io.WriterAdapter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;

/**
 * {@link Control} for displaying diagrams.
 */
public class DiagramControl extends AbstractControlBase
		implements JSDiagramControlCommon, ContentHandler, ContextMenuOwner {

	private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
		ContextMenuOpener.INSTANCE,
		UpdateCommand.INSTANCE,
		DispatchClickCommand.INSTANCE);

	private Diagram _diagram;

	private DefaultScope _graphScope;

	private ContextMenuProvider _contextMenuProvider = NoContextMenuProvider.INSTANCE;


	/**
	 * Creates a {@link DiagramControl}.
	 */
	public DiagramControl() {
		super(COMMANDS);
	}

	@Override
	public Diagram getModel() {
		return _diagram;
	}

	/**
	 * Sets a new {@link Diagram} to display.
	 */
	public void setModel(Diagram diagram) {
		_diagram = diagram;
		requestRepaint();
	}

	@Override
	protected void internalAttach() {
		super.internalAttach();

		getFrameScope().registerContentHandler(null, this);
	}

	@Override
	protected void internalDetach() {
		getFrameScope().deregisterContentHandler(this);

		super.internalDetach();
	}

	@Override
	public boolean isVisible() {
		return true;
	}

	@Override
	protected String getTypeCssClass() {
		return "tlDiagram";
	}

	@Override
	protected void writeControlAttributes(DisplayContext context, TagWriter out) throws IOException {
		super.writeControlAttributes(context, out);

		out.writeAttribute(DATA_CONTENT_ATTR, getFrameScope().getURL(context, this).getURL());
	}

	@Override
	protected void internalWrite(DisplayContext context, TagWriter out) throws IOException {
		out.beginBeginTag(SVG);
		writeControlAttributes(context, out);
		out.endBeginTag();
		out.endTag(SVG);

		JSControlUtil.writeCreateJSControlScript(out, JSDiagramControlCommon.CONTROL_TYPE, getID());
	}

	/**
	 * The provider for a context menu for user objects of {@link SelectableBox} nodes.
	 */
	public ContextMenuProvider getContextMenuProvider() {
		return _contextMenuProvider;
	}

	/**
	 * @see #getContextMenuProvider()
	 */
	public void setContextMenuProvider(ContextMenuProvider contextMenuProvider) {
		_contextMenuProvider = contextMenuProvider;
	}

	@Override
	public Menu createContextMenu(String contextInfo) {
		Widget node = (Widget) _graphScope.resolveOrFail(Integer.parseInt(contextInfo));
		Object userObject = node.getUserObject();
		if (userObject == null) {
			return null;
		}

		return _contextMenuProvider.getContextMenu(userObject);
	}

	@Override
	protected boolean hasUpdates() {
		// TODO: Automatically created
		return false;
	}

	@Override
	protected void internalRevalidate(DisplayContext context, UpdateQueue actions) {
		// TODO: Automatically created

	}

	@Override
	public void handleContent(DisplayContext context, String id, URLParser url) throws IOException, ServletException {
		HttpServletResponse response = context.asResponse();
		if (_diagram == null) {
			response.setContentType("text/plain");
			response.setCharacterEncoding("utf-8");
			response.getWriter().write("");
			return;
		}

		_graphScope = new ExternalScope(2, 0);
		response.setContentType("text/json");
		response.setCharacterEncoding("utf-8");
		_diagram.writeTo(_graphScope, new JsonWriter(new WriterAdapter(response.getWriter())));

		ExternalScope graphScope = new ExternalScope(2, 0);
		StringW buffer = new StringW();
		_diagram.writeTo(graphScope, new JsonWriter(buffer));
	}

	void processUpdate(String patch) throws IOException {
		Logger.info("Diagram update: " + patch, DiagramControl.class);
		JsonReader json = new JsonReader(new StringR(patch));
		_graphScope.applyChanges(json);
	}

	/**
	 * Processing click events sent from the client-side.
	 */
	public void processClick(int nodeId, Set<MouseButton> buttons) {
		ClickTarget node = (ClickTarget) _graphScope.resolveOrFail(nodeId);
		ClickHandler clickHandler = node.getClickHandler();
		if (clickHandler != null) {
			clickHandler.onClick(node, buttons);
		}
	}

	static final class UpdateCommand extends ControlCommand {

		/**
		 * Singleton {@link UpdateCommand} instance.
		 */
		public static final UpdateCommand INSTANCE = new UpdateCommand();

		/**
		 * Creates a {@link UpdateCommand}.
		 */
		private UpdateCommand() {
			super("update");
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.text("Update DiagramControl");
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			String patch = (String) arguments.get("patch");
			try {
				((DiagramControl) control).processUpdate(patch);
			} catch (IOException ex) {
				Logger.error("Faild to update diagram.", ex, DiagramControl.class);
			}
			return HandlerResult.DEFAULT_RESULT;
		}

	}

	static final class DispatchClickCommand extends ControlCommand {

		/**
		 * Singleton {@link DispatchClickCommand} instance.
		 */
		public static final DispatchClickCommand INSTANCE = new DispatchClickCommand();

		/**
		 * Creates a {@link UpdateCommand}.
		 */
		private DispatchClickCommand() {
			super("dispatchClick");
		}

		@Override
		public ResKey getI18NKey() {
			return ResKey.text("Dispatch click");
		}

		@Override
		protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
			@SuppressWarnings("unchecked")
			List<String> buttonNames = (List<String>) arguments.get("mouseButtons");
			Set<MouseButton> buttons =
				buttonNames.stream().map(n -> MouseButton.valueOf(n)).collect(Collectors.toSet());
			int nodeId = ((Number) arguments.get("nodeId")).intValue();
			((DiagramControl) control).processClick(nodeId, buttons);
			return HandlerResult.DEFAULT_RESULT;
		}
	}

}

class ExternalScope extends DefaultScope {

	Map<SharedGraphNode, Integer> _objectIds = new HashMap<>();

	/**
	 * Creates a {@link ExternalScope}.
	 */
	public ExternalScope(int totalParticipants, int participantId) {
		super(totalParticipants, participantId);
	}

	public void clear() {
		_objectIds.clear();
		index().clear();
	}

	@Override
	public int id(SharedGraphNode node) {
		Integer id = _objectIds.get(node);
		return id == null ? 0 : id.intValue();
	}

	@Override
	public void initId(SharedGraphNode node, int id) {
		_objectIds.put(node, Integer.valueOf(id));
	}

}
