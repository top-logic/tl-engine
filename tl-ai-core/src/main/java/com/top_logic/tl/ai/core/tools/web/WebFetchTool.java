/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tl.ai.core.tools.web;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import com.top_logic.basic.Logger;
import com.top_logic.tl.ai.core.scripting.Converters;
import com.top_logic.tl.ai.tool.api.AnswerMarking;
import com.top_logic.tl.ai.tool.api.P;
import com.top_logic.tl.ai.tool.api.ToolGroups;
import com.top_logic.tl.ai.tool.api.ToolMethod;
import com.top_logic.tl.ai.tool.api.ToolMode;
import com.top_logic.tl.ai.tool.api.UntrustedEnvelope;

/**
 * Tool that fetches a single web page or PDF and returns its main textual content.
 *
 * <p>
 * Includes SSRF protection (no internal/private hosts, http(s) only, no credentials in URL),
 * manual redirect handling with per-hop re-validation, and a byte cap.
 * </p>
 *
 * <p>
 * <b>Long documents are read in windows.</b> One answer shows at most the configured number of
 * characters, from the {@code offset} the call gives, and ends with the offset of the next window. A
 * PDF's text carries a marker per page, and the note names the pages a window covers. The extracted
 * text is kept for some minutes ({@link FetchedDocuments}), so reading on neither fetches the
 * document again nor reads a text that changed in between. A call without an offset always fetches
 * the document anew.
 * </p>
 *
 * <p>
 * Which origins it may reach at all is the acting role's {@link BrowsingPolicy}, configured on
 * {@link WebToolsService}. It is applied to every hop, so an allowed origin redirecting to a
 * forbidden one is refused rather than followed.
 * </p>
 *
 * <p>
 * What it returns is a page somebody else wrote, so it comes back inside the
 * {@link UntrustedEnvelope#PAGE_CONTENT untrusted-content envelope}. The tool's own sentences - every
 * {@code "Error: ..."} and the notice about a document it cannot extract text from - stay outside it:
 * they are this application speaking, and marking them as page content would say otherwise.
 * </p>
 *
 * <p>
 * {@link ToolMode#READ} is deliberate and says only that the fetch changes nothing in this
 * application. That it sends a URL to a third party is the group's statement, not the mode's - see
 * {@link ToolGroups#EGRESS}, which is where an administrator gates outbound access.
 * </p>
 */
public class WebFetchTool {

	private static final int MAX_REDIRECTS = 5;

	/** The byte cap when {@link WebToolsService} is not running. */
	static final int DEFAULT_MAX_BYTES = 20_000_000;

	/**
	 * The size of one window of text when {@link WebToolsService} is not running.
	 *
	 * <p>
	 * The same as the agent loop's default budget for one tool answer, so that a window is what the
	 * model would have seen of any other answer.
	 * </p>
	 */
	public static final int DEFAULT_MAX_CHARS = 20_000;

	/** The documents fetched in the last minutes, shared by every call of the production tool. */
	private static final FetchedDocuments DOCUMENTS = new FetchedDocuments();

	private static final String USER_AGENT = "TopLogic-AI-Agent/1.0 (+https://www.top-logic.com)";

	private static final String JUNK_SELECTOR =
		"script,style,noscript,iframe,svg,head,nav,footer,header,form,button,input,select,template";

	/**
	 * How many fetches may be connected at once through {@link Egress#PROXY}, counted in the units
	 * {@link FilteringProxy#maxConnections(int)} takes: each unit is a browser's worth of sockets, far
	 * more than the tool calls of one installation hold open at the same time.
	 */
	private static final int EGRESS_SESSIONS = 4;

