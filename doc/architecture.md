# TopLogic KI-Integration – Architekturübersicht


## Was ist das?

Die TopLogic KI-Integration macht eine TopLogic-Anwendung für
KI-Assistenten zugänglich. Ein KI-Assistent kann die Anwendung
verstehen, mit ihr arbeiten und sie konfigurieren – ähnlich wie ein
erfahrener Berater, der die Anwendung kennt.

Dabei gibt es drei Einsatzformen:

- **TL Copilot**: Ein KI-Assistent direkt in der Anwendung, über
  den Anwender Fragen stellen und Aufgaben ausführen lassen können.

- **Interne Agenten**: Spezialisierte KI-Agenten innerhalb der
  Anwendung, die eigenständig wiederkehrende Aufgaben übernehmen.
  Interne Agenten können dabei auch externe Werkzeuge (Anwendungen) verwenden.
  Die Spezialisierung solcher Agenten manifestiert sind in ihren "Skills", 
  also ihren konkreten Fähigkeiten.
  ### Development Agenten
  Agenten zur Unterstützung der Anwendungsentwicklung.
  Trigger: User-Prompt (Copilot)
  Fokus: Applikationsmodell, Konfiguration, Layout
  Typische Skills: Modellteile suchen, analysieren, modifzieren, Layouts konfigurieren...
  ### Fachliche Agenten
  Agenten zur Interaktion mit Anwendungsdaten
  Trigger: User-Prompt (Copilot), Zeitgesteuert, Ereignissgesteuert, durch externe Agenten
  Fokus: Applikationsdaten, lokal und Systemübergreifend
  Typische Skills: Daten auswerten, überwachen, Datensätze anlegen und ändern, 
  Aktionen in Fremdsystemen anstoßen, Mails versenden ...

- **Externe Agenten**: Eigenständige KI-Werkzeuge (z.B. Claude
  Desktop, Cursor), die sich von außen mit der Anwendung verbinden und
  mit ihr arbeiten.

Alle drei Formen nutzen dieselben Werkzeuge. Was der eingebaute
Assistent kann, kann auch ein Agent – nur eben
automatisch und ohne dass jemand eine Frage stellt.


## Warum?

TopLogic-Anwendungen sind hochgradig konfigurierbar: Datenmodelle,
Oberflächen, Geschäftslogik – vieles lässt sich ohne Programmierung
anpassen. Diese Konfigurierbarkeit ist eine Stärke, erfordert aber
Fachwissen über die Plattform.

Gleichzeitig gibt es in jeder Anwendung eine Lücke zwischen dem, was
die Oberfläche bietet, und dem, was Anwender im Alltag brauchen.
Für häufige Arbeitsschritte gibt es spezialisierte Funktionen –
aber für die vielen einmaligen oder seltenen Aufgaben nicht. "Setze
bei allen Aufträgen aus dem letzten Quartal den Status auf
'archiviert'", "Übertrage die Kontaktdaten aus der alten Struktur
in die neue", "Zeig mir alle Projekte, bei denen Budget und
Aufwand um mehr als 20% auseinanderliegen" – solche Aufgaben fallen
regelmäßig an, aber keine davon rechtfertigt eine eigene
Anwendungsfunktion.

Bisher bleiben zwei Möglichkeiten: Entweder man baut für jeden
solchen Fall eine Spezialfunktion (aufwändig, und die meisten
Wünsche werden nie umgesetzt), oder der Anwender erledigt es von
Hand (fehleranfällig und mühsam bei großen Datenmengen).

Ein KI-Assistent ist das Schweizer Taschenmesser für genau diese
Situationen. Er braucht keine vorgefertigte Funktion – er versteht
die Aufgabe in natürlicher Sprache und führt sie mit den
vorhandenen Werkzeugen aus. Das betrifft nicht nur Massenoperationen,
sondern jede Aufgabe, für die es keinen eigenen Button gibt: Daten
abgleichen, Zusammenfassungen erstellen, Plausibilitäten prüfen,
Änderungen nach Regeln durchführen.

Damit verschiebt sich die Frage von "Haben wir dafür eine Funktion?"
zu "Kann ich das dem Assistenten sagen?" – und die Antwort ist fast
immer ja.

Im Einzelnen profitieren verschiedene Zielgruppen:

- **Endanwender**: Fragen zur Anwendung stellen, Daten suchen und
  auswerten, Routineaufgaben automatisieren – ohne auf eine
  Spezialfunktion warten zu müssen.

- **Konfiguratoren**: Datenmodelle erkunden und erweitern,
  Oberflächen erstellen und anpassen, Konfigurationsänderungen
  durchführen.

- **Entwickler**: Die Anwendungsstruktur verstehen, Änderungen
  planen und umsetzen, Skripte schreiben und testen.


## Der eingebaute Assistent

### Wie funktioniert er?

Der eingebaute Assistent ist ein Chat-Fenster innerhalb der
TopLogic-Anwendung. Der Anwender stellt eine Frage oder gibt einen
Auftrag in natürlicher Sprache. Der Assistent versteht die Anfrage,
führt die nötige Aktion aus und antwortet.

