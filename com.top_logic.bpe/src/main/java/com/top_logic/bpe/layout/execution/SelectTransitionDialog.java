/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.w3c.dom.Document;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagUtil;
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
import com.top_logic.element.layout.formeditor.FormEditorUtil;
import com.top_logic.element.layout.formeditor.builder.TypedForm;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.layout.AbstractResourceProvider;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.CheckException;
import com.top_logic.layout.form.Constraint;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.FormGroup;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.messagebox.MessageBox.ButtonType;
import com.top_logic.layout.messagebox.SimpleFormDialog;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.impl.TransientModelFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;

/**
 * Dialog for asking the user to decide about the next process step.
 */
public class SelectTransitionDialog extends SimpleFormDialog {

	static final Document TEMPLATE = DOMUtil.parseThreadSafe(
		"<div"
			+ " xmlns='" + HTMLConstants.XHTML_NS + "'"
			+ " xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'"
			+ " xmlns:p='" + FormPatternConstants.PATTERN_NS + "'"
		+ ">"
			+ "<div class='mboxImage'>"
				+ "<t:img key='icon' alt=''/>"
			+ "</div>"

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

			+ "<p:field name='custom'/>"

		+ "</div>");

	private Token _token;

	private HandlerResult _suspended;

	private TLObject _formModel;

