/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.security.layout;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.basic.CommandModel;
import com.top_logic.layout.basic.fragments.Fragments;
import com.top_logic.layout.form.control.ChoiceControl;
import com.top_logic.layout.form.model.BooleanField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.messagebox.AbstractTemplateDialog;
import com.top_logic.layout.messagebox.MessageBox;
import com.top_logic.layout.structure.DefaultDialogModel;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.tl.ai.core.security.layout.PolicyDraft.CellState;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.util.Resources;

/**
 * The bulk edit of a policy matrix: grants, forbids or clears the chosen names for every selected role
 * at once.
 */
/*
 * Issue #404. A dialog over the selected rows, because the classic table has no selection of cells,
 * columns or ranges: the rows are what can be selected, and the names are chosen here.
 *
 * What the dialog says before anything happens is the point of it. It counts the cells that change,
 * says which rows the section does not name yet - those are restricted first, or skipped - and says
 * that a row it empties stays restricted rather than falling back to the section default.
 */
final class EditSelectionDialog extends AbstractTemplateDialog {

	static final String FIELD_NAMES = "names";

	static final String FIELD_ACTION = "action";

	static final String FIELD_RESTRICT_FIRST = "restrictFirst";

	private final ToolAccessPolicyComponent _matrix;

	private final List<String> _roles;

	private SelectField _names;

	private SelectField _action;

	private BooleanField _restrictFirst;

	private DynamicFragment _summary;

	/**
	 * Creates an {@link EditSelectionDialog}.
	 *
	 * @param matrix
	 *        The matrix the selection belongs to.
	 * @param roles
	 *        The selected roles.
	 */
	EditSelectionDialog(ToolAccessPolicyComponent matrix, List<String> roles) {
		super(DefaultDialogModel.dialogModel(I18NConstants.DIALOG_EDIT_SELECTION__COUNT
			.fill(Integer.valueOf(roles.size())), DisplayDimension.px(640), DisplayDimension.px(420)));
		_matrix = matrix;
		_roles = roles;
	}

	@Override
	protected void fillFormContext(FormContext context) {
		_names = FormFactory.newSelectField(FIELD_NAMES, _matrix.columnNames(), true, false);
		_names.setOptionLabelProvider(option -> _matrix.nameLabel((String) option));
		// Every name as a checkbox: a pop-up picker would hide what is chosen behind a second dialog.
		_names.setControlProvider((model, style) -> new ChoiceControl((SelectField) model));
		labelAndHelp(_names, _matrix.dimension() == MatrixDimension.CAPABILITIES
			? I18NConstants.FIELD_BULK_GROUPS : I18NConstants.FIELD_BULK_NAMES);
		_names.addValueListener((field, oldValue, newValue) -> _summary.requestRepaint());
		context.addMember(_names);

		_action = FormFactory.newSelectField(FIELD_ACTION,
			List.of(CellState.GRANTED, CellState.DENIED, CellState.ABSENT), false,
			List.of(CellState.GRANTED), false, false, null);
		_action.setMandatory(true);
		_action.setOptionLabelProvider(option -> Resources.getInstance().getString(actionLabel(option)));
		_action.setControlProvider((model, style) -> new ChoiceControl((SelectField) model));
		labelAndHelp(_action, I18NConstants.FIELD_BULK_ACTION);
		_action.addValueListener((field, oldValue, newValue) -> _summary.requestRepaint());
		context.addMember(_action);

		_restrictFirst = FormFactory.newBooleanField(FIELD_RESTRICT_FIRST);
		_restrictFirst.initializeField(Boolean.TRUE);
		labelAndHelp(_restrictFirst, I18NConstants.FIELD_BULK_RESTRICT_FIRST);
		_restrictFirst.addValueListener((field, oldValue, newValue) -> _summary.requestRepaint());
		_restrictFirst.setVisible(_matrix.dimension().isRunSection());
		context.addMember(_restrictFirst);

		_summary = new DynamicFragment(this::summary);
	}

