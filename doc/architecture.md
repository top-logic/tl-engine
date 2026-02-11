# TopLogic KI-Integration -- Architekturuebersicht


## Was ist das?

Die TopLogic KI-Integration macht eine TopLogic-Anwendung fuer
KI-Assistenten zugaenglich. Ein KI-Assistent kann die Anwendung
verstehen, mit ihr arbeiten und sie konfigurieren -- aehnlich wie ein
erfahrener Berater, der die Anwendung kennt.

Dabei gibt es zwei Einsatzformen:

- **Eingebauter Assistent**: Ein KI-Chat direkt in der Anwendung, ueber
  den Anwender Fragen stellen und Aufgaben ausfuehren lassen koennen.

- **Externer Assistent**: Ein eigenstaendiges KI-Werkzeug (z.B. Claude
  Desktop, Cursor), das sich von aussen mit der Anwendung verbindet und
  mit ihr arbeitet.

Beide Formen nutzen dieselben Faehigkeiten. Was der eingebaute
Assistent kann, kann auch der externe -- und umgekehrt.


## Warum?

TopLogic-Anwendungen sind hochgradig konfigurierbar: Datenmodelle,
Oberflaechen, Geschaeftslogik -- vieles laesst sich ohne Programmierung
anpassen. Diese Konfigurierbarkeit ist eine Staerke, erfordert aber
Fachwissen ueber die Plattform.

KI-Assistenten koennen diese Luecke schliessen:

- **Fuer Endanwender**: Fragen zur Anwendung stellen, Daten suchen und
  auswerten, Routineaufgaben automatisieren.

- **Fuer Konfiguratoren**: Datenmodelle erkunden und erweitern,
  Oberflaechen erstellen und anpassen, Konfigurationsaenderungen
  durchfuehren.

- **Fuer Entwickler**: Die Anwendungsstruktur verstehen, Aenderungen
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

### Was kann er?

Der Assistent verfuegt ueber eine Reihe von Werkzeugen, die ihm
Zugriff auf die Anwendung geben:

**Datenmodell erkunden**

Der Assistent kann das Datenmodell der Anwendung lesen und verstehen:
Welche Module gibt es? Welche Typen sind definiert? Welche
Eigenschaften und Beziehungen haben sie? Er kann auch nach
Modellelementen suchen und analysieren, wo ein bestimmter Typ
ueberall verwendet wird.

*Beispiel: "Welche Eigenschaften hat der Typ 'Kunde'?" oder
"Zeig mir alle Typen im Modul 'Vertrieb'."*

**Daten lesen**

Der Assistent kann Instanzen (also die eigentlichen Daten) der
Anwendung lesen: alle Kunden auflisten, einen bestimmten Datensatz
nachschlagen, Daten filtern und zusammenfassen.

*Beispiel: "Liste alle aktiven Projekte auf" oder
"Zeig mir die Details zum Kunden mit der Nummer 4711."*

**Oberflaechen verwalten**

Der Assistent kennt die verfuegbaren Oberflaechenvorlagen und kann
neue Ansichten erstellen. Dazu liest er das Schema einer Vorlage
(welche Parameter werden erwartet?) und erzeugt daraus eine neue
Oberflaeche mit den gewuenschten Einstellungen.

*Beispiel: "Erstelle eine Tabelle fuer alle offenen Auftraege" oder
"Welche Oberflaechenvorlagen gibt es?"*

**Konfigurationsschemas lesen**

Jede Konfigurationsmoeglichkeit in TopLogic hat ein Schema, das
beschreibt, welche Einstellungen moeglich sind. Der Assistent kann
diese Schemas lesen und so verstehen, wie eine bestimmte Komponente
konfiguriert werden kann.

*Beispiel: "Welche Einstellungen hat die Tabellenkomponente?" oder
"Was kann ich an einem Formularlayout alles konfigurieren?"*

**Skripte schreiben und ausfuehren**

TopLogic hat eine eigene Skriptsprache (TL-Script) fuer Berechnungen,
Abfragen und Automatisierungen. Der Assistent kann Skripte schreiben,
auf korrekte Syntax pruefen und ausfuehren. Er kennt auch die
verfuegbaren Funktionen und deren Dokumentation.

*Beispiel: "Berechne die Summe aller offenen Rechnungen" oder
"Schreibe ein Skript, das alle inaktiven Benutzer auflistet."*


## Der externe Assistent

### Wie funktioniert er?

Der externe Assistent ist ein eigenstaendiges KI-Werkzeug, das sich
ueber eine standardisierte Schnittstelle (MCP -- Model Context
Protocol) mit der laufenden TopLogic-Anwendung verbindet. MCP ist ein
offener Standard, der von verschiedenen KI-Werkzeugen unterstuetzt
wird.

Die Verbindung erfolgt ueber HTTP. Der externe Assistent hat dadurch
Zugriff auf dieselben Werkzeuge wie der eingebaute Assistent.
Zusaetzlich stehen ihm weitere Faehigkeiten zur Verfuegung:

**Datenmodell aendern**

Der externe Assistent kann das Datenmodell der Anwendung erweitern:
neue Module, Typen, Eigenschaften und Beziehungen anlegen und
bestehende aendern.

*Beispiel: "Lege einen neuen Typ 'Lieferant' mit den Eigenschaften
Name, Adresse und Bewertung an."*

**Ressourcen abrufen**

Neben den aktiven Werkzeugen bietet die Schnittstelle auch passive
Datenquellen, die der Assistent gezielt abfragen kann -- zum Beispiel
die Typen eines Moduls oder die Konfiguration einer Oberflaeche.


## Zusammenspiel beider Formen

Eingebauter und externer Assistent ergaenzen sich:

| Eigenschaft           | Eingebauter Assistent    | Externer Assistent       |
|-----------------------|--------------------------|--------------------------|
| Zugang                | Direkt in der Anwendung  | Eigenes KI-Werkzeug      |
| Zielgruppe            | Endanwender, Fachabteilung | Konfiguratoren, Entwickler |
| Staerke               | Schnelle Fragen und Aufgaben im Arbeitsalltag | Umfangreiche Konfigurations- und Modellierungsaufgaben |
| Kontext               | Sieht, was der Anwender sieht | Sieht die gesamte Anwendungsstruktur |
| Modell aendern        | Lesen                    | Lesen und Schreiben      |

Beide nutzen denselben Satz an Werkzeugen. Eine Erweiterung der
Werkzeuge kommt automatisch beiden Formen zugute.


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


## Sicherheit

Alle Aktionen des Assistenten laufen innerhalb der
TopLogic-Anwendung und unterliegen denselben Sicherheitsmechanismen
wie regulaere Anwenderaktionen. Der Assistent hat keinen
uneingeschraenkten Zugriff, sondern arbeitet im Rahmen der
konfigurierten Berechtigungen.

Aenderungen am Datenmodell und an Daten erfolgen transaktional: Sie
werden entweder vollstaendig durchgefuehrt oder vollstaendig
zurueckgerollt. Ein fehlgeschlagener Vorgang hinterlaesst keine
inkonsistenten Daten.
