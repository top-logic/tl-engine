# Edge Selection: updateWriter Fix für anonyme SVG-Elemente

**Datum**: 2026-03-31 (aktualisiert 2026-04-01)
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

## Die Lösung: ID-Cache mit Reconciliation

### Kernidee

Wenn der `updateWriter` ein existierendes Element per ID findet:
1. **Alle ID-Nachkommen rekursiv einsammeln** → ID-Cache (Map: ID → DOM-Element)
2. **Alle Kinder des Elements entfernen** (komplett leeren)
3. **`draw()` normal ausführen** — anonyme Elemente werden frisch erzeugt
4. Bei `write(subWidget)`: ID im Cache? → Cached Element an aktuelle Position anhängen. Nicht im Cache? → Echt zeichnen (neues Sub-Widget).
5. **Cache leeren** wenn `endGroup()`/`endPath()` das aktualisierte Element abschliesst — alle IDs im Unterbaum müssen genau vom Render-Vorgang des aktualisierten Elements wieder aufgerufen werden.

### Warum das in 100% der Fälle korrekt ist

- Anonyme Struktur wird **komplett neu gezeichnet** → Änderungen an Verschachtelung, Attributen, Reihenfolge sind kein Problem
- Sub-Widgets werden **exakt dort eingefügt, wo `write()` aufgerufen wird** → Position stimmt immer, auch bei geänderter Zwischenstruktur
- Sub-Widget-Inhalt bleibt unangetastet (aktualisiert sich selbst, wenn dirty)
- DOM-Referenzen bleiben gültig nach `removeChild` — `appendChild` an anderer Stelle versetzt das Element
- Cache-Clearing bei `endGroup()` stellt sicher, dass keine verwaisten Referenzen übrig bleiben

### Ablauf am Beispiel GraphEdge

Ausgangszustand im DOM:
```
<g id="edge123">
  <path d="M0,0 L100,100"/>         ← Kantenlinie (anonym)
  <path pointer-events="all"/>       ← Click-Target (anonym)
  <g transform="translate(50,50)">   ← Decoration-Position (anonym)
    <g id="decoration456">           ← Sub-Widget
      <text>Label</text>
    </g>
  </g>
</g>
```

Update-Vorgang:
1. `beginGroup(edge)` → findet `<g id="edge123">`
2. Cache: `{decoration456: <g id="decoration456">…}`
3. Alle Kinder von `edge123` entfernen → Element ist leer
4. `edge.draw(updateWriter)` läuft:
   - `beginPath(null)` → neuer `<path>` (Kantenlinie), angehängt an `edge123`
   - `endPath()`
   - `beginPath(null)` → neuer `<path>` (Click-Target), angehängt an `edge123`
   - `endPath()`
   - `beginGroup(null)` → neues `<g>` (translate), angehängt an `edge123`, wird Parent
   - `write(decoration)` → `decoration456` im Cache → **cached Element anhängen**
   - `endGroup()` → Parent zurück auf `edge123`
5. `endGroup()` → aktualisiertes Element abgeschlossen → **Cache leeren**

Ergebnis:
```
<g id="edge123">
  <path d="M0,0 L100,100"/>         ← frisch gezeichnet
  <path pointer-events="all"/>       ← frisch gezeichnet
  <g transform="translate(50,50)">   ← frisch gezeichnet
    <g id="decoration456">           ← aus Cache eingefügt, Inhalt intakt
      <text>Label</text>
    </g>
  </g>
</g>
```

### Randfälle

| Fall | Verhalten |
|------|-----------|
| Sub-Widget entfernt | Bleibt im Cache, wird nie eingefügt → GC räumt auf |
| Sub-Widget neu hinzugefügt | Nicht im Cache → `super.write(element)` zeichnet es echt |
| Sub-Widget verschoben | Wird an neuer Position eingefügt (durch `write()`-Aufruf) |
| Verschachtelte Updates | Nur die oberste Ebene cached — innere Widgets aktualisieren sich selbst |

### Code-Skizze für `FlowDiagramClientControl.applyScopeChanges()`

Alle begin-Methoden mit Model-Parameter im SVGBuilder:
- **Hierarchisch** (ändern Parent): `beginGroup(model)`, `beginClipPath(model)`
- **Blatt-Elemente**: `beginPath(model)`, `beginPolyline(model)`, `beginPolygon(model)`

Das Lookup muss bei **allen fünf** greifen, das Depth-Tracking bei **allen hierarchischen** Paaren.

