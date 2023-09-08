/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.util;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.json.JSON;
import com.top_logic.basic.json.JSON.ParseException;
import com.top_logic.element.meta.MEConfigurationValueProvider;
import com.top_logic.element.meta.MetaElementFactory;
import com.top_logic.element.meta.MetaElementHolder;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.WrapperValueAnalyzer;
import com.top_logic.knowledge.wrap.WrapperValueFactory;
import com.top_logic.model.TLClass;

/**
 * Provider class for {@link TLClass} that can be used in config-interfaces. Use this
 * indirection to prevent problems at parsing a config during startup when the module-system is not
 * yet startet. Use {@link MetaElementProvider} as {@link Format}.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
@Format(MetaElementProvider.MetaElementProviderFormat.class)
public class MetaElementProvider implements Provider<Set<? extends TLClass>> {

	private static final String TYPE_SEPARATOR = ";";

	private static Pattern HOLDER_PATTERN = Pattern.compile("(\\{.*\\})(.*)");

	private final String _string;

	private Set<? extends TLClass> _types;

	/**
	 * Creates a new {@link MetaElementProvider} for the given string that must identify a
	 * {@link TLClass}.
	 */
	public MetaElementProvider(String string) {
		_string = string;
	}

	/**
	 * Creates a new {@link MetaElementProvider} initialized with the given {@link TLClass}.
	 */
	public MetaElementProvider(Set<? extends TLClass> types) {
		_types = types;
		_string = initString();
	}

	private String initString() {
		StringBuilder result = new StringBuilder();
		for (TLClass type : _types) {
			if (result.length() > 0) {
				result.append(TYPE_SEPARATOR);
			}
			MetaElementHolder holder = MetaElementUtil.getMetaElementHolder(type);
			String typeName = type.getName();
			if (holder == null) {
				result.append(typeName);
			} else {
				result.append(JSON.toString(WrapperValueAnalyzer.WRAPPER_INSTANCE, holder) + typeName);
			}
		}
		return result.toString();
	}

	/**
	 * Same as {@link #get()} but with more explicit name
	 */
	public Set<? extends TLClass> getTypes() {
		return get();
	}

	@Override
	public Set<? extends TLClass> get() {
		if (StringServices.isEmpty(_string)) {
			return Collections.emptySet();
		}
		if (_types != null) {
			return _types;
		}
		_types = parse(_string);
		return _types;
	}

	private Set<? extends TLClass> parse(String value) {
		Set<TLClass> result = new HashSet<>();
		for (String string : value.split(TYPE_SEPARATOR)) {
			Matcher matcher = HOLDER_PATTERN.matcher(string);
			String meName = string;
			if (matcher.matches()) {
				try {
					Object holder = JSON.fromString(matcher.group(1), WrapperValueFactory.WRAPPER_INSTANCE);
					result.add(((MetaElementHolder) holder).getMetaElement(matcher.group(2)));
					continue;
				} catch (ParseException ex) {
					throw new RuntimeException(ex);
				}
			}
			try {
				result.add(MetaElementFactory.getInstance().getGlobalMetaElement(meName));
				continue;
			} catch (IllegalArgumentException ex) {
				try {
					Logger.warn(
						"No unique config given, using fallback. Result is ambiguous, improve config by naming holder!",
						this);
					result.add(MEConfigurationValueProvider.INSTANCE.getValue("", string));
					continue;
				} catch (ConfigurationException ex1) {
					throw new RuntimeException("Could not get MetaElement for name " + meName, ex1);
				}
			}
		}
		return result;
	}

	/**
	 * the string used to serialize the {@link TLClass}
	 */
	public String getString() {
		return _string;
	}

	/**
	 * {@link ConfigurationValueProvider} for {@link MetaElementProviderFormat}
	 */
	public static class MetaElementProviderFormat extends AbstractConfigurationValueProvider<MetaElementProvider> {

		/**
		 * Singleton <code>INSTANCE</code>
		 */
		public static MetaElementProviderFormat INSTANCE = new MetaElementProviderFormat();

		/**
		 * Creates a new {@link MetaElementProviderFormat}, use {@link #INSTANCE} instead
		 */
		public MetaElementProviderFormat() {
			super(MetaElementProvider.class);
		}

		@Override
		protected MetaElementProvider getValueNonEmpty(String property, CharSequence value)
				throws ConfigurationException {
			return new MetaElementProvider(String.valueOf(value));
		}

		@Override
		protected String getSpecificationNonNull(MetaElementProvider map) {
			return map.getString();
		}

	}

}