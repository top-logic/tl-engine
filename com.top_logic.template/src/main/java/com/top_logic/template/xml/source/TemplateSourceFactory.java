/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.template.xml.source;

import java.util.Map;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.annotation.InstanceFormat;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * Creates instances of {@link TemplateSource}.
 * <p>
 * Projects can subclass this factory if they have their own implementations of
 * {@link TemplateSource} not supported by the framework.
 * </p>
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public class TemplateSourceFactory extends ConfiguredManagedClass<TemplateSourceFactory.Config> {

	/**
	 * Configuration options for {@link TemplateSourceFactory}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<TemplateSourceFactory> {

		/**
		 * @see #getProtocols()
		 */
		String PROTOCOLS = "protocols";

		/**
		 * @see #getDefaultProtocol()
		 */
		String DEFAULT_PROTOCOL = "default-protocol";

		/**
		 * The protocol to use, if no the resource name does not contain a <code>protocol:</code>
		 * prefix and no protocol is defined by a context resource.
		 * 
		 * @see #getProtocols()
		 */
		@Name(DEFAULT_PROTOCOL)
		@Mandatory
		String getDefaultProtocol();

		/**
		 * {@link TemplateLocator}s for various protocols.
		 */
		@Name(PROTOCOLS)
		@Key(ProtocolConfig.NAME_ATTRIBUTE)
		Map<String, ProtocolConfig> getProtocols();

		/**
		 * Configuration for a single protocol.
		 * 
		 * @see #getName() The name of the protocol.
		 * @see #getLocator()
		 */
		interface ProtocolConfig extends NamedConfigMandatory {
			/**
			 * The {@link TemplateLocator} to use for this protocol.
			 */
			@Mandatory
			@InstanceFormat
			TemplateLocator getLocator();
		}

	}

	/**
	 * Creates a {@link TemplateSourceFactory} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TemplateSourceFactory(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Resolves the given templateReference with the known {@link TemplateSource}s.
	 * 
	 * @throws RuntimeException
	 *         If no responsible {@link TemplateSource} is found.
	 */
	public TemplateSource resolve(String templateReference) {
		return resolve(null, templateReference);
	}

	/**
	 * Resolves the given templateReference with the known {@link TemplateSource}s.
	 * 
	 * @param context
	 *        The {@link TemplateSource} that resolved the calling template, or <code>null</code>,
	 *        if this is an initial template invocation.
	 * 
	 * @throws RuntimeException
	 *         If no responsible {@link TemplateSource} is found.
	 */
	@FrameworkInternal
	public TemplateSource resolve(TemplateSource context, String templateReference) {
		int schemaSeparatorIndex = templateReference.indexOf(':');
		TemplateLocator locator;
		String plainName;
		if (schemaSeparatorIndex >= 0) {
			String schemaName = templateReference.substring(0, schemaSeparatorIndex);
			plainName = templateReference.substring(schemaSeparatorIndex + 1);
			locator = locator(schemaName);
		} else {
			plainName = templateReference;
			if (context == null) {
				locator = locator(getConfig().getDefaultProtocol());
			} else {
				locator = context;
			}
		}

		if (locator == null) {
			throw new RuntimeException("No " + TemplateSource.class.getSimpleName()
				+ " known responsible for schema '" + templateReference + "' in context of '" + context + "'.");
		}

		return locator.resolve(context, plainName);
	}

	private TemplateLocator locator(String schemaName) {
		Config.ProtocolConfig locatorConfig = getConfig().getProtocols().get(schemaName);
		if (locatorConfig == null) {
			return null;
		}
		return locatorConfig.getLocator();
	}

	/**
	 * The singleton {@link TemplateSourceFactory} instance.
	 */
	public static TemplateSourceFactory getInstance() {
		return TemplateSourceFactory.Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Singleton reference for {@link TemplateSourceFactory}.
	 */
	public static class Module extends TypedRuntimeModule<TemplateSourceFactory> {

		/**
		 * Singleton {@link TemplateSourceFactory.Module} instance.
		 */
		public static final TemplateSourceFactory.Module INSTANCE = new TemplateSourceFactory.Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<TemplateSourceFactory> getImplementation() {
			return TemplateSourceFactory.class;
		}

	}

}
