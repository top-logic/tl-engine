/*
 * SPDX-FileCopyrightText: 2004 (c) Business Operation Systems GmbH <info@top-logic.com>
 * 
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.util.sched.task.impl;

import static com.top_logic.basic.StringServices.*;
import static com.top_logic.util.sched.task.TaskCommon.*;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.top_logic.basic.CalledByReflection;
import com.top_logic.basic.ConfigurationError;
import com.top_logic.basic.ExceptionUtil;
import com.top_logic.basic.Logger;
import com.top_logic.basic.StringServices;
import com.top_logic.basic.col.Maybe;
import com.top_logic.basic.config.InstantiationContext;
import com.top_logic.basic.config.TypedConfiguration;
import com.top_logic.basic.config.annotation.Name;
import com.top_logic.basic.config.annotation.defaults.BooleanDefault;
import com.top_logic.basic.sched.BatchImpl;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.basic.util.ResKey;
import com.top_logic.basic.util.retry.Retry;
import com.top_logic.basic.util.retry.RetryResult;
import com.top_logic.knowledge.service.PersistencyLayer;
import com.top_logic.knowledge.service.Transaction;
import com.top_logic.util.Utils;
import com.top_logic.util.sched.Scheduler;
import com.top_logic.util.sched.task.Task;
import com.top_logic.util.sched.task.TaskCommon;
import com.top_logic.util.sched.task.TaskState;
import com.top_logic.util.sched.task.log.DefaultTaskLogFileFactory;
import com.top_logic.util.sched.task.log.TaskLog;
import com.top_logic.util.sched.task.log.TaskLogWrapper;
import com.top_logic.util.sched.task.log.TransientTaskLog;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithm;
import com.top_logic.util.sched.task.schedule.SchedulingAlgorithmCombinator;
import com.top_logic.util.sched.task.schedule.legacy.LegacyDailySchedule;
import com.top_logic.util.sched.task.schedule.legacy.LegacyDateSchedule;
import com.top_logic.util.sched.task.schedule.legacy.LegacyMonthlySchedule;
import com.top_logic.util.sched.task.schedule.legacy.LegacyOnceSchedule;
import com.top_logic.util.sched.task.schedule.legacy.LegacySchedulesCommon;
import com.top_logic.util.sched.task.schedule.legacy.LegacyWeeklySchedule;

/**
 * Default implementation for Task.
 * <p>
 * <b>The recommended superclass for implementing a new {@link Task} is the
 * {@link StateHandlingTask}.</b>
 * </p>
 * 
 * <p>
 * Commits in this class have to be retried in most cases, if they fail. The reason is, that the
 * different schedulers of the cluster nodes are synchronized and communicating via the
 * KnowledgeBase. Therefore, concurrent write accesses can happen. Example: A task which is not node
 * local is told to stop from a user that is logged in on a different cluster node. But at the same
 * time the task is storing a warning. If they happen "concurrently" one of them will fail and has
 * to be retried.
 * </p>
 * 
 * @author <a href="mailto:kha@top-logic.com">kha</a>
 */
public class TaskImpl<C extends TaskImpl.Config<?>> extends BatchImpl implements Task {

	/**
	 * Configuration options for {@link TaskImpl}.
	 */
	public interface Config<I extends TaskImpl<?>> extends Task.Config<I> {

		/**
		 * @see #isRunOnStartup()
		 */
		static final String RUN_ON_START_UP_PROPERTY = "run-on-startup";

		/**
		 * Whether this task can run during system startup.
		 */
		@BooleanDefault(true)
		@Name(RUN_ON_START_UP_PROPERTY)
		boolean isRunOnStartup();

	}

	private final class RunRequest {

		private final long _request;

		private final long _start;

		/**
		 * @param request
		 *        see: {@link #getRequest()}
		 * @param start
		 *        see: {@link #getStart()}
		 */
		public RunRequest(long request, Long start) {
			_request = request;
			_start = start;
		}

		/** When the request was made. */
		public long getRequest() {
			return _request;
		}

		/** The requested start time. */
		public Long getStart() {
			return _start;
		}

	}

	/**
	 * The legacy constructors don't use the typed configuration. Therefore, this "may have" a typed
	 * configuration. Therefore the usage of {@link Maybe}.
	 */
	private final Maybe<C> _typedConfig;

	private final SchedulingAlgorithm _schedulingAlgorithm;

