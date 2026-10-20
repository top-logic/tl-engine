/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import com.top_logic.tl.ai.core.tools.web.FilteringProxy.Target;
import com.top_logic.tl.ai.core.tools.web.RecordingServer.Reply;

import junit.framework.TestCase;

/**
 * Reading long documents and PDFs with {@link WebFetchTool}: a document longer than one answer is
 * read in windows, through the {@code offset} each answer names, and none of it is lost on the way.
 *
 * <p>
 * The documents are served by a local fixture server under a name only the scripted lookup knows,
 * so nothing here touches the network. The PDFs are generated with PDFBox.
 * </p>
 */
public class TestWebFetchDocuments extends TestCase {

	/** A globally routable address, so the SSRF check has nothing to refuse. */
	private static final byte[] PUBLIC = { 93, (byte) 184, (byte) 216, 34 };

	private static final Pattern NEXT_OFFSET = Pattern.compile("offset=(\\d+)\\.\\]$");

	private static final String FILLER = "The committee reviewed the figures of the quarter and found "
		+ "them consistent with the plan. ";

	/**
	 * The acceptance case of the change: a 60-page PDF is read page by page through the offsets the
	 * answers name. Every page comes back exactly once and in order, every answer fits the window,
	 * the notes name the pages a window covers - and the document is downloaded once, not once per
	 * window.
	 */
	public void testA60PagePdfIsReadablePageByPageThroughOffsets() throws Exception {
		byte[] pdf = FixturePdf.of(60, page -> "Finding " + page + " of the report. " + FILLER.repeat(12));
		try (RecordingServer origin = RecordingServer.start(target -> Reply.binary("application/pdf", pdf));
				FilteringProxy egress = egressTo(origin)) {
			WebFetchTool tool = new WebFetchTool(publicAnswer(), egress);

			List<String> answers = readAll(tool, "http://fixture.invalid/report.pdf");

			String all = String.join("\n", answers);
			int previous = -1;
			for (int page = 1; page <= 60; page++) {
				String marker = "--- Page " + page + " of 60 ---";
				int at = all.indexOf(marker);
				assertTrue("page " + page + " was read", at >= 0);
				assertEquals("page " + page + " was read once", at, all.lastIndexOf(marker));
				assertTrue("page " + page + " comes after page " + (page - 1), at > previous);
				assertTrue("the text of page " + page + " was extracted", all.contains("Finding " + page + " of"));
				previous = at;
			}
			assertTrue("the document needs several windows: " + answers.size(), answers.size() >= 3);
			for (String answer : answers) {
				assertTrue("a window fits the default size: " + answer.length(),
					answer.length() < WebFetchTool.DEFAULT_MAX_CHARS + 500);
			}
			assertTrue("the first note names the pages: " + tail(answers.get(0)),
				answers.get(0).contains("PDF pages 1 to "));
			assertTrue("the last note says the document ends: " + tail(answers.get(answers.size() - 1)),
				answers.get(answers.size() - 1).endsWith("of 60. This is the end of the document.]"));
			assertEquals("reading on uses the text the first call read", 1, origin.contactCount());
		}
	}

	/**
	 * A page longer than one answer used to lose its middle: the tool cut it to its own cap, and the
	 * agent loop then kept only head and tail of that. Now the reader gets every part of it.
	 */
	public void testTheMiddleOfALongPageIsReachable() throws Exception {
		StringBuilder html = new StringBuilder("<html><head><title>Long</title></head><body>");
		for (int i = 0; i < 600; i++) {
			html.append("<p>Paragraph ").append(i).append(". ").append(FILLER).append("</p>");
		}
		html.append("</body></html>");
		try (RecordingServer origin = RecordingServer.serving(html.toString());
				FilteringProxy egress = egressTo(origin)) {
			WebFetchTool tool = new WebFetchTool(publicAnswer(), egress);

			List<String> answers = readAll(tool, "http://fixture.invalid/long");

			String all = String.join("\n", answers);
			for (int i = 0; i < 600; i++) {
				assertTrue("paragraph " + i + " was read", all.contains("Paragraph " + i + ". "));
			}
			assertTrue("the page needs several windows: " + answers.size(), answers.size() >= 3);
		}
	}

	/** A short page is answered whole, without a note about windows. */
	public void testAShortPageHasNoNote() throws Exception {
		try (RecordingServer origin = RecordingServer.serving("<html><body><p>short page</p></body></html>");
				FilteringProxy egress = egressTo(origin)) {
			String answer = new WebFetchTool(publicAnswer(), egress).webFetch("http://fixture.invalid/", null);

			assertTrue(answer, answer.contains("short page"));
			assertTrue("ends with the envelope: " + answer, answer.endsWith("</untrusted_page_content>"));
		}
	}

