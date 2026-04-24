# LOD-Box — Design (Brainstorming)

Ticket: #29108 (Teilbereich Gantt / Flow-Lib generisch)
Status: Brainstorming — Optionen offen, Entscheidungen markiert mit **[offen]**.
Begleitender Mockup: `2026-04-24-lod-box-mockup.html`

## Ziel

Einführung einer **Level-of-Detail-Box** (`LOD`) in die Flow-Lib: Ein Container, der **N alternative Darstellungen** (Varianten) seines Inhalts vorhält und je nach zur Verfügung stehendem Platz / aktuellem Zoom genau **eine** rendert.

Die Box soll generisch genug sein, um außerhalb des `GanttLayout` nutzbar zu sein (z. B. in Graph-Layouts, Tabellen-Zellen mit variablem Zoom, beliebigen Layouts mit bekannter Kinderbreite), aber innerhalb einer Gantt-Zeile muss sie sich an die **Zeitauflösung** (`GanttAxis.getCurrentZoom()` = `pixelsPerUnit`) hängen, weil diese dort die horizontale Breite eines Spans bestimmt.

## Motivation (konkret)

Heute werden Gantt-Spans mit festem Inhalt gezeichnet und bei geringem Zoom einfach visuell abgeschnitten / überlappen sich. Beispiel:

| Span-Dauer | zoom (px/Einheit) | Pixel-Breite | sinnvolle Darstellung          |
|------------|-------------------|-------------:|--------------------------------|
| 5 Tage     | 50                | 250 px       | Icon + "Build release v1.2"    |
| 5 Tage     | 20                | 100 px       | Icon + "Build"                 |
| 5 Tage     | 4                 | 20 px        | nur Icon                       |
| 5 Tage     | 0.5               | 2.5 px       | farbiger Balken ohne Inhalt    |

Statische Box kann das nicht — eine LOD-Box schon.

## Grundsätze

1. **Die LOD-Box weiß nicht, dass es Gantt gibt.** Sie empfängt ihren Kontext (Breite, Höhe, Zoom) über `RenderContext` — ein etabliertes Tür-zum-Layout-Konzept in der Flow-Lib.
2. **Die Gantt-Zeile ist verantwortlich, den passenden Kontext zu setzen.** `GanttLayout` kennt für jeden Span die exakte Pixelbreite (`(end-start) * pixelsPerUnit`). Es reicht sie über `RenderContext` herab, bevor es `computeIntrinsicSize` auf den Span-Inhalt aufruft.
3. **Die Wahl der Variante fällt in der Intrinsic-Pass-Phase.** Ergebnis: Die reportete intrinsische Höhe passt zur tatsächlich gewählten Variante — Zeilenhöhe stimmt, kein Zweipass-Konflikt.
4. **Außerhalb Gantt:** Wer Kinderbreite kennt (z. B. `Sized`, feste `GridLayout`-Spalten), setzt den Kontext genauso. Wer sie nicht kennt (z. B. flexibles `HorizontalLayout`), setzt nichts → LOD wählt die reichste Variante.

## Datenmodell (Entwurf)

```proto
/**
 * Container that carries multiple rendering variants and renders the one
 * that best fits the available rendering space.
 *
 * Variants are declared from richest to most compact; the first satisfying
 * variant wins. The last variant must match any space (minWidth = 0,
 * minHeight = 0, minZoom = 0) and acts as universal fallback.
 */
message LOD extends Box {
    repeated LODVariant variants;
}

message LODVariant {
    /** The rendered content when this variant is selected. */
    Box content;

    /** Minimum effective pixel width. 0 = any. */
    double minWidth;

    /** Minimum effective pixel height. 0 = any. */
    double minHeight;

    /**
     * Minimum horizontal zoom factor (pixelsPerUnit in Gantt context,
     * SVG scaleX otherwise). 0 = any.
     *
     * Useful for variants whose decision is orthogonal to own box size,
     * e.g. "show week labels only when pixelsPerUnit >= 10".
     */
    double minZoom;
}
```

### Varianten-Reihenfolge: **[offen]**

