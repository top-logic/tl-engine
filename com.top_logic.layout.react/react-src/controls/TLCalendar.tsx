import {
  React, useTLState, useTLCommand, useI18N, startPointerDrag, rootClassName, stateAttr, tooltipProps, usePart,
  useStandaloneKeyboardScope, categoryOf, textOn, isInteractiveWithin,
} from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';
import { useRovingFocus } from './useRovingFocus';

const { useState, useRef, useCallback, useMemo, useEffect } = React;

/**
 * Outlook-style calendar view. Renders the {@code events} pushed by the server-side
 * {@code CalendarViewControl} at day / work-week / week / month / year granularity and reports
 * navigation, selection and drag edits back through commands.
 *
 * Design system: `tl-calendar` (calendar.contract.md). The toolbar is a `tl-toolbar` with the view
 * choice as a `tl-segmented` radio group. The events of a grid - the row of whole days, the time
 * grid, the month - are one list box with one tab stop (useRovingFocus, in the time grid linear):
 * the arrows move through them in the order of time, Enter or Space selects one. An event is drawn
 * in its category (`data-tl-role="category-N"`, categoryOf), in the brand without one, or in the
 * color the server sends with the text readable on it (textOn). The days are buttons named by their
 * full date; today is `aria-current="date"`, every other state of a day a word of `data-tl-state`.
 * Moving, resizing and creating events is done with the pointer.
 */

const I18N_KEYS = {
  'js.calendar.toolbar': 'Calendar',
  'js.calendar.view': 'View',
  'js.calendar.events': 'Events',
  'js.calendar.today': 'Today',
  'js.calendar.previous': 'Previous',
  'js.calendar.next': 'Next',
  'js.calendar.day': 'Day',
  'js.calendar.workWeek': 'Work week',
  'js.calendar.week': 'Week',
  'js.calendar.month': 'Month',
  'js.calendar.year': 'Year',
  'js.calendar.allDay': 'All day',
  'js.calendar.more': 'more',
  'js.calendar.newEventTitle': 'Event title',
};

type Granularity = 'DAY' | 'WORK_WEEK' | 'WEEK' | 'MONTH' | 'YEAR';

interface Ev {
  id: string;
  start: number;
  end: number;
  allDay: boolean;
  title: string;
  tooltip?: string;
  category?: string | null;
  color?: string | null;
  movable: boolean;
  resizable: boolean;
  selected: boolean;
}

const HOUR_HEIGHT = 44;
const SNAP_MIN = 15;
const MS_MIN = 60000;
const MS_HOUR = 3600000;
const MS_DAY = 86400000;

// --- date helpers (client-local time zone) ------------------------------

function startOfDay(ms: number): number {
  const d = new Date(ms);
  d.setHours(0, 0, 0, 0);
  return d.getTime();
}

function addDays(ms: number, n: number): number {
  const d = new Date(ms);
  d.setDate(d.getDate() + n);
  return d.getTime();
}

function dayKey(ms: number): number {
  return startOfDay(ms);
}

function isSameDay(a: number, b: number): boolean {
  return startOfDay(a) === startOfDay(b);
}

function minutesOfDay(ms: number): number {
  return (ms - startOfDay(ms)) / MS_MIN;
}

function snap(min: number): number {
  return Math.round(min / SNAP_MIN) * SNAP_MIN;
}

function clamp(v: number, lo: number, hi: number): number {
  return Math.max(lo, Math.min(hi, v));
}

function parseEvents(raw: unknown): Ev[] {
  if (!Array.isArray(raw)) {
    return [];
  }
  return (raw as Record<string, unknown>[]).map((e) => ({
    id: e.id as string,
    start: e.start as number,
    end: e.end as number,
    allDay: e.allDay === true,
    title: (e.title as string) ?? '',
    tooltip: e.tooltip as string | undefined,
    category: e.category as string | null | undefined,
    color: e.color as string | null | undefined,
    movable: e.movable === true,
    resizable: e.resizable === true,
    selected: e.selected === true,
  }));
}

// --- paint of an event ---------------------------------------------------

/**
 * How an event is drawn: in its category role, in the brand without one (the block's own rule), or
 * in the color the server sends, with the text readable on it.
 */
interface Paint {
  /** The `data-tl-role` of the event: its category; absent for the brand and for a server color. */
  role?: string;

  /** The color the server sends; absent where none is sent or no readable text exists on it. */
  color?: string;

  /** The text role readable on {@link color} (textOn). */
  text?: string;
}

/**
 * The paint of the given event. A server color stands only where a readable text on it is known;
 * otherwise the event falls back to its category, without one to the brand.
 */
function paintOf(ev: Ev, readable: (color: string) => string | null): Paint {
  if (ev.color) {
    const text = readable(ev.color);
    if (text !== null) {
      return { color: ev.color, text };
    }
  }
  return ev.category ? { role: 'category-' + categoryOf(ev.category) } : {};
}

/** The attributes of a filled event in the given paint; the server color stands inline as data. */
function filledProps(paint: Paint, placement?: React.CSSProperties) {
  return {
    'data-tl-role': paint.role,
    style: paint.color ? { ...placement, background: paint.color, color: paint.text } : placement,
  };
}

/** The attributes of the dot of a chip in the given paint: the surface alone, without text. */
function dotProps(paint: Paint) {
  return {
    'data-tl-role': paint.role,
    style: paint.color ? { background: paint.color } : undefined,
  };
}

// --- shared render context ---------------------------------------------

interface Ctx {
  locale: string;
  firstDayOfWeek: number;
  nonWorkingDays: number[];
  dayStartHour: number;
  dayEndHour: number;
  editable: boolean;
  now: number;
  events: Ev[];
  send: (command: string, args?: Record<string, unknown>) => void;
  i18n: Record<string, string>;
  paint: (ev: Ev) => Paint;
}

