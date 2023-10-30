/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider.generic;

import com.top_logic.basic.config.ApplicationConfig;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.knowledge.wrap.Document;
import com.top_logic.layout.Accessor;
import com.top_logic.layout.table.model.ColumnConfiguration.DisplayMode;
import com.top_logic.layout.table.provider.ColumnInfo;
import com.top_logic.layout.table.provider.DocumentColumn;
import com.top_logic.layout.table.provider.EnumColumn;
import com.top_logic.layout.table.provider.GenericColumn;
import com.top_logic.layout.table.provider.GenericTableConfigurationProvider;
import com.top_logic.layout.table.provider.PrimitiveColumn;
import com.top_logic.layout.table.provider.ReferenceColumn;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLEnumeration;
import com.top_logic.model.TLModel;
import com.top_logic.model.TLModule;
import com.top_logic.model.TLPrimitive;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.annotate.DisplayAnnotations;
import com.top_logic.model.annotate.ui.TLColumnInfo;
import com.top_logic.model.util.TLModelUtil;
import com.top_logic.model.util.TLTypeContext;
import com.top_logic.util.model.ModelService;

/**
 * Factory class to create {@link ColumnInfo}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ColumnInfoFactory implements ColumnInfoProvider {

	private static final String GALLERY_IMAGE_MODULE = "tl.imagegallery";
	private static final String GALLERY_IMAGE_TYPE = "GalleryImage";

	private TLType _galleryType;

	/**
	 * Creates a new {@link ColumnInfoFactory}.
	 */
	public ColumnInfoFactory() {
		_galleryType = findGalleryType();
	}

	/**
	 * Resolve the gallery type.
	 * 
	 * @return Null, if no gallery type is found.
	 */
	protected TLType findGalleryType() {
		TLModel model = ModelService.getApplicationModel();
		TLModule galleryModule = model.getModule(GALLERY_IMAGE_MODULE);
		if (galleryModule == null) {
			// No gallery module, e.g. in tests
			return null;
		}
		return galleryModule.getType(GALLERY_IMAGE_TYPE);
	}

	/**
	 * Whether the given {@link TLStructuredTypePart} is technically supported in tables.
	 * 
	 * @param typePart
	 *        Is not allowed to be null.
	 */
	protected boolean isTechnicallyNotSupported(TLTypeContext typePart) {
		if (_galleryType == null) {
			return false;
		}
		return typePart.getType().equals(_galleryType);
	}

	@Override
	public ColumnInfo createColumnInfo(TLTypeContext contentTypeContext, ResKey headerI18NKey) {
		TLColumnInfo annotation = contentTypeContext.getAnnotation(TLColumnInfo.class);
		ColumnInfoProvider provider;
		if (annotation != null) {
			InstantiationContext context = ApplicationConfig.getInstance().getServiceStartupContext();
			provider = new ConfiguredColumnInfoProvider(context, annotation);
			return provider.createColumnInfo(contentTypeContext, headerI18NKey);
		}
	
		TLType contentType = contentTypeContext.getType();
		if (contentType instanceof TLPrimitive) {
			TLPrimitive primitiveType = (TLPrimitive) contentType;
	
			return createPrimitiveColumn(contentTypeContext, primitiveType.getKind(), headerI18NKey);
		} else if (contentType instanceof TLClass) {
			if (Document.DOCUMENT_TYPE.equals(TLModelUtil.qualifiedName(contentType))) {
				return createDocumentColumn(contentTypeContext, headerI18NKey);
			} else {
				return createReferenceColumn(contentTypeContext, headerI18NKey);
			}
		} else if (contentType instanceof TLEnumeration) {
			return createEnumColumn(contentTypeContext, headerI18NKey);
		} else {
			StringBuilder error = new StringBuilder();
			error.append("Unsupported column type '");
			error.append(contentType);
			error.append("' with header key '");
			error.append(headerI18NKey);
			error.append("'.");
			throw new UnsupportedOperationException(error.toString());
		}
	}

	/**
	 * Create an {@link EnumColumn}.
	 * @param contentType
	 *        Content type of the column. Must not be <code>null</code>.
	 * 
	 * @return Never null.
	 */
	protected ColumnInfo createEnumColumn(TLTypeContext contentType, ResKey headerI18NKey) {
		return new EnumColumn(contentType, headerI18NKey, visibility(contentType), getDefaultAccessor());
	}

	/**
	 * Create a {@link ReferenceColumn}.
	 * @param contentType
	 *        Content type of the column. Must not be <code>null</code>.
	 * 
	 * @return Never null.
	 */
	protected final ColumnInfo createReferenceColumn(TLTypeContext contentType, ResKey headerI18NKey) {
		return createReferenceColumn(contentType, headerI18NKey, getDefaultAccessor());
	}

	/**
	 * Create a {@link ReferenceColumn}.
	 * 
	 * @param contentType
	 *        Content type of the column. Must not be <code>null</code>.
	 * 
	 * @return Never null.
	 */
	protected ColumnInfo createReferenceColumn(TLTypeContext contentType, ResKey headerI18NKey,
			Accessor<?> accessor) {
		return new ReferenceColumn(contentType, headerI18NKey, visibility(contentType), accessor);
	}

	/**
	 * Computes the {@link DisplayMode visibility} for the given {@link TLTypePart}.
	 * 
	 * @param typePart
	 *        Part to compute visibility for. May be <code>null</code>.
	 * 
	 * @return May be <code>null</code>
	 */
	protected DisplayMode visibility(TLTypeContext typePart) {
		if (typePart == null) {
			return null;
		}
		if (isTechnicallyNotSupported(typePart)) {
			return DisplayMode.excluded;
		} else if (DisplayAnnotations.isHidden(typePart)) {
			return DisplayMode.excluded;
		}
		return null;
	}

	/**
	 * Create a {@link DocumentColumn}.
	 * @param contentType
	 *        Content type of the column. Must not be <code>null</code>.
	 * 
	 * @return Never null.
	 */
	protected ColumnInfo createDocumentColumn(TLTypeContext contentType, ResKey headerI18NKey) {
		return new DocumentColumn(contentType, headerI18NKey, visibility(contentType), getDefaultAccessor());
	}

	/**
	 * Create a {@link PrimitiveColumn}.
	 * 
	 * @param contentType
	 *        Content type of the column. Must not be <code>null</code>.
	 * 
	 * @return Never null.
	 */
	protected ColumnInfo createPrimitiveColumn(TLTypeContext contentType, TLPrimitive.Kind primitiveKind,
			ResKey headerI18NKey) {
		Accessor<?> defaultAccessor = getDefaultAccessor();
		if (contentType.isMultiple()) {
			return new GenericColumn(contentType, headerI18NKey, visibility(contentType), defaultAccessor);
		}
		return new PrimitiveColumn(contentType, primitiveKind, headerI18NKey, visibility(contentType), defaultAccessor);
	}

	/**
	 * The default {@link ColumnInfo#getAccessor()}.
	 * 
	 * @return Is not allowed to be null.
	 */
	protected Accessor<?> getDefaultAccessor() {
		return GenericTableConfigurationProvider.getDefaultAccessor();
	}

}

