/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.basic.col;

import java.util.Iterator;
import java.util.TreeSet;

/**
 * This class watches a set of {@link LRU}s. 
 * 
 * It initiates the removal of all expired Entries. 
 * The LRU must register itself giving an interval
 * of time to initiate the removal.
 * 
 * @author  <a href="mailto:tsa@top-logic.com">Theo Sattler</a>
 */
public class LRUWatcher implements Runnable {

    /** The singelton LRUMapWatcher. */
    private static LRUWatcher singleton; 

	/** Keeps the watched LRUs. */
	protected final TreeSet<Entry> lrus;

	/** The Watcher Thread, will be recreated as needed. */
    protected Thread thread;

	/**
	 * Non Singleton Constructor.
	 */
	public LRUWatcher() {
        this.lrus   = new TreeSet<>();
        this.thread = null;
	}

	/**
	 * expireLRUs() as long as ther is anything to schedule
	 */
	@Override
	public synchronized void run() {
		try {
			while (this.lrus.size() > 0) {
				long nowMillis = System.currentTimeMillis();
				Entry next;
				long  sleep = 0;
				next = this.lrus.first();
				
				// take the oldest entry until all entries are up to date
				if (next.time < nowMillis) {
					boolean wasRemoved = this.lrus.remove(next);
					assert  wasRemoved : "Failed to remove first element";
					LRU nextLru = next.lru;
					
					long nextTime = nextLru.removeExpired();
					if (nextTime != 0) {
						next.time = nextTime;
						this.lrus.add(next); // nothing (more) to check here
					} else {
						remove(nextLru);
					}
				}
				// Check if we have to sleep and fall asleep
				if (this.lrus.size() > 0) {
					next = this.lrus.first();
					sleep = next.time - nowMillis;
				}
				
				// Sleep until there is more to check
				if (sleep > 0) {
					wait(sleep);
				}
			}
		} catch (InterruptedException ex) {
			// Stop thread.
			lrus.clear();
		}
		
		// Stop and forget about myself
		this.thread = null;
	}

	/**
	 * Register an LRU Obejct.
     * 
     * @param aLRU	the LRU Object to be watched.
	 */
	public synchronized void register(LRU aLRU) {
		// add the map
    	// Since register is (must be) called unsynchronized with regard to
		// the LRU argument, there is the possibility of multiple calls to
		// register() without intermediate remove()s.
    	for (Iterator<Entry> it = lrus.iterator(); it.hasNext();) {
			Entry entry = it.next();
			if (entry.lru == aLRU) return;
		}
    	
    	this.lrus.add(new Entry(aLRU, aLRU.nextExpiration()));

		// wake up the watcher
		if (this.thread == null || !this.thread.isAlive()) {
			this.thread = new Thread(this, "LRUWatcher");
            this.thread.setDaemon(true);

			// thread.setPriority(Thread.MIN_PRIORITY);
            this.thread.start();
		} 
        else {
            // Wake up exiting Watcher 
            notifyAll();
		}
    }
		
	/**
	 * Remove a registered LRUMap.
	 * 
	 * <b>Must be called from a block synchronized at this.</b>
     * 
     * @return true when object was actually removed.
	 */
	private boolean remove(LRU aLRU) {
		for (Iterator<Entry> theIt = lrus.iterator(); theIt.hasNext(); ) {
            Entry theEntry = theIt.next();

			if (theEntry.lru.equals(aLRU)) {
				this.lrus.remove(theEntry);

                if (this.lrus.size() == 0 && this.thread != null) {
                    notifyAll();
                } 

				return true;
			}
		}
        return false;
	}

	/**
	 * Stores the watched {@link LRUMap} and the next time to check it.
	 */
	protected static class Entry implements Comparable<Entry> {
        
        /** the LRU to be checked every time milliseconds */
		LRU lru;
        
        /** time when entry should be checked, next */
		long time;
        
        /** Ctor for Entry */
		public Entry(LRU aLRU, long aTime) {
			this.lru  = aLRU;
			this.time = aTime;
		}

        // Default semantics of equals is just what we ant !
        /** Compare to entries absed on time */
        @Override
		public int compareTo(Entry anObject) {
            
            if (this == anObject) // shortcut equality
                return 0;
            
            long theDiff = this.time - anObject.time;

            // this is less then o
            if (theDiff < 0) {
                return -1;
            }
            if (theDiff > 0) {
                return 1;
            }
            
            // this has equal time as o, give it some consistant order
			return Integer.compare(anObject.hashCode(), this.hashCode());
        }

	}

    /**
     * Get the Singleton LRUMapWatcher.
     */
    public static LRUWatcher getLRUWatcher() {
		return Instance.INSTANCE;
	}

	private static class Instance {
		public static final LRUWatcher INSTANCE = new LRUWatcher();
    }
    

}

