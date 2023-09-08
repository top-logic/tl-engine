/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.xml.annotation;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.dob.meta.MOAnnotation;
import com.top_logic.dob.meta.MOClass;
import com.top_logic.knowledge.service.db2.DBAccess;
import com.top_logic.knowledge.service.db2.DBAccessFactory;

/**
 * {@link MOAnnotation} linking a {@link DBAccessFactory} to {@link MOClass}.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@FrameworkInternal
public interface DBAccessFactoryAnnotation extends MOAnnotation {

	/** Name of the property {@link #getDBAccessFactory()} */
	String DB_ACCESS_FACTORY_PROPERTY = "db-access";

	/**
	 * Factory creating the corresponding {@link DBAccess} implementation.
	 */
	@Name(DB_ACCESS_FACTORY_PROPERTY)
	@InstanceFormat
	@Mandatory
	DBAccessFactory getDBAccessFactory();

	/**
	 * @see #getDBAccessFactory()
	 */
	void setDBAccessFactory(DBAccessFactory factory);

}