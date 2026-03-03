# React Controls Expansion Design

## Context

The `com.top_logic.layout.react` module currently has 22 React controls covering forms, media, files, data display, and basic layout. For building hybrid/responsive applications, there are gaps in layout primitives, app shell navigation, and overlay/feedback patterns.

## Goal

Add 10 new controls in three tiers (bottom-up) to enable modern, composable, responsive UIs.

## Existing Design Language

All new controls use the existing CSS design tokens:
- Spacing: `--spacing-01` through `--spacing-06`
- Colors: `--text-primary`, `--text-secondary`, `--layer-01`, `--layer-accent`, `--border-subtle`, etc.
- Typography: `--font-family`, `--body-compact-01-*`, `--heading-compact-02-*`
- Focus: `--focus`, `--focus-inset`
- Corner radius: `--corner-radius`, `--border-radius-02`

All controls import React from `'tl-react-bridge'` (never from `'react'` directly).

---

## Tier 1: Layout Primitives

### TLStack

Flexbox container with consistent spacing between children.

**State:**
- `direction`: `"column"` | `"row"` (default: `"column"`)
- `gap`: `"compact"` | `"default"` | `"loose"` (maps to `--spacing-02` / `--spacing-04` / `--spacing-06`)
- `align`: `"start"` | `"center"` | `"end"` | `"stretch"` (default: `"stretch"`)
- `wrap`: boolean (default: false)
- `children`: ChildDescriptor[]

**Behavior:** Pure layout, no interaction. Renders children in a flex container.

**CSS classes:** `.tlStack`, `.tlStack--row`, `.tlStack--column`, `.tlStack--gap-compact`, `.tlStack--gap-default`, `.tlStack--gap-loose`

### TLGrid

CSS Grid container with responsive columns.

**State:**
- `columns`: number (fixed column count)
- `minColumnWidth`: string (e.g. `"16rem"`, triggers `auto-fit`)
- `gap`: `"compact"` | `"default"` | `"loose"`
- `children`: ChildDescriptor[]

**Behavior:** When `minColumnWidth` is set, uses `grid-template-columns: repeat(auto-fit, minmax(minColumnWidth, 1fr))` for natural responsive reflow. When only `columns` is set, uses fixed `repeat(columns, 1fr)`.

**CSS classes:** `.tlGrid`, `.tlGrid--gap-compact`, `.tlGrid--gap-default`, `.tlGrid--gap-loose`

### TLCard

Elevated content container, lighter than TLPanel.

**State:**
- `title`: string | null
- `variant`: `"outlined"` | `"elevated"` (border vs shadow)
- `padding`: `"none"` | `"compact"` | `"default"`
- `headerActions`: ChildDescriptor[] (optional buttons in header)
- `child`: ChildDescriptor

**Behavior:** Renders a container with rounded corners. If `title` is set, shows a lightweight header (no minimize/maximize). `headerActions` places small buttons in the header.

**CSS classes:** `.tlCard`, `.tlCard--outlined`, `.tlCard--elevated`, `.tlCard__header`, `.tlCard__body`

---

## Tier 2: App Shell / Navigation

### TLAppBar

Top-level application bar (Material Design top app bar pattern).

**State:**
- `title`: string
- `leading`: ChildDescriptor | null (back arrow, hamburger, or custom)
- `actions`: ChildDescriptor[] (trailing action icons)
- `variant`: `"flat"` | `"elevated"`
- `color`: `"primary"` | `"surface"`

**Behavior:** Fixed-height bar (~3.5rem) with three zones: leading (left), title (center-left, flex: 1), actions (right). Does not own sidebar toggle logic - just renders whatever is in the `leading` slot.

**Color mapping:**
- `"primary"`: `--layer-accent` / `--layer-accent-text` (matches TLPanel header)
- `"surface"`: `--layer-01` / `--text-primary` with `border-bottom`

**CSS classes:** `.tlAppBar`, `.tlAppBar--primary`, `.tlAppBar--surface`, `.tlAppBar--elevated`, `.tlAppBar__leading`, `.tlAppBar__title`, `.tlAppBar__actions`

### TLBottomBar

Bottom navigation for mobile screens.

**State:**
- `items`: `{ id, label, icon, badge? }[]`
- `activeItemId`: string

**Behavior:** Fixed-height bar (~3.5rem) with 3-5 items. Each item: icon above label. Tap sends `selectItem` command. Active item highlighted. Badge styling reuses sidebar badge pattern.

**Commands:** `selectItem({ itemId })`

