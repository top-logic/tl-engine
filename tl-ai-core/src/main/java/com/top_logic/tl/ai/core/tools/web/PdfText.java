/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDDocumentInformation;
import org.apache.pdfbox.pdmodel.encryption.InvalidPasswordException;
import org.apache.pdfbox.text.PDFTextStripper;

/**
 * Extracts the text of a PDF for {@link WebFetchTool}, page by page.
 *
 * <p>
 * Each page starts with a marker line {@code --- Page 3 of 60 ---}, and the offset of each marker is
 * kept, so that an answer can say which pages a window of the text covers. Only the text layer is
 * read: a scanned PDF without one gives no text, and the tool says so.
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
	 * Extracts the text of a PDF.
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
		try (PDDocument document = Loader.loadPDF(body)) {
			int pageCount = document.getNumberOfPages();
			PDFTextStripper stripper = new PDFTextStripper();
			stripper.setSortByPosition(true);
			StringBuilder text = new StringBuilder();
			List<Integer> pageStarts = new ArrayList<>(pageCount);
			boolean anyText = false;
			for (int page = 1; page <= pageCount; page++) {
				stripper.setStartPage(page);
				stripper.setEndPage(page);
				String pageText = stripper.getText(document).strip();
				anyText |= !pageText.isEmpty();
				if (page > 1) {
					text.append("\n\n");
				}
				pageStarts.add(text.length());
				text.append("--- Page ").append(page).append(" of ").append(pageCount).append(" ---\n\n");
				text.append(pageText);
			}
			if (!anyText) {
				throw new PdfException("the PDF has " + pageCount
					+ " pages but no text layer (probably scanned images); cannot extract text.");
			}
			return new FetchedDocument(url, titleOf(document), text.toString(), List.copyOf(pageStarts));
		} catch (InvalidPasswordException ex) {
			throw new PdfException("the PDF is password-protected; cannot extract text.");
		} catch (IOException ex) {
			throw new PdfException("the file is not a readable PDF: " + ex.getMessage());
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