	/**
	 * Time when task was executed the last time ({@link SchedulingAlgorithm#NO_SCHEDULE} for not
	 * yet)
	 */
    protected long   lastSched;

    /**
	 * Time when task should be executed the next time.
	 * 
	 * @see SchedulingAlgorithm#NO_SCHEDULE
	 * @see #getNextShed()
	 */
    protected long   nextShed;

	private RunRequest _runRequest;

	private final Object _runRequestLock = new Object();

	/**
	 * Tests that run {@link Task}s without the {@link Scheduler} need to call
	 * <code>TaskTestUtil.initTaskLog(Task)</code> to initialize this field.
	 */
	private TaskLog _log;

	private Scheduler _scheduler;

	private final boolean _needsMaintenanceMode;

	private final long _maintenanceModeDelay;

	private final boolean _maintenanceModeSafe;

	private final boolean _runOnStartup;

	private final boolean _blockingAllowed;

	private final boolean _blockedByDefault;

    /**
	 * Construct a new, empty Task with the given name.
	 * 
	 * Use for subclasses that will set the variables later.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use instead: {@link #TaskImpl(InstantiationContext, Config)}
	 * 
	 * @param name
	 *        Must not be <code>null</code> or empty. Has to be the name that is registered at the
	 *        {@link Scheduler}.
	 */
	@Deprecated
    protected TaskImpl(String name) {
        /* used by subclasses only */
        super(name);
		_typedConfig = Maybe.none();
		_schedulingAlgorithm = createLegacySchedule(
			/* daytype */0,
			/* date */null,
			/* daymask */0,
			/* hour */0,
			/* minute */0,
			/* interval */0,
			/* stopHour */0,
			/* stopMinute */0);
		_needsMaintenanceMode = Config.DEFAULT_NEEDS_MAINTENANCE_MODE;
		_maintenanceModeDelay = Config.DEFAULT_MAINTENANCE_MODE_DELAY;
		_maintenanceModeSafe = Config.DEFAULT_MAINTENANCE_MODE_SAFE;
		_runOnStartup = true;
		_blockingAllowed = Config.DEFAULT_BLOCKING_ALLOWED;
		_blockedByDefault = Config.DEFAULT_BLOCKED_BY_DEFAULT_VALUE;
    }

    /**
	 * Construct a new Task that will run once in a day in some intervals.
	 * 
	 * The task is designed to work inside a Scheduler otherwise it will behave more or less like a
	 * normal Runnable.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use instead: {@link #TaskImpl(InstantiationContext, Config)}
	 * 
	 * @param aName
	 *        Must not be <code>null</code> or empty. Has to be the name that is registered at the
	 *        {@link Scheduler}.
	 * @param aDaytype
	 *        one of {@link LegacySchedulesCommon#ONCE}, {@link LegacySchedulesCommon#DAILY}, {@link LegacySchedulesCommon#WEEKLY},
	 *        {@link LegacySchedulesCommon#MONTHLY}
	 * @param aDaymask
	 *        ignored for ONCE and DAILY, bit-mask of Weekdays for WEEKLY, bit-mask of days in month
	 *        for monthly.
	 * @param anHour
	 *        (0-23) when task will run (once)
	 * @param aMinute
	 *        (0-59) when task will run (once)
	 */
	@Deprecated
    public TaskImpl(String aName, int aDaytype, int aDaymask, int anHour, int aMinute) {
       super(aName);
		_typedConfig = Maybe.none();
		_schedulingAlgorithm = createLegacySchedule(
			aDaytype,
			/* date */null,
			aDaymask,
			anHour,
			aMinute,
			/* interval */0,
			/* stopHour */0,
			/* stopMinute */0);

		_needsMaintenanceMode = Config.DEFAULT_NEEDS_MAINTENANCE_MODE;
		_maintenanceModeDelay = Config.DEFAULT_MAINTENANCE_MODE_DELAY;
		_maintenanceModeSafe = Config.DEFAULT_MAINTENANCE_MODE_SAFE;
		_runOnStartup = true;
		_blockingAllowed = Config.DEFAULT_BLOCKING_ALLOWED;
		_blockedByDefault = Config.DEFAULT_BLOCKED_BY_DEFAULT_VALUE;
   }