	@Override
	protected TagTemplate getTemplate() {
		return div(css("aiSecurityView aiPolicyBulk"),
			div(css("aiPolicyBulkRoles"),
				htmlTemplate(Fragments.div("aiPolicyBulkLabel", Fragments.message(I18NConstants.BULK_ROLES))),
				htmlTemplate(roleChips())),
			fieldBox(FIELD_NAMES),
			fieldBox(FIELD_ACTION),
			fieldBox(FIELD_RESTRICT_FIRST),
			div(css("aiPolicyBulkSummary"), htmlTemplate(_summary)));
	}

	private HTMLFragment roleChips() {
		List<HTMLFragment> chips = new ArrayList<>();
		for (String role : _roles) {
			chips.add(Fragments.span("aiPolicyChip", Fragments.text(role)));
		}
		return Fragments.div("aiPolicyChips", chips.toArray(new HTMLFragment[chips.size()]));
	}

	/** What applying would do, said before it is done. */
	private HTMLFragment summary() {
		PolicyMatrix matrix = _matrix.matrix();
		List<String> names = ToolAccessPolicyComponent.strings(_names.getValue());
		CellState state = ToolAccessPolicyComponent.single(_action.getValue(), CellState.GRANTED);
		boolean restrictFirst = restrictFirst();
		int cells = matrix.affectedCells(_roles, names, state, restrictFirst);
		int rows = 0;
		int locked = 0;
		for (String role : _roles) {
			if (matrix.isLocked(role)) {
				locked++;
				if (!restrictFirst) {
					continue;
				}
			}
			rows++;
		}
		List<HTMLFragment> parts = new ArrayList<>();
		parts.add(Fragments.message(I18NConstants.BULK_SUMMARY__CELLS_ROWS
			.fill(Integer.valueOf(cells), Integer.valueOf(rows))));
		if (locked > 0) {
			parts.add(Fragments.text(" "));
			parts.add(Fragments.message((restrictFirst ? I18NConstants.BULK_RESTRICTED_FIRST__COUNT
				: I18NConstants.BULK_SKIPPED__COUNT).fill(Integer.valueOf(locked))));
		}
		if (_matrix.dimension().isRunSection() && state != CellState.GRANTED) {
			parts.add(Fragments.text(" "));
			parts.add(Fragments.message(I18NConstants.BULK_EMPTY_ROW_STAYS));
		}
		return Fragments.span(parts.toArray(new HTMLFragment[parts.size()]));
	}

	private boolean restrictFirst() {
		return _matrix.dimension().isRunSection() && Boolean.TRUE.equals(_restrictFirst.getValue());
	}

	@Override
	protected void fillButtons(List<CommandModel> buttons) {
		buttons.add(MessageBox.button(I18NConstants.BULK_APPLY, closeDialogAfter(context -> {
			List<String> names = ToolAccessPolicyComponent.strings(_names.getValue());
			if (!names.isEmpty()) {
				_matrix.applyBulk(_roles, names,
					ToolAccessPolicyComponent.single(_action.getValue(), CellState.GRANTED), restrictFirst());
			}
			return HandlerResult.DEFAULT_RESULT;
		})));
		addCancel(buttons);
	}

	private static ResKey actionLabel(Object option) {
		if (option == CellState.DENIED) {
			return I18NConstants.BULK_ACTION_DENY;
		}
		return option == CellState.ABSENT ? I18NConstants.BULK_ACTION_CLEAR : I18NConstants.BULK_ACTION_GRANT;
	}

	private static void labelAndHelp(com.top_logic.layout.form.FormMember field, ResKey label) {
		Resources resources = Resources.getInstance();
		field.setLabel(resources.getString(label));
		String tooltip = resources.getString(label.tooltipOptional());
		if (tooltip != null) {
			field.setTooltip(tooltip);
		}
	}

}
