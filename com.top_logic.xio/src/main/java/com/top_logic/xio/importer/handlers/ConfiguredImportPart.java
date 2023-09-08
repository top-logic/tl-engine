/*
 * SPDX-FileCopyrightText: 2021 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.xio.importer.handlers;

import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.Location;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.util.ResKey;

/**
 * {@link ImportPart} defined in a configuration file.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public interface ConfiguredImportPart<C extends PolymorphicConfiguration<?>> extends ConfiguredInstance<C>, ImportPart {

	@Override
	default ResKey location() {
		Location location = getConfig().location();
		return I18NConstants.IMPORT_SPEC__FILE_LINE_COL.fill(location.getResource(), location.getLine(),
			location.getColumn());
	}

}
