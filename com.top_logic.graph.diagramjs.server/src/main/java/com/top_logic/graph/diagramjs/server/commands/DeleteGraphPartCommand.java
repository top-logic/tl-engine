/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graph.diagramjs.server.commands;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.DisplayUnit.*;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import com.top_logic.basic.util.ResKey;
import com.top_logic.common.remote.shared.ObjectScope;
import com.top_logic.element.layout.meta.TLModelPartDeleteHandler;
import com.top_logic.graph.common.model.Edge;
import com.top_logic.graph.common.model.GraphPart;
import com.top_logic.graph.common.model.Label;
import com.top_logic.graph.common.model.Node;
import com.top_logic.graph.common.model.impl.SharedGraph;
import com.top_logic.graph.diagramjs.model.DiagramJSGraphModel;
import com.top_logic.graph.diagramjs.server.DiagramJSGraphControl;
import com.top_logic.graph.diagramjs.server.I18NConstants;
import com.top_logic.graph.diagramjs.server.util.GraphModelUtil;
import com.top_logic.graph.diagramjs.server.util.model.TLInheritance;
import com.top_logic.graph.diagramjs.util.DiagramJSGraphControlCommon;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayValue;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.basic.ResourceText;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.structure.DefaultLayoutData;
import com.top_logic.layout.structure.LayoutData;
import com.top_logic.layout.structure.Scrolling;
import com.top_logic.mig.html.layout.MainLayout;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.tool.boundsec.CommandHandler;
import com.top_logic.tool.boundsec.CommandHandlerFactory;
import com.top_logic.tool.boundsec.HandlerResult;

/**
 * A {@link ControlCommand} for deleting graph parts.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public class DeleteGraphPartCommand extends ControlCommand {

	private static final String ID = "id";

	/**
	 * Singleton instance of {@link DeleteGraphPartCommand}.
	 */
	public static final DeleteGraphPartCommand INSTANCE = new DeleteGraphPartCommand();

	private DeleteGraphPartCommand() {
		super(DiagramJSGraphControlCommon.DELETE_GRAPH_PART_COMMAND);
	}

	@Override
	public ResKey getI18NKey() {
		return I18NConstants.DELETE_GRAPH_PART_COMMAND;
	}

	@Override
	protected HandlerResult execute(DisplayContext commandContext, Control control, Map<String, Object> arguments) {
		LayoutData layout = getDialogLayout();
		DiagramJSGraphControl graphControl = (DiagramJSGraphControl) control;
		GraphPart graphPart = getGraphPart(getIDArgument(arguments), graphControl);
		SharedGraph graph = getSharedGraph(graphControl);

		Object tag = graphPart.getTag();
		DisplayValue title = new ResourceText(I18NConstants.DELETE_GRAPH_PART_COMMAND);

		if (!isDeleteProtected(tag)) {
			return createDeleteCheckBox(commandContext, graphControl, layout, graphPart, graph, tag, title);
		} else {
			return createDeleteProtectedMessageBox(commandContext, layout, title);
		}
	}

	private boolean isDeleteProtected(Object tag) {
		if (tag instanceof TLModelPart) {
			return DisplayAnnotations.isDeleteProtected((TLModelPart) tag);
		}

		return false;
	}

	private HandlerResult createDeleteProtectedMessageBox(DisplayContext context, LayoutData layout,
			DisplayValue title) {
		DisplayValue message = new ResourceText(I18NConstants.GRAPH_PART_DELETE_PROTECTED_ENABLED);

		return MessageBox.confirm(context, layout, true, title, message, MessageBox.button(ButtonType.OK));
	}

	private HandlerResult createDeleteCheckBox(DisplayContext context, DiagramJSGraphControl control, LayoutData layout,
			GraphPart graphPart,
			SharedGraph graph, Object tag, DisplayValue title) {
		DisplayValue message = getDialogMessage(graphPart);

		return MessageBox.confirm(context, layout, true, title, message, getDeleteOKButton(control, graph, tag),
			getDeleteChancelButton());
	}

	private CommandModel getDeleteChancelButton() {
		return MessageBox.button(ButtonType.NO);
	}

	private CommandModel getDeleteOKButton(DiagramJSGraphControl control, SharedGraph graph, Object tag) {
		return MessageBox.button(ButtonType.YES, getGraphPartDeletionCommand(control, graph, tag));
	}

	private Command getGraphPartDeletionCommand(DiagramJSGraphControl control, SharedGraph graph, Object tag) {
		return new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext anInnerContext) {
				DiagramJSGraphModel graphModel = (DiagramJSGraphModel) graph;
				Collection<? extends GraphPart> selectedGraphParts = graphModel.getSelectedGraphParts();

				HandlerResult handlerResult = removePersistentGraphPart(anInnerContext, control, tag);

				if (handlerResult.isSuccess()) {
					GraphModelUtil.removeGraphParts(graphModel, selectedGraphParts);
				}

				return handlerResult;
			}
		};
	}

	private String getIDArgument(Map<String, Object> arguments) {
		return (String) arguments.get(ID);
	}

	private DefaultLayoutData getDialogLayout() {
		return new DefaultLayoutData(dim(500, PIXEL), 100, dim(50, PIXEL), 100, Scrolling.AUTO);
	}

	private DisplayValue getDialogMessage(GraphPart graphPart) {
		if (graphPart instanceof Label) {
			return new ResourceText(I18NConstants.DELETE_LABEL_MESSAGE);
		} else if (graphPart instanceof Node) {
			return new ResourceText(I18NConstants.DELETE_NODE_MESSAGE);
		} else if (graphPart instanceof Edge) {
			return new ResourceText(I18NConstants.DELETE_EDGE_MESSAGE);
		} else {
			return new ResourceText(I18NConstants.UNKNOWN_DELETION_TYPE_MESSAGE);
		}
	}

	HandlerResult removePersistentGraphPart(DisplayContext displayContext, DiagramJSGraphControl control,
			Object model) {
		if (model instanceof TLInheritance) {
			GraphModelUtil.deleteInheritance((TLInheritance) model, control);
		} else {
			CommandHandler handler = CommandHandlerFactory.getInstance().getHandler(TLModelPartDeleteHandler.COMMAND);
			MainLayout mainLayout = displayContext.getLayoutContext().getMainLayout();

			return handler.handleCommand(displayContext, mainLayout, model, Collections.emptyMap());
		}

		return HandlerResult.DEFAULT_RESULT;
	}

	SharedGraph getSharedGraph(DiagramJSGraphControl graphControl) {
		return graphControl.getModel().getGraph();
	}

	GraphPart getGraphPart(String id, DiagramJSGraphControl graphControl) {
		ObjectScope objectScope = getObjectScope(graphControl);

		return (GraphPart) objectScope.obj(id);
	}

	private ObjectScope getObjectScope(DiagramJSGraphControl graphControl) {
		return getSharedGraph(graphControl).data().scope();
	}

}
