/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.base.office.excel;

import java.io.IOException;
import java.io.InputStream;

import junit.framework.Test;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;

import test.com.top_logic.TLTestSetup;
import test.com.top_logic.basic.BasicTestCase;

import com.top_logic.base.office.excel.POIStyleManager;

/**
 * Test for {@link POIStyleManager}.
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@SuppressWarnings("javadoc")
public class TestPOIStyleManager extends BasicTestCase {

	private static final String FILE_NAME = "TestPOIStyleManager.xlsx";

	private POIStyleManager _poiStyleManager;

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		_poiStyleManager = new POIStyleManager(open());
	}

	public void testStyleColor() {
		CellStyle newStyle = _poiStyleManager.newStyle();
		newStyle.setFillBackgroundColor(IndexedColors.BLUE.index);
		assertEquals(IndexedColors.BLUE.index, newStyle.getFillBackgroundColor());
		assertNotNull(newStyle.getFillBackgroundColorColor());
		newStyle.setFillForegroundColor(IndexedColors.GREEN.index);
		assertEquals(IndexedColors.GREEN.index, newStyle.getFillForegroundColor());
		assertNotNull(newStyle.getFillForegroundColorColor());
	}

	/**
	 * Load the excel file containing test cases.
	 * 
	 * @return the loaded {@link Workbook}
	 * @throws IOException
	 *         if an error occurred while reading the file
	 * @throws InvalidFormatException
	 *         if the file is malformed
	 */
	private Workbook open() throws IOException, InvalidFormatException {
		final InputStream stream = getClass().getResourceAsStream(FILE_NAME);
		try {
			return WorkbookFactory.create(stream);
		} finally {
			stream.close();
		}
	}

	/**
	 * a cumulative {@link Test} for all Tests in {@link TestPOIStyleManager}.
	 */
	public static Test suite() {
		return TLTestSetup.createTLTestSetup(TestPOIStyleManager.class);
	}

}

