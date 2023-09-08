/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import java.util.regex.Pattern;

import com.top_logic.demo.model.types.DemoTypesFactory;
import com.top_logic.element.structured.StructuredElement;
import com.top_logic.mail.base.MailFolderAware;
import com.top_logic.mail.proxy.AbstractMailProcessor;
import com.top_logic.mail.proxy.ConfiguredMailServerDaemon;
import com.top_logic.mail.proxy.MailProcessor;
import com.top_logic.mail.proxy.MailServerMessage;
import com.top_logic.mail.proxy.exchange.ExchangeMeeting;

/**
 * {@link MailProcessor} for demo types.
 * 
 * <p>
 * To find the {@link MailFolderAware} for a mail the subject of the mail must be of the following
 * form
 * 
 * <pre>
 *  subject : "pathToObject" ':' "any message"
 *  pathToObject : "name of an object", "name of an object" '-&gt;'
 * </pre>
 * 
 * E.g. a mail with subject "B0 -&gt; A0 -&gt; C0 : Some message" would search the element with name
 * C0 as child of element with name A0 as child of element with name B0 as child of root.
 * </p>
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DemoTypesMailProcessor extends AbstractMailProcessor {

	private static Pattern PATH_SPLIT_PATTERN = Pattern.compile("\\s*->\\s*");

	@Override
	public boolean processMeeting(ExchangeMeeting meeting, ConfiguredMailServerDaemon mailServerDaemon, Object token) {
		return false;
	}

	@Override
	public MailFolderAware getMailFolderAware(MailServerMessage message, ConfiguredMailServerDaemon mailServerDaemon, Object token) {

		String name = message.getName();
		int sepIndex = name.indexOf(':');
		if (sepIndex < 0) {
			return null;
		}
		String objectPath = name.substring(0, sepIndex);
		objectPath = objectPath.trim();
		String[] path = PATH_SPLIT_PATTERN.split(objectPath);
		StructuredElement result = DemoTypesFactory.getInstance().getRoot();
		pathStep:
		for (String objectName : path) {
			for (StructuredElement child : result.getChildren()) {
				if (objectName.equals(child.getName())) {
					result = child;
					continue pathStep;
				}
			}
			// No path step with name "objectName" found
			return null;
		}
		if (result instanceof MailFolderAware) {
			return (MailFolderAware) result;
		}
		return null;
	}

}

