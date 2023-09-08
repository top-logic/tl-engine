/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.selection;


import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.top_logic.basic.col.Provider;
import com.top_logic.layout.form.model.SelectField;
import com.top_logic.layout.form.template.FormGroupControl;

/**
 * The class {@link SelectDialogBase} provides dialogs in default style for
 * editing selection of a {@link SelectField}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class SelectDialogBase extends AbstractSelectDialog {
	
	/**
	 * Create a {@link SelectDialogBase} for the given {@link SelectField}.
	 * 
	 * @param targetSelectField
	 *        the field whose selection shall be changed
	 * @param config
	 *        the {@link SelectDialogProvider} which constructs this dialog
	 */
	public SelectDialogBase(SelectField targetSelectField, SelectDialogConfig config) {
		super(targetSelectField, config);
	}

	/**
	 * @see SelectDialogConfig#isLeftToRight()
	 */
    public boolean isLeftToRight() {
		return getConfig().isLeftToRight();
    }
	
	/**
	 * This method returns the template for the {@link FormGroupControl} for the
	 * {@link SelectorContext}.
	 * 
	 */
	protected final Element getTemplate() {
		Document document = getTemplateProvider().get();
		return document.getDocumentElement();
	}

	/**
	 * The algorithm that constructs the rendering template.
	 */
	protected abstract Provider<Document> getTemplateProvider();

}
