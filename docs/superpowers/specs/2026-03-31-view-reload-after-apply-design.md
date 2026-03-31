# View-Reload nach Apply im View Designer

**Date**: 2026-03-31
**Ticket**: #29108
**Parent spec**: `2026-03-27-view-designer-design.md`
**Status**: Design

## Problem

Der View Designer schreibt geänderte Konfigurationen in `.view.xml`-Dateien. Die laufende
Anwendung im Hauptfenster zeigt aber weiterhin den alten Stand, weil der bereits instanziierte
Control-Baum keine Verbindung zu den Quelldateien hat. Ein Full-Page-Reload ist nicht
akzeptabel, da dabei Client-State (Tab-Positionen, Scroll-State, offene Dialoge) verloren
geht und WYSIWYG-Editing unmöglich wird.

## Lösung: ReloadableControl an View-Grenzen

An jeder Stelle, an der eine `.view.xml`-Datei in den Control-Baum eingeht, wird ein
`ReloadableControl`-Wrapper eingefügt. Dieser kennt seinen View-Pfad und kann bei Bedarf
seinen inneren Control-Baum neu aufbauen, ohne dass der Rest der Anwendung betroffen ist.

### Design-Entscheidungen

| Entscheidung | Wahl | Begründung |
|---|---|---|
| Swap-Granularität | Pro View-Datei | Natürliche Grenze, jede `.view.xml` ist eine Einheit |
| Swap-Point | `ReloadableControl` Wrapper | Stabiler Anker im Baum, Parent sieht nur den Wrapper |
| State-Erhalt | ViewContext wird wiederverwendet | Channel-Werte (Dialog-Input, Live-Bindings) bleiben erhalten |
| Benachrichtigung | Listener-Pattern am Root-ViewContext | Dezentral, folgt bestehendem Dirty-Channel-Pattern |
| Designer → App | App-ViewContext als Channel durchschleifen | `OpenDesignerCommand` kennt den App-Kontext bereits |
| Root-View | Gleichbehandelt (auch ReloadableControl) | Keine Sonderfälle, auch `app.view.xml` ist hot-reloadbar |
| Channel-Definition-Änderungen | Nicht behandelt in V1 | Seltener Fall, manuelle Seiten-Aktualisierung zumutbar |

## Architektur

### Beteiligte Komponenten

```
ViewContext
  ├─ addViewReloadListener(ViewReloadListener)
  ├─ removeViewReloadListener(ViewReloadListener)
  └─ fireViewChanged(Set<String> changedPaths)
       └─ Delegation: Child-Kontexte delegieren zum Root

ViewReloadListener (Interface)
  └─ viewChanged(Set<String> changedPaths)

ReloadableControl (ReactCompositeControl)
  ├─ implements ViewReloadListener
  ├─ kennt: viewPath, viewContext, innerControl
  ├─ reload(): Subtree austauschen
  └─ cleanupTree(): Listener abmelden, inneren Control aufräumen

Erzeuger von ReloadableControl:
  ├─ ReferenceElement.createControl()
  ├─ Root-View-Laden (Einstiegspunkt)
  └─ OpenDialogAction (Dialog-Views)

OpenDesignerCommand
  └─ schleift App-ViewContext als "appContext"-Channel in Designer-Kontext

SaveDesignCommand
  └─ holt "appContext"-Channel, ruft fireViewChanged() auf
```

### Datenfluss: Apply mit Reload

```
User klickt "Apply" im Designer
  │
  ▼
SaveDesignCommand
  ├─ 1. Schreibt geänderte .view.xml Datei(en) auf Disk
  ├─ 2. ViewLoader-Cache invalidiert sich automatisch (Timestamp-Vergleich)
  ├─ 3. Holt App-ViewContext aus "appContext"-Channel des Designer-Kontexts
  └─ 4. viewContext.fireViewChanged({"views/sidebar.view.xml", ...})
           │
           ▼
     Alle registrierten ReloadableControls werden benachrichtigt
           │
           ├─ ReloadableControl (viewPath="views/sidebar.view.xml")
           │    → Pfad ist betroffen → reload()
           │
           └─ ReloadableControl (viewPath="views/dialog/edit.view.xml")
                → Pfad nicht betroffen → ignorieren
```

### ReloadableControl im Control-Baum

```
Parent-Control
  └─ ReloadableControl (stabil, ID bleibt gleich)
       └─ inner Control (wird bei reload() ersetzt)
```

Der Parent sieht immer denselben `ReloadableControl`. Nur dessen Inhalt wird ausgetauscht.
Client-seitig bleibt das DOM-Element stabil, React macht ein Diff auf das `children`-Array.

