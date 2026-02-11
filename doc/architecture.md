# TopLogic KI-Integration -- Architekturuebersicht


## Was ist das?

Die TopLogic KI-Integration macht eine TopLogic-Anwendung fuer
KI-Assistenten zugaenglich. Ein KI-Assistent kann die Anwendung
verstehen, mit ihr arbeiten und sie konfigurieren -- aehnlich wie ein
erfahrener Berater, der die Anwendung kennt.

Dabei gibt es drei Einsatzformen:

- **Eingebauter Assistent**: Ein KI-Chat direkt in der Anwendung, ueber
  den Anwender Fragen stellen und Aufgaben ausfuehren lassen koennen.

- **Externer Assistent**: Ein eigenstaendiges KI-Werkzeug (z.B. Claude
  Desktop, Cursor), das sich von aussen mit der Anwendung verbindet und
  mit ihr arbeitet.

- **Fachliche Agenten**: Spezialisierte KI-Agenten innerhalb der
  Anwendung, die eigenstaendig wiederkehrende Aufgaben uebernehmen --
  zeitgesteuert oder als Reaktion auf Ereignisse.

Alle drei Formen nutzen dieselben Werkzeuge. Was der eingebaute
Assistent kann, kann auch ein fachlicher Agent -- nur eben
automatisch und ohne dass jemand eine Frage stellt.


## Warum?

TopLogic-Anwendungen sind hochgradig konfigurierbar: Datenmodelle,
Oberflaechen, Geschaeftslogik -- vieles laesst sich ohne Programmierung
anpassen. Diese Konfigurierbarkeit ist eine Staerke, erfordert aber
Fachwissen ueber die Plattform.

Gleichzeitig gibt es in jeder Anwendung eine Luecke zwischen dem, was
die Oberflaeche bietet, und dem, was Anwender im Alltag brauchen.
Fuer haeufige Arbeitsschritte gibt es spezialisierte Funktionen --
aber fuer die vielen einmaligen oder seltenen Aufgaben nicht. "Setze
bei allen Auftraegen aus dem letzten Quartal den Status auf
'archiviert'", "Uebertrage die Kontaktdaten aus der alten Struktur
in die neue", "Zeig mir alle Projekte, bei denen Budget und
Aufwand um mehr als 20% auseinanderliegen" -- solche Aufgaben fallen
regelmaessig an, aber keine davon rechtfertigt eine eigene
Anwendungsfunktion.

Bisher bleiben zwei Moeglichkeiten: Entweder man baut fuer jeden
solchen Fall eine Spezialfunktion (aufwaendig, und die meisten
Wuensche werden nie umgesetzt), oder der Anwender erledigt es von
Hand (fehleranfaellig und muehsam bei grossen Datenmengen).

Ein KI-Assistent ist das Schweizer Taschenmesser fuer genau diese
Situationen. Er braucht keine vorgefertigte Funktion -- er versteht
die Aufgabe in natuerlicher Sprache und fuehrt sie mit den
vorhandenen Werkzeugen aus. Das betrifft nicht nur Massenoperationen,
sondern jede Aufgabe, fuer die es keinen eigenen Button gibt: Daten
abgleichen, Zusammenfassungen erstellen, Plausibilitaeten pruefen,
Aenderungen nach Regeln durchfuehren.

Damit verschiebt sich die Frage von "Haben wir dafuer eine Funktion?"
zu "Kann ich das dem Assistenten sagen?" -- und die Antwort ist fast
immer ja.

Im Einzelnen profitieren verschiedene Zielgruppen:

- **Endanwender**: Fragen zur Anwendung stellen, Daten suchen und
  auswerten, Routineaufgaben automatisieren -- ohne auf eine
  Spezialfunktion warten zu muessen.

- **Konfiguratoren**: Datenmodelle erkunden und erweitern,
  Oberflaechen erstellen und anpassen, Konfigurationsaenderungen
  durchfuehren.

- **Entwickler**: Die Anwendungsstruktur verstehen, Aenderungen
  planen und umsetzen, Skripte schreiben und testen.


## Der eingebaute Assistent

### Wie funktioniert er?

Der eingebaute Assistent ist ein Chat-Fenster innerhalb der
TopLogic-Anwendung. Der Anwender stellt eine Frage oder gibt einen
Auftrag in natuerlicher Sprache. Der Assistent versteht die Anfrage,
fuehrt die noetige Aktion aus und antwortet.

Hinter den Kulissen verbindet sich der Assistent mit einem
KI-Sprachmodell (z.B. von OpenAI oder Anthropic). Dieses Sprachmodell
entscheidet selbstaendig, welche Werkzeuge es braucht, um die Anfrage
zu beantworten. Es kann mehrere Werkzeuge nacheinander einsetzen, um
komplexe Aufgaben zu loesen.

### Vom Chatbot zum Copiloten

Ein reiner Chatbot beantwortet Fragen. Ein Copilot arbeitet mit --
er sieht, was der Anwender sieht, und handelt im selben Kontext.

Der entscheidende Schritt ist der Zugriff auf den Benutzerkontext:
Der Assistent weiss, welche Ansicht gerade geoeffnet ist, welches
Objekt selektiert ist, welche Filter aktiv sind und welche Felder
im Formular ausgefuellt oder fehlerhaft sind. Damit kann er auf
"dieses hier" reagieren, statt den Anwender nach Typnamen und
Instanz-IDs fragen zu muessen.

