# Edge Selection: updateWriter Fix für anonyme SVG-Elemente

**Datum**: 2026-03-31 (aktualisiert 2026-04-01)
**Branch**: `CWS/CWS_29108_flow_diagram_completion`
**Ticket**: #29108
**Status**: In Umsetzung

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
beginGroup(model) -> ((Widget) model).getClientId() -> getElementById(id)
beginPath(model)  -> ((Widget) model).getClientId() -> getElementById(id)
```

`GraphEdge.draw()` enthält `beginPath()` ohne Model (anonyme Paths). Der `updateWriter` ruft `lookupUpdated(null)` -> `null.getClientId()` -> **TypeError**.

## Voraussetzung: Ein Widget = Ein top-level SVG-Element

Damit der ID-Cache-Ansatz funktioniert, muss jedes Widget **genau ein top-level SVG-Element mit ID** produzieren. Aller Inhalt muss als Kinder dieses Elements erscheinen.

### Problem: Flache Decorator-Struktur

Einige Decorator-Widgets erzeugen aktuell **Geschwister-Elemente** statt verschachtelter Struktur:

```
Widget-Hierarchie:          SVG-Ergebnis (IST):
SelectableBox               <g id="sel">
  Border                      <path id="fill"/>    <- Fill
    Fill                      <text/>               <- Content
      Text, Text              <text/>               <- Content
                              <path id="border"/>   <- Border
                            </g>
```

Ein `write(borderWidget)` im updateWriter findet `border` im Cache und fügt nur den Border-PATH ein — aber `borderWidget.draw()` wäre für Fill + Text + Border zuständig.

### Lösung: Jedes Widget wraps in `<g>`

```
SVG-Ergebnis (SOLL):
<g id="sel">
  <g id="border">
    <g id="fill">
      <path/>             <- Fill-Background
      <text/>             <- Content
      <text/>             <- Content
    </g>
    <path/>               <- Border-Stroke
  </g>
</g>
```

Jetzt ist `write(borderWidget)` -> Cache-Hit -> `<g id="border">` komplett mit allen Kindern einfügen -> fertig.

### Betroffene Operations-Klassen

| Klasse | Aktuell | Fix |
|--------|---------|-----|
| **FillOperations** | `beginPath(self)` + `drawContent()` als Geschwister | `beginGroup(self)` um beides |
| **BorderOperations** | `drawContent()` + `beginPath(self)` als Geschwister | `beginGroup(self)` um beides |
| **ClipBoxOperations** | `beginGroup(self)` + `beginClipPath()` als Geschwister | Äußeres `beginGroup(self)` um beides |
| **TreeConnectionOperations** | Paths + Symbols + Decorations ohne Wrapper | `beginGroup(self)` um alles |

**Bereits korrekt**: GraphEdgeOperations, SelectableBoxOperations, ClickTargetOperations, DropRegionOperations, TooltipOperations, ContextMenuOperations, FloatingLayoutOperations, ImageOperations.

## Die Lösung: ID-Cache mit Reconciliation

### Kernidee

Wenn der `updateWriter` ein existierendes Element per ID findet:
1. **Alle ID-Nachkommen rekursiv einsammeln** -> ID-Cache (Map: ID -> OMSVGElement)
2. **Alle Kinder des Elements entfernen** (komplett leeren)
3. **`draw()` normal ausführen** -- anonyme Elemente werden frisch erzeugt
4. Bei `write(subWidget)`: ID im Cache? -> Cached Element komplett an aktuelle Position anhängen (mit gesamtem Subtree). Nicht im Cache? -> Echt zeichnen (neues Sub-Widget).
5. Bei `beginGroup/beginPath(model)` innerhalb des Updates: ID im Cache? -> Cached Element einfügen, als Parent/Current setzen. Nicht im Cache? -> Normal erstellen.
6. **Cache leeren** wenn `endGroup()`/`endClipPath()` das aktualisierte Element abschliesst.

### Ablauf am Beispiel Node (SelectableBox -> Border -> Fill)

Ausgangszustand:
```
<g id="sel">
  <g id="border">
    <g id="fill">
      <path/>
      <text/>
      <text/>
    </g>
    <path/>
  </g>
</g>
```

Update (CSS-Klasse aendert sich):
1. `beginGroup(selBox)` -> findet `<g id="sel">` im DOM
2. Cache: `{border: <g id="border">...komplett...}`
3. Kinder von `sel` entfernen
4. CSS-Klasse auf `sel` setzen
5. `write(borderWidget)` -> `border` im Cache -> **komplett einfuegen** -> fertig
6. `endGroup()` -> Cache leeren

Ergebnis: `<g id="sel">` hat aktualisierte CSS-Klasse, Border mit gesamtem Inhalt intakt.

### Ablauf am Beispiel Edge (mit anonymen Paths)

Ausgangszustand:
```
<g id="edge">
  <path/>                    <- Kantenlinie (anonym)
  <path pointer-events/>     <- Click-Target (anonym)
  <g transform="...">        <- Decoration-Position (anonym)
    <g id="label">           <- Label Sub-Widget
      <text>name</text>
    </g>
  </g>
</g>
```

Update:
1. `beginGroup(edge)` -> findet `<g id="edge">` im DOM
2. Cache: `{label: <g id="label">...komplett...}`
3. Kinder von `edge` entfernen
4. CSS-Klasse auf `edge` setzen
5. `beginPath(null)` -> anonym -> frisch erzeugt
6. `beginPath(null)` -> anonym -> frisch erzeugt
7. `beginGroup(null)` -> anonym -> frisch erzeugt, wird Parent
8. `write(labelWidget)` -> `label` im Cache -> **komplett einfuegen**
9. `endGroup()` -> Parent zurueck
10. `endGroup()` -> Cache leeren

Ergebnis: Anonyme Paths frisch, Label-Widget intakt.

## Betroffene Dateien

### 1. Operations-Klassen (Widget -> ein top-level `<g>`)

- `com.top_logic.react.flow.common/.../operations/FillOperations.java`
- `com.top_logic.react.flow.common/.../operations/BorderOperations.java`
- `com.top_logic.react.flow.common/.../operations/ClipBoxOperations.java`
- `com.top_logic.react.flow.common/.../operations/tree/TreeConnectionOperations.java`

### 2. SVGBuilder (Accessor-Methoden)

- `com.top_logic.react.flow.client/.../control/SVGBuilder.java` -- `getParent()`, `setCurrent()`

### 3. UpdateWriter

- `com.top_logic.react.flow.client/.../control/FlowDiagramClientControl.java` -- `applyScopeChanges()`

## Verifikation

1. Build: `mvn install -DskipTests=true -pl com.top_logic.react.flow.common,com.top_logic.react.flow.client`
2. Build demo: `mvn install -DskipTests=true -pl com.top_logic.demo`
3. Demo-App starten
4. Modul-Graph -> Modul auswaehlen -> Node klicken -> Details erscheinen, Node hervorgehoben
5. Edge-Linie klicken -> Details erscheinen, Edge hervorgehoben
6. Zwischen Nodes und Edges wechseln -> vorherige Selektion verschwindet, Child-Counts stabil
7. Construction-Plan-Demo -> Selektion funktioniert weiterhin
