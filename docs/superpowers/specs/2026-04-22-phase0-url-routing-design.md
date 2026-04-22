# Phase 0: URL-Routing, Param-Bindings, Bidirektionale Kanäle

**Status**: Draft
**Ticket**: #29108
**Scope**: Core-Framework-Erweiterungen in `com.top_logic.layout.view` und `com.top_logic.layout.react`
**Ziel**: URL-Routing als Querschnitts-Feature bestehender Navigations-Controls, verifiziert in der Demo-App

---

## 1. Kontext

TopLogics React-Integration hat heute **keine Browser-History-Integration**. Navigationen
(Sidebar-Item-Wechsel, Tab-Wechsel, DeckPane-Switch) sind rein zustandsbasiert —
kein `pushState`, kein `popstate`, keine bookmarkbaren URLs, kein Back-Button-Support.

Dieses Projekt führt URL-Routing als **Annotation auf bestehenden Navigations-Controls** ein.
Es ist kein neues Router-Control, sondern eine Querschnitts-Fähigkeit: jedes
Navigations-Control (Sidebar, TabBar, DeckPane, künftig Wizard) kann seine
Navigationen als route-bildend deklarieren.

### Einordnung in das Gesamtprojekt

Phase 0 ist der erste Baustein eines größeren Vorhabens: die Übersetzung eines
React-SPA-Prototypen (Immobilien-App) in eine TopLogic-View-Konfiguration.
Das Gesamtprojekt umfasst:

- **Phase 0** (dieses Dokument): URL-Routing, Param/Query-Bindings, bidirektionale Kanäle
- **Phase 1**: Theme-Modul (Luminous Noir), HeroCardGrid, ChipFilter, Page-Transitions,
  Motion-Presets, Track-A-Views (Explore, Listings, PropertyDetail)
- **Phase 2**: Track-B-Onboarding (Wizard als DeckPane-Komposition, Concierge-Server-Logik)
- **Phase 3**: Finish & Polish (Profile, Mobile-Tuning, Match-Scoring, SemanticSearch)

Phase 0 wird in der Demo-App verifiziert, ohne Estate-spezifischen Code.

---

## 2. Design-Entscheidungen (bestätigt)

| Entscheidung | Wahl | Begründung |
|-------------|------|------------|
| Routing-Paradigma | Annotation auf bestehenden Controls | Kein neues Control, arbeitet mit Sidebar/TabBar/DeckPane zusammen |
| Route-Baum | Inkrementell, session-spezifisch | Kein statisches Manifest; Route-Baum IST der Control-Baum; user-spezifische Sichten möglich |
| Route-Auflösung | Segment-weise, top-down | Jeder RoutingParticipant kennt nur seine direkten Kinder; Materialisierung bei Bedarf |
| Param-Bindings | Auf View-Ebene (`<param-bindings>`) | Konsistent mit bestehender Channel-Architektur (Kanäle leben auf `<view>`-Ebene) |
| Query-Bindings | Auf View-Ebene (`<query-bindings>`) | Analog zu Param-Bindings |
| Objekt-ID-Übersetzung | Bidirektionaler Derived-Channel | `reverse`-Attribut auf `<derived-channel>`, TL-Script in beide Richtungen |
| Datenbindung | TL-nativ (Kanäle, TL-Script) | Kein REST, keine api.js; Objekte als TLObject-Instanzen |
| LayoutComponent | Nicht beteiligt | Alles auf View/UIElement/ReactControl-Ebene |

---

## 3. Architektur

### 3.1 RoutingParticipant SPI

Neues Interface in `com.top_logic.layout.view`:

```java
/**
 * Controls that contribute URL segments implement this interface.
 * Registration/deregistration happens automatically via attach/detach lifecycle.
 */
public interface RoutingParticipant {

    /**
     * Route patterns for this participant's direct children.
     * Example: ["/explore", "/listings", "/property/:estateId"]
     */
    List<RoutePattern> declaredRoutes();

    /**
     * Activate the matching child route. May trigger lazy materialization.
     * Called by RouteManager during deep-link resolution or back-navigation.
     *
     * @param match the matched route pattern with extracted parameters
     */
    void activateRoute(RouteMatch match);

    /**
     * Currently active route segment, or null if no route-forming item is active.
     */
    RouteSegment activeRouteSegment();

    /**
     * Fires when a route-forming navigation happens within this participant.
     */
    void addRouteChangeListener(RouteChangeListener listener);
    void removeRouteChangeListener(RouteChangeListener listener);
}
```

