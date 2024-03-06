/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

import static com.top_logic.basic.shared.string.StringServicesShared.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.net.URLDecoder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.internet.ContentDisposition;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PropertyDescriptor;
import com.top_logic.basic.config.PropertyInitializer;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.Key;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.TagName;
import com.top_logic.basic.config.annotation.ValueInitializer;
import com.top_logic.basic.config.json.JsonUtilities;
import com.top_logic.basic.config.order.DisplayInherited;
import com.top_logic.basic.config.order.DisplayInherited.DisplayStrategy;
import com.top_logic.basic.config.order.DisplayOrder;
import com.top_logic.basic.func.misc.AlwaysFalse;
import com.top_logic.basic.io.binary.BinaryData;
import com.top_logic.basic.io.binary.ByteArrayStream;
import com.top_logic.basic.io.binary.InMemoryBinaryData;
import com.top_logic.basic.json.JSON;
import com.top_logic.service.openapi.common.conf.HttpMethod;
import com.top_logic.service.openapi.common.document.ParameterLocation;
import com.top_logic.service.openapi.common.layout.WithMultiPartBodyTransferType;

/**
 * Interprets the request multipart body as parameter.
 * 
 * <p>
 * This allows to read the resource contents e.g. in a {@link HttpMethod#PUT} request from the
 * method body.
 * </p>
 * 
 * @see RequestBodyParameter
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
@ParameterUsedIn(ParameterLocation.QUERY)
public class MultiPartBodyParameter extends ConcreteRequestParameter<MultiPartBodyParameter.Config> {

	private static final String CONTENT_DISPOSITION_NAME_PARAM = "name";

	private static final String CONTENT_DISPOSITION_FILENAME_PARAM = "filename";

	private static final MimeType MIME_TYPE_ANY_TEXT;

	private static final MimeType MIME_TYPE_JSON;
	static {
		try {
			MIME_TYPE_ANY_TEXT = new MimeType("text/*");
			MIME_TYPE_JSON = new MimeType(JsonUtilities.JSON_CONTENT_TYPE);
		} catch (MimeTypeParseException ex) {
			throw new UnreachableAssertion(ex);
		}
	}

	private static final String CONTENT_TYPE_HEADER = "Content-Type";

	private static final String CONTENT_DISPOSITION_HEADER = "Content-Disposition";

	private static final String FORM_DATA_DISPOSITION = "form-data";

	/**
	 * Configuration options for {@link MultiPartBodyParameter}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	@DisplayOrder({
		Config.NAME_ATTRIBUTE,
		Config.DESCRIPTION,
		Config.PARTS,
		Config.TRANSFER_TYPE,
		Config.REQUIRED,
	})
	@DisplayInherited(DisplayStrategy.IGNORE)
	@TagName("multipart-request-body")
	public interface Config
			extends ConcreteRequestParameter.Config<MultiPartBodyParameter>, WithMultiPartBodyTransferType {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

		/**
		 * Configuration name of the value {@link #getParts()}.
		 */
		String PARTS = "parts";

		/**
		 * The name of the {@link MultiPartBodyParameter} can be used to access all parts of the
		 * body. It is possible to access both the declared parts and the undeclared parts.
		 * 
		 * <p>
		 * The value is a mapping from the field name in the body request to the value of the field.
		 * </p>
		 */
		@Override
		@ValueInitializer(BodyNameInitializer.class)
		String getName();

		/**
		 * There is only one body.
		 */
		@Override
		@Hidden
		@Derived(fun = AlwaysFalse.class, args = {})
		boolean isMultiple();

		@Override
		default boolean isBodyParameter() {
			return true;
		}

		/**
		 * The parts that are allowed for this {@link MultiPartBodyParameter}.
		 */
		@Name(PARTS)
		@Key(BodyPart.NAME_ATTRIBUTE)
		Map<String, BodyPart> getParts();

		/**
		 * Initializer for the name of a {@link MultiPartBodyParameter}.
		 * 
		 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
		 */
		public class BodyNameInitializer implements PropertyInitializer {

			@Override
			public Object getInitialValue(PropertyDescriptor property) {
				return "body";
			}

		}

	}

	/**
	 * Part configuration for {@link MultiPartBodyParameter}.
	 * 
	 * <p>
	 * A {@link BodyPart} defines the name of the field that can be contained in a
	 * multipart/form-data request, whether the field is mandatory, and the format which the content
	 * must have.
	 * </p>
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface BodyPart extends ParameterConfiguration {

		/**
		 * The name of the body parameter.
		 */
		@Override
		String getName();

	}

	/**
	 * Creates a {@link MultiPartBodyParameter} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public MultiPartBodyParameter(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public List<String> getScriptParameterNames() {
		Set<String> partNames = parts().keySet();
		if (partNames.isEmpty()) {
			return super.getScriptParameterNames();
		}
		List<String> result = new ArrayList<>(super.getScriptParameterNames());
		result.addAll(partNames);
		return result;
	}

	@Override
	public void parse(Map<String, Object> parameters, HttpServletRequest req, Map<String, String> parametersRaw)
			throws InvalidValueException {
		Map<?, ?> value = getValue(req, parametersRaw);
		if (value.isEmpty()) {
			checkNonMandatory(getConfig());
		}
		parameters.put(getName(), value);
		for (ParameterConfiguration part : parts().values()) {
			String partName = part.getName();
			Object partValue = value.get(partName);
			if (partValue == null) {
				checkNonMandatory(part);
			}
			if (part.isMultiple()) {
				if (partValue == null) {
					partValue = new MultipleParameterValues();
				}
				assert partValue instanceof MultipleParameterValues : "#getValue(...) ensures that the value is a MultipleParameterValues";
			} else {
				if (partValue instanceof MultipleParameterValues) {
					throw new InvalidValueException(
						"Received multiple values for single parameter '" + partName + "'.");
				}
			}
			parameters.put(partName, partValue);
		}
	}

	private Map<String, ? extends ParameterConfiguration> parts() {
		return getConfig().getParts();
	}

	@Override
	protected Map<?, ?> getValue(HttpServletRequest req, Map<String, String> parametersRaw)
			throws InvalidValueException {
		try {
			switch (getConfig().getTransferType()) {
				case URL_ENCODED: {
					return parseURLEncodedData(req);
				}
				case FORM_DATA: {
					return parseMultiPartFormData(req);
				}
			}
			throw new UnreachableAssertion("Uncovered case: " + getConfig().getTransferType());
		} catch (IOException | ServletException ex) {
			throw new InvalidValueException("Unable to read content from stream.", ex);
		}
		
	}

	private Map<?, ?> parseURLEncodedData(HttpServletRequest req) throws InvalidValueException, IOException {
		Charset characterEncoding = Charset.forName(req.getCharacterEncoding());
		Map<String, Object> values = new HashMap<>();
		StringBuilder name = new StringBuilder();
		StringBuilder value = new StringBuilder();
		boolean nameProcessing = true;
		try (BufferedReader reader = req.getReader()) {
			while (true) {
				int current = reader.read();
				if (current == -1) {
					break;
				}
				char c = (char) current;
				if (nameProcessing) {
					switch (c) {
						case '&': {
							throw new InvalidValueException(
								"Invalid URL encoded data: Unexpected character '&' when processing name '" + name
										+ "'.");
						}
						case '=': {
							nameProcessing = false;
							break;
						}
						default: {
							name.append(c);
							break;
						}
					}
				} else {
					switch (c) {
						case '&': {
							addEncodedValue(values, name.toString(), value.toString(), characterEncoding);
							name.setLength(0);
							value.setLength(0);
							nameProcessing = true;
							break;
						}
						case '=': {
							throw new InvalidValueException(
								"Invalid URL encoded data: Unexpected character '=' when processing value '" + value
										+ "' for name '" + name
										+ "'.");
						}
						default: {
							value.append(c);
							break;
						}
					}
				}
			}
			if (!nameProcessing) {
				addEncodedValue(values, name.toString(), value.toString(), characterEncoding);
			} else if (name.length() > 0) {
				throw new InvalidValueException("Missing value for name: " + name);
			}
		}
		return values;
	}

	private void addEncodedValue(Map<String, Object> values, String name, String value, Charset encoding)
			throws InvalidValueException {
		String decodedValue;
		try {
			decodedValue = URLDecoder.decode(value, encoding);
		} catch (IllegalArgumentException ex) {
			// Illegal strings are encountered.
			throw new InvalidValueException("Illegal URL encoded value: " + value, ex);
		}
		BodyPart bodyPart = getConfig().getParts().get(name);
		if (bodyPart == null) {
			addValue(values, name, decodedValue, false);
		} else {
			ParameterFormat format = bodyPart.getFormat();
			addValue(values, name, parse(decodedValue, format, name), bodyPart.isMultiple());
		}
	}

	private Map<?, ?> parseMultiPartFormData(HttpServletRequest req)
			throws IOException, InvalidValueException, ServletException {
		String characterEncoding = req.getCharacterEncoding();
		Map<String, Object> values = new HashMap<>();
		for (Part part : req.getParts()) {
			addValue(values, part, characterEncoding);
		}
		return values;
	}

	private void addValue(Map<String, Object> values, Part part, String defaultEncoding)
			throws IOException, InvalidValueException {
		String sContentDisposition = part.getHeader(CONTENT_DISPOSITION_HEADER);
		if (isEmpty(sContentDisposition)) {
			throw new InvalidValueException(
				"Missing header '" + CONTENT_DISPOSITION_HEADER + "' for multi-part request.");
		}
		ContentDisposition contentDisposition;
		try {
			contentDisposition = new ContentDisposition(sContentDisposition);
		} catch (javax.mail.internet.ParseException ex) {
			throw new InvalidValueException("Invalid content disposition: " + sContentDisposition, ex);
		}

		if (!FORM_DATA_DISPOSITION.equals(contentDisposition.getDisposition())) {
			throw new InvalidValueException("Expected content disposition '" + FORM_DATA_DISPOSITION + "' but got "
					+ contentDisposition.getDisposition());
		}
		String fieldName = contentDisposition.getParameter(CONTENT_DISPOSITION_NAME_PARAM);
		if (fieldName == null) {
			throw new InvalidValueException("Missing parameter '" + CONTENT_DISPOSITION_NAME_PARAM
					+ "' in content disposition header:  " + sContentDisposition);
		}
		String contentType = part.getHeader(CONTENT_TYPE_HEADER);
		ParameterConfiguration bodyPartDefinition = parts().get(fieldName);
		if (bodyPartDefinition == null) {
			// Unspecified content.
			Object value;
			if (isEmpty(contentType)) {
				value = readBinary(part.getInputStream(), fileName(contentDisposition), BinaryData.CONTENT_TYPE_OCTET_STREAM);
			} else {
				MimeType mimeType = parseMimeType(contentType);
				String charSet = mimeType.getParameter("charset");
				if (charSet == null) {
					charSet = defaultEncoding;
				}
				if (MIME_TYPE_ANY_TEXT.match(mimeType)) {
					value = readString(part.getInputStream(), charSet);
				} else if (MIME_TYPE_JSON.match(mimeType)) {
					String stringValue = readString(part.getInputStream(), charSet);
					try {
						value = JSON.fromString(stringValue);
					} catch (com.top_logic.basic.json.JSON.ParseException ex) {
						throw new InvalidValueException("Invalid JSON value: " + stringValue, ex);
					}
				} else {
					value = readBinary(part.getInputStream(), fileName(contentDisposition), mimeType.getBaseType());
				}
			}
			addValue(values, fieldName, value, false);
		} else {
			Object value;
			if (bodyPartDefinition.getFormat() == ParameterFormat.BINARY) {
				if (isEmpty(contentType)) {
					value = readBinary(part.getInputStream(), fileName(contentDisposition), BinaryData.CONTENT_TYPE_OCTET_STREAM);
				} else {
					MimeType mimeType = parseMimeType(contentType);
					String charSet = mimeType.getParameter("charset");
					if (charSet == null) {
						charSet = defaultEncoding;
					}
					value = readBinary(part.getInputStream(), fileName(contentDisposition), mimeType.getBaseType());
				}
			} else {
				String charSet;
				if (isEmpty(contentType)) {
					charSet = defaultEncoding;
				} else {
					MimeType mimeType = parseMimeType(contentType);
					charSet = mimeType.getParameter("charset");
					if (charSet == null) {
						charSet = defaultEncoding;
					}
				}
				/* ParameterFormat#BINARY is handled above. */
				ParameterFormat format = bodyPartDefinition.getFormat();
				value = parse(readString(part.getInputStream(), charSet), format, bodyPartDefinition.getName());
			}
			addValue(values, fieldName, value, bodyPartDefinition.isMultiple());
		}

	}

	private void addValue(Map<String, Object> values, String fieldName, Object value, boolean forceListValue) {
		Object knownFieldValue = values.get(fieldName);
		if (knownFieldValue != null || values.containsKey(fieldName)) {
			if (knownFieldValue instanceof MultipleParameterValues) {
				((MultipleParameterValues) knownFieldValue).add(value);
			} else {
				values.put(fieldName, MultipleParameterValues.create(knownFieldValue, value));
			}
		} else if (forceListValue) {
			values.put(fieldName, MultipleParameterValues.create(value));
		} else {
			values.put(fieldName, value);
		}
	}

	private String fileName(ContentDisposition contentDisposition) {
		return contentDisposition.getParameter(CONTENT_DISPOSITION_FILENAME_PARAM);
	}

	private String readString(InputStream inputStream, String charSet) throws IOException, UnsupportedEncodingException {
		ByteArrayStream out = new ByteArrayStream();
		inputStream.transferTo(out);
		String value;
		if (charSet == null) {
			value = new String(out.getOrginalByteBuffer(), 0, out.size());
		} else {
			value = new String(out.getOrginalByteBuffer(), 0, out.size(), charSet);
		}
		return value;
	}

	private ByteArrayStream readBinary(InputStream inputStream, String name, String contentType)
			throws IOException {
		ByteArrayStream content;
		if (name == null) {
			content = new ByteArrayStream();
		} else {
			content = new InMemoryBinaryData(contentType, name);
		}
		inputStream.transferTo(content);
		return content;
	}

	private MimeType parseMimeType(String contentType) throws InvalidValueException {
		MimeType mimeType;
		try {
			mimeType = new MimeType(contentType);
		} catch (MimeTypeParseException ex) {
			throw new InvalidValueException("Invalid mime type: " + contentType, ex);
		}
		return mimeType;
	}

	/**
	 * Special {@link ArrayList} extension to determine multiple values for a field name.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	private static class MultipleParameterValues extends ArrayList<Object> {

		public static MultipleParameterValues create(Object o1) {
			MultipleParameterValues multipleParameterValues = new MultipleParameterValues();
			multipleParameterValues.add(o1);
			return multipleParameterValues;
		}

		public static MultipleParameterValues create(Object o1, Object o2) {
			MultipleParameterValues multipleParameterValues = new MultipleParameterValues();
			multipleParameterValues.add(o1);
			multipleParameterValues.add(o2);
			return multipleParameterValues;
		}
	}
}