	/**
	 * Creates a {@link SelectTransitionDialog}.
	 */
	public SelectTransitionDialog(Token token, HandlerResult suspended) {
		super(I18NConstants.SELECT_TRANSITION_DIALOG, DisplayDimension.dim(550, DisplayUnit.PIXEL),
			DisplayDimension.dim(320, DisplayUnit.PIXEL));
		_token = token;
		_suspended = suspended;
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
				Decision selection =
					(Decision) ((SelectField) formContext.getField(INPUT_FIELD)).getSingleSelection();
				if (!formContext.checkAll()) {
					return AbstractApplyCommandHandler.createErrorResult(formContext);
				}
				Command continuation = _suspended
					.resumeContinuation(Collections.singletonMap(FinishTaskCommand.CONTEXT, selection.getLastEdge()));

				HandlerResult result = continuation.executeCommand(aContext);
				if (!result.isSuccess()) {
					return result;
				}
				return getDiscardClosure().executeCommand(aContext);
			}
		};
	}

	static class Decision {

		private final List<Edge> _edges;

		private final ResKey _label;

		private String _tooltip;

		private final ResKey _disabledReason;

		Decision(Token token, Edge... edges) {
			_edges = Arrays.asList(edges);

			ResKey label;
			String tooltip;
			Edge lastEdge = getLastEdge();
			ResKey edgeLabel = lastEdge.getTitle();
			if (edgeLabel != null) {
				label = edgeLabel;
				tooltip = lastEdge.getTooltip().localizeSourceCode();
			} else {
				tooltip = null;
				final String lastEdgeName = lastEdge.getName();
				if (!StringServices.isEmpty(lastEdgeName)) {
					label = ResKey.text(lastEdgeName);
				} else {
					Node target = lastEdge.getTarget();
					String targetName = target.getName();
					if (!StringServices.isEmpty(targetName)) {
						label = ResKey.text(targetName);
					} else if (target instanceof EndEvent) {
						ResKey key;
						EventDefinition definition = ((EndEvent) target).getDefinition();
						boolean terminate = (definition instanceof TerminateEventDefinition);
						if (terminate) {
							key = I18NConstants.EDGE_OPTION_LABEL_ABORT;
						} else {
							key = I18NConstants.EDGE_OPTION_LABEL_CANCEL;
						}
						label = key;
					} else {
						label = I18NConstants.EDGE_OPTION_NO_NAME;
					}
				}
			}

			ResKey disabledReason = null;
			for (Edge edge : edges) {
				// mark impossible transitions
				disabledReason = GuiEngine.getInstance().checkError(token, edge);
				if (disabledReason != null) {
					label = I18NConstants.IMPOSSIBLE_EDGE.fill(label);

					tooltip = TagUtil.encodeXML(Resources.getInstance().getString(disabledReason));
					break;
				}
			}

			_label = label;
			_tooltip = tooltip;
			_disabledReason = disabledReason;
		}

		public List<Edge> getEdges() {
			return _edges;
		}

		public Edge getLastEdge() {
			return _edges.get(_edges.size() - 1);
		}

		public ResKey getDisabledReason() {
			return _disabledReason;
		}
	}

	static class DecisionComparator implements Comparator<Decision> {
		private final Resources _resources;

		private final Collator _collator;

		/**
		 * Creates a {@link SelectTransitionDialog.DecisionComparator}.
		 */
		public DecisionComparator(Locale locale) {
			_resources = Resources.getInstance(locale);
			_collator = Collator.getInstance(locale);
		}

		@Override
		public int compare(Decision self, Decision other) {
			Double weight = self.getLastEdge().getWeight();
			Double otherWeight = other.getLastEdge().getWeight();

			if (weight != null) {
				if (otherWeight != null) {
					return Double.compare(weight.doubleValue(), otherWeight.doubleValue());
				} else {
					// Edge with weight to the top.
					return -1;
				}
			} else {
				if (otherWeight == null) {
					// Both without weight.
					String label = _resources.getString(self._label);
					String otherLabel = _resources.getString(other._label);
					return _collator.compare(label, otherLabel);
				} else {
					// Edge with weight to the top.
					return 1;
				}
			}
		}
	}

	static class DecisionResources extends AbstractResourceProvider {
		/**
		 * Singleton {@link DecisionResources} instance.
		 */
		public static final DecisionResources INSTANCE = new DecisionResources();

		private DecisionResources() {
			// Singleton constructor.
		}

		@Override
		public String getLabel(Object object) {
			return Resources.getInstance().getString(((Decision) object)._label);
		}

		@Override
		public String getTooltip(Object object) {
			return ((Decision) object)._tooltip;
		}

		@Override
		public ThemeImage getImage(Object object, Flavor aFlavor) {
			return null;
		}
	}

	@Override
	protected FormContext createFormContext() {
		AttributeFormContext result = new AttributeFormContext(getResourcePrefix());
		createHeaderMembers(result);
		return result;
	}

	@Override
	protected void fillFormContext(FormContext aContext) {
		List<Decision> edges = getNextEdges(_token);
		SelectField field = FormFactory.newSelectField(SimpleFormDialog.INPUT_FIELD, edges);
		field.addConstraint(new Constraint() {
			@Override
			public boolean check(Object value) throws CheckException {
				Decision decision = (Decision) CollectionUtil.getSingleValueFrom(value);
				ResKey error = decision.getDisabledReason();
				if (error != null) {
					throw new CheckException(Resources.getInstance().getString(error));
				}
				return true;
			}
		});
		field.setOptionLabelProvider(DecisionResources.INSTANCE);
		field.setMandatory(true);
		setInitialSelection(field, edges);
		aContext.addMember(field);

		FormGroup custom = new FormGroup("custom", ResPrefix.NONE);
		aContext.addMember(custom);

		TLClass formType = _token.getNode().getProcess().getParticipant().getEdgeFormType();
		if (formType != null) {
			_formModel = TransientModelFactory.createTransientObject(formType);
			TypedForm typeForm = TypedForm.lookup(formType);

			FormEditorContext context = new FormEditorContext.Builder()
				.formMode(FormMode.CREATE)
				.formType(typeForm.getFormType())
				.concreteType(typeForm.getDisplayedType())
				.model(_formModel)
				.formContext(aContext)
				.contentGroup(custom)
				.build();

			FormEditorUtil.createAttributes(context, typeForm.getFormDefinition());
		} else {
			_formModel = null;
		}
	}

	private List<Decision> getNextEdges(Token token) {
		List<Decision> res = new ArrayList<>();
		Node node = token.getNode();
		Set<? extends Edge> outgoing = node.getOutgoing();
		for (Edge edge : outgoing) {
			Node target = edge.getTarget();
			if (target instanceof Gateway gateway) {
				if (GuiEngine.getInstance().needsDecision(gateway)) {
					for (Edge gatewayOutgoing : gateway.getOutgoing()) {
						res.add(new Decision(token, edge, gatewayOutgoing));
					}
				} else {
					res.add(new Decision(token, edge));
				}
			} else if (target instanceof Task) {
				res.add(new Decision(token, edge));
			}
		}
		res.sort(new DecisionComparator(TLContext.getLocale()));
		return res;
	}

	private void setInitialSelection(SelectField field, List<Decision> options) {
		if (options.size() == 1) {
			field.setAsSingleSelection(options.iterator().next());
			return;
		}
		Decision singlePossibleEdge = null;
		boolean multiplePossibleEdges = false;
		Decision defaultEdge = null;
		optionsLoop:
		for (Decision option : options) {
			for (Edge edge : option.getEdges()) {
				ResKey error = GuiEngine.getInstance().checkError(_token, edge);
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

	private boolean isDefaultEdge(Decision option) {
		Edge lastEdge = option.getLastEdge();
		Node edgeSource = lastEdge.getSource();
		return edgeSource instanceof DefaultGateway
			&& lastEdge.equals(((DefaultGateway) edgeSource).getDefaultFlow());
	}
}