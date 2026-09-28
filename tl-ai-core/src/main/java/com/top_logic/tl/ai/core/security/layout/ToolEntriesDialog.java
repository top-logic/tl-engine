/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.messagebox.AbstractTemplateDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.tl.ai.core.security.layout.ToolAccessPolicyComponent.MatrixRow;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * The single tools one row of the capability matrix grants or forbids by name.
 *
 * <p>
 * Entries naming single tools do not fit into a cell of a matrix of capability groups, so the matrix
 * counts them and this dialog lists them.
 * </p>
 */
final class ToolEntriesDialog extends AbstractTemplateDialog {

	static final String FIELD_GRANTED = "granted";

	static final String FIELD_DENIED = "denied";

	private final ToolAccessPolicyComponent _matrix;

	private final MatrixRow _row;

	private SelectField _granted;

	private SelectField _denied;

	/**
	 * Creates a {@link ToolEntriesDialog}.
	 *
	 * @param matrix
	 *        The matrix the row belongs to.
	 * @param row
	 *        The row whose single tools to edit.
	 */
	ToolEntriesDialog(ToolAccessPolicyComponent matrix, MatrixRow row) {
		super(DefaultDialogModel.dialogModel(I18NConstants.DIALOG_TOOL_ENTRIES__ROW.fill(rowName(row)),
			DisplayDimension.px(640), DisplayDimension.px(360)));
		_matrix = matrix;
		_row = row;
	}

	private static String rowName(MatrixRow row) {
		switch (row.kind()) {
			case BASELINE:
				return Resources.getInstance().getString(I18NConstants.ROW_BASELINE);
			case BACKGROUND:
				return Resources.getInstance().getString(I18NConstants.ROW_BACKGROUND);
			default:
				return row.roleName();
		}
	}

	@Override
	protected void fillFormContext(FormContext context) {
		PolicyDraft draft = _matrix.session().draft();
		List<String> granted;
		List<String> denied;
		switch (_row.kind()) {
			case BASELINE:
				granted = new ArrayList<>(draft.baselineTools());
				denied = List.of();
				break;
			case BACKGROUND:
				granted = new ArrayList<>(draft.backgroundTools());
				denied = List.of();
				break;
			default:
				granted = draft.roleTools(_row.roleName(), false);
				denied = draft.roleTools(_row.roleName(), true);
				break;
		}
		_granted = toolField(FIELD_GRANTED, granted, denied, _row.isRole() ? I18NConstants.FIELD_TOOLS_GRANTED
			: I18NConstants.FIELD_TOOLS_ADMITTED);
		context.addMember(_granted);
		_denied = toolField(FIELD_DENIED, denied, granted, I18NConstants.FIELD_TOOLS_DENIED);
		_denied.setVisible(_row.isRole());
		context.addMember(_denied);
	}

	/**
	 * A picker over every tool the application has, plus the ones the policy names already, so that a
	 * name the application no longer has stays visible and can be removed.
	 */
	private static SelectField toolField(String name, List<String> value, List<String> other, ResKey label) {
		Set<String> options = new LinkedHashSet<>(ToolAccessPolicyComponent.allToolNames());
		options.addAll(value);
		options.addAll(other);
		SelectField field = FormFactory.newSelectField(name, new ArrayList<>(options), true, false);
		field.setAsSelection(value);
		labelAndHelp(field, label);
		return field;
	}

	@Override
	protected TagTemplate getTemplate() {
		return div(css("aiSecurityView aiPolicyToolsDialog"),
			htmlTemplate(Fragments.p("aiSecurityNote", Fragments.message(I18NConstants.TOOL_ENTRIES_NOTE))),
			fieldBox(FIELD_GRANTED),
			fieldBox(FIELD_DENIED));
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(I18NConstants.BULK_APPLY, closeDialogAfter(context -> {
			_matrix.setTools(_row, ToolAccessPolicyComponent.strings(_granted.getValue()),
				ToolAccessPolicyComponent.strings(_denied.getValue()));
			return HandlerResult.DEFAULT_RESULT;
		})));
		addCancel(buttons);
	}

	private static void labelAndHelp(FormMember field, ResKey label) {
		Resources resources = Resources.getInstance();
		field.setLabel(resources.getString(label));
		String tooltip = resources.getString(label.tooltipOptional());
		if (tooltip != null) {
			field.setTooltip(tooltip);
		}
	}

}
