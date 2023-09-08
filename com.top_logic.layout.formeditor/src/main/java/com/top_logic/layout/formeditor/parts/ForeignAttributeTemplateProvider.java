/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.formeditor.parts;

import static com.top_logic.layout.form.template.model.Templates.*;

import java.io.IOException;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.misc.TypedConfigUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.element.layout.formeditor.definition.FieldDefinition;
import com.top_logic.element.layout.formeditor.implementation.FieldDefinitionTemplateProvider;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.html.template.HTMLTemplateFragment;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.DisplayDimension;
import com.top_logic.layout.DisplayUnit;
import com.top_logic.layout.ImageProvider;
import com.top_logic.layout.editor.config.OptionalTypeTemplateParameters;
import com.top_logic.layout.form.control.Icons;
import com.top_logic.layout.form.template.model.Templates;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.form.definition.AttributeDefinition;
import com.top_logic.model.form.implementation.AbstractFormElementProvider;
import com.top_logic.model.form.implementation.FormEditorContext;
import com.top_logic.model.form.implementation.FormMode;
import com.top_logic.model.resources.TLTypePartResourceProvider;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link AbstractFormElementProvider} for {@link ForeignAttributeDefinition}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ForeignAttributeTemplateProvider extends AbstractFormElementProvider<ForeignAttributeDefinition> {

	private static final String NO_BASE_OBJECT_CSS = "foreignAttributeNoBaseObjectMessage";

	private static final ImageProvider IMAGE_PROVIDER =
		ImageProvider.constantImageProvider(Icons.FORM_EDITOR__REFERENCE);

	private final FieldDefinitionTemplateProvider _delegate;

	private final QueryExecutor _foreignObjectQuery;

	private final TLStructuredType _attributeOwnerType;

	/**
	 * Creates a new {@link ForeignAttributeTemplateProvider}.
	 */
	public ForeignAttributeTemplateProvider(InstantiationContext context, ForeignAttributeDefinition config) {
		super(context, config);

		String attributeName = config.getName();
		if (!StringServices.isEmpty(attributeName)) {
			FieldDefinition fieldDefinition = TypedConfiguration.newConfigItem(FieldDefinition.class);
			fieldDefinition.setImplementationClass(FieldDefinitionTemplateProvider.class);
			fieldDefinition.setAttribute(attributeName);
			TLStructuredTypePart typePart = resolvePart(config);
			fieldDefinition.setTypeSpec(TLModelUtil.qualifiedName(typePart.getType()));
			fieldDefinition.setFullQualifiedName(TLModelUtil.qualifiedName(typePart));
			fieldDefinition.setVisibility(config.getVisibility());
			_delegate = TypedConfigUtil.createInstance(fieldDefinition);
			_foreignObjectQuery = QueryExecutor.compile(getConfig().getItem());
			_attributeOwnerType = resolveAttributeOwner(context, config);
		} else {
			_delegate = null;
			_foreignObjectQuery = null;
			_attributeOwnerType = null;
		}
	}

	private TLClass resolveAttributeOwner(InstantiationContext context, ForeignAttributeDefinition config) {
		TLClass attributeOwnerType;
		try {
			attributeOwnerType = config.getOwner().resolveClass();
		} catch (ConfigurationException ex) {
			context.error("Unable to resolve attribute owner type " + config.getOwner(), ex);
			attributeOwnerType = null;
		}
		return attributeOwnerType;
	}

	private TLStructuredTypePart resolvePart(ForeignAttributeDefinition config) {
		try {
			return AttributeDefinition.resolvePart(config);
		} catch (ConfigurationException exception) {
			throw new TopLogicException(com.top_logic.layout.editor.I18NConstants.MODEL_TYPE_NOT_RESOLVED_ERROR, exception);
		}
	}

	@Override
	public boolean getWholeLine(TLStructuredType modelType) {
		if (_delegate != null) {
			return _delegate.getWholeLine(_attributeOwnerType);
		} else {
			return false;
		}
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
	public DisplayDimension getDialogHeight() {
		return DisplayDimension.dim(500, DisplayUnit.PIXEL);
	}

	@Override
	public ResKey getLabel(FormEditorContext context) {
		return I18NConstants.FOREIGN_ATTRIBUTE_LABEL;
	}

	@Override
	public HTMLTemplateFragment createDisplayTemplate(FormEditorContext context) {
		if (_delegate != null) {
			TLObject foreignItem = getForeignObject(context.getModel());
			TLClass formType = OptionalTypeTemplateParameters.resolve(getConfig());
			HTMLTemplateFragment template;
			if (foreignItem != null) {
				context = new FormEditorContext.Builder(context)
					.formType(formType)
					.concreteType(foreignItem.tType())
					.model(foreignItem)
					.build();
				template = _delegate.createDisplayTemplate(context);
			} else if (context.getFormMode() == FormMode.DESIGN) {
				/* In Design mode the default input field should be displayed. As an attribute of an
				 * "different" object is displayed, a domain must be set. */
				context = new FormEditorContext.Builder(context)
					.formType(formType)
					.concreteType(null)
					.model(null)
					.domain(StringServices.randomUUID())
					.build();
				template = _delegate.createDisplayTemplate(context);
			} else {
				/* There is no item to store changed value to. Therefore the user must not try to
				 * set a value. */
				template = handleNoBaseObject(formType);
			}
			return template;
		} else {
			return contentBox(
				div(css("rf_inputCellOneLine rf_emptyCell"), resource(I18NConstants.FOREIGN_ATTRIBUTE_LABEL)),
				getWholeLine(context.getFormType()));
		}
	}

	private TLObject getForeignObject(TLObject baseModel) {
		if (baseModel == null) {
			return null;
		}
		return (TLObject) _foreignObjectQuery.execute(baseModel);
	}

	private HTMLTemplateFragment handleNoBaseObject(TLStructuredType contextType) {
		TLStructuredTypePart part = _delegate.getPart(contextType);
		if (part == null ) {
			return FieldDefinitionTemplateProvider.noSuchAttributeError(contextType, getConfig().getName());
		} else {
			return handleNoBaseObject(part);
		}
	}

	private HTMLTemplateFragment handleNoBaseObject(TLStructuredTypePart part) {
		boolean renderLabelFirst = !AttributeOperations.renderInputBeforeLabel(part);
		Templates.resource(TLTypePartResourceProvider.labelKey(part));
		HTMLTemplateFragment label;
		if (renderLabelFirst) {
			label = text(MetaLabelProvider.INSTANCE.getLabel(part) + ':');
		} else {
			label = text(MetaLabelProvider.INSTANCE.getLabel(part));
		}
		HTMLTemplateFragment content = span(css(NO_BASE_OBJECT_CSS),
			resource(I18NConstants.FOREIGN_ATTRIBUTE_DISABLED_NO_MODEL__ATTRIBUTE.fill(part)));
		return descriptionBox(label, content, renderLabelFirst);
	}

	@Override
	public void renderPDFExport(DisplayContext context, TagWriter out, FormEditorContext renderContext) throws IOException {
		if (_delegate == null) {
			return;
		}
		TLObject foreignItem = getForeignObject(renderContext.getModel());
		if (foreignItem == null) {
			return;
		}
		TLClass formType = OptionalTypeTemplateParameters.resolve(getConfig());
		renderContext = new FormEditorContext.Builder(renderContext)
			.concreteType(foreignItem.tType())
			.model(foreignItem)
			.formType(formType)
			.build();
		_delegate.renderPDFExport(context, out, renderContext);
	}

}

