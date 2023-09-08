/*
 * SPDX-FileCopyrightText: 2016 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.db2.migration.formats;

import java.text.ParseException;
import java.util.Collections;
import java.util.Map;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.commons.codec.binary.Base64;

import com.top_logic.basic.ExtID;
import com.top_logic.basic.ExtIDFormat;
import com.top_logic.basic.IdentifierUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.LongID;
import com.top_logic.basic.StringID;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.TLID;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.ConfigUtil;
import com.top_logic.basic.config.ConfigurationDescriptor;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.ConfigurationItem;
import com.top_logic.basic.config.ConfigurationReader;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.SimpleInstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.XmlDateTimeFormat;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.BinaryDataFactory;
import com.top_logic.basic.io.character.CharacterContents;
import com.top_logic.dob.MetaObject;
import com.top_logic.dob.identifier.DefaultObjectKey;
import com.top_logic.knowledge.objects.identifier.ExtReference;
import com.top_logic.knowledge.objects.identifier.ExtReferenceFormat;
import com.top_logic.knowledge.service.Revision;

/**
 * Parser for attribute serialized attribute value in a dump file.
 * 
 * @see ValueDumper
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public abstract class ValueParser<E extends Throwable> {

	static final String BINARY_DATA_FILE_NAME_PARAMETER = "file-name";

	/**
	 * Separator between the content type of the {@link BinaryData} and the actual {@link Base64}
	 * encoded content.
	 */
	static char BINARY_DATA_TYPE_SEPARATOR = ':';

	private static final Map<String, ConfigurationDescriptor> DESCRIPTORS = Collections.singletonMap("config",
		TypedConfiguration.getConfigurationDescriptor(ConfigurationItem.class));

	/**
	 * Parses the serialized form.
	 * 
	 * @param type
	 *        The expected {@link ValueType} of the serialized from.
	 * @param value
	 *        The serialized form of the value.
	 * @return The concrete value.
	 * @throws E
	 *         If parsing fails.
	 */
	public Object parseValue(ValueType type, String value) throws E {
		try {
			return tryParseValue(type, value);
		} catch (RuntimeException ex) {
			throw parseError("Parsing of " + type + " value '" + value + "' failed.", ex);
		}
	}

	private Object tryParseValue(ValueType type, String value) throws E {
		switch (type) {
			case BOOLEAN:
				return Boolean.valueOf(value);
			case DATE:
				return parseDate(value);
			case DOUBLE:
				return Double.parseDouble(value);
			case FLOAT:
				return Float.parseFloat(value);
			case BYTE:
				return Byte.parseByte(value);
			case SHORT:
				return Short.parseShort(value);
			case CHAR:
				return value.charAt(0);
			case INT:
				return Integer.parseInt(value);
			case LONG:
				return Long.parseLong(value);
			case STRING:
				return value;
			case NULL:
				return parseNull(value);
			case REF:
				return parseId(value);
			case EXT_REF:
				return parseExtRef(value);
			case EXT_ID:
				return parseExtID(value);
			case ENUM:
				return parseEnum(value);
			case TLID:
				return fuzzyFromExternalForm(value);
			case CONFIG:
				return parseConfig(value);
			case INSTANCE:
				return parseInstance(value);
			case SINGLETON:
				return parseSingleton(value);
			case BINARY:
				return Base64.decodeBase64(value);
			case BINARY_DATA:
				return parseBinaryData(value);
		}
		throw new UnreachableAssertion("No such value type: " + type);
	}

	private Object parseBinaryData(String value) throws E {
		int length = value.length();

		StringBuilder contentTypeBuffer = new StringBuilder(length);
		int index = 0;
		while (true) {
			if (index == length) {
				throw parseError("Invalid binary data format: No content type found in '" + value + "'.");
			}
			char ch = value.charAt(index++);
			if (ch == BINARY_DATA_TYPE_SEPARATOR) {
				// Either end of content type or encoded separator
				if (index == length) {
					// End of value, no quoting char.
					break;
				}
				char lookahead = value.charAt(index);
				if (lookahead == BINARY_DATA_TYPE_SEPARATOR) {
					// Quoted separator char.
					index += 1;
					contentTypeBuffer.append(lookahead);
				} else {
					// Separator found, end of contentType.
					break;
				}
			} else {
				contentTypeBuffer.append(ch);
			}
		}

		byte[] decodedInputStream = Base64.decodeBase64(value.substring(index));

		String contentType = contentTypeBuffer.toString();
		try {
			MimeType mimeType = new MimeType(contentType);
			String fileName = mimeType.getParameter(BINARY_DATA_FILE_NAME_PARAMETER);
			if (fileName != null) {
				mimeType.removeParameter(BINARY_DATA_FILE_NAME_PARAMETER);
				contentType = mimeType.toString();

				return BinaryDataFactory.createBinaryData(decodedInputStream, contentType, fileName);
			}
		} catch (MimeTypeParseException ex) {
			Logger.error("Invalid content type in binary data: " + contentType, ex);
		}

		return BinaryDataFactory.createBinaryData(decodedInputStream, contentType);
	}

	private Object parseDate(String value) throws E {
		try {
			return XmlDateTimeFormat.INSTANCE.parseObject(value);
		} catch (ParseException ex) {
			throw parseError("Invalid date format: " + value, ex);
		}
	}

	private Object parseNull(String value) throws E {
		if (!StringServices.isEmpty(value)) {
			parseError("Null value must be empty, found: " + value);
		}
		return null;
	}

	private Object parseSingleton(String value) throws E {
		try {
			return ConfigUtil.getInstanceMandatory(Object.class, "", value);
		} catch (ConfigurationException ex) {
			throw parseError("Cannot resolve singleton: " + value, ex);
		}
	}

	private Object parseInstance(String value) throws E {
		PolymorphicConfiguration<?> config = (PolymorphicConfiguration<?>) parseConfig(value);
		return SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY.getInstance(config);
	}

	private ConfigurationItem parseConfig(String value) throws E {
		try {
			return new ConfigurationReader(SimpleInstantiationContext.CREATE_ALWAYS_FAIL_IMMEDIATELY,
				DESCRIPTORS).setSource(CharacterContents.newContent(value, "<unknown>")).read();
		} catch (ConfigurationException ex) {
			throw parseError("Invalid configuration value: " + value, ex);
		}
	}

	private Object parseEnum(String value) throws E {
		int typeSep = value.indexOf('#');
		if (typeSep < 0) {
			throw parseError("Invalid enum format: " + value);
		}
		String className = value.substring(0, typeSep);
		try {
			@SuppressWarnings("rawtypes")
			Class enumType = Class.forName(className);

			String name = value.substring(typeSep + 1);

			@SuppressWarnings("unchecked")
			Enum<?> result = Enum.valueOf(enumType, name);

			return result;
		} catch (ClassNotFoundException ex) {
			throw parseError("Invalid enum class: " + className, ex);
		}
	}

	private Object parseId(String value) throws E {
		int typeSep = value.indexOf(':');
		if (typeSep < 0) {
			throw parseError("Invalid identifier format, missing type separator: " + value);
		}
		int branchSep = value.indexOf('/', typeSep + 1);
		if (branchSep < 0) {
			throw parseError("Invalid identifier format, missing branch separator: " + value);
		}
		int revSep = value.indexOf('#', branchSep + 1);

		MetaObject objectType = resolve(value.substring(0, typeSep));
		if (objectType == null) {
			return null;
		}
		long branch = Long.parseLong(value.substring(typeSep + 1, branchSep));
		long revision;
		TLID id;
		if (revSep < 0) {
			String idString = value.substring(branchSep + 1);
			id = fuzzyFromExternalForm(idString);
			revision = Revision.CURRENT_REV;
		} else {
			id = fuzzyFromExternalForm(value.substring(branchSep + 1, revSep));
			revision = Long.parseLong(value.substring(revSep + 1));
		}
		return new DefaultObjectKey(branch, revision, objectType, id);
	}

	private ExtID parseExtID(String idString) throws E {
		try {
			return (ExtID) ExtIDFormat.INSTANCE.parseObject(idString);
		} catch (ParseException ex) {
			throw parseError("invalid external id", ex);
		}
	}

	private ExtReference parseExtRef(String refString) throws E {
		try {
			return (ExtReference) ExtReferenceFormat.INSTANCE.parseObject(refString);
		} catch (ParseException ex) {
			throw parseError("invalid external id", ex);
		}
	}

	/**
	 * Creates a {@link TLID} from the external form.
	 * 
	 * <p>
	 * Tries to parse the form as {@link LongID}. If not possible, a {@link StringID} is created.
	 * </p>
	 * 
	 * @see IdentifierUtil#fromExternalForm(String)
	 */
	public TLID fuzzyFromExternalForm(String externalId) {
		try {
			return LongID.fromExternalForm(externalId);
		} catch (NumberFormatException ex) {
			// not a valid long id.
			return StringID.fromExternalForm(externalId);
		}
	}

	/**
	 * Resolves a type name in the current context.
	 * 
	 * @param typeName
	 *        The type name to resolve.
	 * @return The concrete type.
	 * @throws E
	 *         If the type is not found.
	 */
	protected abstract MetaObject resolve(String typeName) throws E;

	private E parseError(String message) throws E {
		throw parseError(message, null);
	}

	/**
	 * Throws the concrete exception type used in the parsing context.
	 * 
	 * @param message
	 *        The exception message.
	 * @param cause
	 *        An optional exception cause.
	 * @return Instead of returning the created exception, it should be thrown.
	 * @throws E
	 *         Always.
	 */
	protected abstract E parseError(String message, Throwable cause) throws E;

}
