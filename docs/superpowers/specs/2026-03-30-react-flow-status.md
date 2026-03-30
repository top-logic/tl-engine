# React Flow Diagram — Aktueller Stand

**Datum**: 2026-03-30 (Abend-Update)
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

## Nächster Schritt: Dynamischer Modul-Graph

TL-Script das ein echtes TL-Modul in einen Graph umrechnet:
- Typen → Nodes (mit UML-Klassenkomposition: Stereotyp, Name, Attribute)
- Vererbung → Edges mit FILLED_ARROW (priority 3)
- Referenzen → Edges mit ARROW/DIAMOND (priority 1-2)
- Kardinalitäten + Rollennamen als zweizeilige EdgeDecorations

Kein neues UIElement nötig — alles über `<flow-diagram>` + TL-Script konfigurierbar.
