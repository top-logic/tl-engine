/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.config.annotation;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.kbbased.filtergen.AttributedValueFilter;
import com.top_logic.model.annotate.TLAttributeAnnotation;
import com.top_logic.model.annotate.TLConstraints;
import com.top_logic.model.annotate.util.ConstraintCheck;

/**
 * {@link TLAttributeAnnotation} specifying a filter that must accept attribute values.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 * 
 * @deprecated Use {@link TLConstraints} with {@link ConstraintCheck}s instead of
 *             {@link AttributedValueFilter}s. Those are in-app configurable and provide reactive
 *             user-feedback in case of constraint validation failures.
 */
@TagName("constraint")
@Deprecated
public interface TLConstraint extends TLAttributeAnnotation {

	/**
	 * Custom tag to create a {@link TLConstraint} annotation.
	 */
	String TAG_NAME = "constraint";

	/** @see #getFilter() */
	String FILTER_PROPERTY = "filter";

	/**
	 * The {@link AttributedValueFilter} to filter options with.
	 */
	@Name(FILTER_PROPERTY)
	@Mandatory
	@DefaultContainer
	PolymorphicConfiguration<AttributedValueFilter> getFilter();

	/** @see #getFilter() */
	void setFilter(PolymorphicConfiguration<AttributedValueFilter> value);

}
