/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.channel.linking.ref;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.mig.html.layout.ComponentReference;

/**
 * A component resolved by component name.
 */
@TagName("target")
public interface NamedComponent extends ComponentRef, ComponentReference {

	// sum interface

}