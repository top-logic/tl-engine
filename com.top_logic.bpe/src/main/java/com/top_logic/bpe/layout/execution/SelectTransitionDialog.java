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
import java.util.stream.Collectors;

import org.w3c.dom.Document;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.bpe.bpml.display.RuleCondition;
import com.top_logic.bpe.bpml.display.RuleType;
import com.top_logic.bpe.bpml.display.SequenceFlowRule;
import com.top_logic.bpe.bpml.display.StandardRule;
import com.top_logic.bpe.bpml.model.DefaultGateway;
import com.top_logic.bpe.bpml.model.Edge;
import com.top_logic.bpe.bpml.model.EndEvent;
import com.top_logic.bpe.bpml.model.EventDefinition;
import com.top_logic.bpe.bpml.model.Gateway;
import com.top_logic.bpe.bpml.model.Node;
import com.top_logic.bpe.bpml.model.SequenceFlow;
import com.top_logic.bpe.bpml.model.Task;
import com.top_logic.bpe.bpml.model.TerminateEventDefinition;
import com.top_logic.bpe.bpml.model.impl.SequenceFlowBase;
import com.top_logic.bpe.execution.engine.GuiEngine;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.bpe.layout.execution.command.FinishTaskCommand;
import com.top_logic.bpe.layout.execution.command.I18NConstants;
import com.top_logic.dob.ex.NoSuchAttributeException;
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
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.component.WarningsDialog;
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
import com.top_logic.model.TLStructuredTypePart;
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

	/**
	 * 
	 * Create command that handles suspended task resumption with cancellation
	 *
	 * @return Command instance that implements the cancellation behavior
	 */
	protected Command getNoCommand() {
		return new Command() {

			@Override
			public HandlerResult executeCommand(DisplayContext aContext) {
				Command continuation =
					_suspended.resumeContinuation(
						Collections.singletonMap(FinishTaskCommand.CONTEXT, FinishTaskCommand.CANCEL));

				continuation.executeCommand(aContext);
				return getDiscardClosure().executeCommand(aContext);
			}

		};

	}

	/**
	 * 
	 * Create command to handle form submission and task continuation When executed, this command:
	 * 1. Validates form input and selection 2. Stores form data if valid 3. Resumes suspended
	 * continuation with selection and form model as additional arguments 4. Handles warnings via
	 * dialog if present 5. Executes continuation and cleanup
	 * 
	 * @return Command instance that implements form processing and continuation logic
	 */
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
				formContext.store();

				Command continuation = _suspended.resumeContinuation(
					new MapBuilder<String, Object>()
						.put(FinishTaskCommand.CONTEXT, selection)
						.put(FinishTaskCommand.ADDITIONAL, _formModel)
						.toMap());

				if (formContext.hasWarnings()) {
					WarningsDialog.openWarningsDialog(aContext.getWindowScope(), ResKey.legacy("workflow"),
						formContext, continuation);
					return getDiscardClosure().executeCommand(aContext);
				}
					HandlerResult result = continuation.executeCommand(aContext);

					if (!result.isSuccess()) {
						return result;
					}
					return getDiscardClosure().executeCommand(aContext);
				}
		};
	}

	/**
	 * Decision about the path to walk.
	 */
	public static class Decision {

		private final List<Edge> _path;

		private final ResKey _label;

		private String _tooltip;

		private final List<ResKey> _disabledReasons;

		private final List<ResKey> _warnings;

		Decision(Token token, Edge... path) {
			_path = Arrays.asList(path);

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

			List<ResKey> disabledReasons = new ArrayList<>();
			List<ResKey> warnings = new ArrayList<>();
			for (Edge edge : path) {
				// mark impossible transitions
				disabledReasons = GuiEngine.getInstance().checkErrors(token, edge);

				if (!disabledReasons.isEmpty()) {
					label = I18NConstants.IMPOSSIBLE_EDGE.fill(label);

					String combinedErrors = disabledReasons.stream()
						.map(resKey -> Resources.getInstance().getString(resKey))
						.collect(Collectors.joining("; "));
					tooltip = TagUtil.encodeXML(combinedErrors);
					break;
				}
				// get warnigs associated with edge
				warnings = GuiEngine.getInstance().checkWarnings(token, edge);
			}

			_label = label;
			_tooltip = tooltip;
			_disabledReasons = disabledReasons;
			_warnings = warnings;
		}

		/**
		 * The {@link Edge} to walk.
		 */
		public List<Edge> getPath() {
			return _path;
		}

		/**
		 * The {@link Edge} pointing to the new target {@link Node} for which a new {@link Token} is
		 * created.
		 */
		public Edge getLastEdge() {
			return _path.get(_path.size() - 1);
		}

		/**
		 * If the {@link #getPath()} cannot be walked, the reason that prevent this decision.
		 */
		public List<ResKey> getDisabledReasons() {
			return _disabledReasons;
		}

		/**
		 * If the {@link #getPath()} can be walked, the warnings that come with this decision.
		 */
		public List<ResKey> getWarnings() {
			return _warnings;
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
		// add Error Constraint
		field.addConstraint(new Constraint() {
			@Override
			public boolean check(Object value) throws CheckException {
				Decision decision = (Decision) CollectionUtil.getSingleValueFrom(value);
				List<ResKey> errors = decision.getDisabledReasons();
				if (!errors.isEmpty()) {
					CheckException[] checkExceptions = new CheckException[errors.size()];
					int index = 0;
					for (ResKey error : errors) {
						checkExceptions[index++] = new CheckException(Resources.getInstance().getString(error));
					}
					throw combine(checkExceptions);
				}
				return true;
			}
		});
		// add Warning Constraint
		field.addWarningConstraint(new Constraint() {
			@Override
			public boolean check(Object value) throws CheckException {
				Decision decision = (Decision) CollectionUtil.getSingleValueFrom(value);
				List<ResKey> warnings = decision.getWarnings();
				if (warnings != null && !warnings.isEmpty()) {
					CheckException[] checkExceptions = new CheckException[warnings.size()];
					int index = 0;
					for (ResKey warning : warnings) {
						checkExceptions[index++] = new CheckException(Resources.getInstance().getString(warning));
					}
					throw combine(checkExceptions);
				}
				return true;
			}
		});

		field.setOptionLabelProvider(DecisionResources.INSTANCE);
		field.setMandatory(true);
		aContext.addMember(field);

		FormGroup custom = new FormGroup("custom", ResPrefix.NONE);
		aContext.addMember(custom);

		// add Value Listener to SelectionField in Order to change the associated formType
		// dynamically
		field.addValueListener(new ValueListener() {
			@Override
			public void valueChanged(FormField selection, Object oldValue, Object newValue) {
				// save old formcontext
				aContext.checkAll();
				aContext.store();

				// remove old custom FormGroup
				aContext.removeMember(custom);

				// create new FormGroup to replace old one
				FormGroup custom = new FormGroup("custom", ResPrefix.NONE);
				aContext.addMember(custom);

				if (newValue == null) {
					return;
				}

				Decision selectedDecision = (Decision) CollectionUtil.getSingleValueFrom(newValue);
				if (selectedDecision == null) {
					return;
				}

				// Get form type based on selection
				TLClass formType = determineFormType(selectedDecision);

				if (formType != null) {
					TLObject oldFormModel = _formModel;

					_formModel = TransientModelFactory.createTransientObject(formType);
					TypedForm typeForm = TypedForm.lookup(formType);

					// copy entered values from the old Form to the new Form if they match
					if (oldFormModel != null) {
						for (TLStructuredTypePart attribute : formType.getAllParts()) {
							String attributeName = attribute.getName();
							if ("tType".equals(attributeName)) {
								continue;
							}
							try {
								Object oldAttribute = oldFormModel.tValueByName(attributeName);
								if (oldAttribute != null) {
									_formModel.tSetData(attributeName, oldAttribute);
								}
							} catch (NoSuchAttributeException e) {
								// Skip attributes that don't exist in old model
								continue;
							}
						}
					}

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

		});

		setInitialSelection(field, edges);
	}

	private List<Decision> getNextEdges(Token token) {
		List<Decision> res = new ArrayList<>();
		Node node = token.getNode();
		Set<? extends Edge> outgoing = node.getOutgoing();
		// Process each outgoing edge from current node
		for (Edge edge : outgoing) {
			Node target = edge.getTarget();
			// Handle Gateway nodes and their decision paths
			if (target instanceof Gateway gateway) {
				if (GuiEngine.getInstance().needsDecision(gateway)) {
					for (Edge gatewayOutgoing : gateway.getOutgoing()) {
						SequenceFlow flow = (SequenceFlow) gatewayOutgoing;
						SequenceFlowRule rule = flow.getRule();
						if (rule != null) {
							// Check visibility rules - only show edges where all HIDDEN conditions
							// are met
							boolean showEdge = rule.getRuleConditions().stream()
								.map(config -> (RuleCondition) new StandardRule(null, (StandardRule.Config<?>) config))
								.filter(condition -> condition.getRuleType() == RuleType.HIDDEN) // Only
																									// consider
																									// HIDDEN
																									// conditions
								.allMatch(condition -> condition.getCondition(token.getProcessExecution()));
							if (showEdge) {
								res.add(new Decision(token, edge, gatewayOutgoing));
							}
						} else {
							// No rules - add decision unconditionally
							res.add(new Decision(token, edge, gatewayOutgoing));
						}
					}
				} else {
					// Gateway doesn't need decision - add direct path
					res.add(new Decision(token, edge));
				}
			} else if (target instanceof Task) {
				res.add(new Decision(token, edge));
			}
		}
		// Sort decisions based on locale
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
			for (Edge edge : option.getPath()) {
				List<ResKey> errors = GuiEngine.getInstance().checkErrors(_token, edge);
				if (!errors.isEmpty()) {
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

	/**
	 * Combines the the given {@link CheckException}s to one {@link CheckException}.
	 *
	 * @param results
	 *        the check results of other checks. The array may contain <code>null</code> elements.
	 * @return a {@link CheckException} containing all messages of the given non-null exceptions.
	 */
	protected CheckException combine(CheckException... results) {
		List<CheckException> exceptions = new ArrayList<>(results.length);
		for (CheckException exception : results) {
			if (exception != null) {
				exceptions.add(exception);
			}
		}
		if (exceptions.isEmpty()) {
			return null;
		} else if (exceptions.size() == 1) {
			return CollectionUtil.getFirst(exceptions);
		} else {
			StringBuilder sb = new StringBuilder();
			boolean further = false;
			for (CheckException e : exceptions) {
				if (further) {
					sb.append("\n");
				} else {
					further = true;
				}
				sb.append(e.getMessage());
			}
			return new CheckException(sb.toString());
		}
	}

	/**
	 * 
	 * Check the last edge in the path first (the edge to the next task) Then Check the first edge
	 * (gateway edge) if available Otherwise Fall back to process-level configuration
	 *
	 * @param decision
	 *        the decision selected in the Dialog
	 * @return the formType
	 */
	private TLClass determineFormType(Decision decision) {
		List<Edge> path = decision.getPath();

		// 1. Check the last edge in the path first (the edge to the next node)
		if (!path.isEmpty()) {
			Edge lastEdge = path.get(path.size() - 1);
			if (lastEdge instanceof SequenceFlowBase) {
				TLClass lastEdgeFormType = ((SequenceFlowBase) lastEdge).getFormType();
				if (lastEdgeFormType != null) {
					return lastEdgeFormType;
				}
			}
		}

		// 2. Check the first edge (gateway edge) if available
		if (!path.isEmpty()) {
			Edge firstEdge = path.get(0);
			if (firstEdge instanceof SequenceFlowBase) {
				TLClass gatewayEdgeFormType = ((SequenceFlowBase) firstEdge).getFormType();
				if (gatewayEdgeFormType != null) {
					return gatewayEdgeFormType;
				}
			}
		}

		// 3. Fall back to processExecution configuration
		return _token.getNode().getProcess().getParticipant().getEdgeFormType();
	}
}