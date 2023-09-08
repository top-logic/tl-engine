/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office.excel.streaming;

import java.io.IOException;

import junit.framework.Test;

import com.top_logic.base.office.excel.streaming.ExcelWriter;

/**
 * Tests an ExcelWriter using Excel2007 (.xlsx) format.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestXLSXExcelWriter extends AbstractExcelWriterTest {

	@Override
	protected ExcelWriter newExcelWriter() throws IOException {
		return new ExcelWriter(true);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestXLSXExcelWriter}.
	 */
	public static Test suite() {
		return suite(TestXLSXExcelWriter.class);
	}

}
