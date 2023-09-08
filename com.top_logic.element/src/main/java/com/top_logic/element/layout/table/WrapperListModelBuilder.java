/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.table;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.col.CloseableIterator;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.element.meta.kbbased.AbstractWrapperResolver;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;

/**
 * Provide {@link Wrapper}s as defined by the configuration for list and grid components.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class WrapperListModelBuilder<C extends WrapperListModelBuilder.Config> extends AbstractConfiguredInstance<C>
		implements ListModelBuilder {

	/**
	 * Configuration for getting attributes from an {@link AbstractWrapperResolver} (factory).
	 * 
	 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
	 */
	public interface Config extends PolymorphicConfiguration<WrapperListModelBuilder<?>> {

		/**
		 * Comma separated list of qualified type name of the {@link Wrapper} to be created.
		 */
		@Format(CommaSeparatedStrings.class)
		List<String> getTypes();

		/**
		 * Setter for {@link #getTypes()}.
		 */
		void setTypes(List<String> types);

	}

	private final Set<TLClass> _types;

	/**
	 * Creates a new {@link WrapperListModelBuilder} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link WrapperListModelBuilder}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public WrapperListModelBuilder(InstantiationContext context, C config) throws ConfigurationException {
		super(context, config);
		_types = new HashSet<>();

		for (String typeName : getConfig().getTypes()) {
			TLClass type = (TLClass) TLModelUtil.findType(typeName);
			_types.addAll(TLModelUtil.getConcreteReflexiveTransitiveSpecializations(type));
		}
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent aComponent) {
		return getAllWrappers();
	}

	@Override
	public boolean supportsModel(Object aModel, LayoutComponent aComponent) {
		return true;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		if (listElement instanceof TLObject) {
			TLObject wrapper = (TLObject) listElement;
			return _types.contains(wrapper.tType());
		}

		return false;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent aComponent, Object anElement) {
		/* The list does not depend on the model. So any model is correct. For stability return
		 * current model. */
		return aComponent.getModel();
	}

	/**
	 * Return all wrappers as defined by the configuration.
	 * 
	 * @return All wrappers known by the factory.
	 */
	@SuppressWarnings("unchecked")
	protected <W extends Wrapper> List<W> getAllWrappers() {
		List<W> wrappers = new ArrayList<>();

		for (TLClass type : _types) {
			try (CloseableIterator<Wrapper> it = MetaElementUtil.iterateDirectInstances(type, Wrapper.class)) {
				while (it.hasNext()) {
					wrappers.add((W) it.next());
				}
			}
		}

		return wrappers;
	}

}
