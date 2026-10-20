/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Extracts the text of a PDF for {@link WebFetchTool}, page by page.
 *
 * <p>
 * Each page starts with a marker line {@code --- Page 3 of 60 ---}, and the offset of each marker is
 * kept, so that an answer can say which pages a window of the text covers. Only the text layer is
 * read: a scanned PDF without one gives no text, and the tool says so. The file comes from the web,
 * so what one extraction costs is bounded: its pages, its characters and the memory of the parser.
 * </p>
 */
final class PdfText {

	/** The first bytes of every PDF file. */
	private static final byte[] MAGIC = { '%', 'P', 'D', 'F', '-' };

	private PdfText() {
		// Static utility.
	}

	/**
	 * Whether a body with the given content type and first bytes is a PDF.
	 *
	 * <p>
	 * A server that does not know the type sends {@code application/octet-stream} or nothing, so
	 * then the first bytes decide.
	 * </p>
	 */
	static boolean isPdf(String contentType, byte[] body) {
		if (contentType.contains("application/pdf") || contentType.contains("application/x-pdf")) {
			return true;
		}
		boolean unknownType = contentType.isEmpty() || contentType.contains("octet-stream");
		return unknownType && startsWithMagic(body);
	}

	private static boolean startsWithMagic(byte[] body) {
		if (body.length < MAGIC.length) {
			return false;
		}
		for (int i = 0; i < MAGIC.length; i++) {
			if (body[i] != MAGIC[i]) {
				return false;
			}
		}
		return true;
	}

	/**
	 * The most pages of one PDF whose text is extracted.
	 *
	 * <p>
	 * A PDF comes from the web, and its size says little about its work: a file of a few megabytes can
	 * have tens of thousands of pages. Together with the bound on the characters, this bounds what one
	 * fetch costs.
	 * </p>
	 */
	static final int MAX_PAGES = 2_000;

	/** The memory the parser keeps decoded streams in before it moves them to a temporary file. */
	private static final long MAX_MAIN_MEMORY = 16L * 1024 * 1024;

	/**
	 * The most memory and temporary file space the parser uses for decoded streams together; a PDF
	 * that needs more is not read. Compressed streams can expand far beyond the size of the file.
	 */
	private static final long MAX_STORAGE = 256L * 1024 * 1024;

	/**
	 * Extracts the text of a PDF, at most {@link #MAX_PAGES} pages and
	 * {@link FetchedDocuments#MAX_DOCUMENT_CHARS} characters of it.
	 *
	 * @param url
	 *        The address the PDF was read from.
	 * @param body
	 *        The complete file.
	 * @return The document with one page marker per page.
	 * @throws PdfException
	 *         When the file cannot be read as a PDF, needs a password, or holds no text.
	 */
	static FetchedDocument extract(String url, byte[] body) throws PdfException {
		return extract(url, body, MAX_PAGES, FetchedDocuments.MAX_DOCUMENT_CHARS);
	}

	/**
	 * Extracts the text of a PDF in one pass over its pages, up to the given bounds.
	 *
	 * <p>
	 * Extraction stops after {@code maxPages} pages, or after the page at which the text reached
	 * {@code maxChars} characters; the text then ends with a note that says after which page it stops.
	 * Package-visible for testing.
	 * </p>
	 */
	static FetchedDocument extract(String url, byte[] body, int maxPages, int maxChars) throws PdfException {
		try (PDDocument document = Loader.loadPDF(body, "", null, null,
				MemoryUsageSetting.setupMixed(MAX_MAIN_MEMORY, MAX_STORAGE).streamCache)) {
			int pageCount = document.getNumberOfPages();
			PageCollector pages = new PageCollector(pageCount, maxChars);
			pages.setSortByPosition(true);
			pages.setEndPage(Math.min(pageCount, maxPages));
			pages.writeText(document, pages.output());
			int extracted = pages.finish();
			if (!pages.anyText()) {
				throw new PdfException("the PDF has " + pageCount
					+ " pages but no text layer (probably scanned images); cannot extract text.");
			}
			StringBuilder text = pages.text();
			if (extracted < pageCount) {
				text.append("\n\n[The text of this document stops here, after page ").append(extracted)
					.append(" of ").append(pageCount).append(": this application reads at most ")
					.append(maxPages).append(" pages and ").append(maxChars).append(" characters of one PDF.]");
			}
			return new FetchedDocument(url, titleOf(document), text.toString(), List.copyOf(pages.pageStarts()),
				List.of(), pageCount);
		} catch (InvalidPasswordException ex) {
			throw new PdfException("the PDF is password-protected; cannot extract text.");
		} catch (IOException ex) {
			throw new PdfException("the file is not a readable PDF: " + ex.getMessage());
		}
	}

	/**
	 * A text stripper that collects the text page by page, with a marker line in front of each page.
	 *
	 * <p>
	 * One pass: a stripper asked for one page at a time walks the page tree from the start for every
	 * page. The stripper writes a page's text to {@link #output()}; {@link #endPage(PDPage)} moves it
	 * to the collected text. A page without content is never processed, so the markers of the pages
	 * before a processed page are written when it ends, and those of the last pages by
	 * {@link #finish()}.
	 * </p>
	 */
	private static final class PageCollector extends PDFTextStripper {

		private final int _pageCount;

		private final int _maxChars;

		private final StringWriter _output = new StringWriter();

		private final StringBuilder _text = new StringBuilder();

		private final List<Integer> _pageStarts = new ArrayList<>();

		private boolean _anyText;

		PageCollector(int pageCount, int maxChars) {
			_pageCount = pageCount;
			_maxChars = maxChars;
		}

		/** Where the stripper writes the text of the page it processes. */
		Writer output() {
			return _output;
		}

		@Override
		protected void endPage(PDPage page) throws IOException {
			super.endPage(page);
			int number = getCurrentPageNo();
			addEmptyPagesBefore(number);
			StringBuffer written = _output.getBuffer();
			addPage(number, written.toString().strip());
			written.setLength(0);
			if (_text.length() >= _maxChars) {
				// The pages after this one are not processed.
				setEndPage(number);
			}
		}

		/**
		 * Adds the markers of the pages without content after the last processed one.
		 *
		 * @return The number of the last page whose text was extracted.
		 */
		int finish() {
			int last = Math.min(_pageCount, getEndPage());
			addEmptyPagesBefore(last + 1);
			return last;
		}

		private void addEmptyPagesBefore(int page) {
			while (_pageStarts.size() < page - 1) {
				addPage(_pageStarts.size() + 1, "");
			}
		}

		private void addPage(int page, String pageText) {
			_anyText |= !pageText.isEmpty();
			if (page > 1) {
				_text.append("\n\n");
			}
			_pageStarts.add(_text.length());
			_text.append("--- Page ").append(page).append(" of ").append(_pageCount).append(" ---\n\n");
			_text.append(pageText);
		}

		StringBuilder text() {
			return _text;
		}

		List<Integer> pageStarts() {
			return _pageStarts;
		}

		boolean anyText() {
			return _anyText;
		}
	}

	private static String titleOf(PDDocument document) {
		PDDocumentInformation info = document.getDocumentInformation();
		String title = info == null ? null : info.getTitle();
		return title == null ? "" : title.strip();
	}

	/**
	 * A PDF {@link PdfText} cannot read; the message is the tool's sentence to the model.
	 */
	static final class PdfException extends Exception {

		PdfException(String message) {
			super(message);
		}
	}
}
