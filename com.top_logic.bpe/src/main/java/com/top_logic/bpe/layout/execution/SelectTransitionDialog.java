/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.w3c.dom.Document;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.bpe.bpml.model.DefaultGateway;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.EndEvent;
import com.top_logic.bpe.bpml.model.EventDefinition;
import com.top_logic.bpe.bpml.model.Gateway;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.Task;
import com.top_logic.bpe.bpml.model.TerminateEventDefinition;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.bpe.layout.execution.command.FinishTaskCommand;
import com.top_logic.bpe.layout.execution.command.I18NConstants;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * Dialog for asking the next process step.
 */
public class SelectTransitionDialog extends SimpleFormDialog {

	public abstract class DisplayType {

		void setLabelProvider(SelectField field, Token token) {
			// do nothing by default
		}

		void setLabel(SelectField field, Token token) {
			// do nothing by default
		};

	}

	public class DecisionSelection extends FlowSelection {

		private Gateway _gateway;

		public DecisionSelection(Gateway gateway) {
			_gateway = gateway;
		}

		@Override
		public void setLabel(SelectField field, Token token) {
			field.setLabel(_gateway.getName());
		}

	}

	public class FlowSelection extends DisplayType {

		@Override
		public void setLabelProvider(SelectField field, Token token) {
			field.setOptionLabelProvider(new LabelProvider() {

				@Override
				public String getLabel(Object object) {
					String result;
					Object[] edges = (Object[]) object;
					Edge lastEdge = (Edge) edges[edges.length - 1];
					String name = lastEdge.getName();
					if (!StringServices.isEmpty(name)) {
						result = name;
					} else {
						Node target = lastEdge.getTarget();
						name = target.getName();
						if (!StringServices.isEmpty(name)) {
							result = name;
						} else if (target instanceof EndEvent) {
							ResKey key;
							EventDefinition definition = ((EndEvent) target).getDefinition();
							boolean terminate = (definition instanceof TerminateEventDefinition);
							if (terminate) {
								key = I18NConstants.EDGE_OPTION_LABEL_ABORT;
							} else {
								key = I18NConstants.EDGE_OPTION_LABEL_CANCEL;
							}
							result = Resources.getInstance().getString(key);
						} else {
							result = Resources.getInstance().getString(I18NConstants.EDGE_OPTION_NO_NAME);
						}
					}

					for (Object edge : edges) {
						// mark impossible transitions
						if (GuiEngine.getInstance().checkError((Edge) edge, token) != null) {
							result = "! " + result;
							break;
						}
					}

					return result;
				}
			});
		}

	}

	static final Document TEMPLATE = DOMUtil.parseThreadSafe(
		"<div"
			+ " xmlns='" + HTMLConstants.XHTML_NS + "'"
			+ " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
			+ " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
			+ ">"
			+ "<div class='mboxImage'>"
			+ "<t:img key='icon' alt=''/>"
			+ "</div>"
			+ "<div>"
			+ "<table>"
			+ "<tr><td>"
			+ "<p><t:text key='message'/></p>"
			+ "<p>"
			+ "<p:field name='" + SimpleFormDialog.INPUT_FIELD + "' style='label' />"
			+ "</p><p>"
			+ "<p:field name='" + SimpleFormDialog.INPUT_FIELD + "' />"
			+ HTMLConstants.NBSP
			+ "<p:field name='" + SimpleFormDialog.INPUT_FIELD + "' style='"
			+ FormTemplateConstants.STYLE_ERROR_VALUE
			+ "' />"
			+ "</p>"
			+ "</td></tr>"
			+ "</table>"
			+ "</div>"
			+ "</div>");

	private Token _token;

	private HandlerResult _suspended;

	private DisplayType _displayType;

	/**
	 * Creates a {@link SelectTransitionDialog}.
	 */
	public SelectTransitionDialog(Token token, HandlerResult suspended) {
		super(I18NConstants.SELECT_TRANSITION_DIALOG, DisplayDimension.dim(550, DisplayUnit.PIXEL),
			DisplayDimension.dim(320, DisplayUnit.PIXEL));
		_token = token;
		_displayType = findDisplayType(token);
		_suspended = suspended;
	}

	private DisplayType findDisplayType(Token token) {
		Node node = token.getNode();
		Set<? extends Edge> outgoing = node.getOutgoing();
		if (outgoing.size() == 1) {
			Node target = outgoing.iterator().next().getTarget();
			if (target instanceof Gateway) {
				if (!StringServices.isEmpty(target.getName())) {
					Set<? extends Edge> outgoing2 = target.getOutgoing();
					for (Edge edge : outgoing2) {
						if (StringServices.isEmpty(edge.getName())) {
							return new FlowSelection();
						}
					}
					return new DecisionSelection((Gateway) target);
				}
			}
		}
		return new FlowSelection();
	}

