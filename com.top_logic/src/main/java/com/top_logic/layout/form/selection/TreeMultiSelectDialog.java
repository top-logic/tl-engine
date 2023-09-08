/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;

import org.w3c.dom.Document;

import com.top_logic.basic.col.Provider;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.FormGroupControl;
import com.top_logic.layout.structure.DialogWindowControl;
import com.top_logic.layout.structure.LayoutControl;
import com.top_logic.layout.structure.LayoutControlAdapter;

/**
 * A {@link SelectDialogBase} which provides dialogs for trees with multiple node selection.
 * 
 * @author <a href="mailto:sts@top-logic.com">Stefan Steinert</a>
 */
public class TreeMultiSelectDialog extends TreeSelectDialog {

	public TreeMultiSelectDialog(SelectField targetSelectField, SelectDialogConfig config) {
		super(targetSelectField, config);
	}

	@Override
	protected LayoutControl createContentView(DialogWindowControl dialog) {
		TreeSelectorContext context = createSelectorContext(dialog);
		FormGroupControl content =
			new FormGroupControl(context, new TreeMultiSelectDialogProvider(context, getConfig()), getTemplate(),
				I18NConstants.TREE_SELECT_DIALOG);
		return new LayoutControlAdapter(content);
	}

	@Override
	protected final Provider<Document> getTemplateProvider() {
		return new TreeMultiSelectTemplateBuilder(getConfig());
	}
}