	/**
	 * A server that does not know the type of a PDF sends {@code application/octet-stream}; the file's
	 * first bytes decide then.
	 */
	public void testAPdfWithoutItsContentTypeIsRecognised() throws Exception {
		byte[] pdf = FixturePdf.of(2, page -> "Untyped page " + page + ".");
		try (RecordingServer origin = RecordingServer.start(
				target -> Reply.binary("application/octet-stream", pdf));
				FilteringProxy egress = egressTo(origin)) {
			String answer = new WebFetchTool(publicAnswer(), egress).webFetch("http://fixture.invalid/f", null);

			assertTrue(answer, answer.contains("Untyped page 2."));
		}
	}

	/** A PDF of scanned images has no text layer; the tool says so instead of answering nothing. */
	public void testAPdfWithoutTextSaysSo() throws Exception {
		byte[] pdf = FixturePdf.of(3, page -> "");
		try (RecordingServer origin = RecordingServer.start(target -> Reply.binary("application/pdf", pdf));
				FilteringProxy egress = egressTo(origin)) {
			String answer = new WebFetchTool(publicAnswer(), egress).webFetch("http://fixture.invalid/s.pdf", null);

			assertTrue(answer, answer.startsWith("Error: the PDF has 3 pages but no text layer"));
		}
	}

	/** Bytes that claim to be a PDF and are not are reported as such, not as an exception. */
	public void testABrokenPdfIsAClearError() throws Exception {
		byte[] broken = "%PDF-1.7 this is not a PDF".getBytes(java.nio.charset.StandardCharsets.US_ASCII);
		try (RecordingServer origin = RecordingServer.start(target -> Reply.binary("application/pdf", broken));
				FilteringProxy egress = egressTo(origin)) {
			String answer = new WebFetchTool(publicAnswer(), egress).webFetch("http://fixture.invalid/b.pdf", null);

			assertTrue(answer, answer.startsWith("Error: the file is not a readable PDF"));
		}
	}

	/**
	 * Reading on reads the text the first call read, even when the page changed since: an offset is a
	 * position in that text and would point anywhere in another one. A call from the start reads anew.
	 */
	public void testReadingOnUsesTheTextOfTheFirstCall() throws Exception {
		AtomicReference<String> live = new AtomicReference<>("<html><body><p>" + "old ".repeat(10_000)
			+ "</p></body></html>");
		try (RecordingServer origin = RecordingServer.start(target -> Reply.html(live.get()));
				FilteringProxy egress = egressTo(origin)) {
			WebFetchTool tool = new WebFetchTool(publicAnswer(), egress);
			int next = nextOffset(tool.webFetch("http://fixture.invalid/p", null));

			live.set("<html><body><p>" + "new ".repeat(10_000) + "</p></body></html>");
			String readOn = tool.webFetch("http://fixture.invalid/p", next);
			assertTrue("the second window is cut from the first text", readOn.contains("old old"));
			assertFalse(readOn.contains("new new"));
			assertEquals(1, origin.contactCount());

			assertTrue("a call from the start reads anew",
				tool.webFetch("http://fixture.invalid/p", null).contains("new new"));
			assertEquals(2, origin.contactCount());
		}
	}

	/**
	 * A document longer than one cache entry is cut to it with a note, and read on from the kept text:
	 * it used to be downloaded and extracted again for every window.
	 */
	public void testAVeryLongDocumentIsDownloadedOnce() throws Exception {
		String paragraph = FILLER.repeat(20) + "\n\n";
		String body = paragraph.repeat(FetchedDocuments.MAX_DOCUMENT_CHARS / paragraph.length() + 100);
		try (RecordingServer origin = RecordingServer.start(target -> new Reply(200, "OK",
				List.of("Content-Type: text/plain; charset=utf-8"), body));
				FilteringProxy egress = egressTo(origin)) {
			WebFetchTool tool = new WebFetchTool(publicAnswer(), egress);
			int next = nextOffset(tool.webFetch("http://fixture.invalid/long.txt", null));

			tool.webFetch("http://fixture.invalid/long.txt", next);
			String last = tool.webFetch("http://fixture.invalid/long.txt", FetchedDocuments.MAX_DOCUMENT_CHARS - 1_000);

			assertEquals("reading on uses the kept text", 1, origin.contactCount());
			assertTrue("the end says where the text is cut: " + tail(last),
				last.contains("[The text of this document is cut here: this application reads at most "
					+ FetchedDocuments.MAX_DOCUMENT_CHARS + " characters of one document.]"));
		}
	}

