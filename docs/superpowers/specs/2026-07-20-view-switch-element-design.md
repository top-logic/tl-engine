# Design: Deklaratives `<switch>`-Element für die `.view.xml`-Schicht

**Datum:** 2026-07-20
**Modul:** `com.top_logic.layout.view` (ggf. `com.top_logic.layout.react`)
**Ausgangspunkt:** `com.top_logic.dev.tools/src/main/webapp/WEB-INF/views/admin/model/model-editor.view.xml`

## Problem / Motivation

Im Modell-Editor (`model-editor.view.xml`) zeigt das Details-Panel ein `<form input="selectedPart">`,
das auf den Kanal `selectedPart` reagiert. Es soll möglich sein, **abhängig vom selektierten Objekt**
eine andere Sicht zu konfigurieren — z.B. für eine selektierte Klasse eine andere Detailsicht als für
ein selektiertes Attribut.

Die `.view.xml`-Schicht bietet dafür heute **keinen** Baustein:

- Es gibt kein `<switch>` / `<case>` / `<if>` / `<conditional>`.
- Kein View-Element besitzt eine `visible` / `render-if`-Expression (`visible-if` etc. sind reine
  Kommando-Sichtbarkeitsregeln, keine Inhalts-Umschaltung).
- Nächste vorhandene Primitive: `<adaptive-detail>` (selektions-/viewport-getrieben, **nicht**
  typ-/wert-getrieben) und `<access-control>` (security-getrieben). Beide passen nicht.
- Das interne `ReactDeckPaneControl` (`com.top_logic.layout.react`) macht bereits „zeige genau ein
  Kind aus einer Liste, gesteuert durch einen server-seitigen aktiven Index", mit lazy Erzeugung und
  Caching — es fehlt ihm aber ein deklaratives `@TagName`-UIElement.

## Ziel

Ein neues, allgemein wiederverwendbares `<switch>`-Element, das abhängig von einem Kanalwert genau
eine von mehreren konfigurierten Sichten anzeigt. Die Fallunterscheidung erfolgt über eine
TL-Script-Boolean-Expression pro Fall (erster Treffer gewinnt), optional mit Default.

## Gewählter Ansatz

**`SwitchElement` auf Basis von `ReactDeckPaneControl`.**

Alternativen und warum verworfen:

- **Eigenes neues React-Control**, das genau ein Kind rendert: mehr Code, kein Vorteil gegenüber der
  Wiederverwendung von `ReactDeckPaneControl`. Verworfen.
- **Kein Switch, stattdessen `<adaptive-detail>` / Umbau:** erfüllt die Anforderung nicht, da nicht
  typ-/wert-getrieben. Verworfen.

## Design

### Neues Element `<switch>`

Paket `com.top_logic.layout.view.element`, Klasse `SwitchElement implements UIElement`,
`@TagName("switch")`. Aufbau analog zu `AdaptiveDetailElement` (das ebenfalls `ChannelRef`-Bindung und
`@TreeProperty`-Kindlisten nutzt).

**Config-Properties:**

- `input` (`ChannelRef`, `@Mandatory`, `@Format(ChannelRefFormat.class)`) — der Kanal, dessen aktueller
  Wert jeder Fall-Test-Expression als Argument übergeben wird.
- `cases` (`@DefaultContainer`, `List<CaseConfig>`) — die Fälle in Reihenfolge.
- `default` (optional, `@TagName("default")`) — Inhalt, falls kein Fall matcht.

**`CaseConfig`** (`@TagName("case")`):

- `test` (`Expr`, `@Mandatory`, `@NonNullable`) — TL-Script-Boolean-Expression. Erhält den aktuellen
  Wert des `input`-Kanals als (einziges) Positionsargument, analog zum Muster in
  `DerivedChannelConfig` (Kanalwerte → Positionsargumente des Ausdrucks) und zu den
  Executability-Expressions (`visible-if`).
- Inhalt: `@TreeProperty List<PolymorphicConfiguration<? extends UIElement>>` (wie
  `AdaptiveDetailElement#getDetail()`). Damit ist als Fall-Inhalt jedes View-Element erlaubt, inkl.
  `<tab-bar>`, `<form>`, `<split-panel>` usw.

**`DefaultConfig`** (`@TagName("default")`): nur die Inhalts-Kindliste, kein `test`.

### Semantik

- Beim Erzeugen und bei **jeder Änderung** des `input`-Kanals: Tests der Reihe nach auswerten; der
  **erste** truthy Treffer bestimmt den aktiven Fall. Matcht keiner, wird `<default>` gezeigt; fehlt
  auch der, ein leerer Pane.
