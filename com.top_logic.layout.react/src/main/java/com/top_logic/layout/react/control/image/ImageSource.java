/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.image;

import java.util.Objects;
import java.util.function.BiConsumer;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.react.DataProvider;

/**
 * The picture a React control displays, resolved from an application value to the client state
 * naming it.
 *
 * <p>
 * The client only ever loads an address. A value that is picture data is served by the displaying
 * control itself and announced as {@link #HAS_DATA}; the client composes the address of the data
 * endpoint from the control it belongs to and appends the {@link #DATA_REVISION}, so that a picture
 * which was replaced is loaded again instead of being taken from the browser cache. A value that is
 * a string is the address itself, which covers an external picture as well as a resource of the web
 * application. Any other value - no value, binary data that is no picture, an unrelated object -
 * leaves the {@link #setFallbackUrl(String) fallback address}, and shows nothing where there is
 * none.
 * </p>
 *
 * <p>
 * A control displaying a picture holds one of these, forwards the values it receives to
 * {@link #setValue(Object)}, and answers {@link DataProvider#getDownloadData(String)} with
 * {@link #getData()}.
 * </p>
 */
public class ImageSource {

	/** State key for the address the picture is loaded from, or {@code null} for no picture. */
	public static final String URL = "url";

	/** State key telling whether the displaying control serves the picture itself. */
	public static final String HAS_DATA = "hasData";

	/** State key counting how often the served picture was replaced. */
	public static final String DATA_REVISION = "dataRevision";

	/** Prefix of the content type of any picture. */
	private static final String IMAGE_TYPE_PREFIX = "image/";

	private final BiConsumer<String, Object> _state;

	private BinaryData _data;

	private String _url;

	private String _fallbackUrl;

	private int _dataRevision;

	/**
	 * Creates an {@link ImageSource} showing nothing.
	 *
	 * @param state
	 *        Where the client state of the picture is written to, the state of the control
	 *        displaying it.
	 */
	public ImageSource(BiConsumer<String, Object> state) {
		_state = state;
		publish();
	}

	/**
	 * Shows the picture the given value denotes.
	 *
	 * @param value
	 *        Picture data, an address naming a picture, or anything else to show no picture.
	 */
	public void setValue(Object value) {
		BinaryData data = value instanceof BinaryData binary && isImage(binary) ? binary : null;
		String url = data == null && value instanceof String address && !address.isEmpty() ? address : null;

		if (data == _data && Objects.equals(url, _url)) {
			return;
		}
		if (data != _data) {
			_dataRevision++;
		}
		_data = data;
		_url = url;
		publish();
	}

	/**
	 * Sets the address to show while the {@link #setValue(Object) value} names no picture.
	 *
	 * @param url
	 *        The address of a picture, already resolved against the context path of the
	 *        application when it names a resource of the web application, or {@code null} to show
	 *        nothing.
	 */
	public void setFallbackUrl(String url) {
		if (Objects.equals(url, _fallbackUrl)) {
			return;
		}
		_fallbackUrl = url;
		publish();
	}

	/**
	 * The picture data the displaying control serves, or {@code null} while the client loads the
	 * picture from an address of its own.
	 */
	public BinaryData getData() {
		return _data;
	}

	private void publish() {
		boolean hasData = _data != null;
		_state.accept(HAS_DATA, Boolean.valueOf(hasData));
		_state.accept(DATA_REVISION, Integer.valueOf(_dataRevision));
		_state.accept(URL, hasData ? null : (_url != null ? _url : _fallbackUrl));
	}

	/**
	 * Whether the given data is a picture the browser can display.
	 *
	 * @implNote The content type is matched case-insensitively against the {@code image} top-level
	 *           media type, tolerating parameters such as {@code image/svg+xml; charset=utf-8}.
	 */
	private static boolean isImage(BinaryData data) {
		String contentType = data.getContentType();
		if (contentType == null) {
			return false;
		}
		return contentType.regionMatches(true, 0, IMAGE_TYPE_PREFIX, 0, IMAGE_TYPE_PREFIX.length());
	}

}
