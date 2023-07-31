/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.formeditor.implementation;

import static com.top_logic.layout.DisplayDimension.*;
import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.formeditor.FormEditorUtil;
import com.top_logic.element.layout.formeditor.definition.FieldDefinition;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.AttributeUpdate;
import com.top_logic.element.meta.SimpleEditContext;
import com.top_logic.element.meta.form.AttributeFormContext;
import com.top_logic.element.meta.form.AttributeFormFactory;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.form.FieldProvider;
import com.top_logic.element.meta.form.fieldprovider.AbstractWrapperFieldProvider;
import com.top_logic.element.meta.form.fieldprovider.TableSupportingComplexFieldProvider;
import com.top_logic.element.meta.form.overlay.TLFormObject;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.basic.ErrorFragmentGenerator;
import com.top_logic.layout.form.FormContainer;
import com.top_logic.layout.form.FormMember;
import com.top_logic.layout.form.boxes.reactive_tag.AttributeImageProvider;
import com.top_logic.layout.form.control.LabelControl;
import com.top_logic.layout.form.model.FormContext;
import com.top_logic.layout.form.template.model.AbstractMember;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.provider.LabelProviderService;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.layout.table.model.TableConfig;
import com.top_logic.layout.table.model.TableConfigurationFactory;
import com.top_logic.layout.table.model.TableConfigurationProvider;
import com.top_logic.layout.table.provider.DefaultTableConfigurationProvider;
import com.top_logic.mig.html.HTMLConstants;
import com.top_logic.mig.html.HTMLUtil;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.AnnotationLookup;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.LabelPosition;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLCreateVisibility;
import com.top_logic.model.annotate.TLVisibility;
import com.top_logic.model.annotate.Visibility;
import com.top_logic.model.annotate.ui.PDFRendererAnnotation;
import com.top_logic.model.annotate.ui.ReferencePresentation;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.form.definition.FormVisibility;
import com.top_logic.model.form.definition.LabelPlacement;
import com.top_logic.model.form.implementation.AbstractFormElementProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.resources.TLTypePartResourceProvider;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.tool.export.pdf.PDFRenderer;
/**
 * Creates a template for a {@link FieldDefinition} and stores the necessary information.
 * 
 * @author <a href="mailto:iwi@top-logic.com">Isabell Wittich</a>
 */
public class FieldDefinitionTemplateProvider extends AbstractFormElementProvider<FieldDefinition> {

	private static final DisplayDimension WIDTH = dim(400, DisplayUnit.PIXEL);

	private static final DisplayDimension HEIGHT = dim(350, DisplayUnit.PIXEL);

	private static final boolean IS_TOOL = false;

	private ImageProvider _imageProvider;

	private FormMember _member;

	/**
	 * Create a new {@link FieldDefinitionTemplateProvider} for a given {@link FieldDefinition} in a
	 * given {@link InstantiationContext}.
	 */
	public FieldDefinitionTemplateProvider(InstantiationContext context, FieldDefinition config) {
		super(context, config);
	}

	private TLStructuredTypePart attribute(FormEditorContext context) {
		TLStructuredType type = context.getConcreteType();

		if (type == null) {
			type = context.getFormType();
		}

		return getPart(type);
	}

	private void createImageProvider(TLTypePart attribute) {
		_imageProvider = new AttributeImageProvider(attribute);
	}

	/**
	 * The {@link TLStructuredTypePart} of the given {@link TLStructuredType} represented by
	 * {@link #getConfig()}.
	 * 
	 * @param type
	 *        The base type to get part from. Must be a type which has a part with the configured
	 *        name.
	 * 
	 * @see FieldDefinition#getAttribute()
	 * @see #getConfig()
	 * @see #createDisplayTemplate(FormEditorContext)
	 */
	public TLStructuredTypePart getPart(TLStructuredType type) {
		return type.getPart(getConfig().getAttribute());
	}

	/**
	 * The {@link FormMember} for the {@link TLStructuredTypePart}.
	 */
	public FormMember getMember() {
		return _member;
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		if (getMember() != null) {
			return getMember().get(AbstractMember.RENDER_WHOLE_LINE);
		} else {
			TLStructuredTypePart part = modelType.getPart(getConfig().getAttribute());
			if (part == null) {
				return false;
			}
			EditContext editContext = new LocalAnnotationsEditContext(part, getConfig());
			return AttributeOperations.renderWholeLine(part, editContext);
		}
	}