   	/**
	 * Construct a new Task to be run at a specific date and the indicated time.
	 * 
	 * The task is designed to work inside a {@link Scheduler} otherwise it will behave more or less
	 * like a normal Thread.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use instead: {@link #TaskImpl(InstantiationContext, Config)}
	 * 
	 * @param aName
	 *        Must not be <code>null</code> or empty. Has to be the name that is registered at the
	 *        {@link Scheduler}.
	 */
	@Deprecated
   public TaskImpl(String aName, Date when, int anHour, int aMinute) {
      super(aName);
		_typedConfig = Maybe.none();
		_schedulingAlgorithm = createLegacySchedule(
			LegacySchedulesCommon.DATE,
			when,
			/* daymask */0,
			anHour,
			aMinute,
			/* interval */0,
			/* stopHour */0,
			/* stopMinute */0);

		_needsMaintenanceMode = Config.DEFAULT_NEEDS_MAINTENANCE_MODE;
		_maintenanceModeDelay = Config.DEFAULT_MAINTENANCE_MODE_DELAY;
		_maintenanceModeSafe = Config.DEFAULT_MAINTENANCE_MODE_SAFE;
		_runOnStartup = true;
		_blockingAllowed = Config.DEFAULT_BLOCKING_ALLOWED;
		_blockedByDefault = Config.DEFAULT_BLOCKED_BY_DEFAULT_VALUE;
	}

    /**
	 * Construct a new Task to be run periodically.
	 * 
	 * The task is designed to work inside a Scheduler otherwise it will behave more or less like a
	 * normal Thread.
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use instead: {@link #TaskImpl(InstantiationContext, Config)}
	 * 
	 * @param aName
	 *        Must not be <code>null</code> or empty. Has to be the name that is registered at the
	 *        {@link Scheduler}.
	 * @param aDaytype
	 *        must include PERIODICALLY bit, otherwise same as simple version.
	 * @param interval
	 *        that Task should run PERIODICALLY (in milliseconds).
	 */
	@Deprecated
    public TaskImpl( String aName,
                int  aDaytype, int aDaymask,
                int  startHour, int startMinute,
                long interval,
                int  stopHour, int stopMinute) {
        super(aName);
		_typedConfig = Maybe.none();
        if (0 == (aDaytype & LegacySchedulesCommon.PERIODICALLY)) {
            throw new IllegalArgumentException("Mut use PERIODICALLY daytype");
        }
		_schedulingAlgorithm = createLegacySchedule(
			aDaytype,
			/* date */null,
			aDaymask,
			startHour,
			startMinute,
			interval,
			stopHour,
			stopMinute);

		_needsMaintenanceMode = Config.DEFAULT_NEEDS_MAINTENANCE_MODE;
		_maintenanceModeDelay = Config.DEFAULT_MAINTENANCE_MODE_DELAY;
		_maintenanceModeSafe = Config.DEFAULT_MAINTENANCE_MODE_SAFE;
		_runOnStartup = true;
		_blockingAllowed = Config.DEFAULT_BLOCKING_ALLOWED;
		_blockedByDefault = Config.DEFAULT_BLOCKED_BY_DEFAULT_VALUE;
    }

    /** Helper function to extract Date Parameters from Properties */
    protected static Date getDateParam(Properties prop, String key) {
        String val = prop.getProperty(key);
        if (val == null) {
            throw new NullPointerException("Missing '" + key +"' property");
        }
        try {
            return TaskImpl.getIsoFormat().parse(val);
        }
        catch (ParseException px) {
            Logger.error("failed to getDateParam("
                + key + ") " + val, px, Task.class);
        }
        return null;
    }

    /** Helper function to extract int Parameters from Properties */
    protected static int getIntParam(Properties prop, String key) {
        String val = prop.getProperty(key);
        if (val == null) {
            throw new NullPointerException("Missing '" + key +"' property");
        }
        return Integer.parseInt(val);
    }

    /** Helper function to extract long Parameters from Properties */
    protected static long getLongParam(Properties prop, String key) {
        String val = prop.getProperty(key);
        if (val == null) {
            throw new NullPointerException("Missing '" + key +"' property");
        }
        return Long.parseLong(val);
    }

    /** Helper function to extract a Bitmask Parameter from Properties */
    protected static int getMaskParam(Properties prop, String key) {
        String val = prop.getProperty(key);
        if (val == null) {
            throw new NullPointerException("Missing '" + key + "' paramater");
        }
        String[] items = StringServices.toArray(val);
        int len  = items.length;
        int mask = 0;
        for (int i=0; i < len; i++) {
            mask |= 1 << Integer.parseInt(items[i]);
        }

        if (mask == 0) {
            throw new IllegalArgumentException(key + ':' + val);
        }
        return mask;
    }

