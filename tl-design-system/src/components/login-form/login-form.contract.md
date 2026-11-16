# Vertrag: Anmeldeformular, Alternativen

Die Gruppe unter dem Hauptknopf eines Anmeldeformulars: „oder“ und die anderen Wege der Anmeldung (SSO-Anbieter,
Passkey), voll breit untereinander. Kein Control für sich: die View legt einen Stack mit dieser Klasse an und
füllt ihn. TopLogic garantiert, dass die Gruppe ohne Knopf samt „oder“ verschwindet, nicht das Aussehen der Knöpfe.

## Tokens

Keine eigenen. Abstände der Gruppe trägt der Stack, die Farbe des Textes „oder“ kommt aus dessen Textton (`tone`). Die Anmeldekarte (`tl-login-card`) liest `--tl-space-sm`, `--tl-space-lg`, `--tl-space-xl`.

## Attribute

| Attribut | Wann |
|---|---|
| `tl-login-alternatives` | am Stack, der „oder“ und die Alternativen hält; sitzt unter dem Hauptknopf im Formular |
| `tl-login-alternatives__or` | am Text „oder“ in der Gruppe; zentriert |
| `tl-login-card` | an der Karte (`<panel appearance="card" css-class="tl-login-card">`) der Login-Seite: Titel ohne Trennlinie, Rand `--tl-space-lg`/`--tl-space-xl`/`--tl-space-sm`, Feldabstand `--tl-space-lg`; nicht am Login-Dialog |

## Konformität

Eine Gruppe besteht den Vertrag, wenn sie ohne Knopf samt „oder“ verschwindet und „oder“ zentriert zeigt.

## Randfälle

- Ohne WebAuthn im Browser zeigt das Passkey-Element einen deaktivierten Knopf: die Gruppe bleibt, „oder“ bleibt
  (die Alternative gibt es, nur nicht hier).
- Eine SSO-Fehlermeldung ohne verfügbare Methode trägt einen Schließknopf: die Gruppe bleibt, „oder“ steht über der
  Meldung.

## Leser

Die Anmeldeformulare der Engine (`login-form.view.xml` in layout.view; das Passkey-Overlay hängt sich in die Gruppe).
