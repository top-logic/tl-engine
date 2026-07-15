# React Dialog Drag/Move/Maximize Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** React-Dialoge (TLWindow) sollen per Titelleiste verschiebbar, per Doppelklick maximierbar/wiederherstellbar und per Maximize-Button umschaltbar sein. Nutzerdefinierte Größenänderungen sollen per `PersonalConfiguration` persistiert werden.

**Architecture:** Das TLWindow positioniert sich aktuell per Flexbox-Zentrierung im Backdrop (`align-items: center; justify-content: center`). Sobald der Nutzer das Fenster bewegt, wird auf absolute Positionierung umgeschaltet. Maximize füllt den gesamten Viewport aus und speichert die vorherige Position/Größe für Restore. Die Drag/Maximize-Logik ist rein client-seitig. Die Größenpersistierung nutzt den bestehenden `resize`-Kommando-Pfad und speichert serverseitig per `PersonalConfiguration` (analog zu den klassischen Dialogen via `AbstractDialogModel`).

**Tech Stack:** TypeScript/React (tl-react-bridge), CSS, Java (ReactWindowControl, PersonalConfiguration, ConfigKey)

---

## Dateiübersicht

| Datei | Aktion | Verantwortung |
|-------|--------|---------------|
| `com.top_logic.layout.react/react-src/controls/TLWindow.tsx` | Modify | Drag-, Maximize-, Restore-Logik hinzufügen |
| `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css` | Modify | Cursor-Styles, Maximize-Zustand, Maximize-Button |
| `com.top_logic.layout.react/react-src/controls/TLDialog.tsx` | Modify | Backdrop-Zentrierung abschalten wenn Fenster manuell positioniert |
| `com.top_logic.layout.react/src/main/java/.../overlay/ReactWindowControl.java` | Modify | Größenpersistierung via PersonalConfiguration |

---

## Designentscheidungen

### Positionierung: Flexbox → Absolute

Aktuell zentriert `.tlDialog__backdrop` das Fenster per `align-items: center; justify-content: center`. Sobald der Nutzer das Fenster zieht, muss das Fenster auf `position: absolute` mit explizitem `top`/`left` wechseln. Dazu:

- TLWindow bekommt lokalen State `dragOffset: { x, y } | null`.
- Wenn `dragOffset` gesetzt ist, wird `position: absolute; top: {y}px; left: {x}px` auf das `.tlWindow`-Element gesetzt.
- Das Backdrop bleibt als Container bestehen (`position: fixed; inset: 0`), aber ohne Flexbox-Zentrierung wenn das Fenster manuell positioniert ist.

### Maximize-Zustand

- Client-only: Keine Server-Synchronisation nötig (wie bei den klassischen Dialogen).
- Speichert `regularBounds` (x, y, width, height) vor dem Maximieren.
- Im Maximize-Zustand: `top: 0; left: 0; width: 100vw; height: 100vh`.
- Resize-Handles und Drag werden im Maximize-Zustand deaktiviert.

### Viewport-Grenzen beim Drag

- Das Fenster darf den Viewport nicht verlassen (analog zu `dlgMove()` im klassischen Code).
- Mindestens die Titelleiste muss sichtbar bleiben.

### Größenpersistierung

Aktuell ist die Höhe `auto` (passt sich dem Inhalt an, begrenzt durch `max-height: 80vh`). Die Breite kommt vom Server (`width`-State). Das `resize`-Kommando aktualisiert den In-Memory-State, geht aber beim nächsten Öffnen verloren.

**Persistierung via PersonalConfiguration:**
- `ReactWindowControl` bekommt einen optionalen `ConfigKey` (analog zu `AbstractDialogModel._configKey`).
- Beim `resize`-Kommando: Breite und Höhe werden in `PersonalConfiguration` gespeichert (als `[width, height]` JSON-Liste, wie bei klassischen Dialogen).
- Beim Konstruieren: Wenn eine gespeicherte Größe existiert, wird sie als initiale Breite verwendet. Die Höhe wird als `min-height` angewendet — der Dialog darf größer werden wenn der Inhalt es erfordert, wird aber nie kleiner als die letzte Nutzerwahl.
- Der Aufrufer (z.B. `ReactDialogManagerControl`) übergibt den `ConfigKey`, der typischerweise vom öffnenden LayoutComponent abgeleitet wird.

---

## Task 1: Drag/Move der Titelleiste

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLWindow.tsx`
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`

- [ ] **Step 1: State und Refs für Drag-Position hinzufügen**