Hinter den Kulissen verbindet sich der Assistent mit einem
KI-Sprachmodell (z.B. von OpenAI oder Anthropic). Dieses Sprachmodell
entscheidet selbständig, welche Werkzeuge es braucht, um die Anfrage
zu beantworten. Es kann mehrere Werkzeuge nacheinander einsetzen, um
komplexe Aufgaben zu lösen.

### Vom Chatbot zum Copiloten

Ein reiner Chatbot beantwortet Fragen. Ein Copilot arbeitet mit –
er sieht, was der Anwender sieht, und handelt im selben Kontext.

Der entscheidende Schritt ist der Zugriff auf den Benutzerkontext:
Der Assistent weiß, welche Ansicht gerade geöffnet ist, welches
Objekt selektiert ist, welche Filter aktiv sind und welche Felder
im Formular ausgefüllt oder fehlerhaft sind. Damit kann er auf
"dieses hier" reagieren, statt den Anwender nach Typnamen und
Instanz-IDs fragen zu müssen.

*Beispiel: Der Anwender öffnet eine Kundenliste und fragt "Warum
fehlt hier die Firma Müller?" Der Assistent sieht die aktive
Ansicht, erkennt die gesetzten Filter und antwortet: "Die Firma
Müller hat den Status 'inaktiv'. Dein aktueller Filter zeigt nur
aktive Kunden."*

Umgekehrt kann der Assistent den Anwender auch aktiv zu Informationen
führen: "Hier ist der Auftrag, den du suchst" – und navigiert die
Anwendung zur entsprechenden Detailansicht. Oder er markiert das
relevante Feld in einem Formular, statt nur dessen Namen zu nennen.

So wird der Assistent zum Copiloten, der neben dem Anwender sitzt,
den Bildschirm mitliest und im richtigen Moment eingreift – nicht
als abstraktes Frage-Antwort-System, sondern als kontextbewusster
Begleiter im Arbeitsalltag.

### Was kann er?

Der Assistent verfügt über Werkzeuge, die ihm Zugriff auf die
Anwendung geben.

**Datenmodell erkunden und erweitern**

Der Assistent kann das Datenmodell der Anwendung lesen und verstehen:
Welche Module gibt es? Welche Typen sind definiert? Welche
Eigenschaften und Beziehungen haben sie? Er kann nach Modellelementen
suchen, analysieren wo ein Typ verwendet wird – und das Modell
erweitern: neue Module, Typen, Eigenschaften und Beziehungen anlegen.

*Beispiel: "Welche Eigenschaften hat der Typ 'Kunde'?" oder "Lege
einen neuen Typ 'Lieferant' mit Name, Adresse und Bewertung an."*

Weitergedacht kann der Assistent eine komplette Fachanwendung im
Dialog aufbauen: "Ich brauche eine Projektverwaltung mit Aufgaben,
Meilensteinen und Ressourcenplanung." Er legt Module, Typen,
Eigenschaften und Beziehungen an, erstellt passende Oberflächen und
konfiguriert Formulare – alles in einem Gespräch. Kein UML, kein
Pflichtenheft, kein Entwickler nötig.

**Daten lesen und auswerten**

Der Assistent kann die eigentlichen Daten der Anwendung lesen: alle
Kunden auflisten, einen bestimmten Datensatz nachschlagen, Daten
filtern und zusammenfassen.

*Beispiel: "Liste alle aktiven Projekte auf" oder "Zeig mir die
Details zum Kunden mit der Nummer 4711."*

Über die reine Abfrage hinaus kann der Assistent Daten analysieren
und interpretieren: "Zeig mir die Umsatzentwicklung der letzten
12 Monate pro Region." Er schreibt ein Skript, führt es aus,
interpretiert die Ergebnisse und erklärt sie in natürlicher Sprache.
Oder er baut gleich eine Dashboard-Ansicht.

**Oberflächen verwalten**

Der Assistent kennt die verfügbaren Oberflächenvorlagen und kann
neue Ansichten erstellen. Er liest das Schema einer Vorlage (welche
Parameter werden erwartet?) und erzeugt daraus eine neue Oberfläche
mit den gewünschten Einstellungen.

*Beispiel: "Erstelle eine Tabelle für alle offenen Aufträge" oder
"Welche Oberflächenvorlagen gibt es?"*

Fachanwender können so ihre Arbeitsumgebung selbst gestalten: "Mach
die Kundenliste übersichtlicher, füge eine Spalte für den letzten
Kontakt hinzu und sortiere nach Priorität." Der Assistent ändert
die Konfiguration, ohne dass jemand einen Editor öffnen muss.

**Konfiguration verstehen**

Jede Konfigurationsmöglichkeit in TopLogic hat ein Schema, das
beschreibt, welche Einstellungen möglich sind. Der Assistent kann
diese Schemas lesen und so verstehen, wie eine bestimmte Komponente
konfiguriert werden kann.

*Beispiel: "Welche Einstellungen hat die Tabellenkomponente?" oder
"Was kann ich an einem Formularlayout alles konfigurieren?"*

**Geschäftslogik beschreiben**

TopLogic hat eine eigene Skriptsprache (TL-Script) für Berechnungen,
Abfragen und Automatisierungen. Der Assistent kann Skripte schreiben,
auf korrekte Syntax prüfen und ausführen. Er kennt die verfügbaren
Funktionen und deren Dokumentation.

