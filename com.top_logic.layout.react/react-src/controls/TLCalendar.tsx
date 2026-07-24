import { React, useTLState, useTLCommand, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useState, useRef, useCallback, useMemo, useEffect } = React;

/**
 * Outlook-style calendar view. Renders the {@code events} pushed by the server-side
 * {@code CalendarViewControl} at day / work-week / week / month / year granularity and reports
 * navigation, selection and drag edits back through commands.
 */

const I18N_KEYS = {
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
};

type Granularity = 'DAY' | 'WORK_WEEK' | 'WEEK' | 'MONTH' | 'YEAR';

interface Ev {
  id: string;
  start: number;
  end: number;
  allDay: boolean;
  title: string;
  tooltip?: string;
  category?: string;
  movable: boolean;
  resizable: boolean;
  selected: boolean;
}

const HOUR_HEIGHT = 44;
const SNAP_MIN = 15;
const MS_MIN = 60000;
const MS_HOUR = 3600000;
const MS_DAY = 86400000;
const PALETTE_SIZE = 8;

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

function categoryClass(category?: string): string {
  if (!category) {
    return 'tlCalEvent--default';
  }
  let hash = 0;
  for (let i = 0; i < category.length; i++) {
    hash = (hash * 31 + category.charCodeAt(i)) | 0;
  }
  return 'tlCalEvent--c' + (Math.abs(hash) % PALETTE_SIZE);
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
    category: e.category as string | undefined,
    movable: e.movable === true,
    resizable: e.resizable === true,
    selected: e.selected === true,
  }));
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
}

function fmt(locale: string, options: Intl.DateTimeFormatOptions, ms: number): string {
  return new Intl.DateTimeFormat(locale, options).format(new Date(ms));
}

function timeRange(locale: string, ev: Ev): string {
  const opts: Intl.DateTimeFormatOptions = { hour: 'numeric', minute: '2-digit' };
  return fmt(locale, opts, ev.start) + '–' + fmt(locale, opts, ev.end);
}

// --- Toolbar ------------------------------------------------------------

const GRANULARITIES: { key: Granularity; label: keyof typeof I18N_KEYS }[] = [
  { key: 'DAY', label: 'js.calendar.day' },
  { key: 'WORK_WEEK', label: 'js.calendar.workWeek' },
  { key: 'WEEK', label: 'js.calendar.week' },
  { key: 'MONTH', label: 'js.calendar.month' },
  { key: 'YEAR', label: 'js.calendar.year' },
];

const Toolbar: React.FC<{
  title: string;
  granularity: Granularity;
  i18n: Record<string, string>;
  send: Ctx['send'];
}> = ({ title, granularity, i18n, send }) => (
  <div className="tlCalToolbar">
    <div className="tlCalNav">
      <button className="tlCalBtn" onClick={() => send('navigate', { direction: 'TODAY' })}>
        {i18n['js.calendar.today']}
      </button>
      <button
        className="tlCalBtn tlCalBtn--icon"
        aria-label={i18n['js.calendar.previous']}
        onClick={() => send('navigate', { direction: 'PREV' })}
      >
        <span className="bi bi-chevron-left" />
      </button>
      <button
        className="tlCalBtn tlCalBtn--icon"
        aria-label={i18n['js.calendar.next']}
        onClick={() => send('navigate', { direction: 'NEXT' })}
      >
        <span className="bi bi-chevron-right" />
      </button>
    </div>
    <div className="tlCalTitle">{title}</div>
    <div className="tlCalGranularity">
      {GRANULARITIES.map((g) => (
        <button
          key={g.key}
          className={'tlCalBtn' + (g.key === granularity ? ' tlCalBtn--active' : '')}
          onClick={() => send('switchGranularity', { granularity: g.key })}
        >
          {i18n[g.label]}
        </button>
      ))}
    </div>
  </div>
);

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

