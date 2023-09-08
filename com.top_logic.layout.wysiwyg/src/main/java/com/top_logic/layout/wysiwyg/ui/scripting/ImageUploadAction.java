/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.wysiwyg.ui.scripting;

import java.util.HashMap;
import java.util.Map;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.ClassDefault;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.element.meta.AbstractStorageBase.Config;
import com.top_logic.layout.VetoException;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.model.AbstractFormField;
import com.top_logic.layout.form.model.FormFieldInternals;
import com.top_logic.layout.scripting.action.ApplicationAction;
import com.top_logic.layout.scripting.recorder.ref.ModelName;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.AbstractApplicationActionOp;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.wysiwyg.ui.StructuredText;

/**
 * {@link ApplicationAction} that represents an image upload for a {@link StructuredText}.
 *
 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
 */
public interface ImageUploadAction extends ApplicationAction {
	/**
	 * {@link AbstractApplicationActionOp} for {@link ImageUploadAction}.
	 *
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public class Op extends AbstractApplicationActionOp<ImageUploadAction> {

		private static final String UNEXPECTED_VETO_RECEIVED = "Unexpected veto received.";

		/**
		 * @param context
		 *        {@link InstantiationContext} context to instantiate sub configurations.
		 * @param config
		 *        {@link Config} for this {@link Op}.
		 */
		public Op(InstantiationContext context, ImageUploadAction config) {
			super(context, config);
		}

		@Override
		protected Object processInternal(ActionContext context, Object argument) throws Throwable {
			try {
				FormField formField = getFormField(context);

				StructuredText structuredText = (StructuredText) formField.getValue();
				String sourceCode = structuredText.getSourceCode();

				Map<String, BinaryData> images = getImages(context, formField, structuredText);

				FormFieldInternals.setValue((AbstractFormField) formField, new StructuredText(sourceCode, images));
			} catch (VetoException exception) {
				ApplicationAssertions.fail(getConfig(), UNEXPECTED_VETO_RECEIVED, exception);
			}

			return argument;
		}

		private Map<String, BinaryData> getImages(ActionContext context, FormField formField, StructuredText text) {
			Map<String, BinaryData> images = text.getImages();

			BinaryData image = getImage(context, formField);
			Map<String, BinaryData> newImages = getImages(image, images);

			return newImages;
		}

		private BinaryData getImage(ActionContext context, FormField formField) {
			return (BinaryData) context.resolve(getConfig().getImage(), formField);
		}

		private FormField getFormField(ActionContext context) {
			return (FormField) context.resolve(getConfig().getField());
		}

		private Map<String, BinaryData> getImages(BinaryData image, Map<String, BinaryData> images) {
			Map<String, BinaryData> newImages = new HashMap<>();

			newImages.putAll(images);
			newImages.put(image.getName(), image);

			return newImages;
		}

	}

	@ClassDefault(ImageUploadAction.Op.class)
	@Override
	Class<? extends AbstractApplicationActionOp<?>> getImplementationClass();

	/**
	 * The {@link FormField} used for structured text.
	 */
	ModelName getField();

	/**
	 * The {@link BinaryData} that should be uploaded.
	 */
	ModelName getImage();

	/**
	 * @param field
	 *        The {@link FormField} used for structured text.
	 */
	void setField(ModelName field);

	/**
	 * @param image
	 *        The {@link BinaryData} that should be uploaded.
	 */
	void setImage(ModelName image);
}
