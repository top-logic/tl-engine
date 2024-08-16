/*
 * SPDX-FileCopyrightText: 2024 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.mail.script;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import jakarta.activation.MimeType;
import jakarta.activation.MimeTypeParseException;
import jakarta.mail.Address;
import jakarta.mail.BodyPart;
import jakarta.mail.Message.RecipientType;
import jakarta.mail.MessagingException;
import jakarta.mail.Multipart;
import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeBodyPart;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMultipart;

import com.top_logic.base.mail.MailSenderService;
import com.top_logic.base.services.simpleajax.HTMLFragment;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.config.ConfigurationException;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.layout.basic.DummyDisplayContext;
import com.top_logic.mig.html.Media;
import com.top_logic.model.TLType;
import com.top_logic.model.search.expr.EvalContext;
import com.top_logic.model.search.expr.GenericMethod;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.config.operations.AbstractSimpleMethodBuilder;
import com.top_logic.model.search.expr.config.operations.ArgumentDescriptor;
import com.top_logic.model.search.expr.config.operations.MethodBuilder;
import com.top_logic.util.error.TopLogicException;

/**
 * TL-Script function sending an e-mail.
 */
public class SendMail extends GenericMethod {

	private static final String TEXT_PLAIN__UTF8 = textPlain();

	/**
	 * Creates a {@link SendMail} expression.
	 */
	protected SendMail(String name, SearchExpression[] arguments) {
		super(name, arguments);
	}

	@Override
	public GenericMethod copy(SearchExpression[] arguments) {
		return new SendMail(getName(), arguments);
	}

	@Override
	public TLType getType(List<TLType> argumentTypes) {
		return null;
	}

	@Override
	protected Object eval(Object[] arguments, EvalContext definitions) {
		String subject = asString(arguments[0]);
		List<Address> to = asAddresses(arguments[1]);
		Object body = arguments[2];
		Collection<?> attachements = asCollection(arguments[3]);
		List<Address> cc = asAddresses(arguments[4]);
		List<Address> bcc = asAddresses(arguments[5]);

		MailSenderService mailService = MailSenderService.getInstance();
		MimeMessage message = mailService.createEmptyMessage();
		try {
			message.setSubject(subject, StringServices.UTF8);

			addRecipients(message, RecipientType.TO, to);
			addRecipients(message, RecipientType.CC, cc);
			addRecipients(message, RecipientType.BCC, bcc);

			if (attachements.isEmpty()) {
				message.setDataHandler(asDataHandler(body));
			} else {
				Multipart multipart = new MimeMultipart();
				BodyPart bodyPart = asBodyPart(body, false);
				multipart.addBodyPart(bodyPart);
				
				for (Object attachement : attachements) {
					BodyPart attachementPart = asBodyPart(attachement, true);
					multipart.addBodyPart(attachementPart);
				}

				message.setContent(multipart);
			}
			
			return mailService.send(message, new ArrayList<>(), false);
		} catch (MessagingException ex) {
			throw new TopLogicException(I18NConstants.ERROR_SENDING_MAIL__TO_MSG.fill(to, ex.getMessage()), ex);
		}
	}

	private BodyPart asBodyPart(Object object, boolean withDefaultFileName) throws MessagingException {
		if (object instanceof BodyPart body) {
			return body;
		}

		MimeBodyPart result = new MimeBodyPart();
		if (object instanceof Map<?, ?> map) {
			Object cid = map.get("cid");
			if (cid != null) {
				result.setContentID(asString(cid));
			}

			Object description = map.get("description");
			if (description != null) {
				result.setDescription(asString(description));
			}

			Object headers = map.get("headers");
			if (headers instanceof Map<?, ?> headerMap) {
				for (Entry<?, ?> entry : headerMap.entrySet()) {
					result.setHeader(asString(entry.getKey()), asString(entry.getValue()));
				}
			}

			Object data = map.get("data");
			DataHandler handler = asDataHandler(data);
			result.setDataHandler(handler);

			Object filename = map.get("filename");
			if (filename != null) {
				result.setFileName(asString(filename));
			} else if (withDefaultFileName) {
				result.setFileName(handler.getName());
			}

			Object disposition = map.get("disposition");
			if (disposition != null) {
				result.setDisposition(asString(disposition));
			} else if (map.containsKey("disposition")) {
				result.setDisposition(null);
			}

			return result;
		}

		DataHandler handler = asDataHandler(object);
		result.setDataHandler(handler);
		if (withDefaultFileName) {
			result.setFileName(handler.getName());
		}
		return result;
	}