*Beispiel: Der Anwender oeffnet eine Kundenliste und fragt "Warum
fehlt hier die Firma Mueller?" Der Assistent sieht die aktive
Ansicht, erkennt die gesetzten Filter und antwortet: "Die Firma
Mueller hat den Status 'inaktiv'. Dein aktueller Filter zeigt nur
aktive Kunden."*

Umgekehrt kann der Assistent den Anwender auch aktiv zu Informationen
fuehren: "Hier ist der Auftrag, den du suchst" -- und navigiert die
Anwendung zur entsprechenden Detailansicht. Oder er markiert das
relevante Feld in einem Formular, statt nur dessen Namen zu nennen.

So wird der Assistent zum Copiloten, der neben dem Anwender sitzt,
den Bildschirm mitliest und im richtigen Moment eingreift -- nicht
als abstraktes Frage-Antwort-System, sondern als kontextbewusster
Begleiter im Arbeitsalltag.

### Was kann er?

Der Assistent verfuegt ueber Werkzeuge, die ihm Zugriff auf die
Anwendung geben.

**Datenmodell erkunden und erweitern**

Der Assistent kann das Datenmodell der Anwendung lesen und verstehen:
Welche Module gibt es? Welche Typen sind definiert? Welche
Eigenschaften und Beziehungen haben sie? Er kann nach Modellelementen
suchen, analysieren wo ein Typ verwendet wird -- und das Modell
erweitern: neue Module, Typen, Eigenschaften und Beziehungen anlegen.

*Beispiel: "Welche Eigenschaften hat der Typ 'Kunde'?" oder "Lege
einen neuen Typ 'Lieferant' mit Name, Adresse und Bewertung an."*

Weitergedacht kann der Assistent eine komplette Fachanwendung im
Dialog aufbauen: "Ich brauche eine Projektverwaltung mit Aufgaben,
Meilensteinen und Ressourcenplanung." Er legt Module, Typen,
Eigenschaften und Beziehungen an, erstellt passende Oberflaechen und
konfiguriert Formulare -- alles in einem Gespraech. Kein UML, kein
Pflichtenheft, kein Entwickler noetig.

**Daten lesen und auswerten**

Der Assistent kann die eigentlichen Daten der Anwendung lesen: alle
Kunden auflisten, einen bestimmten Datensatz nachschlagen, Daten
filtern und zusammenfassen.

*Beispiel: "Liste alle aktiven Projekte auf" oder "Zeig mir die
Details zum Kunden mit der Nummer 4711."*

Ueber die reine Abfrage hinaus kann der Assistent Daten analysieren
und interpretieren: "Zeig mir die Umsatzentwicklung der letzten
12 Monate pro Region." Er schreibt ein Skript, fuehrt es aus,
interpretiert die Ergebnisse und erklaert sie in natuerlicher Sprache.
Oder er baut gleich eine Dashboard-Ansicht.

**Oberflaechen verwalten**

Der Assistent kennt die verfuegbaren Oberflaechenvorlagen und kann
neue Ansichten erstellen. Er liest das Schema einer Vorlage (welche
Parameter werden erwartet?) und erzeugt daraus eine neue Oberflaeche
mit den gewuenschten Einstellungen.

*Beispiel: "Erstelle eine Tabelle fuer alle offenen Auftraege" oder
"Welche Oberflaechenvorlagen gibt es?"*

Fachanwender koennen so ihre Arbeitsumgebung selbst gestalten: "Mach
die Kundenliste uebersichtlicher, fuege eine Spalte fuer den letzten
Kontakt hinzu und sortiere nach Prioritaet." Der Assistent aendert
die Konfiguration, ohne dass jemand einen Editor oeffnen muss.

**Konfiguration verstehen**

Jede Konfigurationsmoeglichkeit in TopLogic hat ein Schema, das
beschreibt, welche Einstellungen moeglich sind. Der Assistent kann
diese Schemas lesen und so verstehen, wie eine bestimmte Komponente
konfiguriert werden kann.

*Beispiel: "Welche Einstellungen hat die Tabellenkomponente?" oder
"Was kann ich an einem Formularlayout alles konfigurieren?"*

**Geschaeftslogik beschreiben**

TopLogic hat eine eigene Skriptsprache (TL-Script) fuer Berechnungen,
Abfragen und Automatisierungen. Der Assistent kann Skripte schreiben,
auf korrekte Syntax pruefen und ausfuehren. Er kennt die verfuegbaren
Funktionen und deren Dokumentation.

*Beispiel: "Berechne die Summe aller offenen Rechnungen" oder
"Schreibe ein Skript, das alle inaktiven Benutzer auflistet."*

Damit kann der Assistent auch Geschaeftsprozesse abbilden: "Wenn ein
Auftrag den Status 'abgeschlossen' bekommt, soll automatisch eine
Rechnung erzeugt werden." Er legt Regeln, Workflows oder
Skript-Ausdruecke an. Die Plattform fuehrt sie aus -- kein deployter
Code, sondern Konfiguration.

**Die Anwendung erklaeren**

Der Assistent kennt das Datenmodell und kann Zusammenhaenge in
natuerlicher Sprache darstellen: "Was bedeutet dieses Feld?", "Warum
sehe ich diese Daten nicht?", "Wie haengen Auftraege und Rechnungen
zusammen?" Die Anwendung wird selbsterklarend.

