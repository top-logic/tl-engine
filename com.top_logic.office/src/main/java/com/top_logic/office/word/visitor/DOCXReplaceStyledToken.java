/*
 * SPDX-FileCopyrightText: 2014 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.visitor;

import java.awt.Color;

import org.docx4j.wml.CTShd;
import org.docx4j.wml.P;
import org.docx4j.wml.PPr;
import org.docx4j.wml.Tc;
import org.docx4j.wml.TcPr;
import org.docx4j.wml.Text;

import com.top_logic.base.office.ppt.StyledValue;
import com.top_logic.basic.StringServices;

/**
 * Does additionally to the text replacing of {@link DOCXReplaceToken} set a background-color.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXReplaceStyledToken extends DOCXReplaceToken {

	private StyledValue _styleConfig;

	/** Creates a new {@link DOCXReplaceStyledToken} */
	public DOCXReplaceStyledToken(String token, StyledValue styledValue) {
		super(token, (String) styledValue.getValue());
		_styleConfig = styledValue;
	}

	private String asHex(Color color) {
		String colorHex = Integer.toHexString(color.getRGB());
		return colorHex.substring(2, colorHex.length());
	}

	private void applyStyle(Tc tablecell) {
		String backgroundColor = asHex(_styleConfig.getBackgroundColor());
		// Get or create properties
		TcPr tcPr = tablecell.getTcPr();
		if (tcPr == null) {
			tcPr = new TcPr();
			tablecell.setTcPr(tcPr);
		}

		// Get or create style container
		CTShd shd = tcPr.getShd();
		if (shd == null) {
			shd = new CTShd();
			tcPr.setShd(shd);
		}
		shd.setFill(backgroundColor);
	}

	private void applyStyle(P paragraph) {
		String backgroundColor = asHex(_styleConfig.getBackgroundColor());
		// Get or create properties
		PPr pPr = paragraph.getPPr();
		if (pPr == null) {
			pPr = new PPr();
			paragraph.setPPr(pPr);
		}

		// Get or create style container
		CTShd shd = pPr.getShd();
		if (shd == null) {
			shd = new CTShd();
			pPr.setShd(shd);
		}
		shd.setFill(backgroundColor);
	}

	@Override
	protected boolean onVisit(Text element) {
		String oldText = element.getValue();
		if (!StringServices.isEmpty(oldText)) {
			if (oldText.contains(_token)) {
				// set element color
				Object paragraphParent = getParagraph().getParent();
				if (paragraphParent instanceof Tc) {
					applyStyle((Tc) paragraphParent);
				} else {
					applyStyle(getParagraph());
				}
				// replace token with text
				super.onVisit(element);
			}
		}

		return true; // go on with replacements until end of document
	}
}