	/**
	 * Construct a new {@link TaskImpl} from {@link Properties}.
	 * <p>
	 * The value stored for key {@link com.top_logic.util.sched.task.Task.Config#NAME_ATTRIBUTE}
	 * must not be <code>null</code> or empty but has to be the name that is registered at the
	 * {@link Scheduler}.
	 * </p>
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use instead: {@link #TaskImpl(InstantiationContext, Config)}
	 */
	@Deprecated
    public TaskImpl(Properties prop)  {
		super(prop.getProperty(Task.Config.NAME_ATTRIBUTE));
		_typedConfig = Maybe.none();

		int daytype = 0;
		int daymask = 0;
		Date date = null;
		int hour = 0;
		int minute = 0;
		long interval = 0;
		int stopHour = 0;
		int stopMinute = 0;

        String val       = prop.getProperty("daytype");
        boolean needMask = false;
        if (val == null) {
            throw new NullPointerException("Missing 'daytype' paramater");
        }
        switch (val.charAt(0)) {
            case 'O':
                daytype = LegacySchedulesCommon.ONCE;                         break;
            case 'M':
                daytype = LegacySchedulesCommon.MONTHLY;   needMask = true;   break;
            case 'W':
                daytype = LegacySchedulesCommon.WEEKLY;    needMask = true;   break;
            case 'D':
                if (val.equals("DAILY"))
                    { daytype = LegacySchedulesCommon.DAILY;  break; }
                else if (val.equals("DATE"))
                    { daytype = LegacySchedulesCommon.DATE;   break; }
            default: throw new IllegalArgumentException("Unknown daytype: " + val);
        }
		String timetype = prop.getProperty("timetype");
		if (timetype != null && timetype.equals("PERIODICALLY")) {
            daytype |= LegacySchedulesCommon.PERIODICALLY;
        }
        if (needMask) {
            daymask = getMaskParam(prop,"daymask");
        }
        if (daytype == LegacySchedulesCommon.DATE) {
            date = getDateParam(prop,"when");
        }

        if (0 != (daytype & LegacySchedulesCommon.PERIODICALLY)) {
            hour       = getIntParam(prop, "startHour");
            minute     = getIntParam(prop, "startMinute");
            interval   = getLongParam(prop, "interval");
            stopHour   = getIntParam(prop, "stopHour");
            stopMinute = getIntParam(prop, "stopMinute");
        }
        else {
            hour   = getIntParam(prop, "hour");
            minute = getIntParam(prop, "minute");
        }

		_schedulingAlgorithm =
			createLegacySchedule(daytype, date, daymask, hour, minute, interval, stopHour, stopMinute);
		_runOnStartup = Boolean.parseBoolean(prop.getProperty("runOnStartup", "true").trim());
		setRunOnStartup(_runOnStartup);
		_needsMaintenanceMode = Config.DEFAULT_NEEDS_MAINTENANCE_MODE;
		_maintenanceModeDelay = Config.DEFAULT_MAINTENANCE_MODE_DELAY;
		_maintenanceModeSafe = Config.DEFAULT_MAINTENANCE_MODE_SAFE;
		_blockingAllowed = Config.DEFAULT_BLOCKING_ALLOWED;
		_blockedByDefault = Config.DEFAULT_BLOCKED_BY_DEFAULT_VALUE;
    }

