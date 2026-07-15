# React Controls Expansion Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Add 10 new React controls (layout primitives, app shell, overlays) to `com.top_logic.layout.react`.

**Architecture:** Each control follows the existing pattern: a `.tsx` React component + CSS in `tlReactControls.css` + registration in `controls-entry.ts` + a Java `ReactControl` subclass. Pure-layout controls (Stack, Grid) have no commands; interactive controls (Dialog, Menu, etc.) define `ControlCommand` subclasses. All controls import React from `'tl-react-bridge'`.

**Tech Stack:** React 18.2, TypeScript, Vite, Java 17, TopLogic `ReactControl` base class, SSE state sync.

---

## Key File Paths

| Purpose | Path |
|---------|------|
| React components | `com.top_logic.layout.react/react-src/controls/` |
| CSS | `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css` |
| Registration | `com.top_logic.layout.react/react-src/controls-entry.ts` |
| Java controls | `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/` |
| I18N constants | `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/I18NConstants.java` |

## Conventions (apply to every task)

- React imports: `import { React, useTLState, useTLCommand, TLChild } from 'tl-react-bridge';`
- Destructure hooks: `const { useCallback, useState, useEffect, useRef } = React;`
- CSS uses BEM: `.tlComponentName__element--modifier`
- CSS tokens: use `var(--spacing-*)`, `var(--text-*)`, `var(--border-*)`, `var(--font-family)`, etc.
- Java: SPDX header, member variables with `_` prefix, static `REACT_MODULE` constant.
- Java package for layout controls: `com.top_logic.layout.react.control.layout`
- Java package for nav controls: `com.top_logic.layout.react.control.nav` (new)
- Java package for overlay controls: `com.top_logic.layout.react.control.overlay` (new)

---

## Task 1: TLStack (React + CSS + Java)

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLStack.tsx`
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactStackControl.java`
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css` (append)
- Modify: `com.top_logic.layout.react/react-src/controls-entry.ts` (add import + register)

**Step 1: Write the React component**

Create `react-src/controls/TLStack.tsx`:

```tsx
import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * A flexbox container that arranges children with consistent spacing.
 *
 * State:
 * - direction: "column" | "row"  (default: "column")
 * - gap: "compact" | "default" | "loose"  (default: "default")
 * - align: "start" | "center" | "end" | "stretch"  (default: "stretch")
 * - wrap: boolean  (default: false)
 * - children: ChildDescriptor[]
 */
const TLStack: React.FC<TLCellProps> = () => {
  const state = useTLState();

  const direction = (state.direction as string) ?? 'column';
  const gap = (state.gap as string) ?? 'default';
  const align = (state.align as string) ?? 'stretch';
  const wrap = state.wrap === true;
  const children = (state.children as unknown[]) ?? [];

  const className = [
    'tlStack',
    `tlStack--${direction}`,
    `tlStack--gap-${gap}`,
    `tlStack--align-${align}`,
    wrap ? 'tlStack--wrap' : '',
  ].filter(Boolean).join(' ');

  return (
    <div className={className}>
      {children.map((child, i) => (
        <TLChild key={i} control={child} />
      ))}
    </div>
  );
};

export default TLStack;
```

**Step 2: Add CSS**

Append to `tlReactControls.css`:

```css
/* --- TLStack --------------------------------------------------------------- */

.tlStack {
  display: flex;
  font-family: var(--font-family);
}

.tlStack--column { flex-direction: column; }
.tlStack--row    { flex-direction: row; }

.tlStack--gap-compact { gap: var(--spacing-02); }
.tlStack--gap-default { gap: var(--spacing-04); }
.tlStack--gap-loose   { gap: var(--spacing-06); }

.tlStack--align-start   { align-items: flex-start; }
.tlStack--align-center  { align-items: center; }
.tlStack--align-end     { align-items: flex-end; }
.tlStack--align-stretch { align-items: stretch; }

.tlStack--wrap { flex-wrap: wrap; }
```

**Step 3: Register in controls-entry.ts**

Add import and register call:

```typescript
import TLStack from './controls/TLStack';
// ...
register('TLStack', TLStack);
```

**Step 4: Write the Java control**

Create `ReactStackControl.java`:

```java
/*
 * SPDX-FileCopyrightText: 2026 (c) Business Operation Systems GmbH <info@top-logic.com>
 *
 * SPDX-License-Identifier: AGPL-3.0-only OR LicenseRef-BOS-TopLogic-1.0
 */
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactControl;

/**
 * A {@link ReactControl} that renders a flexbox container via the {@code TLStack} React component.
 *
 * <p>State:</p>
 * <ul>
 * <li>{@code direction} - "column" or "row"</li>
 * <li>{@code gap} - "compact", "default", or "loose"</li>
 * <li>{@code align} - "start", "center", "end", or "stretch"</li>
 * <li>{@code wrap} - boolean</li>
 * <li>{@code children} - list of child control descriptors</li>
 * </ul>
 */
public class ReactStackControl extends ReactControl {

    private static final String REACT_MODULE = "TLStack";

    private static final String DIRECTION = "direction";
    private static final String GAP = "gap";
    private static final String ALIGN = "align";
    private static final String WRAP = "wrap";
    private static final String CHILDREN = "children";

    private final List<ReactControl> _children;

    /**
     * Creates a vertical stack with default gap.
     */
    public ReactStackControl(List<? extends ReactControl> children) {
        this("column", "default", "stretch", false, children);
    }

    /**
     * Creates a stack with full configuration.
     */
    public ReactStackControl(String direction, String gap, String align, boolean wrap,
            List<? extends ReactControl> children) {
        super(null, REACT_MODULE);
        _children = new ArrayList<>(children);
        putState(DIRECTION, direction);
        putState(GAP, gap);
        putState(ALIGN, align);
        putState(WRAP, Boolean.valueOf(wrap));
        putState(CHILDREN, _children);
    }

