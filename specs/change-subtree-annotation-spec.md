# `@ChangeSubtree` — konfigurierbare Change-Subtree-Zugehörigkeit

Ticket: #29236 (Erweiterung)

## Ziel

Erweiterung des Change-Subtree-Konzepts über Kompositionsreferenzen hinaus. Konsumenten: `SubtreeFilter` (ChangeLog), `ChangeSetReverter` (Rollback, Undo/Redo). Die Konfiguration erfolgt pro Referenz-Ende — einmal definiert, einheitlich in allen drei Pfaden wirksam.

## Semantik

Die Annotation `@ChangeSubtree(true|false)` sitzt an einem **Referenz-Ende** `E`. Der Owner von `E` (die Klasse, auf der `E` deklariert ist) ist die **Root-Seite** der Qualifizierung; der Target-Typ von `E` ist die **Child-Seite**.

Gelesen: "Änderungen an Instanzen des Child-Typs, die über diese Referenz mit einer Root-Instanz verbunden sind, gehören in den Change-Subtree jener Root-Instanz."

- **Komposition**: Das Composite-Ende trägt eine **implizite** `@ChangeSubtree(true)`. `@ChangeSubtree(false)` (an beliebiger Seite des Paares) deaktiviert die Qualifizierung.
- **Nicht-Komposition**: Keine implizite Qualifizierung. `@ChangeSubtree(true)` an Ende `E` aktiviert die Qualifizierung mit `owner(E)` als Root-Typ.

Beide Seiten eines Paares dürfen unabhängig annotiert werden — jede Annotation ist eine separate Qualifizierung mit eigener Root-Seite. Annotation an beiden Seiten mit `true` heißt "die Beziehung qualifiziert in beide Richtungen" (i. d. R. eine Randfall-Konfiguration, aber zulässig).

### Formal

Ein Objekt `x` ist zur Revision `r` im Change-Subtree eines Wurzelobjekts `R` ⟺ es gibt einen gerichteten Pfad `R →* x` zur Revision `r`, wobei jede Kante durch eine qualifizierte Annotation gedeckt ist: zu einer Kante `y → z` existiert ein annotiertes Ende `E` mit `owner(E) = typeof(y)`, `target(E) = typeof(z)`, und zur Revision `r` ist `z ∈ y.tValue(otherEnd(E))` (bzw. äquivalent `y ∈ z.referers(E)`).

Ein `ChangeSet` trifft `R` ⟺ mindestens ein enthaltener `Change` operiert auf einem Objekt, das zur Revision des `ChangeSet` im Change-Subtree von `R` liegt — inklusive Dual-State-Check für Ref-Änderungen (s. u.).

## Annotation

Java-Interface:

```java
@InApp
@TagName("change-subtree")
@TargetType(TLTypeKind.REFERENCE)
public interface TLChangeSubtree extends TLAttributeAnnotation {
    /**
     * Whether the end this annotation sits on qualifies as root-side of a
     * change-subtree relationship: changes to instances reachable through
     * this reference end belong to the subtree of the instance owning the end.
     *
     * <p>For composition references, the composite end defaults to
     * {@code true} (opt-out with {@code false}). For non-composition
     * references, the default is {@code false} (opt-in with {@code true}).</p>
     */
    @Name("include")
    boolean getInclude();
}
```

Modell-XML — Annotation kann an Forward-Ref **oder** Back-Ref sitzen; jede Seite definiert eine eigene Root-Richtung:

```xml
<!-- Forward-Ref annotiert: Project ist Root, Documents sind Children -->
<class name="Project">
  <reference name="documents" type="Document" composite="false" multiple="true">
    <annotations>
      <change-subtree include="true"/>
    </annotations>
  </reference>
</class>

<!-- Back-Ref annotiert: Document ist Root, Project ist Child -->
<class name="Document">
  <reference name="project" type="Project" inverse-reference="documents">
    <annotations>
      <change-subtree include="true"/>
    </annotations>
  </reference>
</class>
```

