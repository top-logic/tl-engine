# Gantt Chart — Design

Ticket: #29108 (Teilbereich)

## Ziel

Eine generische, parametrisierbare Gantt-Chart-Komponente als neues Layout in der TopLogic-Flow-Bibliothek (`com.top_logic.react.flow.*`). Die Komponente ist **breit** ausgelegt: Items können Spannen oder Zeitpunkte sein, Zeilen bilden einen Baum, Abhängigkeiten zwischen Items sind erster Klasse, Zeitachse und Zeit-Semantik werden von der Anwendung geliefert.

Das **vollständige Datenmodell** wird von Anfang an festgelegt; die Umsetzung erfolgt in drei Phasen, die jeweils Teile des Modells aktivieren.

## Nicht-Ziele

- Undo/Redo (Plattform-Sache)
- Viewport-Persistenz (Zoom/Pan) über Sessions
- Initial-View-Logik (Flow-Lib)
- Kontextmenüs (Flow-Lib; Anwendung steuert generisch)
- Lazy-Loading von Items (Phase-1 lädt alles; spätere Erweiterung möglich, aber nicht Teil dieses Designs)
- Löschen, Mehrfach-Drag, Inline-Edit — via Kontextmenü-Kommandos durch die Anwendung
- Komplexes Edge-Routing mit Kollisionsvermeidung

## Grundsatzentscheidungen

1. **Gantt als neuer `Layout`-Subtyp** (`GanttLayout extends Layout`). Items sind generische `Box`-Kinder; `GanttLayout` kümmert sich ausschließlich um Positionierung, Achsen-/Zeilen-Rendering, Dekorations-Rendering und Edge-Routing.
2. **Modell ist das Protokoll.** Die Flow-Lib synchronisiert das Modell client-server; Änderungen (Zoom, Drag-Commits) erfolgen als Modell-Mutationen, nicht über separate RPC-Kanäle.
3. **Zeit-Semantik komplett serverseitig.** Der Client sieht nur Positionen (Pixel bei Zoom 100%) und Ticks. `TimeValue` ist ein server-interner Typ. Anwendungen implementieren das Axis-Verhalten als TL-Script-Funktionen.
4. **Generische Flow-Lib-Erweiterungen inklusive.** Die Einführung von Handles/Ports für Edge-Draw und einer Layout-gesteuerten Drag-Constraint-API sind Teil dieses Tickets (werden von anderen Layouts später mitgenutzt).

## Datenmodell

Alle neuen Typen sind msgbuf-generiert (Modul: `com.top_logic.react.flow.common/data/`).

### GanttLayout

```
GanttLayout extends Layout {
  rootRows    : List<Row>          # Zeilen-Forst (Baum, nicht nur Liste)
  items       : List<Item>         # Span oder Milestone
  edges       : List<Edge>
  decorations : List<Decoration>   # Line oder Range
  axis        : AxisProvider
}
```

Die geerbte `contents : List<Box>` aus `Layout` nimmt die Item-Boxes auf (jedes `Item.box` ist Element von `contents`).

### Row

```
Row {
  id       : String      # opaque Identifier
  label    : String
  children : List<Row>   # echte Hierarchie
}
```

Phase 1: flach gerendert mit optischer Einrückung. Phase 2: interaktiv auf-/zuklappbar.

### Item (Summen-Typ)

```
abstract Item {
  id               : String
  rowId            : String      # Referenz auf Row.id
  box              : Box         # Inhalts-/Rendering-Box, Element von contents
  canMoveTime      : bool        # default true
  canMoveRow       : bool        # default true
  canBeEdgeSource  : bool        # default true
  canBeEdgeTarget  : bool        # default true
}

Span extends Item {
  start          : double        # position (Pixel @ Zoom 100%)
  end            : double
  canResizeStart : bool          # default true
  canResizeEnd   : bool          # default true
}

Milestone extends Item {
  at : double
}
```

`canResize` als Convenience (setzt `canResizeStart` und `canResizeEnd` zusammen) ist eine API-Vereinfachung am Server; im msgbuf-Modell bleiben beide Felder separat.

### Edge

