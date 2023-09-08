/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.knowledge.indexing.lucene;

import java.util.List;

import com.top_logic.basic.tools.LockRequest;
import com.top_logic.basic.tools.LockWaitMonitor;
import com.top_logic.util.monitor.AbstractMonitorComponent;
import com.top_logic.util.monitor.ApplicationMonitor;
import com.top_logic.util.monitor.MonitorComponent;
import com.top_logic.util.monitor.MonitorMessage;
import com.top_logic.util.monitor.MonitorResult;

/**
 * {@link MonitorComponent} for the {@link LuceneIndex}.
 * 
 * @author <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public class LuceneMonitor extends AbstractMonitorComponent {

    /**
     * Will be Created via Introspection by {@link ApplicationMonitor}
     */
    public LuceneMonitor() {
        super();
    }

    /**
     * @see com.top_logic.util.monitor.MonitorComponent#getName()
     */
    @Override
	public String getName() {
        return ("IndexingService");
    }

    /**
     * @see com.top_logic.util.monitor.MonitorComponent#getDescription()
     */
    @Override
	public String getDescription() {
        return ("Monitor the Lucene index and search service");
    }

    /**
     * Check for {@link LuceneIndex#isAlive()}, {@link LuceneIndex#isStopping()},
     * {@link LockWaitMonitor#filterPendingLockRequest(String) long waiting lock requests}
     * and {@link LuceneIndex#queueSizes()}. 
     */
    @Override
	public void checkState(MonitorResult result) {

        LuceneIndex lci = LuceneIndex.getInstance();
        if (!lci.isAlive()) {
			result.addMessage(new MonitorMessage(MonitorMessage.Status.FATAL, "Thread not running", this));
			return;
        }

		int queueSize = lci.queueSizes();
        int failsInSuccession = lci.getFailsInSuccession();
		if (failsInSuccession == 1) {

			String message = "Lucene failed and is restarting. But its index might be corrupted."
				+ " Queue size: " + queueSize;
			result.addMessage(new MonitorMessage(MonitorMessage.Status.FATAL, message, this));
			return;
		}
		else if (failsInSuccession > 1) {

			String message = "Lucene failed " + failsInSuccession + " times in succession."
				+ " It will wait some time before restarting. Also, its index might be corrupted."
				+ " Queue size: " + queueSize;
			result.addMessage(new MonitorMessage(MonitorMessage.Status.FATAL, message, this));
			return;
        }

        if (lci.isIndexUntrustworthy()) {
			String message = "Errors have occurred that might have corrupted the index."
				+ " Queue size: " + queueSize;
			result.addMessage(new MonitorMessage(MonitorMessage.Status.FATAL, message, this));
			return;
        }

        if (lci.isStopping()) {
			result.addMessage(new MonitorMessage(MonitorMessage.Status.ERROR, "Thread is stopping", this));
			return;
        }

        List<LockRequest> longWaitingLockRequests = LockWaitMonitor.INSTANCE.findWarnTimedOutRequests("Lucene");
        if ( ! longWaitingLockRequests.isEmpty()) {

			String message = "Check Logfiles / Lucene locks, queueSize=" + queueSize
				+ ", long waiting lock requests: " + longWaitingLockRequests;
			result.addMessage(new MonitorMessage(MonitorMessage.Status.ERROR, message, this));
			return;
        }

        if (queueSize > lci.getWarnQueueSize()) {
        	String message = "Check Logfiles / Lucene locks, queueSize=" + queueSize + ". Queue is unexpected long!";
			result.addMessage(new MonitorMessage(MonitorMessage.Status.ERROR, message, this));
			return;
        }
		result.addMessage(new MonitorMessage(MonitorMessage.Status.INFO, "Running, queueSize=" + queueSize, this));
    }

}