**Berechtigungen durchschaubar machen**

"Warum kann ich diesen Datensatz nicht bearbeiten?" -- diese Frage
stellt nicht nur der Endanwender, sondern regelmaessig auch der
Entwickler. In einer konfigurierbaren Anwendung mit Rollen,
Gruppenrechten, Typ-Berechtigungen und instanzspezifischen Regeln
ist die Antwort oft nicht offensichtlich. Selbst wer die
Berechtigungskonfiguration kennt, muss im Einzelfall mehrere Ebenen
durchgehen, um nachzuvollziehen, warum ein bestimmter Zugriff
gewaehrt oder verweigert wird.

Der Assistent kann diese Kette nachvollziehen und erklaeren:

*Endanwender: "Warum sehe ich den Menuepunkt 'Berichte' nicht?"
-- "Du hast die Rolle 'Sachbearbeiter'. Der Menuepunkt 'Berichte'
ist nur fuer die Rolle 'Teamleitung' sichtbar. Wende dich an deinen
Administrator, wenn du Zugriff benoetigen."*

*Entwickler: "Warum kann der Benutzer 'meier' keine Auftraege
anlegen?" -- "Der Benutzer hat die Rolle 'Vertrieb-Leser'. Diese
Rolle erlaubt Lesezugriff auf den Typ 'Auftrag', aber kein
Schreiben. Die Berechtigung ist definiert in der Rollengruppe
'Vertrieb' mit der Einschraenkung 'nur lesen'."*

So wird der Assistent zum Berechtigungs-Debugger: Er durchsucht
Rollenzuordnungen, Gruppenmitgliedschaften und Berechtigungsregeln
und liefert eine nachvollziehbare Erklaerung statt einer stummen
Verweigerung. Fuer Endanwender bedeutet das weniger Frust und
weniger Supportanfragen. Fuer Entwickler und Konfiguratoren
verkuerzt es die Zeit fuer Fehlersuche in der
Berechtigungskonfiguration erheblich.

**Qualitaet sichern**

Der Assistent kann Konsistenz zwischen Modell, Konfiguration und
Oberflaechen pruefen: "Sind alle Pflichtfelder auch in den Formularen
sichtbar?" oder "Gibt es Typen ohne Oberflaeche?" Er findet
Ungereimtheiten, die bei manueller Pruefung leicht uebersehen werden.

**Refactoring und Migration**

"Wir wollen 'Lieferant' und 'Kunde' zu 'Geschaeftspartner'
zusammenfuehren." Der Assistent kann das Modell umbauen, Daten
migrieren und Oberflaechen anpassen -- eine Aufgabe, die manuell
Tage dauert.


## Der externe Assistent

### Wie funktioniert er?

Der externe Assistent ist ein eigenstaendiges KI-Werkzeug, das sich
ueber eine standardisierte Schnittstelle (MCP -- Model Context
Protocol) mit der laufenden TopLogic-Anwendung verbindet. MCP ist ein
offener Standard, der von verschiedenen KI-Werkzeugen unterstuetzt
wird.

Die Verbindung erfolgt ueber HTTP. Der externe Assistent hat Zugriff
auf dieselben Werkzeuge wie der eingebaute Assistent. Zusaetzlich
stehen ihm passive Datenquellen zur Verfuegung, die er gezielt
abfragen kann -- zum Beispiel die Typen eines Moduls, die
Konfiguration einer Oberflaeche oder das Schema eines
Konfigurationstyps.


## Fachliche Agenten

### Von der Assistenz zur Automatisierung

Der eingebaute Assistent und der externe Assistent reagieren auf
Anfragen: Ein Mensch stellt eine Frage oder gibt einen Auftrag.
Fachliche Agenten gehen einen Schritt weiter -- sie handeln
eigenstaendig, ohne dass jemand sie anstossen muss.

Ein fachlicher Agent ist ein KI-Agent, der fuer eine bestimmte
Aufgabe in einer konkreten Anwendung eingerichtet wird. Er kennt
die Fachlichkeit, hat Zugriff auf dieselben Werkzeuge wie der
Assistent und arbeitet im Hintergrund.

### Zeitgesteuert

Fachliche Agenten koennen nach Zeitplan arbeiten:

- *Jeden Montagmorgen*: "Pruefe alle offenen Auftraege. Wenn die
  Lieferfrist in weniger als drei Tagen ablaeuft und kein Versand
  erfasst ist, eskaliere an den zustaendigen Sachbearbeiter."

- *Jeden Monatsersten*: "Erstelle eine Uebersicht aller
  abgeschlossenen Projekte mit Budget-Soll-Ist-Vergleich und sende
  sie an die Geschaeftsfuehrung."

- *Taeglich nachts*: "Pruefe die Datenqualitaet: Gibt es Kunden ohne
  Ansprechpartner? Auftraege ohne Positionen? Kontakte mit
  ungueltigen E-Mail-Adressen? Erstelle einen Bericht der
  Auffaelligkeiten."

### Ereignisgesteuert

Noch wirkungsvoller sind Agenten, die auf Ereignisse in der
Anwendung reagieren:

