/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.search.tiles;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.html.template.TagTemplate;
import com.top_logic.layout.ResPrefix;
import com.top_logic.layout.basic.ThemeImage;
import com.top_logic.layout.editor.config.OptionalTypeTemplateParameters;
import com.top_logic.layout.editor.config.TypeTemplateParameters;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLReference;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.definition.NamedPartNames;
import com.top_logic.model.util.AllTypeAttributes;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link AbstractPreviewContent} displaying a form as preview.
 * 
 * <p>
 * A {@link FormPreviewContent} displays some attributes of the given model in a simple form as
 * preview.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@InApp
public class FormPreviewContent extends AbstractPreviewContent<FormPreviewContent.Config> {

	/**
	 * Typed configuration interface definition for {@link FormPreviewContent}.
	 * 
	 * @author <a href="mailto:dbu@top-logic.com">dbu</a>
	 */
	@DisplayOrder({
		Config.TYPE,
		Config.ATTRIBUTES,
		Config.ICON,
	})
	public interface Config extends AbstractPreviewContent.Config<FormPreviewContent>, TypeTemplateParameters {

		/** Configuration name for the value of {@link #getAttributes()}. */
		String ATTRIBUTES = "attributes";

		/** Configuration name for the value of {@link #getIcon()}. */
		String ICON = "icon";

		/**
		 * The attributes of {@link #getType()} which are displayed in the form.
		 */
		@Options(fun = AllTypeAttributes.class, args = @Ref(TYPE), mapping = NamedPartNames.class)
		@Format(CommaSeparatedStrings.class)
		@Name(ATTRIBUTES)
		List<String> getAttributes();

		/**
		 * Icon that is displayed when the given model is not of the expected {@link #getType()}.
		 */
		@Name(ICON)
		ThemeImage getIcon();

	}

	/**
	 * Create a {@link FormPreviewContent}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public FormPreviewContent(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HTMLFragment createPreviewPart(Object model) {
		TLObject typedModel = getModel(model);
		if (typedModel == null) {
			return imagePreview(getConfig().getIcon());
		}
		TLStructuredType type = typedModel.tType();

		AttributeFormContext form = new AttributeFormContext(ResPrefix.GLOBAL);
		TLFormObject editObject = form.editObject(typedModel);
		List<String> attributeNames = getConfig().getAttributes();
		List<HTMLTemplateFragment> templates = new ArrayList<>();
		templates.add(style("display:table"));
		for (String attribute : attributeNames) {
			TLStructuredTypePart part = type.getPartOrFail(attribute);
			AttributeUpdate update = editObject.newEditUpdateDefault(part, true);
			FormMember formMember = form.addFormConstraintForUpdate(update);
			if (formMember == null) {
				continue;
			}
			if (part instanceof TLReference) {
				// Ensure that value is not displayed as table.
				form.setControlProvider(SelectionControlProvider.INSTANCE);
			}
			TagTemplate label = div(style("display:table-cell"), css("rf_label"), labelWithColon(formMember.getName()));
			TagTemplate member = div(style("display:table-cell"), member(formMember));
			templates.add(div(style("display:table-row"), label, member));
		}
		// Deactivate all input fields
		form.setImmutable(true);
		template(form, div(templates.toArray(new HTMLTemplateFragment[templates.size()])));

		return defaultPreview(form.getControlProvider().createFragment(form), "formPreview");
	}

	private TLObject getModel(Object model) {
		TLClass type = OptionalTypeTemplateParameters.resolve(getConfig());
		TLObject object;
		if (model instanceof TLObject) {
			object = (TLObject) model;
		} else {
			return null;
		}
		if (!TLModelUtil.isCompatibleInstance(type, object)) {
			return null;
		}
		return object;

	}

}

