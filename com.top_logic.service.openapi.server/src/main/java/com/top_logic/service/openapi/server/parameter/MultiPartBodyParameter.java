/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.service.openapi.server.parameter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.mail.internet.ContentDisposition;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.MultipartStream;
import org.apache.commons.fileupload.MultipartStream.MalformedStreamException;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.UnreachableAssertion;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Derived;
import com.top_logic.basic.config.annotation.Hidden;
import com.top_logic.basic.config.annotation.TagName;
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
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
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

	private static final Pattern HEADER = Pattern.compile("([^:]*):\\s*(.*)\\R?");

	/**
	 * Configuration options for {@link MultiPartBodyParameter}.
	 */
	@DisplayOrder({
		Config.NAME_ATTRIBUTE,
		Config.DESCRIPTION,
		Config.FORMAT,
		Config.REQUIRED,
		Config.SCHEMA,
		Config.EXAMPLE,
	})
	@DisplayInherited(DisplayStrategy.IGNORE)
	@TagName("multipart-request-body")
	public interface Config extends ConcreteRequestParameter.Config<MultiPartBodyParameter> {

		/** @see com.top_logic.basic.reflect.DefaultMethodInvoker */
		Lookup LOOKUP = MethodHandles.lookup();

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
	protected Object getValue(HttpServletRequest req, Map<String, String> parametersRaw) throws InvalidValueException {
		String characterEncoding = req.getCharacterEncoding();
		Map<String, Object> values = new HashMap<>();
		try {
			MultipartStream multipartStream = parseMultipartBody(req, characterEncoding);
			boolean nextPart = multipartStream.skipPreamble();
			while (nextPart) {
				addValue(values, multipartStream, characterEncoding);
				nextPart = multipartStream.readBoundary();
			}
		} catch (RuntimeException | InvalidValueException e) {
			throw e;
		} catch (IOException ex) {
			throw new InvalidValueException("Invalid multi part body.", ex);
		}
		return values;
	}

	private void addValue(Map<String, Object> values, MultipartStream stream, String defaultEncoding)
			throws IOException, InvalidValueException {
		String sContentDisposition = StringServices.EMPTY_STRING;
		String contentType = StringServices.EMPTY_STRING;
		String headers = stream.readHeaders();
		Matcher headerMatcher = HEADER.matcher(headers);
		while (headerMatcher.find()) {
			String headerName = headerMatcher.group(1);
			if (headerName.equalsIgnoreCase(CONTENT_DISPOSITION_HEADER)) {
				sContentDisposition = headerMatcher.group(2);
			} else if (headerName.equalsIgnoreCase(CONTENT_TYPE_HEADER)) {
				contentType = headerMatcher.group(2);
			}
		}
		if (sContentDisposition.isEmpty()) {
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
		String fileName = contentDisposition.getParameter(CONTENT_DISPOSITION_FILENAME_PARAM);
		
		Object value;
		if (contentType.isEmpty()) {
			value = readBinary(stream, fileName, BinaryData.CONTENT_TYPE_OCTET_STREAM);
		} else {
			MimeType mimeType = parseMimeType(contentType);
			String charSet = mimeType.getParameter("charset");
			if (charSet == null) {
				charSet = defaultEncoding;
			}
			if (MIME_TYPE_ANY_TEXT.match(mimeType)) {
				value = readString(stream, charSet);
			} else if (MIME_TYPE_JSON.match(mimeType)) {
				String stringValue = readString(stream, charSet);
				try {
					value = JSON.fromString(stringValue);
				} catch (com.top_logic.basic.json.JSON.ParseException ex) {
					throw new InvalidValueException("Invalid JSON value: " + stringValue, ex);
				}
			} else {
				value = readBinary(stream, fileName, mimeType.getBaseType());
			}
		}
		values.put(fieldName, value);
	}

	private String readString(MultipartStream stream, String charSet)
			throws MalformedStreamException, IOException, UnsupportedEncodingException {
		ByteArrayStream out = new ByteArrayStream();
		stream.readBodyData(out);
		String value;
		if (charSet == null) {
			value = new String(out.getOrginalByteBuffer(), 0, out.size());
		} else {
			value = new String(out.getOrginalByteBuffer(), 0, out.size(), charSet);
		}
		return value;
	}

	private ByteArrayStream readBinary(MultipartStream stream, String name, String contentType)
			throws MalformedStreamException, IOException {
		ByteArrayStream content;
		if (name == null) {
			content = new ByteArrayStream();
		} else {
			content = new InMemoryBinaryData(contentType, name);
		}
		stream.readBodyData(content);
		return content;
	}

	private MultipartStream parseMultipartBody(HttpServletRequest req, String characterEncoding)
			throws IOException, InvalidValueException {
		String contentType = req.getContentType();
		MimeType mimeType = parseMimeType(contentType);
		MultipartStream multipartStream =
			new MultipartStream(req.getInputStream(), getBoundary(mimeType, characterEncoding), 4096, null);
		multipartStream.setHeaderEncoding(characterEncoding);
		return multipartStream;
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

	private byte[] getBoundary(MimeType mimeType, String characterEncoding)
			throws UnsupportedEncodingException, InvalidValueException {
		String boundaryParam = mimeType.getParameter("boundary");
		if (boundaryParam == null) {
			throw new InvalidValueException("Missing boundary in mime type: " + mimeType);
		}
		byte[] boundary;
		if (characterEncoding != null) {
			boundary = boundaryParam.getBytes(characterEncoding);
		} else {
			boundary = boundaryParam.getBytes();
		}
		return boundary;
	}
}
