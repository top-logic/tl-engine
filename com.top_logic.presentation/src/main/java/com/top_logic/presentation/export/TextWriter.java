/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.presentation.export;

import java.util.List;

import org.apache.poi.sl.usermodel.Insets2D;
import org.apache.poi.sl.usermodel.VerticalAlignment;
import org.apache.poi.xslf.usermodel.XSLFTextParagraph;
import org.apache.poi.xslf.usermodel.XSLFTextRun;
import org.apache.poi.xslf.usermodel.XSLFTextShape;
import org.openxmlformats.schemas.drawingml.x2006.main.CTColor;
import org.openxmlformats.schemas.drawingml.x2006.main.CTSolidColorFillProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextBody;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextCharacterProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.CTTextParagraphProperties;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextCapsType;
import org.openxmlformats.schemas.drawingml.x2006.main.STTextUnderlineType;
import org.openxmlformats.schemas.presentationml.x2006.main.CTShape;

import com.top_logic.presentation.data.PresentationData;
import com.top_logic.presentation.model.BulletSpec;
import com.top_logic.presentation.model.InsetsSpec;
import com.top_logic.presentation.model.ParagraphSpec;
import com.top_logic.presentation.model.RunSpec;
import com.top_logic.presentation.model.RunStyle;
import com.top_logic.presentation.model.TextAlign;
import com.top_logic.presentation.model.TextAnchor;
import com.top_logic.presentation.model.TextSpec;

/**
 * Writes a {@link TextSpec} into a text shape, a placeholder or a table cell.
 *
 * <p>
 * Only the properties given in the description are written, all others are inherited from the
 * placeholder, the slide master or the table. The character formatting of the last run of each
 * paragraph is also written as formatting of the paragraph end, so that an empty paragraph keeps
 * the font size of the text.
 * </p>
 *
 * <p>
 * The writer sets no automatic fitting of the shape (<code>normAutofit</code>): A text in a shape,
 * text box or table cell is fitted when the presentation is created, see {@link TextFitting}, and
 * keeps the resulting font sizes when edited in PowerPoint. A placeholder inherits the fitting of
 * its slide layout.
 * </p>
 */
public final class TextWriter {

	/**
	 * The bullet character of a paragraph that switches on the bullet without giving a character,
	 * if it inherits no bullet character.
	 */
	public static final String DEFAULT_BULLET = "•";

	/**
	 * The distance in points between a bullet written for a paragraph and its text, if the
	 * paragraph inherits no indentation.
	 */
	public static final double BULLET_INDENT = 18;

	private static final String LINE_BREAK = "\\r\\n|\\r|\\n";

	private final DrawingWriter _drawing;

	/**
	 * Creates a {@link TextWriter}.
	 *
	 * @param drawing
	 *        Writes the colors of the text.
	 */
	public TextWriter(DrawingWriter drawing) {
		_drawing = drawing;
	}

	/**
	 * Replaces the text of the given shape.
	 *
	 * @param shape
	 *        The shape to fill.
	 * @param text
	 *        The text to write.
	 * @param path
	 *        The path of the text in the description, for error messages.
	 */
	public void write(XSLFTextShape shape, TextSpec text, String path) {
		write(shape, text, path, 0);
	}

	/**
	 * Replaces the text of the given shape with a part of a text.
	 *
	 * @param shape
	 *        The shape to fill.
	 * @param text
	 *        The part of the text to write.
	 * @param path
	 *        The path of the whole text in the description, for error messages.
	 * @param firstParagraph
	 *        The index of the first paragraph of the part in the whole text, for error messages.
	 */
	public void write(XSLFTextShape shape, TextSpec text, String path, int firstParagraph) {
		writeBody(shape, text);
		shape.clearText();
		List<ParagraphSpec> paragraphs = text.paragraphs();
		for (int n = 0; n < paragraphs.size(); n++) {
			String paragraphPath =
				paragraphs.size() == 1 && firstParagraph == 0 ? path : path + "[" + (firstParagraph + n) + "]";
			writeParagraph(shape.addNewTextParagraph(), paragraphs.get(n), text, paragraphPath);
		}
	}

