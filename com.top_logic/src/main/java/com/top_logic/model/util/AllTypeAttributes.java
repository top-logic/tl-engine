/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.func.Function1;
import com.top_logic.layout.editor.I18NConstants;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.util.error.TopLogicException;

/**
 * {@link Function1} returning {@link TLClass#getAllParts() all parts} for one {@link TLClass}.
 * 
 * @author <a href="mailto:sfo@top-logic.com">sfo</a>
 */
public class AllTypeAttributes extends Function1<Collection<? extends TLStructuredTypePart>, TLModelPartRef> {

	/**
	 * The {@link AllTypeAttributes} instance.
	 */
	public static final AllTypeAttributes INSTANCE = new AllTypeAttributes();

	@Override
	public Collection<? extends TLStructuredTypePart> apply(TLModelPartRef typeRef) {
		if (typeRef != null) {
			try {
				TLClass type = typeRef.resolveClass();
				return findAttributes(type);
			} catch (ConfigurationException exception) {
				throw new TopLogicException(I18NConstants.MODEL_TYPE_NOT_RESOLVED_ERROR, exception);
			}
		}

		return Collections.emptyList();
	}

	/**
	 * Determine the attributes of the referenced type to return in {@link #apply(TLModelPartRef)}.
	 *
	 * @param type
	 *        The resolved {@link TLModelPartRef}.
	 * @return Return value of {@link #apply(TLModelPartRef)}.
	 */
	protected List<TLStructuredTypePart> findAttributes(TLClass type) {
		return list(TLModelUtil.getMetaAttributesInHierarchy(type));
	}

}
