/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html;

import java.util.Collection;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.mig.html.layout.LayoutComponent;

/**
 * A {@link ListModelBuilder} that has an inner {@link ListModelBuilder} and delegates all calls to
 * that.
 * <p>
 * This class is meant to be subclassed: Concrete proxies have to override those methods that they
 * want to intercept.
 * </p>
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public abstract class ProxyListModelBuilder<C extends ProxyListModelBuilder.Config>
		extends AbstractConfiguredInstance<C> implements ListModelBuilder {

	/** {@link ConfigurationItem} for the {@link ProxyListModelBuilder}. */
	public interface Config extends PolymorphicConfiguration<ProxyListModelBuilder<?>> {

		/** Property name of {@link #getInner()}. */
		String INNER = "inner";

		/** The inner, proxied {@link ListModelBuilder}. */
		@Name(INNER)
		PolymorphicConfiguration<? extends ListModelBuilder> getInner();

	}

	private final ListModelBuilder _inner;

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link ProxyListModelBuilder}.
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
	public ProxyListModelBuilder(InstantiationContext context, C config) {
		super(context, config);
		_inner = context.getInstance(config.getInner());
	}

	@Override
	public Collection<?> getModel(Object businessModel, LayoutComponent component) {
		return _inner.getModel(businessModel, component);
	}

	@Override
	public boolean supportsModel(Object model, LayoutComponent component) {
		return _inner.supportsModel(model, component);
	}

	@Override
	public boolean supportsListElement(LayoutComponent contextComponent, Object listElement) {
		return _inner.supportsListElement(contextComponent, listElement);
	}

	@Override
	public Object retrieveModelFromListElement(LayoutComponent contextComponent, Object listElement) {
		return _inner.retrieveModelFromListElement(contextComponent, listElement);
	}

}