*Beispiel: "Berechne die Summe aller offenen Rechnungen" oder
"Schreibe ein Skript, das alle inaktiven Benutzer auflistet."*

Damit kann der Assistent auch Geschäftsprozesse abbilden: "Wenn ein
Auftrag den Status 'abgeschlossen' bekommt, soll automatisch eine
Rechnung erzeugt werden." Er legt Regeln, Workflows oder
Skript-Ausdrücke an. Die Plattform führt sie aus – kein deployter
Code, sondern Konfiguration.

**Die Anwendung erklären**

Der Assistent kennt das Datenmodell und kann Zusammenhänge in
natürlicher Sprache darstellen: "Was bedeutet dieses Feld?", "Warum
sehe ich diese Daten nicht?", "Wie hängen Aufträge und Rechnungen
zusammen?" Die Anwendung wird selbsterklärend.

**Berechtigungen durchschaubar machen**

"Warum kann ich diesen Datensatz nicht bearbeiten?" – diese Frage
stellt nicht nur der Endanwender, sondern regelmäßig auch der
Entwickler. In einer konfigurierbaren Anwendung mit Rollen,
Gruppenrechten, Typ-Berechtigungen und instanzspezifischen Regeln
ist die Antwort oft nicht offensichtlich. Selbst wer die
Berechtigungskonfiguration kennt, muss im Einzelfall mehrere Ebenen
durchgehen, um nachzuvollziehen, warum ein bestimmter Zugriff
gewährt oder verweigert wird.

Der Assistent kann diese Kette nachvollziehen und erklären:

*Endanwender: "Warum sehe ich den Menüpunkt 'Berichte' nicht?"
– "Du hast die Rolle 'Sachbearbeiter'. Der Menüpunkt 'Berichte'
ist nur für die Rolle 'Teamleitung' sichtbar. Wende dich an deinen
Administrator, wenn du Zugriff benötigst."*

*Entwickler: "Warum kann der Benutzer 'meier' keine Aufträge
anlegen?" – "Der Benutzer hat die Rolle 'Vertrieb-Leser'. Diese
Rolle erlaubt Lesezugriff auf den Typ 'Auftrag', aber kein
Schreiben. Die Berechtigung ist definiert in der Rollengruppe
'Vertrieb' mit der Einschränkung 'nur lesen'."*

So wird der Assistent zum Berechtigungs-Debugger: Er durchsucht
Rollenzuordnungen, Gruppenmitgliedschaften und Berechtigungsregeln
und liefert eine nachvollziehbare Erklärung statt einer stummen
Verweigerung. Für Endanwender bedeutet das weniger Frust und
weniger Supportanfragen. Für Entwickler und Konfiguratoren
verkürzt es die Zeit für Fehlersuche in der
Berechtigungskonfiguration erheblich.

**Qualität sichern**

Der Assistent kann Konsistenz zwischen Modell, Konfiguration und
Oberflächen prüfen: "Sind alle Pflichtfelder auch in den Formularen
sichtbar?" oder "Gibt es Typen ohne Oberfläche?" Er findet
Ungereimtheiten, die bei manueller Prüfung leicht übersehen werden.

**Refactoring und Migration**

"Wir wollen 'Lieferant' und 'Kunde' zu 'Geschäftspartner'
zusammenführen." Der Assistent kann das Modell umbauen, Daten
migrieren und Oberflächen anpassen – eine Aufgabe, die manuell
Tage dauert.


## Der externe Agent

### Wie funktioniert er?

Der externe Agent ist ein eigenständiges KI-Werkzeug, das sich
über eine standardisierte Schnittstelle (MCP – Model Context
Protocol) mit der laufenden TopLogic-Anwendung verbindet. MCP ist ein
offener Standard, der von verschiedenen KI-Werkzeugen unterstützt
wird.

Die Verbindung erfolgt über HTTP. Der externe Agent hat Zugriff
auf dieselben Werkzeuge wie der eingebaute Assistent. Zusätzlich
stehen ihm passive Datenquellen zur Verfügung, die er gezielt
abfragen kann – zum Beispiel die Typen eines Moduls, die
Konfiguration einer Oberfläche oder das Schema eines
Konfigurationstyps.


## Der interne Agent

### Von der Assistenz zur Automatisierung

Der eingebaute Assistent reagiert auf
Anfragen: Ein Mensch stellt eine Frage oder gibt einen Auftrag.

Interne Agenten gehen einen Schritt weiter – sie handeln
eigenständig, Ereignis- oder Zeitgesteuert, ohne dass jemand sie anstoßen muss.

Ein interner Agent ist ein KI-Agent, der für eine bestimmte
Aufgabe in einer konkreten Anwendung eingerichtet wird. Er kennt
die Fachlichkeit, hat Zugriff auf dieselben Werkzeuge wie der
Assistent und arbeitet im Hintergrund.

### Zeitgesteuert

Interne Agenten können nach Zeitplan arbeiten:

- *Jeden Montagmorgen*: "Prüfe alle offenen Aufträge. Wenn die
  Lieferfrist in weniger als drei Tagen abläuft und kein Versand
  erfasst ist, eskaliere an den zuständigen Sachbearbeiter."

- *Jeden Monatsersten*: "Erstelle eine Übersicht aller
  abgeschlossenen Projekte mit Budget-Soll-Ist-Vergleich und sende
  sie an die Geschäftsführung."