function fmt(locale: string, options: Intl.DateTimeFormatOptions, ms: number): string {
  return new Intl.DateTimeFormat(locale, options).format(new Date(ms));
}

function timeRange(locale: string, ev: Ev): string {
  const opts: Intl.DateTimeFormatOptions = { hour: 'numeric', minute: '2-digit' };
  return fmt(locale, opts, ev.start) + '–' + fmt(locale, opts, ev.end);
}

/** The full date of a day, the name of the button standing for it. */
function fullDate(locale: string, day: number): string {
  return fmt(locale, { weekday: 'long', day: 'numeric', month: 'long', year: 'numeric' }, day);
}

/** The name of an event in its list: its time and title, for a whole-day event its title and "all day". */
function eventName(ctx: Ctx, ev: Ev): string {
  return ev.allDay
    ? ev.title + ', ' + ctx.i18n['js.calendar.allDay']
    : timeRange(ctx.locale, ev) + ', ' + ev.title;
}

/**
 * The attributes and handlers making an event an option of its list box: a click, Enter or Space
 * selects it.
 */
function optionProps(ctx: Ctx, ev: Ev, tabIndex: number) {
  const select = () => ctx.send('selectEvent', { eventId: ev.id });
  return {
    role: 'option',
    'aria-selected': ev.selected,
    'aria-label': eventName(ctx, ev),
    tabIndex,
    ...tooltipProps(ev.tooltip),
    onClick: select,
    onKeyDown: (e: React.KeyboardEvent) => {
      if (e.target === e.currentTarget && (e.key === 'Enter' || e.key === ' ')) {
        e.preventDefault();
        select();
      }
    },
  };
}

// --- Toolbar ------------------------------------------------------------

const GRANULARITIES: { key: Granularity; label: keyof typeof I18N_KEYS }[] = [
  { key: 'DAY', label: 'js.calendar.day' },
  { key: 'WORK_WEEK', label: 'js.calendar.workWeek' },
  { key: 'WEEK', label: 'js.calendar.week' },
  { key: 'MONTH', label: 'js.calendar.month' },
  { key: 'YEAR', label: 'js.calendar.year' },
];

/** Icon of the button moving to the previous period. */
const PREVIOUS_ICON = 'css:bi bi-chevron-left';

/** Icon of the button moving to the next period. */
const NEXT_ICON = 'css:bi bi-chevron-right';

/**
 * The choice of the view: a radio group of segments. Exactly the chosen segment is in the tab
 * order; the arrows choose the next or previous view, as in every radio group (TLSegmentedChoice).
 */
const ViewChoice: React.FC<{
  granularity: Granularity;
  i18n: Record<string, string>;
  send: Ctx['send'];
}> = ({ granularity, i18n, send }) => {
  const segmentRefs = useRef<(HTMLButtonElement | null)[]>([]);
  const chosen = GRANULARITIES.findIndex((g) => g.key === granularity);

  const onKeyDown = (e: React.KeyboardEvent) => {
    let step = 0;
    if (e.key === 'ArrowRight' || e.key === 'ArrowDown') {
      step = 1;
    } else if (e.key === 'ArrowLeft' || e.key === 'ArrowUp') {
      step = -1;
    } else {
      return;
    }
    e.preventDefault();
    e.stopPropagation();
    // From the focused segment: a second press goes on from the first, before the server answered it.
    const focused = segmentRefs.current.indexOf(e.target as HTMLButtonElement);
    const from = focused >= 0 ? focused : chosen;
    const next = (from + step + GRANULARITIES.length) % GRANULARITIES.length;
    segmentRefs.current[next]?.focus();
    send('switchGranularity', { granularity: GRANULARITIES[next].key });
  };

  return (
    <div className="tl-segmented" role="radiogroup" aria-label={i18n['js.calendar.view']} onKeyDown={onKeyDown}>
      {GRANULARITIES.map((g, index) => (
        <button
          key={g.key}
          ref={(element) => {
            segmentRefs.current[index] = element;
          }}
          type="button"
          role="radio"
          aria-checked={index === chosen}
          tabIndex={index === (chosen < 0 ? 0 : chosen) ? 0 : -1}
          className="tl-segmented__segment tl-type-label"
          onClick={() => send('switchGranularity', { granularity: g.key })}
        >
          <span className="tl-segmented__label">{i18n[g.label]}</span>
        </button>
      ))}
    </div>
  );
};

const Toolbar: React.FC<{
  title: string;
  granularity: Granularity;
  i18n: Record<string, string>;
  send: Ctx['send'];
}> = ({ title, granularity, i18n, send }) => {
  const Button = usePart('Button');
  return (
    <div className="tl-toolbar tl-calendar__toolbar" role="toolbar" aria-label={i18n['js.calendar.toolbar']}>
      <div className="tl-toolbar__group">
        <Button
          label={i18n['js.calendar.today']}
          display="label-only"
          appearance="secondary"
          onClick={() => send('navigate', { direction: 'TODAY' })}
        />
        <Button
          label={i18n['js.calendar.previous']}
          image={PREVIOUS_ICON}
          display="icon-only"
          appearance="secondary"
          onClick={() => send('navigate', { direction: 'PREV' })}
        />
        <Button
          label={i18n['js.calendar.next']}
          image={NEXT_ICON}
          display="icon-only"
          appearance="secondary"
          onClick={() => send('navigate', { direction: 'NEXT' })}
        />
      </div>
      <div className="tl-calendar__title tl-type-heading-sm">{title}</div>
      <ViewChoice granularity={granularity} i18n={i18n} send={send} />
    </div>
  );
};

// --- Time grid (day / work week / week) --------------------------------