- *Neuer Auftrag angelegt*: "Pruefe die Bonitaet des Kunden,
  berechne die voraussichtliche Lieferzeit und benachrichtige das
  Lager."

- *Status auf 'abgeschlossen' gesetzt*: "Erzeuge die Rechnung,
  aktualisiere die Umsatzstatistik und archiviere die
  Projektunterlagen."

- *Neuer Mitarbeiter erfasst*: "Lege die Standardzugaenge an,
  erstelle einen Onboarding-Plan und weise die Einarbeitungsaufgaben
  dem Teamleiter zu."

### Was sie vom Skript unterscheidet

Zeitgesteuerte Aktionen und Ereignisreaktionen gibt es auch ohne KI
-- als fest programmierte Skripte oder Workflow-Regeln. Der
Unterschied liegt in der Flexibilitaet:

Ein Skript fuehrt immer exakt dieselben Schritte aus. Ein fachlicher
Agent *versteht* die Aufgabe und kann auf unvorhergesehene
Situationen reagieren. Wenn die Bonitaetspruefung ein unerwartetes
Ergebnis liefert, kann der Agent entscheiden, ob er den Auftrag
zurueckstellt, eine Teillieferung vorschlaegt oder einen Menschen
einbezieht. Ein Skript bricht ab oder ignoriert das Problem.

Ausserdem sind fachliche Agenten leichter einzurichten. Statt eine
technische Regel zu programmieren, beschreibt man in natuerlicher
Sprache, was der Agent tun soll. Das macht Automatisierung
zugaenglich fuer Konfiguratoren und Fachabteilungen, nicht nur fuer
Entwickler.

### Vom Agenten zum Skript -- und zurueck

Agent und Skript sind keine Gegensaetze, sondern zwei Enden eines
Spektrums. In einer No-Code-Plattform, die ihre eigene
Entwicklungsumgebung ist, kann die KI zwischen beiden Formen
vermitteln.

Der typische Weg beginnt mit einer textuellen Beschreibung: "Pruefe
jeden Abend, ob es Auftraege gibt, deren Lieferfrist morgen ablaeuft,
und sende eine Erinnerung an den zustaendigen Sachbearbeiter." Das
genuegt, um einen fachlichen Agenten einzurichten. Bei jeder
Ausfuehrung interpretiert die KI die Aufgabe neu, waehlt die
passenden Werkzeuge und reagiert auf die aktuelle Datenlage.

Wenn sich herausstellt, dass die Aufgabe immer gleich ablaeuft --
dieselben Schritte, keine Sonderfaelle, keine Ermessensentscheidungen
-- dann kann die KI daraus ein Skript erzeugen. Dieses Skript wird
direkt in die Anwendung eingebunden und laeuft nativ, ohne
KI-Aufruf. Es ist schneller, kostenguenstiger und vorhersagbar.

Der umgekehrte Weg ist ebenso moeglich: Ein bestehendes Skript stoesst
an seine Grenzen, weil neue Sonderfaelle auftreten. Statt das Skript
immer weiter zu verkomplizieren, kann es durch einen fachlichen
Agenten ersetzt werden, der die Grundlogik kennt, aber flexibel auf
Ausnahmen reagiert.

So entsteht ein Kreislauf:

```
  Textuelle             Fachlicher            Generiertes
  Beschreibung  ----->  Agent       ----->    Skript
       ^                (flexibel,            (schnell,
       |                 KI-gestuetzt)         nativ)
       |                                        |
       +-------------- bei Bedarf <-------------+
                        zurueck zum Agenten
```

Die Plattform bietet beiden Formen dieselbe Laufzeitumgebung. Ein
generiertes Skript ist kein Fremdkoerper, der separat deployed werden
muss -- es ist eine regulaere TL-Script-Konfiguration innerhalb der
Anwendung. Und der fachliche Agent, der es erzeugt hat, kann es bei
Bedarf auch wieder anpassen oder erweitern.

Fuer den Anwender bedeutet das: Er beschreibt, was er will. Die
Plattform entscheidet, ob ein Agent oder ein Skript die richtige
Ausfuehrungsform ist -- oder sie beginnt mit dem Agenten und
verdichtet die Loesung spaeter zu einem Skript, wenn sich das Muster
stabilisiert hat.


## Zusammenspiel der drei Formen

Eingebauter Assistent, externer Assistent und fachliche Agenten
ergaenzen sich:

| Eigenschaft      | Eingebauter Assistent     | Externer Assistent          | Fachliche Agenten              |
|------------------|---------------------------|-----------------------------|--------------------------------|
| Zugang           | Direkt in der Anwendung   | Eigenes KI-Werkzeug         | Im Hintergrund                 |
| Ausloser         | Frage oder Auftrag        | Frage oder Auftrag          | Zeitplan oder Ereignis         |
| Zielgruppe       | Endanwender, Fachabteilung | Konfiguratoren, Entwickler  | Fachabteilung, Administratoren |
| Staerke          | Schnelle Hilfe im Arbeitsalltag | Umfangreiche Konfiguration | Wiederkehrende Aufgaben, proaktives Handeln |
| Kontext          | Sieht, was der Anwender sieht | Sieht die gesamte Struktur | Kennt die fachlichen Regeln |

Alle drei nutzen denselben Satz an Werkzeugen. Eine Erweiterung der
Werkzeuge kommt automatisch allen Formen zugute.


