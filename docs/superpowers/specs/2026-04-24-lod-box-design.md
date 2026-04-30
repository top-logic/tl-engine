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

1. **Die LOD-Box weiß nicht, dass es Gantt gibt.** Sie empfängt ihre Constraints (verfügbare Breite/Höhe) als Parameter und ihren Render-Kontext (Zoom, Text-Measurement) über `RenderContext`.
2. **Constraints sind per-call, Kontext ist ambient.** Verfügbare Breite/Höhe variieren von Box zu Box und gehören als Methodenparameter an `computeIntrinsicSize` — symmetrisch zu `distributeSize`. Zoom und Text-Measurement gelten für den ganzen Render-Pass und bleiben in `RenderContext`.
3. **Die Gantt-Zeile ist verantwortlich, die Constraints zu setzen.** `GanttLayout` kennt für jeden Span die exakte Pixelbreite (`(end-start) * pixelsPerUnit`) und reicht sie als `availableWidth` an `computeIntrinsicSize` weiter.
4. **Die Wahl der Variante fällt in der Intrinsic-Pass-Phase.** Die LOD-Box misst die Varianten in Reihenfolge richest→compact und nimmt die erste, deren eigener Intrinsic-Bedarf in `availableWidth` passt. Die reportete intrinsische Höhe gehört zur gewählten Variante — Zeilenhöhe stimmt, kein Zweipass-Konflikt.
5. **Außerhalb Gantt:** Wer Kinderbreite kennt (`Sized`, feste `GridLayout`-Spalten, Drag&Drop-resizable Container), setzt sie genauso als Parameter. Wer sie nicht kennt (z. B. flexibles `HorizontalLayout`), übergibt `POSITIVE_INFINITY` → LOD wählt die reichste Variante.

## Datenmodell (Entwurf)

```proto
/**
 * Container that carries multiple rendering variants and renders the one
 * that best fits the available rendering space.
 *
 * Variants are declared from richest to most compact. During intrinsic
 * sizing, each variant is measured; the first whose intrinsic width and
 * height fit into the available space (and which satisfies its optional
 * extra gates) is selected. The last variant should be a universal
 * fallback (e.g. a colored bar) that always fits.
 */
message LOD extends Box {
    repeated LODVariant variants;
}

message LODVariant {
    /** The rendered content when this variant is selected. */
    Box content;

    /**
     * Optional extra gate: minimum horizontal zoom factor (pixelsPerUnit
     * in Gantt context, SVG scaleX otherwise). 0 = no gate.
     *
     * Useful for variants whose decision is orthogonal to own box size,
     * e.g. "show week labels only when pixelsPerUnit >= 10".
     */
    double minZoom;

    /**
     * Optional extra lower bound on available width, independent of
     * intrinsic width. 0 = no gate. Use for variants that visually fit
     * into less pixels than they functionally need (e.g. a small but
     * interactive click target that requires breathing room).
     */
    double minWidth;

    /** Optional analogue to minWidth for the vertical axis. */
    double minHeight;
}
```

The **primary** fit criterion is the variant's own intrinsic size — the author does **not** declare per-variant pixel thresholds for typical text/icon variants. `minZoom`, `minWidth`, `minHeight` are optional additional gates for cases where intrinsic measurement alone doesn't capture the constraint.

### Varianten-Reihenfolge: **[entschieden]**

Reihenfolge ist **richest → most compact**, erste passende gewinnt. Begründung:

- "Passt rein" ist eine Untergrenze-an-Platz-Bedingung; richest-first liest sich als "nimm die reichste Variante, die noch reinpasst" und entspricht der Intuition.
- Frühes Abbrechen: sobald eine Variante passt, müssen die kompakteren Varianten nicht mehr gemessen werden.

## API-Erweiterung

### `RenderContext`: nur ambient

`RenderContext` bleibt ein Träger für Pass-globale Information. Es bekommt nur den Zoom dazu, alles andere (Größen-Constraints) wandert in die Methoden-Signatur:

```java
public interface RenderContext {
    TextMetrics measure(String text);

    /** Horizontaler Zoom-Faktor; 1.0 wenn unbekannt. */
    default double getZoomX() { return 1.0; }

    /** Vertikaler Zoom-Faktor; 1.0 wenn unbekannt. */
    default double getZoomY() { return 1.0; }
}
```

### `computeIntrinsicSize`: symmetrisch zu `distributeSize`

Heute (vereinfacht):

```java
void computeIntrinsicSize(RenderContext ctx, double offsetX, double offsetY);
void distributeSize(RenderContext ctx, double x, double y, double w, double h);
```

Erweiterung: `computeIntrinsicSize` bekommt ebenfalls Größen-Constraints — als *verfügbares* Maximum (`availableWidth`/`availableHeight`), nicht als Zuteilung:

