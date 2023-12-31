/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.schema;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Nullable;

/**
 * {@link Schema} for a value in the <i>OpenAPI</i> communication.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface Schema extends ConfigurationItem {

	/**
	 * The type of the schema.
	 * 
	 * <p>
	 * The type is a classification of the value.
	 * </p>
	 */
	@Abstract
	String getType();

	/**
	 * Description of the possible value.
	 */
	@Nullable
	String getDescription();

	/**
	 * Setter for {@link #getDescription()}.
	 */
	void setDescription(String value);

	/**
	 * String representation of the default value, that is used when an element of this
	 * {@link Schema} is expected but nothing is given.
	 */
	@Nullable
	String getDefault();

	/**
	 * Setter for {@link #getDefault()}.
	 */
	void setDefault(String value);

	/**
	 * String representation of an potential value that satisfy this schema.
	 */
	@Nullable
	String getExample();

	/**
	 * Setter for {@link #getExample()}.
	 */
	void setExample(String value);

	/**
	 * This schema in serialised form.
	 */
	@Nullable
	String getAsString();

	/**
	 * Setter for {@link #getAsString()}
	 */
	void setAsString(String value);

	/**
	 * Visitor pattern for {@link Schema}.
	 *
	 * @param <R>
	 *        Return type of the visitor.
	 * @param <A>
	 *        Argument type of the visitor.
	 * @param visitor
	 *        The visitor to apply.
	 * @param argument
	 *        Argument for the visitor.
	 * 
	 * @return Return value of the visitor.
	 */
	<R, A> R visit(SchemaVisitor<R, A> visitor, A argument);
}
