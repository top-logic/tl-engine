/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.top_logic.basic.util.I18NBundleSPI;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.ResourcesModule;

/**
 * {@link I18NBundleSPI} that internationalized all resources with their keys.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
final class DummyBundle implements I18NBundleSPI {
	
	/**
	 * Singleton {@link DummyBundle} instance.
	 */
	public static final DummyBundle INSTANCE = new DummyBundle();

	private DummyBundle() {
		// Singleton constructor.
	}

	@Override
	public I18NBundleSPI getFallback() {
		return null;
	}

	@Override
	public ResourcesModule owner() {
		return null;
	}

	@Override
	public String lookup(String key) {
		return key;
	}

	@Override
	public void handleUnknownKey(ResKey key) {
		// Ignore.
	}

	@Override
	public void handleDeprecatedKey(ResKey deprecatedKey, ResKey origKey) {
		// Ignore.
	}

	@Override
	public Locale getLocale() {
		return Locale.ENGLISH;
	}

	@Override
	public List<Locale> getSupportedLocalesInDisplayOrder() {
		return Arrays.asList(getLocale());
	}

	@Override
	public boolean existsResource(String key) {
		return true;
	}

	@Override
	public Set<String> getLocalKeys() {
		return Collections.emptySet();
	}

	@Override
	public String getString(ResKey aKey) {
		return null;
	}

	@Override
	public void invalidate() {
		// Ignore.
	}

}