	/**
	 * The filtering proxy every production fetch goes through, started on first use.
	 *
	 * <p>
	 * <b>Why a proxy.</b> {@link UrlSafety#validateUrl(URI)} resolves the host and checks the
	 * addresses, but an {@link HttpClient} connected by name would resolve it a second time, and a
	 * rebinding host answers the second query with a private address (#581). The JDK client has no
	 * per-client resolver to pin the checked address with. The {@link FilteringProxy} the browser
	 * already uses resolves each connection once, checks every address and connects to exactly that
	 * one, for plain HTTP and for the {@code CONNECT} tunnel of HTTPS alike. The certificate check
	 * stays between the client and the origin, because the tunnel is blind.
	 * </p>
	 *
	 * <p>
	 * Its domain policy permits everything: the role's {@link BrowsingPolicy} is checked by the tool on
	 * every hop, and the browser's installation-wide lists do not govern this tool. The SSRF check is
	 * not a policy the proxy can be configured out of.
	 * </p>
	 */
	private static final class Egress {

		static final FilteringProxy PROXY =
			FilteringProxy.start(host -> null, FilteringProxy.ssrfResolver(), EGRESS_SESSIONS);

		static final HttpClient CLIENT = clientThrough(PROXY);
	}

	private final UrlSafety.NameLookup _names;

	private final FilteringProxy _egress;

	private final HttpClient _client;

	private final FetchedDocuments _documents;

	/**
	 * Creates a {@link WebFetchTool}.
	 */
	public WebFetchTool() {
		// Required public no-arg constructor for ReflectiveToolFactory.
		this(UrlSafety.NameLookup.SYSTEM, Egress.PROXY, Egress.CLIENT, DOCUMENTS);
	}

	/**
	 * Creates a {@link WebFetchTool} that resolves with {@code names}, connects through
	 * {@code egress} and keeps the documents it fetched for itself. Package-visible for testing; the
	 * caller owns and closes {@code egress}.
	 */
	WebFetchTool(UrlSafety.NameLookup names, FilteringProxy egress) {
		this(names, egress, clientThrough(egress), new FetchedDocuments());
	}

	private WebFetchTool(UrlSafety.NameLookup names, FilteringProxy egress, HttpClient client,
			FetchedDocuments documents) {
		_names = names;
		_egress = egress;
		_client = client;
		_documents = documents;
	}

	private static HttpClient clientThrough(FilteringProxy egress) {
		return HttpClient.newBuilder()
			.connectTimeout(Duration.ofSeconds(15))
			.followRedirects(HttpClient.Redirect.NEVER)
			.proxy(ProxySelector.of(new InetSocketAddress(InetAddress.getLoopbackAddress(), egress.port())))
			.build();
	}

