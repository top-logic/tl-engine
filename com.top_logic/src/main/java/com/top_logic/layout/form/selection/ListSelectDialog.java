/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.Command;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.form.template.FormTemplate;
import com.top_logic.layout.provider.PrefixLabelProvider;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;

/**
 * The class {@link ListSelectDialog} is an abstract superclass to select
 * options from a list representation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ListSelectDialog extends SelectDialogBase {

	/**
	 * Creates a {@link ListSelectDialog}.
	 * 
	 * @param targetSelectField
	 *        See {@link AbstractSelectDialog#AbstractSelectDialog(SelectField, SelectDialogConfig)}.
	 * @param config
	 *        See
	 *        {@link SelectDialogBase#SelectDialogBase(SelectField, SelectDialogConfig)}.
	 */
	public ListSelectDialog(SelectField targetSelectField, SelectDialogConfig config) {
		super(targetSelectField, config);
	}

	@Override
	protected LayoutControl createContentView(DialogWindowControl dialog) {
		SelectorContext context = createSelectorContext(dialog);
		DefaultListSelectDialogControlProvider controlProvider =
			new DefaultListSelectDialogControlProvider(context, getConfig());
		FormTemplate template = new FormTemplate(ResPrefix.NONE, controlProvider, false, getTemplate());
		FormGroupControl control = new FormGroupControl(context, template);
		return new LayoutControlAdapter(control);
	}

	/**
	 * Creates the {@link FormContext} for this dialog.
	 */
	protected SelectorContext createSelectorContext(DialogWindowControl dialog) {
		Command closeAction = dialog.getDialogModel().getCloseAction();

		final LabelProvider optionLabels = getTargetField().getOptionLabelProvider();
		LabelProvider pageLabels = new PrefixLabelProvider(optionLabels, 23);

		int optionsPerPage = isLarge() ? getConfig().getOptionsPerPage() : SelectorContext.ALL_OPTIONS_ON_ONE_PAGE;
		SelectorContext ctx = new OptimizedSelectorContext(getTargetField(), pageLabels, optionsPerPage, closeAction, isLeftToRight());
		return ctx;
	}

	/**
	 * Whether there are many options, i.e. whether paging is active (not all
	 * options are shown on the same page)
	 */
	protected boolean isLarge() {
		return getTargetField().getOptionCount() > getConfig().getPagableOptionTreshold();
	}

}

