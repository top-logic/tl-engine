/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.meta.expr.internal;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.DefaultContainer;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.element.meta.kbbased.filtergen.AbstractAttributeValueLocator;
import com.top_logic.element.meta.kbbased.filtergen.AttributeValueLocator;
import com.top_logic.model.TLObject;

/**
 * {@link AttributeValueLocator} that delegates to a chain of {@link AttributeValueLocator}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class Chain extends AbstractAttributeValueLocator implements ConfiguredInstance<Chain.Config> {

	/**
	 * Configuration options of {@link Chain}.
	 */
	@TagName("chain")
	public interface Config extends PolymorphicConfiguration<Chain> {

		/**
		 * Configuration of {@link AttributeValueLocator} steps to chain together.
		 * 
		 * <p>
		 * An {@link AttributeValueLocator} in the chain is invoke on the value returned from the
		 * previous step.
		 * </p>
		 */
		@DefaultContainer
		List<PolymorphicConfiguration<? extends AttributeValueLocator>> getSteps();

	}

	private final List<AttributeValueLocator> _steps;

	private final Config _config;

	/**
	 * Creates a {@link Chain} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public Chain(InstantiationContext context, Config config) {
		_config = config;
		_steps = TypedConfiguration.getInstanceList(context, config.getSteps());
	}

	@Override
	public Config getConfig() {
		return _config;
	}

	@Override
	public Object locateAttributeValue(Object obj) {
		Object value = obj;
		for (AttributeValueLocator locator : _steps) {
			value = locator.locateAttributeValue(value);
		}
		return value;
	}

	@Override
	public Set<? extends TLObject> locateReferers(Object value) {
		if (_steps.isEmpty()) {
			return Collections.emptySet();
		}
		Set<? extends TLObject> sources = _steps.get(_steps.size() - 1).locateReferers(value);

		for (int i = _steps.size() - 2; i >= 0; i--) {
			if (sources.isEmpty()) {
				return sources;
			}
			AttributeValueLocator step = _steps.get(i);
			sources = sources.stream()
				.map(step::locateReferers)
				.collect(HashSet::new, HashSet::addAll, HashSet::addAll);
		}

		return sources;
	}

	@Override
	protected boolean isBackReference() {
		if (_steps.size() != 1) {
			// There is no reference for which the step is a reverse reference.
			return false;
		}
		return AbstractAttributeValueLocator.isBackReferenceLocator(lastStep());
	}

	@Override
	protected boolean isCollection() {
		for (AttributeValueLocator step : _steps) {
			if (AbstractAttributeValueLocator.isCollectionLocator(step)) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected String getValueTypeSpec() {
		return AbstractAttributeValueLocator.getLocatorValueTypeSpec(lastStep());
	}

	@Override
	protected String getReverseEndSpec() {
		return null;
	}

	private AttributeValueLocator lastStep() {
		return _steps.get(_steps.size() - 1);
	}

	/**
	 * Creates the configuration of a {@link Chain}.
	 */
	public static PolymorphicConfiguration<? extends AttributeValueLocator> newInstance(
			List<? extends PolymorphicConfiguration<? extends AttributeValueLocator>> locators) {
		Config config = TypedConfiguration.newConfigItem(Config.class);
		config.getSteps().addAll(locators);
		return config;
	}

}
