/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.mig.html.provider.resource;

import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.ResourceProvider;

/**
 * Marker interface for {@link ResourceProvider} that don't create a
 * {@link #getLink(DisplayContext, Object) link}.
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
public interface INoLinkResourceProvider extends ResourceProvider {

	// Nothing needed in a marker interface.

}