**RoutePattern**: Immutable record, holds the pattern string (e.g., `/property/:estateId`),
the nav-item/tab/child ID it maps to, and a list of parameter names.

**RouteMatch**: Immutable record, holds the matched RoutePattern plus extracted parameter
values (e.g., `{estateId: "42"}`).

**RouteSegment**: Immutable record, holds the concrete URL segment produced by the
currently active route (e.g., `/property/42`).

### 3.2 RouteManager

Neuer Service im `ReactContext` (analog zu `DialogManager`, `ContextMenuOpener`):

```java
public class RouteManager {

    // --- Registration (called from attach/detach lifecycle) ---

    void register(RoutingParticipant participant);
    void unregister(RoutingParticipant participant);

    // --- Forward: navigation event → URL update ---

    // Called internally when a RoutingParticipant fires a route change.
    // Collects active segments from all registered participants (top-down),
    // assembles the full URL, emits RouteChangeEvent via SSE.

    // --- Backward: URL → control activations ---

    /**
     * Resolve a URL to a sequence of control activations.
     * Called on deep-link (session start) or back-navigation (popstate).
     * Walks the participant tree segment-by-segment, materializing lazy content.
     */
    void navigateToRoute(String url);

    // --- URL assembly ---

    /**
     * Current full URL assembled from all active route segments.
     */
    String currentUrl();
}
```

**Lifecycle-Integration** in `ReactControl`:

```java
// In ReactControl.attach():
@Override
protected void onAttach() {
    super.onAttach();
    if (this instanceof RoutingParticipant rp) {
        getReactContext().getRouteManager().register(rp);
    }
}

// In ReactControl.detach():
@Override
protected void onDetach() {
    if (this instanceof RoutingParticipant rp) {
        getReactContext().getRouteManager().unregister(rp);
    }
    super.onDetach();
}
```

**Reaktives Auflösungsmodell**: Der RouteManager merkt sich eine "pending URL"
(aus Deep-Link oder Back-Navigation). Bei jedem `register()` prüft er, ob
unverbrauchte URL-Segmente vorhanden sind und der neue Participant das nächste
Segment matchen kann. Falls ja, ruft er `activateRoute()` auf, was ggf.
Lazy-Materialisierung auslöst, was wiederum weitere Participants registriert.
Die Kette stoppt, wenn alle Segmente verbraucht sind oder kein Match gefunden wird.

### 3.3 Route-Deklaration in der View-Config

Neues optionales `route`-Attribut auf Navigations-Elementen:

```xml
<!-- Sidebar nav-item mit Route -->
<nav-item id="explore" icon="bi-compass" route="/explore">
  <view-ref view="demo/explore.view.xml"/>
  <label><en>Explore</en></label>
</nav-item>

<!-- Nav-item OHNE Route — kein URL-Segment, kein History-Eintrag -->
<nav-item id="admin" icon="bi-gear">
  <view-ref view="demo/admin.view.xml"/>
  <label><en>Admin</en></label>
</nav-item>

<!-- Tab mit Route -->
<tab id="overview" route="/overview">...</tab>

<!-- Tab OHNE Route -->
<tab id="notes">...</tab>

<!-- Nav-item mit Pfad-Parameter -->
<nav-item id="detail" route="/item/:itemId" hidden="true">
  <view-ref view="demo/detail.view.xml">
    <bind channel="itemId" to="itemId"/>
  </view-ref>
  <label><en>Detail</en></label>
</nav-item>
```

**Config-Erweiterung** auf `NavigationItem.Config` (und analog `TabDefinition.Config`):

```java
@Name("route")
String getRoute();  // Optional. Null = not route-forming.
```