    @Override
    protected void cleanupChildren() {
        for (ReactControl child : _children) {
            child.cleanupTree();
        }
    }
}
```

**Step 5: Build and verify**

```bash
cd com.top_logic.layout.react && npm run build
cd com.top_logic.layout.react && mvn install -DskipTests=true
```

**Step 6: Commit**

```bash
git add com.top_logic.layout.react/react-src/controls/TLStack.tsx \
  com.top_logic.layout.react/react-src/controls-entry.ts \
  com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css \
  com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactStackControl.java \
  com.top_logic.layout.react/src/main/webapp/script/
git commit -m "Ticket #29109: Add TLStack layout primitive."
```

---

## Task 2: TLGrid (React + CSS + Java)

**Files:**
- Create: `com.top_logic.layout.react/react-src/controls/TLGrid.tsx`
- Create: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/layout/ReactGridControl.java`
- Modify: `tlReactControls.css` (append)
- Modify: `controls-entry.ts` (add import + register)

**Step 1: Write the React component**

Create `react-src/controls/TLGrid.tsx`:

```tsx
import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * A CSS Grid container with responsive column support.
 *
 * State:
 * - columns: number  (fixed column count, default: null)
 * - minColumnWidth: string  (e.g. "16rem", triggers auto-fit)
 * - gap: "compact" | "default" | "loose"  (default: "default")
 * - children: ChildDescriptor[]
 */
const TLGrid: React.FC<TLCellProps> = () => {
  const state = useTLState();

  const columns = state.columns as number | null;
  const minColumnWidth = state.minColumnWidth as string | null;
  const gap = (state.gap as string) ?? 'default';
  const children = (state.children as unknown[]) ?? [];

  const style: React.CSSProperties = {};
  if (minColumnWidth) {
    style.gridTemplateColumns = `repeat(auto-fit, minmax(${minColumnWidth}, 1fr))`;
  } else if (columns) {
    style.gridTemplateColumns = `repeat(${columns}, 1fr)`;
  }

  return (
    <div className={`tlGrid tlGrid--gap-${gap}`} style={style}>
      {children.map((child, i) => (
        <TLChild key={i} control={child} />
      ))}
    </div>
  );
};

export default TLGrid;
```

**Step 2: Add CSS**

```css
/* --- TLGrid ---------------------------------------------------------------- */

.tlGrid {
  display: grid;
  font-family: var(--font-family);
}

.tlGrid--gap-compact { gap: var(--spacing-02); }
.tlGrid--gap-default { gap: var(--spacing-04); }
.tlGrid--gap-loose   { gap: var(--spacing-06); }
```

**Step 3: Register + Java control**

Java class `ReactGridControl.java` in `control/layout/` package. Same pattern as stack but with `columns`, `minColumnWidth`, `gap`, `children` state keys.

```java
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactControl;

public class ReactGridControl extends ReactControl {

    private static final String REACT_MODULE = "TLGrid";
    private static final String COLUMNS = "columns";
    private static final String MIN_COLUMN_WIDTH = "minColumnWidth";
    private static final String GAP = "gap";
    private static final String CHILDREN = "children";

    private final List<ReactControl> _children;

    /** Creates a responsive grid with auto-fit columns. */
    public ReactGridControl(String minColumnWidth, String gap, List<? extends ReactControl> children) {
        super(null, REACT_MODULE);
        _children = new ArrayList<>(children);
        putState(MIN_COLUMN_WIDTH, minColumnWidth);
        putState(GAP, gap);
        putState(CHILDREN, _children);
    }

    /** Creates a fixed-column grid. */
    public ReactGridControl(int columns, String gap, List<? extends ReactControl> children) {
        super(null, REACT_MODULE);
        _children = new ArrayList<>(children);
        putState(COLUMNS, Integer.valueOf(columns));
        putState(GAP, gap);
        putState(CHILDREN, _children);
    }

    @Override
    protected void cleanupChildren() {
        for (ReactControl child : _children) {
            child.cleanupTree();
        }
    }
}
```

**Step 4: Build and commit**

```bash
cd com.top_logic.layout.react && npm run build && mvn install -DskipTests=true
git add <new files> && git commit -m "Ticket #29109: Add TLGrid layout primitive."
```

---

## Task 3: TLCard (React + CSS + Java)

**Files:**
- Create: `react-src/controls/TLCard.tsx`
- Create: `control/layout/ReactCardControl.java`
- Modify: `tlReactControls.css`, `controls-entry.ts`

**Step 1: Write the React component**

```tsx
import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * An elevated content container, lighter than TLPanel.
 *
 * State:
 * - title: string | null
 * - variant: "outlined" | "elevated"  (default: "outlined")
 * - padding: "none" | "compact" | "default"  (default: "default")
 * - headerActions: ChildDescriptor[]
 * - child: ChildDescriptor
 */
const TLCard: React.FC<TLCellProps> = () => {
  const state = useTLState();

  const title = state.title as string | null;
  const variant = (state.variant as string) ?? 'outlined';
  const padding = (state.padding as string) ?? 'default';
  const headerActions = (state.headerActions as unknown[]) ?? [];
  const child = state.child;

  const hasHeader = title != null || headerActions.length > 0;

  return (
    <div className={`tlCard tlCard--${variant}`}>
      {hasHeader && (
        <div className="tlCard__header">
          {title && <span className="tlCard__title">{title}</span>}
          {headerActions.length > 0 && (
            <div className="tlCard__headerActions">
              {headerActions.map((action, i) => (
                <TLChild key={i} control={action} />
              ))}
            </div>
          )}
        </div>
      )}
      <div className={`tlCard__body tlCard__body--pad-${padding}`}>
        <TLChild control={child} />
      </div>
    </div>
  );
};

export default TLCard;
```

**Step 2: Add CSS**

```css
/* --- TLCard ---------------------------------------------------------------- */

.tlCard {
  border-radius: var(--border-radius-02, 4px);
  font-family: var(--font-family);
  background: var(--layer-01);
  overflow: hidden;
}

.tlCard--outlined {
  border: 1px solid var(--border-subtle);
}

.tlCard--elevated {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12), 0 1px 2px rgba(0, 0, 0, 0.08);
}

.tlCard__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-03) var(--spacing-04);
  border-bottom: 1px solid var(--border-subtle);
}

.tlCard__title {
  font-weight: 600;
  font-size: var(--heading-compact-02-font-size);
  line-height: var(--heading-compact-02-line-height);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tlCard__headerActions {
  display: flex;
  align-items: center;
  gap: var(--spacing-01);
  flex-shrink: 0;
}

.tlCard__body--pad-none    { padding: 0; }
.tlCard__body--pad-compact { padding: var(--spacing-03); }
.tlCard__body--pad-default { padding: var(--spacing-04); }
```