- *Täglich nachts*: "Prüfe die Datenqualität: Gibt es Kunden ohne
  Ansprechpartner? Aufträge ohne Positionen? Kontakte mit
  ungültigen E-Mail-Adressen? Erstelle einen Bericht der
  Auffälligkeiten."

### Ereignisgesteuert

Noch wirkungsvoller sind Agenten, die auf Ereignisse in der
Anwendung reagieren:

- *Neuer Auftrag angelegt*: "Prüfe die Bonität des Kunden,
  berechne die voraussichtliche Lieferzeit und benachrichtige das
  Lager."

- *Status auf 'abgeschlossen' gesetzt*: "Erzeuge die Rechnung,
  aktualisiere die Umsatzstatistik und archiviere die
  Projektunterlagen."

- *Neuer Mitarbeiter erfasst*: "Lege die Standardzugänge an,
  erstelle einen Onboarding-Plan und weise die Einarbeitungsaufgaben
  dem Teamleiter zu."

### Was sie vom Skript unterscheidet

Zeitgesteuerte Aktionen und Ereignisreaktionen gibt es auch ohne KI
– als fest programmierte Skripte oder Workflow-Regeln. Der
Unterschied liegt in der Flexibilität:

Ein Skript führt immer exakt dieselben Schritte aus. Ein fachlicher
Agent *versteht* die Aufgabe und kann auf unvorhergesehene
Situationen reagieren. Wenn die Bonitätsprüfung ein unerwartetes
Ergebnis liefert, kann der Agent entscheiden, ob er den Auftrag
zurückstellt, eine Teillieferung vorschlägt oder einen Menschen
einbezieht. Ein Skript bricht ab oder ignoriert das Problem.

Außerdem sind fachliche Agenten leichter einzurichten. Statt eine
technische Regel zu programmieren, beschreibt man in natürlicher
Sprache, was der Agent tun soll. Das macht Automatisierung
zugänglich für Konfiguratoren und Fachabteilungen, nicht nur für
Entwickler.

### Vom Agenten zum Skript – und zurück

Agent und Skript sind keine Gegensätze, sondern zwei Enden eines
Spektrums. In einer No-Code-Plattform, die ihre eigene
Entwicklungsumgebung ist, kann die KI zwischen beiden Formen
vermitteln.

Der typische Weg beginnt mit einer textuellen Beschreibung: "Prüfe
jeden Abend, ob es Aufträge gibt, deren Lieferfrist morgen abläuft,
und sende eine Erinnerung an den zuständigen Sachbearbeiter." Das
genügt, um einen fachlichen Agenten einzurichten. Bei jeder
Ausführung interpretiert die KI die Aufgabe neu, wählt die
passenden Werkzeuge und reagiert auf die aktuelle Datenlage.

Wenn sich herausstellt, dass die Aufgabe immer gleich abläuft –
dieselben Schritte, keine Sonderfälle, keine Ermessensentscheidungen
– dann kann die KI daraus ein Skript erzeugen. Dieses Skript wird
direkt in die Anwendung eingebunden und läuft nativ, ohne
KI-Aufruf. Es ist schneller, kostengünstiger und vorhersagbar.

Der umgekehrte Weg ist ebenso möglich: Ein bestehendes Skript stößt
an seine Grenzen, weil neue Sonderfälle auftreten. Statt das Skript
immer weiter zu verkomplizieren, kann es durch einen fachlichen
Agenten ersetzt werden, der die Grundlogik kennt, aber flexibel auf
Ausnahmen reagiert.

So entsteht ein Kreislauf:

```
  Textuelle             Fachlicher            Generiertes
  Beschreibung  ----->  Agent       ----->    Skript
       ^                (flexibel,            (schnell,
       |                 KI-gestützt)          nativ)
       |                                        |
       +-------------- bei Bedarf <-------------+
                        zurück zum Agenten
```

Die Plattform bietet beiden Formen dieselbe Laufzeitumgebung. Ein
generiertes Skript ist kein Fremdkörper, der separat deployed werden
muss – es ist eine reguläre TL-Script-Konfiguration innerhalb der
Anwendung. Und der fachliche Agent, der es erzeugt hat, kann es bei
Bedarf auch wieder anpassen oder erweitern.

Für den Anwender bedeutet das: Er beschreibt, was er will. Die
Plattform entscheidet, ob ein Agent oder ein Skript die richtige
Ausführungsform ist – oder sie beginnt mit dem Agenten und
verdichtet die Lösung später zu einem Skript, wenn sich das Muster
stabilisiert hat.


## Zusammenspiel der drei Formen

Eingebauter Assistent, externer Assistent und fachliche Agenten
ergänzen sich:

| Eigenschaft      | Eingebauter Assistent     | Externer Assistent          | Interne Agenten              |
|------------------|---------------------------|-----------------------------|--------------------------------|
| Zugang           | Direkt in der Anwendung   | Eigenes KI-Werkzeug         | Im Hintergrund                 |
| Auslöser         | Frage oder Auftrag        | Frage oder Auftrag          | Zeitplan oder Ereignis         |
| Zielgruppe       | Endanwender, Fachabteilung | Konfiguratoren, Entwickler  | Fachabteilung, Administratoren |
| Stärke           | Schnelle Hilfe im Arbeitsalltag | Umfangreiche Konfiguration | Wiederkehrende Aufgaben, proaktives Handeln |
| Kontext          | Sieht, was der Anwender sieht | Sieht die gesamte Struktur | Kennt die fachlichen Regeln |

