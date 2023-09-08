/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob.schema.config;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.func.Function2;
import com.top_logic.basic.sql.SQLH;
import com.top_logic.dob.sql.DBAttribute;
import com.top_logic.dob.sql.DBIndex;
import com.top_logic.dob.sql.DBTableMetaObject;
import com.top_logic.dob.xml.DOXMLConstants;

/**
 * {@link ConfigurationItem} that describes an element that has a database representation.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Abstract
public interface DBConfiguration extends ConfigurationItem {

	/**
	 * Name of the represented part in the database, e.g. name of the table or the column.
	 * 
	 * @see DBAttribute#getDBName()
	 * @see DBTableMetaObject#getDBName()
	 * @see #getDBNameEffective()
	 * @see DBNameEffective
	 */
	@Name(DOXMLConstants.DB_NAME_ATTRIBUTE)
	@Nullable
	String getDBName();

	/**
	 * The effective name to use for in the DB.
	 * 
	 * <p>
	 * The information is derived from an optional explicit setting in {@link #getDBName()} an a
	 * logical application name of the element.
	 * </p>
	 */
	@Name("db-name-effective")
	@Nullable
	@Abstract
	String getDBNameEffective();

	/**
	 * Compression of the represented database part.
	 * 
	 * @see DBIndex#getCompress()
	 * @see DBTableMetaObject#getCompress()
	 */
	@Name(DOXMLConstants.DB_COMPRESS_ATTRIBUTE)
	@IntDefault(DOXMLConstants.NO_DB_COMPRESS)
	int getDBCompress();

	/**
	 * Function computing the effective DB name depending on it's application name and its db-name
	 * annotation.
	 * 
	 * @see PrimitiveAttributeConfig#getAttributeName()
	 * @see PrimitiveAttributeConfig#getDBName()
	 * @see MetaObjectConfig.TableDBNameEffective
	 */
	class DBNameEffective extends Function2<String, String, String> {
		@Override
		public String apply(String name, String dbName) {
			if (!StringServices.isEmpty(dbName)) {
				return dbName;
			}
			if (StringServices.isEmpty(name)) {
				return null;
			}
			return SQLH.mangleDBName(name);
		}
	}

}