**Step 3: Java control**

```java
package com.top_logic.layout.react.control.layout;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactControl;

public class ReactCardControl extends ReactControl {

    private static final String REACT_MODULE = "TLCard";
    private static final String TITLE = "title";
    private static final String VARIANT = "variant";
    private static final String PADDING = "padding";
    private static final String HEADER_ACTIONS = "headerActions";
    private static final String CHILD = "child";

    private final ReactControl _child;
    private final List<ReactControl> _headerActions;

    public ReactCardControl(String title, String variant, String padding,
            List<? extends ReactControl> headerActions, ReactControl child) {
        super(null, REACT_MODULE);
        _child = child;
        _headerActions = new ArrayList<>(headerActions);
        if (title != null) {
            putState(TITLE, title);
        }
        putState(VARIANT, variant);
        putState(PADDING, padding);
        putState(HEADER_ACTIONS, _headerActions);
        putState(CHILD, child);
    }

    /** Convenience: outlined card with default padding, no header actions. */
    public ReactCardControl(String title, ReactControl child) {
        this(title, "outlined", "default", List.of(), child);
    }

    @Override
    protected void cleanupChildren() {
        _child.cleanupTree();
        for (ReactControl action : _headerActions) {
            action.cleanupTree();
        }
    }
}
```

**Step 4: Build and commit**

```bash
cd com.top_logic.layout.react && npm run build && mvn install -DskipTests=true
git commit -m "Ticket #29109: Add TLCard layout container."
```

---

## Task 4: TLAppBar (React + CSS + Java)

**Files:**
- Create: `react-src/controls/TLAppBar.tsx`
- Create: `control/nav/ReactAppBarControl.java`
- Create: `control/nav/package-info.java`
- Modify: `tlReactControls.css`, `controls-entry.ts`

**Step 1: Write the React component**

```tsx
import { React, useTLState, TLChild } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

/**
 * A top-level application bar with leading slot, title, and trailing actions.
 *
 * State:
 * - title: string
 * - leading: ChildDescriptor | null
 * - actions: ChildDescriptor[]
 * - variant: "flat" | "elevated"  (default: "flat")
 * - color: "primary" | "surface"  (default: "primary")
 */
const TLAppBar: React.FC<TLCellProps> = () => {
  const state = useTLState();

  const title = (state.title as string) ?? '';
  const leading = state.leading;
  const actions = (state.actions as unknown[]) ?? [];
  const variant = (state.variant as string) ?? 'flat';
  const color = (state.color as string) ?? 'primary';

  const className = [
    'tlAppBar',
    `tlAppBar--${color}`,
    variant === 'elevated' ? 'tlAppBar--elevated' : '',
  ].filter(Boolean).join(' ');

  return (
    <header className={className}>
      {leading && (
        <div className="tlAppBar__leading">
          <TLChild control={leading} />
        </div>
      )}
      <h1 className="tlAppBar__title">{title}</h1>
      {actions.length > 0 && (
        <div className="tlAppBar__actions">
          {actions.map((action, i) => (
            <TLChild key={i} control={action} />
          ))}
        </div>
      )}
    </header>
  );
};

export default TLAppBar;
```

**Step 2: Add CSS**

```css
/* --- TLAppBar -------------------------------------------------------------- */

.tlAppBar {
  display: flex;
  align-items: center;
  height: 3.5rem;
  padding: 0 var(--spacing-03);
  gap: var(--spacing-02);
  font-family: var(--font-family);
  flex-shrink: 0;
  box-sizing: border-box;
}

.tlAppBar--primary {
  background: var(--layer-accent);
  color: var(--layer-accent-text);
}

.tlAppBar--surface {
  background: var(--layer-01);
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-subtle);
}

.tlAppBar--elevated {
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.12);
}

.tlAppBar__leading {
  flex-shrink: 0;
}

.tlAppBar__title {
  flex: 1;
  margin: 0;
  font-weight: bold;
  font-size: var(--heading-compact-02-font-size);
  line-height: var(--heading-compact-02-line-height);
  letter-spacing: var(--heading-compact-02-letter-spacing);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tlAppBar__actions {
  display: flex;
  align-items: center;
  gap: var(--spacing-01);
  flex-shrink: 0;
}
```

**Step 3: Java control**

Create `control/nav/package-info.java` and `ReactAppBarControl.java`:

```java
package com.top_logic.layout.react.control.nav;

import java.util.ArrayList;
import java.util.List;

import com.top_logic.layout.react.ReactControl;

public class ReactAppBarControl extends ReactControl {

    private static final String REACT_MODULE = "TLAppBar";
    private static final String TITLE = "title";
    private static final String LEADING = "leading";
    private static final String ACTIONS = "actions";
    private static final String VARIANT = "variant";
    private static final String COLOR = "color";

    private ReactControl _leading;
    private final List<ReactControl> _actions;

    public ReactAppBarControl(String title, String variant, String color,
            ReactControl leading, List<? extends ReactControl> actions) {
        super(null, REACT_MODULE);
        _leading = leading;
        _actions = new ArrayList<>(actions);
        putState(TITLE, title);
        putState(VARIANT, variant);
        putState(COLOR, color);
        if (leading != null) {
            putState(LEADING, leading);
        }
        putState(ACTIONS, _actions);
    }

    /** Convenience: primary flat app bar with no leading. */
    public ReactAppBarControl(String title, List<? extends ReactControl> actions) {
        this(title, "flat", "primary", null, actions);
    }

    public void setTitle(String title) {
        putState(TITLE, title);
    }

    @Override
    protected void cleanupChildren() {
        if (_leading != null) {
            _leading.cleanupTree();
        }
        for (ReactControl action : _actions) {
            action.cleanupTree();
        }
    }
}
```

