
/*
 * SPDX-FileCopyrightText: 2000 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.base.monitor.bus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.Date;

import com.top_logic.base.bus.MonitorEvent;
import com.top_logic.base.user.UserInterface;
import com.top_logic.basic.FileManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.config.ConfiguredInstance;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.defaults.IntDefault;
import com.top_logic.event.bus.AbstractReceiver;
import com.top_logic.event.bus.BusEvent;



/**
 * A Receiver that logs its Events to a File.
 *
 * Processes MonitorEvents only. Writes to logfile which can be
 * configured in TopLogic.xml. If the Logifile is not available
 * Stdout is used.
 *
 * @author    <a href="mailto:mga@top-logic.com">Michael G&auml;nsler</a>
 */
public abstract class DefaultMonitor extends AbstractReceiver implements ConfiguredInstance<DefaultMonitor.Config> {

    /** 
    * the Name of the entry which defines the filename
    */
    private static final String OUT_FILE        = "output";

	/**
	 * default number of events to be stored/cached by this buffer.
	 */
	private static final int DEFAULT_EVENT_CACHE_SIZE = 10;

    /** The delimiter between different values in the output. */
    private static final char DELIMITER = ';';

    /** The file we write (and eventually read) from. */
    protected File output;

	private Config _config;

	/**
	 * Configuration for {@link DefaultMonitor}.
	 * 
	 * @author <a href="mailto:sfo@top-logic.com">Sven Förster</a>
	 */
	public interface Config extends AbstractReceiver.Config {
		/**
		 * The file we write from.
		 */
		String getOutputFile();

		/**
		 * Number of events to store.
		 */
		@IntDefault(DEFAULT_EVENT_CACHE_SIZE)
		int getNumberEventsToStore();
	}

	/**
	 * @param context
	 *        {@link InstantiationContext} context to instantiate sub configurations.
	 * @param config
	 *        Configuration for {@link DefaultMonitor}.
	 */
	public DefaultMonitor(InstantiationContext context, Config config) {
		super(context, config);

		_config = config;
	}

    /**
     * Receive the events and processes this to the writer.
     *
     * @param    aBusEvent    The event to be processed.
     */
    @Override
	public final void receive (BusEvent aBusEvent) {
        if (aBusEvent instanceof MonitorEvent) {
            this.process ((MonitorEvent) aBusEvent);
        }
    }

    protected abstract void process (MonitorEvent anEvent);

    /**
     * Returns the input stream to be used for writing messages.
     *
     * @return    The input stream to be used.
     * @throws    java.io.FileNotFoundException  If there is no file wit  the requested name.
     */
    protected Writer createWriter() throws IOException {
        
        File theFile = this.getFile ();
        if (theFile == null) {
            return new OutputStreamWriter(System.out);
        }
        // else
        return new FileWriter (theFile);
    }

    /**
     * Returns the input stream to be used for writing messages.
     *
     * @return    The input stream to be used.
     * @throws    java.io.FileNotFoundException  If there is no file with the requested name.
     */
    protected Reader createReader() throws IOException {
        
        File theFile = this.getFile ();
        if (theFile == null) {
            return new InputStreamReader(System.in);
        }
        // else
        return new FileReader (theFile);
    }

    /**
     * Returns the name of the file to be used for writing messages.
     *
     * @return    The name of the file to be used.
     */
    public File getFile() {
        if (output == null) {
            try {
				String theFileName = _config.getOutputFile();
                if (theFileName == null)
                    throw new NullPointerException("Check Configuration for "
                        + this.getClass() + ' ' + OUT_FILE);
                output =  FileManager.getInstance()
                    .getIDEFile(theFileName);
                if (!output.exists())
                    output.createNewFile();
            }
            catch (Exception e) {
                Logger.error("Failed to getFile()", e, this);
            }
        }
        return output;
    }

    /**
     * Returns the events and processes this to the writer.
     *
     * @param    anEvent    The event to be processed.
     * @return   The formatted event.
     */
    protected String formatEvent (BusEvent anEvent) {
        String theDate;
        String theResult;

        if (anEvent instanceof MonitorEvent) {
            MonitorEvent theEvent  = (MonitorEvent) anEvent;
            UserInterface user = theEvent.getUser ();
			String userName;
			if (user != null) {
				userName = user.getFullName();
			} else {
				userName = "<no-user>";
			}
            String       theType   = theEvent.getType();
            String       theObject = theEvent.getMessage ().toString ();
            Object       theSourceObject = theEvent.getSourceObject();
            String       theSource = "";
            if (theSourceObject != null) {
                theSource = theSourceObject.toString ();
            }

            theDate   = theEvent.getFormattedDate ();
			theResult = theType + DELIMITER + userName + DELIMITER +
                        theObject + DELIMITER + theSource;
        }
        else {
            theDate   = MonitorEvent.getFormattedDate (new Date ());
            theResult = anEvent.toString ();
        }

        return (theDate + DELIMITER + theResult);
    }

    /**
     * Method to read MonitorEvent from BufferedReader.
     * 
     * @param aReader reader to read from
     * @return event of a kind supported by the extended class
     */     
    public MonitorEvent readEvent(BufferedReader aReader) throws IOException { 
        throw new UnsupportedOperationException("method not yet implemented");
	}

	@Override
	public Config getConfig() {
		return _config;
	}
}
