# React Flow Diagram — Aktueller Stand

**Datum**: 2026-03-31
**Branch**: `CWS/CWS_29108_flow_diagram_completion`
**Ticket**: #29108

## Erreicht

### Phase 1: Flow-Diagramm im React View Framework

- **FlowDiagramElement** — `<flow-diagram>` UIElement mit TL-Script + Input-Channels
  - `inputs` → positionale Argumente für `createChart`
  - `createChart` (Expr) → TL-Script das ein Diagram erzeugt
  - `handlers` → benannte DiagramHandler als implizite Variablen
  - `selection` → Output-Channel für selektiertes userObject
  - ChannelListener rebuildet Diagramm bei Input-Änderung
- **TLFlowDiagram.tsx** — React-Wrapper, remountet GWT bei neuem diagramJson
- **FlowDiagramControl** — ReactControl mit Scope-basierter Kommunikation
  - `updateSelectionChannel()` nach Client Scope-Patch
- **Selection E2E** verifiziert: Klick → Scope-Patch → Channel → Detail-Form
- **Construction Plan Demo** — Legacy 1:1 portiert mit echten Business-Daten

### Phase 2a: GraphLayout (Sugiyama)

#### tl-graph-layouter GWT-kompatibel
- UML-spezifischer Code nach `com.top_logic.graph.diagramjs.server` verschoben
  - DefaultNodeSizer, DiagramTextRenderingUtil, TechnicalNamesLabelProvider,
    JSONDefaultLayoutGraphExporter, DefaultNodePortCoordinateAssigner
  - TLModel-Methoden aus LayoutGraphUtil extrahiert → DiagramJSLayoutGraphUtil
- `LayoutContext` auf `LayoutDirection` reduziert (DiagramJSLayoutContext für Legacy)
- `Sugiyama.layout()` nimmt pluggable `NodeSizer` + `NodePortAssignAlgorithm`
- `ExplicitGraph` nach `com.top_logic.basic.shared.graph` verschoben
- Filter → Predicate, MapBuilder → JDK, I18NConstants entfernt
- `TLGraphLayouter.gwt.xml` in `com.top_logic.graph`
- GWT-Kompilierung verifiziert

#### GraphLayout Proto-Typen
- `GraphLayout extends FloatingLayout` — `repeated GraphEdge edges`, `layerGap`, `nodeGap`
- `GraphEdge extends Widget`:
  - `@Ref Box source`, `@Ref Box target` (non-owning Referenzen)
  - `int priority` — Kantenpriorität für Zyklenbrechen (3=Vererbung, 2=Komposition, 1=Referenz)
  - `ConnectorSymbol sourceSymbol`, `ConnectorSymbol targetSymbol` — Pfeilspitzen/Diamonds
  - `repeated GraphWaypoint waypoints` — orthogonale Pfadsegmente
  - `repeated EdgeDecoration decorations` — Labels an Kantenenden
- `GraphWaypoint` — `double x, y`

#### GraphLayoutOperations
- `computeIntrinsicSize()`:
  1. Intrinsische Größen aller Nodes + Decoration-Content berechnen
  2. Extra-Breite pro Node aus Port-Anzahl + Decoration-Breiten berechnen
  3. LayoutGraph aufbauen mit Priorities
  4. `NodeSizer`: `Math.max(intrinsic, portCount × SCALE + maxDecorWidth)`
  5. Sugiyama mit `DecorationAwarePortCoordinateAssigner` (individuelle Port-Breiten)
  6. Positionen + Waypoints zurückmappen
  7. `distributeSize()` mit erweiterter Node-Breite
- `drawContents()`: Nodes via FloatingLayout + Edges mit Waypoints

#### DecorationAwarePortCoordinateAssigner
- Verteilt Ports mit individueller Breite basierend auf Decoration-Größe + Symbol-Inset
- Ports am linken Rand ihrer Allokation (Label erstreckt sich nach rechts)
- Ports zentriert innerhalb der Node-Breite

