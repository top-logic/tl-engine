/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.schema.setup.config;


import java.util.Set;

import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.meta.MOAnnotation;

/**
 * Configuration of the key attributes of a {@link MetaObject}.
 * 
 * <p>
 * A key attributes is present in each event describing a change of an object of the annotated type
 * regardless whether it's value has changed or not. Marking an attribute as key attribute is
 * useful, if listeners of the annotated type require the value of such attribute for processing the
 * change.
 * </p>
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("key-attributes")
public interface KeyAttributes extends MOAnnotation {

	/** Configuration name of {@link #getAttributes()}. */
	String ATTRIBUTES = "attributes";

	/**
	 * No longer used: The type for which the key attributes are defined.
	 * 
	 * @deprecated See {@link SchemaConfiguration#getKeyAttributes()}.
	 */
	@Deprecated
	@Hidden
	@Name("type")
	String getType();

	/**
	 * All key attributes annotated type.
	 */
	@Format(CommaSeparatedStringSet.class)
	@Name(ATTRIBUTES)
	Set<String> getAttributes();

	/**
	 * Setter for {@link #getAttributes()}.
	 */
	void setAttributes(Set<String> attributes);

}
