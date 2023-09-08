/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.bpml.model.providers;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.form.EditContext;
import com.top_logic.element.meta.kbbased.filtergen.Generator;
import com.top_logic.element.meta.kbbased.filtergen.ListGeneratorAdaptor;
import com.top_logic.model.TLClass;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link Generator} retrieveing {@link TLClass}es that are subclasses of a configurable base class.
 * 
 * @see Config#getBaseClass()
 * @see Config#getIncludeAbstract()
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class SubClassOptions<C extends SubClassOptions.Config<?>> extends ListGeneratorAdaptor
		implements ConfiguredInstance<C> {

	/**
	 * Configuration options for {@link SubClassOptions}.
	 */
	@TagName("sub-classes")
	public interface Config<I extends SubClassOptions<?>> extends PolymorphicConfiguration<I> {

		/**
		 * Name of the base class for looking up reflexive transitive specializations.
		 */
		@Name("base-class")
		String getBaseClass();

		/**
		 * Whether to also retrieve abstract classes.
		 */
		@Name("include-abstract")
		boolean getIncludeAbstract();

	}

	private C _config;

	/**
	 * Creates a {@link SubClassOptions} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public SubClassOptions(InstantiationContext context, C config) {
		_config = config;
	}

	@Override
	public List<?> generateList(EditContext editContext) {
		{
			TLClass baseClass = (TLClass) TLModelUtil.findType(getConfig().getBaseClass());
			Set<TLClass> result;
			if (getConfig().getIncludeAbstract()) {
				result = TLModelUtil.getReflexiveTransitiveSpecializations(baseClass);
			} else {
				result = TLModelUtil.getConcreteReflexiveTransitiveSpecializations(baseClass);
			}
			return new ArrayList<>(result);
		}
	}

	@Override
	public C getConfig() {
		return _config;
	}

}