In `TLWindow.tsx`, nach den bestehenden `localWidth`/`localHeight` States (Zeile 43-44), füge hinzu:

```typescript
// Position state: null = centered by flexbox, set = absolute positioning.
const [position, setPosition] = useState<{ x: number; y: number } | null>(null);
const positionRef = useRef<{ x: number; y: number } | null>(null);
```

- [ ] **Step 2: Drag-Handler für die Titelleiste implementieren**

Nach dem bestehenden `handleMouseDown` (Resize-Handler, Zeile 59-111), füge einen neuen Handler hinzu:

```typescript
const handleTitleMouseDown = useCallback((e: React.MouseEvent) => {
  // Only left mouse button; ignore clicks on buttons inside the header.
  if (e.button !== 0 || (e.target as HTMLElement).closest('button')) return;
  e.preventDefault();

  const el = windowRef.current;
  if (!el) return;
  const rect = el.getBoundingClientRect();

  // If first drag, initialize position from current rendered position.
  const startPos = positionRef.current ?? { x: rect.left, y: rect.top };
  const offsetX = e.clientX - startPos.x;
  const offsetY = e.clientY - startPos.y;

  const handleMouseMove = (ev: MouseEvent) => {
    const viewW = window.innerWidth;
    const viewH = window.innerHeight;
    let newX = ev.clientX - offsetX;
    let newY = ev.clientY - offsetY;

    // Constrain to viewport.
    const elW = el.offsetWidth;
    const elH = el.offsetHeight;
    if (newX + elW > viewW) newX = viewW - elW;
    if (newY + elH > viewH) newY = viewH - elH;
    if (newX < 0) newX = 0;
    if (newY < 0) newY = 0;

    const pos = { x: newX, y: newY };
    positionRef.current = pos;
    setPosition(pos);
  };

  const handleMouseUp = () => {
    document.removeEventListener('mousemove', handleMouseMove);
    document.removeEventListener('mouseup', handleMouseUp);
  };

  document.addEventListener('mousemove', handleMouseMove);
  document.addEventListener('mouseup', handleMouseUp);
}, []);
```

- [ ] **Step 3: Position-State im Style anwenden**

Ersetze die bestehende `style`-Berechnung (Zeile 113-121):

```typescript
const style: React.CSSProperties = {
  width: localWidth != null ? localWidth + 'px' : serverWidth,
  ...(localHeight != null
    ? { height: localHeight + 'px' }
    : serverHeight != null
      ? { height: serverHeight }
      : {}),
  maxHeight: position ? '100vh' : '80vh',
  ...(position
    ? { position: 'absolute' as const, left: position.x + 'px', top: position.y + 'px' }
    : {}),
};
```

- [ ] **Step 4: Title-Bar mousedown Event binden**

Ersetze das Header-`div` (Zeile 135):

```tsx
<div className="tlWindow__header" onMouseDown={handleTitleMouseDown}>
```

- [ ] **Step 5: CSS für Drag-Cursor hinzufügen**

In `tlReactControls.css`, nach `.tlWindow__header` (Zeile 612-619):

```css
.tlWindow__header {
	display: flex;
	align-items: center;
	gap: var(--spacing-03);
	padding: var(--spacing-04);
	border-bottom: 1px solid var(--border-subtle);
	flex-shrink: 0;
	cursor: grab;
	user-select: none;
}

.tlWindow__header:active {
	cursor: grabbing;
}
```

- [ ] **Step 6: Build und manuell testen**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.react
```

Erwartung: Dialog lässt sich an der Titelleiste verschieben. Buttons in der Titelleiste (Close, Toolbar) bleiben klickbar.

- [ ] **Step 7: Commit**

```
Ticket #29108: Add drag/move support to React dialog title bar.
```

---

## Task 2: Maximize / Restore

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLWindow.tsx`
- Modify: `com.top_logic.layout.react/src/main/webapp/style/tlReactControls.css`

- [ ] **Step 1: Maximize-State hinzufügen**

In `TLWindow.tsx`, nach den Position-States:

```typescript
// Maximize state.
const [maximized, setMaximized] = useState(false);
const regularBoundsRef = useRef<{ x: number; y: number; w: string; h: string | null } | null>(null);
```

- [ ] **Step 2: Maximize/Restore Toggle-Handler implementieren**

