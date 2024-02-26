/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.util.model;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.util.ResKey;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.element.meta.kbbased.StorageImplementationFactory;
import com.top_logic.element.model.util.I18NKey;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.util.TLAnnotations;
import com.top_logic.model.export.PreloadContribution;
import com.top_logic.util.model.CompatibilityService;

/**
 * com.top_logic.element variant of {@link CompatibilityService}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ElementCompatibilityService extends CompatibilityService {

	/**
	 * Creates a new {@link ElementCompatibilityService}.
	 */
	public ElementCompatibilityService(InstantiationContext context, ServiceConfiguration<?> config) {
		super(context, config);
	}

	@Override
	public String getTypeName(Wrapper attributed) {
		TLClass metaElement = (TLClass) attributed.tType();
		return MetaElementUtil.getMetaElementType(metaElement);
	}

	@Override
	public boolean isAttributed(Wrapper target) {
		return Wrapper.class.isAssignableFrom(target.getClass());
	}

	@Override
	public ResKey i18nKey(TLModelPart modelPart) {
		return I18NKey.getKey(modelPart);
	}

	@Override
	public PreloadContribution preloadContribution(TLStructuredTypePart part) {
		return AttributeOperations.getStorageImplementation(part).getPreload();
	}

	@Override
	public String getTableFor(TLStructuredType tlClass) {
		return TLAnnotations.getTable(tlClass);
	}

	@Override
	public StorageImplementation createStorage(TLStructuredTypePart part) {
		return StorageImplementationFactory.createStorageImplementation(part);
	}

}
