# Gantt Phase 2.1 — DragConstraints-API + Item-Move/Resize

Ticket: #29108 (Teilbereich)

## Ziel

Generische Drag-Infrastruktur in der Flow-Bibliothek (DragController-Interface, Clone-basierte Drag-Visualisierung, DropArea-Konzept) plus erste Gantt-Nutzung: Items per Drag auf der Zeitachse verschieben, in andere Zeilen ziehen, und Spannen per Kanten-Drag vergrößern/verkleinern.

## Nicht-Ziele

- Handles / Edge-Aufziehen (Phase 2.2)
- `enforce`-Semantik / Kanten-Constraints beim Drag (Phase 2.2)
- Row-Expand/Collapse (Phase 2.3)
- Hover-Highlight für Kanten (Phase 2.3)
- Dekoration-Drag (Phase 2.3)
- Server-seitige Drag-Validierung via explizites Command (nachrüstbar ohne API-Änderung)

## Grundsatzentscheidungen

1. **`DragController` ist ein reines Client-Interface.** Alle Methoden synchron, kein Server-Wissen. Layouts, die DragController implementieren, liefern Constraints und Modell-Mutationen.
2. **Clone-basierte Visualisierung.** Während des Drags wird eine msgbuf-Kopie der Box erstellt und per Re-Layout bei jedem Frame aktualisiert (WYSIWYG). Das Original wird gedimmt, nicht verändert. ESC entfernt den Clone; Drop löst `commitDrag` aus.
3. **Draggable = Parent ist DragController.** Eine Box ist draggable, wenn beim Hochwandern im Parent-Tree ein Parent gefunden wird, der `DragController` implementiert und für sein direktes Kind `canMove`/`canResize` mit `true` antwortet. Nicht-draggable Boxen in einem DragController-Layout dienen als Anfasser für umschließende Drag-Operationen.
4. **DropArea und DragEdge als Proto-Typen.** Können über den Modell-Sync transportiert werden; Layouts, die serverseitig berechnete Drop-Areas brauchen, liefern sie als Modell-Feld mit.
5. **Modell-Patch als Kommunikation.** Der bestehende `@ReactCommand("update")`-Mechanismus transportiert die Modell-Mutationen nach dem Drop. Kein explizites Drag-Command.

---

## Proto-Erweiterungen

In `com.top_logic.react.flow.common/src/main/java/com/top_logic/react/flow/data/data.proto`:

### DragEdge (Enum)

```proto
/** Edge of a box for resize operations. */
enum DragEdge {
    /** Top edge. */
    N;
    /** Right edge. */
    E;
    /** Bottom edge. */
    S;
    /** Left edge. */
    W;
}
```

### DropArea (Message)

```proto
/** A rectangular region where a box may be dropped during drag. */
message DropArea {
    /** X coordinate of the top-left corner (layout units). */
    double x;

    /** Y coordinate of the top-left corner (layout units). */
    double y;

    /** Width of the area (layout units). */
    double width;

    /** Height of the area (layout units). */
    double height;
}
```

---

## DragController Interface

Neues Java-Interface in `com.top_logic.react.flow.common`, Package `com.top_logic.react.flow.operations.drag`:

```java
interface DragController {
    /** Darf diese Box per Drag bewegt werden? */
    boolean canMove(Box box);

    /** Darf diese Box von dieser Kante aus resized werden? */
    boolean canResize(Box box, DragEdge edge);

    /**
     * Gültige Drop-Bereiche für diese Box. Aufgerufen einmal bei Drag-Start.
     *
     * Implementierungen können die Areas lokal berechnen (z.B. Gantt: aus Zeilen-
     * Geometrie) oder vorberechnete Areas aus dem Modell lesen (z.B. ein Layout,
     * das serverseitig berechnete Areas mitliefert).
     */
    List<DropArea> getDropAreas(Box box);

    /**
     * Korrigiere die vorgeschlagene absolute Position beim Move.
     * Aufgerufen jeden Frame während des Drags.
     */
    Point constrainMove(Box box, double proposedX, double proposedY);

    /**
     * Korrigiere die vorgeschlagene absolute Kantenposition beim Resize.
     * Aufgerufen jeden Frame während des Drags.
     */
    double constrainResize(Box box, DragEdge edge, double proposedEdgePos);

    /**
     * Finaler Zustand nach Drop. Schreibt die Modell-Mutation.
     * Die Box hat noch ihre Original-Werte; die finalen Werte kommen als Parameter.
     * Der Controller vergleicht mit den Originalwerten, um die Art der Änderung
     * abzuleiten (Move, Resize links, Resize rechts, Zeilenwechsel).
     */
    void commitDrag(Box box, double finalX, double finalY,
                    double finalWidth, double finalHeight);

    /** Drag abgebrochen (ESC). Optionales Cleanup (z.B. DropArea-Cache verwerfen). */
    void cancelDrag(Box box);
}
```

