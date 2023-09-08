/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.tool.dataImport;

import java.util.Date;

import com.top_logic.basic.Logger;

/**
 * The DummyDataImporter imports nothing, just kills time.
 *
 * @author <a href="mailto:Christian.Braun@top-logic.com">Christian Braun</a>
 */
public class DummyDataImporter extends AbstractDataImporter {

    public static final DummyDataImporter INSTANCE = new DummyDataImporter();

    /** Stores the result of the parsing. */
    public Double parseResult;


    /**
     * Singleton constructor.
     */
    private DummyDataImporter() {}



    @Override
	protected void doPrepareImport() {
        // I don't feel like doing something today.
        // Lets say all is OK.
        log.info("boring!");
        log.warning("to many to do...");
//        log.logError("something is wrong here.");
        log.getResult().setValue(RESULT_IMPORT_DATE, new Date());
        log.getResult().setValue(RESULT_IMPORT_DATA_NEWER, Boolean.valueOf(true));
    }



    @Override
	protected void doParseImport() {
        progress.setExpected(20);
        progress.setCurrent(0);
        for (int i = 1; i < 20; i++) {
            // I'm too tired to work today...
            try { Thread.sleep(1100); } catch (InterruptedException ex) { }
            progress.setCurrent(i);
            log.log("Done: " + i + " of 20.");
        }

        parseResult = Double.valueOf(Math.random());

        progress.setCurrent(20);
        log.log("Done: 20 of 20.\nAll done now. Puh, that was exhausting...");

        log.info(null, "was hard...");
        log.warning(null, "many garbage");
//        log.logError("a big error occurred.");
    }



    @Override
	protected void doCommitImport() {
        progress.setExpected(5);
        progress.setCurrent(0);

        if (parseResult.doubleValue() > 0.5) {
            log.info("thatsGreat");
        }

        for (int i = 1; i < 5; i++) {
            // I'm too tired to work today...
            try { Thread.sleep(1300); } catch (InterruptedException ex) { }
            progress.setCurrent(i);
            log.log("Done: " + i + " of 5.");
        }
        if (false) throw new NullPointerException("Hello! I'm a big and fatal error.");
        progress.setCurrent(5);
        log.log("Done: 5 of 5.\nAll done now. Puh, that was exhausting...");

        log.info(null, "was easy...");
        log.warning(null, "needed many time...");
//        log.logError("nothing worked. all broken.");
    }



    @Override
	protected void doCleanUpImporter() {
        parseResult = null;
        Logger.info("Call a cleaner do clean up for me.", this);
    }

}
