/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.react.flow.operations;

import com.top_logic.react.flow.data.Box;
import com.top_logic.react.flow.data.Sticky;
import com.top_logic.react.flow.svg.RenderContext;
import com.top_logic.react.flow.svg.SvgWriter;

/**
 * Operations for the {@link Sticky} box.
 *
 * <p>
 * The box takes the whole width it is given and gives its content its natural width, but at most
 * the width of the box, so that a {@link com.top_logic.react.flow.data.Text#isEllipsis() text cut
 * to its width} ends with an ellipsis instead of extending beyond the box. The content is drawn in a group of its own, translated by {@link Sticky#getShift()}. A scrolling container
 * moves the content by updating the translation of that group, identified by
 * {@link #contentId(Sticky)}, without drawing the box again.
 * </p>
 */
public interface StickyOperations extends DecorationOperations {

	/**
	 * Suffix of the ID (appended to the client ID of the {@link Sticky}) of the group moving its
	 * content.
	 */
	String CONTENT_ID_SUFFIX = "-sticky";

	@Override
	Sticky self();

	@Override
	default void computeIntrinsicSize(RenderContext context, double offsetX, double offsetY,
			double availableWidth, double availableHeight) {
		self().setX(offsetX);
		self().setY(offsetY);
		DecorationOperations.super.computeIntrinsicSize(context, offsetX, offsetY, availableWidth, availableHeight);
	}

	@Override
	default void distributeSize(RenderContext context, double offsetX, double offsetY, double width, double height) {
		Box content = self().getContent();
		content.distributeSize(context, offsetX, offsetY, Math.min(content.getWidth(), width), height);

		self().setX(offsetX);
		self().setY(offsetY);
		self().setWidth(width);
		self().setHeight(height);
	}

	/**
	 * The largest {@link Sticky#getShift() shift} of the content: the one at which the content
	 * ends at the right edge of the box.
	 */
	default double maxShift() {
		return Math.max(0, self().getWidth() - self().getContent().getWidth());
	}

	/**
	 * Updates the {@link Sticky#getShift() shift} of the content, so that the content starts at
	 * the given X coordinate as far as it stays within the box.
	 *
	 * @param visibleLeft
	 *        The X coordinate of the left edge of the visible area, in the coordinates of the box.
	 * @return The new shift.
	 */
	default double stickTo(double visibleLeft) {
		double shift = Math.max(0, Math.min(maxShift(), visibleLeft - self().getX()));
		if (shift != self().getShift()) {
			self().setShift(shift);
		}
		return shift;
	}

	/**
	 * The ID of the group moving the content of the given box, or <code>null</code> if the box
	 * has not been drawn on a client.
	 */
	static String contentId(Sticky sticky) {
		String clientId = sticky.getClientId();
		return clientId == null ? null : clientId + CONTENT_ID_SUFFIX;
	}

	@Override
	default void draw(SvgWriter out) {
		Sticky self = self();
		out.beginGroup();
		out.attachModel(self);

		out.beginGroup();
		String contentId = contentId(self);
		if (contentId != null) {
			out.writeId(contentId);
		}
		out.translate(self.getShift(), 0);
		drawContent(out);
		out.endGroup();

		out.endGroup();
	}

}
