/*
 * Copyright (c) 2023 Business Operation Systems GmbH. All Rights Reserved.
 */
package com.top_logic.services.jms;

import java.io.IOError;
import java.io.IOException;
import java.io.OutputStream;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.JMSProducer;
import javax.jms.TextMessage;

import com.ibm.msg.client.wmq.WMQConstants;

import com.top_logic.basic.io.binary.BinaryDataSource;
import com.top_logic.services.jms.JMSService.DestinationConfig;

/**
 * Class for a jms producer (sends messages to a queue)
 */
public class Producer extends JMSClient {
	
	private JMSProducer _producer;
	
	/**
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
	 * @param text
	 *        The message to be sent as string
	 */
	public void send(String text) {
		TextMessage message = getContext().createTextMessage(text);
		_producer.send(getDestination(), message);
	}

	/**
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
			message.setStringProperty(WMQConstants.JMS_IBM_CHARACTER_SET,
				new MimeType(data.getContentType()).getParameter("charset"));
		} catch (JMSException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		} catch (MimeTypeParseException ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
		_producer.send(getDestination(), message);
	}
}
