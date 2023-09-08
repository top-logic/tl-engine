/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.security;

import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.util.Resources;

/**
 * Mapping to get value for export of eventually protected values.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class ProtectedValueExportMapping extends AbstractProtectedValueMapping<Object> {

	/** Singleton {@link ProtectedValueExportMapping} instance. */
	public static final ProtectedValueExportMapping INSTANCE = new ProtectedValueExportMapping();

	private ProtectedValueExportMapping() {
		super(SimpleBoundCommandGroup.EXPORT);
	}

	@Override
	protected Object blockedValue(ProtectedValue value) {
		return ProtectedValueRenderer.getBlockedText(Resources.getInstance());
	}

	@Override
	protected Object handleUnprotected(Object input) {
		return input;
	}

}

