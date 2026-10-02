/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate.persistency;

import com.top_logic.basic.StringServices;
import com.top_logic.dob.MOAttribute;
import com.top_logic.dob.attr.BinaryAttributeKind;
import com.top_logic.dob.ex.NoSuchAttributeException;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.service.BinaryStorageSettings;
import com.top_logic.knowledge.service.DynamicBinaryStoragePolicy;
import com.top_logic.knowledge.service.db2.PersistentObject;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLBinaryStorage;

/**
 * {@link DynamicBinaryStoragePolicy} taking kind, store and threshold from the
 * {@link TLBinaryStorage} annotation of the model attribute.
 *
 * <p>
 * The model attribute is the attribute with the name of the dynamic attribute in the model type of
 * the item. Settings not given in the annotation, and all settings for items without model type or
 * attributes without annotation, are the configured defaults.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BinaryStorageAnnotationPolicy implements DynamicBinaryStoragePolicy {

	/**
	 * Singleton {@link BinaryStorageAnnotationPolicy} instance.
	 */
	public static final BinaryStorageAnnotationPolicy INSTANCE = new BinaryStorageAnnotationPolicy();

	/**
	 * Creates a {@link BinaryStorageAnnotationPolicy}.
	 */
	protected BinaryStorageAnnotationPolicy() {
		// Singleton constructor.
	}

	@Override
	public BinaryStorageSettings forValue(KnowledgeItem item, String attribute, BinaryStorageSettings defaults) {
		TLStructuredTypePart part = modelAttribute(item, attribute);
		if (part == null) {
			return defaults;
		}
		TLBinaryStorage annotation = part.getAnnotation(TLBinaryStorage.class);
		if (annotation == null) {
			return defaults;
		}
		BinaryStorageSettings result = defaults;
		BinaryAttributeKind kind = annotation.getKind();
		if (kind != null) {
			result = result.withKind(kind);
		}
		String store = StringServices.nonEmpty(annotation.getStore());
		if (store != null) {
			result = result.withStoreName(store);
		}
		Long threshold = annotation.getThreshold();
		if (threshold != null) {
			result = result.withThreshold(threshold.longValue());
		}
		return result;
	}

	/**
	 * The model attribute of the given item with the given name.
	 *
	 * <p>
	 * The model type is read from the type reference of the item, not from the model object of the
	 * item, since values are assigned while the item is created.
	 * </p>
	 *
	 * @return The model attribute, <code>null</code> if the item has no model type or the type has
	 *         no attribute with the given name.
	 */
	private static TLStructuredTypePart modelAttribute(KnowledgeItem item, String attribute) {
		MOAttribute typeAttribute = item.tTable().getAttributeOrNull(PersistentObject.TYPE_REF);
		if (typeAttribute == null) {
			return null;
		}
		Object typeRef;
		try {
			typeRef = item.getAttributeValue(PersistentObject.TYPE_REF);
		} catch (NoSuchAttributeException ex) {
			return null;
		}
		if (!(typeRef instanceof KnowledgeItem typeItem)) {
			return null;
		}
		TLObject type = typeItem.getWrapper();
		if (!(type instanceof TLStructuredType structuredType)) {
			return null;
		}
		return structuredType.getPart(attribute);
	}

}