```typescript
const handleToggleMaximize = useCallback(() => {
  if (maximized) {
    // Restore.
    const rb = regularBoundsRef.current;
    if (rb) {
      setPosition(rb.x !== -1 ? { x: rb.x, y: rb.y } : null);
      setLocalWidth(null);
      setLocalHeight(null);
    }
    setMaximized(false);
  } else {
    // Save current bounds.
    const el = windowRef.current;
    const rect = el?.getBoundingClientRect();
    regularBoundsRef.current = {
      x: positionRef.current?.x ?? (rect?.left ?? -1),
      y: positionRef.current?.y ?? (rect?.top ?? -1),
      w: localWidth != null ? localWidth + 'px' : serverWidth,
      h: localHeight != null ? localHeight + 'px' : serverHeight,
    };
    setMaximized(true);
    setPosition({ x: 0, y: 0 });
    setLocalWidth(null);
    setLocalHeight(null);
  }
}, [maximized, serverWidth, serverHeight, localWidth, localHeight]);
```

- [ ] **Step 3: Doppelklick auf Titelleiste zum Maximieren**

Das Header-`div` erweitern:

```tsx
<div
  className={`tlWindow__header${maximized ? ' tlWindow__header--maximized' : ''}`}
  onMouseDown={maximized ? undefined : handleTitleMouseDown}
  onDoubleClick={handleToggleMaximize}
>
```

Hinweis: Im maximierten Zustand ist Drag deaktiviert, aber Doppelklick zum Restore funktioniert weiterhin.

- [ ] **Step 4: Maximize-Button in der Titelleiste hinzufügen**

Vor dem Close-Button (Zeile 146), füge ein:

```tsx
<button
  type="button"
  className="tlWindow__maximizeBtn"
  onClick={handleToggleMaximize}
  title={maximized ? i18n['js.window.restore'] : i18n['js.window.maximize']}
>
  {maximized ? (
    // Restore icon: two overlapping squares.
    <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
      <rect x="4" y="8" width="12" height="12" rx="1.5" fill="none" stroke="currentColor" strokeWidth="2" />
      <path d="M8 8V5.5A1.5 1.5 0 0 1 9.5 4H18.5A1.5 1.5 0 0 1 20 5.5V14.5A1.5 1.5 0 0 1 18.5 16H16" fill="none" stroke="currentColor" strokeWidth="2" />
    </svg>
  ) : (
    // Maximize icon: single square.
    <svg viewBox="0 0 24 24" width="18" height="18" aria-hidden="true">
      <rect x="4" y="4" width="16" height="16" rx="1.5" fill="none" stroke="currentColor" strokeWidth="2" />
    </svg>
  )}
</button>
```

Dazu die I18N-Keys erweitern:

```typescript
const I18N_KEYS = {
  'js.window.close': 'Close',
  'js.window.maximize': 'Maximize',
  'js.window.restore': 'Restore',
};
```

- [ ] **Step 5: Style für Maximize-Zustand anpassen**

Die Style-Berechnung erweitern:

```typescript
const style: React.CSSProperties = maximized
  ? { position: 'absolute' as const, top: 0, left: 0, width: '100vw', height: '100vh', maxHeight: '100vh', borderRadius: 0 }
  : {
      width: localWidth != null ? localWidth + 'px' : serverWidth,
      ...(localHeight != null
        ? { height: localHeight + 'px' }
        : serverHeight != null
          ? { height: serverHeight }
          : {}),
      maxHeight: position ? '100vh' : '80vh',
      ...(position
        ? { position: 'absolute' as const, left: position.x + 'px', top: position.y + 'px' }
        : {}),
    };
```

- [ ] **Step 6: Resize-Handles im Maximize-Zustand ausblenden**

Ersetze die Resize-Handle-Ausgabe (Zeile 170-176):

```tsx
{resizable && !maximized && RESIZE_HANDLES.map(dir => (
  <div
    key={dir}
    className={`tlWindow__resizeHandle tlWindow__resizeHandle--${dir}`}
    onMouseDown={(e) => handleMouseDown(dir, e)}
  />
))}
```

- [ ] **Step 7: CSS für Maximize-Button und maximierten Zustand**

In `tlReactControls.css`, nach `.tlWindow__closeBtn` Regeln:

```css
.tlWindow__maximizeBtn {
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

button.tlWindow__maximizeBtn:hover {
	background: var(--layer-hover);
	color: var(--text-primary);
}

button.tlWindow__maximizeBtn:focus-visible {
	outline: 2px solid var(--focus);
	outline-offset: -2px;
}

.tlWindow__header--maximized {
	cursor: default;
}
```