	/**
	 * Is called in the legacy constructors for creating the {@link SchedulingAlgorithm}.
	 * <p>
	 * Hook for subclasses that use the legacy constructors and had overridden one of the deleted
	 * calcFoo methods. That code has to be put to a custom {@link SchedulingAlgorithm} which the
	 * subclass can create by overriding this method.
	 * </p>
	 * 
	 * TODO #6121: Delete TL 5.8.0 deprecation
	 * 
	 * @deprecated Use normal {@link Task} and {@link SchedulingAlgorithm} instantiation via
	 *             {@link TypedConfiguration} instead.
	 */
	protected SchedulingAlgorithm createLegacySchedule(
			int daytype, Date date, int daymask, int hour, int minute, long interval, int stopHour, int stopMinute) {
		/* The bitmask is necessary as there is at least one other bit that is used as an
		 * (independent) bitflag. */
		switch (daytype & LegacySchedulesCommon.DAY_MASK) {
			case LegacySchedulesCommon.ONCE: {
				return new LegacyOnceSchedule(hour, minute);
			}
			case LegacySchedulesCommon.DATE: {
				return new LegacyDateSchedule(date, hour, minute);
			}
			case LegacySchedulesCommon.DAILY: {
				return new LegacyDailySchedule(daytype, hour, minute, interval, stopHour, stopMinute);
			}
			case LegacySchedulesCommon.WEEKLY: {
				return new LegacyWeeklySchedule(daytype, daymask, hour, minute, interval, stopHour, stopMinute);
			}
			case LegacySchedulesCommon.MONTHLY: {
				return new LegacyMonthlySchedule(daytype, daymask, hour, minute, interval, stopHour, stopMinute);
			}
			default: {
				throw new UnsupportedOperationException("Illegal daytype value: " + Integer.toBinaryString(daytype));
			}
		}
	}

    /**
	 * Creates a {@link TaskImpl} from configuration.
	 * 
	 * @param context
	 *        The context for instantiating sub configurations.
	 * @param config
	 *        The configuration.
	 */
	@CalledByReflection
	public TaskImpl(InstantiationContext context, C config) {
    	super (config.getName());
		_typedConfig = Maybe.some(config);
		if (config.getSchedules() == null) {
			throw new ConfigurationError("Property '" + Config.SCHEDULES
				+ "' is not allowed to be null. Task: '" + config.getName() + "'");
		}
		_schedulingAlgorithm = SchedulingAlgorithmCombinator.combine(context, config.getSchedules());

		_runOnStartup = config.isRunOnStartup();
		_needsMaintenanceMode = config.isNeedingMaintenanceMode();
		if (config.getMaintenanceModeDelay() < 0) {
			throw new IllegalArgumentException("Maintenance mode delay must not be negative.");
		}
		_maintenanceModeDelay = config.getMaintenanceModeDelay();
		_maintenanceModeSafe = config.isMaintenanceModeSafe();
		_blockingAllowed = config.isBlockingAllowed();
		_blockedByDefault = config.isBlockedByDefault();
	}

	/**
	 * The {@link TaskImpl.Config configuration} of this {@link TaskImpl}, if there is any.
	 * <p>
	 * The legacy constructors don't use the typed configuration. Therefore, this "may return" a
	 * typed configuration. That's why a {@link Maybe} is returned.
	 * </p>
	 */
	protected Maybe<C> getTypedConfig() {
		return _typedConfig;
	}

	@Override
	public SchedulingAlgorithm getSchedulingAlgorithm() {
		return _schedulingAlgorithm;
	}

	private TransientTaskLog createTransientLog() {
		return new TransientTaskLog(this);
	}

	/**
	 * Calculate and return the time when Task should be executed next.
	 *
	 * @param notBefore
	 *        should be same {@link System#currentTimeMillis()} for all task currently recalculated
	 *        by Scheduler.
	 * @return {@link SchedulingAlgorithm#NO_SCHEDULE} to indicate that no further Scheduling is
	 *         needed.
	 *
	 */
    @Override
	public long calcNextShed(long notBefore) {
		synchronized (_runRequestLock) {
			if (_runRequest != null) {
				nextShed = _runRequest.getStart();
				return nextShed;
			}
		}
		setNextSchedule(getSchedulingAlgorithm().nextSchedule(notBefore, getLastSchedule()));
        return nextShed;
    }

    @Override
	public long getNextShed() {
        return nextShed;
    }

	@Override
	public Maybe<Calendar> getNextSchedule() {
		return nextScheduleFromLong(nextShed);
	}

	/**
	 * Set the time specified by the {@link Calendar} as the next schedule (=run time).
	 * 
	 * <p>
	 * <em>Note:</em> {@link SchedulingAlgorithm#NO_SCHEDULE} means this {@link Task} should not run
	 * any more.
	 * </p>
	 * 
	 * @see TaskCommon#nextScheduleFromLong(long)
	 * @see TaskCommon#nextScheduleToLong(Maybe)
	 */
	protected void setNextSchedule(long next) {
		nextShed = next;
	}

	@Override
	public long getLastSchedule() {
		return lastSched;
	}

