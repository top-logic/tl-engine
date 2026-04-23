# `@ChangeSubtree` — konfigurierbare Change-Subtree-Zugehörigkeit

Ticket: #29236 (Erweiterung)

## Ziel

Erweiterung des Change-Subtree-Konzepts über Kompositionsreferenzen hinaus. Konsumenten: `SubtreeFilter` (ChangeLog), `ChangeSetReverter` (Rollback, Undo/Redo). Die Konfiguration erfolgt pro Referenz — einmal definiert, einheitlich in allen drei Pfaden wirksam.

## Semantik

Ein Objekt `x` ist zur Revision `r` im Change-Subtree eines Wurzelobjekts `R` ⟺ es gibt einen gerichteten Pfad `R →* x` zur Revision `r` entlang qualifizierter Kanten.

Qualifizierung einer Kante erfolgt pro **Referenz-Paar** (Forward-Ref + zugehörige Back-Ref). Die Annotation `@ChangeSubtree` darf an **beliebiger Seite** des Paares sitzen:

- **Kompositions-Paar**: per Default qualifiziert. `@ChangeSubtree(false)` an einer der beiden Seiten deaktiviert das Paar.
- **Nicht-Kompositions-Paar**: per Default nicht qualifiziert. `@ChangeSubtree(true)` an einer der beiden Seiten aktiviert es.

Richtung: Die "Eltern-Kind"-Richtung eines qualifizierten Paares folgt dem Kompositionsmuster — Parent → Child. Für Kompositionspaare ist das durch `composite` strukturell festgelegt. Für Nicht-Kompositionspaare gilt die Konvention **Collection-wertige Seite = Parent-Seite** (so wie in Composition, deren Forward-Ref typischerweise multi-valued ist). Gleich-kardinale Fälle (1:1, n:m) sind in v1 ausgeschlossen; das Modell hat dafür in der Praxis keine natürliche Interpretation.

Konsequenz für die Navigation: Annotation auf der **Forward-Ref** (`Parent.children`) erfordert Reverse-Lookup; Annotation auf der **Back-Ref** (`Child.parent`) ist billiger (direkte Navigation `child.tValue(parent)`). Semantisch sind beide äquivalent — der Benutzer wählt, wo die Annotation sitzt, abhängig davon, welche Seite modelliert/konfigurierbar ist und welcher Navigationsweg gewünscht ist.

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

Modell-XML — Annotation kann an Forward-Ref **oder** Back-Ref sitzen:

```xml
<!-- Annotation auf Forward-Ref (Reverse-Lookup bei Navigation) -->
<reference name="relatedDocuments" type="Document" composite="false" multiple="true">
    <annotations>
        <change-subtree include="true"/>
    </annotations>
</reference>

<!-- Alternativ: Annotation auf Back-Ref (direkte Forward-Navigation, billiger) -->
<reference name="owner" type="Project" inverse-reference="relatedDocuments">
    <annotations>
        <change-subtree include="true"/>
    </annotations>
</reference>
```

Nur **eine** Seite des Paares muss annotiert sein — die Annotation qualifiziert das Paar, nicht die einzelne Referenz. Wird sie an beiden Seiten mit unterschiedlichem Wert gesetzt, gewinnt die spezifischere Paar-Seite (Validierungs-Logik: gleicher Wert verlangt oder Fehler).

Override-Semantik folgt dem Standard-Annotation-Merge (Spezialisierung kippt das Flag ggf. wieder).

## Algorithmus

Kernprädikat: `inSubtree(x, R, r)` — Rückwärts-Traversierung vom `Change`-Objekt zur Wurzel.

Kanten, die im Rückwärts-Walk folgen:
1. **Komposition (Default-Include)**: `x.tContainer()`, sofern das Paar nicht `@ChangeSubtree(false)` trägt.
2. **Opt-in-Nicht-Komposition, Annotation auf Back-Ref**: für jede solche Back-Ref `backRef` direkter Forward-Zugriff `x.tValue(backRef)` — liefert genau den Parent (oder Collection von Parents).
3. **Opt-in-Nicht-Komposition, Annotation auf Forward-Ref**: für jede solche Forward-Ref `fwdRef` die Menge der Quellen via Reverse-Lookup `{s : s.tValue(fwdRef) enthält x zur Revision r}`.