### ReloadableControl.reload()

```java
void reload() {
    // 1. Alten Subtree aufräumen
    _innerControl.cleanupTree();

    // 2. Neue UIElements laden (Cache bereits invalidiert)
    ViewElement newView = ViewLoader.getOrLoadView(_viewPath);

    // 3. Neuen Control mit bestehendem ViewContext erzeugen
    ReactControl newControl = newView.createControl(_viewContext);

    // 4. Inneren Control austauschen
    _innerControl = newControl;

    // 5. State aktualisieren → SSE-Patch an Client
    putState("children", serializeChildren());
}
```

**Warum der ViewContext wiederverwendet werden kann:**
- Channel-Werte (Dialog-Input, Live-Bindings zu Parent) bleiben erhalten
- Die neuen Controls lesen beim Aufbau die bestehenden Channel-Werte
- Dialoge bleiben offen mit ihren Daten

### Listener-Propagierung durch ViewContext-Hierarchie

Alle `ReloadableControl`s registrieren sich am **Root-ViewContext**, unabhängig von der
Verschachtelungstiefe. Child-Kontexte delegieren die Registrierung nach oben:

```java
// In DefaultViewContext:
void addViewReloadListener(ViewReloadListener listener) {
    if (_parent != null) {
        _parent.addViewReloadListener(listener);
    } else {
        _reloadListeners.add(listener);
    }
}

void fireViewChanged(Set<String> changedPaths) {
    if (_parent != null) {
        _parent.fireViewChanged(changedPaths);
    } else {
        for (ViewReloadListener l : _reloadListeners) {
            l.viewChanged(changedPaths);
        }
    }
}
```

### Durchschleifen des App-ViewContext

`OpenDesignerCommand` läuft im Kontext des Hauptfensters. Der App-ViewContext wird als
Channel-Wert in den Designer-Kontext eingespeist:

```java
// In OpenDesignerCommand.execute():
DefaultViewChannel appContextChannel = new DefaultViewChannel("appContext");
appContextChannel.set(context);  // ReactContext des Hauptfensters
designerContext.registerChannel("appContext", appContextChannel);
```

`SaveDesignCommand` greift darauf zu:

```java
// In SaveDesignCommand.execute():
// "context" ist hier der Designer-ReactContext, nicht der App-Kontext.
ViewContext appContext = (ViewContext) context.resolveChannel("appContext").get();
appContext.fireViewChanged(changedViewPaths);
```

### Lifecycle

| Event | Aktion |
|---|---|
| `ReloadableControl` erzeugt | `viewContext.addViewReloadListener(this)` |
| `viewChanged()` empfangen | Prüfe `changedPaths.contains(_viewPath)`, bei Treffer `reload()` |
| `reload()` | `_innerControl.cleanupTree()`, neuen Control erzeugen, State updaten |
| `cleanupTree()` auf ReloadableControl | `viewContext.removeViewReloadListener(this)`, dann `_innerControl.cleanupTree()` |

## Stellen, die ReloadableControl erzeugen

| Stelle | Aktuell | Neu |
|---|---|---|
| Root-View-Laden | Direkt `view.createControl(ctx)` | `new ReloadableControl(viewPath, ctx, view.createControl(ctx))` |
| `ReferenceElement.createControl()` | Direkt `referencedView.createControl(childCtx)` | `new ReloadableControl(viewPath, childCtx, referencedView.createControl(childCtx))` |
| `OpenDialogAction` | Direkt `view.createControl(dialogCtx)` | `new ReloadableControl(viewPath, dialogCtx, view.createControl(dialogCtx))` |

## Einschränkungen (V1)

- **Channel-Definitions-Änderungen**: Wenn die neue View-Datei andere Channel-Namen definiert,
  kann der bestehende ViewContext inkonsistent werden. Seltener Fall beim WYSIWYG-Editing
  (dort ändert man Properties, nicht Channel-Definitionen). Manuelles Seiten-Neuladen ist
  dann zumutbar.

- **Neue Kinder in ReferenceElement**: Wenn eine View nach dem Edit ein neues
  `ReferenceElement` enthält, bekommt dieses beim `createControl()` automatisch einen eigenen
  `ReloadableControl` — kein Sonderfall nötig.

## Nicht im Scope

- **Undo/Redo**: Weiterhin nicht in dieser Iteration
- **Multi-User-Konflikte**: Single-Writer-Annahme bleibt
- **Drag & Drop**: Weiterhin nur Context-Menu