	/**
	 * <b>Subclasses have to call <code>super.run()</code>. <br/>
	 * See {@link #isPersistent()} and {@link #isNodeLocal()} for further important implementation
	 * requirements.</b>
	 */
    @Override
	public void run() {
		updateTimes(now());
	}

	/**
	 * Stores the current time as last run time and calculates the next.
	 * <p>
	 * <b>Must only be used for tests.</b><br/>
	 * This is the only thing that happens in {@link #run()}. If tests want to fake the time, they
	 * have to call this method with the fake time instead of {@link #run()}.
	 * </p>
	 */
	protected final void updateTimes(long now) {
		markAsRun(now);
		nextShed = SchedulingAlgorithm.NO_SCHEDULE;
		calcNextShed(now);
	}

	@Override
	public boolean markAsRun(long runStart) {
		boolean result = false;
		synchronized (_runRequestLock) {
			if ((_runRequest != null) && (_runRequest.getRequest() <= runStart)) {
				_runRequest = null;
				result = true;
			}
		}
		if (runStart > lastSched) {
			result = true;
			lastSched = runStart;
		}
		return result;
	}

	@Override
	public final void forceRun(long start) {
		long now = now();
		synchronized (_runRequestLock) {
			_runRequest = new RunRequest(now, start);
		}
		calcNextShed(now);
	}

	@Override
	public final boolean isForcedRun() {
		synchronized (_runRequestLock) {
			return _runRequest != null;
		}
	}

    @Override
	public String toString() {
		return getClass().getName() + "(" + getName() + ")";
    }

    /** 
     * Avoid this task to be run on system startup.
     * 
     * @param    aFlag    <code>false</code> to avoid running on system startup.
     */
    public void setRunOnStartup(boolean aFlag) {
		if (!aFlag) {
			long now = now();
			long nextRun = calcNextShed(now);
			long currentTimeMillis = System.currentTimeMillis();
			if (nextRun == SchedulingAlgorithm.NO_SCHEDULE) {
				return;
			}
			if (nextRun <= currentTimeMillis) {
				// Make the scheduler assuming this task has been run now.
				lastSched = currentTimeMillis;
				long postponedRun = calcNextShed(now);
				Logger.info("Task '" + getName() + "' was configured to not run on startup."
					+ " Its next run is therefore postponed from " + new Date(nextRun) + " to "
					+ new Date(postponedRun) + ".", TaskImpl.class);
			}
        }
    }

	/**
	 * The current time.
	 */
	protected long now() {
		return System.currentTimeMillis();
	}

	/**
	 * Convert an integer day type back into a readable String.
	 */
    public static String getDayTypeString(int aDaytype) {
        return (aDaytype == LegacySchedulesCommon.ONCE) ? "ONCE" :
               (aDaytype == LegacySchedulesCommon.DATE) ? "DATE" :
               (aDaytype == LegacySchedulesCommon.DAILY) ? "DAILY" :
               (aDaytype == LegacySchedulesCommon.WEEKLY) ? "WEEKLY" :
               (aDaytype == LegacySchedulesCommon.MONTHLY) ? "MONTHLY" : "";
    }

	/**
	 * Formatter according to ISO.
	 * 
	 * This is only one of the possible Formats, though. It is used for the dublin core attributes,
	 * but might be used in HTML-Headers, too
	 */
	public static DateFormat getIsoFormat() {
		return CalendarUtil.newSimpleDateFormat("yyyy-MM-dd");
	}

	/**
	 * Defines, if this task need to run in every cluster node by itself.
	 * 
	 * In some cases it may be needful, if a task runs only on one cluster node (e.g. refresh of
	 * external user data), these tasks need to return <code>false</code> here.
	 * 
	 * <p>
	 * If this method returns <code>false</code>, the {@link Task} has to call
	 * {@link TaskLog#taskStarted()} and
	 * {@link TaskLog#taskEnded(com.top_logic.util.sched.task.result.TaskResult.ResultType, ResKey, Throwable)}
	 * only on one of the nodes. The node is allowed to change from one run to another run, but has
	 * to be the same during one run. <br/>
	 * Additionally, if this method returns <code>false</code>, {@link #isPersistent()} will return
	 * <code>true</code> and the implementation constraints mentioned there apply.
	 * </p>
	 * 
	 * @return <code>true</code> when task must run in every cluster node.
	 */
	@Override
	public boolean isNodeLocal() {
		return true;
	}

	@Override
	public boolean needsMaintenanceMode() {
		return _needsMaintenanceMode;
	}

