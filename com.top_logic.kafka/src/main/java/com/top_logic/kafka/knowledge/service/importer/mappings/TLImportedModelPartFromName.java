/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.knowledge.service.importer.mappings;

import java.util.function.Function;

import com.top_logic.kafka.knowledge.service.TLImported;
import com.top_logic.kafka.knowledge.service.exporter.mappings.TLExportedModelPartToName;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.util.TLModelUtil;

/**
 * Function resolving the qualified name of a {@link TLModelPart}.
 * <p>
 * This is used for {@link TLImported#getValueMapping()}.
 * </p>
 * 
 * @see TLExportedModelPartToName
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLImportedModelPartFromName implements Function<Object, TLModelPart> {

	/** Returns the {@link TLImportedModelPartFromName} instance. */
	public static final TLImportedModelPartFromName INSTANCE = new TLImportedModelPartFromName();

	@Override
	public TLModelPart apply(Object object) {
		if (object == null) {
			return null;
		}
		String qualifiedName = (String) object;
		return (TLModelPart) TLModelUtil.resolveQualifiedName(qualifiedName);
	}

}