Endpunkte kodieren die FS/SS/FF/SF-Variante; `enforce` kodiert die Semantik.

```
Edge {
  id
  sourceItemId     : String
  sourceEndpoint   : { START, END }
  targetItemId     : String
  targetEndpoint   : { START, END }
  enforce          : { STRICT, WARN, NONE }
}
```

| `enforce` | Verhalten bei Constraint-Verletzung |
|-----------|-------------------------------------|
| `STRICT`  | Operation wird verhindert (Client-Constraint, vor Commit) |
| `WARN`    | Operation erlaubt, Kante rot markiert |
| `NONE`    | Reine visuelle Relation, keine zeitliche Semantik |

Für Meilensteine sind START und END positionsgleich; die Endpunkt-Wahl bleibt trotzdem für Anzeige-Ankerpunkte erhalten.

### Decoration (Summen-Typ)

```
abstract Decoration {
  id
  color       : Color
  label       : String
  canMove     : bool              # default false
  relevantFor : Set<String> | null  # null = gesamtes Chart; sonst Teilmenge Row-IDs
}

Line extends Decoration {
  at : double
}

Range extends Decoration {
  from      : double
  to        : double
  canResize : bool                # default false
}
```

Dekorationen nehmen **nicht** an Abhängigkeitskanten teil.

### AxisProvider

Zentrale Stelle für Zeit-Abstraktion. Das Modell trägt alle Felder, die Client und Server austauschen:

```
AxisProvider {
  providerId      : String            # Name des Server-TL-Script-Bundles
  range           : { min, max }      # gesamter darstellbarer Positionsbereich
  currentZoom     : double            # Pixel pro Position (1.0 = 100%)
  currentTicks    : List<Tick>        # für aktuellen Zoom vorberechnet
  snapGranularity : double            # Raster-Einheit für Client-Drag-Snap
}

Tick {
  position : double
  label    : String
  emphasis : double      # 0..1 — Renderer interpoliert Strichstärke/Label-Prominenz
}
```

**Kommunikation via Modell-Synchronisation:**
- Client schreibt `currentZoom` bei Zoom-Änderung (debounced ~100ms).
- Server beobachtet die Änderung, ruft die TL-Script-Funktionen des `providerId`-Bundles, schreibt `currentTicks` und `snapGranularity` zurück.
- Bei Drag-Commit: Client sendet Positions-Wert (über Standard-Flow-Lib-Event-Kanal an den Server-Command, der dann `axisFromPosition` konsultiert).

**Server-TL-Script-Funktionen, die ein AxisProvider bereitstellen muss:**

```
axisToPosition(timeValue) -> position
axisFromPosition(position) -> timeValue
axisTicksFor(range, pixelsPerUnit) -> List<Tick>
axisSnap(position, pixelsPerUnit) -> position
axisFormatLong(timeValue) -> string
```

Anwendungen registrieren solche Bundles zentral (Mechanismus analog zu bestehender Flow-Lib-Extension-Registry — konkrete Form während Phase 1 festzulegen).

## Generische Flow-Lib-Erweiterungen (Teil dieses Tickets)

### Handles (Edge-Draw-Ports)

Boxes können **Handles** tragen — kleine Verbindungspunkte, die bei Hover sichtbar werden und per Drag eine neue Edge aufziehen. Für Gantt:
- Span-Items: Handles links und rechts, leicht außerhalb der Box (kollidieren nicht mit Resize-Zonen)
- Milestone-Items: Handles links und rechts der Raute
- Drag-Herkunft → `sourceEndpoint`, Drop-Ziel → `targetEndpoint`

Handles sind ein generisches Flow-Konzept; das msgbuf-Modell bekommt einen neuen Typ `Handle` an `Box` (oder als Decoration-Variante — während Phase 2 zu entscheiden).

### Layout-gesteuerte Drag-Constraints

Layouts erhalten Hook-Punkte, um Drag-Operationen an ihren `contents` einzuschränken:

```
interface DragConstraints {
  constrainMove(box, proposedX, proposedY) -> (x, y) | null    # null = verhindert
  constrainResize(box, edge, proposedPosition) -> position | null
  isValidDropTarget(box, candidateContainer) -> bool
}
```

