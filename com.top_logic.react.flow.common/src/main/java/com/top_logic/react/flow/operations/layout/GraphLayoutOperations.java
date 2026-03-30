/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations.layout;

import com.top_logic.react.flow.data.GraphLayout;

/**
 * Operations for a {@link GraphLayout}.
 */
public interface GraphLayoutOperations extends FloatingLayoutOperations {

	@Override
	GraphLayout self();

}
