# Thor

> Schlägt per Klick einen oder mehrere Blitze an der Stelle ein, auf die der Spieler schaut (Raytrace), mit optionaler zufälliger Streuung. Gedacht für Donner-/Hammer-Items mit Cooldown.

| | |
|---|---|
| **Config-Key** | `Mechanics.thor` |
| **Gilt für** | Item |
| **Listener-Klasse** | `ThorMechanic.ThorMechanicListener` |
| **Toggle/Sneak-Verhalten** | Auslöser per `trigger` (`right_click`, `shift_right_click` oder `left_click`) |

## Was macht sie?

Beim konfigurierten Trigger (Standard: Rechtsklick) ermittelt die Mechanic per Raytrace aus den Augen des Spielers den ersten getroffenen Block innerhalb von `range`; wird nichts getroffen, wird der Endpunkt der Reichweite verwendet. An dieser Zielposition schlagen `lightning_bolts_amount` Blitze ein. Ist `random_location_variation` größer als `0`, wird jeder Blitz horizontal um einen zufälligen Betrag innerhalb dieses Radius (in Blöcken) versetzt, sodass die Einschläge streuen statt exakt übereinanderzuliegen. Standardmäßig werden echte Blitze (`strikeLightning`) erzeugt, die Schaden verursachen und Feuer entzünden können; mit `visual_only: true` werden nur optische Blitze ohne Schaden/Feuer gespawnt. Jeder Einsatz ist durch einen per-Spieler-`cooldown` (in Sekunden) begrenzt; ist er noch aktiv, erscheint eine Actionbar-Meldung mit der Restzeit.

## Wann einsetzen?

- Donner-/Blitz-Stäbe oder Thor-Hammer, die gezielt Blitze auf die anvisierte Stelle rufen
- Flächen-Angriffe durch mehrere gestreute Einschläge (`lightning_bolts_amount` + `random_location_variation`)
- Reine Show-/Effekt-Items ohne Schaden per `visual_only: true`

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.thor.trigger` | `String` | `right_click` | Auslöser: `right_click`, `shift_right_click` oder `left_click` |
| `Mechanics.thor.lightning_bolts_amount` | `int` | `1` | Anzahl der Blitze pro Einsatz (mind. `1`) |
| `Mechanics.thor.random_location_variation` | `double` (Blöcke) | `0.0` | Horizontaler Zufalls-Versatz jedes Blitzes; `0` = alle exakt auf dem Zielpunkt |
| `Mechanics.thor.range` | `double` | `40.0` | Reichweite des Raytrace zur Zielbestimmung |
| `Mechanics.thor.cooldown` | `Zeit` | `0` | Cooldown pro Spieler; blanke Zahl = Sekunden, oder mit Einheit `10s`, `5min`, `2h`, `500ms` |
| `Mechanics.thor.visual_only` | `bool` | `false` | `true` = nur optische Blitze ohne Schaden/Feuer |
| `Mechanics.thor.sound` | `String` | – | Optionaler Sound beim Einschlag, z. B. `ENTITY_LIGHTNING_BOLT_THUNDER` |

> Nur die Keys, die der Loader tatsächlich liest (`ItemConfigUtil.java`, `loadThorMechanic`). Ohne `Mechanics.thor`-Abschnitt lädt die Mechanic gar nicht.

## Beispiel

```yaml
THOR_HAMMER:
  displayname: "<yellow>Mjölnir"
  Mechanics:
    thor:
      trigger: right_click
      lightning_bolts_amount: 2
      random_location_variation: 2
      range: 40
      cooldown: 30
      visual_only: false
      sound: ENTITY_LIGHTNING_BOLT_THUNDER
```

## Hinweise & Besonderheiten

- Bei `visual_only: false` erzeugt jeder Blitz echten Blitzschaden **und kann Feuer entzünden** – für reinen Show-Effekt `visual_only: true` setzen.
- Das Ziel wird per Raytrace bestimmt: Der Blitz trifft den ersten festen Block in Blickrichtung; ohne Treffer wird der Endpunkt bei `range` verwendet (Blitz in der Luft).
- `random_location_variation` streut nur horizontal (X/Z); die Y-Höhe bleibt auf Zielhöhe.
- Der Cooldown ersetzt den früher üblichen `delay`-Wert. Eine blanke Zahl wird als **Sekunden** interpretiert; alternativ mit Einheit `500ms`, `10s`, `5min`, `2h`, `1d` – gilt für jede Mechanik mit `cooldown`.
- Bei aktivem Cooldown wird kein Blitz erzeugt, stattdessen erscheint eine Actionbar-Meldung mit der verbleibenden Restzeit.
