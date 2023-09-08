/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.dob;

import java.util.Set;

/**
 * The class {@link MOAlternative} provides many {@link MetaObject} which are
 * marked as specialisation.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public interface MOAlternative extends MetaObject {

	/**
	 * the set of {@link MetaObject} which are marked as specialisation
	 *         for this {@link MOAlternative}. Not <code>null</code>.
	 */
	Set<? extends MetaObject> getSpecialisations();

}

