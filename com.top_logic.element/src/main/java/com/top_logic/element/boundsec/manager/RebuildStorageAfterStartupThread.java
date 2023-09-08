/*
 * SPDX-FileCopyrightText: 2010 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.element.boundsec.manager;

import com.top_logic.base.administration.MaintenanceWindowManager;
import com.top_logic.base.cluster.ClusterManager;
import com.top_logic.base.cluster.ClusterManager.PropertyType;
import com.top_logic.base.cluster.PendingChangeException;
import com.top_logic.basic.Logger;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.util.Computation;
import com.top_logic.tool.boundsec.manager.AccessManager;

/**
 * Thread to rebuild security storage after system startup.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class RebuildStorageAfterStartupThread extends Thread {

    /** The name of this Thread. */
    public static final String THREAD_NAME = "RebuildStorageAfterStartupThread";

    public static final String CM_PROPERTY_REBUILD_AFTER_STARTUP = "sam_rebuild_requested";

    public static final int STATE_REBUILD_REQUIRED = 1;
    public static final int STATE_REBUILDING = 2;
    public static final int STATE_REBUILD_DONE = 0;


    /** Flag whether to leave maintenance window manager afterwards. */
    private Boolean leaveMaintenanceWindow;


    /**
     * Creates a new instance of this class.
     */
    public RebuildStorageAfterStartupThread() {
        this(null);
    }

    /**
     * Creates a new instance of this class.
     */
    public RebuildStorageAfterStartupThread(Boolean leaveMaintenanceWindow) {
        super(THREAD_NAME);
        this.leaveMaintenanceWindow = leaveMaintenanceWindow;
    }


    @Override
    public void run() {
        Throwable problem = ThreadContext.inSystemContext(RebuildStorageAfterStartupThread.class, new Computation<Throwable>() {
            @Override
			public Throwable run() {
            	try {
            		doRun();
            	} catch(Throwable ex) {
            		return ex;
            	}
                return null;
            }
        });
        if (problem != null) {
        	Logger.fatal("Unable to rebuild storage after startup. Security may be corrupt.", problem, RebuildStorageAfterStartupThread.class);
        }
    }


    /**
     * Waits the given amount of time and enters the maintenance window mode afterwards.
     */
    public void doRun() {
        ClusterManager cm = ClusterManager.getInstance();

        boolean allRunning = cm.allNodesRunning();
        if (!allRunning) {
            Logger.info("Waiting until all nodes in cluster are running...", RebuildStorageAfterStartupThread.class);
        }
        while (!allRunning) try {
            Thread.sleep(12000);
        }
        catch (InterruptedException e) {
            // ignore;
        }
        finally {
            allRunning = cm.allNodesRunning();
        }

        int value = cm.<Integer>getConfirmedValueWaiting(CM_PROPERTY_REBUILD_AFTER_STARTUP).intValue();
        if (value == STATE_REBUILD_REQUIRED) {
            try {
                cm.setValueIfUnchanged(CM_PROPERTY_REBUILD_AFTER_STARTUP, Integer.valueOf(STATE_REBUILDING));
            }
            catch (PendingChangeException e) {
                Logger.info("Another node is already rebuilding security after startup. Nothing to do for this node.", RebuildStorageAfterStartupThread.class);
                return;
            }
            Logger.info("Preparing rebuilding security storage after startup...", RebuildStorageAfterStartupThread.class);
            cm.setValueAndWait(CM_PROPERTY_REBUILD_AFTER_STARTUP, Integer.valueOf(STATE_REBUILDING));
            MaintenanceWindowManager mwm = MaintenanceWindowManager.getInstance();
            int state = mwm.getMaintenanceModeState();
            boolean leaveMode = leaveMaintenanceWindow != null ? leaveMaintenanceWindow.booleanValue() : state == MaintenanceWindowManager.DEFAULT_MODE;

            if (state != MaintenanceWindowManager.IN_MAINTENANCE_MODE) {
                long timeleft = mwm.getTimeLeft();
                if (!cm.isClusterMode() || timeleft > mwm.minIntervallInCluster) {
                    mwm.enterMaintenanceWindow();
                }
                if (cm.isClusterMode()) try {
                    Thread.sleep(Math.min(timeleft, mwm.minIntervallInCluster) + 1000L);
                }
                catch (InterruptedException e) {
                    // ignore
                }
            }
            cm.waitForConfirmation(CM_PROPERTY_REBUILD_AFTER_STARTUP);
            try {
                Logger.info("Rebuilding security storage after startup...", RebuildStorageAfterStartupThread.class);
                AccessManager.getInstance().reload();
                Logger.info("Rebuilding security storage after startup completed.", RebuildStorageAfterStartupThread.class);
            }
            finally {
                cm.setValue(CM_PROPERTY_REBUILD_AFTER_STARTUP, Integer.valueOf(STATE_REBUILD_DONE));
                if (leaveMode) {
                    mwm.leaveMaintenanceWindow();
                }
            }
        }
        else if (value == STATE_REBUILDING) {
            Logger.info("Another node is already rebuilding security after startup. Nothing to do for this node.", RebuildStorageAfterStartupThread.class);
        }
        else {
            Logger.info("Rebuilding was already done by another node. Nothing to do for this node.", RebuildStorageAfterStartupThread.class);
        }
    }


    /**
     * Marks the application to rebuild security storage after startup.
     */
    static void rebuildAfterStartup() {
        ClusterManager cm = ClusterManager.getInstance();
        cm.declareValue(CM_PROPERTY_REBUILD_AFTER_STARTUP, PropertyType.INT);
        Integer value = cm.getLatestUnconfirmedValue(CM_PROPERTY_REBUILD_AFTER_STARTUP);
        Boolean leaveAfterwards = null;
        if (value == null) {
            cm.setValue(CM_PROPERTY_REBUILD_AFTER_STARTUP, Integer.valueOf(STATE_REBUILD_REQUIRED));
            MaintenanceWindowManager mwm = MaintenanceWindowManager.getInstance();
            leaveAfterwards = Boolean.valueOf(mwm.getMaintenanceModeState() == MaintenanceWindowManager.DEFAULT_MODE);
            mwm.enterMaintenanceWindow();
        }
        Logger.info("Planning security rebuilding after startup...", RebuildStorageAfterStartupThread.class);
        new RebuildStorageAfterStartupThread(leaveAfterwards).start();
    }

}