- **A (gewählt in diesem Entwurf):** richest → most compact; erste **erfüllte** gewinnt.
- **B:** most compact → richest; letzte erfüllte gewinnt. Entspricht Cascaded-CSS-Denken.

Empfehlung: **A**, weil die Bedingungen dann Untergrenzen sind ("mind. X Pixel nötig") und damit intuitiver sind als "max. X Pixel erlaubt".

## RenderContext-Erweiterung

Heute trägt `RenderContext` nur Text-Measurement. Neue Felder:

```java
public interface RenderContext {
    TextMetrics measure(String text);

    /** Horizontaler Zoom-Faktor; 1.0 wenn unbekannt. */
    default double getZoomX() { return 1.0; }

    /** Vertikaler Zoom-Faktor; 1.0 wenn unbekannt. */
    default double getZoomY() { return 1.0; }

    /** Vom Parent reservierte Pixelbreite; POSITIVE_INFINITY wenn unbekannt. */
    default double getAvailableWidth() { return Double.POSITIVE_INFINITY; }

    /** Vom Parent reservierte Pixelhöhe; POSITIVE_INFINITY wenn unbekannt. */
    default double getAvailableHeight() { return Double.POSITIVE_INFINITY; }

    /** Immutable copy with new values. */
    RenderContext withHints(double availableWidth, double availableHeight,
                            double zoomX, double zoomY);
}
```

**Default-Verhalten** (POSITIVE_INFINITY, Zoom 1.0): LOD wählt stets die reichste Variante. Das ist das gewünschte Fallback, wenn niemand die LOD-Box über ihren Kontext informiert hat.

### Propagation: **[offen]**

Zwei Optionen, wie der Kontext durchgereicht wird:

- **(P1) Explizit pro Aufruf:** `box.computeIntrinsicSize(contextWithHints, x, y)` — aufrufendes Layout erzeugt einen modifizierten Context, ruft Kind, ursprünglicher Context bleibt unverändert.
- **(P2) Kontext-Stack:** `RenderContext.pushHints(...)` / `pop()`. Spart Allokation bei vielen Kindern.

Empfehlung: **(P1)**, weil sie threadsafer ist und zum bestehenden Muster passt (Boxes bekommen `offsetX/offsetY` auch als Parameter).

## LOD-Operation

```java
public interface LODOperations extends BoxOperations {
    @Override LOD self();

    @Override
    default void computeIntrinsicSize(RenderContext ctx, double offsetX, double offsetY) {
        LODVariant chosen = pickVariant(ctx);
        self().setChosenVariant(chosen); // transient field
        Box content = chosen.getContent();
        content.computeIntrinsicSize(ctx, offsetX, offsetY);
        self().setWidth(content.getWidth());
        self().setHeight(content.getHeight());
    }

    @Override
    default void distributeSize(RenderContext ctx, double x, double y, double w, double h) {
        Box content = self().getChosenVariant().getContent();
        self().setX(x); self().setY(y);
        self().setWidth(w); self().setHeight(h);
        content.distributeSize(ctx, x, y, w, h);
    }

    default LODVariant pickVariant(RenderContext ctx) {
        double w = ctx.getAvailableWidth();
        double h = ctx.getAvailableHeight();
        double z = ctx.getZoomX();
        for (LODVariant v : self().getVariants()) {
            if (w >= v.getMinWidth() && h >= v.getMinHeight() && z >= v.getMinZoom()) {
                return v;
            }
        }
        return self().getVariants().get(self().getVariants().size() - 1);
    }
}
```

Und der Draw wird über die bestehende Visitor/Dispatch-Mechanik an `chosenVariant.getContent()` delegiert.

## Gantt-Integration

`GanttLayoutOperations.computeIntrinsicSize` (heute Zeile ~106–136) iteriert über Items und ruft `box.computeIntrinsicSize(context, tmpX, tmpY)`. Erweiterung:

