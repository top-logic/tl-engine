/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.dob.DataObject;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.storage.CacheValueFactory;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.internal.PersistentModelPart;
import com.top_logic.model.internal.PersistentModelPart.AnnotationConfigs;

/**
 * {@link CacheValueFactory} creating the cache value for a {@link TLAnnotation}.
 * 
 * <p>
 * Implementations are expected to {@link #getAnnotation(DataObject, Object[], Class) retrieve a
 * certain annotation} and compute/instantiate a value/provider in an implementation of
 * {@link #getCacheValue(MOAttribute, DataObject, Object[])}.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
abstract class AnnotationsBasedCacheValueFactory implements CacheValueFactory {

	/**
	 * Lookup of the {@link TLAnnotation} of the given annotation class from the
	 * {@link AnnotationConfigs} stored in the {@link PersistentModelPart#ANNOTATIONS_MO_ATTRIBUTE
	 * annotations attribute} of the accessed object.
	 *
	 * @param <T>
	 *        The annotation type.
	 * @param item
	 *        The accessed object.
	 * @param storage
	 *        The objects storage values.
	 * @param annotation
	 *        The annotation to retrieve.
	 * @return The annotation of the given annotation type of the given object, or <code>null</code>
	 *         if the given object has no such annotation.
	 */
	protected <T extends TLAnnotation> T getAnnotation(DataObject item, Object[] storage, Class<T> annotation) {
		MOClass metaObject = (MOClass) item.tTable();
		MOAttribute annotationsAttribute = metaObject.getAttributeOrNull(PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE);
		if (annotationsAttribute == null) {
			throw new KnowledgeBaseRuntimeException(
				"Item " + item + " does not have the attribute " + PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE);
		}
		Object annotations = annotationsAttribute.getStorage().getCacheValue(annotationsAttribute, item, storage);
		if (annotations == null) {
			return null;
		}
		PersistentModelPart.AnnotationConfigs annotationConfig = (AnnotationConfigs) annotations;
		return annotationConfig.getAnnotation(annotation);
	}

	@Override
	public boolean preserveCacheValue(MOAttribute cacheAttribute, DataObject changedObject,
			Object[] storage, MOAttribute changedAttribute) {
		// depends just on annotations.
		return !PersistentModelPart.ANNOTATIONS_MO_ATTRIBUTE.equals(changedAttribute.getName());
	}

}

