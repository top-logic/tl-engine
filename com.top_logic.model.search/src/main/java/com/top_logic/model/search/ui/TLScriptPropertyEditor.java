/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.ui;

import com.top_logic.basic.col.Mapping;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.values.edit.EditorFactory;
import com.top_logic.layout.form.values.edit.ValueModel;
import com.top_logic.layout.form.values.edit.editor.Editor;
import com.top_logic.layout.form.values.edit.editor.PlainEditor;
import com.top_logic.model.search.expr.config.dom.Expr;

/**
 * {@link Editor} for displaying properties of type {@link Expr} resolving model references and
 * reporting potential errors.
 * 
 * @see ModelReferenceChecker
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class TLScriptPropertyEditor extends PlainEditor {

	/**
	 * Singleton {@link TLScriptPropertyEditor} instance.
	 */
	@SuppressWarnings("hiding")
	public static final TLScriptPropertyEditor INSTANCE = new TLScriptPropertyEditor();

	private TLScriptPropertyEditor() {
		// Singleton constructor.
	}

	@Override
	protected void init(EditorFactory editorFactory, ValueModel model, FormField field,
			Mapping<Object, Object> uiConversion, Mapping<Object, Object> storageConversion) {
		super.init(editorFactory, model, field, uiConversion, storageConversion);

		field.addConstraint(ModelReferenceChecker.INSTANCE);
	}

}
