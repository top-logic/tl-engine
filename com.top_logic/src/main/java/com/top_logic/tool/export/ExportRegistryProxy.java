/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.export;

/**
 * Proxy for an {@link ExportRegistry}.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public abstract class ExportRegistryProxy implements ExportRegistry {

	@Override
	public Export getExport(String aHandlerID, Object aModel) {
		return impl().getExport(aHandlerID, aModel);
	}

	@Override
	public Export getNextRunningExport(String aTechnology, String aDuration) throws ExportFailure {
		return impl().getNextRunningExport(aTechnology, aDuration);
	}

	@Override
	public void startup() throws ExportFailure {
		impl().startup();
	}

	@Override
	public void shutdown() {
		impl().shutdown();
	}

	/** 
	 * Returns the {@link ExportRegistry} to delegate to.
	 */
	protected abstract ExportRegistry impl();

}