	/**
	 * Fetches a document and returns a window of its extracted text.
	 *
	 * @param url
	 *        The absolute http(s) URL to fetch.
	 * @param offset
	 *        The character offset at which the window starts; {@code null} for the start.
	 * @return The window of the extracted text, or an error string.
	 */
	@ToolMethod(
		description = "Fetch a single web page or PDF by its URL and return the main content as Markdown, "
			+ "preserving headings, links, lists and tables. "
			+ "Use after webSearch to read a specific result, or to read a URL the user provided. "
			+ "A long document is returned in parts: the answer then ends with the offset of the next "
			+ "part, and you read on by calling webFetch again with the same url and that offset. "
			+ "Only public http(s) documents are supported.",
		mode = ToolMode.READ, group = ToolGroups.EGRESS,
		marking = AnswerMarking.TOOL,
		truncationHint = Integer.MAX_VALUE)
	public String webFetch(
			@P("The absolute http or https URL to fetch.") String url,
			@P(value = "Optional: the character offset to start reading at. Leave it out for the start "
				+ "of the document; to read on, give the offset that the previous answer for the same url "
				+ "names.", required = false) Integer offset) {
		if (url == null || url.isBlank()) {
			return "Error: url is required.";
		}
		int start = offset == null ? 0 : offset.intValue();
		if (start < 0) {
			return "Error: offset must be 0 or more.";
		}

		WebToolsService service = WebToolsService.getInstance();
		// Clamp to safe minimums so a misconfigured (zero/negative) limit cannot break the fetch.
		int timeoutSeconds = Math.max(1, service != null ? service.getFetchTimeoutSeconds() : 30);
		int maxBytes = Math.max(1024, service != null ? service.getFetchMaxBytes() : DEFAULT_MAX_BYTES);
		int maxChars = windowChars();

		BrowsingPolicy browsing = WebToolsService.currentPolicy();

		String requestedUrl = url.trim();
		// Before anything else, and before the kept text is asked: an address this run was not given
		// is not opened, whether or not somebody else's run read it.
		String unseen = UrlProvenance.refusal(requestedUrl);
		if (unseen != null) {
			return unseen;
		}
		try {
			// Reading on uses the text the earlier call read; a call from the start reads anew.
			FetchedDocument document = start > 0 ? _documents.get(requestedUrl) : null;
			if (document != null) {
				// Kept from an earlier call, possibly of another account: the acting role's policy is
				// asked again, for the address asked for and for the one the text came from.
				String offPolicy = refuseKept(browsing, requestedUrl, document.url());
				if (offPolicy != null) {
					return offPolicy;
				}
			} else {
				// Cut to what the cache keeps, so a long document is fetched once, not once per window.
				document = fetch(requestedUrl, browsing, timeoutSeconds, maxBytes)
					.cutTo(FetchedDocuments.MAX_DOCUMENT_CHARS);
				_documents.put(requestedUrl, document);
			}
			String answer = renderWindow(requestedUrl, document, start, maxChars);
			if (start > 0 && start >= document.text().length()) {
				// Past the end: nothing was read.
				return answer;
			}
			// The run read this window - not the whole document - so this window is what its
			// reading list and the archived copy gain.
			WebSources.read(requestedUrl, document.url(), document.title(), document.text(), start,
				windowEnd(document.text(), start, maxChars));
			// Where a redirect led and what the page links to are addresses this run was given.
			UrlProvenance.record(document.url());
			document.links().forEach(UrlProvenance::record);
			UrlProvenance.recordAllIn(documentKey(document), document.text());
			return answer;
		} catch (Refusal ex) {
			return ex.getMessage();
		} catch (InterruptedException ex) {
			Thread.currentThread().interrupt();
			return "Error: web fetch was interrupted.";
		} catch (Exception ex) {
			Logger.error("Web fetch failed: " + ex.getMessage(), ex, WebFetchTool.class);
			return "Error: web fetch failed: " + ex.getMessage();
		}
	}

	/**
	 * Fetches {@code requestedUrl}, following redirects by hand, and extracts its text.
	 *
	 * @throws Refusal
	 *         With the tool's answer, when the document cannot be fetched or has no text to extract.
	 */
	private FetchedDocument fetch(String requestedUrl, BrowsingPolicy browsing, int timeoutSeconds,
			int maxBytes) throws Refusal, IOException, InterruptedException {
		String currentUrl = requestedUrl;
		for (int hop = 0; hop <= MAX_REDIRECTS; hop++) {
			URI uri;
			try {
				uri = URI.create(currentUrl);
			} catch (IllegalArgumentException ex) {
				throw new Refusal("Error: invalid URL: " + ex.getMessage());
			}
			String shape = UrlSafety.checkUrlShape(uri);
			if (shape != null) {
				throw new Refusal("Error: " + shape);
			}
			// Re-checked on EVERY hop, exactly as the SSRF check below is: a permitted origin
			// answering with a redirect to an off-policy one is the whole reason the redirects are
			// followed by hand here rather than by the HttpClient. Before the SSRF check, because
			// it needs no network: an off-policy host is refused for that reason even when its
			// name does not resolve (#567).
			String offPolicy = browsing.refuseAccess(uri.getHost());
			if (offPolicy != null) {
				throw new Refusal("Error: " + BrowsingPolicies.audit(browsing, uri.getHost(), offPolicy));
			}
			String validationError = UrlSafety.validateUrl(uri, _names);
			if (validationError != null) {
				throw new Refusal("Error: " + validationError);
			}

			HttpRequest request = HttpRequest.newBuilder(uri)
				.header("User-Agent", USER_AGENT)
				.header("Accept", "text/html,application/xhtml+xml,application/pdf,text/plain;q=0.9,*/*;q=0.8")
				.timeout(Duration.ofSeconds(timeoutSeconds))
				.GET()
				.build();

			HttpResponse<InputStream> response;
			try {
				response = _client.send(request, BodyHandlers.ofInputStream());
			} catch (IOException ex) {
				// A refused HTTPS tunnel reaches the client only as an exception ("Tunnel failed,
				// got: 403"); any other failure is reported as what it is.
				String message = String.valueOf(ex.getMessage());
				String refusal = message.contains("403") ? _egress.refusalFor(uri.getHost()) : null;
				if (refusal != null) {
					throw new Refusal("Error: " + refusal);
				}
				throw ex;
			}
			int status = response.statusCode();
			if (status == 403 && response.headers().firstValue(FilteringProxy.REFUSAL_HEADER).isPresent()) {
				// Refused by the proxy at connect time - typically a host whose address changed
				// since the check above. The reason is taken from the proxy, not from the header,
				// which the upstream server could have written itself.
				String refusal = _egress.refusalFor(uri.getHost());
				if (refusal != null) {
					discard(response);
					throw new Refusal("Error: " + refusal);
				}
			}

			if (status >= 300 && status < 400) {
				discard(response);
				Optional<String> location = response.headers().firstValue("location");
				if (location.isEmpty()) {
					throw new Refusal("Error: redirect (HTTP " + status + ") without a Location header.");
				}
				currentUrl = uri.resolve(location.get()).toString();
				continue;
			}
			if (status / 100 != 2) {
				discard(response);
				throw new Refusal("Error: fetch failed (HTTP " + status + ") for " + currentUrl);
			}

			String contentType =
				response.headers().firstValue("content-type").orElse("").toLowerCase();
			Body body = readLimited(response.body(), maxBytes);
			return extract(currentUrl, contentType, body, maxBytes);
		}
		throw new Refusal("Error: too many redirects (more than " + MAX_REDIRECTS + ") starting from "
			+ requestedUrl);
	}

