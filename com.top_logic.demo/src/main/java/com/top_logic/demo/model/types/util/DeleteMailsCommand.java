/*
 * SPDX-FileCopyrightText: 2015 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.demo.model.types.util;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;

import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseException;
import com.top_logic.knowledge.service.KnowledgeBaseRuntimeException;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mail.base.MailFolder;
import com.top_logic.mail.base.MailFolderAware;
import com.top_logic.mail.base.MailServer;
import com.top_logic.mail.base.imap.IMAPMail;
import com.top_logic.mail.proxy.exchange.ExchangeMail;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.model.TLObject;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.ExecutableState;

/**
 * {@link AbstractCommandHandler} deleting all mails of the model of the component.
 * 
 * @since 5.7.5
 * 
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class DeleteMailsCommand extends AbstractCommandHandler {

	/**
	 * Default {@link ExecutabilityRule} for the {@link DeleteMailsCommand}.
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public static class DeleteMailsExecutability implements ExecutabilityRule {

		@Override
		public ExecutableState isExecutable(LayoutComponent aComponent, Object model, Map<String, Object> someValues) {
			if (!MailServer.isActivated() || !(model instanceof MailFolderAware)) {
				return ExecutableState.NOT_EXEC_HIDDEN;
			}
			MailFolderAware mfa = (MailFolderAware) model;
			MailFolder mailFolder = mfa.getMailFolder();
			if (mailFolder == null) {
				return ExecutableState.createDisabledState(I18NConstants.NOT_EXECUTABLE_NO_MAIL_FOLDER);
			}
			{
				if (mailFolder.isEmpty()) {
					return ExecutableState.createDisabledState(I18NConstants.NOT_EXECUTABLE_EMPTY_MAIL_FOLDER);
				}
			}
			return ExecutableState.EXECUTABLE;
		}

	}

	/**
	 * Configuration of the {@link DeleteMailsCommand}
	 * 
	 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
	 */
	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@ListDefault(DeleteMailsExecutability.class)
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();

	}

	/**
	 * Creates a new {@link DeleteMailsCommand}.
	 */
	public DeleteMailsCommand(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent,
			Object model, Map<String, Object> someArguments) {
		MailFolderAware mailfolderAware = (MailFolderAware) model;
		MailFolder folder = mailfolderAware.getMailFolder();

		{
			KnowledgeBase kb = mailfolderAware.getKnowledgeBase();
			try (Transaction tx = kb.beginTransaction()) {
				Collection<? extends TLObject> content = folder.getContent();
				for (TLObject mail : content) {
					deleteMail((IMAPMail) mail);
				}
				tx.commit();
			} catch (KnowledgeBaseException ex) {
				throw new KnowledgeBaseRuntimeException(ex);
			} catch (MessagingException ex) {
				throw new RuntimeException(ex);
			}
		}
		return HandlerResult.DEFAULT_RESULT;
	}

	private void deleteMail(IMAPMail mail) throws MessagingException {
		new ExchangeMail(mail.getOriginalMail()).delete(true);
		mail.tDelete();
	}

}

