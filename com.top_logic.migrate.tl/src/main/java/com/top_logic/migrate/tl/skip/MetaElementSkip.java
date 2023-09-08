/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.migrate.tl.skip;

import java.util.Set;

import com.top_logic.basic.col.filter.configurable.AbstractConfigurableFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.CommaSeparatedStringSet;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.element.meta.kbbased.KBBasedMetaElement;
import com.top_logic.knowledge.event.ItemCreation;
import com.top_logic.model.TLClass;

/**
 * {@link AbstractConfigurableFilter} that matches {@link TLClass} with certain names.
 * 
 * @since 5.8.0
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class MetaElementSkip extends AbstractConfigurableFilter<ItemCreation, MetaElementSkip.Config> {

	/**
	 * Configuration of an {@link MetaElementSkip}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractConfigurableFilter.Config<MetaElementSkip> {

		/** Name of {@link #getMENames()}. */
		String ME_NAMES = "me-names";

		/**
		 * The names of the {@link TLClass} which this filter shall match.
		 */
		@Name(ME_NAMES)
		@Format(CommaSeparatedStringSet.class)
		Set<String> getMENames();
	}

	private final Set<String> _meNames;

	/**
	 * Creates a new {@link MetaElementSkip}.
	 */
	public MetaElementSkip(InstantiationContext context, Config config) {
		super(context, config);
		_meNames = config.getMENames();
	}

	@Override
	public Class<?> getType() {
		return ItemCreation.class;
	}

	@Override
	protected FilterResult matchesTypesafe(ItemCreation object) {
		if (!KBBasedMetaElement.META_ELEMENT_KO.equals(object.getObjectType().getName())) {
			return FilterResult.FALSE;
		}
		return FilterResult.valueOf(_meNames.contains(object.getValues().get(KBBasedMetaElement.NAME_ATTR)));
	}

}

