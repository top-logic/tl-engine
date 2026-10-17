/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.monitoring.revision;

import java.util.Calendar;
import java.util.Date;

import com.top_logic.basic.DateUtil;
import com.top_logic.basic.thread.ThreadContext;
import com.top_logic.basic.time.CalendarUtil;
import com.top_logic.knowledge.service.HistoryManager;

/**
 * Conversions between the time range and the revision range of the transaction monitor, and the
 * moving of a range by its own width.
 *
 * <p>
 * Both kinds of ranges have an inclusive start and an exclusive end. A <code>null</code> bound
 * leaves the range open on that side.
 * </p>
 *
 * @see RangeMode
 *
 * @author <a href="mailto:daniel.busche@top-logic.com">Daniel Busche</a>
 */
public class RevisionRanges {

	private static final long DAY = 24L * 60 * 60 * 1000;

	/**
	 * The commit number of the first revision created at or after the given point in time.
	 *
	 * <p>
	 * This is the revision bound that corresponds to the given time bound: the revisions in
	 * <code>[firstCommitAt(from), firstCommitAt(to))</code> are exactly the revisions created in
	 * <code>[from, to)</code>.
	 * </p>
	 *
	 * @return <code>null</code>, if the given date is <code>null</code>.
	 */
	public static Long firstCommitAt(HistoryManager hm, Date date) {
		if (date == null) {
			return null;
		}
		return Long.valueOf(hm.getRevisionAt(date.getTime() - 1).getCommitNumber() + 1);
	}

	/**
	 * The point in time that corresponds to the given commit number as a range bound.
	 *
	 * @param commit
	 *        The commit number, or <code>null</code> for an open bound.
	 * @param isStart
	 *        Whether the bound is the start of a range. A start beyond the last revision is the
	 *        current time, an end beyond the last revision is the begin of the day (in the time
	 *        zone of the user) following the creation of the last revision.
	 *
	 * @return The date of the given revision, or <code>null</code> for an open bound.
	 */
	public static Date dateOfCommit(HistoryManager hm, Long commit, boolean isStart) {
		if (commit == null) {
			return null;
		}
		long last = hm.getLastRevision();
		if (commit.longValue() > last) {
			if (isStart) {
				return new Date();
			}
			Calendar nextDay = CalendarUtil.createCalendar(new Date(hm.getRevision(last).getDate()),
				ThreadContext.getTimeZone());
			nextDay.add(Calendar.DAY_OF_MONTH, 1);
			return DateUtil.adjustToDayBegin(nextDay);
		}
		long existing = Math.max(commit.longValue(), hm.getFirstRevision());
		return new Date(hm.getRevision(existing).getDate());
	}

	/**
	 * Moves the revision range <code>[from, to)</code> by its own width.
	 *
	 * <p>
	 * The range is not moved beyond the existing revisions: its start stays at or after the first
	 * revision, its end at or before the revision following the last one.
	 * </p>
	 *
	 * @param direction
	 *        <code>-1</code> to move the range backwards, <code>1</code> to move it forwards.
	 * @return The new start and the new end.
	 */
	public static long[] shift(HistoryManager hm, long from, long to, int direction) {
		long width = to - from;
		if (direction < 0) {
			long start = Math.max(from - width, Math.min(from, hm.getFirstRevision()));
			return new long[] { start, start + width };
		} else {
			long end = Math.min(to + width, Math.max(to, hm.getLastRevision() + 1));
			return new long[] { end - width, end };
		}
	}

	/**
	 * Moves the time range <code>[from, to)</code> by its own width.
	 *
	 * <p>
	 * A range of whole days in the time zone of the user is moved by days, so that its bounds keep
	 * their time of day across a change of the daylight saving time.
	 * </p>
	 *
	 * <p>
	 * A range that already contains the current time is not moved forwards, a range that starts at
	 * or before the first revision is not moved backwards.
	 * </p>
	 *
	 * @param direction
	 *        <code>-1</code> to move the range backwards, <code>1</code> to move it forwards.
	 * @return The new start and the new end.
	 */
	public static Date[] shift(HistoryManager hm, Date from, Date to, int direction) {
		boolean atLimit = direction > 0 ? to.getTime() > System.currentTimeMillis()
			: from.getTime() <= hm.getRevision(hm.getFirstRevision()).getDate();
		if (atLimit) {
			return new Date[] { from, to };
		}
		long width = to.getTime() - from.getTime();
		Calendar start = CalendarUtil.createCalendar(from, ThreadContext.getTimeZone());
		Calendar end = CalendarUtil.createCalendar(to, ThreadContext.getTimeZone());
		long days = Math.round(width / (double) DAY);
		if (days > 0 && sameTimeOfDay(start, end)) {
			start.add(Calendar.DAY_OF_MONTH, (int) (direction * days));
			end.add(Calendar.DAY_OF_MONTH, (int) (direction * days));
			return new Date[] { start.getTime(), end.getTime() };
		}
		return new Date[] { new Date(from.getTime() + direction * width), new Date(to.getTime() + direction * width) };
	}

	private static boolean sameTimeOfDay(Calendar c1, Calendar c2) {
		return c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY)
			&& c1.get(Calendar.MINUTE) == c2.get(Calendar.MINUTE)
			&& c1.get(Calendar.SECOND) == c2.get(Calendar.SECOND)
			&& c1.get(Calendar.MILLISECOND) == c2.get(Calendar.MILLISECOND);
	}

}