	@Override
	public boolean getIsTool() {
		return IS_TOOL;
	}

	@Override
	public ImageProvider getImageProvider() {
		return _imageProvider;
	}

	@Override
	public ResKey getLabel(FormEditorContext context) {
		TLStructuredTypePart part = attribute(context);
		if (part != null) {
			return TLTypePartResourceProvider.labelKey(part);
		} else if (getMember() != null) {
			return ResKey.text(getMember().getName());
		} else {
			return ResKey.NONE;
		}
	}

	@Override
	public String getName() {
		return getConfig().getAttribute();
	}

	@Override
	public HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		TLStructuredType type = context.getFormType();
		TLStructuredTypePart part = attribute(context);
		if (part != null) {
			createImageProvider(part);
			FieldDefinition fieldDefinition = getConfig();
			FormVisibility visibility = calculateVisibility(part, fieldDefinition.getVisibility(), context.getFormMode());
			if(visibility != null) {
				FormMember member = addMember(context.getFormContext(), context.getContentGroup(), context.getModel(),
					type, part, context.getDomain(), visibility, fieldDefinition);
				if (member != null) {
					HTMLTemplateFragment result =
						createFieldTemplate(member, part, AttributeFormFactory.getAttributeUpdate(member),
							context.getLabelPlacement());
					_member = member;
					return result;
				}
			}
				
			return contentBox(Templates.empty());
		} else {
			return noSuchAttributeError(type, getConfig().getAttribute());
		}
	}

	/**
	 * {@link HTMLTemplateFragment} to render an error message that the attribute with the given
	 * name does not exist in the given owner.
	 * 
	 * @see #noSuchAttributeErrorKey(TLStructuredType, String)
	 */
	public static HTMLTemplateFragment noSuchAttributeError(TLStructuredType type, String attribute) {
		ResKey errorMessage = noSuchAttributeErrorKey(type, attribute);

		HTMLFragment error = ErrorFragmentGenerator.errorFragment(HTMLConstants.DIV, errorMessage, null);
		return contentBox(htmlTemplate(error));
	}

	private HTMLFragment noSuchAttributeErrorFragment(TLStructuredType type, String attribute) {
		ResKey errorMessage = noSuchAttributeErrorKey(type, attribute);

		HTMLFragment error = ErrorFragmentGenerator.errorFragment(HTMLConstants.DIV, errorMessage, null);
		return error;
	}

	/**
	 * {@link ResKey} that states that the attribute with the given name does not exist in the given
	 * owner.
	 */
	public static ResKey noSuchAttributeErrorKey(TLStructuredType owner, String attribute) {
		String type = TLModelUtil.qualifiedName(owner);
		return I18NConstants.NO_SUCH_ATTRIBUTE__TYPE__ATTRIBUTE.fill(type, attribute);
	}

	static FormMember addMember(FormContext formContext, FormContainer contentGroup, TLObject model,
			TLStructuredType type, TLStructuredTypePart part, String domain, FormVisibility visibility,
			AnnotationLookup annotations) {
		AttributeFormContext attributeContext = (AttributeFormContext) formContext;
		FormMember member = createFormMember(attributeContext, contentGroup, type, part, model, domain, annotations);

		// Note: It makes no sense to adjust edit mode of fields for historic objects.
		if (member != null && visibility != null
			&& (model == null || model.tHistoryContext() == Revision.CURRENT_REV)) {

			visibility.applyTo(member);

			switch (visibility) {
				case EDITABLE:
				case MANDATORY: {
					setDisabled(attributeContext, type, part, model, false);
					break;
				}
				case READ_ONLY: {
					setDisabled(attributeContext, type, part, model, true);
					break;
				}
				case DEFAULT:
					// No change.
					break;
			}
		}
		return member;
	}

	private static void setDisabled(AttributeFormContext attributeContext, TLStructuredType type,
			TLStructuredTypePart part, TLObject model, boolean disabled) {
		TLFormObject overlay =
			model == null ? attributeContext.createObject(type, null) : attributeContext.editObject(model);
		AttributeUpdate update = overlay.getUpdate(part);
		if (update != null) {
			update.setDisabled(disabled);
		}
	}

	static HTMLTemplateFragment createFieldTemplate(FormMember member, TLStructuredTypePart part,
			AttributeUpdate update, LabelPlacement labelPlacement) {
		LabelPosition labelPosition = AttributeOperations.labelPosition(part, update);
		switch (labelPosition) {
			case DEFAULT:
				return fieldBox(member.getName(), labelPlacement);
			case AFTER_VALUE:
				return fieldBoxInputFirst(member.getName());
		}
		throw LabelPosition.noSuchPosition(labelPosition);
	}

	private static FormMember createFormMember(AttributeFormContext formContext, FormContainer contentGroup,
			TLStructuredType type, TLStructuredTypePart part, TLObject model, String domain,
			AnnotationLookup annotations) {
		if (model != null) {
			return FormEditorUtil.addAnotherMetaAttribute(formContext, contentGroup, part, model, false, annotations);
		} else {
			return FormEditorUtil.addAnotherMetaAttribute(formContext, contentGroup, type, part, domain, annotations);
		}
	}

	static FormVisibility calculateVisibility(TLStructuredTypePart part, FormVisibility formVisibility,
			FormMode formMode) throws UnreachableAssertion {
		if (formVisibility != FormVisibility.DEFAULT || part == null) {
			return formVisibility;
		}
		if (formMode == FormMode.DESIGN) {
			return FormVisibility.DEFAULT;
		}
		boolean isMandatoryModel = part.isMandatory();
		if (formMode == FormMode.CREATE) {
			return createFormVisibility(part, isMandatoryModel);
		} else {
			return editFormVisibility(part, isMandatoryModel);
		}
	}

	private static FormVisibility createFormVisibility(TLStructuredTypePart part, boolean isMandatoryModel) {
		TLCreateVisibility createVisibility = DisplayAnnotations.getCreateVisibilityAnnotation(part);
		if (createVisibility != null) {
			return formVisiblity(createVisibility.getValue(), isMandatoryModel);
		} else {
			return editFormVisibility(part, isMandatoryModel);
		}
	}

	private static FormVisibility editFormVisibility(TLStructuredTypePart part, boolean isMandatoryModel) {
		TLVisibility visibility = DisplayAnnotations.getVisibilityAnnotation(part);
		if (visibility != null) {
			return formVisiblity(visibility.getValue(), isMandatoryModel);
		} else {
			if (isMandatoryModel) {
				return FormVisibility.MANDATORY;
			} else {
				return FormVisibility.DEFAULT;
			}
		}
	}

	private static FormVisibility formVisiblity(Visibility annotatedVisibility, boolean isMandatoryModel) {
		switch (annotatedVisibility) {
			case EDITABLE:
				if (isMandatoryModel) {
					return FormVisibility.MANDATORY;
				} else {
					return FormVisibility.EDITABLE;
				}
			case HIDDEN:
				return null;
			case READ_ONLY:
				return FormVisibility.READ_ONLY;
			default:
				throw new UnreachableAssertion("Unexpected visibility " + annotatedVisibility);
		}
	}

	@Override
	public boolean isVisible(TLStructuredType type, FormMode formMode) {
		return calculateVisibility(getPart(type), getConfig().getVisibility(), formMode) != null;
	}

	@Override
	public DisplayDimension getDialogWidth() {
		return WIDTH;
	}

	@Override
	public DisplayDimension getDialogHeight() {
		return HEIGHT;
	}

	@Override
	public void renderPDFExport(DisplayContext context, TagWriter out, FormEditorContext renderContext)
			throws IOException {
		TLStructuredTypePart part = attribute(renderContext);
		if (part == null) {
			noSuchAttributeErrorFragment(renderContext.getFormType(), getConfig().getAttribute()).write(context, out);
			return;
		}

		HTMLUtil.beginDiv(out, "field");
		LabelPosition labelPosition = AttributeOperations.labelPosition(part);
		switch (labelPosition) {
			case DEFAULT:
				HTMLUtil.beginDiv(out, "label");
				renderLabel(out, part, true);
				HTMLUtil.endDiv(out);

				HTMLUtil.beginDiv(out, "value");
				renderValue(context, out, renderContext.getModel(), part);
				HTMLUtil.endDiv(out);
				break;
			case AFTER_VALUE:
				HTMLUtil.beginSpan(out, "value");
				renderValue(context, out, renderContext.getModel(), part);
				HTMLUtil.endSpan(out);

				HTMLUtil.beginSpan(out, "label");
				renderLabel(out, part, false);
				HTMLUtil.endSpan(out);
				break;
		}
		HTMLUtil.endDiv(out);
	}

	private void renderValue(DisplayContext context, TagWriter out, TLObject model, TLStructuredTypePart part)
			throws IOException {
		Object value = model.tValue(part);
		if (!part.isMultiple()) {
			writeValue(context, out, model, part, value);
		} else {
			ReferencePresentation presentation =
				AttributeOperations.getPresentation(SimpleEditContext.createContext(model, part));
			switch (presentation) {
				case DROP_DOWN:
				case POP_UP:
				case RADIO:
				case RADIO_INLINE:
					writeValue(context, out, model, part, value);
					break;
				case TABLE:
					if (value instanceof Collection<?>) {
						List<?> rows = CollectionUtil.toList((Collection<?>) value);
						TableConfigurationProvider table = tableDefinition(model, part);
						new SimplePDFTableFragment(table, rows).write(context, out);
					} else {
						writeValue(context, out, model, part, value);
					}
					break;
				default:
					throw new IllegalArgumentException(
						"No such " + ReferencePresentation.class.getName() + ": " + presentation);
			}
		}
	}

	private void writeValue(DisplayContext context, TagWriter out, TLObject contextObject, TLStructuredTypePart part, Object value) throws IOException {
		PDFRendererAnnotation rendererAnnotation = part.getAnnotation(PDFRendererAnnotation.class);
		PDFRenderer renderer;
		if (rendererAnnotation != null) {
			renderer = TypedConfigUtil.createInstance(rendererAnnotation.getImpl());
		} else {
			renderer = LabelProviderService.getInstance().getPDFRenderer(value);
		}
		renderer.write(context, out, contextObject, value);
	}

	private TableConfigurationProvider tableDefinition(TLObject model, TLStructuredTypePart part) {
		TableConfigurationProvider tableConfigurationProvider =
			AbstractWrapperFieldProvider.getTableConfigurationProvider(SimpleEditContext.createContext(model, part));
		if (tableConfigurationProvider == DefaultTableConfigurationProvider.INSTANCE) {
			FieldProvider fieldProvider = AttributeOperations.getFieldProvider(part);
			if (fieldProvider instanceof TableSupportingComplexFieldProvider) {
				TableConfig table = ((TableSupportingComplexFieldProvider) fieldProvider).getConfig().getTable();
				if (table != null) {
					tableConfigurationProvider = TableConfigurationFactory
						.toProvider(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY, table);
				}
			}
		}
		List<String> exportColumns = TLAnnotations.getExportColumns(part);
		return TableConfigurationFactory.combine(
			tableConfigurationProvider,
			exportColumns != null ? TableConfigurationFactory.setDefaultColumns(exportColumns)
				: TableConfigurationFactory.emptyProvider(),
			SimplePDFTableFragment.removeNonExportColumns());
	}

	private void renderLabel(TagWriter out, TLStructuredTypePart part, boolean useColon) {
		out.writeText(MetaLabelProvider.INSTANCE.getLabel(part));
		if (useColon) {
			out.writeText(LabelControl.COLON);
		}
	}

	private static class LocalAnnotationsEditContext extends SimpleEditContext {

		private AnnotationLookup _fieldAnnotations;

		LocalAnnotationsEditContext(TLStructuredTypePart attribute, AnnotationLookup localAnnotations) {
			super(attribute);
			_fieldAnnotations = localAnnotations;
		}

		@Override
		public <T extends TLAnnotation> T getAnnotation(Class<T> annotationType) {
			T localAnnotation = _fieldAnnotations.getAnnotation(annotationType);
			if (localAnnotation != null) {
				return localAnnotation;
			}
			return super.getAnnotation(annotationType);
		}
	}

}