```java
SvgWriter updateWriter = new SVGBuilder(_svgDoc, _svg) {

    /** Cache of ID'd elements detached during update. */
    private Map<String, OMSVGElement> _idCache = null;

    /** Nesting depth tracker for cache lifecycle. */
    private int _updateDepth = 0;

    @Override
    public void write(Drawable element) {
        if (_idCache != null && element instanceof Widget) {
            String id = ((Widget) element).getClientId();
            if (id != null) {
                OMSVGElement cached = _idCache.remove(id);
                if (cached != null) {
                    // Re-insert preserved sub-widget at current position.
                    getParent().appendChild(cached);
                    return;
                }
            }
        }
        // New sub-widget: draw it for real.
        super.write(element);
    }

    // --- Hierarchical begin/end (change parent) ---

    @Override
    public void beginGroup(Object model) {
        if (tryLookupAndPrepare(model)) return;
        if (_updateDepth > 0) _updateDepth++;
        super.beginGroup(model);
    }

    @Override
    public void endGroup() {
        endUpdatedScope();
        super.endGroup();
    }

    @Override
    public void beginClipPath(Object model) {
        if (tryLookupAndPrepare(model)) return;
        if (_updateDepth > 0) _updateDepth++;
        super.beginClipPath(model);
    }

    @Override
    public void endClipPath() {
        endUpdatedScope();
        super.endClipPath();
    }

    // --- Leaf begin/end (don't change parent) ---

    @Override
    public void beginPath(Object model) {
        if (tryLookupAndPrepare(model)) return;
        super.beginPath(model);
    }

    @Override
    public void beginPolyline(Object model) {
        if (tryLookupAndPrepare(model)) return;
        super.beginPolyline(model);
    }

    @Override
    public void beginPolygon(Object model) {
        if (tryLookupAndPrepare(model)) return;
        super.beginPolygon(model);
    }

    // --- Shared logic ---

    private void endUpdatedScope() {
        if (_updateDepth > 0) {
            _updateDepth--;
            if (_updateDepth == 0) {
                // Update of the looked-up element is complete.
                _idCache = null;
            }
        }
    }

    private boolean tryLookupAndPrepare(Object model) {
        if (model == null) return false;
        String id = ((Widget) model).getClientId();
        if (id == null) return false;
        OMSVGElement existing = getDoc().getElementById(id);
        if (existing == null) return false;

        // Cache all ID'd descendants before clearing.
        _idCache = new HashMap<>();
        collectIdDescendants(existing, _idCache);

        // Clear all children — anonymous structure will be redrawn.
        while (existing.hasChildNodes()) {
            existing.removeChild(existing.getLastChild());
        }

        setParent(existing);
        _updateDepth = 1;
        return true;
    }

    private void collectIdDescendants(OMSVGElement parent,
            Map<String, OMSVGElement> cache) {
        Node child = parent.getFirstChild();
        while (child != null) {
            if (child instanceof OMSVGElement) {
                OMSVGElement el = (OMSVGElement) child;
                String childId = el.getId();
                if (childId != null && !childId.isEmpty()) {
                    cache.put(childId, el);
                    // Don't recurse into ID'd elements —
                    // they manage their own children.
                } else {
                    collectIdDescendants(el, cache);
                }
            }
            child = child.getNextSibling();
        }
    }
};
```

### Wichtige Design-Entscheidungen

1. **Alle begin-Methoden mit Model**: Das Lookup (`tryLookupAndPrepare`) wird bei allen fünf begin-Methoden aufgerufen, die ein Model-Objekt erhalten: `beginGroup`, `beginClipPath`, `beginPath`, `beginPolyline`, `beginPolygon`.

2. **Depth-Tracking bei hierarchischen Paaren**: `_updateDepth` wird bei `beginGroup`/`endGroup` und `beginClipPath`/`endClipPath` inkrementiert/dekrementiert — also bei allen Paaren, die den Parent ändern. Blatt-Elemente (`beginPath`, `beginPolyline`, `beginPolygon`) ändern den Parent nicht und brauchen kein Depth-Tracking.

3. **Cache-Scope**: Der Cache lebt genau für die Dauer des hierarchischen begin→end-Paares des aktualisierten Elements. Danach wird er verworfen.

4. **Kein Rekursieren in ID-Elemente**: `collectIdDescendants` stoppt bei Elementen mit ID — deren Kinder gehören zu einem anderen Widget und werden von dessen eigenem Update verwaltet.

5. **Neue Sub-Widgets**: Wenn `write()` ein Widget zeichnet, dessen ID nicht im Cache ist, wird `super.write()` aufgerufen — das Element wird echt gerendert.

## Betroffene Datei

`com.top_logic.react.flow.client/.../FlowDiagramClientControl.java` — nur die `applyScopeChanges` Methode

## Verifikation

1. Build: `mvn install -DskipTests=true -pl com.top_logic.react.flow.client`
2. Demo-App starten
3. Modul-Graph → Modul auswählen → Node klicken → Details erscheinen, Node hervorgehoben
4. Edge-Linie klicken → Details erscheinen, Edge hervorgehoben
5. Zwischen Nodes und Edges wechseln → vorherige Selektion verschwindet
6. Construction-Plan-Demo → Selektion funktioniert weiterhin
