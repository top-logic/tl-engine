/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.model.jdbcBinding.api.annotate;

import java.util.List;

import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLReference;
import com.top_logic.model.annotate.TLAttributeAnnotation;

/**
 * {@link TLAttributeAnnotation} for a {@link TLReference} that identifies the target objects by the
 * values in the given columns.
 * 
 * <p>
 * The target object is found by comparing the values in the given columns with the values of the
 * {@link TLTableBinding#getPrimaryKey() primary key} properties of the target table.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@TagName("foreign-key-binding")
public interface TLForeignKeyBinding extends TLAttributeAnnotation {

	/**
	 * The columns whose values are use to identify the target object of the annotated
	 * {@link TLReference}.
	 */
	@Mandatory
	@Format(CommaSeparatedStrings.class)
	List<String> getColumns();

	/**
	 * @see #getColumns()
	 */
	void setColumns(List<String> value);

}
