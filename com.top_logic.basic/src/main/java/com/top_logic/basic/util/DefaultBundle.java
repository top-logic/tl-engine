/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.util;

import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.annotation.FrameworkInternal;

/**
 * Default {@link I18NBundle} implementation.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class DefaultBundle implements I18NBundleSPI {

	private final ResourcesModule _owner;

	/** The locale of the properties (for reloading). */
	private final Locale _locale;

	/** The resource instance refers to the static bundle for the specific language. */
	private final Map<String, String> _bundle;

	private I18NBundleSPI _fallback;

	private boolean _valid = true;

	private List<Locale> _supportedLocalesInDisplayOrder;

	/**
	 * Creates a {@link DefaultBundle}.
	 * 
	 * @param owner
	 *        The context {@link ResourcesModule}.
	 * @param locale
	 *        See {@link #getLocale()}
	 */
	public DefaultBundle(ResourcesModule owner, Locale locale, Map<String, String> bundle, I18NBundleSPI fallback) {
		_owner = owner;
		_locale = locale;
		_fallback = fallback;
		_bundle = bundle;
		_supportedLocalesInDisplayOrder = inDisplayOrder(owner.getSupportedLocales());
	}

	private List<Locale> inDisplayOrder(List<Locale> supportedLocales) {
		List<Locale> result = supportedLocales.stream()
			.filter(l -> l != _locale)
			.sorted((l1, l2) -> l1.getDisplayLanguage(_locale).compareTo(l2.getDisplayLanguage(_locale)))
			.collect(Collectors.toList());

		// Own locale is first.
		result.add(0, _locale);

		return Collections.unmodifiableList(result);
	}

	@Override
	public List<Locale> getSupportedLocalesInDisplayOrder() {
		return _supportedLocalesInDisplayOrder;
	}

	@Override
	public I18NBundleSPI getFallback() {
		return _fallback;
	}

	@Override
	public final ResourcesModule owner() {
		return _owner;
	}

	@Override
	public Locale getLocale() {
		return _locale;
	}

	@Override
	public Set<String> getLocalKeys() {
		return _bundle.keySet();
	}

	@Override
	public String getString(ResKey aKey) {
		if (aKey == null) {
			return StringServices.EMPTY_STRING;
		}

		if (_owner.isAlwaysShowKeys()) {
			return aKey.debug(this);
		}

		return aKey.resolve(this, aKey);
	}

	/**
	 * Whether this bundle is still the active one.
	 */
	protected final boolean isValid() {
		return _valid;
	}

	@Override
	public final void invalidate() {
		_valid = false;
	}

	@Override
	@FrameworkInternal
	public String lookup(String key) {
		String result = lookupLocal(key);
		if (result == null && _fallback != null) {
			return _fallback.lookup(key);
		}
		return result;
	}

	private String lookupLocal(String key) {
		return _bundle.get(key);
	}

	@Override
	public void handleUnknownKey(ResKey key) {
		_owner.handleUnknownKey(this, key);
	}

	@Override
	public void handleDeprecatedKey(ResKey deprecatedKey, ResKey origKey) {
		_owner.handleDeprecatedKey(this, deprecatedKey, origKey);
	}

	@Override
	@FrameworkInternal
	public boolean existsResource(String key) {
		return _bundle.containsKey(key) || (_fallback != null && _fallback.existsResource(key));
	}

	/**
	 * @deprecated Use {@link #getString(ResKey)}.
	 */
	@Deprecated
	public String decodeMessageFromKeyWithEncodedArguments(ResKey keyWithEncodedArguments) {
		return getString(keyWithEncodedArguments);
	}

}
