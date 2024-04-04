/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.kbbased;

import com.top_logic.element.meta.StorageImplementation;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Base class for {@link TLStructuredTypePart} implementations providing the configuration aspect.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ConfiguredAttributeImpl extends KBBasedMetaAttribute {

	/** See definition of {@link KBBasedMetaAttribute#OBJECT_NAME} in ElementMeta.xml */
	public static final String VALIDITY_CHECK_ATTRIBUTE = "validityCheck";

	private static final String STORAGE_IMPLEMENTATION_ATTRIBUTE = "storageImplementation";

	/**
	 * Creates a {@link ConfiguredAttributeImpl} from persistent storage.
	 */
	public ConfiguredAttributeImpl(KnowledgeObject ko) {
		super(ko);
	}

	@Override
	public final StorageImplementation getStorageImplementation() {
		return (StorageImplementation) tGetData(STORAGE_IMPLEMENTATION_ATTRIBUTE);
	}

	@Override
	public boolean isDerived() {
		StorageImplementation storageImplementation = getStorageImplementation();
		if (storageImplementation == null) {
			return false;
		}
		return storageImplementation.isReadOnly();
	}

}