- [ ] **Step 8: Build und manuell testen**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.react
```

Erwartung:
- Maximize-Button neben dem Close-Button sichtbar.
- Klick maximiert den Dialog auf den gesamten Viewport.
- Erneuter Klick (oder Doppelklick auf Titelleiste) stellt die vorherige Größe/Position wieder her.
- Im maximierten Zustand: kein Drag, keine Resize-Handles.

- [ ] **Step 9: Commit**

```
Ticket #29108: Add maximize/restore to React dialogs.
```

---

## Task 3: Drag-Position bei Resize korrigieren

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLWindow.tsx`

Wenn der Nutzer das Fenster bereits verschoben hat und dann über die Nord/West/Nordwest-Handles verkleinert, muss die Position mitverschoben werden, da sich sonst die obere/linke Kante nicht bewegt.

- [ ] **Step 1: Resize-Handler um Positionskorrektur erweitern**

Im bestehenden `handleMouseMove` innerhalb von `handleMouseDown` (der Resize-Handler), nach der Berechnung von `w` und `h`, füge Positionslogik hinzu:

```typescript
const handleMouseMove = (ev: MouseEvent) => {
  const ds = dragState.current;
  if (!ds) return;
  const dx = ev.clientX - ds.startX;
  const dy = ev.clientY - ds.startY;
  let w = ds.startW;
  let h = ds.startH;

  // Calculate new position offset for N/W handles.
  let posXDelta = 0;
  let posYDelta = 0;

  if (ds.dir.includes('e')) w = ds.startW + dx;
  if (ds.dir.includes('w')) { w = ds.startW - dx; posXDelta = dx; }
  if (ds.dir.includes('s')) h = ds.startH + dy;
  if (ds.dir.includes('n')) { h = ds.startH - dy; posYDelta = dy; }

  const newW = Math.max(200, w);
  const newH = Math.max(100, h);

  // Clamp position deltas if size hit minimum.
  if (ds.dir.includes('w') && newW === 200) posXDelta = ds.startW - 200;
  if (ds.dir.includes('n') && newH === 100) posYDelta = ds.startH - 100;

  localWidthRef.current = newW;
  localHeightRef.current = newH;
  setLocalWidth(newW);
  setLocalHeight(newH);

  // Update position if window has been moved and we're resizing from N/W edge.
  if ((posXDelta !== 0 || posYDelta !== 0) && ds.startPos) {
    const newPos = {
      x: ds.startPos.x + posXDelta,
      y: ds.startPos.y + posYDelta,
    };
    positionRef.current = newPos;
    setPosition(newPos);
  }
};
```

Dazu muss `dragState` um `startPos` erweitert werden:

```typescript
const dragState = useRef<{
  dir: ResizeDir;
  startX: number;
  startY: number;
  startW: number;
  startH: number;
  startPos: { x: number; y: number } | null;
} | null>(null);
```

Und in `handleMouseDown`, bei der Initialisierung:

```typescript
dragState.current = {
  dir,
  startX: e.clientX,
  startY: e.clientY,
  startW: rect.width,
  startH: rect.height,
  startPos: positionRef.current ? { ...positionRef.current } : { x: rect.left, y: rect.top },
};
```

Wenn das Fenster noch zentriert war (kein expliziter `position`-State), muss der Resize die Position ebenfalls setzen:

```typescript
// In handleMouseMove, if startPos came from rect (not from prior drag):
if ((posXDelta !== 0 || posYDelta !== 0)) {
  const newPos = {
    x: (ds.startPos?.x ?? 0) + posXDelta,
    y: (ds.startPos?.y ?? 0) + posYDelta,
  };
  positionRef.current = newPos;
  setPosition(newPos);
}
```

- [ ] **Step 2: Build und testen**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.react
```

Erwartung: Beim Resize über die oberen/linken Handles verschiebt sich die Position korrekt mit, sodass die gegenüberliegende Kante stabil bleibt.

- [ ] **Step 3: Commit**

```
Ticket #29108: Fix position tracking during N/W edge resize.
```

---

## Task 4: Größenpersistierung via PersonalConfiguration

**Files:**
- Modify: `com.top_logic.layout.react/src/main/java/com/top_logic/layout/react/control/overlay/ReactWindowControl.java`
- Modify: `com.top_logic.layout.react/react-src/controls/TLWindow.tsx`

Der bestehende `resize`-Kommando-Handler in `ReactWindowControl` aktualisiert nur den In-Memory-State. Dieser Task ergänzt die Persistierung, sodass die Größe beim nächsten Öffnen desselben Dialogs wiederhergestellt wird.

- [ ] **Step 1: ConfigKey-Feld und Konstruktor-Überladung in ReactWindowControl**

In `ReactWindowControl.java`, füge hinzu:

```java
import java.awt.Dimension;
import com.top_logic.knowledge.wrap.person.PersonalConfiguration;
import com.top_logic.layout.table.ConfigKey;

