# Phase 1a: Neue UI-Konzepte (Theme-unabhaengig)

**Status**: Draft
**Ticket**: #29108
**Scope**: Framework-Erweiterungen in `com.top_logic.layout.react`, verifiziert in der Demo-App
**Voraussetzung**: Phase 0 (URL-Routing) abgeschlossen

---

## 1. Kontext

Phase 0 hat das URL-Routing-Fundament gelegt. Phase 1a fuehrt neue UI-Konzepte
ein, die fuer eine moderne SPA-artige Oberflaeche noetig sind — unabhaengig
von einem konkreten Theme. Alle Konzepte werden im Demo-Modul nachgewiesen.

---

## 2. Scope

### In Scope

1. **Page-Transitions** — animierte Uebergaenge beim Seitenwechsel
2. **Motion-Preset-Bibliothek** — gemeinsame Animations-Presets
3. **ScrollAware AppBar** — AppBar reagiert auf Scroll-Position
4. **HeroCardGrid** — Karten-Grid mit Bild-Slot und Overlay
5. **ChipFilterRow** — Filter-Chips mit Kanal-Bindung

### Nicht in Scope

- Luminous-Noir-Theme (eigene Phase)
- Estate-spezifische Komponenten
- Glass-/Glow-CSS-Utilities (gehoeren zum Theme)
- Estate-Model-Erweiterungen

---

## 3. Design

### 3.1 Page-Transitions

Framer Motion wird als Dependency in `tl-react-bridge` aufgenommen. Der
Router-Viewport (dort, wo der aktive Content bei Sidebar/Tab-Wechsel
eingeblendet wird) bekommt eine `AnimatePresence`-Umhuellung.

**Konfiguration im View-XML:**

```xml
<sidebar active-item="dashboard" transition="fade-slide-up">
  ...
</sidebar>

<tab-bar active-tab="overview" transition="fade">
  ...
</tab-bar>
```

Das `transition`-Attribut ist optional. Ohne Attribut: kein Uebergang
(bestehendes Verhalten, rueckwaertskompatibel). Moegliche Presets:
`fade`, `fade-slide-up`, `fade-slide-right`, `scale`, `none`.

**React-Implementierung:**

In `TLSidebar.tsx` und `TLTabBar.tsx` wird der Content-Bereich in
`AnimatePresence` gewrappt. Der aktive Content erhaelt `motion.div` mit
dem konfigurierten Preset als `variants`.

```typescript
import { AnimatePresence, motion } from 'framer-motion';
import { getTransitionPreset } from '../motion/presets';

// In render:
<AnimatePresence mode="wait">
  <motion.div
    key={activeItemId}
    {...getTransitionPreset(transition)}
  >
    <TLChild control={activeContent} />
  </motion.div>
</AnimatePresence>
```

WICHTIG: Framer Motion muss als external in allen Vite-Configs
konfiguriert werden (wie React selbst), damit keine Duplikate
im Bundle landen. Es wird ueber `tl-react-bridge` re-exportiert.

### 3.2 Motion-Preset-Bibliothek

Neue Datei `react-src/motion/presets.ts` in `com.top_logic.layout.react`:

```typescript
export const EASE_STANDARD = [0.25, 0.46, 0.45, 0.94];

export const presets = {
  'fade': {
    initial: { opacity: 0 },
    animate: { opacity: 1 },
    exit: { opacity: 0 },
    transition: { duration: 0.3, ease: EASE_STANDARD }
  },
  'fade-slide-up': {
    initial: { opacity: 0, y: 12 },
    animate: { opacity: 1, y: 0 },
    exit: { opacity: 0, y: -8 },
    transition: { duration: 0.35, ease: EASE_STANDARD }
  },
  'fade-slide-right': { ... },
  'scale': { ... },
  'none': null
};

// Stagger-Presets fuer Listen/Grids:
export const staggerContainer = {
  animate: { transition: { staggerChildren: 0.07, delayChildren: 0.1 } }
};

export const staggerItem = {
  initial: { opacity: 0, y: 8 },
  animate: { opacity: 1, y: 0 }
};
```

Die Presets werden ueber `tl-react-bridge` exportiert, damit App-Module
sie verwenden koennen:

```typescript
// In bridge-entry.ts:
export { presets, staggerContainer, staggerItem, EASE_STANDARD } from './motion/presets';
```

### 3.3 ScrollAware AppBar

Erweiterung von `ReactAppBarControl` (Java) und `TLAppBar.tsx` (React).

**Neues Attribut** auf `AppBarElement.Config`:

```xml
<app-bar variant="flat" scroll-effect="blur">
  ...
</app-bar>
```

`scroll-effect` Optionen:
- `none` (Default): kein Effekt
- `blur`: Hintergrund-Blur + Opacity-Ramp (0-80px Scroll)
- `shadow`: Drop-Shadow erscheint bei Scroll > 0

**React-Implementierung** in `TLAppBar.tsx`:

```typescript
const scrollY = useScroll().scrollY;
const blur = useTransform(scrollY, [0, 80], [0, 12]);
const opacity = useTransform(scrollY, [0, 80], [1, 0.95]);

// Style:
style={{
  backdropFilter: `blur(${blur}px)`,
  backgroundColor: `rgba(var(--appbar-bg-rgb), ${opacity})`
}}
```

Verwendet Framer Motion `useScroll` und `useTransform` — beides aus der
Motion-Bibliothek (Phase 1a Abhaengigkeit).

### 3.4 HeroCardGrid

Neues Control: `ReactHeroCardGridControl` (Java) + `TLHeroCardGrid.tsx` (React).

**View-XML:**

```xml
<hero-card-grid min-column-width="280px" aspect-ratio="4/3" gap="md">
  <card-template>
    <image-slot attribute="image"/>
    <overlay>
      <title attribute="name"/>
      <subtitle attribute="city"/>
    </overlay>
  </card-template>
  <items channel="estates"/>
</hero-card-grid>
```

Das Grid rendert fuer jedes Objekt im `items`-Kanal eine Karte mit:
- **Bild-Bereich**: Aspect-Ratio konfigurierbar (4/3, 16/9, 1/1), Bild aus
  einem Binary-Attribut oder Platzhalter
- **Overlay-Bereich**: Titel + Untertitel + optionale Badges, halbtransparent
  ueber dem Bild
- **Stagger-Animation**: Karten faden per `staggerItem`-Preset ein

Das Grid nutzt CSS Grid mit `auto-fill` und `minmax(minColumnWidth, 1fr)` —
dasselbe responsive Muster wie `ReactGridControl`.

**Demo-Nachweis**: Grid mit `tl.accounts:Person`-Objekten, Platzhalter-Bilder,
Name + Land als Overlay.

### 3.5 ChipFilterRow

Neues Control: `ReactChipFilterRowControl` (Java) + `TLChipFilterRow.tsx` (React).

**View-XML:**

```xml
<chip-filter-row channel="activeFilters">
  <chip id="typeA" label="Type A" value="typeA"/>
  <chip id="typeB" label="Type B" value="typeB"/>
  <chip id="typeC" label="Type C" value="typeC"/>
</chip-filter-row>
```

Oder dynamisch aus einem Kanal:

```xml
<chip-filter-row
    channel="activeFilters"
    options-channel="availableFilters"
    label-attribute="name"
    value-attribute="id"
/>
```

**Verhalten:**
- Klick auf Chip: toggle aktiv/inaktiv
- Aktive Chips werden visuell hervorgehoben
- Der `channel` enthaelt die Liste der aktiven Filter-Werte
- Optional: Freitext-Eingabefeld am Ende der Chip-Leiste
- Chips sind per Klick auf ein X entfernbar (wenn aktiv)

**Kanal-Integration:** Der Kanal `activeFilters` kann als `query-binding`
mit der URL verbunden werden (Phase 0 Infrastruktur).

**Demo-Nachweis**: Chip-Bar ueber einer Tabelle, Chips filtern die
Tabellenzeilen, aktive Filter in der URL als Query-Parameter.

---

## 4. Betroffene Module

| Modul | Aenderungen |
|-------|-----------|
| `com.top_logic.layout.react` | Framer Motion Dependency in `package.json`, Motion-Presets, Page-Transition in TLSidebar/TLTabBar, ScrollAware AppBar, HeroCardGrid Control, ChipFilterRow Control |
| `com.top_logic.layout.view` | `transition`-Attribut auf SidebarElement/TabBarElement, `scroll-effect` auf AppBarElement, HeroCardGridElement, ChipFilterRowElement |
| `com.top_logic.demo` | Demo-Views fuer alle 5 Konzepte |

---

## 5. Demo-Verifikationsszenarien

### 5.1 Page-Transitions

- Sidebar-Wechsel zeigt fade-slide-up Animation (nicht harter Schnitt)
- Tab-Wechsel zeigt fade Animation
- Transition ist konfigurierbar (verschiedene Presets)
- `transition` weglassen = kein Uebergang (rueckwaertskompatibel)

### 5.2 Motion-Presets

- Dashboard-Kacheln staggern beim ersten Einblenden
- Bei Rueckkehr zur gecachten Sicht: kein erneutes Stagger (bereits sichtbar)

### 5.3 ScrollAware AppBar

- Langer Content (z.B. viele Tabellenzeilen)
- Scrollen: AppBar-Hintergrund wird blurred/halbtransparent
- Scroll zurueck auf 0: AppBar ist wieder normal

### 5.4 HeroCardGrid

- Grid mit Platzhalter-Bildern und Overlay-Text
- Responsive: 1 Spalte auf schmal, 2-3 Spalten auf breit
- Karten staggern beim Einblenden

### 5.5 ChipFilterRow

- Chip-Bar mit 3-4 Chips ueber einer Tabelle
- Klick auf Chip: aktiv/inaktiv toggle
- Tabelle filtert sich basierend auf aktiven Chips
- Aktive Chips in URL als Query-Parameter (via query-binding)
