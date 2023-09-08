/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.flex.chart.config.util;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Provider;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationValueProvider;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.json.JSON;
import com.top_logic.element.meta.AttributeOperations;
import com.top_logic.element.meta.MetaElementHolder;
import com.top_logic.element.meta.MetaElementUtil;
import com.top_logic.knowledge.wrap.WrapperValueAnalyzer;
import com.top_logic.model.TLClass;
import com.top_logic.model.TLObject;
import com.top_logic.model.TLStructuredTypePart;

/**
 * Provider class for {@link TLStructuredTypePart} that can be used in config-interfaces. Use this
 * indirection to prevent problems at parsing a config during startup when the module-system is not
 * yet startet. Use {@link MetaAttributeProviderFormat} as {@link Format}.
 * 
 * @author <a href="mailto:cca@top-logic.com">cca</a>
 */
@Format(MetaAttributeProvider.MetaAttributeProviderFormat.class)
public class MetaAttributeProvider implements Provider<TLStructuredTypePart> {

	private static Pattern FULL_QUALIFYING_PATTERN = Pattern.compile("(.*)#(.*)");

	private final String _spec;

	private TLStructuredTypePart _ma;

	/**
	 * Creates a new {@link MetaAttributeProvider} for the given string that must identify a
	 * {@link TLStructuredTypePart}.
	 */
	public MetaAttributeProvider(String spec) {
		_spec = spec;
	}

	/**
	 * Creates a new {@link MetaAttributeProvider} initialized with the given {@link TLStructuredTypePart}.
	 */
	public MetaAttributeProvider(TLStructuredTypePart ma) {
		_ma = ma;
		_spec = toSpec(ma);
	}

	private static String toSpec(TLStructuredTypePart ma) {
		if (ma == null) {
			return StringServices.EMPTY_STRING;
		}
		TLClass me = AttributeOperations.getMetaElement(ma);
		MetaElementHolder holder = MetaElementUtil.getMetaElementHolder(me);
		String maSpec = me.getName() + "#" + ma.getName();
		if (holder == null) {
			return maSpec;
		} else {
			String holderSpec = JSON.toString(WrapperValueAnalyzer.WRAPPER_INSTANCE, holder);
			return holderSpec + maSpec;
		}
	}

	/**
	 * the name of the attribute. May be accessed even if the {@link TLStructuredTypePart}-instance
	 *         identified by the string has not yet been resolved.
	 */
	public String getMetaAttributeName() {
		int pos = _spec.indexOf('#');
		if (pos < 0) {
			return _spec;
		}
		return _spec.substring(pos + 1);
	}

	/**
	 * If the type is configured somewhere else and this config only names the attribute the
	 * context-information must be given to identify the {@link TLStructuredTypePart}. Once it is
	 * initialized the meta-attribute can be retrieved using {@link #get()}.
	 * 
	 * @param obj
	 *        context-information to resolve the {@link TLStructuredTypePart}
	 * @return the attribute identified by this
	 */
	public TLStructuredTypePart getMetaAttribute(Object obj) {
		if (obj instanceof Collection) {
			return ((Collection<?>) obj).isEmpty() ? getMetaAttribute() : getMetaAttribute(CollectionUtil.getFirst(obj));
		}
		if (obj instanceof TLClass) {
			return getMetaAttribute((TLClass) obj);
		}
		if (obj instanceof TLObject) {
			return getMetaAttribute((TLObject) obj);
		}
		return getMetaAttribute();
	}

	/**
	 * see {@link #getMetaAttribute(Object)}
	 */
	public TLStructuredTypePart getMetaAttribute(TLObject att) {
		if (_ma != null) {
			return _ma;
		}
		if (att == null) {
			if (_spec.indexOf('#') > -1) {
				return getMetaAttribute();
			}
			return null;
		}
		TLClass type = (TLClass) att.tType();
		return getMetaAttribute(type);
	}

	/**
	 * see {@link #getMetaAttribute(Object)}
	 */
	public TLStructuredTypePart getMetaAttribute(TLClass me) {
		if (_ma == null) {
			_ma = MetaElementUtil.getMetaAttributeOrNull(me, _spec);
		}
		return get();
	}

	/**
	 * Same as {@link #get()} but with more explicit name
	 */
	public TLStructuredTypePart getMetaAttribute() {
		return get();
	}

	@Override
	public TLStructuredTypePart get() {
		if (StringServices.isEmpty(_spec)) {
			return null;
		}
		if (_ma != null) {
			return _ma;
		}
		Matcher matcher = FULL_QUALIFYING_PATTERN.matcher(_spec);
		if (matcher.matches()) {
			TLClass me = getTypes(matcher.group(1)).iterator().next();
			String maName = matcher.group(2);
			_ma = MetaElementUtil.getMetaAttributeOrNull(me, maName);
		}
		return _ma;
	}

	private Set<? extends TLClass> getTypes(String string) {
		return new MetaElementProvider(string).get();
	}

	/**
	 * the string used to serialize the {@link TLStructuredTypePart}
	 */
	public String getSpec() {
		return _spec;
	}

	/**
	 * {@link ConfigurationValueProvider} for {@link MetaAttributeProvider}
	 */
	public static class MetaAttributeProviderFormat extends AbstractConfigurationValueProvider<MetaAttributeProvider> {

		private static final MetaAttributeProvider NO_ATTRIBUTE = new MetaAttributeProvider((TLStructuredTypePart) null);

		/**
		 * Singleton <code>INSTANCE</code>
		 */
		public static MetaAttributeProviderFormat INSTANCE = new MetaAttributeProviderFormat();

		/**
		 * Creates a new {@link MetaAttributeProviderFormat}, use {@link #INSTANCE} instead
		 */
		public MetaAttributeProviderFormat() {
			super(MetaAttributeProvider.class);
		}

		@Override
		protected MetaAttributeProvider getValueNonEmpty(String property, CharSequence value)
				throws ConfigurationException {
			return new MetaAttributeProvider(value.toString());
		}

		@Override
		protected String getSpecificationNonNull(MetaAttributeProvider provider) {
			return provider.getSpec();
		}

		@Override
		public MetaAttributeProvider defaultValue() {
			return null;
		}
	}

}