/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.model.jdbcBinding.api.annotate;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLReference;
import com.top_logic.model.annotate.TLAttributeAnnotation;

/**
 * {@link TLAttributeAnnotation} for a {@link TLReference} that identifies the target objects by the
 * values in the given columns.
 * 
 * <p>
 * The target objects are found by comparing the values in the given columns from the target table
 * with the {@link TLTableBinding#getPrimaryKey() primary key} column values of the object table.
 * </p>
 * 
 * <p>
 * An ordered reference can be imported by specifying an order column. All objects to be assigned to
 * the reference are then ordered according the values in the specified order column.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@InApp
@TagName("reverse-foreign-key-binding")
public interface TLReverseForeignKeyBinding extends TLAttributeAnnotation {

	/**
	 * The columns whose values are use to identify the target object of the annotated
	 * {@link TLReference}.
	 */
	@Mandatory
	@Format(CommaSeparatedStrings.class)
	List<String> getTargetColumns();

	/**
	 * @see #getTargetColumns()
	 */
	void setTargetColumns(List<String> value);

	/**
	 * Name of a column in the referenced table used for ordering referenced objects.
	 */
	@Nullable
	String getOrderColumn();

	/**
	 * @see #getOrderColumn()
	 */
	void setOrderColumn(String value);

}
