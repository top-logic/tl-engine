/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.boundsec;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;

/**
 * {@link SecurityObjectProvider} referencing a different {@link SecurityObjectProvider} that is
 * known by the {@link SecurityObjectProviderManager}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ReferencedSecurityObjectProvider implements SecurityObjectProvider {

	/**
	 * Configuration for a {@link ReferencedSecurityObjectProvider}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends PolymorphicConfiguration<ReferencedSecurityObjectProvider> {

		/**
		 * The name of the {@link SecurityObjectProvider} to resolve against the
		 * {@link SecurityObjectProviderManager}.
		 */
		@Mandatory
		String getReference();

		/**
		 * Setter for {@link #getReference()}
		 */
		void setReference(String ref);

	}

	private String _reference;

	private SecurityObjectProvider _delegate;

	/**
	 * Creates a new {@link ReferencedSecurityObjectProvider} from the given configuration.
	 * 
	 * @param context
	 *        {@link InstantiationContext} to instantiate sub configurations.
	 * @param config
	 *        Configuration for this {@link ReferencedSecurityObjectProvider}.
	 * 
	 * @throws ConfigurationException
	 *         iff configuration is invalid.
	 */
	public ReferencedSecurityObjectProvider(InstantiationContext context, Config config) throws ConfigurationException {
		_reference = config.getReference();
		if (SecurityObjectProviderManager.Module.INSTANCE.isActive()) {
			resolveReference(context, SecurityObjectProviderManager.getInstance());
		} else if (context instanceof SecurityObjectProviderContext) {
			((SecurityObjectProviderContext) context).resolveLater(this);
		} else {
			throw new IllegalStateException("SecurityObjectProviderManager not started.");
		}
	}

	void resolveReference(InstantiationContext context, SecurityObjectProviderManager manager) {
		if (manager.hasSecurityObjectProvider(_reference)) {
			try {
				_delegate = manager.getSecurityObjectProvider(_reference);
			} catch (ConfigurationException ex) {
				throw new ConfigurationError("Unable to get provider for '" + _reference + "'.", ex);
			}
		} else {
			context.error("Unknown reference '" + _reference + "' in SecurityObjectProviderManager.");
		}
	}


	@Override
	public BoundObject getSecurityObject(BoundChecker aChecker, Object model, BoundCommandGroup aCommandGroup) {
		if (_delegate == null) {
			throw new IllegalStateException("Unresolved reference '" + _reference + "'.");
		}
		return _delegate.getSecurityObject(aChecker, model, aCommandGroup);
	}

}

