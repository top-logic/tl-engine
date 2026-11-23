/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util;

import java.io.IOException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Label;
import com.top_logic.basic.module.BasicRuntimeModule;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ModuleException;
import com.top_logic.basic.module.ModuleUtil;
import com.top_logic.basic.module.ServiceCategory;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.module.category.RuntimeCategory;

/**
 * Delays the full application boot until it is explicitly triggered, redirecting incoming requests
 * to a waiting location while startup is still pending.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Label("Deferred boot")
@ServiceCategory.Of(RuntimeCategory.class)
public class DeferredBootService extends ManagedClass {

	/**
	 * Configuration option for the location where to redirect requests to, if the final system boot
	 * is still pending.
	 */
	public static final String DEFERRED_BOOT_LOCATION_PROPERTY = "deferredBootLocation";

	private AbstractStartStopListener _application;

	private String _deferredBootLocation;

	/**
	 * Configuration for {@link DeferredBootService}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends ServiceConfiguration<DeferredBootService> {
		/**
		 * Deferred boot location.
		 */
		String getDeferredBootLocation();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link DeferredBootService}.
	 */
	public DeferredBootService(InstantiationContext context, Config config) {
		super(context, config);

		_deferredBootLocation = config.getDeferredBootLocation();
	}

	@Override
	protected void shutDown() {
		_application = null;
		super.shutDown();
	}

	/**
	 * The {@link DeferredBootService} instance.
	 */
	public static DeferredBootService getInstance() {
		return Module.INSTANCE.getImplementationInstance();
	}

	/**
	 * Boot the system.
	 * 
	 * <p>
	 * A failure of the boot is handled by {@link AbstractStartStopListener#startFailed(Throwable)},
	 * which reports it and shuts the application down.
	 * </p>
	 * 
	 * @throws BootFailure
	 *         If the booting fails.
	 */
	public void boot() throws BootFailure {
		AbstractStartStopListener application = _application;
		try {
			application.boot();
		} catch (Throwable ex) {
			application.startFailed(ex);
			throw new BootFailure(ex);
		}

		ModuleUtil.INSTANCE.shutDown(Module.INSTANCE);
	}

	/**
	 * Starts the {@link DeferredBootService} that boots the given application on request.
	 * 
	 * @param application
	 *        The application to {@link AbstractStartStopListener#boot() boot}.
	 */
	static void startUp(AbstractStartStopListener application) throws ModuleException {
		ModuleUtil.INSTANCE.startUp(Module.INSTANCE);

		Module.INSTANCE.getImplementationInstance()._application = application;
	}

	/**
	 * {@link BasicRuntimeModule} for {@link DeferredBootService}.
	 * 
	 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
	 */
	public static class Module extends TypedRuntimeModule<DeferredBootService> {

		/**
		 * Singleton {@link DeferredBootService.Module} instance.
		 */
		public static final DeferredBootService.Module INSTANCE = new DeferredBootService.Module();

		@Override
		public Class<DeferredBootService> getImplementation() {
			return DeferredBootService.class;
		}

	}

	/**
	 * Redirection of requests during pending boot through
	 * {@link DeferredBootUtil#redirectOnPendingBoot(HttpServletRequest, HttpServletResponse)}.
	 */
	void redirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
		String deferredBootLocation = request.getContextPath() + _deferredBootLocation;
		response.sendRedirect(deferredBootLocation);
	}

}
