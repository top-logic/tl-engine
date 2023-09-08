/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.model.filter;

import java.util.regex.Pattern;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.Log;
import com.top_logic.basic.col.filter.configurable.AbstractConfigurableFilter;
import com.top_logic.basic.col.filter.configurable.ConfigurableFilter;
import com.top_logic.basic.col.filter.typed.FilterResult;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfiguration;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.format.RegExpValueProvider;
import com.top_logic.basic.tools.NameBuilder;
import com.top_logic.model.TLNamedPart;
import com.top_logic.model.util.TLModelUtil;

/**
 * A {@link ConfigurableFilter} that accepts only {@link TLNamedPart}s whose
 * {@link TLModelUtil#qualifiedName(com.top_logic.model.TLModelPart) qualified} is equal to the
 * given name, respectively matches the given {@link Pattern}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public class TLNamedPartFilter extends AbstractConfigurableFilter<TLNamedPart, TLNamedPartFilter.Config> {

	/**
	 * The {@link TypedConfiguration} of the {@link TLNamedPartFilter}.
	 */
	@TagName("name-filter")
	public interface Config extends AbstractConfigurableFilter.Config<TLNamedPartFilter>, NamedConfiguration {

		/** Property name of {@link #getRegex()}. */
		String REGEX = "regex";

		/**
		 * If the {@link TLModelUtil#qualifiedName(com.top_logic.model.TLModelPart) qualified name}
		 * of the {@link TLNamedPart} matches this Regex, it is accepted by this filter.
		 * <p>
		 * This property is mutual exclusive with {@link #getName()}: Exactly one of them has to be
		 * set, not none, not both.
		 * </p>
		 */
		@Format(RegExpValueProvider.class)
		@Name(REGEX)
		Pattern getRegex();

		/**
		 * If the {@link TLModelUtil#qualifiedName(com.top_logic.model.TLModelPart) qualified name}
		 * of the {@link TLNamedPart} is equal to {@link #getName()}, it is accepted by this filter.
		 * <p>
		 * This property is mutual exclusive with {@link #getRegex()}: Exactly one of them has to be
		 * set, not none, not both.
		 * </p>
		 */
		@Override
		String getName();

	}

	/**
	 * Called by the {@link TypedConfiguration} for creating a {@link TLNamedPartFilter}.
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
	public TLNamedPartFilter(InstantiationContext context, Config config) {
		super(context, config);
		checkConfig(context, config);
	}

	@Override
	public Class<?> getType() {
		return TLNamedPart.class;
	}

	private void checkConfig(Log log, Config config) {
		if (config.getName().isEmpty() == (config.getRegex() == null)) {
			String message = "'" + Config.NAME_ATTRIBUTE + "' and '" + Config.REGEX + "' are mutual exclusive."
				+ " Exactly one of them has to be set, not none, not both.";
			log.error(message, new ConfigurationError(message));
		}
	}

	@Override
	public FilterResult matchesTypesafe(TLNamedPart namedPart) {
		String actualName = TLModelUtil.qualifiedName(namedPart);
		if (!getConfig().getName().isEmpty()) {
			boolean booleanResult = actualName.equals(getConfig().getName());
			return FilterResult.valueOf(booleanResult);
		}
		boolean booleanResult = getConfig().getRegex().matcher(actualName).find();
		return FilterResult.valueOf(booleanResult);
	}

	@Override
	public String toString() {
		return new NameBuilder(this)
			.add(Config.NAME_ATTRIBUTE, getConfig().getName())
			.add(Config.REGEX, getConfig().getRegex().pattern())
			.build();
	}

}