	/**
	 * Adds an empty text body to the given shape, if it has none.
	 *
	 * <p>
	 * A shape created without text has no text body, which must contain body properties.
	 * </p>
	 */
	public static void addTextBody(CTShape shape) {
		CTTextBody body = shape.isSetTxBody() ? shape.getTxBody() : shape.addNewTxBody();
		if (body.getBodyPr() == null) {
			body.addNewBodyPr();
		}
		if (!body.isSetLstStyle()) {
			body.addNewLstStyle();
		}
	}

	private static void writeBody(XSLFTextShape shape, TextSpec text) {
		if (text.anchor() != null) {
			shape.setVerticalAlignment(verticalAlignment(text.anchor()));
		}
		InsetsSpec insets = text.insets();
		if (insets != null) {
			shape.setInsets(new Insets2D(insets.top(), insets.left(), insets.bottom(), insets.right()));
		}
		if (text.wrap() != null) {
			shape.setWordWrap(text.wrap().booleanValue());
		}
	}

	private static VerticalAlignment verticalAlignment(TextAnchor anchor) {
		switch (anchor) {
			case TOP:
				return VerticalAlignment.TOP;
			case MIDDLE:
				return VerticalAlignment.MIDDLE;
			default:
				return VerticalAlignment.BOTTOM;
		}
	}

	private void writeParagraph(XSLFTextParagraph paragraph, ParagraphSpec spec, TextSpec text, String path) {
		TextAlign align = spec.align() != null ? spec.align() : text.align();
		if (align != null) {
			paragraph.setTextAlign(textAlign(align));
		}
		if (spec.level() != null) {
			paragraph.setIndentLevel(spec.level().intValue());
		}
		CTTextParagraphProperties pPr = paragraph.getXmlObject().isSetPPr()
			? paragraph.getXmlObject().getPPr() : paragraph.getXmlObject().addNewPPr();
		if (spec.lineSpacing() != null) {
			pPr.addNewLnSpc().addNewSpcPct()
				.setVal(Integer.valueOf((int) DrawingWriter.percent(spec.lineSpacing().doubleValue())));
		}
		if (spec.spaceBefore() != null) {
			pPr.addNewSpcBef().addNewSpcPts().setVal(points100(spec.spaceBefore().doubleValue()));
		}
		if (spec.spaceAfter() != null) {
			pPr.addNewSpcAft().addNewSpcPts().setVal(points100(spec.spaceAfter().doubleValue()));
		}
		if (spec.bullet() != null) {
			writeBullet(paragraph, pPr, spec.bullet(), path + "." + PresentationData.BULLET);
		}

		RunStyle paragraphStyle = spec.style().inheritFrom(text.style());
		RunStyle lastStyle = paragraphStyle;
		List<RunSpec> runs = spec.runs();
		for (int n = 0; n < runs.size(); n++) {
			RunSpec run = runs.get(n);
			String runPath = runs.size() == 1 ? path : path + "." + PresentationData.RUNS + "[" + n + "]";
			lastStyle = run.style().inheritFrom(paragraphStyle);
			String[] lines = run.text().split(LINE_BREAK, -1);
			for (int line = 0; line < lines.length; line++) {
				if (line > 0) {
					paragraph.addLineBreak();
				}
				XSLFTextRun textRun = paragraph.addNewTextRun();
				textRun.setText(lines[line]);
				CTTextCharacterProperties rPr = textRun.getRPr(true);
				if (rPr.isSetLang()) {
					// The language is inherited from the defaults of the presentation.
					rPr.unsetLang();
				}
				writeCharacterProperties(rPr, lastStyle, runPath);
				if (run.hyperlink() != null) {
					textRun.createHyperlink().setAddress(run.hyperlink());
				}
			}
		}
		if (!lastStyle.isInherit()) {
			writeCharacterProperties(paragraph.getXmlObject().addNewEndParaRPr(), lastStyle, path);
		}
	}

