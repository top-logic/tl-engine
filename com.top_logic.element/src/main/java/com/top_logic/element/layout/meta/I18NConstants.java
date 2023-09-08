/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResKey1;
import com.top_logic.basic.util.ResKey2;
import com.top_logic.layout.I18NConstantsBase;
import com.top_logic.layout.ResPrefix;
import com.top_logic.model.TLClass;

/**
 * {@link I18NConstantsBase} for this package
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class I18NConstants extends I18NConstantsBase {

	public static ResPrefix ATTRIBUTE_TYPE = legacyPrefix("element.meta.attribute.typename.");

	/** Currently no {@link TLClass} selected. */
	public static ResKey NO_META_ELEMENT_SELECTED;

	/** Serialized configuration could not be parsed. */
	public static ResKey CONFIGURATION_NOT_PARSEABLE;

	/** Configuration has unexpected configuration interface. */
	public static ResKey2 CONFIGURATION_OF_UNEXPECTED_TYPE__EXPECTED_ACTUAL;

	public static ResKey DELETE_PROTECTED = legacyKey("element.meta.attribute.edit.attRemove.disabled.deleteProtected");

	public static ResKey TOOLTIP = legacyKey("tl.type.tooltip");

	public static ResKey1 I18N_NAME_COLUMN__LOCALE;

	public static ResKey1 ERROR_CREATING_ATTRIBUTE_FAILD__DETAIL;

	public static ResKey2 ERROR_DELETE_TYPE_WITH_SPECIALIZATIONS__TYPE_SPECIALIZATION;

	public static ResKey1 ERROR_DELETE_TYPE_WITH_INSTANCES__TYPE;

	public static ResKey2 ERROR_DELETE_TYPE_USED_BY_TYPEPART__TYPE_TYPEPART;

	public static ResKey FORM_EDITOR__NO_ATTRIBUTED_OBJECT;

	static {
		initConstants(I18NConstants.class);
	}
}
