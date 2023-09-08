/*
 * SPDX-FileCopyrightText: 2022 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.common.document;

import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.annotation.Label;

/**
 * Common super interface for parameters in an {@link OpenapiDocument}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@Label("Parameter object")
public interface IParameterObject extends ConfigurationItem {

	// Marker interface.

}