Override-Semantik folgt dem Standard-Annotation-Merge (Spezialisierung überschreibt Generalisierung).

## Algorithmus

Navigation ist einheitlich `x.referers(annotatedRef, rev)`, unabhängig davon, ob das annotierte Ende Forward- oder Back-Ref des zugrunde liegenden Paares ist. Die KB löst `referers` intern effizient auf — bei einer Back-Ref-Annotation reduziert sich das auf einen direkten `get`-Aufruf der Gegenseite. Keine Fallback-Logik, keine Fallunterscheidung im Filter.

### Kernprädikat `inSubtree(x, R, r)` — Rückwärts-Traversierung

```java
boolean inSubtree(TLObject x, long rev) {
    if (cached(rev, x)) return cachedValue;
    Deque<TLObject> stack = new ArrayDeque<>();
    Set<ObjectReference> visited = new HashSet<>();
    List<ObjectReference> path = new ArrayList<>();
    stack.push(x);
    while (!stack.isEmpty()) {
        TLObject cur = stack.pop();
        ObjectReference id = unversioned(cur);
        if (!visited.add(id)) continue;
        if (id.equals(rootIdentity)) {
            memoize(path, true); memoize(id, true);
            return true;
        }
        Boolean hit = cache.get(rev, id);
        if (hit != null) {
            if (hit) { memoize(path, true); return true; }
            else continue;
        }
        path.add(id);
        // For every annotated end whose target type matches cur.tType(),
        // follow the edge upward to the root-side candidates.
        for (TLReference annotatedRef : annotatedEndsFor(cur.tType())) {
            for (TLObject root : cur.referers(annotatedRef, rev)) {
                stack.push(root);
            }
        }
    }
    memoize(path, false);
    return false;
}
```

Die Komposition ist kein Sonderfall — `tContainer()` entfällt als separater Pfad. Eine Komposition trägt ihre implizite `@ChangeSubtree(true)`-Qualifizierung auf dem Composite-Ende und wird damit durch dieselbe `annotatedEndsFor`/`referers`-Mechanik abgedeckt (Reverse-Lookup auf dem Composite-Ende ≡ `tContainer()` für direkte Kinder).

**Implementierung der impliziten Komposition-Qualifizierung (Variante A)**: Der Scanner, der `annotatedEndsFor` und die Reachable-Closure aufbaut, behandelt jedes Composite-Ende einer Komposition als wäre es `@ChangeSubtree(true)` annotiert — es sei denn, das Paar trägt explizit `@ChangeSubtree(false)` auf einer beliebigen Seite. Keine virtuelle Annotation-Materialisierung auf Modell-Ebene; die Logik lebt allein im Scanner.

### Dual-State-Check bei Ref-Änderungen

Für Boundary-Events (Moves in/aus einem Subtree) reicht die Prüfung zur Revision `r` allein nicht: bei einer Ref-Modifikation ändert sich die Root-Zugehörigkeit eines Objekts selbst zur Revision `r`. Der Event soll sichtbar sein

- im Subtree des *bisherigen* Roots (Move-Out-Boundary) und
- im Subtree des *neuen* Roots (Move-In-Boundary).

