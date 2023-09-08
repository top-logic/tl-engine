/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLClass;

/**
 * {@link ClassUpdate} making a {@link TLClass} concrete.
 */
@TagName("make-concrete")
public interface MakeConcrete extends ClassUpdate {
	// Pure marker.
}