`hidden="true"` auf einem nav-item bedeutet: das Item wird nicht in der Sidebar angezeigt,
aber es ist als Route erreichbar (z. B. `/item/:itemId` über einen Link aus einer anderen View).

### 3.4 Param-Bindings auf View-Ebene

Neue XML-Elemente in `ViewElement.Config`:

```xml
<!-- detail.view.xml -->
<view>
  <channels>
    <channel name="selectedItem"/>
    <derived-channel name="itemId"
        inputs="selectedItem"
        expr="item -> objectId($item)"
        reverse="id -> resolve(`my.module:MyType`, $id)"/>
  </channels>

  <param-bindings>
    <bind channel="itemId" route-param="itemId"/>
  </param-bindings>

  <content>
    <form input="selectedItem">
      <field attribute="name"/>
    </form>
  </content>
</view>
```

**ParamBindingConfig**:

```java
@TagName("bind")
public interface ParamBindingConfig extends ConfigurationItem {
    @Name("channel")
    @Mandatory
    String getChannel();      // View-Kanal, der den Wert hält

    @Name("route-param")
    @Mandatory
    String getRouteParam();   // Name des URL-Pfad-Parameters (aus :paramName)
}
```

**Bidirektionale Auflösung:**

- **URL → View** (Deep-Link/Back): RouteManager parsed den Pfad-Parameter-Wert
  aus der URL, schreibt ihn in den Kanal (`itemId`). Der `reverse`-Ausdruck des
  bidirektionalen Derived-Channel propagiert zum Quellkanal (`selectedItem`).
  Die View materialisiert mit dem aufgelösten TLObject.

- **View → URL** (Navigation): Benutzer klickt ein Item in einer Tabelle →
  Kanal `selectedItem` ändert sich → `expr` (forward) propagiert zu `itemId` →
  ParamBinding informiert den RouteManager → URL-Update via SSE.

### 3.5 Query-Bindings auf View-Ebene

```xml
<!-- listings.view.xml -->
<view>
  <channels>
    <channel name="typeFilter"/>
    <channel name="roomsFilter"/>
  </channels>

  <query-bindings>
    <bind channel="typeFilter"  query-param="type"/>
    <bind channel="roomsFilter" query-param="rooms"/>
  </query-bindings>

  <content>
    <!-- Filter-Controls schreiben in die Kanäle -->
    ...
  </content>
</view>
```

**QueryBindingConfig** analog zu `ParamBindingConfig`, mit `query-param` statt `route-param`.

**Konvention:** Query-Param-Änderungen nutzen `history.replaceState` (kein neuer
History-Eintrag), Pfad-Änderungen nutzen `history.pushState`. Damit flutet
jede Filter-Eingabe nicht den History-Stack.

### 3.6 Bidirektionaler Derived-Channel

Erweiterung der bestehenden `DerivedChannelConfig`:

```java
@TagName("derived-channel")
public interface DerivedChannelConfig extends ChannelConfig {
    // Bestehend:
    @Name("inputs")
    @Format(CommaSeparatedChannelRefs.class)
    List<ChannelRef> getInputs();

    @Name("expr")
    @Mandatory
    Expr getExpr();              // forward: inputs → derived value

    // NEU:
    @Name("reverse")
    Expr getReverse();           // Optional. reverse: derived value → source value
}
```

**Semantik:**

- Ohne `reverse`: Kanal ist unidirektional (bestehend, rückwärtskompatibel).
- Mit `reverse`: Kanal ist bidirektional. Schreibzugriffe auf den abgeleiteten
  Kanal propagieren über den `reverse`-Ausdruck zurück zum Quellkanal.
- `reverse` bekommt den neuen Wert des abgeleiteten Kanals als Argument und
  muss den Wert des Quellkanals zurückgeben.
- Bei mehreren Inputs (`inputs="a,b"`) ist `reverse` nur sinnvoll, wenn es den
  *primären* Input zurückschreibt. Die Konvention: `reverse` schreibt in den
  *ersten* Input der `inputs`-Liste.

**Beispiel — TLObject ↔ String-ID:**

