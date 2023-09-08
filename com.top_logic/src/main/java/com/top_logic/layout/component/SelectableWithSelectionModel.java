/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.component;

import com.top_logic.mig.html.SelectionModelOwner;

/**
 * Marker interface for implementations of both, {@link Selectable} and {@link SelectionModelOwner}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface SelectableWithSelectionModel extends Selectable, SelectionModelOwner {

	// Sum interface

}

