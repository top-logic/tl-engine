/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.model.jdbcBinding.api.annotate;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.element.model.jdbcBinding.api.ColumnFormat;
import com.top_logic.element.model.jdbcBinding.api.ColumnParser;
import com.top_logic.model.TLProperty;
import com.top_logic.model.annotate.TLAttributeAnnotation;

/**
 * {@link TLAttributeAnnotation} specifying the column from to which the annotated
 * {@link TLProperty} is bound during import/export from/to an external relational schema.
 * 
 * <p>
 * During import, the value of the column is used to set the property. During export, the value of
 * the property is used to update the column value.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("column-binding")
@DisplayOrder({
	TLColumnBinding.NAME,
	TLColumnBinding.PARSER,
	TLColumnBinding.FORMAT,
})
public interface TLColumnBinding extends TLAttributeAnnotation {

	/**
	 * @see #getName()
	 */
	String NAME = "name";

	/**
	 * @see #getParser()
	 */
	String PARSER = "parser";

	/**
	 * @see #getFormat()
	 */
	String FORMAT = "format";

	/**
	 * The name of the column.
	 */
	@Name(NAME)
	@Mandatory
	String getName();

	/**
	 * @see #getName()
	 */
	void setName(String columnName);

	/**
	 * An optional value conversion that maps the column value to an application value during
	 * import.
	 */
	@Name(PARSER)
	PolymorphicConfiguration<? extends ColumnParser> getParser();

	/**
	 * An optional value conversion that maps the property's application value to a column value
	 * during export.
	 */
	@Name(FORMAT)
	PolymorphicConfiguration<? extends ColumnFormat> getFormat();

}
