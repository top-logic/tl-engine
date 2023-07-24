/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.implementation;

import static com.top_logic.element.layout.formeditor.implementation.FieldDefinitionTemplateProvider.*;
import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.formeditor.definition.FieldDefinition;
import com.top_logic.element.layout.formeditor.definition.GroupDefinition;
import com.top_logic.element.layout.formeditor.definition.OtherAttributes;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.boxes.layout.VerticalLayout;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.table.ConfigKey;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AnnotationContainer;
import com.top_logic.model.form.ReactiveFormCSS;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.form.implementation.AbstractFormElementProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormElementTemplateProvider;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.css.CssUtil;

/**
 * {@link FormElementTemplateProvider} rendering an {@link OtherAttributes} element.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class OtherAttributesTemplateProvider extends AbstractFormElementProvider<OtherAttributes> {

	private static final ImageProvider IMAGE_PROVIDER =
		ImageProvider.constantImageProvider(Icons.OTHER_ATTRIBUTES);

	/**
	 * Creates a {@link OtherAttributesTemplateProvider} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public OtherAttributesTemplateProvider(InstantiationContext context, OtherAttributes config) {
		super(context, config);
	}

	@Override
	public HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		OtherAttributes config = getConfig();

		FormMode formMode = context.getFormMode();
		if (formMode == FormMode.DESIGN) {
			HTMLTemplateFragment result =
				GroupDefinitionTemplateProvider.wrapFieldSet(config, getID(), Templates.empty(), ConfigKey.none())
					.setCssClass(CssUtil.joinCssClasses("locked", config.getCssClass()));
			return result;
		} else {
			List<HTMLTemplateFragment> otherAttributeTemplates = new ArrayList<>();
			otherAttributeTemplates.add(css(ReactiveFormCSS.RF_COLUMNS_LAYOUT));
			TLObject model = context.getModel();
			ConfigKey personalizationKey;
			{
				FormContext formContext = context.getFormContext();
				FormContainer contentGroup = context.getContentGroup();
				for (TLStructuredTypePart part : additionalParts(context)) {
					if (((AttributeFormContext) formContext).getAttributeUpdateContainer().getAttributeUpdate(part,
						model) != null) {
						continue;
					}
					FormVisibility visibility = calculateVisibility(part, FormVisibility.DEFAULT, formMode);
					if (visibility == null) {
						// Hidden.
						continue;
					}
					FormMember member = addMember(formContext, contentGroup, model, context.getFormType(), part,
						context.getDomain(), visibility, AnnotationContainer.EMPTY);
					if (member != null) {
						AttributeUpdate update = AttributeFormFactory.getAttributeUpdate(member);
						otherAttributeTemplates.add(createFieldTemplate(member, part, update));
					}
				}
				personalizationKey = ConfigKey.derived(ConfigKey.field(contentGroup), "otherAttributes");
			}

			// Note: The CSS attribute is added unconditionally as first element to the list.
			if (otherAttributeTemplates.size() <= 1) {
				// Hide the box by displaying an empty box list.
				return Templates.collectionBox(VerticalLayout.INSTANCE);
			} else {
				return GroupDefinitionTemplateProvider.wrapFieldSet(config, getID(),
					div(otherAttributeTemplates), personalizationKey);
			}
		}
	}

	private List<TLStructuredTypePart> additionalParts(FormEditorContext context) {
		return additionalParts(context.getFormType(), concreteType(context));
	}

	private TLStructuredType concreteType(FormEditorContext context) {
		TLStructuredType concreteType;
		if (context.getModel() != null) {
			concreteType = context.getModel().tType();
		} else {
			concreteType = context.getConcreteType();
		}
		return concreteType;
	}

	private List<TLStructuredTypePart> additionalParts(TLStructuredType formType, TLStructuredType generalization) {
		Map<String, TLStructuredTypePart> result = new LinkedHashMap<>();
		addAdditionalParts(result, formType, new HashSet<>(), generalization);
		return new ArrayList<>(result.values());
	}

	private void addAdditionalParts(Map<String, TLStructuredTypePart> result, TLStructuredType formType,
			Set<TLStructuredType> seen, TLStructuredType type) {
		if (!seen.add(type)) {
			// Already inspected.
			return;
		}

		if (formType != null) {
			if (TLModelUtil.isCompatibleType(type, formType)) {
				// The current type is a generalization of the concrete type and therefore
				// already covered by the form definition of the
				return;
			}
		}

		for (TLStructuredTypePart part : type.getLocalParts()) {
			String name = part.getName();

			if (!result.containsKey(name)) {
				result.put(name, part);
			}
		}
		
		if (type instanceof TLClass) {
			for (TLStructuredType generalization : ((TLClass) type).getGeneralizations()) {
				addAdditionalParts(result, formType, seen, generalization);
			}
		}
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		return getConfig().getWholeLine();
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
	public ResKey getLabel(FormEditorContext context) {
		return I18NConstants.OTHER_ATTRIBUTES;
	}

	@Override
	public DisplayDimension getDialogHeight() {
		return GroupDefinitionTemplateProvider.HEIGHT;
	}

	@Override
	public void renderPDFExport(DisplayContext context, TagWriter out, FormEditorContext renderContext) throws IOException {
		List<TLStructuredTypePart> additionalParts = additionalParts(renderContext);
		if (additionalParts.isEmpty()) {
			return;
		}
		GroupDefinition groupDefinition = TypedConfiguration.newConfigItem(GroupDefinition.class);
		groupDefinition.setWholeLine(true);
		groupDefinition.setLabel(getConfig().getLabel());
		for (TLStructuredTypePart additionalPart : additionalParts) {
			FieldDefinition fieldDefinition = TypedConfiguration.newConfigItem(FieldDefinition.class);
			fieldDefinition.setAttribute(additionalPart.getName());
			groupDefinition.getContent().add(fieldDefinition);
		}
		GroupDefinitionTemplateProvider additionalAttributesGroup = TypedConfigUtil.createInstance(groupDefinition);
		/* Attributes are resolved in the form type of the context. Therefore it must be set, cause
		 * it can be null. */
		renderContext = new FormEditorContext.Builder(renderContext)
			.formType(concreteType(renderContext))
			.build();
		additionalAttributesGroup.renderPDFExport(context, out, renderContext);
	}

}