	@Override
	public long getMaintenanceModeDelay() {
		return _maintenanceModeDelay;
	}

	@Override
	public boolean isMaintenanceModeSafe() {
		return _maintenanceModeSafe;
	}

	@Override
	public boolean isRunOnStartup() {
		return _runOnStartup;
	}

	@Override
	public boolean isPersistent() {
		return !isNodeLocal();
	}

	@Override
	public TaskLog getLog() {
		return _log;
	}

	private TaskLog createLog(Scheduler scheduler) {
		if (isPersistent()) {
			return createPersistentLog(scheduler);
		} else {
			return createTransientLog();
		}
	}

	private TaskLog createPersistentLog(Scheduler scheduler) {
		if (StringServices.isEmpty(getName())) {
			String message = "Task has no name. Unable to create a persistent task log,"
				+ " as a name is required for that. Class of task: " + getClass().getName();
			Logger.error(message, TaskImpl.class);
			return createTransientLog();
		}
		TaskLogWrapper log = TaskLogWrapper.getLogForTask(TaskImpl.this);
		log.startupNodeClean(scheduler, this);
		return log;
	}

	/**
	 * Returns the {@link Scheduler} to which the {@link Task} is currently attached.
	 * 
	 * @return {@link Maybe#none()} if the {@link Task} is not attached to a {@link Scheduler}.
	 */
	protected Maybe<Scheduler> getScheduler() {
		return Maybe.toMaybe(_scheduler);
	}

	@Override
	public final void attachTo(Scheduler scheduler) {
		if (scheduler == null) {
			throw new NullPointerException("Scheduler is not allowed to be null.");
		}
		if (_scheduler != null) {
			String message = "The Task cannot be attach to a Scheduler, as it is already attached to a Scheduler."
				+ " Task: " + debug(getName()) + "; current Scheduler: " + debug(_scheduler)
				+ "; new Scheduler: " + debug(scheduler);
			throw new NullPointerException(message);
		}
		_scheduler = scheduler;
		_log = createLog(scheduler);
		getLog().setEventQueue(scheduler.getTaskUpdateQueue());
		onAttachToScheduler();

		setRunOnStartup(_runOnStartup);
	}

	@Override
	public final void detachFromScheduler() {
		if (_scheduler == null) {
			throw new NullPointerException("The Task is not attached to a Scheduler and can therefore not be detached.");
		}
		onDetachFromScheduler();
		getLog().setEventQueue(null);
		_log = null;
		_scheduler = null;
	}

	/**
	 * Hook for subclasses. Called at the end of {@link #attachTo(Scheduler)}.
	 * <p>
	 * {@link #getScheduler()} is guaranteed to already return the {@link Scheduler} when this hook
	 * is called.
	 * </p>
	 */
	protected void onAttachToScheduler() {
		// Hook for subclasses
	}

	/**
	 * Hook for subclasses. Called at the beginning of {@link #detachFromScheduler()}.
	 * <p>
	 * {@link #getScheduler()} is guaranteed to still return the {@link Scheduler} when this hook is
	 * called.
	 * </p>
	 */
	protected void onDetachFromScheduler() {
		// Hook for subclasses
	}

	/**
	 * Made final to ensure it's always called. Subclasses can override {@link #signalStopHook()}
	 * instead.
	 */
	@Override
	public synchronized final boolean signalStop() {
		logSignalStop();
		RetryResult<Boolean, List<Throwable>> result = Retry.retry(COMMIT_RETRIES, createExponentialBackoff(),
			TaskImpl.this::signalStopUnsafe);
		if (!result.isSuccess()) {
			String message = "Failed to stop task '" + getName() + "' after " + COMMIT_RETRIES + " attempts."
				+ " Assuming the task does not want to be stopped.";
			RuntimeException retryError = ExceptionUtil.createException(message, result.getReason());
			Logger.error(retryError.getMessage(), retryError, TaskImpl.class);
			return false;
		}
		return true;
	}

	private RetryResult<Boolean, Throwable> signalStopUnsafe() {
		try {
			if (getLog() instanceof TaskLogWrapper) {
				return RetryResult.createSuccess(signalStopClusterGlobal());
			}
			return RetryResult.createSuccess(signalStopClusterNodeLocal());
		} catch (RuntimeException ex) {
			return RetryResult.<Boolean, Throwable> createFailure(ex);
		}
	}

