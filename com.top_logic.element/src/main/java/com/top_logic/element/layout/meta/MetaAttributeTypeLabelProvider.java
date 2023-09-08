/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.element.meta.AttributeSettings;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.ResPrefix;
import com.top_logic.util.Resources;

/**
 * The {@link MetaAttributeTypeLabelProvider} gives the labels for the type of
 * {@link TLStructuredTypePart}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MetaAttributeTypeLabelProvider implements LabelProvider {

	public static final MetaAttributeTypeLabelProvider	INSTANCE	= new MetaAttributeTypeLabelProvider();

	public static final ResPrefix KEY_PREFIX = I18NConstants.ATTRIBUTE_TYPE;

	@Override
	public String getLabel(Object anObject) {
		Resources theResources = Resources.getInstance();
		if (anObject instanceof Integer) {
			int aMetaAttributeType = ((Integer) anObject).intValue();
			String theType = AttributeSettings.getInstance().getTypeAsString(aMetaAttributeType);
			return theResources.getString(KEY_PREFIX.key(theType));
		}
		return anObject.toString();
	}
}
