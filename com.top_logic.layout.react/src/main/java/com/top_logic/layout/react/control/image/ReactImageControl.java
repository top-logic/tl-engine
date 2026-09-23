/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.image;

import java.util.Set;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;

/**
 * A {@link ReactControl} that displays a picture through the {@code TLImage} React component.
 *
 * <p>
 * What is shown is the {@link ImageSource} of this control: picture data this control serves, an
 * address naming a picture, or nothing. The client renders an image element loading that address.
 * </p>
 *
 * <p>
 * Without any of {@link #setAspectRatio(String)}, {@link #setWidth(String)} and
 * {@link #setHeight(String)} the picture is shown in its natural size, limited to the width
 * available. Where a size is given, {@link #setFit(ImageFit)} decides whether a picture of
 * different proportions is cropped or shown as a whole.
 * </p>
 */
public class ReactImageControl extends ReactControl implements DataProvider {

	private static final String REACT_MODULE = "TLImage";

	/** @see #setAlt(String) */
	public static final String ALT = "alt";

	/** @see #setFit(ImageFit) */
	public static final String FIT = "fit";

	/** @see #setAspectRatio(String) */
	public static final String ASPECT_RATIO = "aspectRatio";

	/** @see #setWidth(String) */
	public static final String WIDTH = "width";

	/** @see #setHeight(String) */
	public static final String HEIGHT = "height";

	/** @see #setLazy(boolean) */
	public static final String LAZY = "lazy";

	private final ImageSource _image = new ImageSource(this::putState);

	/**
	 * Creates a {@link ReactImageControl} showing nothing.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 */
	public ReactImageControl(ReactContext context) {
		super(context, null, REACT_MODULE);
		setFit(ImageFit.COVER);
	}

	/**
	 * Shows the picture the given value denotes.
	 *
	 * @see ImageSource#setValue(Object)
	 */
	public void setValue(Object value) {
		_image.setValue(value);
	}

	/**
	 * Sets the address to show while the {@link #setValue(Object) value} names no picture.
	 *
	 * @see ImageSource#setFallbackUrl(String)
	 */
	public void setFallbackUrl(String url) {
		_image.setFallbackUrl(url);
	}

	/**
	 * Sets what the picture shows, for a reader who cannot see it.
	 *
	 * @param alt
	 *        The description, or {@code null} to announce the picture as one.
	 */
	public void setAlt(String alt) {
		putState(ALT, (alt == null || alt.isEmpty()) ? null : alt);
	}

	/**
	 * Sets how a picture whose proportions differ from the box fills it.
	 */
	public void setFit(ImageFit fit) {
		putState(FIT, fit.getExternalName());
	}

	/**
	 * Sets the proportions of the box the picture is shown in, as width and height separated by a
	 * slash, e.g. {@code 16/9}.
	 *
	 * @param aspectRatio
	 *        The proportions, or {@code null} to take them from the picture.
	 */
	public void setAspectRatio(String aspectRatio) {
		putState(ASPECT_RATIO, aspectRatio);
	}

	/**
	 * Sets the width of the box the picture is shown in, as a CSS length, e.g. {@code 12rem}.
	 *
	 * @param width
	 *        The width, or {@code null} to take it from the picture.
	 */
	public void setWidth(String width) {
		putState(WIDTH, width);
	}

	/**
	 * Sets the height of the box the picture is shown in, as a CSS length, e.g. {@code 12rem}.
	 *
	 * @param height
	 *        The height, or {@code null} to take it from the picture.
	 */
	public void setHeight(String height) {
		putState(HEIGHT, height);
	}

	/**
	 * Whether the browser may postpone loading the picture until it comes close to the visible part
	 * of the page.
	 *
	 * <p>
	 * Worth setting for a picture far down a long page, and wrong for one the user sees at once,
	 * which then arrives later than it had to.
	 * </p>
	 */
	public void setLazy(boolean lazy) {
		putState(LAZY, Boolean.valueOf(lazy));
	}

	@Override
	public BinaryData getDownloadData(String key) {
		return _image.getData();
	}

	/**
	 * Rendering-only state keys, omitted from the headless projection.
	 */
	@Override
	protected Set<String> scriptingPresentationKeys() {
		return presentationKeys(super.scriptingPresentationKeys(), FIT, ASPECT_RATIO, WIDTH, HEIGHT, LAZY,
			ImageSource.DATA_REVISION);
	}

}
