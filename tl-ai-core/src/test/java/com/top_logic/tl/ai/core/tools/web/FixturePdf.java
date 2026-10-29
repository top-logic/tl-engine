/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;

/**
 * Generates the PDFs the {@code webFetch} tests read, so that no test needs a file or the network.
 */
final class FixturePdf {

	private FixturePdf() {
		// Static utility.
	}

	/** The text of one page of a generated PDF. */
	interface PageText {

		/** The text of page {@code page}, counted from 1; empty for a page without text. */
		String of(int page);
	}

	/**
	 * A PDF of {@code pages} pages, each holding the text {@code text} gives for it, in lines of at
	 * most 95 characters - at most about 5,500 characters a page.
	 */
	static byte[] of(int pages, PageText text) throws IOException {
		PDType1Font font = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
		try (PDDocument document = new PDDocument()) {
			for (int page = 1; page <= pages; page++) {
				PDPage pdPage = new PDPage();
				document.addPage(pdPage);
				String content = text.of(page);
				if (content.isEmpty()) {
					continue;
				}
				try (PDPageContentStream stream = new PDPageContentStream(document, pdPage)) {
					stream.beginText();
					stream.setFont(font, 10);
					stream.setLeading(12);
					stream.newLineAtOffset(40, 760);
					for (String line : lines(content, 95)) {
						stream.showText(line);
						stream.newLine();
					}
					stream.endText();
				}
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			document.save(out);
			return out.toByteArray();
		}
	}

	/** Breaks {@code text} into lines of at most {@code width} characters, at spaces. */
	private static List<String> lines(String text, int width) {
		List<String> lines = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		for (String word : text.split(" ")) {
			if (line.length() + word.length() + 1 > width) {
				lines.add(line.toString());
				line.setLength(0);
			}
			if (line.length() > 0) {
				line.append(' ');
			}
			line.append(word);
		}
		lines.add(line.toString());
		return lines;
	}
}