type TimeDrag =
  | { mode: 'move'; id: string; grabMin: number; dur: number; dayStart: number; startMin: number }
  | { mode: 'resize'; id: string; dayStart: number; startMin: number; endMin: number }
  | { mode: 'create'; dayStart: number; fromMin: number; toMin: number };

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

  const colsRef = useRef<HTMLDivElement>(null);
  const scrollRef = useRef<HTMLDivElement>(null);
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

  useEffect(() => {
    if (!drag) {
      return;
    }
    const onMove = (e: PointerEvent) => {
      const d = dragRef.current;
      if (!d) {
        return;
      }
      const { dayIndex, min } = pointerToDayMin(e.clientX, e.clientY);
      if (d.mode === 'move') {
        setDrag({ ...d, dayStart: days[dayIndex], startMin: clamp(snap(min - d.grabMin), 0, 24 * 60 - d.dur) });
      } else if (d.mode === 'resize') {
        setDrag({ ...d, endMin: clamp(snap(min), d.startMin + SNAP_MIN, 24 * 60) });
      } else {
        setDrag({ ...d, toMin: clamp(snap(min), 0, 24 * 60) });
      }
    };
    const onUp = () => {
      const d = dragRef.current;
      setDrag(null);
      if (!d) {
        return;
      }
      if (d.mode === 'move') {
        const start = d.dayStart + d.startMin * MS_MIN;
        send('moveEvent', { eventId: d.id, start, end: start + d.dur * MS_MIN });
      } else if (d.mode === 'resize') {
        send('resizeEvent', { eventId: d.id, end: d.dayStart + d.endMin * MS_MIN });
      } else {
        const from = Math.min(d.fromMin, d.toMin);
        const to = Math.max(d.fromMin, d.toMin);
        if (to - from >= SNAP_MIN) {
          send('createSlot', { start: d.dayStart + from * MS_MIN, end: d.dayStart + to * MS_MIN, allDay: false });
        }
      }
    };
    window.addEventListener('pointermove', onMove);
    window.addEventListener('pointerup', onUp, { once: true });
    return () => {
      window.removeEventListener('pointermove', onMove);
      window.removeEventListener('pointerup', onUp);
    };
  }, [drag, days, pointerToDayMin, send]);

  const startMove = (e: React.PointerEvent, ev: Ev, dayStart: number) => {
    if (!editable || !ev.movable) {
      return;
    }
    e.stopPropagation();
    const { min } = pointerToDayMin(e.clientX, e.clientY);
    const dur = (ev.end - ev.start) / MS_MIN;
    setDrag({ mode: 'move', id: ev.id, grabMin: min - minutesOfDay(ev.start), dur, dayStart, startMin: minutesOfDay(ev.start) });
  };

  const startResize = (e: React.PointerEvent, ev: Ev, dayStart: number) => {
    if (!editable || !ev.resizable) {
      return;
    }
    e.stopPropagation();
    setDrag({ mode: 'resize', id: ev.id, dayStart, startMin: minutesOfDay(ev.start), endMin: minutesOfDay(ev.end) });
  };

  const startCreate = (e: React.PointerEvent, dayStart: number) => {
    if (!editable || e.button !== 0) {
      return;
    }
    const { min } = pointerToDayMin(e.clientX, e.clientY);
    setDrag({ mode: 'create', dayStart, fromMin: snap(min), toMin: snap(min) });
  };

  const hours = Array.from({ length: 24 }, (_, h) => h);

  const timedByDay = useMemo(() => {
    return days.map((day) =>
      layoutDay(
        events.filter((ev) => !ev.allDay && ev.start < day + MS_DAY && ev.end > day)
      )
    );
  }, [days, events]);

  const allDayByDay = useMemo(() => {
    return days.map((day) => events.filter((ev) => ev.allDay && ev.start < day + MS_DAY && ev.end > day));
  }, [days, events]);

  const workTop = dayStartHour * HOUR_HEIGHT;
  const workBot = dayEndHour * HOUR_HEIGHT;

  return (
    <div className="tlCalTimeGrid">
      <div className="tlCalTimeHeader">
        <div className="tlCalGutter" />
        {days.map((day) => {
          const weekend = nonWorkingDays.includes(new Date(day).getDay());
          const today = isSameDay(day, ctx.now);
          return (
            <div
              key={day}
              className={'tlCalDayHead' + (weekend ? ' tlCalDayHead--nonworking' : '') + (today ? ' tlCalDayHead--today' : '')}
              onClick={() => send('goto', { date: day, granularity: 'DAY' })}
            >
              <span className="tlCalDayName">{fmt(locale, { weekday: 'short' }, day)}</span>
              <span className="tlCalDayNum">{new Date(day).getDate()}</span>
            </div>
          );
        })}
      </div>

      <div className="tlCalAllDayRow">
        <div className="tlCalGutter tlCalAllDayLabel">{i18n['js.calendar.allDay']}</div>
        {days.map((day, di) => (
          <div
            key={day}
            className="tlCalAllDayCell"
            onClick={() => editable && send('createSlot', { start: day, end: day + MS_DAY, allDay: true })}
          >
            {allDayByDay[di].map((ev) => (
              <div
                key={ev.id}
                className={'tlCalAllDayEvent ' + categoryClass(ev.category) + (ev.selected ? ' tlCalEvent--selected' : '')}
                title={ev.tooltip}
                onClick={(e) => {
                  e.stopPropagation();
                  send('selectEvent', { eventId: ev.id });
                }}
              >
                {ev.title}
              </div>
            ))}
          </div>
        ))}
      </div>

      <div className="tlCalScroll" ref={scrollRef}>
        <div className="tlCalTimeBody" style={{ height: 24 * HOUR_HEIGHT }}>
          <div className="tlCalHourAxis">
            {hours.map((h) => (
              <div key={h} className="tlCalHourLabel" style={{ top: h * HOUR_HEIGHT }}>
                {h === 0 ? '' : fmt(locale, { hour: 'numeric' }, startOfDay(rangeStart) + h * MS_HOUR)}
              </div>
            ))}
          </div>
          <div className="tlCalColumns" ref={colsRef} style={{ gridTemplateColumns: `repeat(${days.length}, 1fr)` }}>
            {days.map((day, di) => {
              const weekend = nonWorkingDays.includes(new Date(day).getDay());
              const preview = drag && (('dayStart' in drag && drag.dayStart === day) || false) ? drag : null;
              return (
                <div
                  key={day}
                  className={'tlCalCol' + (weekend ? ' tlCalCol--nonworking' : '')}
                  onPointerDown={(e) => startCreate(e, day)}
                >
                  {hours.map((h) => (
                    <div key={h} className="tlCalHourLine" style={{ top: h * HOUR_HEIGHT }} />
                  ))}
                  <div className="tlCalWorkBand" style={{ top: workTop, height: workBot - workTop }} />

                  {isSameDay(day, tick) && (
                    <div className="tlCalNowLine" style={{ top: (minutesOfDay(Date.now()) / 60) * HOUR_HEIGHT }} />
                  )}

                  {timedByDay[di].map((p) => {
                    const dragging = drag && 'id' in drag && drag.id === p.ev.id;
                    let top = (p.topMin / 60) * HOUR_HEIGHT;
                    let height = ((p.botMin - p.topMin) / 60) * HOUR_HEIGHT;
                    if (dragging && drag) {
                      if (drag.mode === 'move' && drag.dayStart === day) {
                        top = (drag.startMin / 60) * HOUR_HEIGHT;
                      } else if (drag.mode === 'resize') {
                        height = ((drag.endMin - drag.startMin) / 60) * HOUR_HEIGHT;
                      }
                    }
                    const widthPct = 100 / p.cols;
                    return (
                      <div
                        key={p.ev.id}
                        className={
                          'tlCalEvent ' + categoryClass(p.ev.category) +
                          (p.ev.selected ? ' tlCalEvent--selected' : '') +
                          (dragging ? ' tlCalEvent--dragging' : '')
                        }
                        style={{ top, height, left: `${p.col * widthPct}%`, width: `calc(${widthPct}% - 2px)` }}
                        title={p.ev.tooltip}
                        onPointerDown={(e) => startMove(e, p.ev, day)}
                        onClick={(e) => {
                          e.stopPropagation();
                          send('selectEvent', { eventId: p.ev.id });
                        }}
                      >
                        <span className="tlCalEventTime">{timeRange(locale, p.ev)}</span>
                        <span className="tlCalEventTitle">{p.ev.title}</span>
                        {editable && p.ev.resizable && (
                          <span className="tlCalResizeHandle" onPointerDown={(e) => startResize(e, p.ev, day)} />
                        )}
                      </div>
                    );
                  })}

                  {preview && preview.mode === 'create' && (
                    <div
                      className="tlCalEvent tlCalEvent--preview"
                      style={{
                        top: (Math.min(preview.fromMin, preview.toMin) / 60) * HOUR_HEIGHT,
                        height: (Math.abs(preview.toMin - preview.fromMin) / 60) * HOUR_HEIGHT,
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
  );
};

// --- Month grid ---------------------------------------------------------

const MAX_CHIPS = 3;

const MonthGrid: React.FC<{ ctx: Ctx; rangeStart: number; anchorMonth: number }> = ({ ctx, rangeStart, anchorMonth }) => {
  const { events, locale, nonWorkingDays, send, editable, now } = ctx;

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

  const onDropDay = (e: React.DragEvent, day: number) => {
    e.preventDefault();
    const id = e.dataTransfer.getData('text/plain');
    const ev = events.find((x) => x.id === id);
    if (!ev || !editable || !ev.movable) {
      return;
    }
    const delta = day - startOfDay(ev.start);
    send('moveEvent', { eventId: id, start: ev.start + delta, end: ev.end + delta });
  };

  return (
    <div className="tlCalMonth">
      <div className="tlCalMonthHead">
        {weeks[0].map((day) => (
          <div key={day} className="tlCalMonthWeekday">{fmt(locale, { weekday: 'short' }, day)}</div>
        ))}
      </div>
      <div className="tlCalMonthBody">
        {weeks.map((row, ri) => {
          const rowStart = row[0];
          const rowEnd = addDays(rowStart, 7);
          const spanning = events
            .filter((ev) => (ev.allDay || ev.end - ev.start >= MS_DAY) && ev.start < rowEnd && ev.end > rowStart)
            .sort((a, b) => a.start - b.start)
            .slice(0, 3);
          const barRows = spanning.length;
          return (
            <div key={ri} className="tlCalMonthWeek">
              {/* Base layer: full-height day cells providing borders, backgrounds and day numbers. */}
              <div className="tlCalMonthDays">
                {row.map((day) => {
                  const inMonth = new Date(day).getMonth() === new Date(anchorMonth).getMonth();
                  const weekend = nonWorkingDays.includes(new Date(day).getDay());
                  const today = isSameDay(day, now);
                  return (
                    <div
                      key={day}
                      className={
                        'tlCalMonthCell' + (inMonth ? '' : ' tlCalMonthCell--other') +
                        (weekend ? ' tlCalMonthCell--nonworking' : '')
                      }
                      onDragOver={(e) => e.preventDefault()}
                      onDrop={(e) => onDropDay(e, day)}
                      onClick={() => editable && send('createSlot', { start: day, end: day + MS_DAY, allDay: true })}
                    >
                      <div
                        className={'tlCalMonthDayNum' + (today ? ' tlCalMonthDayNum--today' : '')}
                        onClick={(e) => {
                          e.stopPropagation();
                          send('goto', { date: day, granularity: 'DAY' });
                        }}
                      >
                        {new Date(day).getDate()}
                      </div>
                    </div>
                  );
                })}
              </div>
              {/* Overlay layer: multi-day spanning bars, then per-day timed chips, placed explicitly. */}
              <div className="tlCalMonthOverlay">
                {spanning.map((ev, si) => {
                  const from = Math.max(0, Math.floor((startOfDay(Math.max(ev.start, rowStart)) - rowStart) / MS_DAY));
                  const toExcl = Math.min(7, Math.ceil((ev.end - rowStart) / MS_DAY));
                  return (
                    <div
                      key={ev.id}
                      className={'tlCalMonthBar ' + categoryClass(ev.category) + (ev.selected ? ' tlCalEvent--selected' : '')}
                      style={{ gridColumn: `${from + 1} / ${Math.max(from + 1, toExcl) + 1}`, gridRow: si + 1 }}
                      draggable={editable && ev.movable}
                      onDragStart={(e) => e.dataTransfer.setData('text/plain', ev.id)}
                      title={ev.tooltip}
                      onClick={(e) => {
                        e.stopPropagation();
                        send('selectEvent', { eventId: ev.id });
                      }}
                    >
                      {ev.title}
                    </div>
                  );
                })}
                {row.map((day, ci) => {
                  const timed = events
                    .filter((ev) => !ev.allDay && ev.end - ev.start < MS_DAY && isSameDay(ev.start, day))
                    .sort((a, b) => a.start - b.start);
                  const shown = timed.slice(0, MAX_CHIPS);
                  const more = timed.length - shown.length;
                  return shown.map((ev, k) => (
                    <div
                      key={ev.id}
                      className={'tlCalChip ' + categoryClass(ev.category) + (ev.selected ? ' tlCalEvent--selected' : '')}
                      style={{ gridColumn: ci + 1, gridRow: barRows + 1 + k }}
                      draggable={editable && ev.movable}
                      onDragStart={(e) => e.dataTransfer.setData('text/plain', ev.id)}
                      title={ev.tooltip}
                      onClick={(e) => {
                        e.stopPropagation();
                        send('selectEvent', { eventId: ev.id });
                      }}
                    >
                      <span className="tlCalChipDot" />
                      <span className="tlCalChipTime">{fmt(locale, { hour: 'numeric', minute: '2-digit' }, ev.start)}</span>
                      <span className="tlCalChipTitle">{ev.title}</span>
                    </div>
                  )).concat(
                    more > 0
                      ? [
                          <div
                            key={'more-' + day}
                            className="tlCalMore"
                            style={{ gridColumn: ci + 1, gridRow: barRows + 1 + shown.length }}
                            onClick={() => send('goto', { date: day, granularity: 'DAY' })}
                          >
                            +{more} {ctx.i18n['js.calendar.more']}
                          </div>,
                        ]
                      : []
                  );
                })}
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};

// --- Year grid ----------------------------------------------------------

const YearGrid: React.FC<{ ctx: Ctx; rangeStart: number }> = ({ ctx, rangeStart }) => {
  const { events, locale, firstDayOfWeek, nonWorkingDays, send, now } = ctx;

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
    <div className="tlCalYear">
      {months.map((monthStart) => {
        const first = new Date(monthStart);
        const gridStart = startOfDay(addDays(monthStart, -(((first.getDay() - firstDayOfWeek + 7) % 7))));
        const cells = Array.from({ length: 42 }, (_, i) => addDays(gridStart, i));
        return (
          <div key={monthStart} className="tlCalMini">
            <div
              className="tlCalMiniTitle"
              onClick={() => send('goto', { date: monthStart, granularity: 'MONTH' })}
            >
              {fmt(locale, { month: 'long' }, monthStart)}
            </div>
            <div className="tlCalMiniGrid">
              {weekdayHeader.map((w, i) => (
                <div key={'h' + i} className="tlCalMiniWd">{w}</div>
              ))}
              {cells.map((day) => {
                const inMonth = new Date(day).getMonth() === first.getMonth();
                const weekend = nonWorkingDays.includes(new Date(day).getDay());
                const today = isSameDay(day, now);
                const has = eventDays.has(dayKey(day));
                return (
                  <div
                    key={day}
                    className={
                      'tlCalMiniDay' + (inMonth ? '' : ' tlCalMiniDay--other') +
                      (weekend ? ' tlCalMiniDay--nonworking' : '') + (today ? ' tlCalMiniDay--today' : '') +
                      (has ? ' tlCalMiniDay--event' : '')
                    }
                    onClick={() => send('goto', { date: day, granularity: 'DAY' })}
                  >
                    {new Date(day).getDate()}
                  </div>
                );
              })}
            </div>
          </div>
        );
      })}
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
  };

  return (
    <div id={controlId} className="tlCalendar">
      <Toolbar title={title} granularity={granularity} i18n={i18n} send={send} />
      <div className="tlCalBody">
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
