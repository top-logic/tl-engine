/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.service.monitor;

import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;
import com.top_logic.knowledge.service.HistoryUtils;
import com.top_logic.knowledge.service.KnowledgeBase;
import com.top_logic.knowledge.service.KnowledgeBaseFactory;
import com.top_logic.util.monitor.AbstractMonitorComponent;
import com.top_logic.util.monitor.MonitorMessage;
import com.top_logic.util.monitor.MonitorResult;

/**
 * Monitor to check if the default Knowledgebase works correctly.
 * 
 * @author     <a href="mailto:mga@top-logic.com">Michael Gänsler</a>
 */
public class DBKBMonitor extends AbstractMonitorComponent {

    /**
     * Default CTor
     */
    public DBKBMonitor() {
        super();
    }

    /**
     * @see com.top_logic.util.monitor.MonitorComponent#getName()
     */
    @Override
	public String getName() {
		return ("KnowledgeBase");
    }

    /**
     * @see com.top_logic.util.monitor.MonitorComponent#getDescription()
     */
    @Override
	public String getDescription() {
		return ("Monitor for KnowledgeBases.");
    }

    @Override
	public void checkState(final MonitorResult result) {
		ThreadContext.inSystemContext(this.getClass(), new Computation<Void>() {
            
            @Override
			public Void run() {
            	KnowledgeBaseFactory kbFactory = KnowledgeBaseFactory.getInstance();
				for (String kbName : kbFactory.getKnowledgeBaseNames()) {
					MonitorMessage msg;
            		try {
            			KnowledgeBase kb = kbFactory.getKnowledgeBase(kbName);
						StringBuilder message = new StringBuilder();
						message.append("Database (");
						message.append(kb);
						message.append(", current revision: ");
						message.append(HistoryUtils.getLastRevision(kb.getHistoryManager()));
						message.append(") OK!");
						msg = new MonitorMessage(MonitorMessage.Status.INFO, message.toString(), DBKBMonitor.this);
					} catch (Exception ex) {
						msg = new MonitorMessage(MonitorMessage.Status.FATAL, "Error: " + ex, DBKBMonitor.this);
					}
					result.addMessage(msg);
            	}
				return null;
            }
		});
    }

}