**Step 4: Build and commit**

```bash
cd com.top_logic.layout.react && npm run build && mvn install -DskipTests=true
git commit -m "Ticket #29109: Add TLAppBar top application bar."
```

---

## Task 5: TLBreadcrumb (React + CSS + Java)

**Files:**
- Create: `react-src/controls/TLBreadcrumb.tsx`
- Create: `control/nav/ReactBreadcrumbControl.java`
- Modify: `tlReactControls.css`, `controls-entry.ts`

**Step 1: Write the React component**

```tsx
import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

interface BreadcrumbItem {
  id: string;
  label: string;
}

/**
 * A navigation trail showing the current location in a hierarchy.
 *
 * State:
 * - items: { id, label }[]  (last item = current page)
 */
const TLBreadcrumb: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const items = (state.items as BreadcrumbItem[]) ?? [];

  const handleNavigate = useCallback((itemId: string) => {
    sendCommand('navigate', { itemId });
  }, [sendCommand]);

  return (
    <nav className="tlBreadcrumb" aria-label="Breadcrumb">
      <ol className="tlBreadcrumb__list">
        {items.map((item, index) => {
          const isLast = index === items.length - 1;
          return (
            <li key={item.id} className="tlBreadcrumb__entry">
              {index > 0 && (
                <svg className="tlBreadcrumb__separator" viewBox="0 0 16 16"
                  width="16" height="16" aria-hidden="true">
                  <path d="M6 4l4 4-4 4" fill="none" stroke="currentColor"
                    strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
                </svg>
              )}
              {isLast ? (
                <span className="tlBreadcrumb__current" aria-current="page">{item.label}</span>
              ) : (
                <button
                  type="button"
                  className="tlBreadcrumb__item"
                  onClick={() => handleNavigate(item.id)}
                >
                  {item.label}
                </button>
              )}
            </li>
          );
        })}
      </ol>
    </nav>
  );
};

export default TLBreadcrumb;
```

**Step 2: Add CSS**

```css
/* --- TLBreadcrumb ---------------------------------------------------------- */

.tlBreadcrumb {
  font-family: var(--font-family);
  font-size: var(--body-compact-01-font-size);
}

.tlBreadcrumb__list {
  display: flex;
  align-items: center;
  list-style: none;
  margin: 0;
  padding: 0;
  gap: var(--spacing-01);
}

.tlBreadcrumb__entry {
  display: flex;
  align-items: center;
  gap: var(--spacing-01);
}

.tlBreadcrumb__separator {
  color: var(--text-secondary);
  flex-shrink: 0;
}

.tlBreadcrumb__item {
  border: none;
  background: transparent;
  padding: 0;
  color: var(--link-primary, var(--button-primary));
  font-family: var(--font-family);
  font-size: var(--body-compact-01-font-size);
  cursor: pointer;
}

.tlBreadcrumb__item:hover {
  text-decoration: underline;
}

.tlBreadcrumb__item:focus-visible {
  outline: 2px solid var(--focus);
  outline-offset: 2px;
}

.tlBreadcrumb__current {
  color: var(--text-primary);
}
```

**Step 3: Java control + I18N**

Add to `I18NConstants.java`:

```java
/** @en Breadcrumb item navigated. */
public static ResKey REACT_BREADCRUMB_NAVIGATE;
```

Create `ReactBreadcrumbControl.java`:

```java
package com.top_logic.layout.react.control.nav;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.top_logic.layout.Control;
import com.top_logic.layout.DisplayContext;
import com.top_logic.layout.basic.ControlCommand;
import com.top_logic.layout.react.I18NConstants;
import com.top_logic.layout.react.ReactControl;
import com.top_logic.tool.boundsec.HandlerResult;

public class ReactBreadcrumbControl extends ReactControl {

    private static final String REACT_MODULE = "TLBreadcrumb";
    private static final String ITEMS = "items";
    private static final String ITEM_ID_ARG = "itemId";

    private static final Map<String, ControlCommand> COMMANDS = createCommandMap(
        new NavigateCommand());

    private final Consumer<String> _navigateHandler;

    public ReactBreadcrumbControl(List<BreadcrumbEntry> items, Consumer<String> navigateHandler) {
        super(null, REACT_MODULE, COMMANDS);
        _navigateHandler = navigateHandler;
        updateItems(items);
    }

    public void updateItems(List<BreadcrumbEntry> items) {
        List<Map<String, String>> itemList = new ArrayList<>();
        for (BreadcrumbEntry entry : items) {
            Map<String, String> map = new HashMap<>();
            map.put("id", entry.id());
            map.put("label", entry.label());
            itemList.add(map);
        }
        putState(ITEMS, itemList);
    }

    public record BreadcrumbEntry(String id, String label) {}

    public static class NavigateCommand extends ControlCommand {
        public NavigateCommand() { super("navigate"); }

        @Override
        public com.top_logic.basic.util.ResKey getI18NKey() {
            return I18NConstants.REACT_BREADCRUMB_NAVIGATE;
        }

        @Override
        protected HandlerResult execute(DisplayContext context, Control control, Map<String, Object> arguments) {
            ReactBreadcrumbControl bc = (ReactBreadcrumbControl) control;
            String itemId = (String) arguments.get(ITEM_ID_ARG);
            bc._navigateHandler.accept(itemId);
            return HandlerResult.DEFAULT_RESULT;
        }
    }
}
```

**Step 4: Build and commit**

```bash
cd com.top_logic.layout.react && npm run build && mvn install -DskipTests=true
git commit -m "Ticket #29109: Add TLBreadcrumb navigation trail."
```

---

## Task 6: TLBottomBar (React + CSS + Java)

**Files:**
- Create: `react-src/controls/TLBottomBar.tsx`
- Create: `control/nav/ReactBottomBarControl.java`
- Modify: `tlReactControls.css`, `controls-entry.ts`, `I18NConstants.java`

**Step 1: Write the React component**

