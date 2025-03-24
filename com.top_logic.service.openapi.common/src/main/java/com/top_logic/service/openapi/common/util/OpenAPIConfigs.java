/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.util;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.service.openapi.common.authentication.SecretConfiguration;
import com.top_logic.service.openapi.common.document.SecuritySchemeObject;
import com.top_logic.service.openapi.common.document.SecuritySchemeType;

/**
 * Utility methods for dealing with <i>OpenAPI</i> configurations.
 */
public class OpenAPIConfigs {

	/**
	 * Transfers a value form a {@link Supplier} to a {@link Consumer}, if the value is not empty.
	 */
	public static <T> void transferIfNotEmpty(Supplier<T> supplier, Consumer<? super T> consumer) {
		T t = supplier.get();
		if (!StringServices.isEmpty(t)) {
			consumer.accept(t);
		}
	}

	/**
	 * Transfers a value form a {@link Supplier} to a {@link Consumer}, if the value is not null.
	 */
	public static <T extends ConfigurationItem> void transferCopyIfNotNull(Supplier<T> supplier,
			Consumer<? super T> consumer) {
		T t = supplier.get();
		if (t != null) {
			consumer.accept(TypedConfiguration.copy(t));
		}
	}

	/**
	 * Transfers a value form a {@link Supplier} to a {@link Consumer}, if the value is not
	 * <code>true</code>.
	 */
	public static void transferIfTrue(BooleanSupplier supplier, Consumer<Boolean> consumer) {
		if (supplier.getAsBoolean()) {
			consumer.accept(Boolean.TRUE);
		}
	}

	/**
	 * Creates a {@link SecuritySchemeObject} with given schema name.
	 */
	public static SecuritySchemeObject newSecuritySchema(String schemaName) {
		SecuritySchemeObject securityScheme = TypedConfiguration.newConfigItem(SecuritySchemeObject.class);
		securityScheme.setSchemaName(schemaName);
		return securityScheme;
	}

	/**
	 * Creates a {@link SecuritySchemeObject} with {@link SecuritySchemeType#HTTP} scheme type.
	 */
	public static SecuritySchemeObject newHTTPAuthentication(String schemaName) {
		SecuritySchemeObject securityScheme = newSecuritySchema(schemaName);
		securityScheme.setType(SecuritySchemeType.HTTP);
		return securityScheme;
	}

	/**
	 * The {@link SecretConfiguration}s with the given secret type.
	 */
	public static <T extends SecretConfiguration> Stream<T> secretsOfType(String domain,
			List<? extends SecretConfiguration> secrets, Class<T> secretType) {
		return secrets
			.stream()
			.filter(secretType::isInstance)
			.map(secretType::cast)
			.filter(secret -> secret.getDomain().equals(domain));
	}
}
