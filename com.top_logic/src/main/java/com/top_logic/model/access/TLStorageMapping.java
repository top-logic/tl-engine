/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.access;

import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLProperty;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Annotation that assigns a {@link StorageMapping} algorithm to a {@link TLProperty}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("storage-mapping")
@TargetType(value = {
	TLTypeKind.BINARY, TLTypeKind.BOOLEAN, TLTypeKind.DATE, TLTypeKind.FLOAT, TLTypeKind.INT, TLTypeKind.STRING })
public interface TLStorageMapping extends TLAttributeAnnotation, TLTypeAnnotation {
	
	/**
	 * @see #getImplementation()
	 */
	String IMPLEMENTATION = "implementation";

	/**
	 * The annotated {@link StorageMapping} algorithm.
	 */
	@Name(IMPLEMENTATION)
	@InstanceFormat
	@DefaultContainer
	StorageMapping<?> getImplementation();

	/**
	 * @see #getImplementation()
	 */
	void setImplementation(StorageMapping<?> value);
}
