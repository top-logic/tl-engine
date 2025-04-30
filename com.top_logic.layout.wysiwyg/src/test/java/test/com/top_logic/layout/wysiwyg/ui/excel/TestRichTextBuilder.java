/*
 * SPDX-FileCopyrightText: 2025 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.wysiwyg.ui.excel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import junit.framework.TestCase;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Workbook;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Node;

import com.top_logic.layout.wysiwyg.ui.excel.RichTextBuilder;

/**
 * Test case fir für {@link RichTextBuilder}
 */
@SuppressWarnings("javadoc")
public class TestRichTextBuilder extends TestCase {

	public void testConvert() {
		Workbook workbook = new HSSFWorkbook();
		RichTextBuilder builder = new RichTextBuilder(workbook);
		Node document = Jsoup.parse("<p>Some <b>bold <i>and</i> funny</b> words.</p> <p>And another paragraph.</p>");
		builder.append(document);
		RichTextString text = builder.getText();

		//                 v    v  v     v   
		//            000000000011111111113333333
		//            012345678901234567890123456 7 8901234567890012345678
		assertEquals("Some bold and funny words.\n\nAnd another paragraph.\n", text.toString());
		assertEquals(Arrays.asList(5, 10, 13, 19), runs(text));
	}

	private static List<Integer> runs(RichTextString text) {
		List<Integer> runs = new ArrayList<>();
		for (int n = 0, cnt = text.numFormattingRuns(); n < cnt; n++) {
			int run = text.getIndexOfFormattingRun(n);
			runs.add(Integer.valueOf(run));
		}
		return runs;
	}

}
