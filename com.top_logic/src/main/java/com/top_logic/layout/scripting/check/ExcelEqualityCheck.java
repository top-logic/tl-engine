/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.scripting.check;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import com.top_logic.base.office.OfficeException;
import com.top_logic.base.office.excel.ExcelAccess;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.scripting.runtime.action.ApplicationAssertions;

/**
 * Checks whether the actual Excel file is equal to the expected file based on its textual contents.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public final class ExcelEqualityCheck extends AbstractFileEqualityCheck {

	/**
	 * Creates a {@link ExcelEqualityCheck} from configuration.
	 */
	public ExcelEqualityCheck(InstantiationContext context, FileMatchConfig config) {
		super(context, config);
	}

	@Override
	protected void checkContents(BinaryData actual, BinaryData expected) throws IOException {
		try {
			Map<?, ?> expectedValues = parse(expected);
			Map<?, ?> actualValues = parse(actual);

			ApplicationAssertions.assertMapEquals(_config, "Missmatch in content.", expectedValues, actualValues);
		} catch (OfficeException ex) {
			ApplicationAssertions.fail(_config, "Cannot parse Excel format.", ex);
		}
	}

	private Map<?, ?> parse(BinaryData data) throws OfficeException, IOException {
		InputStream in = data.getStream();
		try {
			return ExcelAccess.newInstance().getValues(in);
		} finally {
			in.close();
		}
	}

}
