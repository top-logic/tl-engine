/*
 * SPDX-FileCopyrightText: 2023 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.services.jms;

import com.top_logic.basic.config.NamedConfigMandatory;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.Mandatory;
import com.top_logic.basic.config.annotation.Name;

/**
 * Base configuration interface for all Clients ({@link Producer} and {@link Consumer}).
 * 
 * @author <a href="mailto:simon.haneke@top-logic.com">Simon Haneke</a>
 */
public interface ClientConfig<I> extends PolymorphicConfiguration<I>, NamedConfigMandatory {
	/**
	 * Configuration name for {@link #getType()}
	 */
	String TYPE = "type";

	/**
	 * Configuration name for {@link #getDestName()}.
	 */
	String DEST_NAME = "dest-name";

	/**
	 * The type the destination of the connection has.
	 */
	@Name(TYPE)
	ClientConfig.Type getType();

	/**
	 * The name of the Queue or Topic that is the destination of the connection. Or the JNDI name of
	 * the destination.
	 */
	@Mandatory
	@Name(DEST_NAME)
	String getDestName();

	/**
	 * The type of the destination, that can be a queue or a topic.
	 */
	public enum Type {
		/**
		 * A queue is a point-to-point connection between a producer and a consumer.
		 */
		QUEUE,

		/**
		 * A topic is a publish/subscribe connection so multiple subscribed consumers can
		 * receive messages simultaneously from a topic.
		 */
		TOPIC;
	}
}