```tsx
import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback } = React;

interface BottomBarItem {
  id: string;
  label: string;
  icon: string;
  badge?: string;
}

/**
 * Bottom navigation bar for mobile screens.
 *
 * State:
 * - items: { id, label, icon, badge? }[]
 * - activeItemId: string
 */
const TLBottomBar: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const items = (state.items as BottomBarItem[]) ?? [];
  const activeItemId = state.activeItemId as string;

  const handleSelect = useCallback((itemId: string) => {
    if (itemId !== activeItemId) {
      sendCommand('selectItem', { itemId });
    }
  }, [sendCommand, activeItemId]);

  return (
    <nav className="tlBottomBar" aria-label="Bottom navigation">
      {items.map(item => {
        const isActive = item.id === activeItemId;
        return (
          <button
            key={item.id}
            type="button"
            className={'tlBottomBar__item' + (isActive ? ' tlBottomBar__item--active' : '')}
            onClick={() => handleSelect(item.id)}
            aria-current={isActive ? 'page' : undefined}
          >
            <span className="tlBottomBar__iconWrap">
              <i className={'tlBottomBar__icon ' + item.icon} aria-hidden="true" />
              {item.badge && <span className="tlBottomBar__badge">{item.badge}</span>}
            </span>
            <span className="tlBottomBar__label">{item.label}</span>
          </button>
        );
      })}
    </nav>
  );
};

export default TLBottomBar;
```

**Step 2: Add CSS**

```css
/* --- TLBottomBar ----------------------------------------------------------- */

.tlBottomBar {
  display: flex;
  align-items: stretch;
  height: 3.5rem;
  border-top: 1px solid var(--border-subtle);
  background: var(--layer-01);
  font-family: var(--font-family);
  flex-shrink: 0;
}

.tlBottomBar__item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.125rem;
  border: none;
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  padding: var(--spacing-02) 0;
  font-family: var(--font-family);
  min-width: 0;
}

.tlBottomBar__item:focus-visible {
  outline: 2px solid var(--focus);
  outline-offset: -2px;
}

button.tlBottomBar__item:hover {
  background: var(--layer-hover);
  color: var(--text-primary);
}

.tlBottomBar__item--active {
  color: var(--button-primary);
}

.tlBottomBar__iconWrap {
  position: relative;
  display: inline-flex;
}

.tlBottomBar__icon {
  font-size: 1.25rem;
}

.tlBottomBar__badge {
  position: absolute;
  top: -0.375rem;
  right: -0.5rem;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 0.875rem;
  height: 0.875rem;
  padding: 0 0.1875rem;
  border-radius: 0.4375rem;
  background: var(--button-primary);
  color: var(--text-on-color);
  font-size: 0.5625rem;
  font-weight: 600;
  line-height: 1;
}

.tlBottomBar__label {
  font-size: 0.6875rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  max-width: 100%;
}
```

**Step 3: Java control + I18N**

Add to `I18NConstants.java`:

```java
/** @en Bottom bar item selected. */
public static ResKey REACT_BOTTOM_BAR_SELECT;
```

Java control follows same pattern as sidebar's `selectItem` command.

**Step 4: Build and commit**

```bash
git commit -m "Ticket #29109: Add TLBottomBar mobile navigation."
```

---

## Task 7: TLDialog (React + CSS + Java)

**Files:**
- Create: `react-src/controls/TLDialog.tsx`
- Create: `control/overlay/ReactDialogControl.java`
- Create: `control/overlay/package-info.java`
- Modify: `tlReactControls.css`, `controls-entry.ts`, `I18NConstants.java`

**Step 1: Write the React component**

```tsx
import { React, useTLState, useTLCommand, TLChild, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect, useRef } = React;

const I18N_KEYS = {
  'js.dialog.close': 'Close',
};

/**
 * A modal dialog overlay.
 *
 * State:
 * - open: boolean
 * - title: string
 * - size: "small" | "medium" | "large"  (default: "medium")
 * - closeOnBackdrop: boolean  (default: true)
 * - actions: ChildDescriptor[]  (footer buttons)
 * - child: ChildDescriptor
 */
const TLDialog: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const open = state.open === true;
  const title = (state.title as string) ?? '';
  const size = (state.size as string) ?? 'medium';
  const closeOnBackdrop = state.closeOnBackdrop !== false;
  const actions = (state.actions as unknown[]) ?? [];
  const child = state.child;

  const dialogRef = useRef<HTMLDivElement>(null);

  const handleClose = useCallback(() => {
    sendCommand('close');
  }, [sendCommand]);

  const handleBackdropClick = useCallback((e: React.MouseEvent) => {
    if (closeOnBackdrop && e.target === e.currentTarget) {
      handleClose();
    }
  }, [closeOnBackdrop, handleClose]);

  // Escape key.
  useEffect(() => {
    if (!open) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') {
        handleClose();
      }
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [open, handleClose]);

  // Focus trap: focus the dialog when it opens.
  useEffect(() => {
    if (open && dialogRef.current) {
      dialogRef.current.focus();
    }
  }, [open]);

  if (!open) return null;

  const titleId = 'tlDialog-title';

  return (
    <div className="tlDialog__backdrop" onClick={handleBackdropClick}>
      <div
        className={`tlDialog tlDialog--${size}`}
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        ref={dialogRef}
        tabIndex={-1}
      >
        <div className="tlDialog__header">
          <span className="tlDialog__title" id={titleId}>{title}</span>
          <button
            type="button"
            className="tlDialog__closeBtn"
            onClick={handleClose}
            title={i18n['js.dialog.close']}
          >
            <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" strokeWidth="2"
                strokeLinecap="round" />
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" strokeWidth="2"
                strokeLinecap="round" />
            </svg>
          </button>
        </div>
        <div className="tlDialog__body">
          <TLChild control={child} />
        </div>
        {actions.length > 0 && (
          <div className="tlDialog__footer">
            {actions.map((action, i) => (
              <TLChild key={i} control={action} />
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default TLDialog;
```

**Step 2: Add CSS**

