/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.config.container;

import com.top_logic.basic.annotation.FrameworkInternal;
import com.top_logic.basic.config.ConfigurationItem;

/**
 * The internal api of {@link ConfigPart} for setting the {@link ConfigPart#container() container.}
 * 
 * @author <a href="mailto:jst@top-logic.com">Jan Stolzenburg</a>
 */
@FrameworkInternal
public interface ConfigPartInternal extends ConfigPart {

	/** Framework-internal setter for {@link ConfigPart#container()} reference. */
	void updateContainer(ConfigurationItem value);

}
