/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.tree;

import com.top_logic.basic.config.annotation.Label;
import com.top_logic.layout.react.control.ModelKeyArguments;

/**
 * Arguments of the {@link ReactTreeControl} command expanding the node whose business object has
 * the given {@link #getKey() identity}.
 *
 * <p>
 * The key is the identity the headless projection puts on each node, so an agent copies a node's
 * {@code key} verbatim into this command.
 * </p>
 */
@Label("Expand node {key}")
public interface ExpandNodeByKeyArguments extends ModelKeyArguments {

	// The key is all the command needs.

}