```xml
<derived-channel name="estateId"
    inputs="selectedEstate"
    expr="estate -> objectId($estate)"
    reverse="id -> resolve(`EstateManagement.Sales:Estate`, $id)"/>
```

Die konkreten TL-Script-Ausdruecke sind anwendungsspezifisch. Anwendungen
verwenden fachliche Schluessel (z. B. das `name`-Attribut eines Objekts),
nicht interne Objekt-IDs. Das Framework macht keine Annahmen ueber die
ID-Semantik — der TL-Script-Ausdruck entscheidet.

### 3.7 SSE-Erweiterungen

Neuer Event-Typ `RouteChangeEvent`:

```java
// In SSEEvent.TypeKind enum:
ROUTE_CHANGE_EVENT,
ROUTE_VETO_EVENT
```

**RouteChangeEvent** (Server → Client):
```json
["RouteChangeEvent", {"url": "/property/42", "replace": false}]
```

**RouteVetoEvent** (Server → Client, wenn Dirty-Veto greift):
```json
["RouteVetoEvent", {"currentUrl": "/listings?type=apartment"}]
```

Client-Seite empfängt `RouteVetoEvent` und korrigiert die URL zurück via
`history.replaceState`, damit die Adressleiste den tatsächlichen Zustand zeigt.

### 3.8 Client-Seite: routeSync-Modul

Neues Bridge-Modul in `tl-react-bridge` (kein Control, globaler Singleton):

```typescript
// bridge/route-sync.ts

import { sseClient, sendCommand } from './sse-client';

export function initRouteSync(): void {
    // Server → Browser URL
    sseClient.on('RouteChangeEvent', (event: RouteChangeEvent) => {
        if (event.replace) {
            history.replaceState(null, '', event.url);
        } else {
            history.pushState(null, '', event.url);
        }
    });

    // Server → URL correction after veto
    sseClient.on('RouteVetoEvent', (event: RouteVetoEvent) => {
        history.replaceState(null, '', event.currentUrl);
    });

    // Browser Back/Forward → Server
    window.addEventListener('popstate', () => {
        sendCommand('navigateToRoute', {
            url: location.pathname + location.search
        });
    });
}
```

`navigateToRoute` ist ein globaler Command, der vom RouteManager verarbeitet wird.

### 3.9 Dirty-Channel-Integration

Wenn der Benutzer Back klickt und die aktuelle View dirty ist:

1. Client sendet `navigateToRoute` mit der Ziel-URL
2. RouteManager ruft `activateRoute()` auf dem betroffenen RoutingParticipant auf
3. Der Participant prüft den Dirty-Channel (bestehende Mechanik, z. B. in
   `ReactSidebarControl.handleSelectItem`)
4. Bei Veto: `ChannelVetoException` wird geworfen
5. RouteManager fängt das Veto und sendet `RouteVetoEvent` (URL-Korrektur)
6. Optional: Bestätigungsdialog wird angezeigt, danach ggf. forced Navigation

Die bestehende Dirty-Channel-Mechanik in `ReactSidebarControl` und
`ReactTabBarControl` bleibt unverändert. Der RouteManager ruft die gleichen
Methoden auf, die auch ein normaler Klick aufruft.

### 3.10 Deep-Link-Aufloesung beim Session-Start

**URL-Struktur:** Die ViewServlet-URL hat aktuell die Form
`/{contextPath}/view/{windowName}/`. Der Window-Name ist session-spezifisch
(`v1a2b3c`, erzeugt per `crypto.getRandomValues()` im Bootstrap-JS) und damit
nicht teilbar/bookmarkbar.

**Aenderung:** Der Window-Name bleibt intern im URL-Pfad (Server-seitig),
wird aber per `history.replaceState` aus der Adressleiste entfernt.

**Sichtbare URL** (was der User sieht): `/{contextPath}/view/property/42`
**Server-URL** (was der Server verarbeitet): `/{contextPath}/view/{windowName}/property/42`

**Subsession-Mechanik:**
- `window.name` wird vom Bootstrap-JS erzeugt und ueberlebt F5 im selben Tab
- Bei Tab-Clone: `window.name` wird NICHT mitgeklont → Bootstrap erzeugt
  eine neue Subsession mit demselben Navigations-Pfad