	/** Of a PDF with more pages than are read, the text stops with a note naming the last page read. */
	public void testAPdfIsReadUpToItsPageBound() throws Exception {
		byte[] pdf = FixturePdf.of(12, page -> "Finding " + page + " of the report.");

		FetchedDocument document = PdfText.extract("https://example.com/r.pdf", pdf, 10, 1_000_000);

		assertTrue(document.text().contains("--- Page 10 of 12 ---\n\nFinding 10 of the report."));
		assertFalse(document.text().contains("Page 11 of 12"));
		assertTrue(document.text(), document.text().endsWith("[The text of this document stops here, after page 10"
			+ " of 12: this application reads at most 10 pages and 1000000 characters of one PDF.]"));
		assertEquals("the document still has 12 pages", 12, document.pageCount());
		assertEquals(10, document.pageStarts().size());
	}

	/** Of a PDF with more text than is read, the text stops after the page that reached the bound. */
	public void testAPdfIsReadUpToItsCharacterBound() throws Exception {
		byte[] pdf = FixturePdf.of(20, page -> "Finding " + page + " of the report. " + FILLER.repeat(10));

		FetchedDocument document = PdfText.extract("https://example.com/r.pdf", pdf, 2_000, 3_000);

		int pages = document.pageStarts().size();
		assertTrue("stopped early: " + pages, pages > 1 && pages < 20);
		assertTrue(document.text().contains("after page " + pages + " of 20"));
		assertTrue("the page that reached the bound is whole", document.text().contains("Finding " + pages + " of"));
	}

	/** A page without text keeps its marker, between pages and at the end, so the page numbers hold. */
	public void testAPageWithoutTextKeepsItsMarker() throws Exception {
		byte[] pdf = FixturePdf.of(5, page -> page == 2 || page == 3 || page == 5 ? "" : "Finding " + page + ".");

		String text = PdfText.extract("https://example.com/r.pdf", pdf).text();

		assertEquals("--- Page 1 of 5 ---\n\nFinding 1.\n\n--- Page 2 of 5 ---\n\n\n\n--- Page 3 of 5 ---\n\n"
			+ "\n\n--- Page 4 of 5 ---\n\nFinding 4.\n\n--- Page 5 of 5 ---\n\n", text);
	}

	/** An offset past the end says how long the document is. */
	public void testAnOffsetPastTheEndIsAClearError() {
		String answer = WebFetchTool.renderWindow("https://example.com/a",
			new FetchedDocument("https://example.com/a", "", "x".repeat(100)), 100, 50);

		assertEquals("Error: offset 100 is at or past the end of the document from https://example.com/a, "
			+ "which has 100 characters.", answer);
	}

	/** A negative offset is refused before anything is fetched. */
	public void testANegativeOffsetIsRefused() {
		assertEquals("Error: offset must be 0 or more.", new WebFetchTool().webFetch("https://example.com/", -1));
	}

	/** A window ends at a paragraph end near its limit, not inside a word. */
	public void testAWindowEndsAtAParagraph() {
		String text = "a".repeat(90) + "\n\n" + "b".repeat(100);

		assertEquals("after the paragraph break", 92, WebFetchTool.windowEnd(text, 0, 100));
		assertEquals("at the end of a text that fits", text.length(), WebFetchTool.windowEnd(text, 92, 200));
		assertEquals("hard, when no break is near", 50, WebFetchTool.windowEnd("c".repeat(100), 0, 50));
	}

	/** Reads a document window by window, following the offset each answer names. */
	private static List<String> readAll(WebFetchTool tool, String url) {
		List<String> answers = new ArrayList<>();
		Integer offset = null;
		for (int call = 0; call < 100; call++) {
			String answer = tool.webFetch(url, offset);
			assertFalse(answer, answer.startsWith("Error"));
			answers.add(answer);
			int next = nextOffset(answer);
			if (next < 0) {
				return answers;
			}
			offset = next;
		}
		throw new AssertionError("The document did not end after 100 windows.");
	}

	/** The offset an answer names for reading on, or {@code -1} when it names none. */
	private static int nextOffset(String answer) {
		Matcher matcher = NEXT_OFFSET.matcher(answer);
		return matcher.find() ? Integer.parseInt(matcher.group(1)) : -1;
	}

	private static String tail(String answer) {
		return answer.substring(Math.max(0, answer.length() - 200));
	}

	private static UrlSafety.NameLookup publicAnswer() {
		return name -> new InetAddress[] { InetAddress.getByAddress(name, PUBLIC) };
	}

	/** A proxy that pins every destination to the local fixture server, as the test seam allows. */
	private static FilteringProxy egressTo(RecordingServer origin) {
		return FilteringProxy.start(host -> null,
			(host, port) -> Target.to(new InetSocketAddress(InetAddress.getLoopbackAddress(), origin.port())));
	}
}
