# `@ChangeSubtree` — konfigurierbare Change-Subtree-Zugehörigkeit

Ticket: #29236 (Erweiterung)

## Ziel

Erweiterung des Change-Subtree-Konzepts über Kompositionsreferenzen hinaus. Konsumenten: `SubtreeFilter` (ChangeLog), `ChangeSetReverter` (Rollback, Undo/Redo). Die Konfiguration erfolgt pro Referenz — einmal definiert, einheitlich in allen drei Pfaden wirksam.

## Semantik

Ein Objekt `x` ist zur Revision `r` im Change-Subtree eines Wurzelobjekts `R` ⟺ es gibt einen gerichteten Pfad `R →* x` zur Revision `r`, wobei jede Kante eine ist von:

- **Kompositionsreferenz**, die **nicht** `@ChangeSubtree(false)` trägt (Default-Include, per Annotation opt-out).
- **Nicht-Kompositionsreferenz**, die `@ChangeSubtree(true)` trägt (Default-Exclude, per Annotation opt-in).

Richtung: `@ChangeSubtree` auf `Source.ref → Target` bedeutet "Target gehört zum Change-Subtree von Source" — analog zur Komposition.

Ein `ChangeSet` trifft `R` ⟺ mindestens ein enthaltener `Change` operiert auf einem Objekt, das zur Revision des `ChangeSet` im Change-Subtree von `R` liegt. Referenz-Änderungen zählen als `Change` auf dem Quellobjekt — damit sind Moves in/aus dem Subtree automatisch die "letzte Änderung im R-Kontext" am Quellobjekt, konsistent mit dem Kompositions-Move-Verhalten.

## Annotation

`TLReferenceAnnotation` in `com.top_logic.model.annotate` (Package existiert, Siedlungsmuster siehe `TLCollectionSeparator`):

```java
@InApp
@TagName("change-subtree")
@TargetType(TLTypeKind.REFERENCE)
public interface TLChangeSubtree extends TLReferenceAnnotation {
    /**
     * Whether the reference contributes to the change-subtree of its source object.
     *
     * <p>For composition references this defaults to {@code true} (opt-out with
     * {@code false}); for non-composition references it defaults to {@code false}
     * (opt-in with {@code true}).</p>
     */
    @Name("include")
    boolean getInclude();
}
```

Modell-XML:

```xml
<reference name="relatedDocuments" type="Document" composite="false" multiple="true">
    <annotations>
        <change-subtree include="true"/>
    </annotations>
</reference>
```

Override-Semantik folgt dem Standard-Annotation-Merge (Spezialisierung kippt das Flag ggf. wieder).

## Algorithmus

Kernprädikat: `inSubtree(x, R, r)` — Rückwärts-Traversierung vom `Change`-Objekt zur Wurzel.

Kanten, die im Rückwärts-Walk folgen:
1. **Komposition (Default-Include)**: `x.tContainer()`, sofern die eingehende Komposition nicht `@ChangeSubtree(false)` trägt.
2. **Opt-in-Nicht-Komposition**: für jede Referenz `ref` mit `@ChangeSubtree(true)` die Menge der Quellen `{s : s.ref.contains(x) zur Revision r}`.

Damit wird aus dem reinen Baum-Walk ein BFS/DFS auf einem DAG. Details:

- **Zyklusschutz**: `Set<ObjectReference>` der bereits besuchten `(unversionedId)` innerhalb einer Einzelabfrage.
- **Existenzielle Zugehörigkeit**: Sobald ein Pfad `R` erreicht, Abbruch mit `true`. Nur wenn kein einziger Pfad `R` erreicht, `false`.
- **Memoisierung** bleibt: `(revision, unversionedId) → boolean`. Pfad-Memoisierung (alle während eines Walks besuchten IDs bekommen das Endergebnis) gilt ungeschmälert — auch für DAG-Pfade, da `inSubtree(y)` nicht von der Herkunft abhängt.
- **Historische Referenzwerte**: Der Reverse-Lookup operiert auf der Ziel-Revision `r`. Wir setzen voraus, dass KB-seitig eine historisch gestellte Abfrage "wer zeigt via `ref` auf `x` zur Revision `r`?" performant verfügbar ist.
- **Deletions**: Für gelöschte Objekte verwendet der `Change` bereits den Wrapper zur Vorgängerrevision. Der Reverse-Lookup muss auf dieser Revision funktionieren — Verifikation während der Implementierung.

Pseudocode:

```java
boolean inSubtree(TLObject x, long rev) {
    if (cached(rev, x)) return cachedValue;
    Deque<TLObject> stack = new ArrayDeque<>();
    Set<ObjectReference> visited = new HashSet<>();
    stack.push(x);
    List<ObjectReference> path = new ArrayList<>();
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
            else { /* this branch is dead — continue other branches */ continue; }
        }
        path.add(id);
        // (1) composition parent unless opted out
        TLObject container = cur.tContainer();
        if (container != null && !isCompositionOptedOut(cur)) {
            stack.push(container);
        }
        // (2) opt-in reverse lookup — only references whose target type is
        // assignable from cur.tType() are candidates.
        for (TLReference ref : optInReferencesFor(cur.tType())) {
            for (TLObject s : referersVia(cur, ref, rev)) {
                stack.push(s);
            }
        }
    }
    memoize(path, false);
    return false;
}
```

Feinheiten:

