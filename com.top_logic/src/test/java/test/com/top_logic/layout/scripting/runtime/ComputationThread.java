/*
 * SPDX-FileCopyrightText: 2009 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package test.com.top_logic.layout.scripting.runtime;

import com.top_logic.basic.Logger;
import com.top_logic.basic.util.Computation;
import com.top_logic.util.TLContext;

/**
 * {@link Thread} that can execute multiple {@link Computation}s.
 * 
 * @author <a href="mailto:bhu@top-logic.com">Bernhard Haumacher</a>
 */
public class ComputationThread extends Thread {

	/**
	 * Whether there is pending {@link #computation} whose processing has not
	 * been started.
	 */
	private boolean hasComputation;

	/**
	 * Whether {@link #computationResult} or {@link #problem} is set.
	 */
	private boolean hasResult;
	
	/**
	 * The compuation that will be executed next. 
	 */
	private Computation computation;
	
	/**
	 * The result of the last {@link #computation}.
	 */
	private Object computationResult;
	
	/**
	 * @see #isIdle()
	 */
	private boolean idle = true;

	/**
	 * Declares that the last computation terminated abnormally.
	 * 
	 * <p>
	 * If {@link #hasProblem} is <code>true</code>, {@link #problem} is set.
	 * </p>
	 */
	private boolean hasProblem;

	/**
	 * {@link RuntimeException} or {@link Error} that occurred during the last
	 * computation.
	 */
	private Throwable problem;

	private boolean terminated;

	/**
	 * Whether thread has performed its {@link #compute(Computation)
	 * computation}.
	 * 
	 * <p>
	 * Note: The result of the computation may not yet have been fetched.
	 * </p>
	 */
	public synchronized boolean isIdle() {
		return this.idle;
	}

	/**
	 * Execute the given {@link Computation} in this thread.
	 * 
	 * @param computation
	 *        the computation to execute.
	 * @return The result computed by the given computation.
	 * @throws InterruptedException
	 *         If the calling thread gets interrupted before the result is
	 *         fetched.
	 */
	public synchronized Object compute(Computation computation) throws InterruptedException {
		enqueueComputation(computation);
		return nextResult();
	}

	private void enqueueComputation(Computation computation) throws InterruptedException {
		while (this.hasComputation) {
			this.wait();
		}
		
		this.computation = computation;
		this.hasComputation = true;
		
		this.notifyAll();
	}
	
	private Object nextResult() throws InterruptedException {
		while (! this.hasResult) {
			wait();
		}

		if (hasProblem) {
			Throwable currentProblem = problem;
			
			this.problem = null;
			this.hasResult = false;
			this.hasProblem = false;
			
			this.notifyAll();
			
			addRequestStack:
			{
				Throwable lastCause = currentProblem;
				Throwable nextCause;
				while ((nextCause = lastCause.getCause()) != null) {
					if (nextCause == lastCause) {
						// Cyclic cause.
						break addRequestStack;
					}
					lastCause = nextCause;
				}
				Exception stacktraceOfRequest = new Exception("Stack trace of request.");
				try {
					lastCause.initCause(stacktraceOfRequest);
				} catch (IllegalStateException ex) {
					// This happens, if the original exception is created with
					// giving explicitly "null" as cause in the constructor. The
					// getCause() returns null, but initCause() fails.
					Logger.error("Could not append request stack trace to problem: " + currentProblem.getMessage(), stacktraceOfRequest, ComputationThread.class);
				}
			}
			
			if (currentProblem instanceof RuntimeException) {
				throw (RuntimeException) currentProblem;
			}
			else if (currentProblem instanceof Error) {
				throw (Error) currentProblem;
			}
			else {
				throw (AssertionError) new AssertionError("Only non-declared exceptions expected").initCause(currentProblem);
			}
		} else {
			Object result = this.computationResult;
			
			this.computationResult = null;
			this.hasResult = false;
			
			this.notifyAll();
			
			return result;
		}
	}
	
	/**
	 * Executes {@link Computation}s pushed in {@link #compute(Computation)}.
	 * 
	 * @see java.lang.Thread#run()
	 */
	@Override
	public void run() {
		try {
			while (true) {
				Computation currentComputation = nextComputation();

				Throwable computationProblem = null;
				try {
					assert TLContext.getContext() == null : "Left over thread context.";
					
					Object currentResult = currentComputation.run();
					
					enqueueResult(currentResult);
				} catch (RuntimeException ex) {
					computationProblem = ex;
				} catch (Error ex) {
					computationProblem = ex;
				}
				
				TLContext danglingContext = TLContext.getContext();
				if (danglingContext != null) {
					if (computationProblem == null) {
						computationProblem = new Exception("Action did not remove its context.");
					}
				}
				
				if (computationProblem != null) {
					enqueueProblem(computationProblem);
				}
			}
		} catch (InterruptedException ex) {
			Logger.info("Pooled thread was terminated.", ComputationThread.class);
		} finally {
			synchronized (this) {
				this.terminated = true;
				this.notifyAll();
			}
		}
	}

	private synchronized void enqueueProblem(Throwable ex) throws InterruptedException {
		while (this.hasResult) {
			wait();
		}
		
		this.problem = ex;
		this.hasResult = true;
		this.hasProblem = true;
		this.idle = true;
		
		this.notifyAll();
	}

	private synchronized void enqueueResult(Object currentResult) throws InterruptedException {
		while (this.hasResult) {
			wait();
		}
		
		this.computationResult = currentResult;
		this.hasResult = true;
		this.hasProblem = false;
		this.idle = true;
		
		this.notifyAll();
	}

	private synchronized Computation nextComputation() throws InterruptedException {
		while (! this.hasComputation) {
			this.wait();
		}
		
		Computation currentComputation = this.computation;
		
		this.computation = null;
		this.hasComputation = false;
		this.idle = false;
		
		this.notifyAll();
		
		return currentComputation;
	}

}
