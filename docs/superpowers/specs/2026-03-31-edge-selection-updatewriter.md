# Edge Selection: updateWriter Fix für anonyme SVG-Elemente

**Datum**: 2026-03-31
**Branch**: `CWS/CWS_29108_flow_diagram_completion`
**Ticket**: #29108
**Status**: Offen — implementierungsbereit

## Kontext

Edge-Selektion ist als Feature fast fertig:
- Proto: `Diagram.selection` = `repeated Widget` (statt `SelectableBox`)
- `GraphEdge` hat `selected`, `selectable`, `clickHandler` Felder
- `GraphEdgeOperations.draw()` rendert `tlCanSelect`/`tlSelected` CSS-Klassen, Click-Handler, transparentes Click-Target mit `pointer-events:all`
- `FlowFactory.graphEdge()` akzeptiert `userObject` Parameter
- `SelectionUtil` für gemeinsame `isSelected()`/`setSelected()` über beide Typen
- CSS: `g.tlSelected > path { stroke: var(--focus) }` für Edge-Highlighting

## Das Problem

`applyScopeChanges` rendert dirty Widgets inkrementell via `updateWriter`. Der `updateWriter` findet existierende SVG-Elemente per `clientId`:

```java
beginGroup(model) → ((Widget) model).getClientId() → getElementById(id)
beginPath(model)  → ((Widget) model).getClientId() → getElementById(id)
```

`GraphEdge.draw()` enthält `beginPath()` ohne Model (anonyme Paths). Der `updateWriter` ruft `lookupUpdated(null)` → `null.getClientId()` → **TypeError**.

Bei `SelectableBox` tritt das Problem nicht auf, weil deren Inhalt über `out.write(content)` gezeichnet wird — und der `updateWriter` `write()` überspringt.

## Warum `out.write()` keine Lösung ist

Den Edge-Inhalt in `out.write(Drawable)` einzuwickeln würde den Inhalt beim Update komplett überspringen. Das funktioniert für Selection-Toggles (nur CSS-Klasse ändert sich), aber NICHT wenn sich Edge-Properties ändern (Farbe, Strich, Waypoints). Der Inhalt muss bei Bedarf neu gezeichnet werden können.

## Die Lösung: updateWriter für anonyme Elemente ertüchtigen

Der `updateWriter` muss generisch mit Elementen ohne `clientId` umgehen:

### Wenn `beginGroup(model)` / `beginPath(model)` mit einem Model aufgerufen wird das eine ID hat:
1. Existierendes SVG-Element per `getElementById(clientId)` finden
2. **Alle ID-losen Kinder des Elements entfernen** (werden neu gezeichnet)
3. Element als aktuellen Parent setzen

### Wenn `beginGroup(null)` / `beginPath(null)` aufgerufen wird (anonymes Element):
1. **Normal erstellen** (an den aktuellen Parent anhängen, wie der reguläre SVGBuilder)

### Ergebnis:
- Elemente MIT IDs werden gefunden und in-place aktualisiert
- ID-lose Kinder werden gelöscht und frisch gezeichnet
- Kein Inhalt wird übersprungen — alles wird korrekt gerendert
- Keine Sonderbehandlung für bestimmte Widget-Typen im DiagramControl

### Code-Skizze für `FlowDiagramClientControl.applyScopeChanges()`:

```java
SvgWriter updateWriter = new SVGBuilder(_svgDoc, _svg) {
    @Override
    public void write(Drawable element) {
        // Skip: child widgets with own IDs update themselves.
    }

    @Override
    public void beginGroup(Object model) {
        if (tryLookupAndPrepare(model)) return;
        super.beginGroup(model);
    }

    @Override
    public void beginPath(Object model) {
        if (tryLookupAndPrepare(model)) return;
        super.beginPath(model);
    }

    private boolean tryLookupAndPrepare(Object model) {
        if (model == null) return false;
        String id = ((Widget) model).getClientId();
        if (id == null) return false;
        OMSVGElement existing = getDoc().getElementById(id);
        if (existing == null) return false;

        // Remove ID-less children — they'll be re-created by the draw() call.
        removeAnonymousChildren(existing);
        setParent(existing);
        return true;
    }

    private void removeAnonymousChildren(OMSVGElement parent) {
        Node child = parent.getFirstChild();
        while (child != null) {
            Node next = child.getNextSibling();
            if (child instanceof Element) {
                String childId = ((Element) child).getId();
                if (childId == null || childId.isEmpty()) {
                    parent.removeChild(child);
                }
            }
            child = next;
        }
    }
};
```

## Betroffene Datei

`com.top_logic.react.flow.client/.../FlowDiagramClientControl.java` — nur die `applyScopeChanges` Methode

## Verifikation

1. Build: `mvn install -DskipTests=true -pl com.top_logic.react.flow.client`
2. Demo-App starten
3. Modul-Graph → Modul auswählen → Node klicken → Details erscheinen, Node hervorgehoben
4. Edge-Linie klicken → Details erscheinen, Edge hervorgehoben
5. Zwischen Nodes und Edges wechseln → vorherige Selektion verschwindet
6. Construction-Plan-Demo → Selektion funktioniert weiterhin
