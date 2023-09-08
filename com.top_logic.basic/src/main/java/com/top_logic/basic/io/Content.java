/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.io;

import com.top_logic.basic.Named;
import com.top_logic.basic.io.character.CharacterContent;

/**
 * Abstract base interface for input providers.
 * 
 * <p>
 * This interface is not intended to be implemented directly outside the framework. All
 * implementations must either implement {@link BinaryContent}, or {@link CharacterContent}.
 * </p>
 * 
 * @see BinaryContent
 * @see CharacterContent
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface Content extends Named {

	/** Constant to be used if some content has no name. */
	String NO_NAME = "[no name]";

	/**
	 * A name for the content, for debugging purpose.
	 * <p>
	 * The name should tell the developer where the content is from. For example a path or an url.
	 * </p>
	 */
	@Override
	public String toString();

	/**
	 * The (file, resource, url,...) name of this content.
	 */
	@Override
	default String getName() {
		return toString();
	}

}
