/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.model.diff.config;

import com.top_logic.basic.config.annotation.Abstract;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.model.TLModelPart;
import com.top_logic.model.annotate.TLAnnotation;

/**
 * Base properties for modifying a model part.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
@Abstract
public interface PartUpdate extends Update {

	/**
	 * The {@link TLModelPart} to remove a {@link TLAnnotation} from.
	 */
	@Mandatory
	String getPart();

	/** @see #getPart() */
	void setPart(String value);

}