- Umschalten erfolgt **nur bei Wechsel des Treffer-Falls**. Bleibt derselbe Fall aktiv und ändert sich
  nur das selektierte Objekt (z.B. Attribut A → Attribut B), schaltet der Switch nicht um — das an den
  Kanal gebundene Fall-Formular aktualisiert sich selbst.
- Inhalte werden **lazy** erzeugt und pro Fall gecacht (durch `ReactDeckPaneControl`), sodass z.B. ein
  Editiermodus beim Hin- und Herschalten erhalten bleibt.

### Implementierung (`createControl`)

- `input`-Kanal via `ViewContext#resolveChannel(ChannelRef)` auflösen (wie `AdaptiveDetailElement`).
- Für jeden Fall (+ Default + leerer Fallback) eine `ReactDeckPaneControl.ChildFactory` bereitstellen,
  die den jeweiligen Fall-Inhalt lazy als Control erzeugt.
- Test-Expressions mit demselben Mechanismus kompilieren/auswerten, den die View-Schicht bereits für
  `Expr` verwendet (siehe `DerivedChannelFactory` / Executability-Auswertung). Der aktive Index wird
  aus dem ersten truthy Test berechnet.
- Einen Listener am `input`-Kanal registrieren: bei Wertänderung Index neu berechnen und — nur bei
  Indexwechsel — `ReactDeckPaneControl#selectChild(index)` aufrufen.
- Fallback für „kein Treffer, kein Default": stets ein zusätzlicher leerer Pane, damit die
  Deck-Bedingung „nicht leer" erfüllt ist.

### Verdrahtung im Modell-Editor

Im Details-Pane von `model-editor.view.xml` (aktuell Zeilen 401–417) wird der Panel-Inhalt durch ein
`<switch>` ersetzt. Für den konkreten Demonstrationsfall zeigen **beide** Zweige eine einfache Form
(keine Tabbar):

```xml
<panel>
  <title>
    <en>Details</en>
    <de>Details</de>
  </title>
  <switch input="selectedPart">
    <case test="p -> $p != null &amp;&amp; $p.instanceOf(`tl.model:TLClass`)">
      <form input="selectedPart" withEditMode="true">
        <field attribute="name"/>
      </form>
    </case>
    <default>
      <form input="selectedPart" withEditMode="true">
        <field attribute="name"/>
      </form>
    </default>
  </switch>
</panel>
```

Ergebnis: Ist eine Klasse selektiert, greift der `TLClass`-Fall; sonst (Attribut / Modul / nichts) der
Default. Die generelle Fähigkeit, im `TLClass`-Fall stattdessen eine `<tab-bar>` zu konfigurieren,
bleibt durch das Element erhalten und ist nur in diesem konkreten Beispiel nicht genutzt.

## Betroffene Dateien

- **Neu:** `com.top_logic.layout.view/src/main/java/com/top_logic/layout/view/element/SwitchElement.java`
  (Element + `CaseConfig` + `DefaultConfig`).
- **Ggf. neu/geändert:** ein `ReactControl`, das `ReactDeckPaneControl` an den Kanal-Listener koppelt
  (falls nicht direkt in `SwitchElement#createControl` erledigbar).
- **Geändert:** `com.top_logic.dev.tools/.../admin/model/model-editor.view.xml` (Details-Pane).
- **Ggf.:** `messages_*.properties` in `com.top_logic.layout.view`, falls neue I18N-Keys für Labels
  entstehen (bei diesem Design voraussichtlich keine).

## Verifikation

1. Bauen: `com.top_logic.layout.view` (und `com.top_logic.layout.react`, falls dort geändert) via
   `mvn install -pl …` vom Projekt-Root.
2. Manuell mit Playwright in `com.top_logic.demo.react` (React-View-Testbett laut CLAUDE.md):
   Modell-Editor öffnen, im Diagramm eine Klasse vs. ein Attribut selektieren und prüfen, dass der
   `<switch>` den jeweils passenden Zweig zeigt und der Editiermodus beim Umschalten erhalten bleibt.

## Offene Detailpunkte (Defaults gesetzt)

- **Kein Treffer / kein Default:** leerer Pane (kein Fehler).
- **`input` nur für Tests:** Fall-Inhalte binden ihre Kanäle selbst; `input` steuert ausschließlich die
  Fallauswahl.
- **Fehler in einer Test-Expression:** wird wie sonst bei `Expr`-Auswertung in der View-Schicht
  behandelt (Logging / Fehleranzeige); kein spezielles Handling in diesem Design.