```java
double zoom = axis.getCurrentZoom(); // pixelsPerUnit

for (GanttItem item : self.getItems()) {
    Box box = item.getBox();
    if (box == null) continue;

    RenderContext itemCtx;
    if (item instanceof GanttSpan span) {
        double w = (span.getEnd() - span.getStart()) * zoom;
        itemCtx = context.withHints(w, /*availableH*/ Double.POSITIVE_INFINITY,
                                    /*zoomX*/ zoom, /*zoomY*/ 1.0);
    } else if (item instanceof GanttPoint pt) {
        // Punkte haben keine definierte Breite; nur Zoom reicht.
        itemCtx = context.withHints(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY,
                                    zoom, 1.0);
    } else {
        itemCtx = context;
    }
    box.computeIntrinsicSize(itemCtx, tmpX, tmpY);
    // ... wie bisher.
}
```

**Wichtig:** `zoomY = 1.0` — die Gantt-Zeile zoomt vertikal **nicht**. Eine LOD-Box innerhalb eines Spans entscheidet horizontale Varianten (Textlänge), nicht vertikale.

Für **Zeilen-Labels** im Gantt-Sidebar: deren Breite = `columnWidth`, passt ebenfalls via Kontext. Zoom bleibt 1, weil der Sidebar nicht gezoomt wird.

Für **Dekorations-Labels**: wenn sie nicht an ein Item gebunden sind, haben sie typischerweise keine feste Breite — Kontext bleibt default.

## Dimension von "Zoom" **[offen]**

- **Ein Skalar** (z. B. `zoomX`): bildet Gantt 1:1 ab. Einfach.
- **Zwei Skalare** (`zoomX`, `zoomY`): nötig falls jemand später einen vertikalen Zoom einbaut (z. B. zoombarer Graph-Editor).
- **Affine Matrix (CTM)**: vollständig, aber massiver Overhead für heutigen Anwendungsfall.

Empfehlung: **zwei Skalare**. Das deckt den aktuellen Fall sauber ab und kostet fast nichts.

Effektive-Pixel-Frage: Soll "Pixelbreite" die *logische* oder die *nach Ancestor-Transformen effektive* sein? Solange Flow-Lib keine nested SVG-Transformationen mit Skalierung aufbaut, sind beide identisch. **Entscheidung:** logische Breite genügt; bei späterer Einführung nested scaling wird `zoomX/zoomY` multiplikativ durchgereicht.

## Intrinsic-Size-Stabilität **[offen, wichtig]**

Frage: Darf die intrinsische Höhe einer LOD-Box je nach gewählter Variante schwanken?

- **Ja** (gewähltes Modell): Row-Height wird über Items ermittelt; wenn Rich-Variante 40px und Compact 18px hoch ist, bekommt die Zeile bei zoom-out eine kleinere Höhe. Möglicherweise erwünscht (dichte Übersicht), möglicherweise irritierend (Zeile "zuckt" beim Zoomen).
- **Nein** (Option): `LOD` selbst deklariert einen festen `minHeight`/`fixedHeight`; alle Varianten werden dort hineingezwängt.

Empfehlung: **"Ja" mit opt-in Stabilizer** — per optionalem `fixedHeight`-Attribut auf `LOD`, sonst variabel. Der Gantt-Demo kann damit experimentieren, welches Verhalten besser wirkt.

## Nicht-Gantt-Anwendungsfälle

Damit das Konzept nicht nur Gantt-intern ist, sollten wir ≥ 2 nicht-Gantt-Fälle identifizieren, um sicher zu sein, dass das API auch dort sauber passt:

1. **`Sized`-Wrapper** — eine Box mit expliziter Breite. Wenn Inhalt LOD ist, reicht `Sized` seinen festgelegten Wert als `availableWidth` durch. Trivial.
2. **`GridLayout` mit fester Spaltenbreite** — Kind bekommt im Intrinsic-Pass die Spaltenbreite bereits als Hint. Die Zelle kann dann zwischen "Text" und "Icon+Text" wählen.
3. **Graph-Nodes bei Diagram-Zoom** — wenn der ganze Diagram-Viewport gezoomt wird (nicht Gantt-Fall, aber `FlowDiagramClientControl` hat ein Diagram-Pan/Zoom), dann reicht `DiagramOperations.draw` den Zoom als `zoomX=zoomY=currentScale` durch. Knotenboxen mit LOD können daraufhin Labels ausblenden.
4. **Tabellen-Zellen in `GridLayout`** — wenn eine Spalte schmal ist, fallback auf abgekürzten Inhalt.