**CSS classes:** `.tlBottomBar`, `.tlBottomBar__item`, `.tlBottomBar__item--active`, `.tlBottomBar__icon`, `.tlBottomBar__label`, `.tlBottomBar__badge`

### TLBreadcrumb

Navigation trail showing current location.

**State:**
- `items`: `{ id, label }[]` (last = current page)

**Behavior:** Items separated by chevrons. All except last are clickable (send `navigate` command). Last item is plain text. On small screens, collapses middle items to `...`.

**Commands:** `navigate({ itemId })`

**CSS classes:** `.tlBreadcrumb`, `.tlBreadcrumb__item`, `.tlBreadcrumb__separator`, `.tlBreadcrumb__current`

---

## Tier 3: Overlay / Feedback Controls

### TLDialog

Modal overlay for confirmations and focused tasks.

**State:**
- `open`: boolean
- `title`: string
- `size`: `"small"` | `"medium"` | `"large"` (24rem / 32rem / 48rem)
- `closeOnBackdrop`: boolean (default: true)
- `actions`: ChildDescriptor[] (footer buttons)
- `child`: ChildDescriptor

**Behavior:** Centered overlay with semi-transparent backdrop. Focus trapped inside. Escape sends `close` command. Three zones: title bar, scrollable body, pinned footer.

**A11y:** `role="dialog"`, `aria-modal="true"`, `aria-labelledby` on title.

**Commands:** `close()`

**CSS classes:** `.tlDialog__backdrop`, `.tlDialog`, `.tlDialog--small`, `.tlDialog--medium`, `.tlDialog--large`, `.tlDialog__header`, `.tlDialog__body`, `.tlDialog__footer`

### TLDrawer

Slide-in panel from a screen edge.

**State:**
- `open`: boolean
- `position`: `"left"` | `"right"` | `"bottom"` (default: `"right"`)
- `size`: `"narrow"` | `"medium"` | `"wide"` (16rem / 24rem / 36rem)
- `title`: string | null
- `child`: ChildDescriptor

**Behavior:** Slides in via CSS `transform: translateX/translateY`. Optional title bar with close button. No backdrop, no focus trapping (main content stays interactive). Escape sends `close`.

**Commands:** `close()`

**CSS classes:** `.tlDrawer`, `.tlDrawer--left`, `.tlDrawer--right`, `.tlDrawer--bottom`, `.tlDrawer--open`, `.tlDrawer__header`, `.tlDrawer__body`

### TLSnackbar

Transient notification messages.

**State:**
- `message`: string
- `variant`: `"info"` | `"success"` | `"warning"` | `"error"`
- `action`: `{ label, commandName }` | null
- `duration`: number (ms, 0 = sticky, default: 5000)
- `visible`: boolean

**Behavior:** Bottom-center float. Auto-dismisses after `duration` ms (client timer, sends `dismiss`). One at a time. Slide-up enter, fade-out exit.

**Commands:** `dismiss()`, dynamic action command

**CSS classes:** `.tlSnackbar`, `.tlSnackbar--info`, `.tlSnackbar--success`, `.tlSnackbar--warning`, `.tlSnackbar--error`, `.tlSnackbar--visible`, `.tlSnackbar__message`, `.tlSnackbar__action`

### TLMenu

Popup menu triggered by an anchor element.

**State:**
- `open`: boolean
- `anchorId`: string
- `items`: `{ id, label, icon?, disabled?, type: "item" | "separator" }[]`

**Behavior:** Positioned below anchor via `getBoundingClientRect()` + fixed positioning. Flips if near screen edge. Keyboard: ArrowUp/Down, Enter, Escape. Close on outside click.

**Commands:** `selectItem({ itemId })`, `close()`

**A11y:** `role="menu"`, `role="menuitem"`, roving tabindex.

**CSS classes:** `.tlMenu`, `.tlMenu__item`, `.tlMenu__item--active`, `.tlMenu__item--disabled`, `.tlMenu__separator`, `.tlMenu__icon`, `.tlMenu__label`

---

## Implementation Order

1. **TLStack** - simplest, pure layout
2. **TLGrid** - pure layout, slightly more CSS
3. **TLCard** - first container with optional header
4. **TLAppBar** - composes with existing controls via slots
5. **TLBreadcrumb** - simple, self-contained
6. **TLBottomBar** - mirrors sidebar item/badge patterns
7. **TLDialog** - generalizes TLPhotoCapture overlay pattern
8. **TLDrawer** - similar to dialog but edge-attached
9. **TLSnackbar** - self-contained with timer logic
10. **TLMenu** - generalizes sidebar flyout pattern

Each control = one `.tsx` file + CSS block in `tlReactControls.css` + registration in `controls-entry.ts`.
