/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor;

import static com.top_logic.layout.form.template.model.Templates.*;

import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.element.layout.meta.I18NConstants;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.AttributeUpdateFactory;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.MetaControlProvider;
import com.top_logic.element.meta.gui.MetaAttributeGUIHelper;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormField;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.model.FormFactory;
import com.top_logic.layout.form.template.ControlProvider;
import com.top_logic.layout.form.template.model.AbstractMember;
import com.top_logic.layout.form.template.model.DescriptionBoxTemplate;
import com.top_logic.layout.form.template.model.Embedd;
import com.top_logic.layout.form.template.model.internal.TemplateAnnotation;
import com.top_logic.layout.form.template.model.internal.TemplateControlProvider;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.form.definition.FormDefinition;
import com.top_logic.model.form.implementation.FormDefinitionTemplateProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.util.Resources;

/**
 * A utility class to create {@link FormMember}s for the form editor.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FormEditorUtil {

	/**
	 * Create a {@link FormMember} for the given meta attribute. This method will not append the
	 * {@link FormMember} to the {@link AttributeFormContext}.
	 * 
	 * @param formContext
	 *        The form context to create the {@link FormMember}, must not be <code>null</code>.
	 * @param contentGroup
	 *        The group in the from to add new fields to.
	 * @param type
	 *        The type to display a form for.
	 * @param attribute
	 *        The meta attribute to be appended, must not be <code>null</code>.
	 * @param domain
	 *        Optional additional identifier to the newly created member. May be <code>null</code>.
	 * @param annotations
	 *        Custom attribute annotations.
	 * @return The created {@link FormMember}.
	 */
	public static FormMember createAnotherMetaAttribute(AttributeFormContext formContext, FormContainer contentGroup,
			TLStructuredType type, TLStructuredTypePart attribute, String domain,
			AnnotationLookup annotations) {
		String attributeId = MetaAttributeGUIHelper.getAttributeIDCreate(attribute, domain);

		if (contentGroup.hasMember(attributeId)) {
			FormMember member = contentGroup.getMember(attributeId);
			AttributeUpdate update = AttributeFormFactory.getAttributeUpdate(member);
			update.setLocalAnnotations(annotations);
			boolean renderWholeLine = AttributeOperations.renderWholeLine(attribute, update);
			member.set(AbstractMember.RENDER_WHOLE_LINE, renderWholeLine);
			return member;
		} else {
			AttributeUpdate update = AttributeUpdateFactory
				.createAttributeUpdateForCreate(formContext.getAttributeUpdateContainer(), type, attribute, domain);
			update.setLocalAnnotations(annotations);
			return createMember(formContext, attribute, update);
		}
	}

	private static FormMember createMember(AttributeFormContext formContext, TLStructuredTypePart attribute,
			AttributeUpdate update) {
		FormMember member = null;

		if (update != null) {
			member = formContext.createFormMemberForUpdate(update);
			boolean renderWholeLine = false;

			if (member != null) {
				renderWholeLine = AttributeOperations.renderWholeLine(attribute, update);
			} else {
				member = createPlaceholder(attribute);
				renderWholeLine = true;
			}

			member.set(AbstractMember.RENDER_WHOLE_LINE, renderWholeLine);
			member.set(AbstractMember.FIRST_COLUMN_WIDTH, DescriptionBoxTemplate.FIRST_COLUMN_WIDTH);
		}
		return member;
	}

	/**
	 * Create a {@link FormMember} for the given meta attribute with data of a given
	 * {@link Wrapper}. This method will not append the {@link FormMember} to the
	 * {@link AttributeFormContext}.
	 * 
	 * @param formContext
	 *        The form context to create the {@link FormMember}, must not be <code>null</code>.
	 * @param contentGroup
	 *        The group in the from to add new fields to.
	 * @param aMA
	 *        The meta attribute to be appended, must not be <code>null</code>.
	 * @param anAttributed
	 *        The object to get the current values from.
	 * @param isDisabled
	 *        If <code>true</code> no values can be changes (for FormConstraints).
	 * @param annotations
	 *        Custom attribute annotations.
	 * @return The created {@link FormMember}.
	 */
	public static FormMember createAnotherMetaAttributeForEdit(AttributeFormContext formContext,
			FormContainer contentGroup, TLStructuredTypePart aMA, TLObject anAttributed, boolean isDisabled,
			AnnotationLookup annotations) {
		String name = MetaAttributeGUIHelper.getAttributeID(aMA, anAttributed);

		if (contentGroup.hasMember(name)) {
			return contentGroup.getMember(name);
		} else {
			AttributeUpdate theUpdate =
				formContext
					.editObject(anAttributed)
					.newEditUpdateCustom(aMA, isDisabled, false, annotations);
			FormMember member = createMember(formContext, aMA, theUpdate);

			return member;
		}
	}

	/**
	 * Add an attribute update for the given meta attribute to the given form context.
	 * 
	 * @param aContext
	 *        The form context to append the update to, must not be <code>null</code>.
	 * @param contentGroup
	 *        The group in the from to add new fields to.
	 * @param type
	 *        The type to display a form for.
	 * @param attribute
	 *        The meta attribute to be appended, must not be <code>null</code>.
	 * @param domain
	 *        Optional additional identifier to the newly created member. May be <code>null</code>.
	 * @param annotations
	 *        Custom attribute annotations.
	 * @return The created {@link FormMember}.
	 */
	public static FormMember addAnotherMetaAttribute(AttributeFormContext aContext, FormContainer contentGroup,
			TLStructuredType type, TLStructuredTypePart attribute, String domain,
			AnnotationLookup annotations) {
		FormMember member = createAnotherMetaAttribute(aContext, contentGroup, type, attribute, domain, annotations);
		addMember(contentGroup, member);

		return contentGroup.getMember(member.getName());
	}

	/**
	 * Create a {@link FormMember} and add it to the given {@link AttributeFormContext}.
	 * 
	 * @param aContext
	 *        The {@link AttributeFormContext} to add the {@link FormMember} to.
	 * @param contentGroup
	 *        The group in the from to add new fields to.
	 * @param aMA
	 *        The meta attribute to be appended, must not be <code>null</code>.
	 * @param anAttributed
	 *        The object to get the current values from.
	 * @param isDisabled
	 *        If <code>true</code> no values can be changes (for FormConstraints).
	 * @param annotations
	 *        Custom attribute annotations.
	 * @return The created {@link FormMember}.
	 */
	public static FormMember addAnotherMetaAttribute(AttributeFormContext aContext, FormContainer contentGroup,
			TLStructuredTypePart aMA, TLObject anAttributed, boolean isDisabled,
			AnnotationLookup annotations) {
		FormMember member =
			createAnotherMetaAttributeForEdit(aContext, contentGroup, aMA, anAttributed, isDisabled, annotations);
		if (member == null) {
			return null;
		}
		addMember(contentGroup, member);

		return contentGroup.getMember(member.getName());
	}

	private static void addMember(FormContainer contentGroup, FormMember member) {
		if (member != null && !(contentGroup.hasMember(member.getName()))) {
			contentGroup.addMember(member);
		}
	}

	private static FormField createPlaceholder(TLStructuredTypePart aMA) {
		FormField field = FormFactory.newStringField(aMA.getName() + aMA.tId());
		field.setLabel(MetaLabelProvider.INSTANCE.getLabel(aMA));
		field.initializeField(Resources.getInstance().getString(I18NConstants.FORM_EDITOR__NO_ATTRIBUTED_OBJECT));
		field.setImmutable(true);

		return field;
	}

	/**
	 * Creates a display group for a dynamic form.
	 */
	public static void createEditorGroup(AttributeFormContext formContext, TLStructuredType formType,
			FormDefinition formDefinition, TLObject contextModel, FormMode formMode) {
		FormEditorContext context = new FormEditorContext.Builder()
			.formMode(formMode)
			.formType(formType)
			.model(contextModel)
			.formContext(formContext)
			.contentGroup(formContext)
			.build();
		
		FormEditorUtil.createAttributes(context, formDefinition);
	}

	/**
	 * Creates fields in the given {@link FormContainer} described by the given
	 * {@link FormDefinition} and annotates a corresponding template for display.
	 * 
	 * @param context
	 *        Context information that controls the creation of the form.
	 * @param formDefinition
	 *        The {@link FormDefinition} describing the form to display.
	 */
	public static void createAttributes(FormEditorContext context, FormDefinition formDefinition) {
		if (context.getFormMode() == FormMode.DESIGN) {
			throw new IllegalArgumentException("Create attributes must not be called in design mode.");
		}
		FormContainer contentGroup = context.getContentGroup();
		if (formDefinition != null) {
			FormDefinitionTemplateProvider formProvider = TypedConfigUtil.createInstance(formDefinition);
			HTMLTemplateFragment formTemplate = formProvider.createTemplate(context);
			template(contentGroup, div(formTemplate));
		} else {
			template(contentGroup, div());
		}
	}

	/**
	 * Applies the given {@link HTMLTemplateFragment} to the given {@link FormMember}.
	 */
	public static <M extends FormMember> M template(M field, HTMLTemplateFragment template) {
		ControlProvider cp;
		if (template instanceof Embedd) {
			cp = new TemplateAnnotation((Embedd) template);
		} else {
			cp = new TemplateControlProvider(template, MetaControlProvider.INSTANCE);
		}
		field.setControlProvider(cp);
		return field;
	}
}