/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.tokenReplacer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.Part;
import org.docx4j.openpackaging.parts.PartName;
import org.docx4j.openpackaging.parts.Parts;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.P;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;

import com.top_logic.base.office.AbstractOffice.TokenReplacer;
import com.top_logic.base.office.ppt.StyledValue;
import com.top_logic.office.word.visitor.DOCXReplaceStyledToken;
import com.top_logic.office.word.visitor.DOCXReplaceToken;
import com.top_logic.office.word.visitor.DOCXVisitor;

/**
 * {@link TokenReplacer} that replaces all {@link Text}s matching the given token with the given
 * replacement.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXStringReplacer implements TokenReplacer {
	@Override
	public boolean replaceToken(String token, Object replacement, Object anApplication, Object aDocument,
			Stack aReferencesStack) throws Exception {

		WordprocessingMLPackage document = (WordprocessingMLPackage) aDocument;

		// first replace tokens in mainDocumentPart and all other parts
		boolean hasBeenReplaced = false;
		Parts parts = document.getParts();
		HashMap<PartName, Part> partsList = parts.getParts();
		for (Part part : partsList.values()) {
			if (part instanceof MainDocumentPart || part instanceof FooterPart || part instanceof HeaderPart) {
				hasBeenReplaced |= doTextReplacement(token, replacement, (ContentAccessor) part);
			} else {
				// ignore other unknown parts
			}
		}

		return hasBeenReplaced;
	}

	/**
	 * Replace given tokens in given {@link Part} with given replacement.
	 * 
	 * @param token
	 *        The token to be replaced
	 * @param replacement
	 *        The replacement used to substitute the token with
	 * @param part
	 *        The {@link Part} in which the replacements have to be performed
	 * @return <code>true</code> if a replacement has been performed, <code>false</code> otherwise
	 */
	protected boolean doTextReplacement(String token, Object replacement, ContentAccessor part) {
		return replaceText(token, replacement, part);
	}

	/**
	 * Replaces all {@link Text}s in the given {@link ContentAccessor} that match the given token
	 * with the given replacement.
	 * 
	 * @param token
	 *        The pattern to search for.
	 * @param replacement
	 *        The replacement used to replace the found tokens with.
	 * @param part
	 *        The {@link ContentAccessor} in which the replacements are performed.
	 * @return <code>true</code> if a replacement has been performed, <code>false</code> otherwise.
	 */
	private boolean replaceText(String token, Object replacement, ContentAccessor part) {
		if (replacement instanceof StyledValue) {
			DOCXReplaceStyledToken visitor = new DOCXReplaceStyledToken(token, (StyledValue) replacement);
			DOCXVisitor.visit(part, visitor);
			return visitor.isTokenReplaced();
		}
		DOCXReplaceToken visitor = new DOCXReplaceToken(token, (String) replacement);
		DOCXVisitor.visit(part, visitor);
		// TODO FSC/JES: think about this again
//		addMultilineText(visitor);
		return visitor.isTokenReplaced();
	}

	private void addMultilineText(DOCXReplaceToken visitor) {
		Map<P, Map<Integer, P>> paragraphsToAdd = visitor.getParagraphsToAdd();
		int hdrCounter = 0;
		int ftrCounter = 0;
		int tcCounter = 0;
		int bodyCounter = 0;
		int txbxCounter = 0;
		for (P object : paragraphsToAdd.keySet()) {
			Map<Integer, P> map = paragraphsToAdd.get(object);
			Object parent = object.getParent();
			List<Object> mainContent = new ArrayList<>();
			int innerCounter = 0;

			// add paragraphs
			for (Integer index : map.keySet()) {
				if (parent instanceof Hdr) {
					mainContent = ((Hdr) parent).getContent();
					mainContent.add(index + hdrCounter, map.get(index));
					innerCounter++;
				} else if (parent instanceof Ftr) {
					mainContent = ((Ftr) parent).getContent();
					mainContent.add(index + ftrCounter, map.get(index));
					innerCounter++;
				} else if (parent instanceof Tc) {
					mainContent = ((Tc) parent).getContent();
					mainContent.add(index + tcCounter, map.get(index));
					innerCounter++;
				} else if (parent instanceof Body) {
					mainContent = ((Body) parent).getContent();
					mainContent.add(index + bodyCounter, map.get(index));
					innerCounter++;
				} else if (parent instanceof CTTxbxContent) {
					mainContent = ((CTTxbxContent) parent).getEGBlockLevelElts();
					mainContent.add(index + txbxCounter, map.get(index));
					innerCounter++;
				}
			}

			// after adding paragraphs increase specific content counter
			if (parent instanceof Hdr) {
				hdrCounter += innerCounter;
			} else if (parent instanceof Ftr) {
				ftrCounter += innerCounter;
			} else if (parent instanceof Tc) {
				tcCounter += innerCounter;
			} else if (parent instanceof Body) {
				bodyCounter += innerCounter;
			} else if (parent instanceof CTTxbxContent) {
				txbxCounter += innerCounter;
			}

		}
	}
}