Alle drei nutzen denselben Satz an Werkzeugen. Eine Erweiterung der
Werkzeuge kommt automatisch allen Formen zugute.


## Fähigkeiten im Überblick

```
+-----------------------------------------------------------+
|                    KI-Assistent                           |
|                                                           |
|   "Welche Typen gibt es?"    "Erstelle eine Tabelle"     |
|   "Führe dieses Skript aus"  "Lege einen neuen Typ an"   |
+----------------------------+------------------------------+
                             |
                             v
+-----------------------------------------------------------+
|                   Werkzeugkasten                          |
|                                                           |
|   Modell         Daten          Oberflächen               |
|   erkunden       lesen          verwalten                 |
|                                                           |
|   Konfigurations-  TL-Script     Modell                   |
|   schemas lesen    ausführen     erweitern                |
+----------------------------+------------------------------+
                             |
                             v
+-----------------------------------------------------------+
|               TopLogic-Anwendung                          |
|                                                           |
|   Datenmodell    Geschäftsdaten    Oberflächen            |
|   Konfiguration  Skripte           Benutzer               |
+-----------------------------------------------------------+
```


## Semantisches Gedächtnis: Der Vectorindex

### Warum ein Vectorindex?

Die Werkzeuge geben dem Assistenten Zugriff auf die Anwendung. Aber
Zugriff allein reicht nicht – der Assistent braucht auch Verständnis.
Wenn ein Anwender fragt "Wie richte ich eine Genehmigungsstufe ein?",
muss der Assistent nicht nur wissen, welche Werkzeuge er hat, sondern
auch, wie die Plattform funktioniert, wie das konkrete Datenmodell
aufgebaut ist und welche fachlichen Zusammenhänge es gibt.

Dieses Verständnis entsteht durch einen Vectorindex: eine semantische
Wissensbasis, die Texte nicht nach Stichworten, sondern nach Bedeutung
durchsuchbar macht. Der Assistent findet relevante Informationen, auch
wenn die Formulierung nicht exakt übereinstimmt – "Lieferung" findet
auch "Versand", "Speditionsauftrag" und "Warenausgang".

Der Vectorindex wirkt auf drei Ebenen:

### Plattformverständnis

Für die Konfiguration einer spezifischen Plattform gibt es in den
Trainingsdaten der KI kaum Beispiele. Plattform-Dokumentation,
Best Practices, Konfigurationsbeispiele und Hilfetexte können als
Vectorindex bereitgestellt werden. Der Assistent findet so die
relevanten Dokumentationsstellen, bevor er überhaupt ein Werkzeug
aufruft.

*Beispiel: "Wie konfiguriere ich eine Tabelle mit gruppierten
Zeilen?" – der Vectorindex findet die passende Dokumentation zur
Tabellenkomponente und ihren Gruppierungsoptionen, auch wenn der
Anwender nicht den exakten Konfigurationsnamen kennt.*

Das kompensiert gezielt die Lücke im Vorwissen der KI: Was nicht in
den Trainingsdaten steckt, steckt im Vectorindex.

### Modellverständnis

Die Werkzeuge liefern strukturierte Informationen über das
Datenmodell: Typen, Eigenschaften, Beziehungen. Aber ein Vectorindex
über Modellbeschreibungen, Labels und Annotationen ermöglicht
zusätzlich semantische Suche: "Ich brauche etwas, das mit
Rechnungsstellung zu tun hat" findet Typen wie "Rechnung",
"Rechnungsposition", "Fakturierung" und "Zahlungseingang" – auch
wenn der Suchbegriff wörtlich nirgends vorkommt.

In einer modellbasierten Plattform können die Embeddings mit
strukturellem Wissen angereichert werden: Typzugehörigkeiten,
Beziehungen, Eigenschaftssemantik. Ein Kundendatensatz ist nicht
nur ein Textblock, sondern ein Objekt mit bekanntem Typ, bekannten
Beziehungen und bekannter Rolle im Geschäftsprozess. Das Modell
*ist* der Kontext, den man bei reiner Textsuche mühsam
rekonstruieren müsste.

### Datenverständnis

Die wirkungsvollste Ebene betrifft die konkreten Geschäftsdaten
einer Kundenanwendung. Wenn der Vectorindex nicht nur das Modell,
sondern auch die Inhalte der Anwendung erfasst, kann der Assistent
Fragen beantworten, die über strukturierte Abfragen hinausgehen:

- *Ähnlichkeitssuche*: "Finde Kunden mit ähnlichem Profil wie die
  Firma Müller" – basierend auf der Gesamtheit der Kundendaten,
  nicht auf einzelnen Feldern.

- *Dokumentensuche*: Viele Fachanwendungen verwalten Dokumente –
  Verträge, Protokolle, Spezifikationen. Ein Vectorindex über den
  Dokumentinhalt macht sie durchsuchbar: "Gibt es in unseren
  Verträgen Klauseln zur Haftungsbegrenzung?"

