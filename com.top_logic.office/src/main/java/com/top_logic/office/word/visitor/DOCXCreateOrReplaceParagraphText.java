/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word.visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.docx4j.jaxb.Context;
import org.docx4j.wml.Br;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.ObjectFactory;
import org.docx4j.wml.P;
import org.docx4j.wml.R;
import org.docx4j.wml.RPr;
import org.docx4j.wml.Text;

import com.top_logic.basic.StringServices;

/**
 * Visits the first {@link P}-element and replaces all it's content with a new {@link Text} -element
 * containing the given "_text" if at least one {@link Text} does exist in this {@link P}. If no
 * {@link Text} exists in this {@link P}-element, it creates a new {@link Text} containing the given
 * "_text".
 * 
 * @author <a href="mailto:jes@top-logic.com">jes</a>
 */
public class DOCXCreateOrReplaceParagraphText extends DOCXVisitor {

	private final String _text;

	/**
	 * Create a new {@link DOCXCreateOrReplaceParagraphText}-visitor
	 * 
	 * @param text
	 *        The {@link String} used to set as first {@link Text}s value
	 */
	public DOCXCreateOrReplaceParagraphText(String text) {
		_text = text;
	}

	@Override
	protected boolean onVisit(P paragraph) {
		Text firstText = getFirstText(paragraph);
		if (firstText == null) {
			// no text-element available in this paragraph --> create one
			createText(paragraph, null);
		}else{
			// text-element found in paragraph --> delete content and create new
			R firstRun = getFirstRun(paragraph);
			RPr runProperties = null;
			if (firstRun != null) {
				runProperties = firstRun.getRPr();
			}
			paragraph.getContent().removeAll(paragraph.getContent());
			createText(paragraph, runProperties);
		}
		return false; // don't go on with this visitor
	}

	private boolean createText(P paragraph, RPr properties) {
		ObjectFactory factory = Context.getWmlObjectFactory();
		R newR = factory.createR();

		List<Object> content = newR.getContent();

		List<?> parts = textWithBr(_text);
		for (Object object : parts) {
			content.add(object);
		}

		if (properties != null) {
			newR.setRPr(properties);
		}
		paragraph.getContent().add(newR);
		return true;
	}

	/**
	 * Split a string with newline characters '\n' into a list of {@link Text} and {@link Br}s.
	 */
	protected static List<Object> textWithBr(String string) {
		ObjectFactory factory = Context.getWmlObjectFactory();

		int parts = StringServices.count(string, "\n");
		if (parts == 0) {
			Text text = factory.createText();
			text.setValue(string);
			return Collections.<Object> singletonList(text);
		}

		List<Object> list = new ArrayList<>(parts * 2);
		StringBuilder buffer = new StringBuilder();
		for (int i = 0, length = string.length(); i < length; i++) {
			char c = string.charAt(i);
			if ('\n' == c) {
				addBufferedText(buffer, list, factory);
				list.add(factory.createBr());
			} else {
				buffer.append(c);
			}
		}

		addBufferedText(buffer, list, factory);

		return list;
	}

	private static void addBufferedText(StringBuilder buffer, List<Object> content, ObjectFactory factory) {
		if (buffer.length() > 0) {
			Text text = factory.createText();
			text.setValue(buffer.toString());
			content.add(text);
			buffer.setLength(0);
		}
	}

	/**
	 * Returns the first {@link Text} element in the given {@link ContentAccessor}-structure.
	 * 
	 * @param part
	 *        The {@link ContentAccessor} from which the first {@link Text} element has to be
	 *        returned.
	 * @return The first {@link Text} element within the given {@link ContentAccessor}.
	 */
	public static Text getFirstText(ContentAccessor part) {
		DOCXFindFirstText visitor = new DOCXFindFirstText();
		DOCXVisitor.visit(part, visitor);
		return visitor.getText();
	}

	/**
	 * Returns the first {@link R} element in the given {@link ContentAccessor}-structure.
	 * 
	 * @param part
	 *        The {@link ContentAccessor} from which the first {@link R} element has to be returned.
	 * @return The first {@link R} element within the given {@link ContentAccessor}.
	 */
	public static R getFirstRun(ContentAccessor part) {
		DOCXFindFirstRun visitor = new DOCXFindFirstRun();
		DOCXVisitor.visit(part, visitor);
		return visitor.getRun();
	}
}