	private void logSignalStop() {
		if (Logger.isDebugEnabled(TaskImpl.class)) {
			Logger.debug("Task '" + getName() + "' was signaled to stop.",
				new RuntimeException("Stacktrace"), TaskImpl.class);
		}
	}

	private boolean signalStopClusterNodeLocal() {
		if (getLog().getState() == TaskState.RUNNING) {
			getLog().taskCanceling();
			boolean superResult = super.signalStop();
			boolean hookResult = signalStopHook();
			return superResult & hookResult;
		}
		return true;
	}

	/** Hook for subclasses. Is called in {@link #signalStop()}. */
	protected boolean signalStopHook() {
		return true;
	}

	private boolean signalStopClusterGlobal() {
		RetryResult<Boolean, List<Throwable>> result = Retry.retry(COMMIT_RETRIES, createExponentialBackoff(),
			TaskImpl.this::trySignalStopClusterGlobal);
		if (!result.isSuccess()) {
			throw ExceptionUtil.createException("Failed " + COMMIT_RETRIES + " times to signal task '" + getName()
				+ "' to stop. Giving up.", result.getReason());
		}
		return result.getValue();
	}

	private RetryResult<Boolean, Throwable> trySignalStopClusterGlobal() {
		if (getLog().getState() != TaskState.RUNNING) {
			// Test without 'lock()' first,
			// to prevent conflicts with the commit of taskStarted.
			return RetryResult.createSuccess(true);
		}
		Transaction transaction = PersistencyLayer.getKnowledgeBase().beginTransaction();
		try {
			TaskLogWrapper logWrapper = (TaskLogWrapper) getLog();
			logWrapper.touch();
			if (logWrapper.getState() != TaskState.RUNNING) {
				// The chance for this is almost(!) 0 as it is checked above,
				// but to be (race condition) safe, it's here anyway.
				transaction.rollback();
				return RetryResult.createSuccess(true);
			}
			if (isTaskExecutingOnLocalNode(logWrapper)) {
				boolean result = signalStopClusterGlobalOnLocalNode(logWrapper);
				transaction.commit();
				return RetryResult.createSuccess(result);
			} else {
				logWrapper.setState(TaskState.CANCELING);
				transaction.commit();
				return RetryResult.createSuccess(true);
			}
		} catch (RuntimeException ex) {
			transaction.rollback();
			return RetryResult.<Boolean, Throwable> createFailure(ex);
		} finally {
			transaction.rollback();
		}
	}

	/**
	 * @throws NullPointerException
	 *         If there is no current result.
	 */
	private boolean isTaskExecutingOnLocalNode(TaskLogWrapper logWrapper) {
		long executingNode = logWrapper.getCurrentResult().getClusterNodeId();
		long localNode = getCurrentClusterNodeId();
		return Utils.equals(executingNode, localNode);
	}

	private boolean signalStopClusterGlobalOnLocalNode(TaskLogWrapper logWrapper) {
		logWrapper.setState(TaskState.CANCELING);
		boolean superResult = super.signalStop();
		boolean hookResult = signalStopHook();
		return superResult & hookResult;
	}

	@Override
	protected synchronized boolean getShouldStop() {
		// Check if another cluster node signaled to stop by checking the TaskState.
		return super.getShouldStop() || isCancelling();
	}

	private boolean isCancelling() {
		TaskLog log = getLog();
		return log != null && log.getState() == TaskState.CANCELING;
	}

	/**
	 * Utility for subclasses to be called when a new run starts. Creates the log {@link File} for
	 * that run.
	 * <p>
	 * It is recommended, but not mandatory, for subclasses to use this method.
	 * </p>
	 * 
	 * @param start
	 *        Must not be <code>null</code>.
	 * 
	 * @return Is <code>null</code> when no log file is used.
	 */
	protected File createLogFile(Date start) {
		return DefaultTaskLogFileFactory.INSTANCE.getLogFile(this, start);
	}

	@Override
	public void logGuiWarning(String message) {
		if (getLog().getState() == TaskState.INACTIVE) {
			throw new IllegalStateException("Gui warnings can only be logged while the task is running.");
		}
		getLog().getCurrentResult().addWarning(message);
	}

	@Override
	public boolean isBlockingAllowed() {
		return _blockingAllowed;
	}

	@Override
	public boolean isBlockedByDefault() {
		return _blockedByDefault;
	}

}
