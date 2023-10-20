/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.util;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLReference;
import com.top_logic.model.form.definition.NamedPartNames;

/**
 * Definition of a {@link TLReference}.
 */
@DisplayOrder({
	TLReferenceConfig.OWNER,
	TLReferenceConfig.NAME_ATTRIBUTE
})
public interface TLReferenceConfig extends NamedConfigMandatory {

	/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
	Lookup LOOKUP = MethodHandles.lookup();

	/** Configuration option for value {@link #getOwner()}. */
	String OWNER = "owner";

	/**
	 * Name of the reference.
	 */
	@Override
	@Options(fun = AllReferenceAttributes.class, args = @Ref(OWNER), mapping = NamedPartNames.class)
	String getName();

	/**
	 * The name of the type that owns the reference to resolve.
	 */
	@Name(OWNER)
	@Mandatory
	TLModelPartRef getOwner();

	/**
	 * Determines the configured {@link TLReference}.
	 */
	default TLReference resolve() {
		TLClass owner;
		try {
			owner = getOwner().resolveClass();
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
		return (TLReference) owner.getPartOrFail(getName());
	}

}