## Faehigkeiten im Ueberblick

```
+-----------------------------------------------------------+
|                    KI-Assistent                           |
|                                                           |
|   "Welche Typen gibt es?"    "Erstelle eine Tabelle"     |
|   "Fuehre dieses Skript aus" "Lege einen neuen Typ an"   |
+----------------------------+------------------------------+
                             |
                             v
+-----------------------------------------------------------+
|                   Werkzeugkasten                          |
|                                                           |
|   Modell         Daten          Oberflaechen              |
|   erkunden       lesen          verwalten                 |
|                                                           |
|   Konfigurations-  TL-Script     Modell                   |
|   schemas lesen    ausfuehren    erweitern                |
+----------------------------+------------------------------+
                             |
                             v
+-----------------------------------------------------------+
|               TopLogic-Anwendung                          |
|                                                           |
|   Datenmodell    Geschaeftsdaten    Oberflaechen          |
|   Konfiguration  Skripte           Benutzer               |
+-----------------------------------------------------------+
```


## Semantisches Gedaechtnis: Der Vectorindex

### Warum ein Vectorindex?

Die Werkzeuge geben dem Assistenten Zugriff auf die Anwendung. Aber
Zugriff allein reicht nicht -- der Assistent braucht auch Verstaendnis.
Wenn ein Anwender fragt "Wie richte ich eine Genehmigungsstufe ein?",
muss der Assistent nicht nur wissen, welche Werkzeuge er hat, sondern
auch, wie die Plattform funktioniert, wie das konkrete Datenmodell
aufgebaut ist und welche fachlichen Zusammenhaenge es gibt.

Dieses Verstaendnis entsteht durch einen Vectorindex: eine semantische
Wissensbasis, die Texte nicht nach Stichworten, sondern nach Bedeutung
durchsuchbar macht. Der Assistent findet relevante Informationen, auch
wenn die Formulierung nicht exakt uebereinstimmt -- "Lieferung" findet
auch "Versand", "Speditionsauftrag" und "Warenausgang".

Der Vectorindex wirkt auf drei Ebenen:

### Plattformverstaendnis

Fuer die Konfiguration einer spezifischen Plattform gibt es in den
Trainingsdaten der KI kaum Beispiele. Plattform-Dokumentation,
Best Practices, Konfigurationsbeispiele und Hilfetexte koennen als
Vectorindex bereitgestellt werden. Der Assistent findet so die
relevanten Dokumentationsstellen, bevor er ueberhaupt ein Werkzeug
aufruft.

*Beispiel: "Wie konfiguriere ich eine Tabelle mit gruppierten
Zeilen?" -- der Vectorindex findet die passende Dokumentation zur
Tabellenkomponente und ihren Gruppierungsoptionen, auch wenn der
Anwender nicht den exakten Konfigurationsnamen kennt.*

Das kompensiert gezielt die Luecke im Vorwissen der KI: Was nicht in
den Trainingsdaten steckt, steckt im Vectorindex.

### Modellverstaendnis

Die Werkzeuge liefern strukturierte Informationen ueber das
Datenmodell: Typen, Eigenschaften, Beziehungen. Aber ein Vectorindex
ueber Modellbeschreibungen, Labels und Annotationen ermoeglicht
zusaetzlich semantische Suche: "Ich brauche etwas, das mit
Rechnungsstellung zu tun hat" findet Typen wie "Rechnung",
"Rechnungsposition", "Fakturierung" und "Zahlungseingang" -- auch
wenn der Suchbegriff woertlich nirgends vorkommt.

In einer modellbasierten Plattform koennen die Embeddings mit
strukturellem Wissen angereichert werden: Typzugehoerigkeiten,
Beziehungen, Eigenschaftssemantik. Ein Kundendatensatz ist nicht
nur ein Textblock, sondern ein Objekt mit bekanntem Typ, bekannten
Beziehungen und bekannter Rolle im Geschaeftsprozess. Das Modell
*ist* der Kontext, den man bei reiner Textsuche muehsam
rekonstruieren muesste.

### Datenverstaendnis

Die wirkungsvollste Ebene betrifft die konkreten Geschaeftsdaten
einer Kundenanwendung. Wenn der Vectorindex nicht nur das Modell,
sondern auch die Inhalte der Anwendung erfasst, kann der Assistent
Fragen beantworten, die ueber strukturierte Abfragen hinausgehen:

- *Aehnlichkeitssuche*: "Finde Kunden mit aehnlichem Profil wie die
  Firma Mueller" -- basierend auf der Gesamtheit der Kundendaten,
  nicht auf einzelnen Feldern.

- *Dokumentensuche*: Viele Fachanwendungen verwalten Dokumente --
  Vertraege, Protokolle, Spezifikationen. Ein Vectorindex ueber den
  Dokumentinhalt macht sie durchsuchbar: "Gibt es in unseren
  Vertraegen Klauseln zur Haftungsbegrenzung?"

- *Muster erkennen*: "Welche Projekte hatten aehnliche Probleme wie
  Projekt X?" -- der Vectorindex findet Aehnlichkeiten ueber
  Freitextfelder, Kommentare und Beschreibungen, die eine
  strukturierte Abfrage nicht erfassen wuerde.

