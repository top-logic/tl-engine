/*
 * SPDX-FileCopyrightText: 2002 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.MapBinding;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.TypedRuntimeModule;

/**
 * This class allows a Reloadable Configuration of the default CachePolicy used by most JSP Pages and Servlets.
 * 
 * Th Cacheploicy tells the UserAgent (vugo Browser) whwn to relaod/cache pages.
 * 
 * Use the top-logix.xml to configure this class, It can be reloaded
 * an this way cchange in a running system.
 * 
 * @author <a href="mailto:klaus.halfmann@top-logic.com">Klaus Halfmann</a>
 */
public class CachePolicy extends ConfiguredManagedClass<CachePolicy.Config> {

	/**
	 * Configuration for {@link CachePolicy}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<CachePolicy> {
		/**
		 * Headers. See {@link Config#getHeaders}.
		 */
		String HEADERS = "headers";

		/** Getter for {@link Config#HEADERS}. */
		@Name(HEADERS)
		@MapBinding(tag = "header", key = "name", attribute = "value")
		Map<String, String> getHeaders();
	}
	
	/**
	 * Creates a new {@link CachePolicy}.
	 */
	public CachePolicy(InstantiationContext context, Config config) {
		super(context, config);
	}

	/**
	 * Actual worker function that sets the Headers in a Response.
	 * 
	 * It just takes alle Propeties annd put them into the Header.
	 */
	public void setCachePolicy(HttpServletResponse aResponse) {
		Map<String, String> headers = getConfig().getHeaders();
		for (Entry<String, String> entry : headers.entrySet()) {
			String name = entry.getKey();
			String value = entry.getValue();
			aResponse.setHeader(name, value);
		}
    }

	/**
	 * Gets the unique instance of the class.
	 */
    public static synchronized CachePolicy getInstance () {
		return Module.INSTANCE.getImplementationInstance();
    }

	/**
	 * Module for instantiation of the {@link CachePolicy}.
	 */
	public static class Module extends TypedRuntimeModule<CachePolicy> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<CachePolicy> getImplementation() {
			return CachePolicy.class;
		}

	}

}
