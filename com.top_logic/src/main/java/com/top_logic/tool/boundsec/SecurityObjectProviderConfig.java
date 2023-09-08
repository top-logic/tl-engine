/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.layout.ModelSpec;
import com.top_logic.tool.boundsec.securityObjectProvider.ConfiguredModelSecurityProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.ModelSecurityObjectProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.PathSecurityObjectProvider;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityObjectProviderFormat;
import com.top_logic.tool.boundsec.securityObjectProvider.SecurityRootObjectProvider;

/**
 * Configuration of a {@link SecurityObjectProvider}.
 *
 * @author <a href="mailto:Dmitry.Ivanizki@top-logic.com">Dmitry Ivanizki</a>
 */
@Abstract
public interface SecurityObjectProviderConfig extends ConfigurationItem {

	/** @see #getSecurityObject() */
	public static final String SECURITY_OBJECT = "securityObject";

	/**
	 * The {@link SecurityObjectProvider algorithm} that navigates to the {@link Object} on which
	 * the security is checked.
	 */
	@Name(SECURITY_OBJECT)
	@Format(CompactSecurityObjectProviderFormat.class)
	PolymorphicConfiguration<? extends SecurityObjectProvider> getSecurityObject();

	/**
	 * Format to serialize an In App configured {@link SecurityObjectProvider} in compact form.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class CompactSecurityObjectProviderFormat
			extends AbstractConfigurationValueProvider<PolymorphicConfiguration<? extends SecurityObjectProvider>> {

		static final String SECURITY_ROOT = "securityRoot";

		static final String MODEL = "model";

		/** {@link Pattern} for allowed values. */
		public static final Pattern PATTERN = pattern();

		/**
		 * Creates a new {@link CompactSecurityObjectProviderFormat}.
		 */
		public CompactSecurityObjectProviderFormat() {
			super(PolymorphicConfiguration.class);
		}

		private static Pattern pattern() {
			return Pattern
				.compile("(?:"
					+ group(MODEL) + "|"
					+ group(SECURITY_ROOT) + "|"
					+ group(SecurityObjectProviderManager.PATH_SECURITY_OBJECT_PROVIDER + "[\\w\\.]*") + "|"
					+ group(ModelSpec.Format.MODEL_PATTERN.pattern()) +
					")");
		}

		private static String group(String pattern) {
			return "(" + pattern + ")";
		}

		@Override
		protected PolymorphicConfiguration<? extends SecurityObjectProvider> getValueNonEmpty(String propertyName,
				CharSequence propertyValue) throws ConfigurationException {
			Matcher matcher = PATTERN.matcher(propertyValue);
			if (matcher.matches()) {
				if (matcher.group(1) != null) {
					return TypedConfiguration.createConfigItemForImplementationClass(ModelSecurityObjectProvider.class);
				} else if (matcher.group(2) != null) {
					return TypedConfiguration.createConfigItemForImplementationClass(SecurityRootObjectProvider.class);
				} else if (matcher.group(3) != null) {
					return SecurityObjectProviderFormat.INSTANCE.getValue(propertyName, matcher.group(3));
				} else if (matcher.group(4) != null) {
					ConfiguredModelSecurityProvider.Config result =
						TypedConfiguration.newConfigItem(ConfiguredModelSecurityProvider.Config.class);
					ModelSpec modelSpec = ModelSpec.Format.INSTANCE
						.getValue(ConfiguredModelSecurityProvider.Config.MODEL, matcher.group(4));
					result.setModel(modelSpec);
					return result;
				} else {
					throw new UnreachableAssertion("At least one group is not null.");
				}
			} else {
				throw new ConfigurationException(I18NConstants.INVALID_SECURITY_OBJECT_PROVIDER_SERIALIZATION,
					propertyName, propertyValue);
			}
		}

		@Override
		protected String getSpecificationNonNull(
				PolymorphicConfiguration<? extends SecurityObjectProvider> configValue) {
			if (isModelSecurityObjectProvider(configValue)) {
				return MODEL;
			}
			if (isSecurityRootObjectProvider(configValue)) {
				return SECURITY_ROOT;
			}
			if (configValue instanceof PathSecurityObjectProvider.Config) {
				return SecurityObjectProviderFormat.INSTANCE.getSpecification(configValue);
			}
			if (configValue instanceof ConfiguredModelSecurityProvider.Config) {
				return ModelSpec.Format.INSTANCE
					.getSpecification(((ConfiguredModelSecurityProvider.Config) configValue).getModel());
			}
			throw new IllegalArgumentException();
		}

		@Override
		public boolean isLegalValue(Object value) {
			if (!super.isLegalValue(value)) {
				return false;
			}
			if (value == null) {
				return true;
			}
			return (value instanceof ConfiguredModelSecurityProvider.Config
				&& ModelSpec.Format.INSTANCE.isLegalValue(((ConfiguredModelSecurityProvider.Config) value).getModel()))
				|| value instanceof PathSecurityObjectProvider.Config
				|| isSecurityRootObjectProvider((PolymorphicConfiguration<?>) value)
				|| isModelSecurityObjectProvider((PolymorphicConfiguration<?>) value);
		}

		private boolean isSecurityRootObjectProvider(PolymorphicConfiguration<?> value) {
			return SecurityRootObjectProvider.class.isAssignableFrom(value.getImplementationClass());
		}

		private boolean isModelSecurityObjectProvider(PolymorphicConfiguration<?> value) {
			return ModelSecurityObjectProvider.class.isAssignableFrom(value.getImplementationClass());
		}

	}
}