```css
/* --- TLDialog -------------------------------------------------------------- */

.tlDialog__backdrop {
  position: fixed;
  inset: 0;
  z-index: 10000;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.5);
}

.tlDialog {
  background: var(--layer-01);
  border-radius: var(--border-radius-02, 4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  max-height: 80vh;
  font-family: var(--font-family);
  outline: none;
}

.tlDialog--small  { width: 24rem; }
.tlDialog--medium { width: 32rem; }
.tlDialog--large  { width: 48rem; }

.tlDialog__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-04);
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.tlDialog__title {
  font-weight: bold;
  font-size: var(--heading-compact-02-font-size);
  line-height: var(--heading-compact-02-line-height);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tlDialog__closeBtn {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 2rem;
  height: 2rem;
  border: none;
  border-radius: var(--corner-radius);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
  flex-shrink: 0;
}

button.tlDialog__closeBtn:hover {
  background: var(--layer-hover);
  color: var(--text-primary);
}

button.tlDialog__closeBtn:focus-visible {
  outline: 2px solid var(--focus);
  outline-offset: -2px;
}

.tlDialog__body {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-04);
}

.tlDialog__footer {
  display: flex;
  justify-content: flex-end;
  gap: var(--spacing-03);
  padding: var(--spacing-03) var(--spacing-04);
  border-top: 1px solid var(--border-subtle);
  flex-shrink: 0;
}
```

**Step 3: Java control + I18N**

Add to `I18NConstants.java`:

```java
/** @en Dialog closed. */
public static ResKey REACT_DIALOG_CLOSE;

/** @en Close @de Schlie\u00dfen */
public static ResKey JS_DIALOG_CLOSE = ResKey.internalCreate("js.dialog.close");
```

Java control: `ReactDialogControl` with `open`, `title`, `size`, `closeOnBackdrop` state, single `CloseCommand`, and `open()`/`close()` methods that patch state.

**Step 4: Build and commit**

```bash
git commit -m "Ticket #29109: Add TLDialog modal overlay."
```

---

## Task 8: TLDrawer (React + CSS + Java)

**Files:**
- Create: `react-src/controls/TLDrawer.tsx`
- Create: `control/overlay/ReactDrawerControl.java`
- Modify: `tlReactControls.css`, `controls-entry.ts`, `I18NConstants.java`

**Step 1: Write the React component**

Similar to TLDialog but slides from edge. Key differences:
- Uses CSS `transform` with `transition` for slide animation.
- No backdrop (main content stays interactive).
- Position: left/right uses `translateX`, bottom uses `translateY`.
- Always renders the DOM (for smooth transition), but translates off-screen when closed.

```tsx
import { React, useTLState, useTLCommand, TLChild, useI18N } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect } = React;

const I18N_KEYS = {
  'js.drawer.close': 'Close',
};

const TLDrawer: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();
  const i18n = useI18N(I18N_KEYS);

  const open = state.open === true;
  const position = (state.position as string) ?? 'right';
  const size = (state.size as string) ?? 'medium';
  const title = state.title as string | null;
  const child = state.child;

  const handleClose = useCallback(() => {
    sendCommand('close');
  }, [sendCommand]);

  useEffect(() => {
    if (!open) return;
    const handleKeyDown = (e: KeyboardEvent) => {
      if (e.key === 'Escape') handleClose();
    };
    document.addEventListener('keydown', handleKeyDown);
    return () => document.removeEventListener('keydown', handleKeyDown);
  }, [open, handleClose]);

  const className = [
    'tlDrawer',
    `tlDrawer--${position}`,
    `tlDrawer--${size}`,
    open ? 'tlDrawer--open' : '',
  ].filter(Boolean).join(' ');

  return (
    <aside className={className} aria-hidden={!open}>
      {title !== null && (
        <div className="tlDrawer__header">
          <span className="tlDrawer__title">{title}</span>
          <button type="button" className="tlDrawer__closeBtn" onClick={handleClose}
            title={i18n['js.drawer.close']}>
            <svg viewBox="0 0 24 24" width="20" height="20" aria-hidden="true">
              <line x1="6" y1="6" x2="18" y2="18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
              <line x1="18" y1="6" x2="6" y2="18" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            </svg>
          </button>
        </div>
      )}
      <div className="tlDrawer__body">
        {child && <TLChild control={child} />}
      </div>
    </aside>
  );
};

export default TLDrawer;
```

**Step 2: Add CSS**

```css
/* --- TLDrawer -------------------------------------------------------------- */

.tlDrawer {
  position: fixed;
  z-index: 9000;
  background: var(--layer-01);
  box-shadow: -2px 0 8px rgba(0, 0, 0, 0.15);
  display: flex;
  flex-direction: column;
  transition: transform 0.25s ease;
  font-family: var(--font-family);
}

.tlDrawer--left, .tlDrawer--right { top: 0; bottom: 0; }
.tlDrawer--left   { left: 0;  transform: translateX(-100%); }
.tlDrawer--right  { right: 0; transform: translateX(100%); }
.tlDrawer--bottom { left: 0; right: 0; bottom: 0; transform: translateY(100%); }

.tlDrawer--left.tlDrawer--open   { transform: translateX(0); }
.tlDrawer--right.tlDrawer--open  { transform: translateX(0); }
.tlDrawer--bottom.tlDrawer--open { transform: translateY(0); }

.tlDrawer--narrow { width: 16rem; }
.tlDrawer--medium { width: 24rem; }
.tlDrawer--wide   { width: 36rem; }
.tlDrawer--bottom.tlDrawer--narrow { width: auto; height: 16rem; }
.tlDrawer--bottom.tlDrawer--medium { width: auto; height: 24rem; }
.tlDrawer--bottom.tlDrawer--wide   { width: auto; height: 36rem; }

.tlDrawer__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: var(--spacing-03) var(--spacing-04);
  border-bottom: 1px solid var(--border-subtle);
  flex-shrink: 0;
}

.tlDrawer__title {
  font-weight: 600;
  font-size: var(--heading-compact-02-font-size);
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.tlDrawer__closeBtn {
  display: inline-flex;
  justify-content: center;
  align-items: center;
  width: 2rem;
  height: 2rem;
  border: none;
  border-radius: var(--corner-radius);
  background: transparent;
  color: var(--text-secondary);
  cursor: pointer;
  padding: 0;
}

button.tlDrawer__closeBtn:hover {
  background: var(--layer-hover);
  color: var(--text-primary);
}

button.tlDrawer__closeBtn:focus-visible {
  outline: 2px solid var(--focus);
  outline-offset: -2px;
}

.tlDrawer__body {
  flex: 1;
  overflow-y: auto;
  padding: var(--spacing-04);
}
```

