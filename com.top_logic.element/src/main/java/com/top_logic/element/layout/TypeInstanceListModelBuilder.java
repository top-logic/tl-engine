/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.LogProtocol;
import com.top_logic.basic.Protocol;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.mig.html.ListModelBuilder;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.util.TLModelUtil;

/**
 * a {@link ListModelBuilder} that returns instances of the configured types.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TypeInstanceListModelBuilder implements ListModelBuilder,
		ConfiguredInstance<TypeInstanceListModelBuilder.Config> {

	/**
	 * {@link ConfigurationItem} for the {@link TypeInstanceListModelBuilder}.
	 */
	public interface Config extends PolymorphicConfiguration<TypeInstanceListModelBuilder> {

		/** Property name of {@link #getMetaElements()}. */
		String META_ELEMENTS = "meta-elements";

		/** Property name of {@link #getExcludeSubtypes()}. */
		String EXCLUDE_SUBTYPES = "exclude-subtypes";

		/**
		 * Comma-separated list of the {@link TLClass} names whose instances should be returned.
		 */
		@Format(CommaSeparatedStrings.class)
		@Mandatory
		@Name(META_ELEMENTS)
		List<String> getMetaElements();

		/**
		 * Should instances of subtypes be excluded?
		 */
		@BooleanDefault(false)
		@Name(EXCLUDE_SUBTYPES)
		boolean getExcludeSubtypes();

	}

	private final Config _config;

	private final Set<TLClass> _types;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link TypeInstanceListModelBuilder}.
	 * <p>
	 * <b>Don't call directly.</b> Use
	 * {@link InstantiationContext#getInstance(PolymorphicConfiguration)} instead.
	 * </p>
	 * 
	 * @param context
	 *        For error reporting and instantiation of dependent configured objects.
	 * @param config
	 *        The configuration for the new instance.
	 */
	@CalledByReflection
	public TypeInstanceListModelBuilder(InstantiationContext context, Config config) {
		_config = config;
		Protocol protocol = new LogProtocol(TypeInstanceListModelBuilder.class);
		_types = TLModelUtil.findClasses(protocol, config.getMetaElements());
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent component) {
		// "Set" not "List" to eliminate duplicates
		Set<TLObject> result = new HashSet<>();
		for (TLClass tlClass : _types) {
			if (getConfig().getExcludeSubtypes()) {
				result.addAll(MetaElementUtil.getAllDirectInstancesOf(tlClass, TLObject.class));
			} else {
				result.addAll(MetaElementUtil.getAllInstancesOf(tlClass, TLObject.class));
			}
		}
		/* Return a List, not the Set, as this is a "ListModelBuilder" and even though the return
		 * type does not guarantee it, some of the code may rely on it. */
		return new ArrayList<>(result);
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return model == null;
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		if (!(listElement instanceof TLObject)) {
			return false;
		}
		TLObject tlObject = (TLObject) listElement;
		for (TLClass tlClass : _types) {
			if (getConfig().getExcludeSubtypes()) {
				if (tlClass.equals(tlObject.tType())) {
					return true;
				}
			} else {
				if (TLModelUtil.isCompatibleInstance(tlClass, tlObject)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return null;
	}

}
