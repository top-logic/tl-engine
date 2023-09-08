/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.form.util;

import java.util.Collection;

import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.func.Function0;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.layout.LabelProvider;
import com.top_logic.layout.admin.component.TLServiceResourceProvider;
import com.top_logic.layout.form.values.edit.OptionMapping;
import com.top_logic.layout.provider.AbstractMappingResourceProviderBase;

/**
 * Utilities for the module system for the form hierarchy.
 * 
 * @implNote As the module system is defined in another eclipse module, the form utilities are
 *           defined here.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ModuleSystemFormUtils {

	/**
	 * {@link Function0} returning all known service modules.
	 * 
	 * @see ModuleUtil#getAllModules()
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class AllModules extends Function0<Collection<BasicRuntimeModule<?>>> {

		@Override
		public Collection<BasicRuntimeModule<?>> apply() {
			return ModuleUtil.getAllModules();
		}

	}

	/**
	 * {@link OptionMapping} that maps a {@link BasicRuntimeModule} to its class and vice-versa.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ModuleClassMapping implements OptionMapping {

		@Override
		public Object toSelection(Object option) {
			if (option == null) {
				return null;
			}
			return ((BasicRuntimeModule<?>) option).getClass();
		}

		@Override
		public Object asOption(Iterable<?> allOptions, Object selection) {
			if (selection == null) {
				return null;
			}
			try {
				return ModuleUtil.moduleByClass((Class<? extends BasicRuntimeModule<?>>) selection);
			} catch (ConfigurationException ex) {
				throw new ConfigurationError(ex);
			}
		}
	}

	/**
	 * {@link LabelProvider} for {@link BasicRuntimeModule}s.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class ModuleLabel extends AbstractMappingResourceProviderBase {

		/**
		 * Creates a new {@link ModuleLabel}.
		 */
		public ModuleLabel() {
			super(TLServiceResourceProvider.INSTANCE);
		}

		@Override
		protected Object mapValue(Object anObject) {
			if (anObject instanceof BasicRuntimeModule<?>) {
				return ((BasicRuntimeModule<?>) anObject).getImplementation();
			}
			return anObject;
		}

	}

}