#### GraphEdgeOperations
- Zeichnet orthogonale SVG-Polylines aus Waypoints
- Inset-Verkürzung der Linie für ConnectorSymbols
- `ConnectorSymbolRenderer` — gemeinsame Symbol-Zeichenlogik (ARROW, FILLED_ARROW, DIAMOND, FILLED_DIAMOND)
- `autoOffsetPosition()` — berechnet Label-Positionierung aus lokaler Kantenrichtung:
  - Labels immer rechts der Kante
  - Target-Ende: über dem Knoten (BOTTOM_LEFT)
  - Source-Ende: unter dem Knoten (TOP_LEFT)
- `drawDecorations()` mit Polyline-Interpolation via `linePosition`

#### Text-Metrik
- `Text.fontSize` ist jetzt `double` (Pixel) mit Default 14, nicht mehr String
- `RenderContext.measure(text, fontFamily, fontSize)` — Font-Properties durchgereicht
- `AWTContext` + `JSRenderContext` nutzen die tatsächliche Font-Größe/Familie
- Kein hardcodiertes "Arial" / 14px mehr in der Breitenberechnung

#### TL-Script-Funktionen
- `reactFlowGraphLayout(nodes, edges, layerGap, nodeGap, ...)`
- `reactFlowGraphEdge(source, target, priority, sourceSymbol, targetSymbol, strokeStyle, thickness, dashes, decorations)`
- `reactFlowDecoration(content, linePosition, offsetPosition)` — offsetPosition optional (auto-computed)
- `reactFlowText(text, ..., fontSize: 10, ...)` — fontSize jetzt numerisch

#### Demo (graph-layout-demo.view.xml)
- 6 Nodes: Person, Company, Address, Order, Product, Category
- Vererbung: Person→Company mit FILLED_ARROW (priority 3)
- Referenzen: Person→Address, Person→Order mit ARROW (priority 1)
- Komposition: Order→Product mit FILLED_DIAMOND (priority 2)
- Zweizeilige Labels an Kantenenden: Name + Kardinalität (fontSize: 10)
- Label am Source-Ende: "persons / *" bei Person→Company

#### Tests
- `TestGraphLayout` — gleicher Graph wie Demo, 6 Nodes, 5 Edges mit Decorations
- Unit-Test verifiziert Node-Positionen, Waypoints (4 pro Edge), Layout-Dimensionen

#### Client-Fixes
- Scope-Changes nach Layout+Draw droppen (kein spurious onChange)
- GWT PRETTY-Mode half beim Debugging

## Module

| Modul | Rolle |
|-------|-------|
| `com.top_logic.react.flow.common` | Shared: msgbuf Model, Layout-Ops, SVG-Rendering, GraphLayout, Sugiyama-Bridge (GWT-kompatibel) |
| `com.top_logic.react.flow.server` | Server: FlowDiagramControl, FlowDiagramElement, FlowFactory, AWTContext |
| `com.top_logic.react.flow.client` | GWT-Client: SVGBuilder, FlowDiagramClientControl, ReactBridge, TLFlowDiagram.tsx |
| `com.top_logic.graph.layouter` | Sugiyama-Algorithmus (GWT-kompatibel, keine TL-Model-Dependencies) |
| `com.top_logic.basic.shared` | ExplicitGraph (GWT-kompatibel) |

## Offene Gaps (Phase 1)

| Gap | Priorität |
|-----|-----------|
| Context Menu (Gap 4) | Mittel |
| Drag-and-Drop (Gap 3) | Mittel |
| Model Observation (Gap 8) | Mittel |
| Legacy JSNI (Gap 6) | Mittel |
| Lazy Update (Gap 7) | Niedrig |

### Phase 2b: Dynamischer Modul-Graph (module-graph-demo.view.xml)

#### Architektur
- **Reine TL-Script-Lösung** — kein neuer Java-Code, alles deklarativ in `<flow-diagram>`
- Input-Channel: Modul-Auswahl aus Tabelle → TL-Script erzeugt UML-Klassendiagramm
- Selection-Channel: Klick auf Typ → Details-Formular

#### UML-Klassendiagramm-Aufbau
- **Node-Komposition** pro TLClass:
  - «abstract»-Stereotyp (bedingt, fontSize 10, grau via fillStyle)
  - Klassenname (bold)
  - Attribut-Kompartiment (TLProperty-Teile mit `name : typeName`, fontSize 11)
  - Kompartiment-Trenner via `reactFlowBorder(top: true, left/right/bottom: false)`
  - Weißer Hintergrund (`reactFlowFill(fill: "white")`)
  - `reactFlowSelection(userObject: $class)` für Typ-Selektion
  - "extends module:ClassName" bei externen Generalizations (grau, fontSize 9)
