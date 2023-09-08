/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.layout.tiles.document;

import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.control.DisplayPDFControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.model.HiddenField;
import com.top_logic.layout.form.model.StringField;
import com.top_logic.mig.html.ModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.util.Resources;

/**
 * {@link ModelBuilder} creating a {@link FormContext} displaying a PDF document using the
 * {@link DisplayPDFControl}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class AbstractDisplayDocumentBuilder<C extends PolymorphicConfiguration<?>>
		extends AbstractConfiguredInstance<C>
		implements ModelBuilder {

	private static final String FIELD_NAME = "field";

	private static final String DOCUMENT_TILE_CSS_CLASS = "documentTile";

	/**
	 * Creates a new {@link AbstractDisplayDocumentBuilder}.
	 */
	public AbstractDisplayDocumentBuilder(InstantiationContext context, C config) {
		super(context, config);
	}

	@Override
	public Object getModel(Object businessModel, LayoutComponent aComponent) {
		FormContext context = new FormContext(aComponent);
		context.addMember(createField(FIELD_NAME, businessModel, aComponent));
		context.setImmutable(true);

		template(context, div(css(DOCUMENT_TILE_CSS_CLASS), member(FIELD_NAME)));
		return context;
	}

	/**
	 * Creates the only field in the result {@link FormContext}.
	 * 
	 * @param fieldName
	 *        Name of the returned field.
	 * @param businessModel
	 *        The business model to determine document to display from.
	 * @param component
	 *        Displayed component
	 * 
	 * @see #getModel(Object, LayoutComponent)
	 */
	protected abstract FormMember createField(String fieldName, Object businessModel, LayoutComponent component);

	/**
	 * Creates a {@link FormMember} displaying the given document with a {@link DisplayPDFControl}.
	 * 
	 * @param fieldName
	 *        Name of the result field.
	 * @param document
	 *        The document to display.
	 */
	protected FormMember createDisplayPDFField(String fieldName, BinaryDataSource document) {
		HiddenField field = FormFactory.newHiddenField(fieldName, document);
		field.setControlProvider(DisplayPDFControl.CONTROL_PROVIDER);
		return field;
	}

	/**
	 * Helper method to create a field displaying the given error message.
	 * 
	 * @param fieldName
	 *        Name of the result field.
	 * @param errorMsg
	 *        Error message to display.
	 */
	protected StringField errorField(String fieldName, ResKey errorMsg) {
		StringField errorField = FormFactory.newStringField(fieldName);
		errorField.setAsString(Resources.getInstance().getString(errorMsg));
		return errorField;
	}

}
