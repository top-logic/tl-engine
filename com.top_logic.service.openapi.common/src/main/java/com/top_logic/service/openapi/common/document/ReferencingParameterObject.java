/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.annotation.Final;

/**
 * {@link IParameterObject} that references a {@link ReferencableParameterObject}.
 *
 * @see ReferencableParameterObject
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Final
public interface ReferencingParameterObject extends IParameterObject, ReferencingObject {

	// No special properties here
}

