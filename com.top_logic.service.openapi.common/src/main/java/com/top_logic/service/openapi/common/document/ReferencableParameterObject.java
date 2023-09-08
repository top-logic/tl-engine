/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

/**
 * {@link ParameterObject} that can be referenced by a
 * {@link ReferencableParameterObject#getReferenceName() reference name}.
 * 
 * @see ReferencingParameterObject
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface ReferencableParameterObject extends ParameterObject, ReferencableObject {

	// No special properties here

}

