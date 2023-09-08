/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config.annotation;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.knowledge.wrap.WebFolder;
import com.top_logic.model.annotate.StringAnnotation;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * {@link TLAnnotation} that defines the {@link WebFolder#getFolderType() folder type} of an
 * attribute of type {@link WebFolder}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("folder-type")
@TargetType(value = { TLTypeKind.COMPOSITION }, name = WebFolder.WEB_FOLDER_TYPE)
public interface FolderType extends StringAnnotation, TLAttributeAnnotation {

	/**
	 * @see WebFolder#getFolderType()
	 */
	@Override
	String getValue();

}