- **Reachable-Typ-Closure** (aus `R.tType()` als Seed): Ein Objekt kann nur dann im Change-Subtree von `R` liegen, wenn sein Typ forward-reachable von `R.tType()` über Subtree-Kanten ist. Wir berechnen beim Filter-Aufbau einmalig per Fixpunkt:

  ```
  reachable = { R.tType() }                    // plus alle Subtypen
  repeat:
    for T in reachable:
      for each composition T.ref → Target without @ChangeSubtree(false):
        reachable += Target (incl. Subtypen)
      for each opt-in T.ref → Target with @ChangeSubtree(true):
        reachable += Target (incl. Subtypen)
  until fixed point
  ```

  Der Modell-Scan wird dabei auf die in `reachable` neu aufgenommenen Typen beschränkt; das ganze Modell wird nie traversiert.

- **Typ-Dispatch für Opt-in-Referenzen**: Eine Opt-in-Referenz `ref : Source → Target` ist nur dann Kandidat für ein Objekt `x`, wenn `x.tType()` Subtyp von (oder gleich) `Target` ist. Die Filterklasse hält dafür einen `Map<TLType, List<TLReference>>`-Cache:
  - `allOptInReferences` fällt als Nebenprodukt des Closure-Schritts ab (nur Referenzen aus `reachable`).
  - Per-Type-Lookup wird lazy gefüllt: `optInReferencesFor(t)` bildet beim ersten Zugriff auf `t` die Liste `{ref ∈ allOptInReferences : t.isAssignableTo(ref.getType())}` und cached sie.
  - Bei typischer Verteilung schrumpft die Reverse-Lookup-Kandidatenmenge pro `cur` auf meist 0–1.

- **Frühe Ablehnung**: Ist `cur.tType() ∉ reachable`, kann `cur` nicht zu R gehören — Walk auf diesem Zweig abbrechen (Ergebnis `false` für diesen Pfad, per Memoisierung pro `unversionedId` festgehalten).
- `isCompositionOptedOut(cur)` bestimmt sich aus der Referenz, die `cur` in seinen Container einbindet (via `cur.tType()` / Container-Navigation oder direkt über das KB-Event bei der Change-Erstellung — letzteres ist billiger).
- Die heutige "Pfad-Präfixe zwischen Geschwistern geteilt"-Optimierung bleibt für den Kompositions-Teil intakt.

## Betroffene Klassen

- `com.top_logic.model.annotate.TLChangeSubtree` (**neu**)
- `com.top_logic.element.changelog.SubtreeFilter` — `inSubtree` erweitern; `optInReferences` berechnen; Kompositions-Opt-out berücksichtigen.
- `com.top_logic.element.changelog.ChangeSetReverter` — nutzt `SubtreeFilter` bereits, kein strukturschneidender Eingriff. Die Stream-Filterung in `revertSubtreeTo` verwendet dasselbe Prädikat. Sicherstellen, dass `findUndoCandidate` / `findRedoCandidate` konsistent bleiben (geht über `readLog` → `SubtreeFilter`, erledigt).
- `ChangeLogBuilder` — kein Eingriff (Filter ist injiziert).
- Demo-Modell (`com.top_logic.demo`) — eine Beispiel-Referenz mit `<change-subtree include="true"/>` bzw. `include="false"` für Dokumentation und Tests.

## Tests

Modul `com.top_logic.element`, Paket `test.com.top_logic.element.changelog`:

1. `TestChangeSubtreeAnnotation` — neue Klasse.
   - Opt-out: Kompositionsreferenz `noise` mit `@ChangeSubtree(false)`. Änderungen an Kindern über `noise` dürfen nicht im Filter erscheinen.
   - Opt-in single: Nicht-Kompositionsreferenz `mirror` mit `@ChangeSubtree(true)`. Änderungen am Ziel erscheinen im Filter der Quelle.
   - Opt-in multi-valued: Quelle referenziert mehrere Ziele via `@ChangeSubtree(true)`-Collection-Ref.
   - Opt-in + Komposition zusammen: Ziel liegt teils in Komposition, teils via Opt-in; beide Pfade zählen existenziell.
   - Zyklusschutz: A → B (opt-in), B → A (opt-in) führt nicht zur Endlosschleife.
   - Historischer Wechsel: Ziel war zur Revision r₁ in Opt-in-Referenz, zur Revision r₂ entfernt — nur die Änderungen im Gültigkeitszeitraum erscheinen.
2. Ergänzungen an `TestChangeSetReverter` — `revertSubtreeTo(root, target)` rollt Opt-in-Kinder ebenso zurück wie Kompositions-Kinder.
3. Bestehende `TestSubtreeChangeLog`-Cases bleiben grün (Regressionsschutz für das Default-Verhalten ohne Annotation).

## Nicht in diesem Scope

- Persistenter Index `(revision, ancestorId)`. Memoisierung + KB-Reverse-Index reichen.
- UI für die Annotation-Bearbeitung (Standard-Annotation-Editor reicht).
- Performance-Benchmarks als Teil der Abnahme. Nur Korrektheit per Tests; Performance-Drift wird durch bestehende Change-Log-Benchmarks (falls vorhanden) überwacht.

## Offene Punkte zur Verifikation während Implementierung

- Konkrete KB-API für "Referers zu `(x, ref, rev)` historisch". Erwartete Kandidaten: `KnowledgeBase.getReferers(...)`, `WrapperUtils.getReferer(...)` bzw. historische Varianten. Falls nur live-Abfrage performant ist: Fallback über Event-Stream-Analyse oder Fallback-Scan akzeptabel, solange das Testfenster (`windowSize = 50`) beherrschbar bleibt.
- Feststellung der "einbindenden Referenz" eines Change-Objekts für Kompositions-Opt-out: entweder aus dem `Change`-Event (billig — die KB-Seite kennt den Referenztyp), oder aus der Typ-Struktur via `tContainer()` und Suche nach Komposition. Präferiert: direkt aus dem Event beim `ChangeSetAnalyzer`.
