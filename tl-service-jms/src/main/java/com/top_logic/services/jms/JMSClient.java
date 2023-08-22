package com.top_logic.services.jms;

import java.io.Closeable;

import javax.jms.Destination;
import javax.jms.JMSContext;
import javax.jms.JMSException;

import com.ibm.msg.client.jms.JmsConnectionFactory;
import com.ibm.msg.client.jms.JmsFactoryFactory;
import com.ibm.msg.client.wmq.WMQConstants;

import com.top_logic.services.jms.JMSService.TargetQueueConfig;
import com.top_logic.util.Resources;

public class JMSClient implements Closeable {

	// System exit status value (assume unset value to be 1)
	private static int status = 1;

	private JmsFactoryFactory _ff;

	private JmsConnectionFactory _cf;

	private JMSContext _context;

	private Destination _destination;

	/**
	 * Constructor for every JMSClient (producer and consumer) that initializes the connection
	 * factory.
	 */
	public JMSClient(TargetQueueConfig config) throws JMSException {
		_ff = JmsFactoryFactory.getInstance(WMQConstants.WMQ_PROVIDER);
		_cf = _ff.createConnectionFactory();
		// Set the properties
		_cf.setStringProperty(WMQConstants.WMQ_HOST_NAME, config.getHost());
		_cf.setIntProperty(WMQConstants.WMQ_PORT, config.getPort());
		_cf.setStringProperty(WMQConstants.WMQ_CHANNEL, config.getChannel());
		_cf.setIntProperty(WMQConstants.WMQ_CONNECTION_MODE, WMQConstants.WMQ_CM_CLIENT);
		_cf.setStringProperty(WMQConstants.WMQ_QUEUE_MANAGER, config.getQueueManager());
		_cf.setStringProperty(WMQConstants.WMQ_APPLICATIONNAME,
			Resources.getSystemInstance().getString(com.top_logic.layout.I18NConstants.APPLICATION_TITLE));
		_cf.setBooleanProperty(WMQConstants.USER_AUTHENTICATION_MQCSP, true);
		_cf.setStringProperty(WMQConstants.USERID, config.getUser());
		_cf.setStringProperty(WMQConstants.PASSWORD, config.getPassword());
		// cf.setStringProperty(WMQConstants.WMQ_SSL_CIPHER_SUITE, "*TLS12ORHIGHER");

		// Create JMS objects
		_context = _cf.createContext();
		_destination = getContext().createQueue("queue:///" + config.getQueueName());
	}

	/**
	 * Record this run as successful.
	 */
	private static void recordSuccess() {
		System.out.println("SUCCESS");
		status = 0;
		return;
	}

	/**
	 * Record this run as failure.
	 *
	 * @param ex
	 *        desc
	 */
	private static void recordFailure(Exception ex) {
		if (ex != null) {
			if (ex instanceof JMSException) {
				processJMSException((JMSException) ex);
			} else {
				System.out.println(ex);
			}
		}
		System.out.println("FAILURE");
		status = -1;
		return;
	}

	/**
	 * Process a JMSException and any associated inner exceptions.
	 *
	 * @param jmsex
	 *        desc
	 */
	private static void processJMSException(JMSException jmsex) {
		System.out.println(jmsex);
		Throwable innerException = jmsex.getLinkedException();
		if (innerException != null) {
			System.out.println("Inner exception(s):");
		}
		while (innerException != null) {
			System.out.println(innerException);
			innerException = innerException.getCause();
		}
		return;
	}

	protected JMSContext getContext() {
		return _context;
	}

	protected Destination getDestination() {
		return _destination;
	}

	@Override
	public void close() {
		_context.close();
	}

}
