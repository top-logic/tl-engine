/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.annotate;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLModule;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * Annotation to realize backwards-compatible qualified names for {@link TLClass}es for references
 * into the configuration.
 * 
 * <p>
 * Before {@link TLModule} was introduced, abstract types formerly known as meta elements had by
 * convention pseudo-qualified names as their type name. After transformation into a TL6 model,
 * these qualified names must be kept to avoid a huge migration. Even if those types are now part of
 * {@link TLModule}s, they keep their configuration-relevant qualified name using an explicit
 * annotation created by the model transformation.
 * </p>
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @deprecated No longer in use. This class is kept for compatibility with existing models.
 */
@Deprecated
@TagName("legacy-qualified-name")
public interface LegacyQualifiedNameForCompatibility extends TLTypeAnnotation {

	/**
	 * The explicitly set qualified name of a type.
	 */
	@Mandatory
	@Deprecated
	String getQualifiedName();

	/**
	 * @see #getQualifiedName()
	 */
	@Deprecated
	void setQualifiedName(String value);

}
