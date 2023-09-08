/*
 * SPDX-FileCopyrightText: 2013 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.composite;

import java.util.List;

import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;



/**
 * A {@link Task} that consists of a {@link List} of child {@link Task}s.
 * 
 * <p>
 * The child tasks should get no {@link SchedulingAlgorithm}, as they are never scheduled.
 * </p>
 * 
 * <p>
 * The list of children is not allowed to change, as that would cause inconsistencies in the
 * {@link Scheduler}. See all usages of {@link CompositeTask} and {@link CompositeTaskUtil} in the
 * {@link Scheduler} for details.
 * </p>
 * 
 * @see CompositeTaskUtil
 * 
 * @author <a href="mailto:Jan Stolzenburg@top-logic.com">Jan Stolzenburg</a>
 */
public interface CompositeTask extends Task {

	/**
	 * A new {@link List} with the child {@link Task}s of this one.
	 * 
	 * @return Never <code>null</code>
	 */
	public List<Task> getChildren();

	/**
	 * In addition to what's already described in {@link Task#signalStop()}, this method has to call
	 * {@link Task#signalStop()} on it's currently active child to stop it.
	 */
	@Override
	public boolean signalStop();

	/**
	 * In addition to what's already described in {@link Task#signalShutdown()}, this method has to
	 * call {@link Task#signalShutdown()} on it's currently active child.
	 */
	@Override
	public void signalShutdown();

	/**
	 * In addition to what's already described in {@link Task#isPersistent()}, this method has to
	 * return <code>true</code>, if at least one of the {@link #getChildren() children} returns
	 * <code>true</code> for this method. It is free though to return <code>true</code>, even if all
	 * the children return <code>false</code>. (A {@link CompositeTask} {@link #isPersistent()} if
	 * at least one of its children is it.)
	 */
	@Override
	public boolean isPersistent();

	/**
	 * In addition to what's already described in {@link Task#isNodeLocal()}, this method has to
	 * return <code>false</code>, if at least one of the {@link #getChildren() children} returns
	 * <code>false</code> for this method. It is free though to return <code>false</code>, even if
	 * all the children return <code>true</code>. (A {@link CompositeTask} is only
	 * {@link #isNodeLocal()} if all of its children are.)
	 */
	@Override
	public boolean isNodeLocal();

	/**
	 * In addition to what's already described in {@link Task#needsMaintenanceMode()}, this method
	 * has to return <code>true</code>, if at least one of the {@link #getChildren() children}
	 * returns <code>true</code> for this method. It is free though to return <code>true</code>,
	 * even if all the children return <code>false</code>. (A {@link CompositeTask}
	 * {@link #needsMaintenanceMode()} if at least one of its children needs it.)
	 */
	@Override
	public boolean needsMaintenanceMode();

	/**
	 * In addition to what's already described in {@link Task#isMaintenanceModeSafe()}, this method
	 * has to return <code>true</code>, if this {@link CompositeTask} itself is configured as
	 * maintenance mode safe, or all of its children return <code>true</code>. But it is also free
	 * to ignore its children and return <code>false</code> anyway.
	 */
	@Override
	public boolean isMaintenanceModeSafe();

}
