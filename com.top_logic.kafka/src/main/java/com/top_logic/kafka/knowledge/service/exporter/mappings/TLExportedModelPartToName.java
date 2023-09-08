/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service.exporter.mappings;

import java.util.function.Function;

import com.top_logic.dob.identifier.ObjectKey;
import com.top_logic.kafka.knowledge.service.TLExported;
import com.top_logic.kafka.knowledge.service.importer.mappings.TLImportedModelPartFromName;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;

/**
 * Function transforming the given {@link TLModelPart} to its qualified name.
 * <p>
 * This is used for {@link TLExported#getValueMapping()}.
 * </p>
 * 
 * @see TLImportedModelPartFromName
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLExportedModelPartToName implements Function<Object, String> {

	/** Returns the {@link TLExportedModelPartToName} instance. */
	public static final TLExportedModelPartToName INSTANCE = new TLExportedModelPartToName();

	@Override
	public String apply(Object object) {
		if (object == null) {
			return null;
		}
		ObjectKey key = (ObjectKey) object;
		TLModelPart modelPart = (TLModelPart) resolve(key);
		return TLModelUtil.qualifiedName(modelPart);
	}

	private TLObject resolve(ObjectKey key) {
		return PersistencyLayer.getKnowledgeBase().resolveObjectKey(key).getWrapper();
	}

}