// Neue Konstanten:
private static final String CONFIG_KEY_SIZE_SUFFIX = "reactDialogSize";
private static final String MIN_HEIGHT = "minHeight";

// Neues Feld:
private ConfigKey _configKey;
```

Neue Konstruktor-Überladung mit `ConfigKey`:

```java
/**
 * Creates a window control with size persistence.
 *
 * @param context
 *        The React context for ID allocation and SSE registration.
 * @param title
 *        The window title.
 * @param width
 *        The default window width.
 * @param closeHandler
 *        Called when the close button is clicked.
 * @param configKey
 *        Key for storing personalized size in {@link PersonalConfiguration}.
 */
public ReactWindowControl(ReactContext context, String title, DisplayDimension width,
        Runnable closeHandler, ConfigKey configKey) {
    this(context, title, width, closeHandler);
    _configKey = ConfigKey.derived(configKey, CONFIG_KEY_SIZE_SUFFIX);
    applyCustomizedSize();
}
```

Im bestehenden Konstruktor `_configKey` auf `ConfigKey.none()` setzen:

```java
public ReactWindowControl(ReactContext context, String title, DisplayDimension width, Runnable closeHandler) {
    super(context, null, REACT_MODULE);
    _closeHandler = closeHandler;
    _configKey = ConfigKey.none();
    setTitle(title);
    setWidth(width);
    setResizable(false);
    setActions(List.of());
}
```

- [ ] **Step 2: Gespeicherte Größe beim Öffnen anwenden**

```java
private void applyCustomizedSize() {
    String key = _configKey.get();
    if (key == null) {
        return;
    }
    PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
    if (config == null) {
        return;
    }
    Object jsonValue = config.getJSONValue(key);
    if (!(jsonValue instanceof List<?> list) || list.size() != 2) {
        return;
    }
    int width = ((Number) list.get(0)).intValue();
    int height = ((Number) list.get(1)).intValue();
    putState(WIDTH, width + "px");
    putState(MIN_HEIGHT, height + "px");
}
```

- [ ] **Step 3: handleResize um Persistierung erweitern**

Ersetze den bestehenden `handleResize`:

```java
@ReactCommand("resize")
void handleResize(Map<String, Object> args) {
    Object w = args.get("width");
    Object h = args.get("height");
    if (w != null) {
        putStateSilent(WIDTH, w.toString());
    }
    if (h != null) {
        putStateSilent(HEIGHT, h.toString());
    }
    saveCustomizedSize(w, h);
}

private void saveCustomizedSize(Object widthValue, Object heightValue) {
    String key = _configKey.get();
    if (key == null) {
        return;
    }
    // Parse pixel values.
    int width = parsePx(widthValue, -1);
    int height = parsePx(heightValue, -1);
    if (width < 0 && height < 0) {
        return;
    }

    PersonalConfiguration config = PersonalConfiguration.getPersonalConfiguration();
    if (config == null) {
        return;
    }

    // Merge with existing values if only one dimension changed.
    Object existing = config.getJSONValue(key);
    if (existing instanceof List<?> list && list.size() == 2) {
        if (width < 0) width = ((Number) list.get(0)).intValue();
        if (height < 0) height = ((Number) list.get(1)).intValue();
    }

    if (width > 0 && height > 0) {
        config.setJSONValue(key, List.of(width, height));
    }
}

private static int parsePx(Object value, int fallback) {
    if (value == null) return fallback;
    String s = value.toString().trim();
    if (s.endsWith("px")) {
        s = s.substring(0, s.length() - 2);
    }
    try {
        return Integer.parseInt(s);
    } catch (NumberFormatException e) {
        return fallback;
    }
}
```

- [ ] **Step 4: minHeight im TLWindow-Client auswerten**

In `TLWindow.tsx`, den `minHeight`-State aus dem Server-State lesen:

```typescript
const serverMinHeight = (state.minHeight as string | null) ?? null;
```

In der Style-Berechnung (normaler, nicht-maximierter Zustand):

```typescript
const style: React.CSSProperties = maximized
  ? { ... }
  : {
      width: localWidth != null ? localWidth + 'px' : serverWidth,
      ...(localHeight != null
        ? { height: localHeight + 'px' }
        : serverHeight != null
          ? { height: serverHeight }
          : {}),
      ...(serverMinHeight != null && localHeight == null
        ? { minHeight: serverMinHeight }
        : {}),
      maxHeight: position ? '100vh' : '80vh',
      ...(position
        ? { position: 'absolute' as const, left: position.x + 'px', top: position.y + 'px' }
        : {}),
    };