- *Wissen aus Gespraechen*: Vergangene Konversationen mit dem
  Assistenten koennen indiziert werden. So baut sich ueber die
  Zeit ein institutionelles Gedaechtnis auf: Was wurde schon
  gefragt? Welche Loesungen wurden gefunden? Welche
  Entscheidungen wurden getroffen?

### Zusammenspiel mit den Werkzeugen

Der Vectorindex ersetzt die Werkzeuge nicht -- er ergaenzt sie. Die
Werkzeuge liefern praezise, strukturierte Antworten: "Der Typ Kunde
hat die Eigenschaften Name, Adresse, Umsatz." Der Vectorindex
liefert Kontext und Hintergrund: "Kunden mit hohem Umsatz werden
im Quartalsbericht gesondert ausgewiesen, siehe Dokumentation zur
Key-Account-Verwaltung."

Zusammen ergibt das einen Assistenten, der nicht nur Zugriff auf die
Anwendung hat, sondern sie auch wirklich versteht -- die Plattform,
das Modell und die konkreten Daten.


## Warum eine No-Code-Plattform mit KI?

### Der Unterschied zur KI-gestuetzten Programmierung

Wenn KI heute in der Softwareentwicklung eingesetzt wird, generiert sie
meistens Quellcode: Java, TypeScript, SQL, HTML, CSS. Das funktioniert
erstaunlich gut -- aber der generierte Code muss kompiliert, getestet
und deployed werden, bevor er laeuft.

Bei einer modellbasierten Plattform wie TopLogic ist der Ansatz
grundsaetzlich anders: Die KI schreibt keinen Code, sondern
konfiguriert. Sie beschreibt *was* sie will, nicht *wie* es technisch
umgesetzt wird. Die Plattform kuemmert sich um das Wie.

Das ist ein feiner, aber wichtiger Unterschied. Die KI ersetzt nicht
den Entwickler, indem sie denselben Job macht (Code schreiben).
Stattdessen wird der Job selbst ueberfluessig, weil kein Code noetig
ist.

### Vorteile

**Kleinerer Fehlerraum**

Eine Konfiguration ist entweder gueltig oder nicht -- das Schema sagt
es sofort. Generierter Code kann syntaktisch korrekt, aber inhaltlich
falsch sein: Sicherheitsluecken, Timing-Probleme, falsche Datentypen.
Die Plattform verhindert ganze Fehlerklassen, die bei generiertem Code
auftreten koennen.

**Sofortige Wirkung**

Aenderungen an Konfiguration und Modell wirken sofort. Bei
herkoemmlicher Entwicklung muss generierter Code erst kompiliert,
getestet und in Betrieb genommen werden. Das dauert und ist eine
zusaetzliche Fehlerquelle.

**Garantierte Konsistenz**

Die Plattform sorgt dafuer, dass Datenmodell, Datenhaltung und
Oberflaeche zusammenpassen. Bei herkoemmlicher Entwicklung muss die KI
Datenbankschema, Schnittstellen, Oberflaeche und Tests konsistent
halten -- eine Aufgabe, an der auch erfahrene Entwickler regelmaessig
scheitern.

**Weniger Halluzination**

Die KI arbeitet mit konkreten Schemas und bekommt strukturierte
Antworten. Sie erfindet keine Schnittstellen, sondern nutzt
vorhandene. Bei der Code-Generierung erfindet die KI regelmaessig
Funktionen und Bibliotheken, die es gar nicht gibt.

**Nachvollziehbarkeit**

Konfigurationsaenderungen sind deklarativ: Man sieht, was eingestellt
ist, nicht wie es umgesetzt wurde. Generierter Code ist schwerer zu
pruefen -- war er vor der Aenderung korrekt? Was genau hat sich
geaendert? Bei Konfiguration ist das offensichtlich.

**Fachleute koennen mitreden**

Ein Fachanwender kann eine Modellbeschreibung lesen und sagen "Ja, das
stimmt" oder "Nein, da fehlt etwas". Generierten Programmcode kann er
nicht beurteilen. Die Plattform bringt die KI naeher an die
Fachsprache.

**Weniger Entscheidungen, weniger Fehler**

Statt tausend Zeilen Code zu generieren, trifft die KI zehn
Konfigurationsentscheidungen. Die Fehlerrate pro Entscheidung mag
aehnlich sein -- aber zehn statt tausend Entscheidungen ergeben in
Summe weniger Fehler.

### Grenzen

**Plattform-Decke**

Was die Plattform nicht kann, kann auch die KI nicht konfigurieren.
Herkoemmliche Entwicklung hat diese Grenze prinzipiell nicht. Sehr
spezifische Oberflaechen-Interaktionen oder Integrationen mit
Fremdsystemen, die die Plattform nicht vorsieht, erfordern weiterhin
klassische Entwicklung.

**Plattform-Wissen noetig**

Die KI muss die Konzepte und Eigenheiten der Plattform kennen. Bei
allgemeiner Programmierung reicht Wissen ueber gaengige Sprachen und
Frameworks, das in den Trainingsdaten der KI massiv vertreten ist.
Plattform-spezifisches Wissen ist seltener -- das wird ueber
Werkzeuge, Schemas und den Vectorindex kompensiert (siehe
*Semantisches Gedaechtnis*).

**Weniger Vorwissen**