`GanttLayoutOperations` implementiert das: horizontaler Move übersetzt in Zeitverschiebung, vertikaler Move snappt auf Zeilen, Resize auf Start/Ende, kein Overlap innerhalb einer Zeile.

Die genaue API wird während Phase 2 in Abstimmung mit den bestehenden `*LayoutOperations` festgelegt.

## Interaktions-Design

**Operationszonen pro Item:**

```
         ○ (handle)               ○ (handle)
         │                        │
  ┌──────┴────────────────────────┴─────────┐
  │                                         │
  │ ╞═══════════ body (move) ═══════════╡   │
  │                                         │
  └──────┬────────────────────────┬─────────┘
         │                        │
    ← resize                 resize →
      (schmale                 (schmale
       Randzone)                Randzone)
```

- **Body**: horizontaler Drag = Zeit-Move; vertikaler Drag = Zeilenwechsel (snapped).
- **Linke/rechte Randzone** (~4–6 px): Resize-Cursor, Drag ändert Start/Ende.
- **Handles** (hover-sichtbar, links/rechts außerhalb): Drag zieht neue Edge auf. Herkunfts-Seite → `sourceEndpoint`.
- Milestones: keine Resize-Zonen; Handles links/rechts der Raute.

**Edge-Typ-Herleitung beim Aufziehen:**

| Quelle → Ziel | resultierende FS/SS/FF/SF |
|---------------|---------------------------|
| END → START   | FS (Finish-to-Start, klassisch) |
| START → START | SS |
| END → END     | FF |
| START → END   | SF |

`enforce` defaulted bei interaktiv angelegten Kanten auf `STRICT`. Die Anwendung kann es über eigene Attribut-Editoren/Kontextmenüs ändern (auch auf `NONE` = rein dekorative Relation).

**Client-Server-Arbeitsteilung bei Drag:**
- Statische Regeln (Flags, Constraints aus `enforce=STRICT`-Kanten) werden **clientseitig** ausgewertet. Flüssige UI.
- Server-Command `canDropTo(itemId, targetRowId) → { ok: bool, reason?: ResKey }` für Zeilenwechsel. Cache pro laufender Drag-Interaktion (erster Versuch pro Zielzeile befragt Server, Folgeversuche aus Cache). Auf Basis des Ergebnisses werden ungeeignete Zeilen visuell markiert.
- Snap während Drag: clientseitig gegen `snapGranularity` (aus Modell). Server-`axisSnap` wird **beim Commit** angewendet als finale Autorität.
- Selektion ist **entkoppelt** von Drag/Resize — Interaktionen funktionieren ohne vorherige Selektion.

## Achsen-Rendering

- `GanttLayoutOperations` rendert die Achse aus `currentTicks`. Strichstärke und Label-Prominenz werden aus `emphasis` linear interpoliert.
- Der darstellbare Bereich ergibt sich aus `AxisProvider.range`; der aktuell sichtbare Ausschnitt aus der Flow-`viewBox`.
- Zeilen-Hintergrundstreifen (Lanes) werden mitgerendert — vertikale Trennungen, optional alternierend eingefärbt.
- Dekorationen werden über die Lanes gezeichnet (Line = vertikaler Strich, Range = farbiger Streifen), gegebenenfalls auf ihre `relevantFor`-Zeilen beschränkt.

## Edge-Routing

Einfaches orthogonales Routing: aus der Quell-Handle-Seite raus, vertikal in einen Kanal zwischen Zeilen, horizontal bis zur Ziel-X, vertikal an die Ziel-Zeile, orthogonal ans Ziel-Handle. Keine Item-Kollisionsvermeidung.

Lesbarkeit bei dichten Charts wird durch **Hover-Highlight** abgefedert (Phase 2): alle Edges im Normalfall dezent; bei Hover/Selektion eines Items werden seine Kanten prominent. Kante-rot bei `enforce=WARN`-Verletzung bleibt unabhängig davon immer sichtbar.

## Serverseite

### Control

Neue Control-Klasse analog `FlowDiagramControl`, die ein `GanttLayout`-Modell ausliefert und Modell-Mutationen empfängt. Eventuelle Ableitung von `FlowDiagramControl` wird in Phase 1 geprüft.

