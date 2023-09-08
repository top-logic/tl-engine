/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.db.model;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.Ref;
import com.top_logic.basic.config.annotation.defaults.NullDefault;
import com.top_logic.basic.func.Function2;

/**
 * {@link DBSchemaPart} with a database name.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface DBNamedPart extends DBSchemaPart {

	/**
	 * See {@link #getName()}.
	 */
	String NAME = "name";

	/**
	 * See {@link #getDBName()}.
	 */
	String DERIVED_DB_NAME = "derived-db-name";

	/**
	 * See {@link #getExplicitDBName()}.
	 */
	String EXPLICIT_DB_NAME = "dbname";

	/**
	 * See {@link #getComment()}.
	 */
	String COMMENT = "comment";

	/**
	 * The name of this part.
	 */
	@Name(NAME)
	@Mandatory
	@Nullable
	String getName();

	/**
	 * Sets the {@link #getName()} property.
	 */
	void setName(String value);
	
	/**
	 * The database internal name of this part.
	 */
	@Name(DERIVED_DB_NAME)
	@Derived(fun = DBName.class, args = { @Ref(EXPLICIT_DB_NAME), @Ref(NAME) })
	String getDBName();
	
	/**
	 * The database internal name of this part.
	 */
	@Name(EXPLICIT_DB_NAME)
	@NullDefault
	String getExplicitDBName();

	/**
	 * Sets the {@link #getDBName()} property.
	 */
	void setExplicitDBName(String value);

	/**
	 * The table comment.
	 */
	@Name(COMMENT)
	@NullDefault
	String getComment();

	/**
	 * Sets the {@link #getComment()} property.
	 */
	void setComment(String value);

	/**
	 * Implementation of {@link DBNamedPart#getDBName()}.
	 */
	class DBName extends Function2<String, String, String> {
		@Override
		public String apply(String value, String defaultValue) {
			if (value != null) {
				return value;
			}
			return defaultValue;
		}

	}

}
