# React Calendar View — Design (#29421)

A green-field React **calendar view** control plus a declarative `<calendar>`
element for the `com.top_logic.layout.view` `.view.xml` composition layer. Outlook
is the rough guidance for the view catalog and interaction model.

This is a newly designed component. Nothing is carried over from existing UI — the
platform has no calendar view today.

## Scope (v1)

**Granularities**, built on exactly two renderers:

| Renderer | Views |
| --- | --- |
| Time-grid (hour axis, all-day row, current-time line, overlap columns) | Day, Work week, Week, and — for free — N-day |
| Month-grid (6×7 day cells, day-spanning all-day bars, "+N more" overflow) | Month, Year (12 mini-months, click to drill down) |

**Editing** is full pointer interaction: drag-move to reschedule, resize to change
duration, drag-select an empty slot to create.

**Non-event visuals** are config-driven: a working-hours band (start/end hour) and
non-working-day shading with locale defaults, plus the current-time indicator. A
pluggable per-day decorator seam is the extension point for holidays and similar
markings. There is no holiday backend — the platform has none today
(`DateUtil.isWorkingDay` is weekend-only and explicitly ignores holidays).

**Out of scope** (later tickets): recurrence (RRULE expansion / exception dates),
per-event timezone conversion, cross-resource lanes / room-booking side-by-side,
infinite-scroll month, and ICS export / printing.

## Architecture: two layers

The event binding is deliberately split so the control is usable from plain Java
without touching the configuration layer, while declarative usage adds an
expression-driven wrapper on top.

### Layer 1 — plain Java model (no configuration required)

- `CalendarEvent` — one rendered appointment:
  `getStart()`, `getEnd()`, `isAllDay()`, `getTitle()`, `getTooltip()`,
  `getCategory()` (drives the bar's theme color), `isMovable()`, `isResizable()`,
  and `getBusinessObject()` — the back-reference that lets the selection channel map
  a rendered bar to its domain object.
- `CalendarModel` — supplies events for a `[from, to)` interval and receives edits:
  `moveEvent(e, newStart, newEnd)`, `resizeEvent(e, newEnd)`, `createEvent(slot)`.

An application implements these two interfaces and hands the model to the React
control `CalendarViewControl` (the sibling of `TableViewControl` /
`react-src/controls/TLTableView.tsx`, extending
`com.top_logic.layout.react.control.ReactControl`).

### Layer 2 — declarative `CalendarElement`

A `@TagName("calendar")` `UIElement`, modeled directly on
`com.top_logic.layout.view.element.TableElement`:

- Binds a model channel of business objects. An `<event>` config sub-element — the
  `<column>` analogue — carries per-field TL-Script expressions: `start`, `end`,
  `all-day`, `title`, `tooltip`, `category`, `movable`, `resizable`.
- These compile (via `QueryExecutor`) into an
  `ExpressionCalendarEvent implements CalendarEvent` **wrapper per business object**,
  so Layer 1 sees a uniform `CalendarModel` and never learns that expressions exist.

## Channels and commands

- **model** (in) — business objects, already scoped to the visible range by whoever
  supplies them.
- **selection** (out, two-way) — the selected event, driving master/detail exactly
  like a table row. Reuses `TableElement`'s `ChannelRef` selection idiom.
- **visible-range** (out) — publishes the currently displayed `[from, to)` (a
  `com.top_logic.base.time` range value) so a supplier component lazily queries only
  what is shown. `TableElement`'s `RowSourceObserver` (re-run a `rows` expression
  when an input channel changes) is the template for the supplier side.
- Recordable `ViewCommand`s: navigation (`prev` / `next` / `today` / `goto`),
  `switch-granularity`, and the edit trio `moveEvent` / `resizeEvent` / `createSlot`.

**Declarative edit write-back.** The `<calendar>` config declares
`on-move` / `on-resize` / `on-create` TL-Script expressions that receive the object
and the new start/end and mutate the object; `CalendarElement` opens the transaction
around them (a value-listener has no ambient transaction).

## Reuse map

The calendar's rendering and interaction are all new. Only date math and the
established view-layer wiring are reused:

| Need | Existing infrastructure |
| --- | --- |
| Grid geometry (which day/week/month cells a view holds) | `com.top_logic.base.time.{DayRange, WeekRange, MonthRange, YearRange, TimeRangeIterator}`, `TimeRangeService` |
| Locale/TZ calendar, first-day-of-week, calendar-week, formatting | `com.top_logic.basic.time.CalendarUtil`; `TimeZones` / `UserTimeZoneDefault`; `com.top_logic.basic.Day` (all-day) |
| React control base + reference pair | `com.top_logic.layout.react.control.ReactControl`; `TableViewControl` ↔ `TLTableView.tsx` |
| UIElement + channel binding pattern | `com.top_logic.layout.view.element.TableElement` and its `Config` |

## Verification

Wire an editable example calendar into `com.top_logic.demo.react` (served at
`/view/`) and verify all five granularities plus drag-move / resize / create in the
browser.

## Build sequence

1. Layer 1 interfaces (`CalendarEvent`, `CalendarModel`) + `CalendarViewControl` +
   `react-src` time-grid renderer (Day/Week) with static events.
2. Month-grid renderer (Month/Year); granularity switching.
3. Pointer editing (drag-move, resize, drag-to-create) against the `CalendarModel`
   mutation methods.
4. `visible-range` and `selection` channels.
5. `CalendarElement` + `<event>` expressions + `ExpressionCalendarEvent` wrapper +
   write-back expressions.
6. Working-hours / non-working-day shading config + decorator seam.
7. Demo view in `com.top_logic.demo.react`; browser verification.

## Status

Implemented. Delivered classes:

- `com.top_logic.layout.react.control.calendar` — `CalendarEvent`, `CalendarModel`,
  `CalendarModelListener`, `DefaultCalendarEvent`, `DefaultCalendarModel`,
  `CalendarViewControl` (JS component `TLCalendar`).
- `react-src/controls/TLCalendar.tsx` — toolbar, time grid (day / work-week /
  week), month grid, year grid; drag-move / resize / drag-to-create; working-hours
  and non-working-day shading; current-time line. Client i18n keys `js.calendar.*`.
- `com.top_logic.layout.view.element` — `CalendarElement` (`<calendar>` tag) and
  `ExpressionCalendarModel` (the expression-derived event wrapper + transactional
  `on-move` / `on-resize` / `on-create` write-back).

Demo: `com.top_logic.demo.react` — `demo.calendar:Appointment` type,
`views/calendar.view.xml` (calendar + selection-driven detail form), and a
**Calendar** sidebar entry. Verified in the browser: all five granularities render
and navigate; create (click + drag), select, drag-move, resize and delete all work
end-to-end and persist; zero console errors.

Not yet built:

- **`visible-range` output channel** (agreed v1 scope) — the control computes and
  displays `[from, to)` internally but does not yet publish it to an output channel
  for lazy, range-scoped object loading; `CalendarElement` currently loads its
  objects eagerly (`all(…)`). This is the one remaining agreed-scope item; its
  consumer-facing value contract (what the channel carries and how an `<objects>`
  expression reads it) is an open design point.
- The pluggable per-day **decorator seam** (holiday marking etc.) — a later
  extension point, not agreed v1 scope.