## Zusammenspiel mit Axis-Providern **[offen]**

Axis-Ticks werden heute vom `AxisProvider` clientseitig erzeugt. Die haben schon eine *implizite* LOD-Logik ("bei zoom < X zeig nur Jahre, sonst Monate"). Zwei Möglichkeiten:

- **Konvergent**: AxisProvider liefern schon LOD-Boxen als Tick-Labels. Die Wahl erfolgt dann zentral im LOD-Mechanismus, AxisProvider müssen nicht mehr selbst entscheiden.
- **Parallel**: AxisProvider bleiben eigenständig (sie wissen ohnehin besser, was pro Zeitauflösung sinnvoll ist). LOD gilt nur für Item-Inhalt.

Empfehlung: **parallel**. AxisProvider treffen Entscheidungen basierend auf Tick-Dichte und Semantik (Monatsgrenzen), das ist reichhaltiger als was generisches LOD leisten kann. LOD adressiert den primären Anwendungsfall "Item-Inhalt".

## TL-Script-API (Entwurf)

```
reactFlowLOD(
    variants: list(
        reactFlowLODVariant(minWidth: 200, content: fullBox),
        reactFlowLODVariant(minWidth: 60,  content: mediumBox),
        reactFlowLODVariant(minWidth: 15,  content: iconBox),
        reactFlowLODVariant(              content: barBox)   // fallback
    )
)
```

Hilfskonstruktor für den häufigsten Fall "nur breitenbasiert":

```
reactFlowLODByWidth(
    list(200, fullBox),
    list( 60, mediumBox),
    list( 15, iconBox),
    barBox  // fallback
)
```

## Offene Fragen / Entscheidungen

| # | Frage                                                            | Empfehlung                                       |
|---|------------------------------------------------------------------|--------------------------------------------------|
| 1 | Varianten-Reihenfolge richest→compact vs umgekehrt               | richest→compact, erste erfüllte gewinnt          |
| 2 | Context-Propagation per Parameter vs Stack                       | per Parameter (`withHints`)                      |
| 3 | Ein Zoom-Skalar vs zwei vs volle Matrix                          | zwei Skalare (`zoomX`, `zoomY`)                  |
| 4 | Variante-abhängige vs fixe intrinsische Höhe                     | variabel, opt-in `fixedHeight` bei `LOD`         |
| 5 | Axis-Ticks via LOD integrieren?                                  | nein, parallel belassen                          |
| 6 | LOD als `Box`-Subtyp vs als Markierungs-Decorator (wie `Fill`)   | `Box`-Subtyp, analog zu `Stack`                  |
| 7 | Unterstützt eine Variante ihrerseits LOD (Rekursion)?            | ja — emergiert natürlich aus der Semantik        |
| 8 | Hysterese beim Varianten-Wechsel?                                | **[offen]** erst nach Nutzer-Feedback entscheiden|
| 9 | Animation zwischen Varianten?                                    | erst einmal nein; harter Snap                    |

## Was eine spätere Implementation-Plan-Iteration klären muss

- Soll die Varianten-Wahl in `distributeSize` revidiert werden dürfen (wenn `distributeSize` eine andere Breite zuteilt als die Intrinsic-Hint versprochen hat)?
- Wie reagiert der Client (`FlowDiagramClientControl`) auf Zoom-Änderungen? Aktuell löst Zoom ein vollständiges Re-Layout aus (GanttLayoutOperations.computeIntrinsicSize wird wieder gerufen) — das würde LOD automatisch neu entscheiden. Das ist der Happy Path.
- Test: scripted test, der bei verschiedenen `zoom`-Werten prüft, welche Variante aktiv ist (per `__tlWidget`-Attribut auslesbar).

## Nicht-Ziele

- Kein responsives Design auf Viewport-Ebene (Media-Queries o. ä.) — das ist außerhalb der Flow-Lib.
- Keine automatische Variantengenerierung aus einer "reichen" Variante. Varianten sind deklarativ.
- Keine Animationen / Cross-Fades zwischen Varianten (siehe offene Frage #9).
