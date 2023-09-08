/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Comparator;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstanceAccess;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;

/**
 * {@link Comparator} delegating to another {@link Comparator} implementation.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComparatorProxy<T> implements Comparator<T>, ConfiguredInstance<ComparatorProxy.Config<? super T>> {

	/**
	 * Configuration for a {@link ComparatorProxy}.
	 */
	public interface Config<S> extends PolymorphicConfiguration<Comparator<S>> {

		/**
		 * The delegate {@link Comparator}.
		 */
		@Mandatory
		PolymorphicConfiguration<Comparator<S>> getBaseComparator();

		/**
		 * Setter for {@link #getBaseComparator()}.
		 */
		void setBaseComparator(PolymorphicConfiguration<Comparator<S>> base);

	}

	private Comparator<? super T> _impl;

	private Config<? super T> _config;

	/**
	 * Creates a {@link ComparatorProxy} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public ComparatorProxy(InstantiationContext context, Config<? super T> config) {
		this(context.getInstance(config.getBaseComparator()));
		_config = config;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Config<? super T> getConfig() {
		if (_config != null) {
			return _config;
		}
		try {
			@SuppressWarnings("rawtypes")
			Config<? super T> config = (Config) TypedConfiguration.createConfigItemForImplementationClass(getClass());
			return fillConfigForProgrammaticallyCreatedProxy(config, _impl);
		} catch (ConfigurationException ex) {
			throw new ConfigurationError(ex);
		}
	}

	/**
	 * Fills the given newly configuration created to get a configuration that represents this
	 * proxy.
	 * <p>
	 * This method is used to synthesize a configuration for a programmatically created
	 * {@link ComparatorProxy}.
	 * </p>
	 * 
	 * @param config
	 *        New configuration representing this proxy
	 * @param impl
	 *        The actual implementation comparator.
	 * @return The given config.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected Config fillConfigForProgrammaticallyCreatedProxy(Config config, Comparator impl) {
		config.setBaseComparator((PolymorphicConfiguration) InstanceAccess.INSTANCE.getConfig(impl));
		return config;
	}

	/**
	 * Creates a {@link ComparatorProxy}.
	 *
	 * @param impl
	 *        The {@link Comparator} delegate.
	 */
	public ComparatorProxy(Comparator<? super T> impl) {
		_impl = impl;
	}

	@Override
	public int compare(T o1, T o2) {
		return _impl.compare(o1, o2);
	}

}
