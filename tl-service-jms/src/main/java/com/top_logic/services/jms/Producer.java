/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms;

import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.services.jms.JMSService.DestinationConfig;

import jakarta.jms.BytesMessage;
import jakarta.jms.JMSException;
import jakarta.jms.JMSProducer;
import jakarta.jms.TextMessage;

/**
 * Class for a jms producer (sends messages to a queue).
 */
public class Producer extends JMSClient {
	
	private JMSProducer _producer;
	
	/**
	 * Constructor for a producer that sets the config and creates an Producer on the context.
	 * 
	 * @param config
	 *        The config for the connection to the queue
	 * @throws JMSException
	 *         Exception if something is not jms conform
	 */
	public Producer(DestinationConfig config) throws JMSException {
		super(config);
		_producer = getContext().createProducer();
	}

	/**
	 * Method to send a string (or XML) message to a queue or a topic.
	 * 
	 * @param text
	 *        The message to be sent as string
	 */
	public void send(String text) {
		TextMessage message = getContext().createTextMessage(text);
		_producer.send(getDestination(), message);
	}

	/**
	 * Method to send a binary message to a queue or a topic.
	 * 
	 * @param data
	 *        Binary message to be sent
	 */
	public void send(BinaryDataSource data) {
		BytesMessage message = getContext().createBytesMessage();
		OutputStream out = new OutputStream() {

			@Override
			public void write(int b) throws IOException {
				try {
					message.writeByte((byte) b);
				} catch (JMSException ex) {
					throw new IOException(ex);
				}
			}

			@Override
			public void write(byte[] b) throws IOException {
				try {
					message.writeBytes(b);
				} catch (JMSException ex) {
					throw new IOException(ex);
				}
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				try {
					message.writeBytes(b, off, len);
				} catch (JMSException ex) {
					throw new IOException(ex);
				}
			}
		};
		try {
			data.deliverTo(out);
		} catch (IOException ex) {
			throw new IOError(ex);
		}
		try {
			message.setStringProperty(getCharsetProperty(),
				new MimeType(data.getContentType()).getParameter("charset"));
		} catch (JMSException ex) {
			ex.printStackTrace();
		} catch (MimeTypeParseException ex) {
			ex.printStackTrace();
		}
		_producer.send(getDestination(), message);
	}
}
