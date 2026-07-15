# Gantt Chart Phase 1 — Feedback Collection

Open items from Phase 1 manual verification and code review. Will be addressed in one follow-up batch before Phase 2 starts.

**Conventions:**
- **Kind:** `Bug` / `UX` / `Refactor` / `Phase-2-Prep` / `i18n`
- Add items as they come up; strike through (or remove) when done.

---

## Items

### 1. DeepL-Fehlübersetzung für `ganttSpan` (i18n)

`messages_de.properties` enthält „Gantt-Spannungselement erstellen" für `ganttSpan`. „Spannung" bedeutet hier unbeabsichtigt elektrische Spannung. Korrekt wäre z.B. „Gantt-Spanne erstellen" oder „Zeitspanne anlegen".

Betroffene Stelle: `com.top_logic.react.flow.server/src/main/java/META-INF/messages_de.properties`.

**Ziel:** Händisch korrigieren (per `feedback_de_messages_editable`: manuelle Edits in `messages_de.properties` werden vom Build nicht überschrieben).

Weitere übersetzungswürdige Auffälligkeiten im selben Schub prüfen (`AxisProviderService` → „Berechnung des Axis-Dienstes" ist ebenfalls schief, „Berechnungen" als Übersetzung für `providers` ist irreführend).

---

### 2. Emphasis-Schwellen im `DaysSinceEpochAxisProvider` (UX)

Bei Zoom 20 sind die Monatsbeschriftungen (März/Apr/Mai) zu subtil — nur Jan ragt visuell heraus. Bei Zoom 1 war hingegen gar nichts lesbar. Emphasis sollte vom Zoom abhängen (pixelsPerUnit-Parameter wird schon reingereicht, aber ignoriert).

Betroffene Stelle: `DaysSinceEpochAxisProvider.ticksFor` — `emphasis` wird hart auf `1.0` (Jan) / `0.35` (andere) gesetzt.

**Ziel:** Emphasis je nach Zoom-Stufe anpassen; ggf. bei sehr niedrigem Zoom nur Jahresmarker emittieren, bei mittlerem Monats + Jahre, bei hohem zusätzlich Tage.

---

### 3. Tree-Walks in `GanttLayoutOperations` dreifach (Refactor)

Der Row-Forst wird pro `layout()`-Durchgang drei- bis viermal traversiert: `computeIntrinsicSize` baut Row-Index, `distributeSize` ruft `computeIntrinsicSize` erneut (doppelter Aufbau), `drawDecorations` baut ihn nochmal, `drawRowLanes` traversiert ebenfalls. Solange das Layout klein ist, unkritisch; bei mehr Daten oder häufigen Re-Layouts (Phase 2) wird's sichtbar.

Betroffene Stelle: `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/operations/layout/GanttLayoutOperations.java`.

**Ziel:** Einmaliger Row-Index-Aufbau pro Layout-Pass; `distributeSize` überspringt Neuberechnung wenn bereits aktuell; `drawDecorations`/`drawRowLanes` teilen sich einen Kontext.

---

### 4. Edge-Routing geht durch `midY`, nicht durch einen Zeilen-Zwischenkanal (Phase-2-Prep)

`drawEdges` berechnet `midY = (sy + ty) / 2.0`, also die vertikale Mitte zwischen Quell- und Zielitem. Bei nicht-benachbarten Zeilen läuft der vertikale Abschnitt der Kante dann quer durch ein fremdes Item. Spec verlangt einen „Kanal" zwischen den Zeilen.

Betroffene Stelle: `GanttLayoutOperations.drawEdges`.

**Ziel:** `midY` wählt die Mitte zwischen zwei benachbarten Row-Bounds (z.B. Unterkante Quellzeile + halber Row-Gap), statt die arithmetische Mitte der Items. Kollisionsvermeidung mit anderen Items ist weiterhin out-of-scope für Phase 2.

---

### 5. Milestone rendert als Box, nicht als Raute (Phase-2-Prep)

Spec zeigt Meilensteine als Raute (◇). Aktuell wird einfach die vom Anwender gelieferte `box` an der Meilenstein-Position quadratisch eingefügt.

Betroffene Stellen:
- Positionierung in `GanttLayoutOperations.computeIntrinsicSize` (ok, zentriert)
- Rendering — weil Items generische Flow-Nodes sind, ist die Raute-Form strikt genommen Sache der Anwendung. Fraglich, ob das Gantt-Modul hier überhaupt eingreifen soll.

