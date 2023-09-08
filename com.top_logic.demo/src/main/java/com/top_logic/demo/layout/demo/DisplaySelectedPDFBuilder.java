/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.layout.demo;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.FragmentControl;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.ValueListener;
import com.top_logic.layout.form.control.DisplayPDFControl;
import com.top_logic.layout.form.model.DataField;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link ModelBuilder} creating a {@link FormContext} to demonstrate inline display of PDF
 * documents.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DisplaySelectedPDFBuilder implements ModelBuilder {

	/**
	 * Singleton {@link DisplaySelectedPDFBuilder} instance.
	 */
	public static final DisplaySelectedPDFBuilder INSTANCE = new DisplaySelectedPDFBuilder();

	private DisplaySelectedPDFBuilder() {
		// Singleton constructor.
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		FormContext context = new FormContext(aComponent);
		FormField uploadField = addUploadField(context);
		FormField displayField = addDisplayField(context, uploadField);
		template(context, div(
			div(fieldBox(uploadField.getName()),
				div(member(displayField.getName())))));
		return context;
	}

	private FormField addDisplayField(FormContext fc, FormField uploadField) {
		HiddenField displayField = FormFactory.newHiddenField("displayField");
		displayField.setControlProvider(new ControlProvider() {

			@Override
			public Control createControl(Object model, String style) {
				return new FragmentControl(new HTMLFragment() {

					@Override
					public void write(DisplayContext context, TagWriter out) throws IOException {
						out.beginBeginTag(HTMLConstants.DIV);
						out.writeAttribute(HTMLConstants.STYLE_ATTR, "position:relative; height:600px;");
						out.endBeginTag();
						DisplayPDFControl.CONTROL_PROVIDER.createControl(model, style).write(context, out);
						out.endTag(HTMLConstants.DIV);
					}
				});
			}
		});
		uploadField.addValueListener(new ValueListener() {
			
			@Override
			public void valueChanged(FormField field, Object oldValue, Object newValue) {
				if (newValue == null) {
					displayField.setValue(newValue);
					return;
				}
				BinaryDataSource newPDF = (BinaryDataSource) newValue;
				String name = newPDF.getName();
				if (!name.endsWith(".pdf")) {
					field.setValue(oldValue);
					throw new TopLogicException(ResKey.text(fc.getResources().getStringResource("onlyPDF")));
				}
				displayField.setValue(newPDF);
			}
		});
		fc.addMember(displayField);
		return displayField;
	}

	private FormField addUploadField(FormContext fc) {
		DataField upload = FormFactory.newDataField("upload");
		fc.addMember(upload);
		return upload;
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

}

