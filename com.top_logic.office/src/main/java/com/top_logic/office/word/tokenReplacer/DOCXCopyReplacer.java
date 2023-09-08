/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.tokenReplacer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.docx4j.XmlUtils;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.P;

import com.top_logic.base.office.AbstractOffice.TokenReplacer;
import com.top_logic.base.office.word.CopyReplacerConfig;
import com.top_logic.office.word.visitor.DOCXFindTextEquals;
import com.top_logic.office.word.visitor.DOCXVisitor;

/**
 * This {@link TokenReplacer} copies a part of a given {@link WordprocessingMLPackage} -document,
 * replaces tokens in this part with the replacements found in the given CopyReplaceronfig
 * "aReplacement" (which is assumed to be a {@link CopyReplacerConfig}) and adds a counting to them.
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXCopyReplacer implements TokenReplacer {

	@Override
	public boolean replaceToken(String aToken, Object aReplacement, Object anApplication, Object aDocument,
			Stack aReferencesStack) throws Exception {

		assert aReplacement instanceof CopyReplacerConfig;

		// 1. get the main mainDocumentPart of the template
		WordprocessingMLPackage document = (WordprocessingMLPackage) aDocument;
		MainDocumentPart mainDocumentPart = document.getMainDocumentPart();

		// 2. get the copyReplacerConfig settings
		CopyReplacerConfig copyConfig = (CopyReplacerConfig) aReplacement;
		String startToken = aToken;
		String endToken = copyConfig.endToken != null ? copyConfig.endToken : aToken + "_END";
		int numberOfCopies = copyConfig.copyNo;

		// 3. find start-/end- element
		P startElement; // to be removed
		P endElement; // to be removed
		startElement = findElement(mainDocumentPart, startToken);
		endElement = findElement(mainDocumentPart, endToken);
		if (startElement == null || endElement == null) {
			// token not found!
			return false;
		}

		// 4. get elements to copy
		List<Object> content = mainDocumentPart.getContent();
		int idxOfStartElement = content.indexOf(startElement);
		int idxOfEndElement = content.indexOf(endElement);
		List<Object> elementsToCopy = new ArrayList<>(content.subList(idxOfStartElement + 1, idxOfEndElement));

		// 5. remove all elements, because this should be done when numberOfCopies == 0
		deleteCopedSection(startElement, endElement, content, elementsToCopy);

		// 6. add elements that should be copied
		addCopiedSection(numberOfCopies, content, idxOfStartElement, elementsToCopy);

		// 7. replace tokens with replacements defined in CopyReplacerConfig and add a counting
		replaceTokensAddCounting(anApplication, aDocument, aReferencesStack, copyConfig);

		return true;
	}

	private void replaceTokensAddCounting(Object anApplication, Object aDocument, Stack aReferencesStack,
			CopyReplacerConfig copyConfig) throws Exception {
		if (copyConfig.tagReplacements != null) {
			Iterator theReplIt = copyConfig.tagReplacements.keySet().iterator();
			while (theReplIt.hasNext()) {
				String theToken = (String) theReplIt.next();
				String theRepl = (String) copyConfig.tagReplacements.get(theToken);
				DOCXCountingStringReplacer theReplacer = new DOCXCountingStringReplacer();
				theReplacer.replaceToken(theToken, theRepl, anApplication, aDocument, aReferencesStack);
			}
		}
	}

	private void addCopiedSection(int numberOfCopies, List<Object> content, int idxOfStartElement,
			List<Object> elementsToCopy) {
		int pointer = idxOfStartElement;
		for (int i = 0; i < numberOfCopies; i++) {
			List<Object> copiedSection = copyElements(elementsToCopy);
			int sectionSize = copiedSection.size();
			if (sectionSize > 0) {
				content.addAll(pointer, copiedSection);
				pointer += sectionSize;
			}
		}
	}

	private void deleteCopedSection(P startElement, P endElement, List<Object> content, List<Object> elementsToCopy) {
		content.remove(startElement);
		content.removeAll(elementsToCopy);
		content.remove(endElement);
	}

	private P findElement(MainDocumentPart mainDocumentPart, String token) {
		// Assume that copy replacer token is the only content in one paragraph
		DOCXFindTextEquals visitor = new DOCXFindTextEquals(token);
		DOCXVisitor.visit(mainDocumentPart, visitor);
		P startElement = visitor.getParagraph();
		return startElement;
	}

	private List<Object> copyElements(List<Object> elementsToCopy) {
		List<Object> copiedElements = new ArrayList<>();
		if (!elementsToCopy.isEmpty()) {
			int numberCopiedElements = elementsToCopy.size();
			for (int n = 0; n < numberCopiedElements; n++) {
				Object element = elementsToCopy.get(n);
				Object elementCopy = XmlUtils.deepCopy(element);
				copiedElements.add(elementCopy);
			}
		}
		return copiedElements;
	}

}