	@Override
	protected FormTemplate getTemplate() {
		return defaultTemplate(TEMPLATE, false, I18NConstants.SELECT_TRANSITION_DIALOG);
	}

	@Override
	protected void fillButtons(List<CommandModel> someButtons) {
		someButtons.add(MessageBox.button(ButtonType.OK, getYesCommand()));
		someButtons.add(MessageBox.button(ButtonType.CANCEL, getNoCommand()));

	}

	protected Command getNoCommand() {
		return new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext aContext) {
				Command continuation =
					_suspended.resumeContinuation(Collections.singletonMap(FinishTaskCommand.CONTEXT, FinishTaskCommand.CANCEL));

				continuation.executeCommand(aContext);
				return getDiscardClosure().executeCommand(aContext);
			}

		};

	}

	protected Command getYesCommand() {
		return new Command() {
			@Override
			public HandlerResult executeCommand(DisplayContext aContext) {
				FormContext formContext = getFormContext();
				Object[] selection =
					(Object[]) ((SelectField) formContext.getField(INPUT_FIELD)).getSingleSelection();
				if (!formContext.checkAll()) {
					return AbstractApplyCommandHandler.createErrorResult(formContext);
				}
				Command continuation = _suspended
					.resumeContinuation(Collections.singletonMap(FinishTaskCommand.CONTEXT, selection[selection.length - 1]));

				HandlerResult result = continuation.executeCommand(aContext);
				if (!result.isSuccess()) {
					return result;
				}
				return getDiscardClosure().executeCommand(aContext);
			}
		};
	}

	@Override
	protected void fillFormContext(FormContext aContext) {
		Set<Edge[]> edges = getNextEdges(_token);
		SelectField field = FormFactory.newSelectField(SimpleFormDialog.INPUT_FIELD, edges);
		field.addConstraint(new Constraint() {

			@Override
			public boolean check(Object value) throws CheckException {
				Object first = CollectionUtil.getFirst(value);
				checkPotentialEdge(first);
				if (first instanceof Object[]) {
					for (Object potEdge : (Object[]) first) {
						checkPotentialEdge(potEdge);
					}
				}
				return true;
			}

			@SuppressWarnings("synthetic-access")
			private void checkPotentialEdge(Object first) throws CheckException {
				if (!(first instanceof Edge)) {
					return;
				}
				Edge edge = (Edge) first;
				ResKey error = GuiEngine.getInstance().checkError(edge, _token);
				if (error != null) {
					throw new CheckException(Resources.getInstance().getString(error));
				}
			}
		});
		_displayType.setLabelProvider(field, _token);
		_displayType.setLabel(field, _token);
		field.setMandatory(true);
		setInitialSelection(field, edges);
		aContext.addMember(field);
	}

	private Set<Edge[]> getNextEdges(Token token) {
		Set<Edge[]> res = new HashSet<>();
		Node node = token.getNode();
		Set<? extends Edge> outgoing = node.getOutgoing();
		for (Edge edge : outgoing) {
			Node target = edge.getTarget();
			if (target instanceof Gateway) {
				Gateway gateway = (Gateway) target;
				if (GuiEngine.getInstance().isManual(gateway)) {
					for (Edge gatewayOutgoing : gateway.getOutgoing()) {
						res.add(new Edge[] { edge, gatewayOutgoing });
					}

				} else {
					res.add(new Edge[] { edge });
				}
			} else if (target instanceof Task) {
				res.add(new Edge[] { edge });
			}
		}
		return res;
	}

	private void setInitialSelection(SelectField field, Set<Edge[]> options) {
		if (options.size() == 1) {
			field.setAsSingleSelection(options.iterator().next());
			return;
		}
		Edge[] singlePossibleEdge = null;
		boolean multiplePossibleEdges = false;
		Edge[] defaultEdge = null;
		optionsLoop:
		for (Edge[] option : options) {
			for (Edge edge : option) {
				ResKey error = GuiEngine.getInstance().checkError(edge, _token);
				if (error != null) {
					// Select only possible edges
					continue optionsLoop;
				}
			}
			if (isDefaultEdge(option)) {
				// Select default edge if exists
				defaultEdge = option;
				break;
			}
			if (singlePossibleEdge == null) {
				singlePossibleEdge = option;
			} else {
				// Multiple possible edges.
				multiplePossibleEdges = true;
			}
		}
		if (defaultEdge != null) {
			field.setAsSingleSelection(defaultEdge);
		} else if (!multiplePossibleEdges && singlePossibleEdge != null) {
			field.setAsSingleSelection(singlePossibleEdge);
		}
	}

	private boolean isDefaultEdge(Edge[] option) {
		Edge lastEdge = option[option.length - 1];
		Node edgeSource = lastEdge.getSource();
		return edgeSource instanceof DefaultGateway
			&& lastEdge.equals(((DefaultGateway) edgeSource).getDefaultFlow());
	}
}