interface Placed {
  ev: Ev;
  topMin: number;
  botMin: number;
  col: number;
  cols: number;
}

/** Column layout for overlapping timed events within a single day. */
function layoutDay(events: Ev[]): Placed[] {
  const sorted = [...events].sort((a, b) => a.start - b.start || b.end - a.end);
  const placed: Placed[] = [];
  let cluster: Placed[] = [];
  let clusterEnd = -1;

  const flush = () => {
    const cols = cluster.reduce((m, p) => Math.max(m, p.col + 1), 0);
    for (const p of cluster) {
      p.cols = cols;
    }
    placed.push(...cluster);
    cluster = [];
    clusterEnd = -1;
  };

  for (const ev of sorted) {
    if (cluster.length > 0 && ev.start >= clusterEnd) {
      flush();
    }
    const used = new Set(cluster.filter((p) => p.ev.end > ev.start).map((p) => p.col));
    let col = 0;
    while (used.has(col)) {
      col++;
    }
    cluster.push({
      ev,
      topMin: minutesOfDay(ev.start),
      botMin: minutesOfDay(ev.start) + Math.max(15, (ev.end - ev.start) / MS_MIN),
      col,
      cols: 1,
    });
    clusterEnd = Math.max(clusterEnd, ev.end);
  }
  if (cluster.length > 0) {
    flush();
  }
  return placed;
}

/** The positions of the items of the given groups in one sequence: the position of each group's first item. */
function offsetsOf(groups: readonly unknown[][]): number[] {
  const offsets: number[] = [];
  let next = 0;
  for (const group of groups) {
    offsets.push(next);
    next += group.length;
  }
  return offsets;
}

/** Selects the events of the time grid, evaluated on its list box. */
const TIMED_OPTIONS = ':scope > .tl-calendar__column > [role="option"]';

/** Selects the events of the row of whole days, evaluated on its list box. */
const ALL_DAY_OPTIONS = ':scope > .tl-calendar__all-day-cell > [role="option"]';

/** Selects the events of the month, evaluated on its list box. */
const MONTH_OPTIONS = ':scope > .tl-calendar__week > .tl-calendar__overlay > [role="option"]';

/** Selects the days of a mini month, evaluated on its grid. */
const MINI_DAYS = ':scope > .tl-calendar__mini-day';

type TimeDrag =
  | { mode: 'move'; id: string; grabMin: number; dur: number; dayStart: number; startMin: number; origStartMs: number }
  | { mode: 'resize'; id: string; dayStart: number; startMin: number; endMin: number; origEndMs: number }
  | { mode: 'create'; dayStart: number; fromMin: number; toMin: number };

/** A slot the user selected but has not confirmed yet. It exists in the browser only. */
type PendingCreate = { start: number; end: number; allDay: boolean };

/**
 * Title input of an event that is not created yet: Enter creates it with the entered title,
 * Escape and losing the focus drop the stub without contacting the server.
 *
 * Escape reaches the stub through the keyboard dispatcher: while the stub exists, a scope of its
 * own binds Escape to dropping the stub - and to nothing else, so a window around the calendar
 * stays open.
 */
const CreateStub: React.FC<{
  placeholder: string;
  style?: React.CSSProperties;
  onCommit: (title: string) => void;
  onDiscard: () => void;
}> = ({ placeholder, style, onCommit, onDiscard }) => {
  // Enter both commits and blurs the input: whichever event arrives first decides.
  const settled = useRef(false);
  const settle = (title: string | null) => {
    if (settled.current) {
      return;
    }
    settled.current = true;
    if (title === null) {
      onDiscard();
    } else {
      onCommit(title);
    }
  };
  useStandaloneKeyboardScope(true, {
    ESCAPE: () => {
      settle(null);
      return true;
    },
  });
  return (
    <div className="tl-calendar__event tl-type-label" data-tl-state="preview" style={style}>
      <input
        className="tl-field tl-calendar__create tl-type-label"
        autoFocus
        placeholder={placeholder}
        aria-label={placeholder}
        onPointerDown={(e) => e.stopPropagation()}
        onClick={(e) => e.stopPropagation()}
        onKeyDown={(e) => {
          if (e.key === 'Escape') {
            // The dispatcher's: the binding of the stub drops it (see above).
            return;
          }
          e.stopPropagation();
          if (e.key === 'Enter') {
            settle(e.currentTarget.value);
          }
        }}
        onBlur={() => settle(null)}
      />
    </div>
  );
};

/** The slot a grid currently offers for creation, and the ways to end that state. */
const usePendingCreate = (send: Ctx['send']) => {
  const [pending, setPending] = useState<PendingCreate | null>(null);
  // Read through a ref so that committing does not have to be re-created per keystroke.
  const pendingRef = useRef<PendingCreate | null>(null);
  pendingRef.current = pending;
  const open = useCallback((slot: PendingCreate) => setPending(slot), []);
  const discard = useCallback(() => setPending(null), []);
  const commit = useCallback(
    (title: string) => {
      const slot = pendingRef.current;
      if (slot) {
        send('createSlot', { ...slot, title });
      }
      setPending(null);
    },
    [send]
  );
  return { pending, open, commit, discard };
};