- F5 kostet einen Extra-Roundtrip (Bootstrap → `window.name` lesen → Redirect
  mit Window-Name → Server baut den State)

**Sequenz bei direktem Einstieg ueber URL `/demo/view/item/42`:**

```
1. GET /demo/view/item/42 (kein Window-Name im Pfad)
2. ViewServlet: kein Window-Name → Bootstrap-Page ausliefern
3. Bootstrap-JS: window.name lesen (existiert bei F5) oder neu erzeugen
4. Redirect zu /demo/view/v1a2b3c/item/42
5. ViewServlet: Window-Name v1a2b3c + Route "item/42"
   → Session + SubSession fuer Window v1a2b3c
6. ReactContext + RouteManager werden erzeugt
   RouteManager.pendingUrl = "item/42"
7. View-Config laden → AppShell aufbauen → Content-Slot materialisiert
8. RoutingParticipant.attach() → register()
9. RouteManager: pending URL hat unverbrauchte Segmente
   → prueft declaredRoutes(): findet /item/:itemId
   → activateRoute() → Lazy-Materialisierung
10. Param-Bindings greifen, Kanaele werden befuellt
11. Erster SSE-Flush: Client rendert den Detail-Screen
12. Client: history.replaceState(null, '', '/demo/view/item/42')
    → Window-Name verschwindet aus der Adressleiste
```

Ab diesem Zeitpunkt schreiben alle Route-Aenderungen (`pushState`/`replaceState`)
URLs **ohne** Window-Name. Saubere, teilbare, bookmarkbare URLs.

**F5:** URL ist `/demo/view/item/42` (ohne Window-Name) → Schritt 1-12 erneut.
`window.name` existiert noch → selbe Subsession wird wiederhergestellt.

**Tab-Clone:** `window.name` nicht kopiert → neue Subsession → selber
Navigations-Pfad wird frisch aufgeloest.

**Bookmark/Teilen:** URL ohne Window-Name → funktioniert als neuer Einstiegspunkt.

### 3.11 URL-Komposition aus verschachtelten Participants

Mehrere RoutingParticipants in der Hierarchie produzieren zusammengesetzte URLs:

```
AppShell
  └─ Sidebar (RoutingParticipant)
       └─ "explore" (route="/explore")
            └─ explore.view.xml
                 └─ TabBar (RoutingParticipant)
                      ├─ "featured" (route="/featured") → URL: /explore/featured
                      └─ "nearby" (route="/nearby")     → URL: /explore/nearby
```

Der RouteManager traversiert bei jeder Route-Änderung alle registrierten
Participants top-down (Eltern vor Kindern) und konkateniert deren
`activeRouteSegment()` zur vollen URL. Query-Params werden vom innersten
aktiven Participant angehängt.

### 3.12 Abgrenzungen

- **Dialoge**: kein Route-Verhalten. Back-Button schließt keine Dialoge.
- **Verschachtelte Routen**: beliebige Tiefe (durch hierarchische Komposition),
  aber keine `Outlet`-Semantik. Jeder Route-Level hat genau ein aktives Child.
- **Wildcard-Routen / 404**: Default-Route via `<nav-item id="not-found" route="*">`.
- **Route-Guards / Redirects**: kein dedizierter Mechanismus; Navigation per
  Command-Action (z. B. Threshold-Screen navigiert programmatisch basierend
  auf Benutzer-Erkennung).

---

## 4. Betroffene Module

| Modul | Änderungen |
|-------|-----------|
| `com.top_logic.layout.view` | `RoutingParticipant` SPI, `RoutePattern`, `RouteMatch`, `RouteSegment`, `ParamBindingConfig`, `QueryBindingConfig`, `DerivedChannelConfig` (reverse-Attribut), bidirektionale Channel-Runtime |
| `com.top_logic.layout.react` | `RouteManager` im `ReactContext`, `RouteChangeEvent`/`RouteVetoEvent` SSE-Events, `navigateToRoute` globaler Command, `routeSync` Bridge-Modul, `onAttach`/`onDetach` Registration |
| `com.top_logic.layout.react` (Controls) | `ReactSidebarControl`: `RoutingParticipant`-Implementierung + `route`-Attribut auf `NavigationItem.Config`; analog `ReactTabBarControl`, `ReactDeckPaneControl` |
| `com.top_logic.demo` | Demo-Views mit Route-Attributen, Param-Binding, Query-Binding, bidirektionalem Channel; manuelles Testen von Deep-Links, Back-Button, Dirty-Veto |

