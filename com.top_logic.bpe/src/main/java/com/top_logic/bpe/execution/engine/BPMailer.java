/*
 * SPDX-FileCopyrightText: 2019 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.bpe.execution.engine;

import static com.top_logic.model.search.expr.SearchExpressionFactory.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.top_logic.base.mail.MailHelper;
import com.top_logic.base.mail.MailHelper.SendMailResult;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.xml.TagWriter;
import com.top_logic.bpe.bpml.model.SendTask;
import com.top_logic.bpe.execution.model.ProcessExecution;
import com.top_logic.bpe.execution.model.Token;
import com.top_logic.contact.business.PersonContact;
import com.top_logic.knowledge.wrap.person.Person;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.DefaultDisplayContext;
import com.top_logic.model.search.expr.SearchExpression;
import com.top_logic.model.search.expr.query.ExpressionFragment;
import com.top_logic.tool.boundsec.wrap.Group;

/**
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class BPMailer {

	private Token _token;

	private ProcessExecution _processExecution;

	private SendTask _sendTask;

	public BPMailer(Token token, ProcessExecution processExecution, SendTask sendTask) {
		_token = token;
		_processExecution = processExecution;
		_sendTask = sendTask;
	}

	public void sendMail() {
		MailHelper mailHelper = MailHelper.getInstance();
		List<String> receivers = getReceivers();
		String subject = getSubject();
		String content = getContent();
		Logger.info("Send mail to " + receivers + ", subject: " + subject + ", content: " + content, BPMailer.class);
		SendMailResult result = mailHelper.sendSystemMail(receivers, subject,
			content, MailHelper.CONTENT_TYPE_HTML_UTF8);
		Logger.info("Result of send mail: " + result, BPMailer.class);
	}

	private String getSubject() {
		SearchExpression template = _sendTask.getSubjectTemplate();
		return substitute(template);
	}

	private String getContent() {
		SearchExpression template = _sendTask.getContentTemplate();
		return substitute(template);
	}

	private String substitute(SearchExpression template) {

		TagWriter tagWriter = new TagWriter();
		DisplayContext displayContext = DefaultDisplayContext.getDisplayContext();


		if (template != null) {
			ExpressionFragment expressionFragment = new ExpressionFragment(call(template, literal(_token)));
			try {
				expressionFragment.write(displayContext, tagWriter);
				return tagWriter.toString();
			} catch (IOException ex) {
				Logger.error("Problem expanding template", ex, this);
			}
		}
		return "";
	}

	private List<String> getReceivers() {
		List<String> adresses = new ArrayList<>();
		for (PersonContact contact : getReceiverContacts()) {
			String mail = contact.getEMail();
			if (!StringServices.isEmpty(mail)) {
				adresses.add(mail);
			}
		}
		return adresses;
	}

	private Collection<PersonContact> getReceiverContacts(){
		Collection<PersonContact> res = new HashSet<>();
		SearchExpression receiverRule = _sendTask.getReceiverRule();
		if (receiverRule != null) {
			Object calculate = ExecutionEngine.getInstance().calculate(receiverRule, _processExecution);
			if (calculate instanceof Collection) {
				for (Object obj : ((Collection) calculate)) {
					if (obj instanceof PersonContact) {
						res.add((PersonContact) obj);
					}
					if (obj instanceof Person) {
						res.add(PersonContact.getPersonContact((Person) obj));
					}
				}
			}
			else if (calculate instanceof PersonContact) {
				res.add((PersonContact) calculate);
			}
			else if (calculate instanceof Person) {
				res.add(PersonContact.getPersonContact((Person) calculate));
			}
		}

		Set<? extends Group> receiverGroups = _sendTask.getReceiverGroups();
		for (Group group : receiverGroups) {
			for (Person person : group.getMembers()) {
				res.add(PersonContact.getPersonContact(person));
			}
		}
		return res;
	}
}
