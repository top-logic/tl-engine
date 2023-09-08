/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.message;

import java.io.IOException;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParseException;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.message.Message;
import com.top_logic.basic.message.Template;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.ex.UnknownTypeException;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.knowledge.objects.KnowledgeItem;
import com.top_logic.knowledge.objects.KnowledgeObject;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Revision;
import com.top_logic.knowledge.wrap.Wrapper;
import com.top_logic.knowledge.wrap.WrapperFactory;
import com.top_logic.layout.form.model.DescriptiveParsePosition;

/**
 * Serializer for persistent storage of internationalizable {@link Message}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class MessageStoreFormat extends Format implements MessageConstants {

//	private static final String COMMON_PREFIX = "com.top_logic.";

	/**
	 * Converts the given message to a serialized string for persistent storage.
	 * 
	 * @param message
	 *        The message to serialize.
	 */
	public static String toString(Message message) {
		StringBuilder buffer = new StringBuilder();
		appendBuffer(buffer, message);
		return buffer.toString();
	}

	/**
	 * Appends the given message to the given buffer as serialized string for
	 * persistent storage.
	 * 
	 * @param buffer
	 *        The buffer to append to.
	 * @param message
	 *        The message to serialize.
	 * 
	 * @see #appendAppendable(Appendable, Message) for appending to a
	 *      general {@link Appendable}.
	 */
	public static void appendBuffer(StringBuilder buffer, Message message) {
		try {
			appendAppendable(buffer, message);
		} catch (IOException ex) {
			throw new UnreachableAssertion("Writing to a StringBuilder must not fail.", ex);
		}
	}
	
	/**
	 * Appends the given message to the given buffer as serialized string for
	 * persistent storage.
	 * 
	 * @param buffer
	 *        The buffer to append to.
	 * @param message
	 *        The message to serialize.
	 * 
	 * @see #appendAppendable(Appendable, Message) for appending to a
	 *      general {@link Appendable}.
	 */
	public static void appendBuffer(StringBuffer buffer, Message message) {
		try {
			appendAppendable(buffer, message);
		} catch (IOException ex) {
			throw new UnreachableAssertion("Writing to a StringBuilder must not fail.", ex);
		}
	}

	/**
	 * Appends the given message to the given {@link Appendable} as serialized
	 * string for persistent storage.
	 * 
	 * @param buffer
	 *        The buffer to append to.
	 * @param message
	 *        The message to serialize.
	 * 
	 * @see #appendBuffer(StringBuilder, Message) for appending to a
	 *      {@link StringBuilder}.
	 */
	public static void appendAppendable(Appendable buffer, Message message) throws IOException {
		appendMessage(buffer, false, message);
	}
	
	private static void appendMessage(Appendable buffer, boolean inner, Message message) throws IOException {
		if (message == null) {
			// Safety, if null messages are serialized.
			buffer.append(TYPE_NULL);
			return;
		}
		
		appendTemplateKey(buffer, message.getTemplate());
		Object[] args = message.getArguments();
		if (args != null) {
			for (Object arg : args) {
				buffer.append(MSG_ARG_SEPARATOR);
				appendObj(buffer, arg);
			}
		}
	}

	private static void appendTemplateKey(Appendable buffer, Template template) throws IOException {
		String nameSpace = template.getNameSpace();
		String localName = template.getLocalName();
		
		if (nameSpace != null) {
			// Note: Dropping the common prefix makes it impossible to find the
			// defining class for a key without try and error, because there are
			// classes that have not the common prefix (test, applications from
			// partners).
			//
//			if (contextPackage.startsWith(COMMON_PREFIX)) {
//				buffer.append(contextPackage, COMMON_PREFIX.length(), contextPackage.length());
//			} else {
				buffer.append(nameSpace);
//			}
			buffer.append(MSG_NAME_SEPARATOR);
		}
		buffer.append(localName);
	}

	private static void appendObj(Appendable buffer, Object obj) throws IOException {
		if (obj == null) {
			buffer.append(TYPE_NULL);
		}
		else if (obj instanceof CharSequence) {
			CharSequence s = ((CharSequence) obj);
			appendString(buffer, s);
		}
		else if (obj instanceof Number) {
			if (obj instanceof Integer) {
				buffer.append(TYPE_INT);
				buffer.append(String.valueOf(obj));
			}
			else if (obj instanceof Long) {
				buffer.append(TYPE_LONG);
				buffer.append(String.valueOf(obj));
			}
			else if (obj instanceof Float) {
				buffer.append(TYPE_FLOAT);
				buffer.append(String.valueOf(obj));
			}
			else if (obj instanceof Double) {
				buffer.append(TYPE_DOUBLE);
				buffer.append(String.valueOf(obj));
			}
		}
		else if (obj instanceof Date) {
			buffer.append(XmlDateTimeFormat.formatTimeStamp(((Date) obj).getTime()));
		}
		else if (obj instanceof Boolean) {
			buffer.append(((Boolean) obj).booleanValue() ? TRUE_VALUE : FALSE_VALUE);
		}
		else if (obj instanceof List<?>) {
			List<?> l = ((List<?>) obj);
			buffer.append(TYPE_LIST);
			for (int n = 0, cnt = l.size(); n < cnt; n++) {
				if (n > 0) {
					buffer.append(LIST_NEXT);
				}
				appendObj(buffer, l.get(n));
			}
			buffer.append(LIST_END);
		}
		else if (obj instanceof Map<?, ?>) {
			Map<?, ?> m = ((Map<?, ?>) obj);
			buffer.append(TYPE_MAP);
			boolean first = true;
			for (Entry<?, ?> entry : m.entrySet()) {
				if (first) {
					first = false;
				} else {
					buffer.append(MAP_NEXT);
				}
				appendObj(buffer, entry.getKey());
				buffer.append(MAP_VALUE);
				appendObj(buffer, entry.getValue());
			}
			buffer.append(MAP_END);
		}
		else if (obj instanceof Class<?>) {
			buffer.append(TYPE_CLASS);
			buffer.append(((Class<?>) obj).getName());
		}
		else if (obj instanceof Wrapper) {
			buffer.append(TYPE_WRAPPER);
			KnowledgeObject ko = ((Wrapper) obj).tHandle();
			appendKO(buffer, ko);
		}
		else if (obj instanceof KnowledgeItem) {
			buffer.append(TYPE_KO);
			KnowledgeItem ko = (KnowledgeItem) obj;
			appendKO(buffer, ko);
		}
		else if (obj instanceof Message) {
			Message m = (Message) obj;
			buffer.append(TYPE_MSG);
			appendMessage(buffer, true, m);
			buffer.append(MSG_END);
		}
		else {
			throw new IllegalArgumentException("Unsupported message type argument '" + obj.getClass() + "'.");
		}
	}

	private static void appendString(Appendable buffer, CharSequence s) throws IOException {
		buffer.append(TYPE_STRING);
		// Quote specials in first character and argument separator in all characters.
		int length = s.length();
		for (int n = 0; n < length; n++) {
			char ch = s.charAt(n);
			if (ch == QUOTE_SYMBOL || ch == STRING_END) {
				buffer.append(QUOTE_SYMBOL);
			}
			buffer.append(ch);
		}
		buffer.append(STRING_END);
	}

	private static void appendKO(Appendable buffer, KnowledgeItem ko) throws IOException {
		String moName = ko.tTable().getName();
		TLID objectName = ko.getObjectName();
		long branchId = ko.getBranchContext();
		long revId = ko.getHistoryContext();
		
		buffer.append(moName);
		buffer.append(KO_NAME_SEPARATOR);
		buffer.append(IdentifierUtil.toExternalForm(objectName));
		buffer.append(KO_BRANCH_SEPARATOR);
		buffer.append(String.valueOf(branchId));
		buffer.append(KO_REVISION_SEPARATOR);
		if (revId != Revision.CURRENT_REV) {
			buffer.append(String.valueOf(revId));
		} else {
			buffer.append(KO_REVISION_CURRENT);
		}
	}

	@Override
	public StringBuffer format(Object obj, StringBuffer buffer, FieldPosition pos) {
		appendBuffer(buffer, (Message) obj);
		return buffer;
	}

	@Override
	public Object parseObject(String source, ParsePosition pos) {
		return parseMessage(source, pos);
	}

	/**
	 * Parses the given storage representation of a {@link Message} that was
	 * produced by this {@link MessageStoreFormat}.
	 * 
	 * @param source
	 *        The serialized message.
	 * @return The parsed message.
	 * @throws ParseException
	 *         If the given string could not be parsed into a message.
	 */
	public static Message parseMessage(String source) throws ParseException {
		DescriptiveParsePosition pos = new DescriptiveParsePosition(0);
		Message result = parseMessage(source, pos);
		if (pos.getErrorIndex() >= 0) {
			throw new ParseException("Invalid message format: " + pos.getErrorMessage(), pos.getErrorIndex());
		}
		return result;
	}
	
	private static Message parseMessage(String source, ParsePosition pos) {
		int index = pos.getIndex();
		int length = source.length();
		if (index >= length) {
			return null;
		}
		
		String key = getPart(source, pos);
		
		String namespace;
		String localname;
		int localNameSepIndex = key.lastIndexOf(MSG_NAME_SEPARATOR);
		if (localNameSepIndex < 0) {
			namespace = null;
			localname = key;
		} else {
			namespace = key.substring(0, localNameSepIndex);
			localname = key.substring(localNameSepIndex + 1);
		}
		
		
		int firstArgSepIndex = index + key.length();
		if (firstArgSepIndex >= length || source.charAt(firstArgSepIndex) != MSG_ARG_SEPARATOR) {
			return new CustomMessage(namespace, localname);
		}
		
		pos.setIndex(firstArgSepIndex);
		
		List<Object> args = new ArrayList<>();
		while (pos.getIndex() < length) {
			int argSepIndex = pos.getIndex();
			char argSep = source.charAt(argSepIndex);
			if (argSep == MSG_END) {
				break;
			}
			if (argSep != MSG_ARG_SEPARATOR) {
				error(pos, argSepIndex, "Expected argument separator.");
				return null;
			}
			
			pos.setIndex(argSepIndex + 1);
			
			Object arg = parseArg(source, pos);
			if (pos.getErrorIndex() >= 0) {
				return null;
			}
			
			args.add(arg);
		}
		
		return new CustomMessage(namespace, localname, args.toArray());
	}

	private static void error(ParsePosition pos, int errorIndex, String errorMessage) {
		pos.setErrorIndex(errorIndex);
		
		if (pos instanceof DescriptiveParsePosition) {
			((DescriptiveParsePosition) pos).setErrorDescription(errorMessage);
		}
	}

	private static Object parseArg(String source, ParsePosition pos) {
		int index = pos.getIndex();
		char typeCode = source.charAt(index++);
		pos.setIndex(index);
		
		switch (typeCode) {
		case TYPE_NULL:
			return null;
		case TYPE_CLASS: {
			String className = getPart(source, pos);
			try {
				return Class.forName(className);
			} catch (ClassNotFoundException ex) {
				error(pos, index, "Argument class not found.");
				return null;
			}
		}
		case TYPE_DOUBLE: {
			String value = getPart(source, pos);
			try {
				return Double.parseDouble(value);
			} catch (NumberFormatException ex) {
				error(pos, index, "Not a double value.");
				return null;
			}
		}
		case TYPE_FLOAT: {
			String value = getPart(source, pos);
			try {
				return Float.parseFloat(value);
			} catch (NumberFormatException ex) {
				error(pos, index, "Not a float value.");
				return null;
			}
		}
		case TYPE_INT: {
			String value = getPart(source, pos);
			try {
				return Integer.parseInt(value);
			} catch (NumberFormatException ex) {
				error(pos, index, "Not an interger value.");
				return null;
			}
		}
		case TYPE_LONG: {
			String value = getPart(source, pos);
			try {
				return Long.parseLong(value);
			} catch (NumberFormatException ex) {
				error(pos, index, "Not a long value.");
				return null;
			}
		}
		case TYPE_STRING: {
			return parseString(source, pos);
		}
		case TYPE_WRAPPER:
			KnowledgeItem ko = parseRef(source, pos);
			if (ko != null) {
				// IGNORE FindBugs(RV_RETURN_VALUE_IGNORED_NO_SIDE_EFFECT): For better portability.
				return WrapperFactory.getWrapper((KnowledgeObject) ko);
			} else {
				return null;
			}
		case TYPE_KO: {
			return parseRef(source, pos);
		}
			
		case TYPE_LIST:
			return parseList(source, pos);

		case TYPE_MAP:
			return parseMap(source, pos);
			
		case TYPE_MSG: {
			Message result = parseMessage(source, pos);
			
			int afterMessageIndex = pos.getIndex();
			if (afterMessageIndex >= source.length()) {
				error(pos, afterMessageIndex, "Missing end of inner message.");
				return null;
			}

			char endMarker = source.charAt(afterMessageIndex);
			if (endMarker != MSG_END) {
				error(pos, afterMessageIndex, "Expected end of inner message.");
				return null;
			}
			
			return result;
		}

		case TYPE_TRUE: {
			if (! source.startsWith(TRUE_VALUE, index - 1)) {
				error(pos, index - 1, "Expected true value.");
				return null;
			}
			pos.setIndex(index - 1 + TRUE_VALUE.length());
			return true;
		}

		case TYPE_FALSE:
			if (! source.startsWith(FALSE_VALUE, index - 1)) {
				error(pos, index - 1, "Expected false value.");
				return null;
			}
			pos.setIndex(index - 1 + FALSE_VALUE.length());
			return false;
			
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9': {
			// Re-read type code.
			pos.setIndex(index - 1);
			return XmlDateTimeFormat.INSTANCE.parseObject(source, pos);
		}
			
		default: 
			// Note: index was already incremented.
			error(pos, index - 1, "Expected type code.");
			return null;
		}
	}

	private static Object parseMap(String source, ParsePosition pos) {
		int length = source.length();
		
		HashMap<Object, Object> result = new HashMap<>();
		
		{
			int index = pos.getIndex();
			if (index >= length) {
				error(pos, length, "Missing end of map.");
				return null;
			}
			
			char ch = source.charAt(index);
			if (ch == MAP_END) {
				pos.setIndex(index + 1);
				return result;
			}
		}
		
		while (true) {
			Object key = parseArg(source, pos);
			if (pos.getErrorIndex() >= 0) {
				return null;
			}

			int afterKeyIndex = pos.getIndex();
			if (afterKeyIndex >= length) {
				error(pos, length, "Missing map value.");
				return null;
			}
			
			char valueSeparator = source.charAt(afterKeyIndex);
			if (valueSeparator != MAP_VALUE) {
				error(pos, length, "Expected map value separator.");
				return null;
			}
			
			pos.setIndex(afterKeyIndex + 1);
			
			Object value = parseArg(source, pos);
			if (pos.getErrorIndex() >= 0) {
				return null;
			}
			
			result.put(key, value);
			
			int afterIndex = pos.getIndex();
			if (afterIndex >= length) {
				error(pos, length, "Missing end of map.");
				return null;
			}
			
			char separator = source.charAt(afterIndex);
			if (separator == MAP_END) {
				pos.setIndex(afterIndex + 1);
				return result;
			}
			
			if (separator != MAP_NEXT) {
				error(pos, length, "Expected map entry separator.");
				return null;
			}
			
			pos.setIndex(afterIndex + 1);
		}
	}
	
	private static Object parseList(String source, ParsePosition pos) {
		int length = source.length();
		
		ArrayList<Object> result = new ArrayList<>();
		
		{
			int index = pos.getIndex();
			if (index >= length) {
				error(pos, length, "Missing end of list.");
				return null;
			}
			
			char ch = source.charAt(index);
			if (ch == LIST_END) {
				pos.setIndex(index + 1);
				return result;
			}
		}

		while (true) {
			Object element = parseArg(source, pos);
			if (pos.getErrorIndex() >= 0) {
				return null;
			}

			result.add(element);
			
			int afterIndex = pos.getIndex();
			if (afterIndex >= length) {
				error(pos, length, "Missing end of list.");
				return null;
			}
			
			char separator = source.charAt(afterIndex);
			if (separator == LIST_END) {
				pos.setIndex(afterIndex + 1);
				return result;
			}
			
			if (separator != LIST_NEXT) {
				error(pos, length, "Expected list separator.");
				return null;
			}
			
			pos.setIndex(afterIndex + 1);
		}
	}

	private static KnowledgeItem parseRef(String source, ParsePosition pos) {
		final int startIndex = pos.getIndex();
		int nameSepIndex = source.indexOf(KO_NAME_SEPARATOR, startIndex);
		if (nameSepIndex < 0) {
			error(pos, startIndex, "Invalid reference, missing object name.");
			return null;
		}
		
		String typeName = source.substring(startIndex, nameSepIndex);
		
		int nameStart = nameSepIndex + 1;
		int branchSepIndex = source.indexOf(KO_BRANCH_SEPARATOR, nameStart);
		if (branchSepIndex < 0) {
			error(pos, startIndex, "Invalid reference, missing branch.");
			return null;
		}
		
		TLID objectName = IdentifierUtil.fromExternalForm(source.substring(nameStart, branchSepIndex));
		
		int branchStart = branchSepIndex + 1;
		int revSepIndex = source.indexOf(KO_REVISION_SEPARATOR, branchStart);
		if (revSepIndex < 0) {
			error(pos, startIndex, "Invalid reference, missing revision.");
			return null;
		}
		
		long branchId = Long.parseLong(source.substring(branchStart, revSepIndex));
		
		int revStart = revSepIndex + 1;
		pos.setIndex(revStart);
		String revName = getPart(source, pos);
		
		long rev;
		if (revName.equals(KO_REVISION_CURRENT)) {
			rev = Revision.FIRST_REV;
		} else {
			try {
				rev = Long.parseLong(revName);
			} catch (NumberFormatException ex) {
				error(pos, revStart, "Invalid reference, revision is not a number.");
				return null;
			}
		}
		
		KnowledgeBase kb = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
		MetaObject objectType;
		try {
			objectType = kb.getMORepository().getMetaObject(typeName);
		} catch (UnknownTypeException ex) {
			error(pos, startIndex, "Invalid reference, no such type.");
			return null;
		}
		return kb.resolveObjectKey(new DefaultObjectKey(branchId, rev, objectType, objectName));
	}

	private static Object parseString(String source, ParsePosition pos) {
		final int startIndex = pos.getIndex();
		final int length = source.length();
		
		int endIndex = startIndex;
		while (true) {
			if (endIndex >= length) {
				error(pos, endIndex, "Missing end of string.");
				return null;
			}
			
			char ch = source.charAt(endIndex);
			switch (ch) {
			case STRING_END: {
				pos.setIndex(endIndex + 1);
				return source.substring(startIndex, endIndex);
			}
			
			case QUOTE_SYMBOL: {
				StringBuilder buffer = new StringBuilder(source.subSequence(startIndex, endIndex));
				return parseStringBuffered(source, buffer, endIndex + 1, endIndex + 2, pos);
			}
			
			default: {
				endIndex++;
				break;
			}
			}
		}
	}

	private static Object parseStringBuffered(String source, StringBuilder buffer, int startIndex, int endIndex, ParsePosition pos) {
		final int length = source.length();
		
		while (true) {
			if (endIndex >= length) {
				error(pos, endIndex, "Invalid reference, missing end of string.");
				return null;
			}
			
			char ch = source.charAt(endIndex);
			switch (ch) {
			case STRING_END: {
				pos.setIndex(endIndex + 1);
				buffer.append(source.subSequence(startIndex, endIndex));
				return buffer.toString();
			}
			
			case QUOTE_SYMBOL: {
				buffer.append(source.subSequence(startIndex, endIndex));
				startIndex = endIndex + 1;
				endIndex = endIndex + 2;
				break;
			}
			
			default: {
				endIndex++;
				break;
			}
			}
		}
	}

	private static String getPart(String source, ParsePosition pos) {
		int index = pos.getIndex();
		
		int endIndex = index;
		int length = source.length();
		
		findEnd:
		while (endIndex < length) {
			char ch = source.charAt(endIndex);
			switch (ch) {
			case MSG_ARG_SEPARATOR:
			case MSG_END:
			case LIST_NEXT:
			case LIST_END:
			case MAP_VALUE:
			// case MAP_NEXT: (Same as LIST_END)
			case MAP_END:
				break findEnd;
			}
			
			endIndex++;
		}
		
		pos.setIndex(endIndex);
		return source.substring(index, endIndex);
	}


}
