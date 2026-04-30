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

## Zusammenspiel mit Axis-Providern **[entschieden — konvergent]**

Axis-Ticks werden heute vom serverseitigen `AxisProvider` (Java-Interface, registriert in `AxisProviderService`, ausgewählt über `GanttAxis.providerId`) erzeugt: pro Aufruf `(rangeMin, rangeMax, pixelsPerUnit) → AxisContent(rows, items)`. Die Methode hat schon eine *implizite* LOD-Logik ("bei zoom < X zeig nur Jahre, sonst Monate"), eingebrannt in Java.

**Beobachtung:** `AxisProvider` ist konzeptionell schon ein Box-Generator — er liefert `Text.create()...`-Items. Was er an Eigenleistung erbringt, ist (a) Tick-Enumeration in einem Range, (b) Granularitätswahl über Zoom. Beides lässt sich in Flow-Mechanik ausdrücken, sobald LOD steht — die visuelle Tick-Erzeugung wird damit komplett zu regulärem Diagrammaufbau.

### Phase-1-Vorgehen: pure Flow-Mechanik

Achseninhalt wird durch reguläre `GanttSpan`/`GanttPoint`-Items mit LOD-Boxen als Inhalt ausgedrückt. Beispiel für die Demo (Tage-seit-Epoch-Achse):

```
// Year row: ein GanttSpan pro Jahr, Inhalt zoom-adaptiv
GanttSpan jan-dec 2024
   .box = LOD(
              Text("2024"),     // reichste Variante
              Text("'24"),      // mittel
              empty             // Fallback
           )

// Month row: ein GanttPoint pro Monatsanfang
GanttPoint 2024-03-01
   .box = LOD(
              Text("March 2024"),
              Text("Mar"),
              Text("M"),
              empty
           )

// Day row: ein GanttPoint pro Tag
GanttPoint 2024-03-15
   .box = LOD(
              Text("Fri 15"),
              Text("15"),
              tickMark(),
              empty
           )
```

**Inter-Layer-Suppression emergiert** aus der LOD-Wahl pro Tick: bei `zoom = 0.5 px/day` ist ein Monat 15 px breit, keine Variante außer Empty passt — die Monatszeile zeigt nur leere Inhalte. Bei `zoom = 50 px/day` ist ein Tag 50 px breit, "Fri 15" passt, die Tageszeile materialisiert. Keine `activeWhen`-Schwellen, keine zentrale Dispatch-Logik nötig.

### Was bleibt vom AxisProvider

Die Java-Schnittstelle `AxisProvider` schrumpft auf **Achsen-Semantik**, nicht mehr Achsen-*Inhalt*:

- **Snap-Granularität** (`snapGranularity(pixelsPerUnit) → double`) — Metadaten zur Achse, vom Client für Drag-Rounding gebraucht. Kein Box-Inhalt, also keine Auflösung in Flow-Mechanik. Bleibt auf der Schnittstelle (oder wandert auf `GanttAxis` als Skalar/Recipe).
- **Position ↔ Domain-Wert-Konvertierung** — `toPosition(date) → double`, `fromPosition(double) → date`. Wird für Drag&Drop-Snap, Tooltips, Server-Verifikation gebraucht. Bleibt anwendungsseitig, getrennt vom visuellen Achsenaufbau.

Die *Tick-Erzeugung* verschwindet aus `AxisProvider`. Eine Anwendung produziert Achsen-Items zusammen mit den fachlichen Items beim Modellaufbau — durch denselben Mechanismus, mit dem sie heute Items erzeugt (Java/TL-Script/Modell-Konfiguration), mit LOD-Boxen als Inhalt.

### Lazy-Materialisierung als Folge-Optimierung

Statisches Materialisieren aller Ticks aller Granularitäten ist für kleine bis mittlere Diagramme unproblematisch. Für große Ranges (10 Jahre Tagesticks ≈ 3650 Items, davon 99 % nie sichtbar) brauchen wir später eine **Tick-Grammatik** als Datenstruktur: geschlossenes Vokabular `unit ∈ {YEAR, QUARTER, MONTH, WEEK, DAY, HOUR, MINUTE}` plus Anker und Label-Format. Der zugehörige Iterator wird als gewöhnlicher Java-Code in `flow.common` implementiert — läuft client- *und* serverseitig (PDF-Generierung) — und materialisiert Ticks im sichtbaren Range on-demand.

Dies ist **nicht Phase 1**. Erst wenn die pure-Flow-Mechanik-Lösung steht und gemessene Range-Größen die Optimierung verlangen.

### Eject-Hatch für domänenspezifische Achsen

