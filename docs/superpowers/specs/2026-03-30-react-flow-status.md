# React Flow Diagram — Aktueller Stand

**Datum**: 2026-03-30
**Branch**: `CWS/CWS_29108_flow_diagram_completion`
**Ticket**: #29108

## Erreicht

### Phase 1: Flow-Diagramm im React View Framework

- **FlowDiagramElement** — Deklaratives `<flow-diagram>` UIElement mit:
  - `inputs` (Channel-Liste) — Werte werden als positionale Argumente an `createChart` übergeben
  - `createChart` (TL-Script Expr) — erzeugt ein Diagram aus Channel-Werten
  - `handlers` (benannte DiagramHandler) — als implizite Variablen im Script verfügbar
  - `selection` (Output-Channel) — schreibt selektiertes userObject
  - `observed` (Expr) — vorbereitet für Model Observation (noch nicht verdrahtet)
  - Channel-Listener rebuildet Diagramm bei Input-Änderung

- **TLFlowDiagram.tsx** — Minimaler React-Wrapper, remountet GWT bei neuem diagramJson

- **FlowDiagramControl** — ReactControl mit:
  - Scope-basierter Serialisierung (msgbuf SharedGraph)
  - `@ReactCommand` Handler für click, drop, contextMenu, selection, update
  - `updateSelectionChannel()` nach Scope-Patch vom Client

- **Selection E2E** — Komplett verifiziert:
  - Product-Tabelle → Channel → Diagram-Rebuild
  - Node-Klick → Scope-Patch → Server → Selection-Channel → Detail-Form mit Feldern

- **Construction Plan Demo** — Legacy-FlowChart 1:1 portiert:
  - `test.flowchart`-Modell (Product → FlowNode → Location → Part)
  - TL-Script mit `reactFlow*`-Funktionen
  - Split-Panel: Produkte | Bauplan + Details

### Phase 2a: GraphLayout (Sugiyama)

- **tl-graph-layouter GWT-kompatibel** gemacht:
  - UML-spezifischer Code (DefaultNodeSizer, DiagramTextRenderingUtil, TechnicalNamesLabelProvider, JSONDefaultLayoutGraphExporter) nach `com.top_logic.graph.diagramjs.server` verschoben
  - `LayoutContext` auf `LayoutDirection` reduziert
  - `Sugiyama.layout()` nimmt pluggable `NodeSizer` + `NodePortAssignAlgorithm`
  - `ExplicitGraph` nach `com.top_logic.basic.shared.graph` verschoben
  - Filter/FilterFactory → `java.util.function.Predicate`, MapBuilder → JDK, I18NConstants entfernt
  - `TLGraphLayouter.gwt.xml` in `com.top_logic.graph`
  - GWT-Kompilierung verifiziert

- **GraphLayout in data.proto**:
  - `GraphLayout extends FloatingLayout` — mit `repeated GraphEdge edges`, `layerGap`, `nodeGap`
  - `GraphEdge extends Widget` — `@Ref Box source`, `@Ref Box target`, `repeated GraphWaypoint waypoints`
  - `GraphWaypoint` — `double x, y`

- **GraphLayoutOperations**:
  - `computeIntrinsicSize()` — baut LayoutGraph aus Widget-Nodes, ruft Sugiyama auf, mappt Positionen + Waypoints zurück
  - `distributeSize()` wird auf Nodes aufgerufen nach Layout
  - `drawContents()` — zeichnet Nodes (via FloatingLayout) + Edges

- **GraphEdgeOperations** — rendert orthogonale SVG-Polylines aus Waypoints

- **TL-Script-Funktionen**: `reactFlowGraphLayout()`, `reactFlowGraphEdge()`

- **Unit-Test**: `TestGraphLayout` — gleicher Graph wie Demo, 6 Nodes, 5 Edges, 4 Waypoints pro Edge

- **Graph-Layout Demo**: Statischer Graph (Person→Company, Person→Address, Person→Order, Order→Product, Product→Category)

- **Client-Error gefixt**: Scope-Changes nach Layout+Draw droppen (`_scope.dropChanges()`)

## Architektur-Überblick

```
View XML
  <flow-diagram createChart="..." inputs="..." selection="...">

FlowDiagramElement (UIElement)
  - Kompiliert TL-Script einmal, shared über Sessions
  - createControl(): Channel-Werte → TL-Script → Diagram → FlowDiagramControl
  - ChannelListener rebuildet bei Input-Änderung

FlowDiagramControl (ReactControl)
  - Serialisiert Diagram als JSON via msgbuf SharedGraph
  - Pusht diagram-State via SSE
  - Empfängt Client-Commands (@ReactCommand)
  - Selection → ViewChannel

TLFlowDiagram.tsx (React)
  - Mountet GWT bei diagramJson-Änderung
  - Cleanup bei Unmount

FlowDiagramClientControl (GWT → JS)
  - Deserialisiert JSON → Diagram
  - layout() → computeIntrinsicSize() (inkl. Sugiyama für GraphLayout)
  - draw() → SVGBuilder → SVG DOM
  - Click/Drop/Pan/Zoom Event-Handler
  - Scope-Changes → ReactBridge.sendCommand()

TL-Script Funktionen (FlowFactory)
  - reactFlowChart(), reactFlowText(), reactFlowBorder(), reactFlowPadding(), ...
  - reactFlowSelection(), reactFlowTree(), reactFlowConnection()
  - reactFlowGraphLayout(), reactFlowGraphEdge()
  - reactFlowFill(), reactFlowAlign(), reactFlowVertical(), reactFlowGrid(), ...
```

## Module

| Modul | Rolle |
|-------|-------|
| `com.top_logic.react.flow.common` | Shared: msgbuf Model, Layout-Ops, SVG-Rendering (GWT-kompatibel) |
| `com.top_logic.react.flow.server` | Server: FlowDiagramControl, FlowDiagramElement, FlowFactory, AWTContext |
| `com.top_logic.react.flow.client` | GWT-Client: SVGBuilder, FlowDiagramClientControl, ReactBridge, TLFlowDiagram.tsx |
| `com.top_logic.graph.layouter` | Sugiyama-Algorithmus (GWT-kompatibel) |

## Offene Gaps (Phase 1)

| Gap | Priorität | Status |
|-----|-----------|--------|
| Context Menu (Gap 4) | Mittel | Offen — Menu-Serialisierung zum Client fehlt |
| Drag-and-Drop (Gap 3) | Mittel | Offen — Legacy tlDnD API nicht verfügbar |
| Model Observation (Gap 8) | Mittel | Offen — `_getObserved` nicht verdrahtet |
| Legacy JSNI (Gap 6) | Mittel | Offen — DnD-Referenzen |
| Lazy Update (Gap 7) | Niedrig | Offen — einfacher Debounce |

## Nächster Schritt: Dynamischer Modul-Graph

Kein neues UIElement (`<model-graph>`). Stattdessen:
- TL-Script-Funktionen die ein TLModule in einen Graph umrechnen
- Typen → Nodes (mit Name, optional Stereotyp/Attribute)
- Referenzen/Vererbung → Edges (mit Dekorationen)
- Alles über die bestehende `<flow-diagram>`-Konfiguration