**Step 3: Java control + I18N, build, commit**

```bash
git commit -m "Ticket #29109: Add TLDrawer slide-in panel."
```

---

## Task 9: TLSnackbar (React + CSS + Java)

**Files:**
- Create: `react-src/controls/TLSnackbar.tsx`
- Create: `control/overlay/ReactSnackbarControl.java`
- Modify: `tlReactControls.css`, `controls-entry.ts`, `I18NConstants.java`

**Step 1: Write the React component**

Key behavior: client-side auto-dismiss timer that sends `dismiss` command to server.

```tsx
import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect, useState } = React;

/**
 * Transient notification message at bottom of screen.
 *
 * State:
 * - message: string
 * - variant: "info" | "success" | "warning" | "error"
 * - action: { label, commandName } | null
 * - duration: number  (ms, 0 = sticky)
 * - visible: boolean
 */
const TLSnackbar: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const message = (state.message as string) ?? '';
  const variant = (state.variant as string) ?? 'info';
  const action = state.action as { label: string; commandName: string } | null;
  const duration = (state.duration as number) ?? 5000;
  const visible = state.visible === true;

  const [exiting, setExiting] = useState(false);

  const handleDismiss = useCallback(() => {
    setExiting(true);
    setTimeout(() => {
      sendCommand('dismiss');
      setExiting(false);
    }, 200); // match fade-out animation
  }, [sendCommand]);

  const handleAction = useCallback(() => {
    if (action) {
      sendCommand(action.commandName);
    }
    handleDismiss();
  }, [sendCommand, action, handleDismiss]);

  // Auto-dismiss timer.
  useEffect(() => {
    if (!visible || duration === 0) return;
    const timer = setTimeout(handleDismiss, duration);
    return () => clearTimeout(timer);
  }, [visible, duration, handleDismiss]);

  if (!visible && !exiting) return null;

  return (
    <div className={`tlSnackbar tlSnackbar--${variant}${exiting ? ' tlSnackbar--exiting' : ''}`}
      role="status" aria-live="polite">
      <span className="tlSnackbar__message">{message}</span>
      {action && (
        <button type="button" className="tlSnackbar__action" onClick={handleAction}>
          {action.label}
        </button>
      )}
    </div>
  );
};

export default TLSnackbar;
```

**Step 2: Add CSS**

```css
/* --- TLSnackbar ------------------------------------------------------------ */

.tlSnackbar {
  position: fixed;
  bottom: var(--spacing-04);
  left: 50%;
  transform: translateX(-50%) translateY(0);
  z-index: 11000;
  display: flex;
  align-items: center;
  gap: var(--spacing-03);
  padding: var(--spacing-03) var(--spacing-04);
  border-radius: var(--border-radius-02, 4px);
  font-family: var(--font-family);
  font-size: var(--body-compact-01-font-size);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  animation: tlSnackbarSlideUp 0.2s ease;
  max-width: 40rem;
}

.tlSnackbar--exiting {
  animation: tlSnackbarFadeOut 0.2s ease forwards;
}

.tlSnackbar--info    { background: var(--background-inverse, #393939); color: var(--text-inverse, #fff); }
.tlSnackbar--success { background: var(--support-success, #198038); color: #fff; }
.tlSnackbar--warning { background: var(--support-warning, #f1c21b); color: #000; }
.tlSnackbar--error   { background: var(--support-error, #da1e28); color: #fff; }

.tlSnackbar__message {
  flex: 1;
}

.tlSnackbar__action {
  border: none;
  background: transparent;
  color: inherit;
  font-family: var(--font-family);
  font-size: var(--body-compact-01-font-size);
  font-weight: 600;
  cursor: pointer;
  text-decoration: underline;
  white-space: nowrap;
}

.tlSnackbar__action:focus-visible {
  outline: 2px solid currentColor;
  outline-offset: 2px;
}

@keyframes tlSnackbarSlideUp {
  from { transform: translateX(-50%) translateY(1rem); opacity: 0; }
  to   { transform: translateX(-50%) translateY(0);    opacity: 1; }
}

@keyframes tlSnackbarFadeOut {
  from { opacity: 1; }
  to   { opacity: 0; }
}
```

**Step 3: Java control + I18N, build, commit**

```bash
git commit -m "Ticket #29109: Add TLSnackbar transient notifications."
```

---

## Task 10: TLMenu (React + CSS + Java)

**Files:**
- Create: `react-src/controls/TLMenu.tsx`
- Create: `control/overlay/ReactMenuControl.java`
- Modify: `tlReactControls.css`, `controls-entry.ts`, `I18NConstants.java`

**Step 1: Write the React component**

Key behavior: positioned relative to anchor element, roving tabindex keyboard navigation (reusing sidebar flyout pattern), close on outside click.