- *Muster erkennen*: "Welche Projekte hatten ähnliche Probleme wie
  Projekt X?" – der Vectorindex findet Ähnlichkeiten über
  Freitextfelder, Kommentare und Beschreibungen, die eine
  strukturierte Abfrage nicht erfassen würde.

- *Wissen aus Gesprächen*: Vergangene Konversationen mit dem
  Assistenten können indiziert werden. So baut sich über die
  Zeit ein institutionelles Gedächtnis auf: Was wurde schon
  gefragt? Welche Lösungen wurden gefunden? Welche
  Entscheidungen wurden getroffen?

### Zusammenspiel mit den Werkzeugen

Der Vectorindex ersetzt die Werkzeuge nicht – er ergänzt sie. Die
Werkzeuge liefern präzise, strukturierte Antworten: "Der Typ Kunde
hat die Eigenschaften Name, Adresse, Umsatz." Der Vectorindex
liefert Kontext und Hintergrund: "Kunden mit hohem Umsatz werden
im Quartalsbericht gesondert ausgewiesen, siehe Dokumentation zur
Key-Account-Verwaltung."

Zusammen ergibt das einen Assistenten, der nicht nur Zugriff auf die
Anwendung hat, sondern sie auch wirklich versteht – die Plattform,
das Modell und die konkreten Daten.


## Warum eine No-Code-Plattform mit KI?

### Der Unterschied zur KI-gestützten Programmierung

Wenn KI heute in der Softwareentwicklung eingesetzt wird, generiert sie
meistens Quellcode: Java, TypeScript, SQL, HTML, CSS. Das funktioniert
erstaunlich gut – aber der generierte Code muss kompiliert, getestet
und deployed werden, bevor er läuft.

Bei einer modellbasierten Plattform wie TopLogic ist der Ansatz
grundsätzlich anders: Die KI schreibt keinen Code, sondern
konfiguriert. Sie beschreibt *was* sie will, nicht *wie* es technisch
umgesetzt wird. Die Plattform kümmert sich um das Wie.

Das ist ein feiner, aber wichtiger Unterschied. Die KI ersetzt nicht
den Entwickler, indem sie denselben Job macht (Code schreiben).
Stattdessen wird der Job selbst überflüssig, weil kein Code nötig
ist.

### Vorteile

**Kleinerer Fehlerraum**

Eine Konfiguration ist entweder gültig oder nicht – das Schema sagt
es sofort. Generierter Code kann syntaktisch korrekt, aber inhaltlich
falsch sein: Sicherheitslücken, Timing-Probleme, falsche Datentypen.
Die Plattform verhindert ganze Fehlerklassen, die bei generiertem Code
auftreten können.

**Sofortige Wirkung**

Änderungen an Konfiguration und Modell wirken sofort. Bei
herkömmlicher Entwicklung muss generierter Code erst kompiliert,
getestet und in Betrieb genommen werden. Das dauert und ist eine
zusätzliche Fehlerquelle.

**Garantierte Konsistenz**

Die Plattform sorgt dafür, dass Datenmodell, Datenhaltung und
Oberfläche zusammenpassen. Bei herkömmlicher Entwicklung muss die KI
Datenbankschema, Schnittstellen, Oberfläche und Tests konsistent
halten – eine Aufgabe, an der auch erfahrene Entwickler regelmäßig
scheitern.

**Weniger Halluzination**

Die KI arbeitet mit konkreten Schemas und bekommt strukturierte
Antworten. Sie erfindet keine Schnittstellen, sondern nutzt
vorhandene. Bei der Code-Generierung erfindet die KI regelmäßig
Funktionen und Bibliotheken, die es gar nicht gibt.

**Nachvollziehbarkeit**

Konfigurationsänderungen sind deklarativ: Man sieht, was eingestellt
ist, nicht wie es umgesetzt wurde. Generierter Code ist schwerer zu
prüfen – war er vor der Änderung korrekt? Was genau hat sich
geändert? Bei Konfiguration ist das offensichtlich.

**Fachleute können mitreden**

Ein Fachanwender kann eine Modellbeschreibung lesen und sagen "Ja, das
stimmt" oder "Nein, da fehlt etwas". Generierten Programmcode kann er
nicht beurteilen. Die Plattform bringt die KI näher an die
Fachsprache.

**Weniger Entscheidungen, weniger Fehler**

Statt tausend Zeilen Code zu generieren, trifft die KI zehn
Konfigurationsentscheidungen. Die Fehlerrate pro Entscheidung mag
ähnlich sein – aber zehn statt tausend Entscheidungen ergeben in
Summe weniger Fehler.

### Grenzen

**Plattform-Decke**

Was die Plattform nicht kann, kann auch die KI nicht konfigurieren.
Herkömmliche Entwicklung hat diese Grenze prinzipiell nicht. Sehr
spezifische Oberflächen-Interaktionen oder Integrationen mit
Fremdsystemen, die die Plattform nicht vorsieht, erfordern weiterhin
klassische Entwicklung.

**Plattform-Wissen nötig**

Die KI muss die Konzepte und Eigenheiten der Plattform kennen. Bei
allgemeiner Programmierung reicht Wissen über gängige Sprachen und
Frameworks, das in den Trainingsdaten der KI massiv vertreten ist.
Plattform-spezifisches Wissen ist seltener – das wird über
Werkzeuge, Schemas und den Vectorindex kompensiert (siehe
*Semantisches Gedächtnis*).

