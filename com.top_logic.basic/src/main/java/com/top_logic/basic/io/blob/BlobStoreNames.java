/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io.blob;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.basic.func.Function0;

/**
 * Option function listing the names of the {@link BlobStore}s configured in the
 * {@link BlobStoreService}.
 *
 * <p>
 * Used as option provider for configuration properties naming a blob store. If the
 * {@link BlobStoreService} is not active, there are no options.
 * </p>
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BlobStoreNames extends Function0<List<String>> {

	@Override
	public List<String> apply() {
		if (!BlobStoreService.Module.INSTANCE.isActive()) {
			return new ArrayList<>();
		}
		return new ArrayList<>(BlobStoreService.getInstance().getStores().keySet());
	}

}
