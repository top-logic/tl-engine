/*
 * SPDX-FileCopyrightText: 2018 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.layout.meta;

import static com.top_logic.basic.shared.collection.factory.CollectionFactoryShared.*;
import static java.util.Comparator.*;

import java.util.List;

import com.top_logic.basic.format.configured.Formatter.Config;
import com.top_logic.basic.format.configured.FormatterService;
import com.top_logic.basic.func.Function0;
import com.top_logic.layout.form.values.edit.annotation.Options;

/**
 * {@link Options Option provider} for the
 * {@link com.top_logic.basic.format.configured.Formatter.Config#getFormats() configured formats}.
 * 
 * @author <a href="mailto:msi@top-logic.com">msi</a>
 */
public class FormatOptions extends Function0<List<String>> {

	@Override
	public List<String> apply() {
		Config config = FormatterService.getInstance().getConfig();
		List<String> list = list(config.getFormats().keySet());
		list.sort(naturalOrder());
		return list;
	}

}