Die Engine bestimmt für jede qualifizierte Paar-Annotation einmalig beim Filter-Aufbau, welcher der drei Fälle greift (basierend auf `TLReference.getEnd()`-Metadaten — forward vs. backward). Für Paare, die nur auf einer Seite definiert sind, greift ausschließlich der verfügbare Modus.

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
        // (1) composition parent unless the pair is opted out
        TLObject container = cur.tContainer();
        if (container != null && !isCompositionPairOptedOut(cur)) {
            stack.push(container);
        }
        // (2a) opt-in, annotation on back-ref: direct forward navigation
        for (TLReference backRef : optInBackRefsFor(cur.tType())) {
            for (TLObject p : asCollection(cur.tValue(backRef))) {
                stack.push(p);
            }
        }
        // (2b) opt-in, annotation on forward-ref: reverse lookup
        for (TLReference fwdRef : optInForwardRefsFor(cur.tType())) {
            for (TLObject s : referersVia(cur, fwdRef, rev)) {
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
      // forward composition edges (parent→child)
      for each composition T.ref → Target, pair not @ChangeSubtree(false):
        reachable += Target (incl. Subtypen)
      // forward non-composition edges opt-in on forward-ref
      for each non-composition T.ref → Target with ref @ChangeSubtree(true):
        reachable += Target (incl. Subtypen)
      // pair edges opt-in on back-ref: a back-ref Child.parent→Parent
      // with @ChangeSubtree(true) on the back-ref, where Parent ∈ reachable
      // ⇒ follow the pair in forward direction: add Child.
      for each back-ref C.backRef → P with @ChangeSubtree(true) on backRef,
          where P ∈ reachable:
        reachable += C (incl. Subtypen)
  until fixed point
  ```

  Der Modell-Scan wird dabei auf die in `reachable` neu aufgenommenen Typen beschränkt; das ganze Modell wird nie traversiert.

- **Typ-Dispatch für Opt-in-Referenzen** — getrennt nach Annotation-Site:
  - **Back-Ref-annotiert** (`optInBackRefsFor(t)`): Back-Ref `Child.parent → Parent` ist Kandidat für Objekt `x`, wenn `x.tType()` Subtyp von `Child` (`backRef.getOwner()`) ist. Der Lookup `cur.tValue(backRef)` ist dann direkt.
  - **Forward-Ref-annotiert** (`optInForwardRefsFor(t)`): Forward-Ref `Parent.children → Child` ist Kandidat für Objekt `x`, wenn `x.tType()` Subtyp von `Child` (`fwdRef.getType()`) ist. Reverse-Lookup gegen diese Referenz.
  - Beide Maps (`Map<TLType, List<TLReference>>`) werden beim Filter-Aufbau aus der Reachable-Closure (Nebenprodukt) erzeugt und lazy per-Type gefüllt.
  - Bei typischer Verteilung schrumpft jede Kandidatenmenge pro `cur` auf 0–1.

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
   - Opt-in forward-ref: Forward-Nicht-Kompositionsreferenz `Parent.peers → Child` mit `@ChangeSubtree(true)`. Änderungen an Kindern erscheinen im Filter von `Parent` via Reverse-Lookup.
   - Opt-in back-ref: Back-Ref `Child.owner → Parent` mit `@ChangeSubtree(true)` (Forward-Ref unannotiert). Äquivalent zum Forward-Fall, Navigation via direkter `tValue`-Aufruf.
   - Opt-in multi-valued: Parent-Seite ist Collection-wertig, Navigation über beide Sites getestet.
   - Opt-in + Komposition kombiniert: Ziel liegt teils in Komposition, teils via Opt-in; beide Pfade zählen existenziell.
   - Zyklusschutz: A → B (opt-in), B → A (opt-in) führt nicht zur Endlosschleife.
   - Historischer Wechsel: Ziel war zur Revision r₁ in Opt-in-Referenz, zur Revision r₂ entfernt — nur Änderungen im Gültigkeitszeitraum erscheinen.
2. Ergänzungen an `TestChangeSetReverter` — `revertSubtreeTo(root, target)` rollt Opt-in-Kinder ebenso zurück wie Kompositions-Kinder.
3. Bestehende `TestSubtreeChangeLog`-Cases bleiben grün (Regressionsschutz für das Default-Verhalten ohne Annotation).

## Nicht in diesem Scope

- Persistenter Index `(revision, ancestorId)`. Memoisierung + KB-Reverse-Index reichen.
- UI für die Annotation-Bearbeitung (Standard-Annotation-Editor reicht).
- Performance-Benchmarks als Teil der Abnahme. Nur Korrektheit per Tests; Performance-Drift wird durch bestehende Change-Log-Benchmarks (falls vorhanden) überwacht.

## Offene Punkte zur Verifikation während Implementierung

- Konkrete KB-API für "Referers zu `(x, ref, rev)` historisch". Erwartete Kandidaten: `KnowledgeBase.getReferers(...)`, `WrapperUtils.getReferer(...)` bzw. historische Varianten. Falls nur live-Abfrage performant ist: Fallback über Event-Stream-Analyse oder Fallback-Scan akzeptabel, solange das Testfenster (`windowSize = 50`) beherrschbar bleibt.
- Feststellung der "einbindenden Referenz" eines Change-Objekts für Kompositions-Opt-out: entweder aus dem `Change`-Event (billig — die KB-Seite kennt den Referenztyp), oder aus der Typ-Struktur via `tContainer()` und Suche nach Komposition. Präferiert: direkt aus dem Event beim `ChangeSetAnalyzer`.