**Offene Frage:** Sollen Meilensteine im Gantt-Modul als Raute gerendert werden (dann brauchen wir einen speziellen Rendering-Pfad für `GanttMilestone.box`, z.B. Clip-Path), oder liefert die Anwendung einfach eine Raute-geformte Box (z.B. via SVG `polygon`-Widget)? In der Spec steht „Grundformen: … Punkt/Raute (Meilenstein, ohne Dauer)" — das klingt nach Kernverantwortung des Gantt-Moduls.

---

### 6. Demo-Zoom hart verdrahtet (UX / Phase-3-Prep)

Der Demo-Wert `zoom: 20` ist eine Ad-hoc-Konstante, die nur die aktuelle Range lesbar macht. Für andere Zeiträume müsste er nachjustiert werden. Phase 3 bringt Zoom-Reaktivität — bis dahin wäre ein „fit-to-width"-Verhalten (zoom aus Viewport-Breite / Range berechnen) hilfreich, insbesondere für die Demo.

**Ziel:** Entweder automatische Initial-Zoom-Berechnung in `ganttAxis`, oder ein `ganttAxisFit(providerId, rangeMin, rangeMax, targetWidth)`-Helper.

---

### 7. `GanttLayoutOperations` Farbliterale (Refactor)

Achsen-, Lane- und Decoration-Farben sind als String-Literale inline (`#f8f8f8`, `#c0c0c0`, `#707070`, `#e0e0e0`, `#a0a0a0`, `#202020`, `#303030`, `#f5f5f5`, `#ffffff`). Für Theming wären sie besser als `static final String`-Konstanten oder gar als CSS-Variablen im Stylesheet, nicht im Rendering-Code.

**Ziel:** Farben als benannte Konstanten sammeln; ggf. in ein Rendering-Konfigurationsobjekt auslagern, wenn Phase 2 Stylings konfigurierbar machen soll.

---

### 8. `GanttRangeDecoration.canResize` aufspalten (Refactor / Modell-Konsistenz)

Auf `GanttSpan` sind `canResizeStart` und `canResizeEnd` getrennt steuerbar (klassischer Gantt-Fall: fester Start, verschiebbares Ende). Auf `GanttRangeDecoration` gibt es dagegen nur ein einzelnes `canResize`-Flag. Das ist asymmetrisch und war nicht bewusst so gewählt.

Analoges Szenario für Range-Decorations: eine Freeze-Phase mit festem Anfang (Go-Live-Datum) und variablem Ende (je nach Testfortschritt) kann aktuell nicht ausgedrückt werden.

Betroffene Stellen:
- Proto: `GanttRangeDecoration` in `data.proto` — `canResize` → `canResizeFrom` + `canResizeTo`
- Factory: `FlowFactory.ganttRangeDeco` — Parameter aufspalten
- Rendering: aktuell ignoriert `drawDecorations` die Flags ohnehin (Phase 2 bringt Interaktion), aber die Spec-Stelle braucht ein Update

**Ziel:** Zwei unabhängige Flags `canResizeFrom` / `canResizeTo`, analog zum Span-Muster.

---

## Design-Entscheidungen (nicht zu ändern)

Diese Asymmetrien sind **bewusst** und sollten beim Nachziehen nicht "aus Konsistenz" geglättet werden:

### D1. Unterschiedliche Defaults für `can*`-Flags zwischen Items und Decorations

- **Items (`GanttItem`, `GanttSpan`):** `canMoveTime`, `canMoveRow`, `canResizeStart`, `canResizeEnd`, `canBeEdgeSource`, `canBeEdgeTarget` defaulten alle auf `true`.
- **Decorations (`GanttDecoration`, `GanttRangeDecoration`):** `canMove` und `canResize` defaulten auf `false`.

**Warum:** Items sind die primäre Interaktionsfläche des Charts — Tasks werden planmäßig verschoben, resized, verknüpft. Bei Decorations (Heute-Linie, Freeze-Zeitraum, Release-Fenster) sind die meisten in der Praxis statisch; Verschieben/Resize ist der **Sonderfall**, der explizit aktiviert werden muss.

Nicht angleichen.

---

## Process

Neue Punkte hier unten anhängen. Wenn die Liste „rund" ist, einen Implementierungs-Plan ableiten und als Batch abarbeiten.