	private DataHandler asDataHandler(Object input) {
		Object object;
		if (input instanceof HTMLFragment html) {
			object = new BinaryDataSource() {
				@Override
				public String getName() {
					return "content.html";
				}

				@Override
				public long getSize() {
					return -1;
				}

				@Override
				public String getContentType() {
					return "text/html; charset=utf-8";
				}

				@Override
				public void deliverTo(OutputStream out) throws IOException {
					TagWriter xml = new TagWriter(new OutputStreamWriter(out, StandardCharsets.UTF_8));
					html.write(new DummyDisplayContext().initOutputMedia(Media.PDF), xml);
					xml.flush();
				}
			};
		} else {
			object = input;
		}
		
		if (object instanceof BinaryDataSource binary) {
			return new DataHandler(new DataSource() {
				@Override
				public OutputStream getOutputStream() throws IOException {
					throw new UnsupportedOperationException("Cannot write to a binary data.");
				}

				@Override
				public String getName() {
					return binary.getName();
				}

				@Override
				public InputStream getInputStream() throws IOException {
					return binary.toData().getStream();
				}

				@Override
				public String getContentType() {
					return binary.getContentType();
				}
			});
		}

		DataHandler result = new DataHandler(new DataSource() {
			@Override
			public OutputStream getOutputStream() throws IOException {
				throw new UnsupportedOperationException("Cannot write to a binary data.");
			}

			@Override
			public String getName() {
				return "content.txt";
			}

			@Override
			public InputStream getInputStream() throws IOException {
				return new ByteArrayInputStream(asString(object).getBytes(StandardCharsets.UTF_8));
			}

			@Override
			public String getContentType() {
				return TEXT_PLAIN__UTF8;
			}
		});
		return result;
	}

	private void addRecipients(MimeMessage message, RecipientType type, List<Address> addresses)
			throws MessagingException {
		for (Address address : addresses) {
			message.setRecipient(type, address);
		}
	}

	private List<Address> asAddresses(Object object) {
		return asCollection(object).stream().map(this::asAddress).toList();
	}

	private Address asAddress(Object object) {
		if (object instanceof Address address) {
			return address;
		}

		String email = asString(object);
		try {
			return new InternetAddress(email);
		} catch (AddressException ex) {
			throw new TopLogicException(I18NConstants.ERROR_INVALID_ADDRESS__VAL_MSG.fill(email, ex.getMessage()), ex);
		}
	}

	@Override
	public boolean isSideEffectFree() {
		return false;
	}

	private static String textPlain() {
		try {
			MimeType result = new MimeType("text", "plain");
			result.setParameter("charset", StandardCharsets.UTF_8.name());
			return result.toString();
		} catch (MimeTypeParseException ex) {
			return "text/plain";
		}
	}

	/**
	 * {@link MethodBuilder} creating {@link SendMail} expressions.
	 */
	public static final class Builder extends AbstractSimpleMethodBuilder<SendMail> {

		private static final ArgumentDescriptor DESCRIPTOR = ArgumentDescriptor.builder()
			.mandatory("subject")
			.mandatory("to")
			.mandatory("body")
			.optional("attachements")
			.optional("cc")
			.optional("bcc")
			.build();

		/**
		 * Creates a {@link Builder}.
		 */
		public Builder(InstantiationContext context, Config<?> config) {
			super(context, config);
		}

		@Override
		public ArgumentDescriptor descriptor() {
			return DESCRIPTOR;
		}

		@Override
		public SendMail build(Expr expr, SearchExpression[] args) throws ConfigurationException {
			return new SendMail(getConfig().getName(), args);
		}
	}

}