Fuer gaengige Programmiersprachen und Frameworks gibt es Millionen von
Beispielen in den Trainingsdaten der KI. Fuer die Konfiguration einer
spezifischen Plattform praktisch keine. Die KI stuetzt sich auf
Werkzeuge, Schemas und den Vectorindex statt auf gelerntes Wissen.
Das macht die Qualitaet dieser Bausteine umso wichtiger.

**Grenzen der Konfigurationssprache**

Manche Anforderungen lassen sich in Programmcode natuerlicher
ausdruecken als in Konfiguration. "Sortiere die Liste nach einem
komplexen Score aus drei gewichteten Faktoren" ist in Code trivial, in
Konfiguration moeglicherweise umstaendlich. Die Skriptsprache der
Plattform kann vieles abfangen, aber nicht alles.


## Erweiterbarkeit

Die Werkzeuge des Assistenten koennen auf mehrere Arten erweitert
werden:

- **Neue Werkzeuge programmieren**: Entwickler koennen neue Werkzeuge
  in Java implementieren und registrieren. Ein neues Werkzeug steht
  automatisch beiden Assistentenformen zur Verfuegung.

- **Skript-basierte Datenquellen**: Neue Datenquellen koennen per
  TL-Script definiert werden -- ohne Programmierung. So koennen
  Konfiguratoren dem Assistenten Zugriff auf zusaetzliche Informationen
  geben.

- **Anpassung des Verhaltens**: Ueber Systemprompts und
  Werkzeugauswahl laesst sich steuern, welche Faehigkeiten der
  Assistent hat und wie er sich verhaelt.


## Sicherheit und Berechtigungen

### Grundprinzip

Der Assistent handelt immer *als* der angemeldete Nutzer. Er hat
exakt dieselben Rechte -- kann nichts sehen, was der Nutzer nicht
sehen duerfte, und nichts aendern, was der Nutzer nicht aendern
duerfte. Er ist wie ein Sachbearbeiter mit denselben Berechtigungen,
der aber effizienter arbeitet, weil er Abkuerzungen kennt.

Aenderungen am Datenmodell und an Daten erfolgen transaktional: Sie
werden entweder vollstaendig durchgefuehrt oder vollstaendig
zurueckgerollt. Ein fehlgeschlagener Vorgang hinterlaesst keine
inkonsistenten Daten.

### Berechtigungen auf zwei Ebenen

In einer klassischen TopLogic-Anwendung sind Berechtigungen stark an
die Oberflaeche gebunden: Ein Nutzer darf eine bestimmte Ansicht
sehen, ein bestimmtes Kommando ausfuehren, ein bestimmtes Feld in
einem bestimmten Formular bearbeiten. Das ergibt Sinn, weil die
Oberflaeche der einzige Zugang zur Anwendung ist.

Der KI-Assistent arbeitet aber nicht ueber die Oberflaeche. Er greift
direkt auf das Datenmodell und die Geschaeftsdaten zu. Wuerde man ihn
an die Oberflaechenberechtigungen binden, muesste er sich gedanklich
durch Ansichten und Kommandos hangeln -- und ein grosser Teil seines
Effizienzvorteils ginge verloren.

Die Loesung liegt darin, Berechtigungen auf zwei Ebenen zu betrachten:

**Daten-Ebene**: Wer darf welche Daten lesen und schreiben --
unabhaengig von der Oberflaeche? Das betrifft Typen, Instanzen und
Eigenschaften. Zum Beispiel: "Rolle X darf Kunden lesen aber nicht
aendern", "Rolle Y darf Auftraege anlegen."

**Oberflaechen-Ebene**: Welche Ansichten und Kommandos sind sichtbar?
Das ist eine *Projektion* der Daten-Ebene auf die Benutzeroberflaeche.
Sie bestimmt, *wie* ein Nutzer seine Rechte ausueben kann -- nicht
*welche* Rechte er hat.

Fuer die Oberflaeche reicht die Oberflaechenebene, weil sie die
Datenberechtigungen implizit abbildet: Wenn ein Nutzer den
"Loeschen"-Button nicht sieht, kann er nicht loeschen.

Fuer den KI-Assistenten braucht man die Daten-Ebene explizit. Er
prueft nicht "darf ich den Button klicken?", sondern "darf dieser
Nutzer Instanzen vom Typ Kunde aendern?" Das ist direkter, schneller
-- und genauso sicher.

### Was dabei gewonnen wird

Wenn die Daten-Ebene sauber existiert, gewinnen beide Seiten:

- **Der Assistent** arbeitet effizient, weil er direkt auf Daten
  zugreift und bei jedem Zugriff gegen die Datenberechtigungen
  geprueft wird. Kein Umweg ueber simulierte Oberflaecheninteraktionen.

- **Die Oberflaeche** wird einfacher, weil sie sich auf die
  Datenberechtigungen stuetzen kann, statt eigene Regeln zu pflegen.
  "Zeige den Loeschen-Button" wird abgeleitet aus "Nutzer darf diesen
  Typ loeschen", statt separat konfiguriert.

- **Die Konsistenz** steigt: Es gibt eine einzige Quelle der Wahrheit
  fuer Berechtigungen. Ob ein Zugriff ueber die Oberflaeche, den
  Assistenten oder eine Schnittstelle kommt -- dieselben Regeln gelten.

- **Zukuenftige Schnittstellen** profitieren ebenfalls. Jede
  Erweiterung -- REST-API, Batch-Jobs, Integrationen mit
  Fremdsystemen -- kann sich auf dieselbe Berechtigungsschicht
  stuetzen.


