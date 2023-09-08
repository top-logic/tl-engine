/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.table.provider.generic;

import com.top_logic.basic.util.ResKey;
import com.top_logic.layout.table.provider.ColumnInfo;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.ui.TLColumnInfo;
import com.top_logic.model.util.TLTypeContext;
import com.top_logic.model.util.TypePartContext;
import com.top_logic.util.model.CompatibilityService;

/**
 * Algorithm creating a {@link ColumnInfo} based on {@link TLStructuredTypePart} and a content type.
 * 
 * <p>
 * A {@link ColumnInfoProvider} is either annotated by the {@link TLColumnInfo} annotation, or
 * configured for in the attribute settings.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ColumnInfoProvider {

	/**
	 * Creates a {@link ColumnInfo} based on the given {@link TLStructuredTypePart}.
	 * 
	 * @param typePart
	 *        Must not be <code>null</code>.
	 */
	default ColumnInfo createColumnInfo(TLStructuredTypePart typePart) {
		return createColumnInfo(new TypePartContext(typePart), CompatibilityService.getInstance().i18nKey(typePart));
	}

	/**
	 * Creates a {@link ColumnInfo} based on a given {@link TLStructuredTypePart} and a content
	 * type.
	 * 
	 * @param contentType
	 *        Content type of the column. Must not be <code>null</code>.
	 * @param headerI18NKey
	 *        {@link ResKey} for the header of the column.
	 */
	ColumnInfo createColumnInfo(TLTypeContext contentType, ResKey headerI18NKey);

}
