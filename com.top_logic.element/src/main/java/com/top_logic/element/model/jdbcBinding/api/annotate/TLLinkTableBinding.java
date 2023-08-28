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
	TLLinkTableBinding.SOURCE_KEY,
	TLLinkTableBinding.DESTINATION_KEY,
})
@InApp
@TagName("link-table-binding")
public interface TLLinkTableBinding extends TLAttributeAnnotation {

	/**
	 * @see #getLinkTable()
	 */
	final String LINK_TABLE = "link-table";

	/**
	 * @see #getSourceKey()
	 */
	final String SOURCE_KEY = "source-key";

	/**
	 * @see #getDestinationKey()
	 */
	final String DESTINATION_KEY = "destination-key";

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
	 * Specifies the foreign key of the link table that references the source object defining the
	 * annotated reference.
	 */
	@Name(SOURCE_KEY)
	@Format(CommaSeparatedStrings.class)
	List<String> getSourceKey();

	/**
	 * @see #getSourceKey()
	 */
	void setSourceKey(List<String> value);

	/**
	 * Specifies the foreign key of the link table that references the values of the reference.
	 */
	@Name(DESTINATION_KEY)
	@Format(CommaSeparatedStrings.class)
	List<String> getDestinationKey();

	/**
	 * @see #getDestinationKey()
	 */
	void setDestinationKey(List<String> value);

}
