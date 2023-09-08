/*
 * SPDX-FileCopyrightText: 2008 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched;

import static com.top_logic.basic.DateUtil.*;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Queue;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Second;
import org.jfree.data.time.TimeSeriesCollection;

import com.top_logic.base.chart.dataset.ExtendedTimeSeries;
import com.top_logic.base.monitor.bus.EventBuffer;
import com.top_logic.basic.AliasManager;
import com.top_logic.basic.Logger;
import com.top_logic.basic.Reloadable;
import com.top_logic.basic.ReloadableManager;
import com.top_logic.basic.config.CommaSeparatedStrings;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.annotation.Format;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.FormattedDefault;
import com.top_logic.basic.config.annotation.defaults.LongDefault;
import com.top_logic.basic.module.ConfiguredManagedClass;
import com.top_logic.basic.module.ManagedClass;
import com.top_logic.basic.module.ServiceDependencies;
import com.top_logic.basic.module.TypedRuntimeModule;
import com.top_logic.basic.time.TimeZones;
import com.top_logic.layout.Accessor;
import com.top_logic.util.Resources;
import com.top_logic.util.TLContext;
import com.top_logic.util.monitor.ApplicationMonitor;
import com.top_logic.util.monitor.MonitorComponent;
import com.top_logic.util.monitor.MonitorMessage;
import com.top_logic.util.monitor.MonitorResult;

/**
 * {@link ManagedClass} to log memory usage.
 * 
 * <p>
 * In the default configuration a {@link MemoryUsageEntry} is created each
 * {@link Config#getLoggingInterval()} milliseconds and buffered for
 * {@link Config#getLoggingPeriod()} milliseconds.
 * </p>
 * 
 * <p>
 * A {@link Config#getLoggingPeriod()} of <code>0</code> disables buffering.
 * </p>
 * 
 * <p>
 * If {@link Config#getLoggingAttributes()} are specified, these attributes of a
 * {@link MemoryUsageEntry} is logged to the {@link Logger}.
 * </p>
 * 
 * <p>
 * If {@link Config#getCreateReport()} is set a graphic report of used memory is created
 * periodically.
 * </p>
 *
 * @author <a href="mailto:fsc@top-logic.com">fsc</a>
 */