**Weniger Vorwissen**

Für gängige Programmiersprachen und Frameworks gibt es Millionen von
Beispielen in den Trainingsdaten der KI. Für die Konfiguration einer
spezifischen Plattform praktisch keine. Die KI stützt sich auf
Werkzeuge, Schemas und den Vectorindex statt auf gelerntes Wissen.
Das macht die Qualität dieser Bausteine umso wichtiger.

**Grenzen der Konfigurationssprache**

Manche Anforderungen lassen sich in Programmcode natürlicher
ausdrücken als in Konfiguration. "Sortiere die Liste nach einem
komplexen Score aus drei gewichteten Faktoren" ist in Code trivial, in
Konfiguration möglicherweise umständlich. Die Skriptsprache der
Plattform kann vieles abfangen, aber nicht alles.


## Erweiterbarkeit

Die Werkzeuge des Assistenten können auf mehrere Arten erweitert
werden:

- **Neue Werkzeuge programmieren**: Entwickler können neue Werkzeuge
  in Java implementieren und registrieren. Ein neues Werkzeug steht
  automatisch den Assistentenformen zur Verfügung.

- **Skript-basierte Datenquellen**: Neue Datenquellen können per
  TL-Script definiert werden – ohne Programmierung. So können
  Konfiguratoren dem Assistenten Zugriff auf zusätzliche Informationen
  geben.

- **Anpassung des Verhaltens**: Über Systemprompts und
  Werkzeugauswahl lässt sich steuern, welche Fähigkeiten der
  Assistent hat und wie er sich verhält.


## Sicherheit und Berechtigungen

### Grundprinzip

Der Assistent handelt immer *als* der angemeldete Nutzer. Er hat
exakt dieselben Rechte – kann nichts sehen, was der Nutzer nicht
sehen dürfte, und nichts ändern, was der Nutzer nicht ändern
dürfte. Er ist wie ein Sachbearbeiter mit denselben Berechtigungen,
der aber effizienter arbeitet, weil er Abkürzungen kennt.

Änderungen am Datenmodell und an Daten erfolgen transaktional: Sie
werden entweder vollständig durchgeführt oder vollständig
zurückgerollt. Ein fehlgeschlagener Vorgang hinterlässt keine
inkonsistenten Daten.

### Berechtigungen auf zwei Ebenen

In einer klassischen TopLogic-Anwendung sind Berechtigungen stark an
die Oberfläche gebunden: Ein Nutzer darf eine bestimmte Ansicht
sehen, ein bestimmtes Kommando ausführen, ein bestimmtes Feld in
einem bestimmten Formular bearbeiten. Das ergibt Sinn, weil die
Oberfläche der einzige Zugang zur Anwendung ist.

Der KI-Assistent arbeitet aber nicht über die Oberfläche. Er greift
direkt auf das Datenmodell und die Geschäftsdaten zu. Würde man ihn
an die Oberflächenberechtigungen binden, müsste er sich gedanklich
durch Ansichten und Kommandos hangeln – und ein großer Teil seines
Effizienzvorteils ginge verloren.

Die Lösung liegt darin, Berechtigungen auf zwei Ebenen zu betrachten:

**Daten-Ebene**: Wer darf welche Daten lesen und schreiben –
unabhängig von der Oberfläche? Das betrifft Typen, Instanzen und
Eigenschaften. Zum Beispiel: "Rolle X darf Kunden lesen aber nicht
ändern", "Rolle Y darf Aufträge anlegen."

**Oberflächen-Ebene**: Welche Ansichten und Kommandos sind sichtbar?
Das ist eine *Projektion* der Daten-Ebene auf die Benutzeroberfläche.
Sie bestimmt, *wie* ein Nutzer seine Rechte ausüben kann – nicht
*welche* Rechte er hat.

Für die Oberfläche reicht die Oberflächenebene, weil sie die
Datenberechtigungen implizit abbildet: Wenn ein Nutzer den
"Löschen"-Button nicht sieht, kann er nicht löschen.

Für den KI-Assistenten braucht man die Daten-Ebene explizit. Er
prüft nicht "darf ich den Button klicken?", sondern "darf dieser
Nutzer Instanzen vom Typ Kunde ändern?" Das ist direkter, schneller
– und genauso sicher.

### Was dabei gewonnen wird

Wenn die Daten-Ebene sauber existiert, gewinnen beide Seiten:

- **Der Assistent** arbeitet effizient, weil er direkt auf Daten
  zugreift und bei jedem Zugriff gegen die Datenberechtigungen
  geprüft wird. Kein Umweg über simulierte Oberflächeninteraktionen.

- **Die Oberfläche** wird einfacher, weil sie sich auf die
  Datenberechtigungen stützen kann, statt eigene Regeln zu pflegen.
  "Zeige den Löschen-Button" wird abgeleitet aus "Nutzer darf diesen
  Typ löschen", statt separat konfiguriert.

- **Die Konsistenz** steigt: Es gibt eine einzige Quelle der Wahrheit
  für Berechtigungen. Ob ein Zugriff über die Oberfläche, den
  Assistenten oder eine Schnittstelle kommt – dieselben Regeln gelten.