	/**
	 * Extracts the text of a fetched body by its content type.
	 *
	 * @throws Refusal
	 *         With the tool's answer, when the body holds no text this tool can extract.
	 */
	private static FetchedDocument extract(String url, String contentType, Body body, int maxBytes)
			throws Refusal {
		if (PdfText.isPdf(contentType, body.bytes())) {
			if (!body.complete()) {
				// A PDF keeps its index at the end of the file, so the part that was read is useless.
				throw new Refusal("Error: the PDF at " + url + " is larger than the " + maxBytes
					+ " bytes this application reads (fetch-max-bytes); cannot extract text.");
			}
			try {
				return PdfText.extract(url, body.bytes());
			} catch (PdfText.PdfException ex) {
				throw new Refusal("Error: " + ex.getMessage());
			}
		}
		String text = new String(body.bytes(), charsetOf(contentType));
		if (contentType.contains("html") || contentType.contains("xml")
				|| (contentType.isEmpty() && looksLikeHtml(text))) {
			Document page = Jsoup.parse(text, url);
			// Taken before the conversion, which removes the head with the title in it.
			String title = page.title();
			List<String> links = new ArrayList<>();
			String markdown = htmlToMarkdown(page, links).strip();
			return new FetchedDocument(url, title, markdown, List.of(), List.copyOf(links));
		}
		if (contentType.startsWith("text/") || contentType.contains("json") || contentType.isEmpty()) {
			return new FetchedDocument(url, "", text.strip());
		}
		// The tool's own sentence, not page content, so it is not wrapped as one.
		throw new Refusal("Fetched " + url + " (" + contentType
			+ "), but it is not a text, HTML or PDF document; cannot extract text.");
	}

	/**
	 * What tells one fetched text from another, for reading its addresses once per run: the address it
	 * was read from and its text. The hash of a string is computed once and kept with it, so asking
	 * again for the next window of the same text costs nothing.
	 */
	private static String documentKey(FetchedDocument document) {
		String text = document.text();
		return "document:" + document.url() + "#" + text.length() + ":" + text.hashCode();
	}

	/**
	 * Applies the acting role's policy to a document kept from an earlier call.
	 *
	 * @return The tool's answer when the policy refuses one of the two addresses, else {@code null}.
	 */
	private static String refuseKept(BrowsingPolicy browsing, String requestedUrl, String finalUrl) {
		for (String address : new String[] { requestedUrl, finalUrl }) {
			String host = URI.create(address).getHost();
			String offPolicy = browsing.refuseAccess(host);
			if (offPolicy != null) {
				return "Error: " + BrowsingPolicies.audit(browsing, host, offPolicy);
			}
		}
		return null;
	}

