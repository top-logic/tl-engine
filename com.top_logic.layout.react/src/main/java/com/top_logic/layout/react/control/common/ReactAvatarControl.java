/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.common;

import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.layout.react.DataProvider;
import com.top_logic.layout.react.ReactContext;
import com.top_logic.layout.react.control.ReactControl;
import com.top_logic.layout.react.control.image.ImageSource;

/**
 * A {@link ReactControl} that displays a person or object as a circular avatar via the
 * {@code TLAvatar} React component.
 *
 * <p>
 * The circle shows the picture of the one it represents. Where there is none, the client fills it
 * with the initials of the display name over a background color derived from that name.
 * </p>
 */
public class ReactAvatarControl extends ReactControl implements DataProvider {

	private static final String REACT_MODULE = "TLAvatar";

	/** State key for the display name the avatar represents, or {@code null} for no value. */
	public static final String NAME = "name";

	/** @see #setSize(AvatarSize) */
	public static final String SIZE = "size";

	private final ImageSource _image = new ImageSource(this::putState);

	/**
	 * Creates a new {@link ReactAvatarControl}.
	 *
	 * @param context
	 *        The {@link ReactContext} for ID allocation and SSE registration.
	 * @param name
	 *        The initial display name, or {@code null} for no value.
	 */
	public ReactAvatarControl(ReactContext context, String name) {
		super(context, null, REACT_MODULE);
		setName(name);
		setSize(AvatarSize.DEFAULT);
	}

	/**
	 * Updates the displayed name.
	 *
	 * @param name
	 *        The new display name, or {@code null} to display nothing.
	 */
	public void setName(String name) {
		putState(NAME, name);
	}

	/**
	 * Sets the diameter of the circle.
	 */
	public void setSize(AvatarSize size) {
		putState(SIZE, size.getExternalName());
	}

	/**
	 * Shows the picture the given value denotes instead of the initials.
	 *
	 * @see ImageSource#setValue(Object)
	 */
	public void setImage(Object value) {
		_image.setValue(value);
	}

	/**
	 * Sets the address of the picture to show while the {@link #setImage(Object) image value} names
	 * none.
	 *
	 * @see ImageSource#setFallbackUrl(String)
	 */
	public void setFallbackImageUrl(String url) {
		_image.setFallbackUrl(url);
	}

	@Override
	public BinaryData getDownloadData(String key) {
		return _image.getData();
	}

}
