/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.config;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.layout.form.template.SelectionControlProvider;
import com.top_logic.layout.form.values.edit.annotation.ControlProvider;
import com.top_logic.layout.form.values.edit.annotation.OptionLabels;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.resources.TLPartScopedResourceProvider;
import com.top_logic.model.util.AllTypes;

/**
 * Configuration fragment referencing a type by its qualified name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface TypeRef extends ConfigurationItem {

	/** @see #getTypeSpec() */
	String TYPE_SPEC = "type";

	/**
	 * Name of the target type of this attribute.
	 */
	@Name(TYPE_SPEC)
	@Nullable
	@Options(fun = AllTypes.class, mapping = TLModelPartMapping.class)
	@OptionLabels(TLPartScopedResourceProvider.class)
	@ControlProvider(SelectionControlProvider.class)
	String getTypeSpec();

	/** @see #getTypeSpec() */
	void setTypeSpec(String value);

}
