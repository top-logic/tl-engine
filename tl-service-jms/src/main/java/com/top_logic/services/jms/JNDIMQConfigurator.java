package com.top_logic.services.jms;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.services.jms.JMSService.MQSystemConfigurator;

import jakarta.jms.ConnectionFactory;
import jakarta.jms.JMSException;

public class JNDIMQConfigurator extends AbstractConfiguredInstance<JNDIMQConfigurator.Config<?>> implements MQSystemConfigurator {

	/**
	 * Configuration options for {@link JNDIMQConfigurator}.
	 */
	public interface Config<I extends JNDIMQConfigurator> extends PolymorphicConfiguration<I> {
		/**
		 * 
		 */
	}

	/**
	 * Creates a {@link JNDIMQConfigurator} from configuration.
	 * 
	 * @param config
	 *        The configuration.
	 */
	public JNDIMQConfigurator(InstantiationContext context, Config<?> config) {
		super(context, config);
//		_processing = QueryExecutor.compile(config.getProcessing());
	}

	@Override
	public ConnectionFactory setupMQConnection(String un, String pw) throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}
}
