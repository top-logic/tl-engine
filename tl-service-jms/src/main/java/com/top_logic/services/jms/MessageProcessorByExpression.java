package com.top_logic.services.jms;

import javax.jms.Message;

import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.model.search.expr.config.dom.Expr;
import com.top_logic.model.search.expr.query.QueryExecutor;
import com.top_logic.services.jms.JMSService.MessageProcessor;

public class MessageProcessorByExpression implements MessageProcessor {

	private QueryExecutor _processing;

	public interface Config extends PolymorphicConfiguration {
		/**
		 * The function that will be used to process messages the consumer will receive.
		 */
		Expr getProcessing();
	}

	/**
	 * Creates a {@link MessageProcessorByExpression} from configuration.
	 * 
	 * @param config
	 *        The configuration.
	 */
	public MessageProcessorByExpression(Config config) {
		super();
		_processing = QueryExecutor.compile(config.getProcessing());
	}

	@Override
	public void processMessage(Message message) {
		_processing.execute(message);
	}

}