---

## Generischer Drag-Mechanismus (GWT-Client)

Neuer Code in `com.top_logic.react.flow.client`, eingebunden in die bestehende SVG-Event-Behandlung von `FlowDiagramClientControl`.

### mousedown

1. **Hit-Test:** Welche Box wurde getroffen? (Bestehende SVG-Event-Infrastruktur liefert die `clientId` → Box-Lookup.)
2. **Kanten-Erkennung:** Maus-Position relativ zur Box-Bounding-Box. Innerhalb einer schmalen Randzone (~4px) → Resize-Kandidat (`DragEdge` aus nächstgelegener Kante). Sonst → Move-Kandidat.
3. **DragController-Suche:** Walk Parent-Tree aufwärts. Für jeden Parent, der `DragController` implementiert: prüfe `canMove(directChild)` (bei Move) oder `canResize(directChild, edge)` (bei Resize). Erster Treffer gewinnt → Drag-Target = das direkte Kind dieses DragControllers.
4. **Drag-State initialisieren:**
   - `getDropAreas(dragTarget)` aufrufen → cachen
   - msgbuf-Kopie der Drag-Target-Box erstellen (Clone)
   - Original-Box im SVG dimmen (`opacity: 0.3`)
   - Clone ins SVG einfügen (als separate Gruppe über dem Diagramm)
   - Maus-Offset relativ zum Clone merken (damit der Drag nicht „springt")

### mousemove

1. **Vorgeschlagene Position** aus aktuellem Maus-Offset + gespeichertem Anfangs-Offset berechnen.
2. **Constraint:** `constrainMove(clone, proposedX, proposedY)` oder `constrainResize(clone, edge, proposedEdgePos)` aufrufen → korrigierte Werte.
3. **Clone aktualisieren:**
   - Bei Move: `clone.setX(correctedX)`, `clone.setY(correctedY)` (Größe bleibt)
   - Bei Resize: Kantenposition auf der Clone-Box anpassen (x/width bei W/E, y/height bei N/S)
   - `clone.computeIntrinsicSize(context, ...)` + `clone.distributeSize(context, ...)` → Re-Layout des Clone-Inhalts
4. **Clone SVG neu rendern** (bestehende `SvgWriter`-Pipeline auf den Clone anwenden; altes Clone-SVG ersetzen).
5. **Cursor:** `no-drop` wenn die Clone-Mitte außerhalb aller DropAreas liegt, sonst `grabbing`. Bei Resize: `ew-resize` (E/W) oder `ns-resize` (N/S).

### mouseup (Drop)

1. `commitDrag(originalBox, clone.getX(), clone.getY(), clone.getWidth(), clone.getHeight())` aufrufen.
2. DragController schreibt die Modell-Mutationen auf die Original-Box (Position, Größe, ggf. `rowId`).
3. Clone aus SVG entfernen, Original-Opacity wiederherstellen.
4. Modell-Patch wird automatisch über den bestehenden `update`-Kanal an den Server gesendet.
5. Server empfängt Patch → kann optional validieren/korrigieren → sendet Re-Layout-Patch zurück.

### Escape / Abbruch

1. `cancelDrag(originalBox)` aufrufen.
2. Clone aus SVG entfernen, Original-Opacity wiederherstellen.
3. Kein Modell-Update, kein Server-Roundtrip.

---

## Gantt-Implementierung

`GanttLayoutOperations` implementiert zusätzlich zu `BoxOperations` das `DragController`-Interface.

### canMove(box)

Item für diese Box finden (Iteration über `self.getItems()`, Vergleich `item.getBox() == box`). Rückgabe: `item.getCanMoveTime() || item.getCanMoveRow()`.

Wenn kein Item gefunden → `false` (Box ist kein Gantt-Item, z.B. ein Row-Label).

### canResize(box, edge)

Item finden. Nur für `GanttSpan`:
- `WEST` → `span.getCanResizeStart()`
- `EAST` → `span.getCanResizeEnd()`
- `NORTH` / `SOUTH` → `false`

`GanttPoint` → `false` für alle Kanten (kein Resize bei Punkt-Items).

### getDropAreas(box)

Item finden. Drop-Areas aus der Zeilen-Geometrie berechnen (Zeilen-Y-Positionen sind aus dem letzten `computeIntrinsicSize`-Durchgang bekannt via `RowGeometry`):

- `canMoveRow = true` → eine `DropArea` pro Zeile (`x = columnWidth`, `y = rowY`, `width = chartWidth`, `height = rowTotalHeight`)
- `canMoveRow = false` → nur die aktuelle Zeile
- `canMoveTime = false` → jede DropArea auf die aktuelle X-Position einschränken (Breite = aktuelle Box-Breite)

Interne Map `DropArea → rowId` für die spätere Auflösung in `commitDrag` aufbauen und cachen.

### constrainMove(box, proposedX, proposedY)

- **X:** Snap auf nächstes Vielfaches von `axis.snapGranularity`. Clamp auf `[columnWidth + rangeMin * zoom, columnWidth + rangeMax * zoom]`. Wenn `canMoveTime = false` → Original-X beibehalten.
- **Y:** Nächste gültige Zeile (aus DropAreas). Snap auf Zeilen-Y + `rowPadding`. Wenn `canMoveRow = false` → Original-Y beibehalten.
- Rückgabe: korrigierte absolute Position.

### constrainResize(box, edge, proposedEdgePos)

- Snap auf `snapGranularity`.
- `WEST`: Clamp so dass `newStart < currentEnd` (Mindestbreite = 1 Snap-Einheit).
- `EAST`: Clamp so dass `newEnd > currentStart`.
- Rückgabe: korrigierte Kantenposition.

### commitDrag(box, finalX, finalY, finalW, finalH)

Item finden. Vergleich mit Original:

```
deltaX = finalX - box.getX()
deltaW = finalW - box.getWidth()
deltaY = finalY - box.getY()
```

**Zeitverschiebung (Span):**
- `deltaX != 0 && deltaW == 0` → Move: `newStart = (finalX - columnWidth) / zoom + rangeMin`. `newEnd = newStart + (end - start)`.
- `deltaX != 0 && deltaW != 0` → Resize Start (WEST): `newStart = (finalX - columnWidth) / zoom + rangeMin`. End bleibt.
- `deltaX == 0 && deltaW != 0` → Resize End (EAST): `newEnd = (finalX + finalW - columnWidth) / zoom + rangeMin`. Start bleibt.

**Zeitverschiebung (Point):**
- `newAt = (finalX - columnWidth) / zoom + rangeMin` (bei Move).

**Zeilenwechsel:**
- `deltaY != 0` → Zielzeile aus interner `DropArea → rowId`-Map nachschlagen → `item.setRowId(targetRowId)`.

### cancelDrag(box)

Interne DropArea-Map und DropArea-Cache verwerfen.

---

## Demo-Erweiterung

`com.top_logic.demo/src/main/webapp/WEB-INF/views/demo/gantt-demo.view.xml` wird erweitert, um die Drag-Funktionalität zu demonstrieren:

- Bestehende Spans (Spec, Build, QA) behalten ihre Defaults (`canMove: true, canResize: true`) → sind draggbar und resizable.
- Bestehender Point (Beta) behält Default (`canMove: true`) → ist draggbar, aber nicht resizable.
- Ein neues Item wird hinzugefügt, das `canMoveRow: false` setzt → kann nur auf der Zeitachse verschoben werden, demonstriert die Y-Einschränkung.
- Ein neues Item mit `canResize: false` → nicht vergrößerbar, demonstriert die Resize-Einschränkung.
- Nach einem Drag und Drop sollte die Ansicht korrekt neu gelayoutet werden (Server-Re-Layout nach Modell-Patch).

---

## Offene Punkte (während Implementierung zu klären)

- **Clone-Erzeugung in GWT:** Exakte API zum tiefen Klonen einer msgbuf-Box (vermutlich via `writeTo` / `readFrom` auf einem in-memory-Buffer). Während der Implementierung anhand der msgbuf-API festlegen.
- **Clone-Rendering:** Ob der Clone in dieselbe SVG-Root eingefügt wird (unter dem Diagramm-`<g>`) oder in ein separates Overlay-Element. Ersteres ist einfacher; Overlay verhindert Z-Order-Probleme mit anderen Elementen.
- **Performance:** Re-Layout + Re-Render des Clones bei jedem mousemove-Frame. Für typische Gantt-Items (Border + Padding + Text) trivial; bei komplexen verschachtelten Layouts ggf. throttlen (z.B. `requestAnimationFrame`).
- **Maus-Capture:** Während des Drags muss `mousemove`/`mouseup` global gefangen werden (nicht nur auf dem SVG-Element), damit der Drag nicht verloren geht wenn die Maus das Diagramm verlässt. Standard-Pattern: `setCapture` auf dem SVG-Root-Element.
