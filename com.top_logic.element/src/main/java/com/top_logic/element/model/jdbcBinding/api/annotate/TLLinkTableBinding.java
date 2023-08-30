/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.element.model.jdbcBinding.api.annotate;

import java.util.List;

import com.top_logic.basic.annotation.InApp;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Nullable;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.model.annotate.TLAttributeAnnotation;

/**
 * {@link TLAttributeAnnotation} for a reference whose values should be imported form a separate
 * link table with two foreign keys.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@DisplayOrder({
	TLLinkTableBinding.LINK_TABLE,
	TLLinkTableBinding.SOURCE_COLUMNS,
	TLLinkTableBinding.DESTINATION_COLUMNS,
})
@InApp
@TagName("link-table-binding")
public interface TLLinkTableBinding extends TLAttributeAnnotation {

	/**
	 * @see #getLinkTable()
	 */
	final String LINK_TABLE = "link-table";

	/**
	 * @see #getSourceColumns()
	 */
	final String SOURCE_COLUMNS = "source-columns";

	/**
	 * @see #getDestinationColumns()
	 */
	final String DESTINATION_COLUMNS = "destination-columns";

	/**
	 * The name of the link table.
	 */
	@Name(LINK_TABLE)
	@Mandatory
	String getLinkTable();

	/**
	 * @see #getLinkTable()
	 */
	void setLinkTable(String value);

	/**
	 * Specifies the foreign key columns of the link table that reference the source object defining
	 * the annotated reference.
	 */
	@Name(SOURCE_COLUMNS)
	@Format(CommaSeparatedStrings.class)
	List<String> getSourceColumns();

	/**
	 * @see #getSourceColumns()
	 */
	void setSourceColumns(List<String> value);

	/**
	 * Specifies the foreign key columns of the link table that reference the values of the
	 * reference.
	 */
	@Name(DESTINATION_COLUMNS)
	@Format(CommaSeparatedStrings.class)
	List<String> getDestinationColumns();

	/**
	 * @see #getDestinationColumns()
	 */
	void setDestinationColumns(List<String> value);

	/**
	 * The optional order attribute to create an ordered reference.
	 */
	@Nullable
	String getOrderColumn();

	/**
	 * @see #getOrderColumn()
	 */
	void setOrderColumn(String value);

}