Für Achsen, die das Tick-Grammatik-Vokabular nicht abdeckt (Sprints, Fiskal-Quartale mit anwendungsspezifischer Semantik), bleibt der Weg "Anwendung emittiert Items in TL-Script oder Java beim Modellaufbau" offen — das ist der Phase-1-Default ohne Sonderfall. Eine eigene serverseitige TL-Script-Funktion, die nur für Achsen aufgerufen wird, ist nicht nötig.

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
| 6  | Axis-Ticks via LOD integrieren?                                    | **entschieden**: ja, konvergent. Achseninhalt = reguläre Items mit LOD-Boxen; `AxisProvider` schrumpft auf Snap + Position-Konversion |
| 7  | LOD als `Box`-Subtyp vs als Markierungs-Decorator (wie `Fill`)     | `Box`-Subtyp, analog zu `Stack`                    |
| 8  | Unterstützt eine Variante ihrerseits LOD (Rekursion)?              | ja — emergiert natürlich aus der Semantik          |
| 9  | Hysterese beim Varianten-Wechsel?                                  | **[offen]** erst nach Nutzer-Feedback entscheiden  |
| 10 | Animation zwischen Varianten?                                      | erst einmal nein; harter Snap                      |
| 11 | Verhalten in Flex-Containern, wo `availableWidth` erst in distribute bekannt ist | **[offen]**: vorerst Richest-Wahl mit `POSITIVE_INFINITY`; iteratives Reflow-Protokoll als Folgeticket |

## Validierung in Phase 1

Drei Punkte, die der erste Prototyp verproben muss, bevor die Konvergenz "Achse = LOD-Items" als tragfähig gilt:

1. **Leerzeilen-Kollaps.** Eine Tageszeile, deren Ticks bei niedrigem Zoom alle die Empty-Variante wählen, soll keine Row-Höhe belegen. Frage: kollabiert `GanttLayout.computeIntrinsicSize` automatisch korrekt, sobald die Row-Höhe aus den maximalen Item-Intrinsic-Höhen entsteht (Empty-Box hat Höhe 0 → Row-Höhe 0)? Oder brauchen wir eine explizite "Row sichtbar wenn mindestens ein Item nicht-leer"-Regel? Verprobungsschritt: Demo mit drei Tick-Zeilen (Year/Month/Day) bauen, durch Zoom-Range fahren, Row-Heights inspizieren.

2. **Snap-Granularität in Phase 1.** `snapGranularity(pixelsPerUnit)` ist Metadaten, kein Box-Inhalt — geht nicht in Flow-Mechanik auf. Phase-1-Vorschlag: bleibt vorerst ein einfacher Skalar auf `GanttAxis` (z. B. immer 1.0 für die Demo). Folgeüberlegung: später ein deklaratives Recipe "snap = die Periode der feinsten visuell aktiven Zeile" — allerdings erst, *nachdem* wir verstanden haben, wie sich "visuell aktive Zeile" sauber aus LOD ableitet (siehe Punkt 1).

3. **Position ↔ Domain-Wert-Konvertierung.** Drag&Drop-Snap, Tooltips, Server-seitige Verifikation brauchen die Konvertierung "Position 19432 ↔ 2023-03-15". Das ist nicht visuell — es ist semantisch. Phase-1-Vorschlag: bleibt eine schmale anwendungsseitige API (im Rumpf des heutigen `AxisProvider` oder als separates `AxisSemantics`-Interface), getrennt vom visuellen Achsenaufbau. Konsequenz: aus `AxisProvider` wird nicht "weg", sondern "schrumpft auf Position-Konversion + Snap"; die `buildAxis(...)`-Methode entfällt.

Wenn alle drei Punkte sauber durchlaufen, ist die Konvergenz validiert und die Lazy-Materialisierung (Tick-Grammatik-Iterator) eine reine Performance-Folgearbeit.

## Was eine spätere Implementation-Plan-Iteration klären muss

- Migration der bestehenden `Box`-Implementationen auf die neue `computeIntrinsicSize`-Signatur (`availableWidth/Height` als Pflichtparameter). Default-Implementation kann die Werte ignorieren — die Migration ist mechanisch, aber breit.
- Soll die Varianten-Wahl in `distributeSize` revidiert werden dürfen (wenn `distributeSize` eine andere Breite zuteilt als der Intrinsic-Pass-Hint)? Relevant für Flex-Container (Frage #11).
- Wie reagiert der Client (`FlowDiagramClientControl`) auf Zoom-Änderungen? Aktuell löst Zoom ein vollständiges Re-Layout aus (GanttLayoutOperations.computeIntrinsicSize wird wieder gerufen) — das würde LOD automatisch neu entscheiden. Das ist der Happy Path.
- Test: scripted test, der bei verschiedenen `zoom`-Werten prüft, welche Variante aktiv ist (per `__tlWidget`-Attribut auslesbar).
- Migration der heutigen `AxisProvider`-Implementationen: `buildAxis(...)` entfällt; die Demo-Achse `DaysSinceEpochAxisProvider` wird in eine Modell-Aufbau-Funktion überführt, die Tick-Items mit LOD-Boxen emittiert. Position-Konvertierung und `snapGranularity` bleiben oder wandern in ein eigenes (kleineres) Interface.

## Nicht-Ziele

- Kein responsives Design auf Viewport-Ebene (Media-Queries o. ä.) — das ist außerhalb der Flow-Lib.
- Keine automatische Variantengenerierung aus einer "reichen" Variante. Varianten sind deklarativ.
- Keine Animationen / Cross-Fades zwischen Varianten (siehe offene Frage #9).