## Ausblick: Ueber die einzelne Anwendung hinaus

### Anwendungen, die sich selbst kennen

Alles bisher Beschriebene spielt sich innerhalb einer einzelnen
Anwendung ab. Aber in einem Unternehmen gibt es nicht eine Anwendung,
sondern viele -- und jede modellbasierte Anwendung hat eine
Eigenschaft, die herkoemmliche Software nicht hat: Sie kennt sich
selbst.

Eine TopLogic-Anwendung weiss, welche Datentypen sie verwaltet,
welche Beziehungen zwischen ihnen bestehen, welche
Geschaeftsprozesse sie abbildet und wie ihre Oberflaechen aufgebaut
sind. Dieses Wissen steckt nicht in einer externen Dokumentation, die
veralten kann, sondern im laufenden System selbst. Was der
KI-Assistent fuer einen einzelnen Anwender tut -- die Anwendung
erkunden und erklaeren -- kann er prinzipiell auch fuer andere
Systeme tun.

### Agenten als Schnittstelle

Heute werden Integrationen zwischen Anwendungen ueber
zweckgebundene Schnittstellen realisiert: Eine REST-API fuer
Kundendaten, ein Kafka-Topic fuer Auftragsereignisse, ein
Dateiexport fuer das Controlling. Jede Schnittstelle wird fuer
einen bestimmten Zweck gebaut und muss bei Aenderungen angepasst
werden.

Wenn eine Anwendung ihren Agenten auch nach aussen zugaenglich
macht, entsteht eine Schnittstelle anderer Art. Statt eines starren
Datenformats bietet sie einen Gespraechspartner an, der die
Anwendung versteht und Fragen beantworten kann:

- "Welche Auftragstypen gibt es in eurem System?"
- "Gibt es offene Auftraege fuer den Kunden mit der Nummer 4711?"
- "Wie ist der Genehmigungsprozess fuer Bestellungen aufgebaut?"

Das ersetzt nicht die klassischen Schnittstellen -- fuer hohe
Durchsaetze und Echtzeitverarbeitung bleibt eine dedizierte API
der richtige Weg. Aber fuer Erkundung, Abstimmung und
gelegentliche Abfragen bietet ein Agent etwas, das keine API kann:
Er versteht Kontext und kann erklaeren.

### Lebendige Unternehmensarchitektur

Wenn jede Anwendung im Unternehmen einen Agenten hat, der ihr
Modell und ihre Prozesse kennt, ergibt sich eine neue Moeglichkeit:
Ein uebergreifender Agent, der die Unternehmensarchitektur nicht
aus Dokumenten liest, sondern aus den laufenden Systemen.

Heute sieht Unternehmensarchitektur oft so aus: Es gibt
Architekturdiagramme, Schnittstellenverzeichnisse und
Prozessdokumentationen. Sie werden mit Aufwand erstellt und sind
am naechsten Tag schon nicht mehr ganz aktuell. Niemand hat ein
vollstaendiges, aktuelles Bild der Systemlandschaft.

Ein Architektur-Agent koennte die Agenten der einzelnen Anwendungen
befragen:

- "Welche Systeme verwalten Kundendaten? Wie sind die Datenmodelle
  aufgebaut? Wo gibt es Ueberschneidungen?"
- "Wenn wir den Typ 'Lieferant' im ERP aendern -- welche anderen
  Systeme sind betroffen?"
- "Zeig mir den kompletten Lebenszyklus eines Auftrags ueber alle
  beteiligten Systeme hinweg."

Die Antworten kommen nicht aus einer Dokumentation, sondern aus den
Systemen selbst -- und sind damit immer aktuell.

### Was das veraendert

**Integrationsplanung wird einfacher.** Statt Schnittstellendoku-
mentationen zu lesen und Ansprechpartner zu suchen, fragt ein Agent
die beteiligten Systeme direkt. "Welche Daten kann das CRM liefern?
In welchem Format? Wie oft aendern sie sich?"

**Auswirkungsanalysen werden moeglich.** Vor einer Aenderung kann
der Architektur-Agent pruefen, welche Systeme betroffen waeren. Das
passiert nicht auf Basis eines statischen Modells, sondern auf Basis
des tatsaechlichen Zustands.

**Redundanzen werden sichtbar.** Wenn mehrere Systeme aehnliche
Datenmodelle haben, kann der Agent das erkennen und aufzeigen. "Das
CRM und das ERP verwalten beide Kundenadressen, aber mit
unterschiedlichen Feldern. Hier ist der Vergleich." Besonders
wirkungsvoll wird das mit semantischer Suche ueber die Vectorindizes
der einzelnen Anwendungen: Der Agent findet Ueberschneidungen auch
dann, wenn die Systeme unterschiedliche Begriffe verwenden --
"Geschaeftspartner" im einen, "Mandant" im anderen.

**Dokumentation wird ueberfluessig -- oder erzeugt sich selbst.**
Wenn ein Agent die Architektur jederzeit live abfragen kann, braucht
es kein manuell gepflegtes Architekturverzeichnis mehr. Oder der
Agent erzeugt die Dokumentation auf Knopfdruck aus dem aktuellen
Stand -- immer aktuell, immer vollstaendig.
