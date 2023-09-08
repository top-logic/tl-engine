/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.form.definition;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.layout.form.values.edit.annotation.Options;
import com.top_logic.model.TLStructuredType;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.TLType;
import com.top_logic.model.util.AllTypeAttributes;
import com.top_logic.model.util.TLModelPartRef;

/**
 * Specification of a {@link TLStructuredTypePart}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface AttributeDefinition extends NamedConfigMandatory {

	/** Configuration option for value {@link #getOwner()}. */
	String OWNER = "owner";

	/**
	 * Name of the attribute to display.
	 */
	@Override
	@Options(fun = AllTypeAttributes.class, args = @Ref(OWNER), mapping = NamedPartNames.class)
	String getName();

	/**
	 * The name of the type that owns the attribute to resolve.
	 */
	@Name(OWNER)
	@Abstract
	@Hidden
	@Nullable
	TLModelPartRef getOwner();

	/**
	 * Resolves the referenced {@link TLStructuredTypePart} of the given configuration.
	 */
	static TLStructuredTypePart resolvePart(AttributeDefinition config) throws ConfigurationException {
		TLStructuredType ownerClass = resolveOwner(config);
		if (ownerClass == null) {
			return null;
		}

		String name = config.getName();
		if (StringServices.isEmpty(name)) {
			return null;
		}
		return ownerClass.getPart(name);
	}

	/**
	 * Resolves the owner of the referenced {@link TLStructuredTypePart} of the given configuration.
	 */
	static TLStructuredType resolveOwner(AttributeDefinition config) throws ConfigurationException {
		TLModelPartRef ownerTypeRef = config.getOwner();
		if (ownerTypeRef == null) {
			return null;
		}
		TLType ownerType = ownerTypeRef.resolveType();
		if (ownerType instanceof TLStructuredType) {
			return (TLStructuredType) ownerType;
		} else {
			return null;
		}
	}

}