- **Zukünftige Schnittstellen** profitieren ebenfalls. Jede
  Erweiterung – REST-API, Batch-Jobs, Integrationen mit
  Fremdsystemen – kann sich auf dieselbe Berechtigungsschicht
  stützen.


## Ausblick: Über die einzelne Anwendung hinaus

### Anwendungen, die sich selbst kennen

Alles bisher Beschriebene spielt sich innerhalb einer einzelnen
Anwendung ab. Aber in einem Unternehmen gibt es nicht eine Anwendung,
sondern viele – und jede modellbasierte Anwendung hat eine
Eigenschaft, die herkömmliche Software nicht hat: Sie kennt sich
selbst.

Eine TopLogic-Anwendung weiß, welche Datentypen sie verwaltet,
welche Beziehungen zwischen ihnen bestehen, welche
Geschäftsprozesse sie abbildet und wie ihre Oberflächen aufgebaut
sind. Dieses Wissen steckt nicht in einer externen Dokumentation, die
veralten kann, sondern im laufenden System selbst. Was der
KI-Assistent für einen einzelnen Anwender tut – die Anwendung
erkunden und erklären – kann er prinzipiell auch für andere
Systeme tun.

### Agenten als Schnittstelle

Heute werden Integrationen zwischen Anwendungen über
zweckgebundene Schnittstellen realisiert: Eine REST-API für
Kundendaten, ein Kafka-Topic für Auftragsereignisse, ein
Dateiexport für das Controlling. Jede Schnittstelle wird für
einen bestimmten Zweck gebaut und muss bei Änderungen angepasst
werden.

Wenn eine Anwendung ihren Agenten auch nach außen zugänglich
macht, entsteht eine Schnittstelle anderer Art. Statt eines starren
Datenformats bietet sie einen Gesprächspartner an, der die
Anwendung versteht und Fragen beantworten kann:

- "Welche Auftragstypen gibt es in eurem System?"
- "Gibt es offene Aufträge für den Kunden mit der Nummer 4711?"
- "Wie ist der Genehmigungsprozess für Bestellungen aufgebaut?"

Das ersetzt nicht die klassischen Schnittstellen – für hohe
Durchsätze und Echtzeitverarbeitung bleibt eine dedizierte API
der richtige Weg. Aber für Erkundung, Abstimmung und
gelegentliche Abfragen bietet ein Agent etwas, das keine API kann:
Er versteht Kontext und kann erklären.

### Lebendige Unternehmensarchitektur

Wenn jede Anwendung im Unternehmen einen Agenten hat, der ihr
Modell und ihre Prozesse kennt, ergibt sich eine neue Möglichkeit:
Ein übergreifender Agent, der die Unternehmensarchitektur nicht
aus Dokumenten liest, sondern aus den laufenden Systemen.

Heute sieht Unternehmensarchitektur oft so aus: Es gibt
Architekturdiagramme, Schnittstellenverzeichnisse und
Prozessdokumentationen. Sie werden mit Aufwand erstellt und sind
am nächsten Tag schon nicht mehr ganz aktuell. Niemand hat ein
vollständiges, aktuelles Bild der Systemlandschaft.

Ein Architektur-Agent könnte die Agenten der einzelnen Anwendungen
befragen:

- "Welche Systeme verwalten Kundendaten? Wie sind die Datenmodelle
  aufgebaut? Wo gibt es Überschneidungen?"
- "Wenn wir den Typ 'Lieferant' im ERP ändern – welche anderen
  Systeme sind betroffen?"
- "Zeig mir den kompletten Lebenszyklus eines Auftrags über alle
  beteiligten Systeme hinweg."

Die Antworten kommen nicht aus einer Dokumentation, sondern aus den
Systemen selbst – und sind damit immer aktuell.

### Was das verändert

**Integrationsplanung wird einfacher.** Statt Schnittstellendoku-
mentationen zu lesen und Ansprechpartner zu suchen, fragt ein Agent
die beteiligten Systeme direkt. "Welche Daten kann das CRM liefern?
In welchem Format? Wie oft ändern sie sich?"

**Auswirkungsanalysen werden möglich.** Vor einer Änderung kann
der Architektur-Agent prüfen, welche Systeme betroffen wären. Das
passiert nicht auf Basis eines statischen Modells, sondern auf Basis
des tatsächlichen Zustands.

**Redundanzen werden sichtbar.** Wenn mehrere Systeme ähnliche
Datenmodelle haben, kann der Agent das erkennen und aufzeigen. "Das
CRM und das ERP verwalten beide Kundenadressen, aber mit
unterschiedlichen Feldern. Hier ist der Vergleich." Besonders
wirkungsvoll wird das mit semantischer Suche über die Vectorindizes
der einzelnen Anwendungen: Der Agent findet Überschneidungen auch
dann, wenn die Systeme unterschiedliche Begriffe verwenden –
"Geschäftspartner" im einen, "Mandant" im anderen.

**Dokumentation wird überflüssig – oder erzeugt sich selbst.**
Wenn ein Agent die Architektur jederzeit live abfragen kann, braucht
es kein manuell gepflegtes Architekturverzeichnis mehr. Oder der
Agent erzeugt die Dokumentation auf Knopfdruck aus dem aktuellen
Stand – immer aktuell, immer vollständig.
