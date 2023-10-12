package com.top_logic.services.jms;

import javax.jms.ConnectionFactory;
import javax.jms.JMSException;

import com.top_logic.basic.config.AbstractConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.services.jms.JMSService.MQSystemConfig;

public class JNDIMQConfig extends AbstractConfiguredInstance<JNDIMQConfig.Config<?>> implements MQSystemConfig {

	/**
	 * Configuration options for {@link JNDIMQConfig}.
	 */
	public interface Config<I extends JNDIMQConfig> extends PolymorphicConfiguration<I> {
		/**
		 * 
		 */
	}

	/**
	 * Creates a {@link JNDIMQConfig} from configuration.
	 * 
	 * @param config
	 *        The configuration.
	 */
	public JNDIMQConfig(InstantiationContext context, Config<?> config) {
		super(context, config);
//		_processing = QueryExecutor.compile(config.getProcessing());
	}

	@Override
	public ConnectionFactory setupMQConnection() throws JMSException {
		// TODO Auto-generated method stub
		return null;
	}
}
