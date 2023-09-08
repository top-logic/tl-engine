/*
 * SPDX-FileCopyrightText: 2012 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.office.word;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import jakarta.xml.bind.JAXBElement;

import org.docx4j.openpackaging.io.SaveToZipFile;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.vml.CTFill;
import org.docx4j.vml.CTShape;
import org.docx4j.vml.CTTextPath;
import org.docx4j.vml.CTTextbox;
import org.docx4j.wml.CTTxbxContent;
import org.docx4j.wml.ContentAccessor;
import org.docx4j.wml.Pict;
import org.docx4j.wml.Text;

import com.top_logic.base.office.word.WordAccess;
import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.CollectionUtil;
import com.top_logic.basic.col.Mapping;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.office.word.tokenReplacer.DOCXCopyReplacer;
import com.top_logic.office.word.tokenReplacer.DOCXStringReplacer;
import com.top_logic.office.word.tokenReplacer.DOCXTableReplacer;

/**
 * Access to word documents via Docx4j.
 * 
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
public class DOCXWordAccess extends WordAccess {

	/**
	 * Create a new instance of DOCXWordAccess
	 */
	@CalledByReflection
	public DOCXWordAccess(InstantiationContext context, ServiceConfiguration config) {
		super(context, config);

		addTokenReplacer("VALUE_", new DOCXStringReplacer());
		addTokenReplacer("TABLE_", new DOCXTableReplacer());
		addTokenReplacer("COPY_", new DOCXCopyReplacer());
	}

	@Override
	public Mapping getStringMapping() {
		return StringMapping.INSTANCE;
	}

	/**
	 * Testmethod to print the structure of a docx4j document
	 */
	public static void print(Object child, int indent) {

		for (int i = 0; i < indent; i++) {
			System.out.print("    ");
		}

		System.out.println(child.getClass().getCanonicalName());

		if (child instanceof Text) {
			for (int i = 0; i < indent; i++) {
				System.out.print("    ");
			}
			System.out.print("    ");
			System.err.println("\"" + ((Text) child).getValue() + "\"");
		}

		if (child instanceof ContentAccessor) {
			ContentAccessor element = (ContentAccessor) child;
			List<Object> contents = element.getContent();
			for (Object content : contents) {
				print(content, indent + 1);
			}
		}
		else if (child instanceof JAXBElement<?>) {
			JAXBElement<?> element = (JAXBElement<?>) child;
			Object value = element.getValue();

			print(value, indent + 1);
		}
		else if (child instanceof Pict) {
			Pict element = (Pict) child;
			List<Object> anyAndAny = element.getAnyAndAny();
			for (Object object : anyAndAny) {
				print(object, indent + 1);
			}
		}
		else if (child instanceof CTShape) {
			CTShape element = (CTShape) child;
			List<JAXBElement<?>> pathOrFormulasOrHandles = element.getPathOrFormulasOrHandles();
			for (JAXBElement<?> object : pathOrFormulasOrHandles) {
				print(object, indent + 1);
			}
		}
		else if (child instanceof CTTextbox) {
			CTTextbox element = (CTTextbox) child;
			CTTxbxContent txbxContent = element.getTxbxContent();
			List<Object> egBlockLevelElts = txbxContent.getEGBlockLevelElts();
			for (Object object : egBlockLevelElts) {
				print(object, indent + 1);
			}
		}
		else if (child instanceof CTTextPath) {
			indent++;
			for (int i = 0; i < indent; i++) {
				System.out.print("    ");
			}
			System.out.println("\"" + ((CTTextPath) child).getString() + "\"");
		}
		else if (child instanceof CTFill) {
			indent++;
			for (int i = 0; i < indent; i++) {
				System.out.print("    ");
			}
			System.out.println("\"" + ((CTFill) child).getTitle() + "\"");
		}
		else if (child instanceof CTShape) {
			((CTShape) child).getTitle();
		}
	}

	@Override
	protected Map getTableValues(Object aDoc, Stack someRefs) throws Exception {
		return null;
	}

	@Override
	protected Map getTables(Object aDoc, Stack someRefs) {
		return null;
	}

	@Override
	protected Object createApplication(Stack someRefs) throws Exception {
		return null;
	}

	@Override
	protected void releaseStack(Stack someRefs) {
		// not needed with DOCX4J
	}

	@Override
	protected Object getDocument(BinaryData aName, Object anApplication, Stack someRefs) throws Exception {
		try (InputStream in = aName.getStream()) {
			WordprocessingMLPackage document = WordprocessingMLPackage.load(in);
			return document;
		}
	}

	@Override
	protected Map getValuesFromDoc(Object aDoc, Mapping aMapping, Stack someRefs) throws Exception {
		return null;
	}

	@Override
	protected boolean setResult(Object anAppl, Object aDoc, Object anObject, String aKey, Object aValue, Stack someRefs)
			throws Exception {
		return false;
	}

	@Override
	protected Map getFields(Object aDoc, Stack someRefs) throws Exception {
		return Collections.EMPTY_MAP;
	}

	@Override
	protected void save(Object anAppl, Object aDoc, String aName, Stack someRefs) throws Exception {
		WordprocessingMLPackage document = (WordprocessingMLPackage) aDoc;
		provideWordOutput(document, aName);
	}

	private void provideWordOutput(WordprocessingMLPackage document, String docName) throws Exception {
		SaveToZipFile saver = new SaveToZipFile(document);

		FileOutputStream out = new FileOutputStream(docName);
		try {
			saver.save(out);
		} finally {
			out.close();
		}
	}

	@Override
	protected void closeDocument(Object aDoc, Stack someRefs) throws Exception {
	}

	@Override
	protected String getVersion(Stack someRefs) throws Exception {
		return null;
	}

	@Override
	protected StringLengthComparator getTokenKeyComparator() {
		return new DOCXWordAccessTokenkeyComparator(CollectionUtil.toList(tokenReplacers.keySet()));
	}

	private class DOCXWordAccessTokenkeyComparator extends StringLengthComparator {

		private final List<String> replacerKeys;

		/**
		 * Creates a new instance of this class.
		 */
		public DOCXWordAccessTokenkeyComparator(List<String> replacerKeys) {
			this.replacerKeys = replacerKeys;
		}

		@Override
		public int compare(Object o1, Object o2) {
			if (o1 == null && o2 == null)
				return 0;
			if (o1 == null)
				return -1;
			if (o2 == null)
				return 1;
			boolean c1 = ((String) o1).startsWith("COPY_");
			boolean c2 = ((String) o2).startsWith("COPY_");
			if (c1 && !c2)
				return -1;
			if (!c1 && c2)
				return 1;
			int result = priorityValue(o1) - priorityValue(o2);
			return result == 0 ? super.compare(o1, o2) : result;
		}

		private int priorityValue(Object o) {
			String s = (String) o;
			int length = replacerKeys.size();
			for (int i = 0; i < length; i++) {
				if (s.startsWith(replacerKeys.get(i)))
					return i;
			}
			return length;
		}

	}

	private static class StringMapping implements Mapping<String, String> {

		public static final Mapping<String, String> INSTANCE = new StringMapping();

		@Override
		public String map(String input) {
			return input;
		}
	}
}
