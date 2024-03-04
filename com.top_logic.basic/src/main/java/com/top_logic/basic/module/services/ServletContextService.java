/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.module.services;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import jakarta.servlet.ServletContext;

import com.top_logic.basic.Environment;
import com.top_logic.basic.Logger;
import com.top_logic.basic.core.workspace.Workspace;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.RestartException;
import com.top_logic.basic.tooling.ModuleLayoutConstants;
import com.top_logic.basic.version.Version;

/**
 * The class {@link ServletContextService} is a service which provides
 * informations about the context in which the application runs.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public final class ServletContextService extends ManagedClass {

	private final ServletContext context;

	private final File _application;

	ServletContextService(ServletContext ctx) throws ModuleException {
		if (ctx == null) {
			throw new ModuleException(("Given " + ServletContext.class.getSimpleName() + " must not be null."), ServletContextService.class);
		}
		this.context = ctx;
		String rootPath = context.getRealPath("/");
		if (rootPath == null) {
			rootPath = System.getProperty(Version.getApplicationName() + ".rootPath");
			if (rootPath == null) {
				throw new ModuleException("Can't determine RootPath", ServletContextService.class);
			}
		}
		File rootFile = new File(rootPath);
		if (!Environment.isDeployed()) {
			rootFile = new File(Workspace.projectDir(rootFile), ModuleLayoutConstants.WEBAPP_DIR);
		}
		try {
			_application = rootFile.getCanonicalFile();
		} catch (IOException ex) {
			throw new ModuleException("Can not detemine canonical path of " + rootFile, ex, ServletContextService.class);
		}
	}

	/**
	 * The applications root folder.
	 */
	public File getApplication() {
		return _application;
	}
	
	public Version getVersion() {
		return Version.getApplicationVersion();
	}

	public ServletContext getServletContext() {
		return context;
	}

	public static ServletContextService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Installs the given context and starts the {@link ServletContextService}.
	 * If it is already started it is restarted (including all depending
	 * services).
	 * 
	 * @param ctx
	 *        the new {@link ServletContext}
	 * @throws IllegalArgumentException
	 *         if the given {@link ServletContext} is <code>null</code>;
	 * @throws ModuleException
	 *         iff starting the {@link ServletContextService} fails (or in
	 *         restart case any dependent service)
	 */
	public static final void startUpServletContextService(final ServletContext ctx)
			throws IllegalArgumentException, ModuleException {
		if (ctx == null) {
			throw new IllegalArgumentException("The given " + ServletContext.class.getSimpleName() + " must not be null");
		}
		final Module module = Module.INSTANCE;
		if (module.isActive()) {
			ServletContext oldCtx = module.context;
			try {
				ModuleUtil.INSTANCE.restart(module, new Runnable() {
					@Override
					public void run() {
						module.init(ctx);
					}
				});
			} catch(RestartException ex) {
				Logger.error("Unable to restart '" + module + "'. Trying to reinstall instances with old properties.", ex, ServletContextService.class);
				ModuleUtil.INSTANCE.shutDown(module);
				module.init(oldCtx);
				for (BasicRuntimeModule<?> dependent: ex.getCurrentlyStartedDependents()) {
					ModuleUtil.INSTANCE.startUp(dependent);
				}
				throw ex;
			}
		} else {
			module.init(ctx);
			ModuleUtil.INSTANCE.startUp(module);
		}

	}

	public static final class Module extends BasicRuntimeModule<ServletContextService> {

		public static final ServletContextService.Module INSTANCE = new ServletContextService.Module();

		private static final Collection<? extends Class<? extends BasicRuntimeModule<?>>> DEPENDENCIES = NO_DEPENDENCIES;

		ServletContext context;

		@Override
		public Collection<? extends Class<? extends BasicRuntimeModule<?>>> getDependencies() {
			return DEPENDENCIES;
		}

		@Override
		public Class<ServletContextService> getImplementation() {
			return ServletContextService.class;
		}

		@Override
		protected ServletContextService newImplementationInstance() throws ModuleException {
			return new ServletContextService(context);
		}

		void init(ServletContext ctx) {
			this.context = ctx;
		}
	}

}
