/*
 * SPDX-FileCopyrightText: 2017 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.kafka.layout.kafka;

import com.top_logic.kafka.layout.kafka.KafkaMessageDetailColumnRenderer.KafkaMessageRenderer;
import com.top_logic.layout.ReadOnlyAccessor;
import com.top_logic.layout.tree.model.TLTreeNode;

/**
 * Accessor to the kafka topic interface.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class KafkaTopicAccessor extends ReadOnlyAccessor<Object> {

	/** Access to {@link KafkaTopic#getName()}. */
	public static final String KEY_NAME = "name";

	/** Access to {@link KafkaTopic#getID()}. */
	public static final String KEY_ID = "id";

	/** Access to {@link KafkaMessage#getValue()}. */
	public static final String KEY_MESSAGE = "message";

	/** Access to {@link KafkaMessage#getDate()}. */
	public static final String KEY_DATE = "date";

	/** Access to {@link KafkaMessage#getDate()}. */
	public static final String KEY_DETAILS = "details";

	/** Access to {@link KafkaMessage#getKey()}. */
	public static final String KEY_KEY = "key";

	/** Access to {@link KafkaMessage#getOffset()}. */
	public static final String KEY_OFFSET = "offset";

	@Override
	public Object getValue(Object object, String property) {
		Object theObject = (object instanceof TLTreeNode) ? ((TLTreeNode<?>) object).getBusinessObject() : object;

		switch (property) {
			case KEY_NAME:
				return (theObject instanceof KafkaTopic) ? ((KafkaTopic<?>) theObject).getName() : null;
			case KEY_ID:
				return (theObject instanceof KafkaTopic) ? ((KafkaTopic<?>) theObject).getID() : null;
			case KEY_MESSAGE:
				return (theObject instanceof KafkaMessage) ? getMessage((KafkaMessage) theObject) : null;
			case KEY_DATE:
				return (theObject instanceof KafkaMessage)
					? DateAccessor.INSTANCE.getValue((KafkaMessage) theObject, property) : null;
			case KEY_DETAILS:
				return (theObject instanceof KafkaMessage) ? getDetails((KafkaMessage) theObject) : null;
			case KEY_KEY:
				return (theObject instanceof KafkaMessage) ? ((KafkaMessage) theObject).getKey() : null;
			case KEY_OFFSET:
				return (theObject instanceof KafkaMessage) ? ((KafkaMessage) theObject).getOffset() : null;
			default:
				return null;
		}
	}

	/**
	 * Return the detail information to the given message.
	 * 
	 * @param aMessage
	 *        The message to get the detail from.
	 * @return In this implementation, the given message.
	 */
	protected Object getDetails(KafkaMessage aMessage) {
		return aMessage;
	}

	/**
	 * Return the value of the given message.
	 * 
	 * @param aMessage
	 *        The message to get the detail from.
	 * @return The requested value as escaped string.
	 */
	protected Object getMessage(KafkaMessage aMessage) {
		return KafkaMessageRenderer.escape(aMessage.getValue().toString());
	}

	/**
	 * Accessor to {@link KafkaMessage#getDate()}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static final class DateAccessor extends ReadOnlyAccessor<KafkaMessage> {

		/** Instance of {@link DateAccessor}. */
		public static DateAccessor INSTANCE = new DateAccessor();

		@Override
		public Object getValue(KafkaMessage object, String property) {
			return object.getDate();
		}

	}
}
