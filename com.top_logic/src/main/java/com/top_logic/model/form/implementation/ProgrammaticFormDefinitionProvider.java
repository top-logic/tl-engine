/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.form.implementation;

import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.Flavor;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.form.values.edit.AllInAppImplementations;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.form.definition.FormElement;

/**
 * {@link AbstractFormElementProvider} delegating to {@link FormElementTemplateProvider} usable for
 * {@link InApp} development.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Code")
public class ProgrammaticFormDefinitionProvider
		extends AbstractFormElementProvider<ProgrammaticFormDefinitionProvider.Config> {

	private static final ImageProvider IMAGE_PROVIDER = new ImageProvider() {
		@Override
		public ThemeImage getImage(Object obj, Flavor flavor) {
			// This must not be accessed in a static block.
			return Icons.FORM_EDITOR_TOOL_CODE_ELEMENT;
		}
	};

	/**
	 * Configuration for a {@link ProgrammaticFormDefinitionProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@TagName("code")
	public interface Config extends FormElement<ProgrammaticFormDefinitionProvider> {

		/**
		 * The {@link FormElementTemplateProvider} that creates the form elements and the displaying
		 * template.
		 */
		@Options(fun = AllInAppImplementations.class)
		@Label("implementation")
		@DefaultContainer
		PolymorphicConfiguration<? extends FormElementTemplateProvider> getImpl();

	}

	private FormElementTemplateProvider _impl;

	/**
	 * Creates a new {@link ProgrammaticFormDefinitionProvider}.
	 * 
	 */
	public ProgrammaticFormDefinitionProvider(InstantiationContext context, Config config) {
		super(context, config);
		_impl = context.getInstance(config.getImpl());
	}

	/**
	 * The delegate implementation.
	 */
	protected FormElementTemplateProvider getImpl() {
		return _impl;
	}

	@Override
	protected HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		if (_impl == null) {
			return contentBox(resource(I18NConstants.FIELD_WITHOUT_IMPLEMENTATION));
		}
		return _impl.createTemplate(context);
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		if (_impl == null) {
			return false;
		}
		return _impl.getWholeLine(modelType);
	}

	@Override
	public boolean isVisible(FormEditorContext context) {
		if (_impl == null) {
			return super.isVisible(context);
		}
		return _impl.isVisible(context);
	}

	@Override
	public boolean getIsTool() {
		return true;
	}

	@Override
	public ImageProvider getImageProvider() {
		return IMAGE_PROVIDER;
	}

	@Override
	public boolean openDialog() {
		return true;
	}

}

