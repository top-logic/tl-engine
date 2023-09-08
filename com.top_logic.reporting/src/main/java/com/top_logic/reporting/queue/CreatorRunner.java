/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.reporting.queue;

import java.io.File;

import com.top_logic.basic.Logger;

/**
 * Use a {@link ReportCreator} to move Files from {@link #workDir} into {@link #openDir} wile not {@link #stopped}.
 * 
 * @author     <a href="mailto:fma@top-logic.com">fma</a>
 */
public class CreatorRunner extends Thread implements ReportQueueConstants {
	
    private ReportCreator creator;
	private File workDir;
	private File openDir;
	
	private boolean stopped;

    public CreatorRunner(String someName) {
        super(someName);
        this.setDaemon(true);
        initialize();
        stopped = false;
        this.start();
    }
    /**
     * set the thread to daemon status
     */
    private void initialize (){
    	creator = new ReportCreator();
    	workDir = new File(WORK_PATH);
    	openDir = new File(OPEN_PATH);
    }
    
    /**
     * The run method waits ( TODO ... until a notification is send, that a job is to execute.)
     * Then the executeJobs method is called.
     * @see java.lang.Runnable#run()
     */ 
    @Override
	public void run () {
        while (!stopped) {
            try {
                sleep(100); // Workaround for #816 so you have a chance to kill the Server 
                // TODO KHA try using the timestamp of the 
                // Directory instead of listing all files
                if (anyJobsWaiting()) {
                    creator.createReport();
                } else {
                    sleep(500);
                }
            }
            catch (InterruptedException ex) { // Ok triggered by add.
                Logger.debug("Returned from sleep in ReportCreator ",
                             ex, this);
            }
        }
        Logger.info("CreatorRunner finished runnig, stop was called",this);
    }
    
    private boolean anyJobsWaiting(){
		return anyFiles(workDir) || anyFiles(openDir);
    }
    
	private boolean anyFiles(File dir) {
		File[] files = dir.listFiles();
    	return files != null && files.length > 0;
	}
    
    public void stopRunner(){
    	stopped=true;
    }
}
