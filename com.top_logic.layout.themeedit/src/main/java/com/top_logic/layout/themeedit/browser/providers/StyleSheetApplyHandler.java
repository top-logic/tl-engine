/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.component.AbstractApplyCommandHandler;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.CommandHandler;

/**
 * {@link CommandHandler} storing a style sheet back to disk.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StyleSheetApplyHandler extends AbstractApplyCommandHandler {

	/**
	 * Creates a {@link StyleSheetApplyHandler} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public StyleSheetApplyHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	protected boolean storeChanges(LayoutComponent component, FormContext formContext, Object model) {
		FormField field = formContext.getField(StyleSheetFormBuilder.STYLE_DATA_FIELD);
		String fileName = field.get(StyleSheetFormBuilder.FILE_NAME);
		try {
			File file = FileManager.getInstance().getIDEFile(fileName);
			try (OutputStream out = new FileOutputStream(file)) {
				try (Writer writer = new OutputStreamWriter(out, StyleSheetFormBuilder.styleSheetCharSet())) {
					writer.write((String) field.getValue());
				}
			}
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		return true;
	}

}