### Script-Funktionen (Anwendungs-API)

Das Gantt-Modul registriert TL-Script-Funktionen, mit denen Anwendungen das Modell bauen:

```
ganttLayout(rows, items, edges?, decorations?, axis)
ganttRow(id, label, children?)
ganttSpan(id, box, rowId, start, end, flags?)
ganttMilestone(id, box, rowId, at, flags?)
ganttEdge(sourceItemId, sourceEndpoint, targetItemId, targetEndpoint, enforce?)
ganttLineDeco(at, color, label, relevantFor?, canMove?)
ganttRangeDeco(from, to, color, label, relevantFor?, canMove?, canResize?)
ganttAxis(providerId, range, initialZoom?)
```

Für den `AxisProvider` registriert die Anwendung ein eigenes Script-Bundle mit den Funktionen `axisToPosition`/`axisFromPosition`/`axisTicksFor`/`axisSnap`/`axisFormatLong`.

## Phasen

Die Phasen aktivieren Teile des **vollständigen** Modells. Das Modell wird in Phase 1 final festgelegt und in späteren Phasen nicht umgebaut.

### Phase 1 — Statische Darstellung + Demo

- Vollständiges msgbuf-Modell (alle oben definierten Typen).
- `GanttLayoutOperations` rendert: Achse mit `currentTicks` (einmal serverseitig berechnet, keine Zoom-Reaktion), Zeilen-Forst flach mit Einrückung, Items an korrekten Positionen, Edges statisch, Dekorationen.
- Server-Control + Script-Funktionen zum Modellbau.
- AxisProvider-Registry auf dem Server.
- Demo-AxisProvider (z.B. „Tage seit Epoch" mit festen Monats-/Jahres-Ticks).
- Gantt-Demo in `com.top_logic.demo` — anschaubar, keine Interaktion.

**Nicht in Phase 1:** Handles, Drag-Constraints, `canDropTo`, Hover-Highlight, Zoom-Reaktion der Achse, Row-Expand.

### Phase 2 — Interaktionen

- Flow-Lib-Erweiterungen: Handle-Typ, `DragConstraints`-API.
- Item-Move (Zeit + Zeile), Resize (Start/Ende), Edge-Aufziehen mit Endpunkt-Herleitung.
- `enforce`-Semantik (STRICT/WARN) clientseitig ausgewertet.
- `canDropTo`-Server-Command mit Drag-lokalem Cache.
- Dekorationen bewegbar/resize-bar (gemäß Flags).
- Row-Auf-/Zuklappen wird interaktiv (Modell bleibt unverändert).
- Hover-Highlight für Kanten.

### Phase 3 — Zoom-adaptive Achse

- Zoom-Änderungen am Viewport aktualisieren `currentZoom` im Modell.
- Server reagiert, ruft TL-Script-Provider (`axisTicksFor`, und ggf. `axisSnap` für neue `snapGranularity`), schreibt `currentTicks`/`snapGranularity` zurück.
- Client rendert neu.
- `axisFormatLong` wird für Tooltips genutzt.

## Offene Punkte für Phase 1

- Präzise Form der AxisProvider-Registry (neu vs. bestehendes Flow-Lib-Extension-Pattern).
- Ob `GanttControl` von `FlowDiagramControl` ableitet oder parallel lebt.
- Genaues Verhältnis zu bestehenden Layout-Typen (prüfen, ob `GanttLayout` direkt `Layout` erbt oder ein Zwischentyp sinnvoll ist).
- Konkrete Form, in der `Handle` ans msgbuf-`Box`-Modell angehängt wird (als Feld, als Decoration-Variante, oder als eigenständige Entität mit `boxId`-Referenz) — in Phase 2 zu entscheiden, aber rückwirkungsfrei fürs Gantt-Modell.

## Referenzen

- Branch: `CWS/CWS_29108_gantt_chart_react_flow`
- Betroffene Module: `com.top_logic.react.flow.common`, `com.top_logic.react.flow.server`, `com.top_logic.react.flow.client`, `com.top_logic.demo`