---

## 5. Demo-Verifikationsszenarien

### 5.1 Basis-Routing

- Demo-Sidebar hat 3 nav-items mit `route`-Attributen (`/dashboard`, `/forms`, `/tables`)
- Klick auf nav-item ändert die URL in der Adressleiste
- Back-Button kehrt zum vorherigen nav-item zurück
- Forward-Button geht wieder vorwärts
- Direkter Aufruf von `/forms` per URL zeigt die Forms-Sicht

### 5.2 Param-Binding mit bidirektionalem Channel

- Demo-View: Tabelle mit Objekten, Klick auf ein Objekt navigiert zu `/item/:id`
- URL zeigt `/item/42` (mit korrekter ID)
- Bookmark `/item/42` öffnen: Detail-Sicht zeigt das richtige Objekt
- Back von `/item/42` zeigt die Tabelle

### 5.3 Query-Binding

- Demo-View: Tabelle mit Filter-Dropdown
- Filter auf "TypeA" setzen: URL ändert sich zu `/tables?type=TypeA`
  (kein neuer History-Eintrag, replaceState)
- Bookmark `/tables?type=TypeA` öffnen: Filter ist vorausgewählt

### 5.4 Dirty-Veto

- Demo-View: Formular editieren (dirty), dann Back-Button drücken
- Navigation wird verhindert, URL bleibt korrekt
- Bestätigungsdialog (falls implementiert) oder stilles Veto

### 5.5 Verschachtelte Routen

- Demo: Sidebar-Item "Explore" → enthält TabBar mit Tabs "Featured" und "Nearby"
- Tab "Featured" hat route="/featured": URL wird `/explore/featured`
- Tab "Nearby" hat route="/nearby": URL wird `/explore/nearby`
- Back von `/explore/nearby` → `/explore/featured`
- Bookmark `/explore/nearby` → öffnet Explore mit Nearby-Tab aktiv

### 5.6 Hidden Route (Programmtische Navigation)

- Demo: Tabelle mit Klick-Action, die zu `/item/:id` navigiert
- Nav-item mit `hidden="true"` ist nicht in der Sidebar sichtbar
- URL `/item/42` funktioniert trotzdem als Deep-Link

---

## 6. Geklaerte Fragen

1. **`objectId()` / `resolve()`**: Ausgeklammert fuer Phase 0. Anwendungen
   verwenden fachliche Schluessel (z. B. `name`-Attribut), nicht interne
   Objekt-IDs. Der TL-Script-Ausdruck im `reverse`-Attribut ist frei
   konfigurierbar — die Demo kann ein einfaches Attribut-Lookup verwenden.

2. **Initial-URL-Transport**: ViewServlet extrahiert den Route-Teil aus dem
   HTTP-Request-Pfad (alles nach `/view/`). Der Window-Name fliesst ueber
   `window.name` (Browser-Property) + Custom-Header, nicht ueber den URL-Pfad.
   Siehe Sektion 3.10.

3. **Relative vs. absolute Routen**: Nur relative Routen. Absolute Routen
   wuerden die Kompositionsfaehigkeit verschachtelter Participants brechen.

## 7. Offene Fragen (Edge-Cases, zur Implementierungszeit)

1. **Mehrere RoutingParticipants auf gleicher Ebene**: Kann das passieren?
   Falls ja: welcher hat Vorrang beim Segment-Matching?

2. **URL-Encoding**: Wie werden Sonderzeichen in Param-Werten behandelt?
   Standard-URL-Encoding?

3. **`hidden`-Attribut auf NavigationItem.Config**: Muss als Config-Property
   spezifiziert werden (existiert noch nicht).
