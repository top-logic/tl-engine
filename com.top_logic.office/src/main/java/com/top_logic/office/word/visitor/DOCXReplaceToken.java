/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.visitor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.Body;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.Ftr;
import org.docx4j.wml.Hdr;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;

import com.top_logic.basic.StringServices;
import com.top_logic.util.Utils;

/**
 * Replaces all {@link Text}-values that contain the given {@link #_token} with the given
 * {@link #_replacement}.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXReplaceToken extends DOCXVisitor {

	Map<P, Map<Integer, P>> _paragraphsToAdd;

	public Map<P, Map<Integer, P>> getParagraphsToAdd() {
		return (_paragraphsToAdd);
	}

	/** the string to replace */
	protected final String _token;

	/** the string to replace the _token with */
	protected final String _replacement;

	private boolean _tokenReplaced;

	private P _paragraph;

	/**
	 * Create a new instance of {@link DOCXReplaceToken}-visitor
	 * 
	 * @param token
	 *        The string to replace
	 * @param replacement
	 *        The replacement to replace the token with
	 */
	public DOCXReplaceToken(String token, String replacement) {
		_token = token;
		_replacement = replacement;
		_paragraphsToAdd = new HashMap<>();
	}

	/** returns <code>true</code> if a token was replaced, <code>false</code> otherwise */
	public boolean isTokenReplaced() {
		return (_tokenReplaced);
	}

	/** holds the actual processed paragraph */
	@Override
	protected boolean onVisit(P paragraph) {
		_paragraph = paragraph;
		return true;
	}

	@Override
	protected boolean onVisit(Text element) {
		String oldText = element.getValue();

		if (!StringServices.isEmpty(oldText)) {
				
//			if (_replacement.contains("\n")) {
				// TODO FSC/JES: think about this again
//				replaceMultilineText(element, oldText);
//			} else {
				if (oldText.contains(_token)) {
					String newText = getNewText(oldText);
					if (!Utils.equals(newText, oldText)) {
						element.setValue(newText);
						_tokenReplaced = true;
					}
				}
//			}
		}

		return true; // go on with replacements until end of document
	}

	private void replaceMultilineText(Text element, String oldText) {
		Object paragraphParent = getParagraph().getParent();
		List<Object> mainContent = new ArrayList<>();
		if (paragraphParent instanceof Hdr) {
			mainContent = ((Hdr) paragraphParent).getContent();
		} else if (paragraphParent instanceof Ftr) {
			mainContent = ((Ftr) paragraphParent).getContent();
		} else if (paragraphParent instanceof Tc) {
			mainContent = ((Tc) paragraphParent).getContent();
		} else if (paragraphParent instanceof Body) {
			mainContent = ((Body) paragraphParent).getContent();
		} else if (paragraphParent instanceof CTTxbxContent) {
			mainContent = ((CTTxbxContent) paragraphParent).getEGBlockLevelElts();
		} else if (paragraphParent instanceof JAXBElement<?>) {
			mainContent = getParagraph().getContent();
		}
		int index = mainContent.indexOf(getParagraph());

		if (getParagraph() != null && index >= 0) {
			// TODO use "contains()" here too
			if (oldText.equals(_token)) {
				Map<Integer, P> toAdd = new HashMap<>();
				ObjectFactory factory = Context.getWmlObjectFactory();
				int counter = 0;
				String[] replacementParts = _replacement.split("\n");
				for (String newText : replacementParts) {
					if (counter == 0) {
						element.setValue(newText);
						counter++;
					} else {
						P createdP = createParagraphContainingText(factory, newText);
						toAdd.put(index + counter, createdP);
						counter++;
					}
				}
				_paragraphsToAdd.put(getParagraph(), toAdd);
				_tokenReplaced = true;
			}
		}
	}

	/**
	 * Replaces each substring of the given string "oldText" that matches the {@link #_token} with
	 * the {@link #_replacement}.
	 * 
	 * @param oldText
	 *        The string in which to replace the {@link #_token}s with the {@link #_replacement}
	 * @return The replaced string
	 */
	protected String getNewText(String oldText) {
		return oldText.replaceAll(_token, _replacement);
	}

	private P createParagraphContainingText(ObjectFactory factory, String text) {
		P createdP = factory.createP();
		R createdR = factory.createR();
		Text createdText = factory.createText();
		createdText.setValue(text);
		createdR.getContent().add(createdText);
		createdP.getContent().add(createdR);
		createdP.setPPr(getParagraph().getPPr());
		return createdP;
	}

	/**
	 * Returns the currently visited paragraph.
	 * 
	 * @return May be <code>null</code> when no paragraph parent is visited.
	 */
	protected final P getParagraph() {
		return _paragraph;
	}
}