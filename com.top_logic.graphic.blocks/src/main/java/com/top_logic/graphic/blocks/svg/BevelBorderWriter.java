/*
 * SPDX-FileCopyrightText: 2020 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.graphic.blocks.svg;

/**
 * {@link SvgWriterAdapter} that only draws these parts of the outline that receive light when
 * highlighting a beveled shape from the top left assuming the outline is drawn in clockwise
 * direction.
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class BevelBorderWriter extends SvgWriterAdapter {

	/** 
	 * Creates a {@link BevelBorderWriter}.
	 */
	public BevelBorderWriter(SvgWriter impl) {
		super(impl);
	}
	
	@Override
	public void lineToHorizontalRel(double dx) {
		if (dx > 0) {
			super.lineToHorizontalRel(dx);
		} else {
			moveToRel(dx, 0);
		}
	}

	@Override
	public void lineToVerticalRel(double dy) {
		if (dy < 0) {
			super.lineToVerticalRel(dy);
		} else {
			moveToRel(0, dy);
		}
	}

	@Override
	public void lineToRel(double dx, double dy) {
		if (dx >= 0 && dy <= 0) {
			super.lineToRel(dx, dy);
		} else {
			moveToRel(dx, dy);
		}
	}

	@Override
	public void rect(double x, double y, double w, double h) {
		if (w < 0) {
			rect(x + w, y, -w, h);
		} else if (h < 0) {
			rect(x, y + h, w, -h);
		} else {
			moveToRel(x, y + h);
			super.lineToVerticalRel(-h);
			super.lineToHorizontalRel(w);
		}
	}

	@Override
	public void rect(double x, double y, double w, double h, double rx, double ry) {
		if (w < 0) {
			rect(x + w, y, -w, h, rx, ry);
		} else if (h < 0) {
			rect(x, y + h, w, -h, rx, ry);
		} else {
			moveToRel(x, y + h);
			super.lineToVerticalRel(-(h + 2 * ry));
			super.roundedCorner(false, rx, ry);
			super.lineToHorizontalRel(w - 2 * rx);
		}
	}

	@Override
	public void curveToRel(double dx1, double dy1, double dx2, double dy2, double dx, double dy) {
		if (dx >= 0 && dy <= 0) {
			super.curveToRel(dx1, dy1, dx2, dy2, dx, dy);
		} else {
			moveToRel(dx, dy);
		}
	}

}
