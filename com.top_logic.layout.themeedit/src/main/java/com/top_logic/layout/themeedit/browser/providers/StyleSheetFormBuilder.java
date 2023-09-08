/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.themeedit.browser.providers;

import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import com.top_logic.basic.FileManager;
import com.top_logic.basic.col.TypedAnnotatable;
import com.top_logic.basic.col.TypedAnnotatable.Property;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.io.StreamUtilities;
import com.top_logic.basic.shared.string.StringServicesShared;
import com.top_logic.layout.codeedit.control.CodeEditorControl;
import com.top_logic.layout.codeedit.control.CodeEditorControl.WarnLevel;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * {@link ModelBuilder} creating the form displaying the CSS data of a referenced style sheet.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class StyleSheetFormBuilder implements ModelBuilder {

	static final String STYLE_DATA_FIELD = "styleData";
	static final Property<String> FILE_NAME = TypedAnnotatable.property(String.class, "fileName");

	/**
	 * Singleton {@link StyleSheetFormBuilder} instance.
	 */
	public static final StyleSheetFormBuilder INSTANCE = new StyleSheetFormBuilder();

	private StyleSheetFormBuilder() {
		// Singleton constructor.
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		FormContext result = new FormContext(aComponent);
		StringField field = newCSSField(STYLE_DATA_FIELD);

		if (businessModel != null) {
			String fileName = ((NamedConfiguration) businessModel).getName();
			field.set(FILE_NAME, fileName);
			try (InputStream in = FileManager.getInstance().getStream(fileName)) {
				String cssData = StreamUtilities.readAllFromStream(in, styleSheetCharSet());
				field.setValue(cssData);
			} catch (IOException ex) {
				throw new IOError(ex);
			}
		} else {
			field.setImmutable(true);
		}

		result.addMember(field);
		return result;
	}

	private StringField newCSSField(String fieldName) {
		StringField field = FormFactory.newStringField(fieldName);
		CodeEditorControl.CP cp = new CodeEditorControl.CP(CodeEditorControl.MODE_CSS);
		/* <i>TopLogic</i> uses variables with own syntax (e.g. %BACKGROUND_COLOR%) which would produce
		 * errors. Therefore warnings must be disabled. */
		cp.warnLevel(WarnLevel.NONE);
		field.setControlProvider(cp);
		field.setCssClasses(CodeEditorControl.FULL_SIZE_CSS_CLASS);
		return field;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return aModel == null || aModel instanceof NamedConfiguration;
	}

	static Charset styleSheetCharSet() {
		return StringServicesShared.CHARSET_UTF_8;
	}

}
