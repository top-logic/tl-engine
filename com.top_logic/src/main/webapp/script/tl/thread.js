/*
 * Thread and lock library for JavaScript.
 *
 * For more background information on the JavaScript synchronization issue, see:
 *    http://www.onjava.com/pub/a/onjava/2006/04/05/ajax-mutual-exclusion.html?page=3
 *    http://taylor-hughes.com/?entry=112
 *    http://ajaxian.com/archives/ajax-from-scratch-implementing-mutual-exclusion-in-javascript
 *
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */

/**
 * Thread class.
 *
 * <p>
 * A thread is realized by a repeatedly invoked function. Each invocation of the 
 * thread's run function corresponds to one time slot of the thread. The thread 
 * may store state between consecutive invocations to the run function in the 
 * thread object. The thread ends, after the threads stop() method has been 
 * called and the last invocation of the run function returns. 
 * </p>
 */
function Thread(run) {
	this.run = run;
	this.stopped = false;
	this.delay = 100;
	
	this._timer = null;

	var current = this;
	this._nextClosure = function() {
		current.next();
	}
}

Thread.prototype = {
	start: function() {
		// Schedule next time slice for immediate execution.
		this._schedule(1);
	},
	
	stop: function() {
		this.stopped = true;
	},
	
	next: function() {
		// Release the timer.
		this._timer = null;
		
		if (! this.stopped) {
			this.run();
		}
		
		this._schedule(this.delay);
	},
	
	_schedule: function(delay) {
		// Check again, because the state may have changed.
		if (! this.stopped) {
			this._timer = window.setTimeout(this._nextClosure, delay);
		}
	}
}


/**
 * Lock class.
 * 
 * <p>
 * A lock can be acquired and released. A lock can be acquired by at least one actor at 
 * a time. Calling acquire() on a lock that is already owned by another actor returns 
 * <code>false</code>. An actor is identified by a string that is passed to the acquire() 
 * and release() methods. Calls to acquire() and release() must occur pairwise. Acquiring 
 * a lock multiple times by the same actor is supported.
 * </p>
 */
function Lock() {
	this.keys = {}
}

Lock.prototype = {
	acquire: function(id) {
		if (id in this.keys) {
			// Reenter lock.
			this.keys[id]++;
			return true;
		}
		
		// Announce interest in lock.
		this.keys[id] = 1;
		
		if (this.size() > 1) {
			// Concurrent attempt to acquire, or locked by other party.
			delete this.keys[id];
			return false;
		}
		
		// Lock has been acquired.
		return true;
	},
	
	release: function(id) {
		if (! (id in this.keys)) {
			throw new Error("IllegalMonitorStateException: Lock not acquired.");
		}

		this.keys[id]--;
		if (this.keys[id] == 0) {
			// Release lock.
			delete this.keys[id];
			return true;
		} else {
			return false;
		}
	},
	
	depth: function(id) {
		if (! (id in this.keys)) {
			throw new Error("IllegalMonitorStateException: Not lock owner.");
		}
		
		return this.keys[id];
	},
	
	size: function() {
		var result = 0;
		for (var key in this.keys) {
			result++;
		}
		return result;
	}
}
