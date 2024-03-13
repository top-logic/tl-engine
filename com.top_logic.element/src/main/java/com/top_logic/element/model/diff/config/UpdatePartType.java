/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.model.TLTypePart;
import com.top_logic.model.config.TypeRefMandatory;

/**
 * Sets the {@link TLTypePart#getType() type} of a part.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@TagName("update-target-type")
public interface UpdatePartType extends PartUpdate, TypeRefMandatory {

	// Pure sum interface.

}
