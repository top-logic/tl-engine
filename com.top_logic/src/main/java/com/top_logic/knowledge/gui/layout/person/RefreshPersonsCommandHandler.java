/*
 * SPDX-FileCopyrightText: 2005 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.gui.layout.person;

import java.util.List;
import java.util.Map;

import com.top_logic.base.locking.Lock;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.PolymorphicConfiguration;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.ListDefault;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.knowledge.wrap.person.PersonManager;
import com.top_logic.layout.DisplayContext;
import com.top_logic.mig.html.layout.LayoutComponent;
import com.top_logic.tool.boundsec.AbstractCommandHandler;
import com.top_logic.tool.boundsec.CommandGroupReference;
import com.top_logic.tool.boundsec.HandlerResult;
import com.top_logic.tool.boundsec.simple.SimpleBoundCommandGroup;
import com.top_logic.tool.execution.ExecutabilityRule;
import com.top_logic.tool.execution.RootOnlyExecutabilityRule;

/**
 * This handler synchronizes users with external systems.
 * 
 * @author    <a href="mailto:tri@top-logic.com">Thomas Richter</a>
 */
public class RefreshPersonsCommandHandler extends AbstractCommandHandler {

	/** ID of this handler. */
	public static final String COMMAND_ID = "refreshPersonsCommand";

	public interface Config extends AbstractCommandHandler.Config {

		@Override
		@FormattedDefault(SimpleBoundCommandGroup.WRITE_NAME)
		CommandGroupReference getGroup();

		@Override
		@ListDefault(RootOnlyExecutabilityRule.class)
		List<PolymorphicConfiguration<? extends ExecutabilityRule>> getExecutability();
	}

	public RefreshPersonsCommandHandler(InstantiationContext context, Config config) {
		super(context, config);
	}

	@Override
	public HandlerResult handleCommand(DisplayContext aContext, LayoutComponent aComponent, Object model, Map<String, Object> someArguments) {
		Lock theTokenCtxt = null;
		KnowledgeBase kb = KnowledgeBaseFactory.getInstance().getDefaultKnowledgeBase();
		try (Transaction tx = kb.beginTransaction()) {
			theTokenCtxt = PersonManager.getManager().initUserMap();
			if (theTokenCtxt != null && theTokenCtxt.isStateAcquired() && theTokenCtxt.check()) {
				aComponent.invalidate();
				tx.commit();
			}
		} catch (Exception e) {
			Logger.error("run(): Unexpected Error during refreshing of Users:", e, this);
			return HandlerResult.error(I18NConstants.REFRESH_ACCOUNTS_FAILED, e);
		} finally {
			if (theTokenCtxt != null) {
				try {
					theTokenCtxt.unlock();
				} catch (Exception ex) {
					// Ignore
				}
			} else {
				Logger.warn("Unable to get token for user refresh. Skipping this cycle", this);
			}
		}
		return HandlerResult.DEFAULT_RESULT;
	}

}
