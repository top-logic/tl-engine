/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.factory.agent;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.top_logic.basic.util.I18NBundle;
import com.top_logic.basic.util.ResKey;

/**
 * {@link I18NBundle} writing a key as its text, so that a test checks which message a component
 * chose without resources being loaded.
 */
public final class KeyBundle implements I18NBundle {

	/** The single instance. */
	public static final KeyBundle INSTANCE = new KeyBundle();

	private KeyBundle() {
		// Singleton.
	}

	@Override
	public Locale getLocale() {
		return Locale.ROOT;
	}

	@Override
	public List<Locale> getSupportedLocalesInDisplayOrder() {
		return List.of(Locale.ROOT);
	}

	@Override
	public Set<String> getLocalKeys() {
		return Set.of();
	}

	@Override
	public String getString(ResKey key) {
		return key.toString();
	}

	@Override
	public String getStringWithoutFallback(ResKey key) {
		return key.toString();
	}

}
