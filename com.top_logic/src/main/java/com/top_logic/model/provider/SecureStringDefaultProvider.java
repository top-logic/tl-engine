/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */

package com.top_logic.model.provider;

import java.util.function.Supplier;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.basic.config.constraint.annotation.Bound;
import com.top_logic.basic.config.constraint.annotation.Bounds;
import com.top_logic.basic.config.constraint.annotation.Comparision;
import com.top_logic.basic.encryption.SecureRandomService;
import com.top_logic.model.TLStructuredTypePart;
import com.top_logic.model.annotate.TLTypeKind;
import com.top_logic.model.annotate.TargetType;

/**
 * A {@link DefaultProvider} to create secure random strings.
 * 
 * <p>
 * The string consists of groups of eight characters from <blockquote> {@code 0123456789abcdef}
 * </blockquote> separated by '-'.
 * </p>
 * 
 * @see SecureRandomService
 * @see UuidDefaultProvider
 */
@TargetType(TLTypeKind.STRING)
@Label("Secure random string")
public class SecureStringDefaultProvider extends AbstractConfiguredInstance<SecureStringDefaultProvider.Config>
		implements DefaultProvider, Supplier<String> {

	/**
	 * Typed configuration interface definition for {@link SecureStringDefaultProvider}.
	 */
	public interface Config extends PolymorphicConfiguration<SecureStringDefaultProvider> {

		/**
		 * The number of random bytes to produce the string.
		 */
		@IntDefault(16)
		@Bounds({
			@Bound(comparison = Comparision.GREATER, value = 0),
			@Bound(comparison = Comparision.SMALLER, value = 1_000_000)
		})
		int getSize();
	}

	/**
	 * Create a {@link SecureStringDefaultProvider}.
	 * 
	 * @param context
	 *        the {@link InstantiationContext} to create the new object in
	 * @param config
	 *        the configuration object to be used for instantiation
	 */
	public SecureStringDefaultProvider(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public String get() {
		return SecureRandomService.getInstance().getRandomString(getConfig().getSize());
	}

	@Override
	public Object createDefault(Object context, TLStructuredTypePart attribute, boolean createForUI) {
		return get();
	}

}

