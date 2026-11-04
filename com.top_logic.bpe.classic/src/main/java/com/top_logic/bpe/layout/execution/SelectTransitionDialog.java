/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.layout.execution;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.w3c.dom.Document;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.MapBuilder;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.basic.xml.TagUtil;
import com.top_logic.bpe.execution.engine.Decision;
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
import com.top_logic.layout.wysiwyg.ui.i18n.I18NStructuredText;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.impl.TransientModelFactory;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

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
		super(I18NConstants.SELECT_TRANSITION_DIALOG, DisplayDimension.dim(720, DisplayUnit.PIXEL),
			DisplayDimension.dim(400, DisplayUnit.PIXEL));
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

	@Override
	public boolean isChanged() {
		// Disable dirty handling for this dialog since the form data is not meant to be saved
		// in the traditional sense. The OK button handles form submission and continuation.
		return false;
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
			return Resources.getInstance().getString(((Decision) object).getDisplayLabel());
		}

		@Override
		public String getTooltip(Object object) {
			Decision decision = (Decision) object;
			if (decision.isBlocked()) {
				return TagUtil.encodeXML(decision.getErrors().stream()
					.map(Resources.getInstance()::getString)
					.collect(Collectors.joining("; ")));
			}
			I18NStructuredText tooltip = decision.getTooltip();
			return tooltip != null ? tooltip.localizeSourceCode() : null;
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
		List<Decision> edges = GuiEngine.getInstance().getDecisions(_token);
		Optional<Decision> defaultEdge = edges.stream().filter(Decision::isDefault).findFirst();
		SelectField field = FormFactory.newSelectField(SimpleFormDialog.INPUT_FIELD, edges);
		if (defaultEdge.isPresent()) {
			field.setAsSingleSelection(defaultEdge.get());
		}
		// add Error Constraint
		field.addConstraint(new Constraint() {
			@Override
			public boolean check(Object value) throws CheckException {
				Decision decision = (Decision) CollectionUtil.getSingleValueFrom(value);
				if (decision == null) {
					return true;
				}
				List<ResKey> errors = decision.getErrors();
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
				// Warnings of an edge leading to the gateway are confirmed before this dialog opens.
				List<ResKey> warnings = GuiEngine.getInstance().checkWarnings(_token, decision.getLastEdge());
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
				TLClass formType = selectedDecision.getFormType();

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
									_formModel.tUpdateByName(attributeName, oldAttribute);
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
}
