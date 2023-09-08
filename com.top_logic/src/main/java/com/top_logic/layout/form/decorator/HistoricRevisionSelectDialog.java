/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.decorator;

import java.util.Date;

import org.w3c.dom.Document;

import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.xml.DOMUtil;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.SingleSelectionModel;
import com.top_logic.layout.component.model.SingleSelectionListener;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.SelectionPartControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.FormPatternConstants;
import com.top_logic.layout.form.template.FormTemplateConstants;
import com.top_logic.mig.html.DefaultSingleSelectionModel;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.SelectionModel;
import com.top_logic.mig.html.SelectionModelOwner;

/**
 * {@link RevisionSelectDialog} for historic objects. The user can select between the current
 * revision and a revision for a selected date.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
class HistoricRevisionSelectDialog extends RevisionSelectDialog implements SelectionModelOwner {

	private static final String HISTORIC_RADIO_BUTTON = "historicRadioButton";

	private static final String CURRENT_RADIO_BUTTON = "currentRadioButton";

	private static final String COMPARE_CURRENT_I18N_SUFFIX = "compareCurrent";

	static final Property<SingleSelectionModel> SELECTION_MODEL =
		TypedAnnotatable.property(SingleSelectionModel.class, "selectionModel");

	private static final Document TEMPLATE;
	static {
		StringBuilder template = new StringBuilder();
		template.append("<t:group");
		template.append(" xmlns='" + HTMLConstants.XHTML_NS + "'");
		template.append(" xmlns:t='" + FormTemplateConstants.TEMPLATE_NS + "'");
		template.append(" xmlns:p='" + FormPatternConstants.PATTERN_NS + "'");
		template.append(">");
		template.append("<div style='margin: 5px; padding-left: 10px;'>");
		template.append("<div>");
		template.append("<p:field name='" + CURRENT_RADIO_BUTTON + "' />");
		template.append("<t:text key='" + COMPARE_CURRENT_I18N_SUFFIX + "'/>");
		template.append("</div>");
		template.append("<div>");
		template.append("<p:field name='" + HISTORIC_RADIO_BUTTON + "' />");
		appendDateField(template);
		template.append("</div>");
		template.append("</div>");
		template.append("</t:group>");
		TEMPLATE = DOMUtil.parseThreadSafe(template.toString());
	}

	private static class CloseCommand extends AbstractCloseCommand {

		public CloseCommand(RevisionSelectDialog dialog) {
			super(dialog);
		}

		@Override
		protected Revision getRevision() {
			FormContext formContext = _dialog.getFormContext();
			SingleSelectionModel singleSelectionModel = formContext.get(SELECTION_MODEL);
			FormMember selectedField = (FormMember) singleSelectionModel.getSingleSelection();
			if (CURRENT_RADIO_BUTTON.equals(selectedField.getName())) {
				return Revision.CURRENT;
			}

			FormField field = formContext.getField(DATE_FIELD_NAME);
			Date date = (Date) field.getValue();
			Revision revision = HistoryUtils.getRevisionAt(date.getTime());
			return revision;
		}

	}

	DefaultSingleSelectionModel _selectionModel;

	protected HistoricRevisionSelectDialog(RevisionCallback revCallback, DisplayDimension width, DisplayDimension height,
			boolean suppressRecording) {
		super(revCallback, width, height, suppressRecording);
	}

	@Override
	public SelectionModel getSelectionModel() {
		return _selectionModel;
	}

	@Override
	protected AbstractCloseCommand createCloseCommand() {
		return new CloseCommand(this);
	}

	@Override
	protected void fillFormContext(FormContext context) {
		super.fillFormContext(context);
		_selectionModel = new DefaultSingleSelectionModel(this);

		ControlProvider cp = new ControlProvider() {

			@Override
			public Control createControl(Object model, String style) {
				return new SelectionPartControl(_selectionModel, model);
			}
		};
		final HiddenField currentRadio = FormFactory.newHiddenField(CURRENT_RADIO_BUTTON);
		currentRadio.setControlProvider(cp);
		final HiddenField historicRadio = FormFactory.newHiddenField(HISTORIC_RADIO_BUTTON);
		historicRadio.setControlProvider(cp);
		final FormField newDateField = getDateField(context);
		_selectionModel.addSingleSelectionListener(new SingleSelectionListener() {

			@Override
			public void notifySelectionChanged(SingleSelectionModel model, Object formerlySelectedObject,
					Object selectedObject) {
				if (selectedObject == currentRadio) {
					updateCloseCommandExecutability(true);
				} else if (selectedObject == historicRadio) {
					updateCloseCommandExecutability(newDateField.hasValue() && newDateField.getValue() != null);
				}
				newDateField.setDisabled(selectedObject != historicRadio);
			}

		});
		_selectionModel.setSingleSelection(currentRadio);

		context.addMember(currentRadio);
		context.addMember(historicRadio);
		context.set(SELECTION_MODEL, _selectionModel);
	}

	@Override
	protected Document getTemplate() {
		return TEMPLATE;
	}

}

