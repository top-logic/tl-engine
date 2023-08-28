/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.model.jdbcBinding.api.annotate;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.element.model.jdbcBinding.api.RowReader;
import com.top_logic.element.model.jdbcBinding.api.RowWriter;
import com.top_logic.element.model.jdbcBinding.api.TypeSelector;
import com.top_logic.model.config.TLTypeAnnotation;

/**
 * {@link TLTypeAnnotation} specifying the table to which the annotated type is bound.
 * 
 * <p>
 * During import, objects of the annotated type are created from rows of the specified table. During
 * export, objects of the annotated type are written to rows of the specified table.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DisplayOrder({
	TLTableBinding.NAME,
	TLTableBinding.PRIMARY_KEY,
	TLTableBinding.TYPE_SELECTOR,
	TLTableBinding.ROW_READER,
	TLTableBinding.ROW_WRITER,
})
@InApp
@TagName("table-binding")
public interface TLTableBinding extends TLTypeAnnotation {

	/**
	 * @see #getName()
	 */
	final String NAME = "name";

	/**
	 * @see #getPrimaryKey()
	 */
	final String PRIMARY_KEY = "primary-key";

	/**
	 * @see #getTypeSelector()
	 */
	final String TYPE_SELECTOR = "type-selector";

	/**
	 * @see #getRowReader()
	 */
	final String ROW_READER = "row-reader";

	/**
	 * @see #getRowWriter()
	 */
	final String ROW_WRITER = "row-writer";

	/**
	 * The name of the table.
	 */
	@Name(NAME)
	@Mandatory
	String getName();

	/**
	 * @see #getName()
	 */
	void setName(String value);

	/**
	 * Specifies the primary key of the table that is used to reference objects through
	 * {@link TLForeignKeyBinding foreign keys}.
	 */
	@Name(PRIMARY_KEY)
	@Format(CommaSeparatedStrings.class)
	List<String> getPrimaryKey();

	/**
	 * @see #getPrimaryKey()
	 */
	void setPrimaryKey(List<String> value);

	/**
	 * Optional type lookup for a polymorphic binding.
	 * 
	 * <p>
	 * The type selector function computes a sub-type of the annotated type that should be used when
	 * importing an object from a certain row.
	 * </p>
	 */
	@Name(TYPE_SELECTOR)
	PolymorphicConfiguration<? extends TypeSelector> getTypeSelector();

	/**
	 * Optional post-processing of an object after read from a table row.
	 */
	@Name(ROW_READER)
	PolymorphicConfiguration<? extends RowReader> getRowReader();

	/**
	 * Optional post-processing of a table row after exporting an object.
	 */
	@Name(ROW_WRITER)
	PolymorphicConfiguration<? extends RowWriter> getRowWriter();

}