```java
void computeIntrinsicSize(RenderContext ctx,
                          double offsetX, double offsetY,
                          double availableWidth, double availableHeight);

void distributeSize(RenderContext ctx,
                    double x, double y, double w, double h);
```

**Semantik:**

- `availableWidth/Height` = Obergrenze, die das Kind zur Kenntnis nehmen *darf*. `POSITIVE_INFINITY` bedeutet "Parent kennt seine Breite/Höhe noch nicht — wünsch dir, was du brauchst".
- Im Gegensatz zu `distributeSize` sind die Werte **nicht bindend**: Eine reguläre Box ignoriert sie und reportet weiterhin ihre natürliche Größe. Nur Boxen, die ihr Verhalten platzabhängig variieren wollen (LOD, später ggf. Wrap-Layouts), nutzen sie.
- Bestehende Box-Implementationen ignorieren die neuen Parameter — die Migration ist mechanisch.

**Anwendungsfälle, in denen der Parent die Constraints kennt:**

- Gantt-Span: Breite = `(end - start) * pixelsPerUnit`.
- `Sized`: Breite/Höhe explizit per Konfiguration gesetzt.
- `GridLayout` mit fester Spaltenbreite.
- Drag&Drop-resizable Container: Größe ergibt sich aus User-Interaktion.
- Viewport-bound root: Diagram-Canvas-Größe.

**Default-Verhalten** (POSITIVE_INFINITY, Zoom 1.0): LOD wählt stets die reichste Variante. Das ist das gewünschte Fallback, wenn der Parent nichts Genaueres weiß.

## LOD-Operation

```java
public interface LODOperations extends BoxOperations {
    @Override LOD self();

    @Override
    default void computeIntrinsicSize(RenderContext ctx,
                                      double offsetX, double offsetY,
                                      double availableWidth, double availableHeight) {
        double zoom = ctx.getZoomX();
        LODVariant chosen = null;
        for (LODVariant v : self().getVariants()) {
            // Optionale Zusatz-Gates zuerst — billiger als Probe-Intrinsic.
            if (zoom < v.getMinZoom()) continue;
            if (availableWidth < v.getMinWidth()) continue;
            if (availableHeight < v.getMinHeight()) continue;

            // Probe-Intrinsic der Variante: misst, wieviel Platz sie braucht.
            // availableWidth/Height werden propagiert, falls die Variante
            // selbst LOD-fähige Kinder enthält.
            Box content = v.getContent();
            content.computeIntrinsicSize(ctx, offsetX, offsetY,
                                         availableWidth, availableHeight);
            if (content.getWidth() <= availableWidth
                    && content.getHeight() <= availableHeight) {
                chosen = v;
                break;
            }
        }
        if (chosen == null) {
            // Letzte Variante als Fallback: gilt als universal passend.
            chosen = self().getVariants().get(self().getVariants().size() - 1);
            chosen.getContent().computeIntrinsicSize(ctx, offsetX, offsetY,
                                                    availableWidth, availableHeight);
        }
        self().setChosenVariant(chosen); // transient field
        self().setWidth(chosen.getContent().getWidth());
        self().setHeight(chosen.getContent().getHeight());
    }

    @Override
    default void distributeSize(RenderContext ctx, double x, double y, double w, double h) {
        Box content = self().getChosenVariant().getContent();
        self().setX(x); self().setY(y);
        self().setWidth(w); self().setHeight(h);
        content.distributeSize(ctx, x, y, w, h);
    }
}
```

Und der Draw wird über die bestehende Visitor/Dispatch-Mechanik an `chosenVariant.getContent()` delegiert.

**Kosten:** Bis zu N Probe-Intrinsics pro LOD-Box. In der Praxis bricht die Schleife bei der ersten passenden Variante ab — bei richest-first und großem `availableWidth` typischerweise nach einem Aufruf.

## Gantt-Integration

`GanttLayoutOperations.computeIntrinsicSize` (heute Zeile ~106–136) iteriert über Items und ruft `box.computeIntrinsicSize(context, tmpX, tmpY)`. Erweiterung:

```java
double zoom = axis.getCurrentZoom(); // pixelsPerUnit

// Zoom einmal pro Pass auf den Context legen.
RenderContext spanCtx = context.withZoom(zoom, /*zoomY*/ 1.0);

for (GanttItem item : self.getItems()) {
    Box box = item.getBox();
    if (box == null) continue;

    double availW;
    double availH = Double.POSITIVE_INFINITY;
    if (item instanceof GanttSpan span) {
        availW = (span.getEnd() - span.getStart()) * zoom;
    } else {
        // Punkte/sonstige Items haben keine definierte Breite.
        availW = Double.POSITIVE_INFINITY;
    }
    box.computeIntrinsicSize(spanCtx, tmpX, tmpY, availW, availH);
    // ... wie bisher.
}
```