	private void writeBullet(XSLFTextParagraph paragraph, CTTextParagraphProperties pPr, BulletSpec bullet,
			String path) {
		if (!bullet.visible()) {
			paragraph.setBullet(false);
			return;
		}
		boolean inherited = paragraph.isBullet() && paragraph.getBulletCharacter() != null;
		if (bullet.character() != null || !inherited) {
			paragraph.setBullet(true);
			paragraph.setBulletCharacter(bullet.character() != null ? bullet.character() : DEFAULT_BULLET);
			Double indent = paragraph.getIndent();
			if (indent == null || indent.doubleValue() == 0) {
				Double margin = paragraph.getLeftMargin();
				paragraph.setLeftMargin(Double.valueOf((margin == null ? 0 : margin.doubleValue()) + BULLET_INDENT));
				paragraph.setIndent(Double.valueOf(-BULLET_INDENT));
			}
		}
		if (bullet.color() != null) {
			CTColor color = pPr.isSetBuClr() ? pPr.getBuClr() : pPr.addNewBuClr();
			_drawing.writeColor(new DrawingWriter.ColorTarget(color::addNewSchemeClr, color::addNewSrgbClr),
				bullet.color(), path + "." + PresentationData.COLOR);
		}
	}

	private static org.apache.poi.sl.usermodel.TextParagraph.TextAlign textAlign(TextAlign align) {
		switch (align) {
			case LEFT:
				return org.apache.poi.sl.usermodel.TextParagraph.TextAlign.LEFT;
			case CENTER:
				return org.apache.poi.sl.usermodel.TextParagraph.TextAlign.CENTER;
			case RIGHT:
				return org.apache.poi.sl.usermodel.TextParagraph.TextAlign.RIGHT;
			default:
				return org.apache.poi.sl.usermodel.TextParagraph.TextAlign.JUSTIFY;
		}
	}

	/**
	 * Writes the given character formatting.
	 *
	 * @param rPr
	 *        The character properties of a text run or a paragraph end.
	 * @param style
	 *        The formatting, of which the given properties are written.
	 * @param path
	 *        The path of the formatted text in the description, for error messages.
	 */
	public void writeCharacterProperties(CTTextCharacterProperties rPr, RunStyle style, String path) {
		if (style.bold() != null) {
			rPr.setB(style.bold().booleanValue());
		}
		if (style.italic() != null) {
			rPr.setI(style.italic().booleanValue());
		}
		if (style.underline() != null) {
			rPr.setU(style.underline().booleanValue() ? STTextUnderlineType.SNG : STTextUnderlineType.NONE);
		}
		if (style.caps() != null) {
			rPr.setCap(style.caps().booleanValue() ? STTextCapsType.ALL : STTextCapsType.NONE);
		}
		if (style.size() != null) {
			rPr.setSz(points100(style.size().doubleValue()));
		}
		if (style.characterSpacing() != null) {
			rPr.setSpc(points100(style.characterSpacing().doubleValue()));
		}
		if (style.color() != null) {
			if (rPr.isSetSolidFill()) {
				rPr.unsetSolidFill();
			}
			CTSolidColorFillProperties fill = rPr.addNewSolidFill();
			_drawing.writeColor(new DrawingWriter.ColorTarget(fill::addNewSchemeClr, fill::addNewSrgbClr),
				style.color(), path + "." + PresentationData.COLOR);
		}
		if (style.font() != null) {
			(rPr.isSetLatin() ? rPr.getLatin() : rPr.addNewLatin()).setTypeface(style.font());
		}
	}

	private static int points100(double points) {
		return (int) Math.round(points * DrawingWriter.POINT_100);
	}

}