@ServiceDependencies({ AliasManager.Module.class, ApplicationMonitor.Module.class })
public class MemoryObserverThread extends ConfiguredManagedClass<MemoryObserverThread.Config>
		implements MonitorComponent, Reloadable {
	
	/**
	 * Configuration for {@link MemoryObserverThread}.
	 */
	public interface Config extends ConfiguredManagedClass.Config<MemoryObserverThread> {

		/**
		 * Whether to create a report image (PNG). See {@link Config#getCreateReport}.
		 */
		String CREATE_REPORT = "createReport";

		/**
		 * Log these attributes. See {@link Config#getLoggingAttributes}.
		 */
		String LOGGING_ATTRIBUTES = "loggingAttributes";

		/**
		 * Logging interval of memory usage in milliseconds. See {@link Config#getLoggingInterval}.
		 */
		String LOGGING_INTERVAL = "loggingInterval";

		/**
		 * Logging period of memory usage in milliseconds. Internally buffered, used for GUI and reports.
		 * See {@link Config#getLoggingPeriod}.
		 */
		String LOGGING_PERIOD = "loggingPeriod";

		/**
		 * Name of the file where to store the report. See {@link Config#getReportFile}.
		 */
		String REPORT_FILE = "reportFile";

		/**
		 * Report these attributes. See {@link Config#getReportAttributes}.
		 */
		String REPORT_ATTRIBUTES = "reportAttributes";

		/**
		 * Reporting interval of memory usage in milliseconds. See {@link Config#getReportInterval}.
		 */
		String REPORT_INTERVAL = "reportInterval";

		/**
		 * Reporting period of memory usage in milliseconds. Restricted to {@link Config#LOGGING_PERIOD}.
		 * See {@link Config#getReportPeriod}.
		 */
		String REPORT_PERIOD = "reportPeriod";

		/** Getter for {@link Config#CREATE_REPORT}. */
		@Name(CREATE_REPORT)
		boolean getCreateReport();

		/** Getter for {@link Config#LOGGING_ATTRIBUTES}. */
		@Name(LOGGING_ATTRIBUTES)
		@FormattedDefault("used,total")
		@Format(CommaSeparatedStrings.class)
		List<String> getLoggingAttributes();

		/** Getter for {@link Config#LOGGING_INTERVAL}. */
		@Name(LOGGING_INTERVAL)
		@LongDefault(60000) // 1 minute
		long getLoggingInterval();

		/** Getter for {@link Config#LOGGING_PERIOD}. */
		@Name(LOGGING_PERIOD)
		@LongDefault(86400000) // 24 hours
		long getLoggingPeriod();

		/** Getter for {@link Config#REPORT_FILE}. */
		@Name(REPORT_FILE)
		@FormattedDefault("%LOG_PATH%/memory_usage.png")
		String getReportFile();

		/** Getter for {@link Config#REPORT_ATTRIBUTES}. */
		@Name(REPORT_ATTRIBUTES)
		@FormattedDefault("used,total")
		@Format(CommaSeparatedStrings.class)
		List<String> getReportAttributes();

		/** Getter for {@link Config#REPORT_INTERVAL}. */
		@Name(REPORT_INTERVAL)
		/** 5*{@link Config#LOGGING_INTERVAL} = 5 minutes */
		@LongDefault(300000)
		long getReportInterval();

		/** Getter for {@link Config#REPORT_PERIOD}. */
		@Name(REPORT_PERIOD)
		@LongDefault(21600000) // 6 hours
		long getReportPeriod();
	}

	private static final String SEPARATOR = ",";

	/** Indicates that the thread is to be terminated */
    private boolean terminate = false;
    
	/** buffer for the MemoryUsageEntries */
	ObjectBuffer<MemoryUsageEntry> buffer;
    
	/** thread for generating a JFreeChart report */
	MemoryReportThread reporterThread;
    private final ObserverThread observerThread;

    /**
	 * Creates a {@link MemoryObserverThread}.
	 */
	public MemoryObserverThread(InstantiationContext context, Config config) {
		super(context, config);
		observerThread = new ObserverThread();
        this.reload();
		observerThread.start();
    }
    
    /**
     * @see com.top_logic.basic.Reloadable#reload()
     */
    @Override
	public synchronized boolean reload() {
		this.stopLogging();

		// stop running reporter
		if (this.reporterThread != null) {
			this.reporterThread.kill();
			this.reporterThread = null;
		}

		// copy old buffer
		ObjectBuffer<MemoryUsageEntry> theOldEntries = this.buffer;
		long loggingPeriod = getLoggingPeriod();
		if (loggingPeriod > 0) {
			this.buffer = new ObjectBuffer<>((int) (loggingPeriod / getLoggingInterval()));
			if (theOldEntries != null) {
				this.buffer.add(theOldEntries);
			}
		}

		boolean createReport = getConfig().getCreateReport();
		if (createReport && this.buffer == null) {
			throw new IllegalArgumentException("createReport only works with loggingPeriod > 0");
		}
		if (createReport) {
			this.reporterThread = new MemoryReportThread();
			this.reporterThread.start();
		}

		this.startLogging();
		return true;
    }
    
    /**
     * Create a new MemoryUsageEntry
     */
	MemoryUsageEntry createEntry() {
        Runtime theRT = Runtime.getRuntime();
        return new MemoryUsageEntry(System.currentTimeMillis(), theRT.maxMemory(), theRT.totalMemory(), theRT.freeMemory());
    }

	/**
	 * Write a theEntry if any {@link Config#LOGGING_ATTRIBUTES} are configured
	 */
	void log(MemoryUsageEntry theEntry) {
		List<String> loggingAttributes = getConfig().getLoggingAttributes();
		if (!loggingAttributes.isEmpty()) {
            StringBuffer theMessage = new StringBuffer(92);
			theMessage.append(getIso8601DateFormat().format(theEntry.getDate())).append(SEPARATOR);
			for (String attribute : loggingAttributes) {
				theMessage.append(MemoryUsageEntryAccessor.INSTANCE.getValue(theEntry, attribute));
                theMessage.append(SEPARATOR);
            }
    
            Logger.info(theMessage.toString(), this);
        }
    }
    
    /**
     * Return true of logging and reporting is activated
     */
    public synchronized boolean isLogging() {
        return ! this.terminate;
    }
    
    /**
     * Start all log and report activities
     */
    public synchronized void stopLogging() {
        this.terminate = true;
        notifyAll();
    }
    
    /**
     * Stop all log and report activities 
     */
    public synchronized void startLogging() {
        this.terminate = false;
        notifyAll();
    }
    
    /**
     * Return a List of all buffered {@link MemoryUsageEntry}s
     * The list is ordered by the creation date of the entries, that means
     * the first element is the newest.  
     */
    public synchronized List<? extends MemoryUsageEntry> getEntries() {
        if (this.buffer != null) {
            return this.buffer.getAll();
        }
        return Collections.emptyList();
    }

    /**
     * Return a List of the last maxreturns buffered {@link MemoryUsageEntry}s
     * The list is ordered by the creation date of the entries, that means
     * the first element is the newest.  
     */
    public synchronized List<MemoryUsageEntry> getLatestEntries(int maxreturns) {
        if (this.buffer != null) {
            return this.buffer.getLatestEntries(maxreturns);
        }
        return Collections.emptyList();
    }
    
	@Override
	public void checkState(MonitorResult result) {
		result.addMessage(newMonitorMessage());
	}

	private MonitorMessage newMonitorMessage() {
		List<MemoryUsageEntry> theLatest = this.getLatestEntries(1);
        if (! theLatest.isEmpty()) {
            MemoryUsageEntry theEntry   = theLatest.get(0);
            return new MonitorMessage(MonitorMessage.Status.INFO, "Free: " + theEntry.getFree() + " Used: " + theEntry.getUsed(), this);
        }
        return new MonitorMessage(MonitorMessage.Status.INFO, "No info", this);
	}

    @Override
	public String getDescription() {
        return "Overview over memory usage.";
    }

	@Override
	public String getName() {
		return observerThread.getName();
	}

    @Override
	public boolean usesXMLProperties() {
        return true;
    }

	/**
	 * Create a new MemoryUsageEntry every {@link Config#LOGGING_INTERVAL} when {@link #isLogging()}
	 * 
	 * @author <a href=mailto:daniel.busche@top-logic.com>Daniel Busche</a>
	 */
    private final class ObserverThread extends Thread {
    	
		private static final String NAME = "TL-MemoryObserver";

    	private boolean killed = false;
    	
		ObserverThread() {
			super(NAME);
			setDaemon(true);
		}

	    /**
	     * Overridden to inform reporter thread
	     * @see java.lang.Thread#start()
	     */
	    @Override
		public synchronized void start() {
	        if (reporterThread != null) {
	            if (! reporterThread.isAlive()) {
	                reporterThread.start();
	            }
	        }
	        super.start();
	    }
	    
	    /**
	     * Overridden to inform reporter thread 
	     * @see java.lang.Thread#interrupt()
	     */
	    @Override
		public synchronized void interrupt() {
	        if (reporterThread != null) {
	            reporterThread.interrupt();
	        }
	        super.interrupt();
	    }

		@Override
		public void run() {
	        while (true) {
	            try {
	                if (isLogging()) {
	                    MemoryUsageEntry theEntry = createEntry();
	                    
	                    log(theEntry);
	                    
	                    synchronized (MemoryObserverThread.this) {
	                        /*
	                         * Add an entry to the internal buffer
	                         */
		                    if (buffer != null) {
							    buffer.add(theEntry);
							}
	                    }
	                }
	                synchronized (MemoryObserverThread.this) {
						MemoryObserverThread.this.wait(getLoggingInterval());
	                	if (killed) {
	                		break;
	                	}
					}
	            } 
	            catch (InterruptedException iex) {
	                Logger.error("Unable to run", iex, this);
	            }
	        }
	    }
		
		void kill() {
			synchronized(MemoryObserverThread.this) {
				killed = true;
				if (reporterThread != null) {
					reporterThread.kill();
				}
				MemoryObserverThread.this.notifyAll();
			}
			
		}
	    
}

	/**
     * The MemoryReportThread generates a jfreechart report of {@link MemoryUsageEntry}s
     * and writes this chart to a preconfigured {@link #reportFile}.
     * 
     * The Report will be generated each {@link #reportInterval} milliseconds and will
     * show the {@link #reportAttributes} of the memory usage of the last "reportPeriod"
     * milliseconds.
     * 
     * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
     */
    private class MemoryReportThread extends Thread {
        
        private static final String NAME = "TL-MemoryReporter";
        
        /** file of the report */
        private String   reportFile;
        /** reported attributes of {@link MemoryUsageEntryAccessor} */
        private final List<String>     reportAttributes;
        private final long     reportInterval;
        private int      entryCount;
        private boolean  killed;
        
		MemoryReportThread() {
            super(NAME);
			setDaemon(true);
			Config config = getConfig();
			reportFile = config.getReportFile();
			reportAttributes = config.getReportAttributes();
			reportInterval = config.getReportInterval();
			long reportPeriod = config.getReportPeriod();
			entryCount = (int) (reportPeriod / reportInterval);
			if (entryCount > buffer.getSize()) {
				entryCount = buffer.getSize();
			}
        }
        
        @Override
		public void run() {
            while (true) {
                try {
                    if (isLogging()) {
                        this.writeChart();
                        System.gc();
                    }
                    Thread.sleep(this.reportInterval);
                    synchronized(MemoryObserverThread.this) {
	                    if (killed) {
	                    	break;
	                    }
                    }
                } 
                catch (InterruptedException iex) {
					Logger.error("Unextepected InterruptedException", iex, MemoryReportThread.class);
                }
            }
        }
        
        /**
         * Get a chart that show the memory usage of the last report period
         */
        private JFreeChart createChart() {
            return createMemoryUsageChart(getLatestEntries(this.entryCount), this.reportAttributes);
        }
        
        /**
         * Write the report to the configured file
         */
        private void writeChart() {
            try {
				File file = new File(this.reportFile);
				File dir = file.getParentFile();
				if (!dir.exists()) {
					boolean ok = dir.mkdirs();
					if (!ok) {
						Logger.error(
							"Unable to create directory '" + dir.getAbsolutePath() + "' for memory usage chart.",
							MemoryObserverThread.class);

					}
				}
				ChartUtils.writeChartAsPNG(new FileOutputStream(file), this.createChart(), 1024, 768);
            }
            catch (Exception ex) {
                Logger.error("Unable to write memory usage chart." + ex, this);
            }
        }
        
		void kill() {
			synchronized(MemoryObserverThread.this) {
				this.killed = true;
			}
        }
    }
    
    /**
     * Create a Linechart of someMemoryUsageEntries with someAttributes as Y-values
     */
    public static JFreeChart createMemoryUsageChart(List<? extends MemoryUsageEntry> someMemoryUsageEntries, List<?> someAttributes) {
		TimeSeriesCollection theTSC = new TimeSeriesCollection(TimeZones.systemTimeZone());
        for (int i=0; i<someAttributes.size(); i++) {
            addTimeSeries(theTSC, String.valueOf(someAttributes.get(i)), someMemoryUsageEntries);
        }
        
        Resources theRes = Resources.getInstance();
		return ChartFactory.createTimeSeriesChart(
			theRes.getString(I18NConstants.MEMORY_CHART_TITLE),
			theRes.getString(I18NConstants.MEMORY_CHART_AXIS_TIME),
			theRes.getString(I18NConstants.MEMORY_CHART_AXIS_VALUE), theTSC, true, false, false);
    }
    
    private static void addTimeSeries(TimeSeriesCollection aColl, String anAttribute, List<? extends MemoryUsageEntry> someEntries) {
		ExtendedTimeSeries theSeries =
			new ExtendedTimeSeries(Resources.getInstance().getString(
				I18NConstants.MEMORY_OBSERVER.key(anAttribute)),
				Second.class);
        for (MemoryUsageEntry theEntry : someEntries) {
			theSeries.add(new Second(theEntry.getDate(), TimeZones.systemTimeZone(), TLContext.getLocale()),
				((Number) MemoryUsageEntryAccessor.INSTANCE.getValue(theEntry, anAttribute)).doubleValue());
        }
        aColl.addSeries(theSeries);
    }
    
    /**
     * The MemoryUsageEntry holds memory usage data 
     * 
     * @author    <a href=mailto:fsc@top-logic.com>fsc</a>
     */
    public static final class MemoryUsageEntry {
        
		private Date _time;

		private long _max;

		private long _total;

		private long _free;

		/**
		 * Creates a {@link MemoryUsageEntry}.
		 *
		 * @param aTime
		 *        See {@link #getDate()}.
		 * @param aMax
		 *        See {@link #getMax()}.
		 * @param aTotal
		 *        See {@link #getTotal()}.
		 * @param aFree
		 *        See {@link #getFree()}.
		 */
        public MemoryUsageEntry(long aTime, long aMax, long aTotal, long aFree) {
			_time = new Date(aTime);
			_max = aMax;
			_total = aTotal;
			_free = aFree;
        }
        
		/**
		 * Time when event was recorded.
		 */
		public Date getDate() {
			return _time;
		}

        /**
		 * Number of bytes currently available for future object allocations without enlarging the
		 * VM heap.
		 * 
		 * @see Runtime#freeMemory()
		 */
		public long getFree() {
			return _free;
        }

		/**
		 * Maximum number of bytes available to the VM.
		 * 
		 * @see Runtime#maxMemory()
		 */
		public long getMax() {
			return _max;
        }
        
        /**
		 * Number of bytes reserved by the VM.
		 * 
		 * @see Runtime#totalMemory()
		 */
		public long getTotal() {
			return _total;
        }
        
        /**
		 * Number of bytes still available for increasing the VM heap.
		 */
		public long getAvailable() {
			return getMax() - getTotal();
        }
        
        /**
		 * Number of bytes currently used for currently allocated objects in the VM heap.
		 */
		public long getUsed() {
			return getTotal() - getFree();
        }
        
        @Override
		public boolean equals(Object aObj) {
			if (aObj == this) {
				return true;
			}
        	if (aObj instanceof MemoryUsageEntry) {
				return _time.equals(((MemoryUsageEntry) aObj)._time);
        	}
        	return false;
        }
        
        @Override
        public int hashCode() {
			return 17 + _time.hashCode();
        }
        
        @Override
		public String toString() {
			return getIso8601DateFormat().format(_time) + SEPARATOR +
                getUsed() + SEPARATOR +
				_free + SEPARATOR +
				_total + SEPARATOR +
                getAvailable() + SEPARATOR + 
				_max;
        }
    }
    
	public static class MemoryUsageEntryAccessor implements Accessor<MemoryUsageEntry> {
        
        public static final MemoryUsageEntryAccessor INSTANCE = new MemoryUsageEntryAccessor();
        
        public static final String DATE  = "date";

		/** Maximum number of bytes available to VM */
        public static final String MAX   = "max";

        /** Number of bytes reserved by VM */
        public static final String TOTAL = "total";

        /** Number of bytes currently available */
        public static final String FREE  = "free";

        /** Number of bytes currently used */
        public static final String USED  = "used";

        /** Number of bytes still available to VM */
        public static final String AVAIL = "avail";

		private static final double MB_BYTES = 1024 * 1024;
        
		/** Use #INSTANCE */
        public MemoryUsageEntryAccessor() {
			// Use Instance
        }
        
		@Override
		public Object getValue(MemoryUsageEntry theEntry, String aProperty) {
            
            if (DATE.equals(aProperty)) {
                return theEntry.getDate();
            }
            else if (MAX.equals(aProperty)) {
				return theEntry.getMax() / MB_BYTES;
            }
            else if (TOTAL.equals(aProperty)) {
				return theEntry.getTotal() / MB_BYTES;
            }
            else if (FREE.equals(aProperty)) {
				return theEntry.getFree() / MB_BYTES;
            }
            else if (USED.equals(aProperty)) {
				return theEntry.getUsed() / MB_BYTES;
            }
            else if (AVAIL.equals(aProperty)) {
				return theEntry.getAvailable() / MB_BYTES;
            }
            
            return null;
        }
        
		@Override
		public void setValue(MemoryUsageEntry aObject, String aProperty, Object aValue) {
            throw new UnsupportedOperationException();
        }
    }

	/**
	 * Circular Buffer for a certain number of Object for later access.
	 * 
	 * TODO KHA Use some kind of {@link Queue} or moved this into tl-basic.col.
	 * 
	 * @see EventBuffer
	 * 
	 * @author <a href=mailto:fsc@top-logic.com>fsc</a>
	 */
    public static class ObjectBuffer<E> {

        private final E[] buffer;    
        private int size;
        private int pointer = 0;    
        
        public ObjectBuffer (int aSize) {
            this.size = aSize;
            this.buffer = (E[]) new Object[this.size];
        }    

        /**
         * Returns this buffer's size.
         *
         * @return    The buffer size.
         */
        public int getSize () {
            return this.size;
        }
        
        /**
         * Appends the given object to the buffered objects.
         * 
         * If buffer is already full for this object to be appended 
         * the first object is silently removed.
         *
         * @param    anEvent    The event to be added to the buffer.
         */
        public void add (E anEvent) {
            synchronized (this.buffer) {
                this.buffer[this.pointer] = anEvent;   
                this.pointer = (this.pointer < this.size-1)?++this.pointer:0;                                            
            }   
        }    
        
        /**
         * Returns all buffered objects.
         * 
         * The first element in the returned list is the lastest added object.
         * @return   A list of Objects.
         */
        public List<E> getAll () {
            return this.getLatestEntries (this.size);
        }    

        /**
         * Returns the latest buffered objects up to given number.
         *
         * The first element in the returned list is the  lastest added object.
         * @param    maxreturns    The maximum number of objects that
         *                         should be returned.
         * @return   A list of Objects.
         */
        public List<E> getLatestEntries (int maxreturns) {
            synchronized (this.buffer) {  
                maxreturns = (maxreturns > this.size) ? this.size:maxreturns;
                maxreturns = (maxreturns < 0) ? 0 : maxreturns;             
                final List<E> theResult = new ArrayList<>(maxreturns);  
                for (int i=0; i<maxreturns; i++) {
                    E theEvent = this.buffer[(this.pointer > i)?(this.pointer - i - 1):(this.size + this.pointer - i - 1)];
                    if(theEvent != null) {
                        theResult.add(theEvent);
                    }
                }
                return theResult;
            }
        }
        
        /**
         * Adds all entries from aBuffer to this buffer.
         */
        public void add(ObjectBuffer<? extends E> aBuffer) {
            List<? extends E> theNew = aBuffer.getAll();
            synchronized (this) {
                // reverse order
                for (int i=theNew.size()-1; i>=0; i--) {
                    this.add(theNew.get(i));
                }
            }
        }
    }

	/**
	 * This method returns the logInterval.
	 * 
	 * @return Returns the logInterval.
	 */
	public long getLoggingInterval() {
		return getConfig().getLoggingInterval();
	}

	/**
	 * This method returns the logPeriod.
	 * 
	 * @return Returns the logPeriod.
	 */
	public long getLoggingPeriod() {
		return getConfig().getLoggingPeriod();
	}

	/**
	 * Getter for the single instance of this class.
	 */
    public static final MemoryObserverThread getInstance() {
    	return Module.INSTANCE.getImplementationInstance();
    }
    
	/**
	 * Module for instantiation of the {@link MemoryObserverThread}.
	 */
	public static final class Module extends TypedRuntimeModule<MemoryObserverThread> {

		/** Singleton for this module. */
		public static final Module INSTANCE = new Module();

		private Module() {
			// Singleton constructor.
		}

		@Override
		public Class<MemoryObserverThread> getImplementation() {
			return MemoryObserverThread.class;
		}
	}

	@Override
	protected void startUp() {
		super.startUp();

		ReloadableManager.getInstance().addReloadable(this);
		ApplicationMonitor.getInstance().registerMonitor(observerThread.getName(), this);
	}

	@Override
	protected void shutDown() {
		ReloadableManager.getInstance().removeReloadable(this);
		ApplicationMonitor.getInstance().unregisterMonitor(observerThread.getName());

		observerThread.kill();

		super.shutDown();
	}
    

}