```

Damit gilt: Die Höhe passt sich dem Inhalt an, wird aber mindestens so hoch wie die letzte Nutzereinstellung.

- [ ] **Step 5: Build und testen**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.react
```

Erwartung:
- Dialog wird mit Standard-Größe geöffnet (kein gespeicherter Wert).
- Nutzer ändert die Größe per Resize → Größe wird an Server gesendet und in PersonalConfiguration gespeichert.
- Dialog schließen und erneut öffnen → Breite entspricht der gespeicherten, Höhe ist mindestens so groß wie gespeichert.
- Wenn der Inhalt mehr Platz braucht als die gespeicherte Höhe, wächst der Dialog.

- [ ] **Step 6: Commit**

```
Ticket #29108: Persist dialog size in PersonalConfiguration.
```

---

## Task 5: Escape und Backdrop-Klick bei verschobenem Dialog

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLDialog.tsx`

Kein Änderungsbedarf: `TLDialog.tsx` behandelt Escape und Backdrop-Klick unabhängig von der Position des Kindfensters. Da das `.tlWindow` mit `position: absolute` innerhalb des Backdrops liegt, funktioniert der Backdrop-Klick auf die freie Fläche weiterhin korrekt. Dieser Task ist nur eine Verifikation.

- [ ] **Step 1: Manuell verifizieren**

1. Dialog öffnen.
2. Dialog verschieben.
3. Auf den Backdrop (außerhalb des Fensters) klicken → Dialog schließt sich.
4. Dialog erneut öffnen, maximieren, Escape drücken → Dialog schließt sich.

- [ ] **Step 2: Falls Backdrop-Klick nicht funktioniert**

Wenn das Fenster mit `position: absolute` das Click-Event auf dem Backdrop schluckt: Sicherstellen, dass `e.target === e.currentTarget` weiterhin greift. Da das Fenster ein Kind-Element des Backdrops ist, sollte der Click auf den Backdrop (nicht auf das Fenster) weiterhin `e.target === e.currentTarget` erfüllen.

---

## Task 6: Gesamtintegration und finaler Test

**Files:**
- Modify: `com.top_logic.layout.react/react-src/controls/TLWindow.tsx` (falls Korrekturen nötig)

- [ ] **Step 1: Vollständigen Build ausführen**

```bash
mvn install -DskipTests=true -pl com.top_logic.layout.react
```

- [ ] **Step 2: Manuelle Testmatrix durchgehen**

| Test | Erwartung |
|------|-----------|
| Dialog öffnen | Zentriert angezeigt |
| Titelleiste ziehen | Dialog folgt der Maus |
| An Viewport-Rand ziehen | Bleibt im sichtbaren Bereich |
| Maximize-Button klicken | Füllt gesamten Viewport |
| Restore-Button klicken | Kehrt zu vorheriger Position/Größe zurück |
| Doppelklick Titelleiste | Togglet Maximize/Restore |
| Resize im normalen Modus | 8-Richtungen funktionieren |
| Resize nach Drag | Position bleibt korrekt |
| N/W-Resize | Gegenüberliegende Kante bleibt stabil |
| Resize im Maximize-Modus | Handles nicht sichtbar |
| Drag im Maximize-Modus | Nicht möglich |
| Escape drücken | Dialog schließt sich |
| Backdrop klicken | Dialog schließt sich |
| Close-Button | Dialog schließt sich |
| Toolbar-Buttons in Titelleiste | Weiterhin klickbar, lösen keinen Drag aus |
| Zweiten Dialog öffnen | Stapelt sich korrekt über den ersten |
| Resize → Schließen → Erneut öffnen | Breite entspricht gespeichertem Wert |
| Resize Höhe → Schließen → Erneut öffnen mit wenig Inhalt | minHeight greift, Dialog nicht kleiner als gespeichert |
| Resize Höhe → Schließen → Erneut öffnen mit viel Inhalt | Dialog wächst über minHeight hinaus |

- [ ] **Step 3: Finaler Commit (falls Korrekturen nötig)**

```
Ticket #29108: Polish dialog drag/maximize behavior.
```
