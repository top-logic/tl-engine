/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.html;

import java.nio.charset.StandardCharsets;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A read-only control that displays an HTML fragment.
 *
 * <p>
 * Renders as a {@code TLHtml} React component. The control shows either the HTML fragment given to
 * {@link #setHtml(String)} or the message given to {@link #setError(String)} - whichever was set
 * last; setting one clears the other.
 * </p>
 *
 * <p>
 * The fragment reaches the browser as it stands. Whether its content may be shown is decided by the
 * caller: the fragment passed to {@link #setHtml(String)} is the one that is published, and a
 * fragment the caller rejects is reported through {@link #setError(String)} instead.
 * </p>
 *
 * <p>
 * The {@link #DISPLAY display mode} chosen at construction time decides how the fragment is shown -
 * {@link #DISPLAY_INLINE} inserts it into the page around it, {@link #DISPLAY_DOCUMENT} shows it in
 * a frame of its own, {@link #DISPLAY_THUMBNAIL} shows that frame scaled down to a preview. Only
 * content inserted into the page travels with the state: {@link #HTML}
 * carries the fragment in {@link #DISPLAY_INLINE} and stays empty in the other modes, where a frame
 * fetches the content from the data endpoint this control serves as a {@link DataProvider} instead.
 * The document is thus a resource of its own with an address the frame can be pointed at and
 * reloaded from, and it crosses the wire once rather than twice. Which content that address
 * delivers is told by {@link #DATA_REVISION}, which changes with every content: the frame carries
 * the current revision in its URL, so a replaced document is fetched rather than taken from the
 * browser cache.
 * </p>
 *
 * <p>
 * A document is shown with a print button when {@link #PRINT} is set; the button hands the frame to
 * the browser's print dialog. A thumbnail is a picture of a document rather than the document
 * itself and carries no such button, so {@link #PRINT} has no effect on it. Its frame is laid out at
 * {@link #THUMBNAIL_WIDTH} by {@link #THUMBNAIL_HEIGHT} CSS pixels and scaled down to the space the
 * preview is given, which keeps the aspect ratio of the two.
 * </p>
 */
public class ReactHtmlControl extends ReactControl implements DataProvider {

	/** Name of the React component this control renders as. */
	private static final String COMPONENT = "TLHtml";

	/** State key holding the display mode, one of the {@code DISPLAY_}-prefixed constants. */
	public static final String DISPLAY = "display";

	/** State key holding the HTML fragment inserted into the page in {@link #DISPLAY_INLINE}. */
	public static final String HTML = "html";

	/** State key holding the message shown instead of a fragment that cannot be displayed. */
	public static final String ERROR = "error";

	/** State key whose value changes each time the displayed content is replaced. */
	public static final String DATA_REVISION = "dataRevision";

	/** State key telling whether a document is shown with a button that prints it. */
	public static final String PRINT = "print";

	/** State key holding the width in CSS pixels a thumbnail lays its content out at. */
	public static final String THUMBNAIL_WIDTH = "thumbnailWidth";

	/** State key holding the height in CSS pixels a thumbnail lays its content out at. */
	public static final String THUMBNAIL_HEIGHT = "thumbnailHeight";

	/** {@link #DISPLAY} mode inserting the fragment into the page around it. */
	public static final String DISPLAY_INLINE = "inline";

	/** {@link #DISPLAY} mode showing the fragment as a page of its own. */
	public static final String DISPLAY_DOCUMENT = "document";

	/** {@link #DISPLAY} mode showing a scaled-down preview of the fragment. */
	public static final String DISPLAY_THUMBNAIL = "thumbnail";

	/** The content type the document served from the data endpoint is delivered with. */
	private static final String DOCUMENT_CONTENT_TYPE = "text/html; charset=UTF-8";

	/** The name the document served from the data endpoint is delivered under. */
	private static final String DOCUMENT_NAME = "document.html";

	private final String _display;

	private String _html = "";

	private int _dataRevision;

	/**
	 * Creates a {@link ReactHtmlControl} showing nothing.
	 *
	 * @param display
	 *        The display mode, one of the {@code DISPLAY_}-prefixed constants.
	 * @param print
	 *        Whether a document is shown with a button that prints it.
	 * @param cssClass
	 *        Additional CSS class to append to the default {@code tlHtml} class, or {@code null}.
	 * @param thumbnailWidth
	 *        The width in CSS pixels a thumbnail lays its content out at.
	 * @param thumbnailHeight
	 *        The height in CSS pixels a thumbnail lays its content out at.
	 */
	public ReactHtmlControl(ReactContext context, String display, boolean print, String cssClass,
			int thumbnailWidth, int thumbnailHeight) {
		super(context, null, COMPONENT);
		_display = display;
		putState(DISPLAY, display);
		putState(HTML, _html);
		putState(ERROR, null);
		putState(DATA_REVISION, _dataRevision);
		putState(PRINT, print);
		putState(THUMBNAIL_WIDTH, thumbnailWidth);
		putState(THUMBNAIL_HEIGHT, thumbnailHeight);
		setCssClass(cssClass);
	}

	/**
	 * Displays the given HTML fragment and clears the message set before.
	 *
	 * @param html
	 *        The fragment to display, or {@code null} to display nothing.
	 */
	public void setHtml(String html) {
		update(html != null ? html : "", null);
	}

	/**
	 * Displays the given message in place of a fragment and clears the fragment set before.
	 *
	 * @param message
	 *        The message to display, or {@code null} to display nothing.
	 */
	public void setError(String message) {
		update("", message);
	}

	/**
	 * Publishes the content to display and the message taking its place, and announces that the
	 * document served from the data endpoint is another one.
	 *
	 * <p>
	 * The content itself is published only where the client inserts it into the page; a frame
	 * fetches it from the data endpoint, so sending it along would send it twice.
	 * </p>
	 */
	private void update(String html, String error) {
		Object tx = beginUpdate();
		_html = html;
		_dataRevision++;
		putState(HTML, DISPLAY_INLINE.equals(_display) ? html : "");
		putState(ERROR, error);
		putState(DATA_REVISION, _dataRevision);
		commitUpdate(tx);
	}

	/**
	 * The content currently displayed, as the document a frame of this control fetches.
	 */
	@Override
	public BinaryData getDownloadData(String key) {
		return BinaryDataFactory.createBinaryData(_html.getBytes(StandardCharsets.UTF_8), DOCUMENT_CONTENT_TYPE,
			DOCUMENT_NAME);
	}

}
