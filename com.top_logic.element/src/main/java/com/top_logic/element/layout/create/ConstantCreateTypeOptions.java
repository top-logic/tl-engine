/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.create;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.col.Filter;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.layout.basic.LabelSorter;
import com.top_logic.layout.provider.MetaLabelProvider;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLScope;
import com.top_logic.model.util.TLModelPartRef;
import com.top_logic.model.util.TLModelUtil;

/**
 * {@link CreateTypeOptions} that derive from a single static type.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Constant type options")
public class ConstantCreateTypeOptions<C extends ConstantCreateTypeOptions.Config<?>>
		extends AbstractConfiguredInstance<C> implements CreateTypeOptions {

	private TLClass _type;

	/**
	 * Configuration options for {@link ConstantCreateTypeOptions}.
	 */
	public interface Config<I extends ConstantCreateTypeOptions<?>> extends PolymorphicConfiguration<I> {

		/**
		 * The type to instantiate.
		 * 
		 * <p>
		 * If the type is {@link TLClass#isAbstract() abstract} or {@link #getIncludeSubtypes()} is
		 * true, the user is able to choose from all specializations of the given type.
		 * </p>
		 */
		@Mandatory
		TLModelPartRef getType();

		/**
		 * Whether to enable the user to choose the type to instantiate from all subtypes of
		 * {@link #getType()}.
		 */
		@BooleanDefault(true)
		boolean getIncludeSubtypes();

	}

	/**
	 * Creates a {@link ConstantCreateTypeOptions} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ConstantCreateTypeOptions(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);

		_type = config.getType().resolveClass();
	}

	@Override
	public List<TLClass> getPossibleTypes(Object contextModel) {
		if (_type.isAbstract() || getConfig().getIncludeSubtypes()) {
			Filter<? super TLClass> excludeForeignLocalTypes = t -> {
				if (t.isAbstract()) {
					return false;
				}
				TLScope scope = t.getScope();
				return (scope == t.getModule()) || scope == contextModel;
			};
			List<TLClass> types =
				new ArrayList<>(TLModelUtil.getReflexiveTransitiveSpecializations(excludeForeignLocalTypes, _type));
			LabelSorter.sortByLabelInline(types, MetaLabelProvider.INSTANCE);
			return types;
		} else {
			return Collections.singletonList(_type);
		}
	}

	@Override
	public TLClass getDefaultType(Object contextModel, List<TLClass> typeOptions) {
		if (!_type.isAbstract()) {
			return _type;
		}
		return CreateTypeOptions.super.getDefaultType(contextModel, typeOptions);
	}

}
