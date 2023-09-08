/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config.annotation;

import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.meta.schema.HolderType;
import com.top_logic.model.annotate.TLAnnotation;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLAnnotation} referencing a scope in which the attribute target type is declared.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("create-scope")
public interface ScopeRef extends TLAttributeAnnotation, TLTypeAnnotation {

	/** @see #getScopeRef() */
	String SCOPE_REF = "scope-ref";

	/** @see #getCreateType() */
	String CREATE_TYPE = "create-type";

	/**
	 * The scope reference.
	 * 
	 * @see HolderType#findScope(Object, String)
	 */
	@Name(SCOPE_REF)
	@StringDefault(HolderType.GLOBAL)
	String getScopeRef();

	/** @see #getScopeRef() */
	void setScopeRef(String value);

	/**
	 * The name of the concrete type to instantiate, when creating new elements.
	 */
	@Name(CREATE_TYPE)
	@Mandatory
	String getCreateType();

	/** @see #getCreateType() */
	void setCreateType(String name);

}
