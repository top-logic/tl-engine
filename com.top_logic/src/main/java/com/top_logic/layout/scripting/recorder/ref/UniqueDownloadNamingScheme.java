/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.recorder.ref;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.layout.WindowScope;
import com.top_logic.layout.scripting.runtime.ActionContext;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;
import com.top_logic.layout.structure.BrowserWindowControl;

/**
 * {@link ModelNamingScheme} for {@link UniqueDownload}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class UniqueDownloadNamingScheme extends AbstractModelNamingScheme<BinaryDataSource, UniqueDownload> {

	@Override
	protected void initName(UniqueDownload name, BinaryDataSource model) {
		// no initialisation
	}

	@Override
	public BinaryDataSource locateModel(ActionContext context, UniqueDownload name) {
		WindowScope windowScope = context.getDisplayContext().getWindowScope();
		BinaryDataSource result =
			BrowserWindowControl.Internal.getUniqueDownload((BrowserWindowControl) windowScope);
		ApplicationAssertions.assertNotNull(name, "Download is not available.", result);
		return result;
	}

	@Override
	public Class<? extends UniqueDownload> getNameClass() {
		return UniqueDownload.class;
	}

	@Override
	public Class<BinaryDataSource> getModelClass() {
		return BinaryDataSource.class;
	}

	@Override
	protected boolean isCompatibleModel(BinaryDataSource model) {
		return false;
	}

}