Der Check hängt vom Event-Typ ab (nicht von einer generischen „r-1 ∨ r"-Regel):

| Event | Membership-Check | Begründung |
|---|---|---|
| `ObjectCreation` | bei `r` | Objekt existiert bei `r-1` nicht → `r-1`-Check trivial `false`, überflüssig. |
| `ItemDeletion` | bei `r-1` (Wrapper zur Vorgängerrevision) | Objekt existiert bei `r` nicht. Liefert Move-Out-Boundary zum letzten bekannten Zustand. |
| `ItemUpdate` auf **TLReference-Attribut** | `r-1` ∨ `r` | Membership kann sich durch diesen Change ändern. Move-Out + Move-In sichtbar. |
| `ItemUpdate` auf Non-Ref-Attribut | bei `r` | Membership ist invariant gegenüber dem Change. |

**Optimierung**: Dual-Check nur, wenn das geänderte Ref-Attribut (oder sein Gegenstück) in der globalen Menge annotierter Enden liegt. Sonst Single-Check bei `r`. Das ist eine Verfeinerung innerhalb der Regel, keine semantische Änderung.

### Opt-out wirkt auch auf Ref-Modifikationen

Ein `@ChangeSubtree(include=false)` auf einem Referenz-Ende deaktiviert die Zugehörigkeit der Kinder zum Change-Subtree *und* blendet Modifikationen am Ref-Attribut selbst aus:

**Regel**: Ein `Update`-Change, dessen Modifikationen **ausschließlich** auf Referenz-Attributen stattfinden, die explizit mit `@ChangeSubtree(include=false)` auf einer Paar-Seite deaktiviert sind, wird aus dem Log gefiltert — unabhängig davon, ob das modifizierte Objekt im Subtree liegt.

**Begründung**: Ohne diese Regel würde der reine Akt „Note an `Project.notes` anhängen" ein `Update(Project, notes=…)` erzeugen, das trivial im Subtree von `Project` liegt und damit akzeptiert würde — entgegen der Opt-out-Absicht. Ein Update mit gemischten Modifikationen (eine Opt-out-Ref *und* ein reguläres Attribut) bleibt sichtbar, weil das reguläre Attribut den Event trägt.

**Delete** ist von der Regel nicht direkt betroffen: Objekt-Deletions werden durch die normale Subtree-Zugehörigkeit gefiltert (das gelöschte Objekt ist bei Opt-out mangels qualifizierter Kanten nicht erreichbar). Ref-Modifikationen, die Elemente aus der Opt-out-Ref entfernen (ohne Objekt-Delete), laufen als `Update` durch die Regel oben.

### Reachable-Typ-Closure (aus `R.tType()`)

Ein Objekt kann nur dann im Change-Subtree von `R` liegen, wenn sein Typ forward-reachable von `R.tType()` über qualifizierte Kanten ist.

```
reachable = { R.tType() }                      // plus alle Subtypen
repeat:
  for T in reachable:
    for each annotated end E with owner(E) = T:
      reachable += target(E) (incl. Subtypen)
until fixed point
```

Das schließt die implizit-annotierten Komposition-Composite-Enden mit ein. Der Modell-Scan folgt nur neu aufgenommenen Typen; das ganze Modell wird nie traversiert.

### Typ-Dispatch

`annotatedEndsFor(t)`: lazy gefüllter `Map<TLType, List<TLReference>>`.
- Kandidat: annotiertes Ende `E`, wenn `t` Subtyp von `target(E)` ist.
- Aufbau aus der Reachable-Closure (Nebenprodukt).
- Bei typischer Verteilung: 0–1 Kandidaten pro `cur`.

### Feinheiten

- **Frühe Ablehnung**: `cur.tType() ∉ reachable` ⇒ Zweig abbrechen (Ergebnis `false`, memoized).
- **Zyklusschutz**: `visited`-Set der bereits besuchten `unversionedId` pro Einzelabfrage.
- **Memoisierung**: `(revision, unversionedId) → boolean`. Cache-Scope ist **pro `SubtreeFilter`-Instanz** (Lebensdauer eines ChangeLog-Requests). Pfad-Memoisierung bleibt (alle während eines Walks besuchten IDs erhalten das Endergebnis), unabhängig von DAG-Pfaden.
- **Historische Referenzwerte**: `TLObject.tReferers(ref)` auf dem historischen Wrapper (Change trägt den Wrapper zur passenden Revision). KB-seitige Optimierung für Back-Ref-Fall wird vorausgesetzt (reduziert sich auf einen direkten Ref-Lookup).
- **Deletions**: Für gelöschte Objekte nutzt `Change` den Wrapper zur Vorgängerrevision — die Reverse-Lookup-Semantik überträgt sich.
- **Dual-State-Lookup**: Nur bei Ref-Änderungs-Events aktiv; innerhalb der Pfad-Memoisierung wird pro Revision eigener Cache-Eintrag geführt.

## Betroffene Klassen

- `com.top_logic.model.annotate.TLChangeSubtree` (**neu**).
- `com.top_logic.element.changelog.SubtreeFilter` — `inSubtree` auf uniforme `referers`-Mechanik umstellen; Reachable-Closure und Typ-Dispatch einziehen; Dual-State-Check auf Ref-Änderungs-Events.
- `com.top_logic.element.changelog.ChangeSetReverter` — nutzt `SubtreeFilter` bereits, kein struktureller Eingriff. `revertSubtreeTo`, `findUndoCandidate`, `findRedoCandidate` sind über `readLog` → `SubtreeFilter` automatisch konsistent.
- `ChangeLogBuilder` — kein Eingriff (Filter ist injiziert).
- Demo-Modell (`com.top_logic.demo`) — eigenes neues Modell „Project Management" (Project/Milestone/Ticket/Note/Person), um alle Annotations-Varianten (Opt-in Forward-Ref, Opt-in Back-Ref, Opt-out Komposition) mit fachlicher Lesbarkeit darzustellen.

## Tests

Modul `com.top_logic.element`, Paket `test.com.top_logic.element.changelog`:

1. `TestChangeSubtreeAnnotation` — neue Klasse.
   - Opt-out Komposition: Kompositionsreferenz `noise` mit `@ChangeSubtree(false)`. Änderungen an Kindern darunter erscheinen nicht im Filter von R=Parent.
   - Opt-in Forward-Ref: `Parent.peers → Child` mit `@ChangeSubtree(true)`. R=Parent sieht Änderungen an referenzierten Children.
   - Opt-in Back-Ref: `Child.owner → Parent` mit `@ChangeSubtree(true)`. R=Child sieht Änderungen am referenzierten Parent. *Zwei unterschiedliche Root-Richtungen, nicht zwei Schreibweisen derselben.*
   - Dual-State / Move-Out: Back-Ref-Annotation. `a1.bs` entfernt `bx`. Der Event erscheint im Log von R=bx (Move-Out-Boundary) und im Log von R=b_new, falls parallel hinzugefügt.
   - Multi-valued Navigation: Collection-wertige Seite wird korrekt über `referers` verteilt.
   - Komposition + Opt-in kombiniert: beide Qualifizierungen tragen; existenzielle Zugehörigkeit.
   - Zyklusschutz: A-B wechselseitig annotiert führt nicht zur Endlosschleife.
   - Historischer Wechsel: Ref-Wechsel zwischen r₁ und r₂ — Änderungen erscheinen nur im Log der zur Revision der jeweiligen Änderung zuständigen Root.
2. Ergänzungen an `TestChangeSetReverter` — `revertSubtreeTo(root, target)` rollt qualifizierte Kinder (via Annotation auf beliebiger Paar-Seite) ebenso zurück wie Kompositions-Kinder.
3. Bestehende `TestSubtreeChangeLog`-Cases bleiben grün (Regressionsschutz für das Default-Verhalten ohne Annotation).

## Nicht in diesem Scope

- Persistenter Index `(revision, ancestorId)`. Memoisierung + KB-Reverse-Index reichen.
- UI für die Annotation-Bearbeitung (Standard-Annotation-Editor reicht).
- Performance-Benchmarks als Teil der Abnahme. Nur Korrektheit per Tests.

## Offene Punkte zur Verifikation während Implementierung

- Konkrete KB-API für `x.referers(ref, rev)` historisch. Kandidaten: `KnowledgeBase.getReferers(...)`, `WrapperUtils.getReferer(...)`, historische Varianten. Optimierung durch die KB für Back-Ref-Case (reduziert auf direkten `get`) ist vorauszusetzen; Verifikation in der Implementierung.
- Erkennung "Change ist Ref-Modifikation" für Dual-State-Trigger — kommt bei `ChangeSetAnalyzer` aus der Event-Metadata (ItemUpdate mit TLReference als `TLStructuredTypePart`). Integration erfolgt dort, wo der Filter die Change sieht.