const TimeGrid: React.FC<{ ctx: Ctx; rangeStart: number; granularity: Granularity }> = ({
  ctx,
  rangeStart,
  granularity,
}) => {
  const { events, locale, nonWorkingDays, dayStartHour, dayEndHour, now, send, editable, i18n } = ctx;

  const days = useMemo(() => {
    const count = granularity === 'DAY' ? 1 : 7;
    const all: number[] = [];
    for (let i = 0; i < count; i++) {
      const d = addDays(rangeStart, i);
      if (granularity === 'WORK_WEEK' && nonWorkingDays.includes(new Date(d).getDay())) {
        continue;
      }
      all.push(d);
    }
    return all;
  }, [rangeStart, granularity, nonWorkingDays]);

  const create = usePendingCreate(send);
  const colsRef = useRef<HTMLDivElement>(null);
  const scrollRef = useRef<HTMLDivElement>(null);
  const timedListRef = useRef<HTMLDivElement>(null);
  const allDayListRef = useRef<HTMLDivElement>(null);
  const [drag, setDrag] = useState<TimeDrag | null>(null);
  const dragRef = useRef<TimeDrag | null>(null);
  dragRef.current = drag;

  const [tick, setTick] = useState(Date.now());
  useEffect(() => {
    const h = window.setInterval(() => setTick(Date.now()), 60000);
    return () => window.clearInterval(h);
  }, []);

  const pointerToDayMin = useCallback(
    (clientX: number, clientY: number): { dayIndex: number; min: number } => {
      const el = colsRef.current;
      if (!el) {
        return { dayIndex: 0, min: 0 };
      }
      const rect = el.getBoundingClientRect();
      const colW = rect.width / days.length;
      const dayIndex = clamp(Math.floor((clientX - rect.left) / colW), 0, days.length - 1);
      const y = clientY - rect.top + el.scrollTop;
      const min = clamp((y / HOUR_HEIGHT) * 60, 0, 24 * 60);
      return { dayIndex, min };
    },
    [days.length]
  );

  // The gesture that edits the grid: moving an event, dragging its lower edge, or pulling a new
  // slot open. The handlers are installed once, when the gesture starts, and read the drag from
  // dragRef, which every render keeps current - so they always act on the state the grid shows.
  const beginDrag = (e: React.PointerEvent, initial: TimeDrag, cursor?: string) => {
    e.preventDefault();
    create.discard();
    dragRef.current = initial;
    setDrag(initial);

    startPointerDrag(e, {
      cursor,
      // The gesture is captured to the grid, not to the event bar it started on: an event dragged
      // into another day is rendered in that day's column, and a capture on the bar would end with
      // the bar the drag leaves behind.
      captureOn: colsRef.current,

      onMove: (ev) => {
        const d = dragRef.current;
        if (!d) {
          return;
        }
        const { dayIndex, min } = pointerToDayMin(ev.clientX, ev.clientY);
        if (d.mode === 'move') {
          setDrag({ ...d, dayStart: days[dayIndex], startMin: clamp(snap(min - d.grabMin), 0, 24 * 60 - d.dur) });
        } else if (d.mode === 'resize') {
          setDrag({ ...d, endMin: clamp(snap(min), d.startMin + SNAP_MIN, 24 * 60) });
        } else {
          setDrag({ ...d, toMin: clamp(snap(min), 0, 24 * 60) });
        }
      },

      onEnd: () => {
        const d = dragRef.current;
        dragRef.current = null;
        setDrag(null);
        if (!d) {
          return;
        }
        if (d.mode === 'move') {
          const start = d.dayStart + d.startMin * MS_MIN;
          // Only a real move (pointer actually shifted the event) writes back; a plain click falls
          // through to selection without a no-op transaction.
          if (start !== d.origStartMs) {
            send('moveEvent', { eventId: d.id, start, end: start + d.dur * MS_MIN });
          }
        } else if (d.mode === 'resize') {
          const end = d.dayStart + d.endMin * MS_MIN;
          if (end !== d.origEndMs) {
            send('resizeEvent', { eventId: d.id, end });
          }
        } else {
          const from = Math.min(d.fromMin, d.toMin);
          const to = Math.max(d.fromMin, d.toMin);
          if (to - from >= SNAP_MIN) {
            create.open({ start: d.dayStart + from * MS_MIN, end: d.dayStart + to * MS_MIN, allDay: false });
          }
        }
      },

      onCancel: () => {
        // The pointer went away without a release, e.g. because a native gesture took the pointer
        // over. Nothing is written back and the grid shows the event where it was.
        dragRef.current = null;
        setDrag(null);
      },
    });
  };

  const startMove = (e: React.PointerEvent, ev: Ev, dayStart: number) => {
    if (!editable || !ev.movable) {
      return;
    }
    e.stopPropagation();
    const { min } = pointerToDayMin(e.clientX, e.clientY);
    const dur = (ev.end - ev.start) / MS_MIN;
    beginDrag(e, {
      mode: 'move', id: ev.id, grabMin: min - minutesOfDay(ev.start), dur, dayStart,
      startMin: minutesOfDay(ev.start), origStartMs: ev.start,
    }, 'pointer');
  };

  const startResize = (e: React.PointerEvent, ev: Ev, dayStart: number) => {
    if (!editable || !ev.resizable) {
      return;
    }
    e.stopPropagation();
    beginDrag(e, {
      mode: 'resize', id: ev.id, dayStart, startMin: minutesOfDay(ev.start),
      endMin: minutesOfDay(ev.end), origEndMs: ev.end,
    }, 'ns-resize');
  };

  const startCreate = (e: React.PointerEvent, dayStart: number) => {
    if (!editable || e.button !== 0) {
      return;
    }
    const { min } = pointerToDayMin(e.clientX, e.clientY);
    beginDrag(e, { mode: 'create', dayStart, fromMin: snap(min), toMin: snap(min) });
  };

  const hours = Array.from({ length: 24 }, (_, h) => h);

  // A dragged event is laid out at its dragged position, so its bar follows the pointer across
  // day columns instead of staying in the column it started in.
  const displayEvents = useMemo(() => {
    if (drag === null || !('id' in drag)) {
      return events;
    }
    const d = drag;
    return events.map((ev) => {
      if (ev.id !== d.id) {
        return ev;
      }
      if (d.mode === 'move') {
        const start = d.dayStart + d.startMin * MS_MIN;
        return { ...ev, start, end: start + d.dur * MS_MIN };
      }
      return { ...ev, end: d.dayStart + d.endMin * MS_MIN };
    });
  }, [events, drag]);

  const timedByDay = useMemo(() => {
    return days.map((day) =>
      layoutDay(
        displayEvents.filter((ev) => !ev.allDay && ev.start < day + MS_DAY && ev.end > day)
      )
    );
  }, [days, displayEvents]);

  const allDayByDay = useMemo(() => {
    return days.map((day) => displayEvents.filter((ev) => ev.allDay && ev.start < day + MS_DAY && ev.end > day));
  }, [days, displayEvents]);

  // Each list box holds its events day after day, a day's in the order of time (layoutDay): the
  // order the arrows follow.
  const timedOffsets = offsetsOf(timedByDay);
  const timedOrder = timedByDay.flat();
  const timedRoving = useRovingFocus(
    timedListRef, TIMED_OPTIONS, timedOrder.length, timedOrder.findIndex((p) => p.ev.selected), { linear: true });
  const allDayOffsets = offsetsOf(allDayByDay);
  const allDayOrder = allDayByDay.flat();
  const allDayRoving = useRovingFocus(
    allDayListRef, ALL_DAY_OPTIONS, allDayOrder.length, allDayOrder.findIndex((ev) => ev.selected), { linear: true });

  const workTop = dayStartHour * HOUR_HEIGHT;
  const workBot = dayEndHour * HOUR_HEIGHT;

  return (
    <div className="tl-calendar__time-grid">
      <div className="tl-calendar__head">
        <div className="tl-calendar__gutter" />
        {days.map((day) => {
          const weekend = nonWorkingDays.includes(new Date(day).getDay());
          const today = isSameDay(day, now);
          return (
            <button
              key={day}
              type="button"
              className="tl-calendar__day-head"
              aria-label={fullDate(locale, day)}
              aria-current={today ? 'date' : undefined}
              data-tl-state={stateAttr(weekend && 'nonworking')}
              onClick={() => send('goto', { date: day, granularity: 'DAY' })}
            >
              <span className="tl-calendar__weekday tl-type-label">{fmt(locale, { weekday: 'short' }, day)}</span>
              <span className="tl-calendar__day-number tl-type-heading-sm">{new Date(day).getDate()}</span>
            </button>
          );
        })}
      </div>

      <div className="tl-calendar__all-day">
        <div className="tl-calendar__gutter tl-calendar__all-day-label tl-type-label">{i18n['js.calendar.allDay']}</div>
        <div
          ref={allDayListRef}
          className="tl-calendar__events"
          role="listbox"
          aria-label={i18n['js.calendar.events'] + ', ' + i18n['js.calendar.allDay']}
          onKeyDown={allDayRoving.onKeyDown}
          onFocus={allDayRoving.onFocus}
        >
          {days.map((day, di) => (
            <div
              key={day}
              className="tl-calendar__all-day-cell"
              onClick={(e) => {
                if (editable && !isInteractiveWithin(e.target, e.currentTarget)) {
                  create.open({ start: day, end: day + MS_DAY, allDay: true });
                }
              }}
            >
              {create.pending && create.pending.allDay && create.pending.start === day && (
                <CreateStub
                  placeholder={i18n['js.calendar.newEventTitle']}
                  onCommit={create.commit}
                  onDiscard={create.discard}
                />
              )}
              {allDayByDay[di].map((ev, k) => (
                <div
                  key={ev.id}
                  className="tl-calendar__event tl-type-label"
                  {...filledProps(ctx.paint(ev))}
                  {...optionProps(ctx, ev, allDayRoving.tabIndexOf(allDayOffsets[di] + k))}
                >
                  {ev.title}
                </div>
              ))}
            </div>
          ))}
        </div>
      </div>

      <div className="tl-calendar__scroll" ref={scrollRef}>
        <div className="tl-calendar__time-body" style={{ height: 24 * HOUR_HEIGHT }}>
          <div className="tl-calendar__hours">
            {hours.map((h) => (
              <div key={h} className="tl-calendar__hour tl-type-label" style={{ top: h * HOUR_HEIGHT }}>
                {h === 0 ? '' : fmt(locale, { hour: 'numeric' }, startOfDay(rangeStart) + h * MS_HOUR)}
              </div>
            ))}
          </div>
          <div className="tl-calendar__columns" ref={colsRef} style={{ gridTemplateColumns: `repeat(${days.length}, 1fr)` }}>
            <div
              ref={timedListRef}
              className="tl-calendar__events"
              role="listbox"
              aria-label={i18n['js.calendar.events']}
              onKeyDown={timedRoving.onKeyDown}
              onFocus={timedRoving.onFocus}
            >
              {days.map((day, di) => {
                const weekend = nonWorkingDays.includes(new Date(day).getDay());
                const preview = drag && (('dayStart' in drag && drag.dayStart === day) || false) ? drag : null;
                return (
                  <div
                    key={day}
                    className="tl-calendar__column"
                    data-tl-state={stateAttr(weekend && 'nonworking')}
                    onPointerDown={(e) => startCreate(e, day)}
                  >
                    {/* The working time lies under the hour lines; a non-working day has none. */}
                    {!weekend && (
                      <div className="tl-calendar__work" style={{ top: workTop, height: workBot - workTop }} />
                    )}
                    {hours.map((h) => (
                      <div key={h} className="tl-calendar__hour-line" style={{ top: h * HOUR_HEIGHT }} />
                    ))}

                    {isSameDay(day, tick) && (
                      <div className="tl-calendar__now" style={{ top: (minutesOfDay(Date.now()) / 60) * HOUR_HEIGHT }} />
                    )}

                    {timedByDay[di].map((p, k) => {
                      const dragging = drag !== null && 'id' in drag && drag.id === p.ev.id;
                      const top = (p.topMin / 60) * HOUR_HEIGHT;
                      const height = ((p.botMin - p.topMin) / 60) * HOUR_HEIGHT;
                      const widthPct = 100 / p.cols;
                      return (
                        <div
                          key={p.ev.id}
                          className="tl-calendar__event tl-type-label"
                          data-tl-state={stateAttr(dragging && 'dragging')}
                          {...filledProps(ctx.paint(p.ev), {
                            top,
                            height,
                            left: `${p.col * widthPct}%`,
                            width: `calc(${widthPct}% - 2px)`,
                          })}
                          {...optionProps(ctx, p.ev, timedRoving.tabIndexOf(timedOffsets[di] + k))}
                          onPointerDown={(e) => startMove(e, p.ev, day)}
                        >
                          <span className="tl-calendar__event-time tl-type-label">{timeRange(locale, p.ev)}</span>
                          <span className="tl-calendar__event-title tl-type-label-strong">{p.ev.title}</span>
                          {editable && p.ev.resizable && (
                            <span className="tl-calendar__resize" onPointerDown={(e) => startResize(e, p.ev, day)} />
                          )}
                        </div>
                      );
                    })}

                    {create.pending && !create.pending.allDay && startOfDay(create.pending.start) === day && (
                      <CreateStub
                        placeholder={i18n['js.calendar.newEventTitle']}
                        style={{
                          top: (minutesOfDay(create.pending.start) / 60) * HOUR_HEIGHT,
                          height: ((create.pending.end - create.pending.start) / MS_MIN / 60) * HOUR_HEIGHT,
                          left: 0,
                          width: 'calc(100% - 2px)',
                        }}
                        onCommit={create.commit}
                        onDiscard={create.discard}
                      />
                    )}

                    {preview && preview.mode === 'create' && (
                      <div
                        className="tl-calendar__event"
                        data-tl-state="preview"
                        style={{
                          top: (Math.min(preview.fromMin, preview.toMin) / 60) * HOUR_HEIGHT,
                          height: (Math.abs(preview.toMin - preview.fromMin) / 60) * HOUR_HEIGHT,
                          left: 0,
                          width: 'calc(100% - 2px)',
                        }}
                      />
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

// --- Month grid ---------------------------------------------------------

const MAX_CHIPS = 3;

/** An event as the month shows it in a week: a filled bar over its days, or a chip on its day. */
interface MonthEntry {
  ev: Ev;
  bar: boolean;
  placement: React.CSSProperties;
}

/** The "+n more" of a day of a week. */
interface MoreEntry {
  day: number;
  count: number;
  placement: React.CSSProperties;
}

const MonthGrid: React.FC<{ ctx: Ctx; rangeStart: number; anchorMonth: number }> = ({ ctx, rangeStart, anchorMonth }) => {
  const { events, locale, nonWorkingDays, send, editable, now, i18n } = ctx;

  const create = usePendingCreate(send);
  const listRef = useRef<HTMLDivElement>(null);

  // The event dragged from this month, and the day it is dragged over.
  const [dragged, setDragged] = useState<string | null>(null);
  const [dropDay, setDropDay] = useState<number | null>(null);

  const weeks = useMemo(() => {
    const w: number[][] = [];
    for (let i = 0; i < 6; i++) {
      const row: number[] = [];
      for (let j = 0; j < 7; j++) {
        row.push(addDays(rangeStart, i * 7 + j));
      }
      w.push(row);
    }
    return w;
  }, [rangeStart]);

  // The events of each week: the bars over several days, then per day the chips of the timed events
  // and the "+n more" of those not shown, placed explicitly. In the list box they stand in the
  // order of time, the order the arrows follow.
  const layout = useMemo(() => weeks.map((row) => {
    const rowStart = row[0];
    const rowEnd = addDays(rowStart, 7);
    const spanning = events
      .filter((ev) => (ev.allDay || ev.end - ev.start >= MS_DAY) && ev.start < rowEnd && ev.end > rowStart)
      .sort((a, b) => a.start - b.start)
      .slice(0, 3);
    const barRows = spanning.length;
    const entries: MonthEntry[] = spanning.map((ev, si) => {
      const from = Math.max(0, Math.floor((startOfDay(Math.max(ev.start, rowStart)) - rowStart) / MS_DAY));
      const toExcl = Math.min(7, Math.ceil((ev.end - rowStart) / MS_DAY));
      return {
        ev,
        bar: true,
        placement: { gridColumn: `${from + 1} / ${Math.max(from + 1, toExcl) + 1}`, gridRow: si + 1 },
      };
    });
    const more: MoreEntry[] = [];
    row.forEach((day, ci) => {
      const timed = events
        .filter((ev) => !ev.allDay && ev.end - ev.start < MS_DAY && isSameDay(ev.start, day))
        .sort((a, b) => a.start - b.start);
      const shown = timed.slice(0, MAX_CHIPS);
      shown.forEach((ev, k) => entries.push({ ev, bar: false, placement: { gridColumn: ci + 1, gridRow: barRows + 1 + k } }));
      if (timed.length > shown.length) {
        more.push({
          day, count: timed.length - shown.length, placement: { gridColumn: ci + 1, gridRow: barRows + 1 + shown.length },
        });
      }
    });
    entries.sort((a, b) => a.ev.start - b.ev.start);
    return { entries, more };
  }), [weeks, events]);

  const offsets = offsetsOf(layout.map((week) => week.entries));
  const order = layout.flatMap((week) => week.entries);
  const roving = useRovingFocus(listRef, MONTH_OPTIONS, order.length, order.findIndex((entry) => entry.ev.selected));

  const draggedEvent = dragged === null ? undefined : events.find((ev) => ev.id === dragged);

  const startDrag = (e: React.DragEvent, ev: Ev) => {
    e.dataTransfer.setData('text/plain', ev.id);
    setDragged(ev.id);
  };

  const endDrag = () => {
    setDragged(null);
    setDropDay(null);
  };

  const onDragOverDay = (e: React.DragEvent, day: number) => {
    e.preventDefault();
    // Marked only for an event of this month that may be moved: the drop moves nothing else.
    if (editable && draggedEvent?.movable && dropDay !== day) {
      setDropDay(day);
    }
  };

  const onDragLeaveDay = (e: React.DragEvent, day: number) => {
    if (!e.currentTarget.contains(e.relatedTarget as Node | null) && dropDay === day) {
      setDropDay(null);
    }
  };

  const onDropDay = (e: React.DragEvent, day: number) => {
    e.preventDefault();
    // Ended here as well: the event may move to another week before its drag end arrives.
    endDrag();
    const id = e.dataTransfer.getData('text/plain');
    const ev = events.find((x) => x.id === id);
    if (!ev || !editable || !ev.movable) {
      return;
    }
    const delta = day - startOfDay(ev.start);
    send('moveEvent', { eventId: id, start: ev.start + delta, end: ev.end + delta });
  };

  return (
    <div className="tl-calendar__month">
      <div className="tl-calendar__month-head">
        {weeks[0].map((day) => (
          <div key={day} className="tl-calendar__month-weekday tl-type-label">{fmt(locale, { weekday: 'short' }, day)}</div>
        ))}
      </div>
      <div className="tl-calendar__month-body">
        <div
          ref={listRef}
          className="tl-calendar__events"
          role="listbox"
          aria-label={i18n['js.calendar.events']}
          onKeyDown={roving.onKeyDown}
          onFocus={roving.onFocus}
        >
          {weeks.map((row, ri) => (
            <div key={ri} className="tl-calendar__week">
              {/* Base layer: full-height day cells providing borders, backgrounds and day numbers. */}
              <div className="tl-calendar__days">
                {row.map((day) => {
                  const inMonth = new Date(day).getMonth() === new Date(anchorMonth).getMonth();
                  const weekend = nonWorkingDays.includes(new Date(day).getDay());
                  const today = isSameDay(day, now);
                  return (
                    <div
                      key={day}
                      className="tl-calendar__day"
                      aria-current={today ? 'date' : undefined}
                      data-tl-state={stateAttr(!inMonth && 'other', weekend && 'nonworking', dropDay === day && 'drop-onto')}
                      onDragOver={(e) => onDragOverDay(e, day)}
                      onDragLeave={(e) => onDragLeaveDay(e, day)}
                      onDrop={(e) => onDropDay(e, day)}
                      onClick={(e) => {
                        if (editable && !isInteractiveWithin(e.target, e.currentTarget)) {
                          create.open({ start: day, end: day + MS_DAY, allDay: true });
                        }
                      }}
                    >
                      <button
                        type="button"
                        className="tl-calendar__day-number tl-type-heading-sm"
                        aria-label={fullDate(locale, day)}
                        onClick={() => send('goto', { date: day, granularity: 'DAY' })}
                      >
                        {new Date(day).getDate()}
                      </button>
                      {create.pending && create.pending.start === day && (
                        <CreateStub
                          placeholder={i18n['js.calendar.newEventTitle']}
                          onCommit={create.commit}
                          onDiscard={create.discard}
                        />
                      )}
                    </div>
                  );
                })}
              </div>
              {/* Overlay layer: multi-day bars and per-day timed chips, placed explicitly. */}
              <div className="tl-calendar__overlay">
                {layout[ri].entries.map(({ ev, bar, placement }, k) => {
                  const paint = ctx.paint(ev);
                  const common = {
                    ...optionProps(ctx, ev, roving.tabIndexOf(offsets[ri] + k)),
                    'data-tl-state': stateAttr(dragged === ev.id && 'dragging'),
                    draggable: editable && ev.movable,
                    onDragStart: (e: React.DragEvent) => startDrag(e, ev),
                    onDragEnd: endDrag,
                  };
                  return bar ? (
                    <div key={ev.id} className="tl-calendar__event tl-type-label" {...filledProps(paint, placement)} {...common}>
                      {ev.title}
                    </div>
                  ) : (
                    <div key={ev.id} className="tl-calendar__chip tl-type-label" style={placement} {...common}>
                      <span className="tl-calendar__chip-dot" {...dotProps(paint)} />
                      <span className="tl-calendar__event-time tl-type-label">
                        {fmt(locale, { hour: 'numeric', minute: '2-digit' }, ev.start)}
                      </span>
                      <span className="tl-calendar__event-title tl-type-label">{ev.title}</span>
                    </div>
                  );
                })}
                {layout[ri].more.map(({ day, count, placement }) => {
                  const text = '+' + count + ' ' + i18n['js.calendar.more'];
                  return (
                    <button
                      key={'more-' + day}
                      type="button"
                      className="tl-calendar__more tl-type-label"
                      style={placement}
                      aria-label={text + ', ' + fullDate(locale, day)}
                      onClick={() => send('goto', { date: day, granularity: 'DAY' })}
                    >
                      {text}
                    </button>
                  );
                })}
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

// --- Year grid ----------------------------------------------------------

/**
 * A month of the year: its name, a button to the month, and its days as buttons in a grid with one
 * tab stop - today, in a month without today the first of the month. The arrows move from day to
 * day and from week to week, Enter opens the day.
 */
const MiniMonth: React.FC<{
  ctx: Ctx;
  monthStart: number;
  eventDays: Set<number>;
  weekdayHeader: string[];
}> = ({ ctx, monthStart, eventDays, weekdayHeader }) => {
  const { locale, firstDayOfWeek, nonWorkingDays, send, now } = ctx;
  const gridRef = useRef<HTMLDivElement>(null);

  const first = new Date(monthStart);
  const gridStart = startOfDay(addDays(monthStart, -(((first.getDay() - firstDayOfWeek + 7) % 7))));
  const cells = Array.from({ length: 42 }, (_, i) => addDays(gridStart, i));
  const inMonth = (day: number) => new Date(day).getMonth() === first.getMonth();
  const todayIndex = cells.findIndex((day) => inMonth(day) && isSameDay(day, now));
  const preferred = todayIndex >= 0 ? todayIndex : cells.findIndex((day) => isSameDay(day, monthStart));
  const roving = useRovingFocus(gridRef, MINI_DAYS, cells.length, preferred);
  const name = fmt(locale, { month: 'long', year: 'numeric' }, monthStart);

  return (
    <div className="tl-calendar__mini">
      <button
        type="button"
        className="tl-calendar__mini-title tl-type-label-strong"
        aria-label={name}
        onClick={() => send('goto', { date: monthStart, granularity: 'MONTH' })}
      >
        {fmt(locale, { month: 'long' }, monthStart)}
      </button>
      <div
        ref={gridRef}
        className="tl-calendar__mini-grid"
        role="group"
        aria-label={name}
        onKeyDown={roving.onKeyDown}
        onFocus={roving.onFocus}
      >
        {weekdayHeader.map((w, i) => (
          <span key={'h' + i} className="tl-calendar__mini-weekday tl-type-label" aria-hidden="true">{w}</span>
        ))}
        {cells.map((day, index) => {
          const weekend = nonWorkingDays.includes(new Date(day).getDay());
          const today = isSameDay(day, now);
          const has = eventDays.has(dayKey(day));
          return (
            <button
              key={day}
              type="button"
              className={'tl-calendar__mini-day ' + (has ? 'tl-type-label-strong' : 'tl-type-label')}
              aria-label={fullDate(locale, day)}
              aria-current={today ? 'date' : undefined}
              data-tl-state={stateAttr(!inMonth(day) && 'other', weekend && 'nonworking', has && 'event')}
              tabIndex={roving.tabIndexOf(index)}
              onClick={() => send('goto', { date: day, granularity: 'DAY' })}
            >
              {new Date(day).getDate()}
            </button>
          );
        })}
      </div>
    </div>
  );
};

const YearGrid: React.FC<{ ctx: Ctx; rangeStart: number }> = ({ ctx, rangeStart }) => {
  const { events, locale, firstDayOfWeek } = ctx;

  const eventDays = useMemo(() => {
    const s = new Set<number>();
    for (const ev of events) {
      let d = startOfDay(ev.start);
      const end = ev.end;
      while (d < end) {
        s.add(d);
        d = addDays(d, 1);
      }
    }
    return s;
  }, [events]);

  const year = new Date(rangeStart).getFullYear();
  const months = Array.from({ length: 12 }, (_, m) => new Date(year, m, 1).getTime());

  const weekdayHeader = useMemo(() => {
    const base = new Date(2023, 0, 1); // a Sunday
    return Array.from({ length: 7 }, (_, i) => {
      const d = new Date(base);
      d.setDate(base.getDate() + ((firstDayOfWeek + i) % 7));
      return new Intl.DateTimeFormat(locale, { weekday: 'narrow' }).format(d);
    });
  }, [locale, firstDayOfWeek]);

  return (
    <div className="tl-calendar__year">
      {months.map((monthStart) => (
        <MiniMonth key={monthStart} ctx={ctx} monthStart={monthStart} eventDays={eventDays} weekdayHeader={weekdayHeader} />
      ))}
    </div>
  );
};

// --- Root component -----------------------------------------------------

const TLCalendar: React.FC<TLCellProps> = ({ controlId }) => {
  const state = useTLState();
  const send = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const granularity = (state.granularity as Granularity) ?? 'WEEK';
  const rangeStart = (state.rangeStart as number) ?? Date.now();
  const anchor = (state.anchor as number) ?? rangeStart;
  const title = (state.title as string) ?? '';

  // The text readable on a server color, asked once per color and rendering: the candidates are
  // read from the theme in force, which may have changed since the last one.
  const readable = new Map<string, string | null>();
  const readableOn = (color: string) => {
    if (!readable.has(color)) {
      readable.set(color, textOn(color));
    }
    return readable.get(color)!;
  };

  const ctx: Ctx = {
    locale: (state.locale as string) ?? 'en',
    firstDayOfWeek: (state.firstDayOfWeek as number) ?? 0,
    nonWorkingDays: (state.nonWorkingDays as number[]) ?? [0, 6],
    dayStartHour: (state.dayStartHour as number) ?? 8,
    dayEndHour: (state.dayEndHour as number) ?? 18,
    editable: state.editable !== false,
    now: (state.now as number) ?? Date.now(),
    events: parseEvents(state.events),
    send,
    i18n,
    paint: (ev) => paintOf(ev, readableOn),
  };

  return (
    <div id={controlId} className={rootClassName(state, 'tl-calendar')}>
      <Toolbar title={title} granularity={granularity} i18n={i18n} send={send} />
      <div className="tl-calendar__body">
        {granularity === 'MONTH' ? (
          <MonthGrid ctx={ctx} rangeStart={rangeStart} anchorMonth={anchor} />
        ) : granularity === 'YEAR' ? (
          <YearGrid ctx={ctx} rangeStart={rangeStart} />
        ) : (
          <TimeGrid ctx={ctx} rangeStart={rangeStart} granularity={granularity} />
        )}
      </div>
    </div>
  );
};

export default TLCalendar;