- **Enumeration-Nodes**:
  - «enumeration»-Stereotyp, Classifier-Liste, hellgelber Hintergrund
- **Generalisierungs-Kanten**:
  - `TLClass#generalizations` → CLOSED_ARROW (hohles Dreieck, priority 3)
  - Source=Elternklasse (oben), Target=Kindklasse (unten) für korrekte UML-Richtung
  - Nur Kanten innerhalb des Moduls (name-basierter Lookup)
- **Referenz-Kanten**:
  - `TLReference` aus `localParts` → ARROW (priority 1) oder FILLED_DIAMOND (priority 2)
  - Komposition erkannt via `TLReference#end → TLAssociationEnd#composite`
  - Komposition: sourceSymbol FILLED_DIAMOND + "1" Decoration am Source-Ende
  - Kardinalität aus mandatory/multiple: 0..1 / 1 / * / 1..*
  - Rollenname + Kardinalität als zweizeilige EdgeDecoration (linePosition 1.0)

#### TL-Script Model-Navigation
- `all(\`tl.model:TLModule\`)` → Modul-Liste
- `$module.get(\`tl.model:TLModule#types\`).filter(instanceOf(\`tl.model:TLClass\`))` → Klassen
- `$module.get(\`tl.model:TLModule#types\`).filter(instanceOf(\`tl.model:TLEnumeration\`))` → Enums
- `$class.get(\`tl.model:TLStructuredType#localParts\`)` → Properties + References
- `$class.get(\`tl.model:TLClass#generalizations\`)` → Elternklassen
- `$nodes.indexBy(n -> $n["userObject"].get(\`tl.model:TLNamedPart#name\`))` → Name→Box-Mapping

#### Layout
- Links: Modul-Tabelle (20%)
- Rechts oben: Klassendiagramm via `reactFlowGraphLayout` (75%)
- Rechts unten: Typ-Details-Formular (25%)

### Phase 2c: Layout-Engine-Fixes

#### Viewport & Panning
- Container `overflow: hidden` verhindert Browser-Scrollbar
- Container `draggable="true"` aktiviert HTML5-Drag für Pan-by-Drag
- Initialer ViewBox fittet Diagramm-Inhalt mit Seitenverhältnis-Erhalt
- Initialer Zoom auf max 100% begrenzt (kleine Diagramme nicht vergrößert)
- Resize-Observer überspringt Server-Updates via `_resizing`-Flag (kein Loop)
- `destroy()` räumt SVG + Zoom-Display auf (kein Stacking bei Modulwechsel)

#### Sugiyama-Integration
- **Negative Koordinaten**: Shift aller Nodes/Waypoints so dass min(x,y) >= 0
- **Edge-Inversion**: `DecorationAwarePortCoordinateAssigner` und `computeTotalPortWidth()` berücksichtigen `isReversed()` via XOR
- **NodeSizer exakt**: Port-Breiten im NodeSizer-Lambda direkt aus `n.outgoingEdges()`/`n.incomingEdges()` berechnet (nach Zyklenumkehr), identisch zur Port-Assigner-Logik

#### Text-Metrik
- `RenderContext.measure()` hat jetzt `fontWeight`-Parameter
- `AWTContext`: `Font.BOLD` für Bold-Messung
- `JSRenderContext`: `"bold 14px Arial"` als Canvas-Font
- `TextOperations.computeIntrinsicSize()` reicht `fontWeight` durch

## Offene Gaps (Phase 1)

| Gap | Priorität |
|-----|-----------|
| Context Menu (Gap 4) | Mittel |
| Drag-and-Drop (Gap 3) | Mittel |
| Model Observation (Gap 8) | Mittel |
| Legacy JSNI (Gap 6) | Mittel |
| Lazy Update (Gap 7) | Niedrig |

## Nächste Schritte

- TLAssociation-Kanten die nicht als TLReference sichtbar sind
- Navigierbare Kanten zu Typen in anderen Modulen (Cross-Modul-Referenzen)
- Tabellen-Spalten einschränken (TableConfigurationProvider-Syntax klären)