```tsx
import { React, useTLState, useTLCommand } from 'tl-react-bridge';
import type { TLCellProps } from 'tl-react-bridge';

const { useCallback, useEffect, useRef, useState } = React;

interface MenuItem {
  id: string;
  label: string;
  icon?: string;
  disabled?: boolean;
  type: 'item' | 'separator';
}

/**
 * A popup menu triggered by an anchor element.
 *
 * State:
 * - open: boolean
 * - anchorId: string
 * - items: MenuItem[]
 */
const TLMenu: React.FC<TLCellProps> = () => {
  const state = useTLState();
  const sendCommand = useTLCommand();

  const open = state.open === true;
  const anchorId = state.anchorId as string;
  const items = (state.items as MenuItem[]) ?? [];

  const menuRef = useRef<HTMLDivElement>(null);
  const [position, setPosition] = useState<{ top: number; left: number }>({ top: 0, left: 0 });
  const [focusedIndex, setFocusedIndex] = useState(0);

  const focusableItems = items.filter(it => it.type === 'item' && !it.disabled);

  // Position relative to anchor.
  useEffect(() => {
    if (!open || !anchorId) return;
    const anchor = document.getElementById(anchorId);
    if (!anchor) return;
    const rect = anchor.getBoundingClientRect();
    const menuHeight = menuRef.current?.offsetHeight ?? 200;
    const menuWidth = menuRef.current?.offsetWidth ?? 200;

    let top = rect.bottom + 4;
    let left = rect.left;

    // Flip vertically if near bottom.
    if (top + menuHeight > window.innerHeight) {
      top = rect.top - menuHeight - 4;
    }
    // Flip horizontally if near right edge.
    if (left + menuWidth > window.innerWidth) {
      left = rect.right - menuWidth;
    }

    setPosition({ top, left });
    setFocusedIndex(0);
  }, [open, anchorId]);

  const handleClose = useCallback(() => {
    sendCommand('close');
  }, [sendCommand]);

  const handleSelect = useCallback((itemId: string) => {
    sendCommand('selectItem', { itemId });
  }, [sendCommand]);

  // Close on outside click.
  useEffect(() => {
    if (!open) return;
    const handleMouseDown = (e: MouseEvent) => {
      if (menuRef.current && !menuRef.current.contains(e.target as Node)) {
        handleClose();
      }
    };
    document.addEventListener('mousedown', handleMouseDown);
    return () => document.removeEventListener('mousedown', handleMouseDown);
  }, [open, handleClose]);

  // Keyboard navigation.
  const handleKeyDown = useCallback((e: React.KeyboardEvent) => {
    if (e.key === 'Escape') { handleClose(); return; }
    if (e.key === 'ArrowDown') {
      e.preventDefault();
      setFocusedIndex(i => (i + 1) % focusableItems.length);
    } else if (e.key === 'ArrowUp') {
      e.preventDefault();
      setFocusedIndex(i => (i - 1 + focusableItems.length) % focusableItems.length);
    } else if (e.key === 'Enter' || e.key === ' ') {
      e.preventDefault();
      const item = focusableItems[focusedIndex];
      if (item) handleSelect(item.id);
    }
  }, [handleClose, handleSelect, focusableItems, focusedIndex]);

  // Focus menu on open.
  useEffect(() => {
    if (open && menuRef.current) {
      menuRef.current.focus();
    }
  }, [open]);

  if (!open) return null;

  return (
    <div
      className="tlMenu"
      role="menu"
      ref={menuRef}
      tabIndex={-1}
      style={{ position: 'fixed', top: position.top, left: position.left }}
      onKeyDown={handleKeyDown}
    >
      {items.map((item, index) => {
        if (item.type === 'separator') {
          return <hr key={index} className="tlMenu__separator" />;
        }
        const focusIdx = focusableItems.indexOf(item);
        const isFocused = focusIdx === focusedIndex;
        return (
          <button
            key={item.id}
            type="button"
            className={'tlMenu__item' + (isFocused ? ' tlMenu__item--focused' : '') +
              (item.disabled ? ' tlMenu__item--disabled' : '')}
            role="menuitem"
            disabled={item.disabled}
            tabIndex={isFocused ? 0 : -1}
            onClick={() => handleSelect(item.id)}
          >
            {item.icon && <i className={'tlMenu__icon ' + item.icon} aria-hidden="true" />}
            <span className="tlMenu__label">{item.label}</span>
          </button>
        );
      })}
    </div>
  );
};

export default TLMenu;
```

**Step 2: Add CSS**

```css
/* --- TLMenu ---------------------------------------------------------------- */

.tlMenu {
  z-index: 10000;
  min-width: 10rem;
  background: var(--layer-01);
  border: 1px solid var(--border-subtle);
  border-radius: var(--border-radius-02, 4px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  padding: var(--spacing-02) 0;
  font-family: var(--font-family);
  outline: none;
}

.tlMenu__item {
  display: flex;
  align-items: center;
  gap: var(--spacing-03);
  width: 100%;
  padding: var(--spacing-03) var(--spacing-04);
  border: none;
  background: transparent;
  color: var(--text-secondary);
  font-size: var(--body-compact-01-font-size);
  font-family: var(--font-family);
  cursor: pointer;
  text-align: left;
}

button.tlMenu__item:hover,
.tlMenu__item--focused {
  background: var(--layer-hover);
  color: var(--text-primary);
}

button.tlMenu__item:focus-visible {
  outline: 2px solid var(--focus);
  outline-offset: -2px;
}

.tlMenu__item--disabled {
  color: var(--text-disabled);
  cursor: default;
}

button.tlMenu__item--disabled:hover {
  background: transparent;
  color: var(--text-disabled);
}

.tlMenu__separator {
  border: none;
  border-top: 1px solid var(--border-subtle);
  margin: var(--spacing-02) var(--spacing-04);
}

.tlMenu__icon {
  flex-shrink: 0;
  width: 1rem;
  text-align: center;
  font-size: 1rem;
}

.tlMenu__label {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
```

**Step 3: Java control + I18N**

Add to `I18NConstants.java`:

```java
/** @en Menu item selected. */
public static ResKey REACT_MENU_SELECT;

/** @en Menu closed. */
public static ResKey REACT_MENU_CLOSE;
```

Java control `ReactMenuControl`: `open`/`close` methods, `SelectItemCommand`, `CloseCommand`.

**Step 4: Build and commit**

```bash
git commit -m "Ticket #29109: Add TLMenu popup menu."
```

---

## Task 11: Final build + regenerate messages

**Step 1: Rebuild everything**

```bash
cd com.top_logic.layout.react && npm run build
cd com.top_logic.layout.react && mvn install -DskipTests=true
```

**Step 2: Commit regenerated messages**

```bash
git add com.top_logic.layout.react/src/main/java/META-INF/messages_*.properties
git commit -m "Ticket #29109: Regenerate messages for new React controls."
```

**Step 3: Verify the bundle**

Check that `tl-react-controls.js` includes all 32 controls (22 existing + 10 new).