**Wichtig:** `zoomY = 1.0` — die Gantt-Zeile zoomt vertikal **nicht**. Eine LOD-Box innerhalb eines Spans entscheidet horizontale Varianten (Textlänge), nicht vertikale.

Für **Zeilen-Labels** im Gantt-Sidebar: `availableWidth` = `columnWidth`. Zoom bleibt 1, weil der Sidebar nicht gezoomt wird.

Für **Dekorations-Labels**: wenn sie nicht an ein Item gebunden sind, haben sie typischerweise keine feste Breite — `availableWidth = POSITIVE_INFINITY`.

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

Da die Variantenwahl primär aus dem Intrinsic-Bedarf resultiert, wird der Aufruf deutlich schlanker — Varianten sind reine Box-Listen, keine Schwellen-Tupel:

```
reactFlowLOD(
    fullBox,    // wird gewählt, wenn intrinsic.w(fullBox)   <= avail
    mediumBox,  // sonst, wenn intrinsic.w(mediumBox) <= avail
    iconBox,    // sonst, wenn intrinsic.w(iconBox)   <= avail
    barBox      // Fallback (sollte immer passen)
)
```

Für die selteneren Fälle mit zusätzlichen Gates (`minZoom`, `minWidth`-Override) gibt es einen ausführlicheren Konstruktor:

```
reactFlowLODGated(
    reactFlowLODVariant(minZoom: 10, content: detailedBox),
    reactFlowLODVariant(             content: simpleBox),
    reactFlowLODVariant(             content: barBox)
)
```

## Offene Fragen / Entscheidungen

| #  | Frage                                                              | Status / Empfehlung                                |
|----|--------------------------------------------------------------------|----------------------------------------------------|
| 1  | Varianten-Reihenfolge richest→compact vs umgekehrt                 | **entschieden**: richest→compact                   |
| 2  | Constraints via `RenderContext` vs als Methodenparameter           | **entschieden**: Methodenparameter, symmetrisch zu `distributeSize` |
| 3  | Ein Zoom-Skalar vs zwei vs volle Matrix                            | zwei Skalare (`zoomX`, `zoomY`) im `RenderContext` |
| 4  | Auto-Fit vs deklarierte `minWidth`-Schwellen                       | **entschieden**: Auto-Fit primär, `minWidth`/`minZoom` als optionale Zusatz-Gates |
| 5  | Variante-abhängige vs fixe intrinsische Höhe                       | variabel, opt-in `fixedHeight` bei `LOD`           |
| 6  | Axis-Ticks via LOD integrieren?                                    | nein, parallel belassen                            |
| 7  | LOD als `Box`-Subtyp vs als Markierungs-Decorator (wie `Fill`)     | `Box`-Subtyp, analog zu `Stack`                    |
| 8  | Unterstützt eine Variante ihrerseits LOD (Rekursion)?              | ja — emergiert natürlich aus der Semantik          |
| 9  | Hysterese beim Varianten-Wechsel?                                  | **[offen]** erst nach Nutzer-Feedback entscheiden  |
| 10 | Animation zwischen Varianten?                                      | erst einmal nein; harter Snap                      |
| 11 | Verhalten in Flex-Containern, wo `availableWidth` erst in distribute bekannt ist | **[offen]**: vorerst Richest-Wahl mit `POSITIVE_INFINITY`; iteratives Reflow-Protokoll als Folgeticket |

## Was eine spätere Implementation-Plan-Iteration klären muss

- Migration der bestehenden `Box`-Implementationen auf die neue `computeIntrinsicSize`-Signatur (`availableWidth/Height` als Pflichtparameter). Default-Implementation kann die Werte ignorieren — die Migration ist mechanisch, aber breit.
- Soll die Varianten-Wahl in `distributeSize` revidiert werden dürfen (wenn `distributeSize` eine andere Breite zuteilt als der Intrinsic-Pass-Hint)? Relevant für Flex-Container (Frage #11).
- Wie reagiert der Client (`FlowDiagramClientControl`) auf Zoom-Änderungen? Aktuell löst Zoom ein vollständiges Re-Layout aus (GanttLayoutOperations.computeIntrinsicSize wird wieder gerufen) — das würde LOD automatisch neu entscheiden. Das ist der Happy Path.
- Test: scripted test, der bei verschiedenen `zoom`-Werten prüft, welche Variante aktiv ist (per `__tlWidget`-Attribut auslesbar).

## Nicht-Ziele

- Kein responsives Design auf Viewport-Ebene (Media-Queries o. ä.) — das ist außerhalb der Flow-Lib.
- Keine automatische Variantengenerierung aus einer "reichen" Variante. Varianten sind deklarativ.
- Keine Animationen / Cross-Fades zwischen Varianten (siehe offene Frage #9).
