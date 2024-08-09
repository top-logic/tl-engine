/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.StringDefault;
import com.top_logic.element.meta.schema.HolderType;
import com.top_logic.model.TLClass;

/**
 * Configuration of the super type of the type defined in an {@link ObjectTypeConfig}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ExtendsConfig extends ConfigurationItem {

	/**
	 * Default tag name for {@link ExtendsConfig} items.
	 */
	String TAG_NAME = "generalization";

	/** Property name of {@link #getTypeName()}. */
	String TYPE = "type";

	/** Property name of {@link #getScopeRef()}. */
	String SCOPE_REF = "scope";

	/**
	 * Name of the super type.
	 * 
	 * <p>
	 * When referencing types in another module, the name may be qualified by
	 * <code>[module-name]:[type-name]</code>.
	 * </p>
	 * 
	 * @see #getTypeName()
	 * @see #getModuleName()
	 */
	@Name(TYPE)
	@Mandatory
	String getQualifiedTypeName();

	/** @see #getTypeName() */
	void setQualifiedTypeName(String value);

	/**
	 * The local name part of {@link #getQualifiedTypeName()}
	 */
	@Hidden
	@Derived(fun = LocalName.class, args = @Ref(TYPE))
	String getTypeName();

	/**
	 * The module name part of {@link #getQualifiedTypeName()}.
	 * 
	 * <p>
	 * If {@link #getQualifiedTypeName()} is not qualified, <code>null</code> is returned.
	 * </p>
	 */
	@Derived(fun = ModuleName.class, args = @Ref(TYPE))
	@Nullable
	@Hidden
	String getModuleName();

	/**
	 * The scope in which the referenced type is found.
	 * 
	 * <p>
	 * Valid values are:
	 * </p>
	 * <ul>
	 * <li>{@link HolderType#GLOBAL}</li>
	 * <li>{@link HolderType#THIS}</li>
	 * <li>{@link HolderType#PARENT}</li>
	 * <li>The structure type of the nearest ancestor-or-self element that defines the
	 * {@link TLClass}</li>
	 * </ul>
	 */
	@Name(SCOPE_REF)
	@StringDefault(HolderType.GLOBAL)
	String getScopeRef();

	/** @see #getScopeRef() */
	void setScopeRef(String value);
}