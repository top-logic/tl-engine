/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.visitor;

import java.util.List;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.FooterPart;
import org.docx4j.openpackaging.parts.WordprocessingML.HeaderPart;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.vml.CTShape;
import org.docx4j.vml.CTTextbox;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.P;
import org.docx4j.wml.Pict;
import org.docx4j.wml.R;
import org.docx4j.wml.Tbl;
import org.docx4j.wml.Tc;
import org.docx4j.wml.Text;
import org.docx4j.wml.Tr;

/**
 * Visits the parts of DOCX4J-documents (parts of {@link WordprocessingMLPackage}).
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXVisitor {

	/**
	 * Creates a {@link DOCXVisitor}
	 */
	public DOCXVisitor() {
	}

	/**
	 * Decide where to delegate a part of a DOCX4J document
	 * 
	 * @return Returns <code>true</code> if the given part is supported, <code>false</code>
	 *         otherwise
	 */
	public boolean accept(Object part) {

		if (part instanceof MainDocumentPart) {
			return onVisit((MainDocumentPart) part);
		}

		if (part instanceof FooterPart) {
			return onVisit((FooterPart) part);
		}

		if (part instanceof HeaderPart) {
			return onVisit((HeaderPart) part);
		}

		if (part instanceof JAXBElement<?>) {
			return onVisit((JAXBElement<?>) part);
		}

		if (part instanceof P) {
			return onVisit((P) part);
		}

		if (part instanceof Tbl) {
			return onVisit((Tbl) part);
		}

		if (part instanceof Tc) {
			return onVisit((Tc) part);
		}

		if (part instanceof R) {
			return onVisit((R) part);
		}

		if (part instanceof Text) {
			return onVisit((Text) part);
		}

		if (part instanceof Tr) {
			return onVisit((Tr) part);
		}

		if (part instanceof Pict) {
			return onVisit((Pict) part);
		}

		if (part instanceof CTShape) {
			return onVisit((CTShape) part);
		}

		if (part instanceof CTTextbox) {
			return onVisit((CTTextbox) part);
		}

		// ignore unknown classes
		return true;
	}

	/**
	 * Subclasses can do additional stuff here
	 * 
	 * @param element
	 *        The element to perform actions on
	 * @return Returns <code>true</code> if the visiting should go on, <code>false</code> otherwise
	 */
	protected boolean onVisit(MainDocumentPart element) {
		return true;
	}

	/**
	 * Subclasses can do additional stuff here
	 * 
	 * @param element
	 *        The element to perform actions on
	 * @return Returns <code>true</code> if the visiting should go on, <code>false</code> otherwise
	 */
	protected boolean onVisit(FooterPart element) {
		return true;
	}

	/**
	 * Subclasses can do additional stuff here
	 * 
	 * @param element
	 *        The element to perform actions on
	 * @return Returns <code>true</code> if the visiting should go on, <code>false</code> otherwise
	 */
	protected boolean onVisit(HeaderPart element) {
		return true;
	}

	/**
	 * Subclasses can do additional stuff here
	 * 
	 * @param element
	 *        The element to perform actions on
	 * @return Returns <code>true</code> if the visiting should go on, <code>false</code> otherwise
	 */
	protected boolean onVisit(Tbl element) {
		return true;
	}

	/**
	 * Subclasses can do additional stuff here
	 * 
	 * @param element
	 *        The element to perform actions on
	 * @return Returns <code>true</code> if the visiting should go on, <code>false</code> otherwise
	 */
	protected boolean onVisit(Tr element) {
		return true;
	}

	/**
	 * Subclasses can do additional stuff here
	 * 
	 * @param element
	 *        The element to perform actions on
	 * @return Returns <code>true</code> if the visiting should go on, <code>false</code> otherwise
	 */
	protected boolean onVisit(JAXBElement<?> element) {
		return true;
	}

	/**
	 * Subclasses can do additional stuff here
	 * 
	 * @param element
	 *        The element to perform actions on
	 * @return Returns <code>true</code> if the visiting should go on, <code>false</code> otherwise
	 */
	protected boolean onVisit(Tc element) {
		return true;
	}

	/**
	 * Subclasses can do additional stuff here
	 * 
	 * @param paragraph
	 *        The element to perform actions on
	 * @return Returns <code>true</code> if the visiting should go on, <code>false</code> otherwise
	 */
	protected boolean onVisit(P paragraph) {
		return true;
	}

	/**
	 * Subclasses can do additional stuff here
	 * 
	 * @param element
	 *        The element to perform actions on
	 * @return Returns <code>true</code> if the visiting should go on, <code>false</code> otherwise
	 */
	protected boolean onVisit(R element) {
		return true;
	}

	/**
	 * Subclasses can do additional stuff here
	 * 
	 * @param element
	 *        The element to perform actions on
	 * @return Returns <code>true</code> if the visiting should go on, <code>false</code> otherwise
	 */
	protected boolean onVisit(Text element) {
		return true;
	}

	/**
	 * Subclasses can do additional stuff here
	 * 
	 * @param element
	 *        The element to perform actions on
	 * @return Returns <code>true</code> if the visiting should go on, <code>false</code> otherwise
	 */
	protected boolean onVisit(Pict element) {
		return true;
	}

	/**
	 * Subclasses can do additional stuff here
	 * 
	 * @param element
	 *        The element to perform actions on
	 * @return Returns <code>true</code> if the visiting should go on, <code>false</code> otherwise
	 */
	protected boolean onVisit(CTShape element) {
		return true;
	}

	/**
	 * Subclasses can do additional stuff here
	 * 
	 * @param element
	 *        The element to perform actions on
	 * @return Returns <code>true</code> if the visiting should go on, <code>false</code> otherwise
	 */
	protected boolean onVisit(CTTextbox element) {
		return true;
	}

	/**
	 * Visit the elements of the given DOCX4J part
	 * 
	 * @param part
	 *        The part to visit
	 * @param visitor
	 *        The visitor to use for visiting
	 * @return Returns <code>true</code> if the given part is accepted, <code>false</code>
	 *         otherwise
	 */
	public static boolean visit(Object part, DOCXVisitor visitor) {

		if (!visitor.accept(part)) {
			return false;
		}

		if (part instanceof ContentAccessor) {
			List<Object> contents = ((ContentAccessor) part).getContent();

			for (Object object : contents) {
				if (!visit(object, visitor)) {
					return false;
				}
			}
		}
		else if (part instanceof JAXBElement<?>) {
			JAXBElement<?> element = (JAXBElement<?>) part;
			Object value = element.getValue();
			if (!visit(value, visitor)) {
				return false;
			}
		}
		else if (part instanceof Pict) {
			Pict element = (Pict) part;
			List<Object> anyAndAny = element.getAnyAndAny();
			for (Object object : anyAndAny) {
				if (!visit(object, visitor)) {
					return false;
				}
			}
		}
		else if (part instanceof CTShape) {
			CTShape element = (CTShape) part;
			List<JAXBElement<?>> pathOrFormulasOrHandles = element.getPathOrFormulasOrHandles();
			for (JAXBElement<?> object : pathOrFormulasOrHandles) {
				if (!visit(object, visitor)) {
					return false;
				}
			}
		}
		else if (part instanceof CTTextbox) {
			CTTextbox element = (CTTextbox) part;
			CTTxbxContent txbxContent = element.getTxbxContent();
			List<Object> egBlockLevelElts = txbxContent.getEGBlockLevelElts();
			for (Object object : egBlockLevelElts) {
				if (!visit(object, visitor)) {
					return false;
				}
			}
		}

		return true;
	}
}