	/**
	 * Renders one window of a document's text into the untrusted-data envelope, followed by where the
	 * window lies in the document and how to read on. Package-visible for testing.
	 *
	 * <p>
	 * The window is cut, not the document: the model sees the text from {@code offset} on, at most
	 * {@code maxChars} characters of it, and is told the offset at which the next window starts. A cut
	 * that kept the head and the tail would drop the middle of a long document without a way to get
	 * it back. The cut is moved back to a paragraph, line or word end when one is near, so that a
	 * window does not end inside a word.
	 * </p>
	 *
	 * <p>
	 * The envelope is the same one, from the same place, that {@code BrowserTool} puts around what it
	 * reads: a page is a page whether a browser rendered it or this tool fetched it, and a barrier
	 * that covered only one of the two routes to a page would just tell an attacker which route to
	 * aim at. The note about the window is the tool's own sentence, so it stays outside.
	 * </p>
	 *
	 * @param requestedUrl
	 *        The address the model asked for, which the note tells it to ask for again.
	 * @param document
	 *        The extracted document. Its URL is the one actually fetched, after any redirects - the
	 *        page decided it, so it is escaped into the envelope's {@code source} attribute rather
	 *        than trusted.
	 * @param offset
	 *        Where the window starts.
	 * @param maxChars
	 *        How many characters the window holds at most.
	 * @see UntrustedEnvelope#PAGE_CONTENT
	 */
	static String renderWindow(String requestedUrl, FetchedDocument document, int offset, int maxChars) {
		String text = document.text();
		int length = text.length();
		if (offset > 0 && offset >= length) {
			return "Error: offset " + offset + " is at or past the end of the document from " + requestedUrl
				+ ", which has " + length + " characters.";
		}
		int end = windowEnd(text, offset, maxChars);
		String out = UntrustedEnvelope.PAGE_CONTENT.wrap(document.url(), text.substring(offset, end));
		if (offset == 0 && end == length) {
			return out;
		}
		StringBuilder note = new StringBuilder(out);
		note.append("\n\n[This is characters ").append(offset).append(" to ").append(end)
			.append(" of ").append(length);
		if (document.pageCount() > 0) {
			note.append(", PDF pages ").append(document.pageAt(offset)).append(" to ")
				.append(document.pageAt(end - 1)).append(" of ").append(document.pageCount());
		}
		note.append(".");
		if (end < length) {
			note.append(" To read on, call webFetch with the same url and offset=").append(end).append(".]");
		} else {
			note.append(" This is the end of the document.]");
		}
		return note.toString();
	}

	/**
	 * How many characters one window of a document shows: the configured {@code fetch-max-chars}, or
	 * {@link #DEFAULT_MAX_CHARS} while the web tools service is not running.
	 */
	public static int windowChars() {
		WebToolsService service = WebToolsService.getInstance();
		// Clamped to a safe minimum so a misconfigured (zero/negative) limit cannot break the windows.
		return Math.max(256, service != null ? service.getFetchMaxChars() : DEFAULT_MAX_CHARS);
	}

	/**
	 * Where a window that starts at {@code offset} ends: at most {@code maxChars} characters on, and
	 * moved back to the end of a paragraph, a line or a word in the last fifth of the window.
	 */
	public static int windowEnd(String text, int offset, int maxChars) {
		int hardEnd = (int) Math.min(text.length(), (long) offset + maxChars);
		if (hardEnd == text.length()) {
			return hardEnd;
		}
		int earliest = offset + (maxChars * 4) / 5;
		for (String separator : new String[] { "\n\n", "\n", " " }) {
			int at = text.lastIndexOf(separator, hardEnd - separator.length());
			if (at >= earliest) {
				return at + separator.length();
			}
		}
		// No break near the end: cut hard, but never between the two halves of a surrogate pair.
		return Character.isHighSurrogate(text.charAt(hardEnd - 1)) ? hardEnd - 1 : hardEnd;
	}

