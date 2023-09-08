/*
 * SPDX-FileCopyrightText: 2011 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge;

import com.top_logic.dob.meta.MOReference;

/**
 * Extension of {@link MOReference} which provides methods internally used by the framework.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface MOReferenceInternal extends MOReference {

	@Override
	KIReferenceStorage getStorage();

}

