/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec.securityObjectProvider;

import com.top_logic.basic.config.AbstractConfigurationValueProvider;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.tool.boundsec.ReferencedSecurityObjectProvider;
import com.top_logic.tool.boundsec.ReferencedSecurityObjectProvider.Config;
import com.top_logic.tool.boundsec.SecurityObjectProvider;
import com.top_logic.tool.boundsec.SecurityObjectProviderManager;
import com.top_logic.tool.boundsec.securityObjectProvider.path.PathFormat;

/**
 * {@link AbstractConfigurationValueProvider} for {@link SecurityObjectProvider} that either
 * instantiates the configured class or creates a {@link PathSecurityObjectProvider} for the
 * configured path.
 * 
 * @see PathSecurityObjectProvider
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class SecurityObjectProviderFormat
		extends AbstractConfigurationValueProvider<PolymorphicConfiguration<? extends SecurityObjectProvider>> {

	private static String CLASS_PREFIX = "class:";

	private static String TL_PREFIX = "com.top_logic.";

	/** Singleton {@link SecurityObjectProviderFormat}. */
	public static final SecurityObjectProviderFormat INSTANCE = new SecurityObjectProviderFormat();

	private SecurityObjectProviderFormat() {
		super(PolymorphicConfiguration.class);
	}

	@Override
	protected PolymorphicConfiguration<? extends SecurityObjectProvider> getValueNonEmpty(String propertyName,
			CharSequence propertyValue) throws ConfigurationException {
		String configuredValue = propertyValue.toString();
		if (configuredValue.startsWith(SecurityObjectProviderManager.PATH_SECURITY_OBJECT_PROVIDER)) {
			String configuredPath =
				configuredValue.substring(SecurityObjectProviderManager.PATH_SECURITY_OBJECT_PROVIDER.length());
			return PathSecurityObjectProvider.toConfig(propertyName, configuredPath);
		}
		String configuredClass;
		if (configuredValue.startsWith(CLASS_PREFIX)) {
			configuredClass = configuredValue.substring(CLASS_PREFIX.length());
		} else if (configuredValue.startsWith(TL_PREFIX)) {
			configuredClass = configuredValue;
		} else {
			configuredClass = null;
		}
		if (configuredClass != null) {
			Class<? extends SecurityObjectProvider> implClass =
				ConfigUtil.getClassForNameMandatory(SecurityObjectProvider.class, propertyName, configuredClass);
			return TypedConfiguration.createConfigItemForImplementationClass(implClass);
		}

		Config referenceConfig = TypedConfiguration.newConfigItem(ReferencedSecurityObjectProvider.Config.class);
		referenceConfig.setReference(configuredValue);
		return referenceConfig;
	}

	@Override
	protected String getSpecificationNonNull(PolymorphicConfiguration<? extends SecurityObjectProvider> configValue) {
		if (configValue instanceof PathSecurityObjectProvider.Config) {
			return SecurityObjectProviderManager.PATH_SECURITY_OBJECT_PROVIDER
				+ PathFormat.INSTANCE.getSpecification(((PathSecurityObjectProvider.Config) configValue).getPath());
		}
		if (configValue instanceof ReferencedSecurityObjectProvider.Config) {
			return ((ReferencedSecurityObjectProvider.Config) configValue).getReference();
		}

		return CLASS_PREFIX + configValue.getClass().getName();
	}

}