	/**
	 * Validates a URL for fetching. Returns {@code null} if acceptable, otherwise a human-readable
	 * reason for rejection. Package-visible for testing.
	 *
	 * @see UrlSafety#validateUrl(URI)
	 */
	static String validateUrl(URI uri) {
		return UrlSafety.validateUrl(uri);
	}

	private static boolean looksLikeHtml(String body) {
		String head = (body.length() > 512 ? body.substring(0, 512) : body).toLowerCase();
		return head.contains("<html") || head.contains("<!doctype html") || head.contains("<body")
			|| head.contains("<div") || head.contains("<p>");
	}

	/**
	 * Converts an HTML document to Markdown after stripping non-content chrome (scripts, styles,
	 * navigation, etc.). Relative links and image sources are resolved against {@code baseUrl} so
	 * the resulting Markdown contains usable absolute URLs. Package-visible for testing.
	 */
	static String htmlToMarkdown(String html, String baseUrl) {
		return htmlToMarkdown(Jsoup.parse(html, baseUrl), new ArrayList<>());
	}

	/**
	 * {@link #htmlToMarkdown(String, String)} of a parsed document, also collecting the absolute
	 * address of every link the Markdown keeps into {@code links}. Removes the non-content chrome from
	 * {@code document}.
	 */
	private static String htmlToMarkdown(Document document, List<String> links) {
		document.select(JUNK_SELECTOR).remove();
		Element root = document.body() != null ? document.body() : document;

		// Inline the resolved absolute URLs so links survive the conversion to Markdown.
		for (Element link : root.select("a[href]")) {
			String absolute = link.absUrl("href");
			if (!absolute.isEmpty()) {
				link.attr("href", absolute);
				links.add(absolute);
			}
		}
		for (Element image : root.select("img[src]")) {
			String absolute = image.absUrl("src");
			if (!absolute.isEmpty()) {
				image.attr("src", absolute);
			}
		}

		String markdown = Converters.htmlToMarkdown(root.html());
		return markdown.replaceAll("\\n{3,}", "\n\n").strip();
	}

	private static void discard(HttpResponse<InputStream> response) throws IOException {
		try (InputStream body = response.body()) {
			body.readAllBytes();
		}
	}

	/**
	 * Reads a body up to {@code maxBytes}.
	 *
	 * @return The bytes read, and whether that is the whole body.
	 */
	private static Body readLimited(InputStream in, int maxBytes) throws IOException {
		try (in) {
			ByteArrayOutputStream buffer = new ByteArrayOutputStream();
			byte[] chunk = new byte[8192];
			int total = 0;
			int read;
			while ((read = in.read(chunk)) != -1) {
				if (total + read > maxBytes) {
					buffer.write(chunk, 0, maxBytes - total);
					return new Body(buffer.toByteArray(), false);
				}
				buffer.write(chunk, 0, read);
				total += read;
			}
			return new Body(buffer.toByteArray(), true);
		}
	}

	/**
	 * A body as far as it was read.
	 *
	 * @param bytes
	 *        The bytes read.
	 * @param complete
	 *        Whether these are all bytes of the body, or the byte cap stopped the read.
	 */
	private record Body(byte[] bytes, boolean complete) {
		// Value type only.
	}

	/**
	 * Why a document was not fetched; the message is the tool's whole answer.
	 */
	private static final class Refusal extends Exception {

		Refusal(String answer) {
			super(answer);
		}
	}

	private static Charset charsetOf(String contentType) {
		int idx = contentType.indexOf("charset=");
		if (idx >= 0) {
			String name = contentType.substring(idx + "charset=".length()).trim();
			int semi = name.indexOf(';');
			if (semi >= 0) {
				name = name.substring(0, semi).trim();
			}
			name = name.replace("\"", "");
			try {
				if (!name.isEmpty() && Charset.isSupported(name)) {
					return Charset.forName(name);
				}
			} catch (Exception ex) {
				// Fall through to default.
			}
		}
		return StandardCharsets.UTF_8;
	}
}
