/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.config;

import java.util.List;

import com.top_logic.basic.col.filter.configurable.ConfigurableAndFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableNotFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableOrFilter;
import com.top_logic.basic.col.filter.typed.TypedFilter;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.Subtypes;
import com.top_logic.basic.config.annotation.Subtypes.Subtype;
import com.top_logic.knowledge.service.db2.migration.filters.KindFilter;
import com.top_logic.knowledge.service.db2.migration.filters.NavigateBackwardsFilter;
import com.top_logic.knowledge.service.db2.migration.filters.NavigateReferenceFilter;
import com.top_logic.knowledge.service.db2.migration.rewriters.ReferenceHasTypeFilter;
import com.top_logic.knowledge.service.db2.migration.rewriters.ReferencesModelElementFilter;

/**
 * {@link ConfigurationItem} holding a list of {@link TypedFilter}
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface FilterConfig extends ConfigurationItem {

	/**
	 * @see #getFilters()
	 */
	String FILTERS = "filters";

	/**
	 * The list o configured filters.
	 */
	@Subtypes({
		@Subtype(tag = "and", type = ConfigurableAndFilter.Config.class),
		@Subtype(tag = "or", type = ConfigurableOrFilter.Config.class),
		@Subtype(tag = "not", type = ConfigurableNotFilter.Config.class),
		@Subtype(tag = "event", type = KindFilter.Config.class),
		@Subtype(tag = "navigate", type = NavigateReferenceFilter.Config.class),
		@Subtype(tag = "navigate-reverse", type = NavigateBackwardsFilter.Config.class),
		@Subtype(tag = ReferenceHasTypeFilter.Config.TAG_NAME, type = ReferenceHasTypeFilter.Config.class),
		@Subtype(tag = ReferencesModelElementFilter.Config.TAG_NAME, type = ReferencesModelElementFilter.Config.class),
	})
	@Name(FILTERS)
	List<PolymorphicConfiguration<? extends TypedFilter>> getFilters();

	/**
	 * Setter for {@link #getFilters()}.
	 */
	void setFilters(List<PolymorphicConfiguration<? extends TypedFilter>> filters);

}
