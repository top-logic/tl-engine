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
 * Tests an ExcelWriter using Excel2003 (.xls) format.
 * 
 * @since 5.7.4
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class TestXLSExcelWriter extends AbstractExcelWriterTest {

	@Override
	protected ExcelWriter newExcelWriter() throws IOException {
		return new ExcelWriter(false);
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestXLSExcelWriter}.
	 */
	public static Test suite() {
		return suite(TestXLSExcelWriter.class);
	}

}
