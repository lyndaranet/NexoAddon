# Dash

> Schleudert den Spieler per Rechtsklick in Blickrichtung nach vorne – ein einfacher Sprint-Dash mit Cooldown, Partikeln und Sound.

| | |
|---|---|
| **Config-Key** | `Mechanics.dash` |
| **Gilt für** | Item (Waffe/Werkzeug) |
| **Listener-Klasse** | `Dash.DashListener` |
| **Toggle/Sneak-Verhalten** | Immer aktiv per Rechtsklick; optional nur nutzbar, während der Spieler schleicht (`require_sneaking: true`) |

## Was macht sie?

Rechtsklickt der Spieler (Luft oder Block) mit einem Dash-Item in der Haupthand, prüft die Mechanic zunächst `require_sneaking` und den Cooldown. Ist beides erfüllt, wird das Interact-Event abgebrochen, dem Spieler eine Geschwindigkeit in seine aktuelle Blickrichtung verpasst (horizontal skaliert mit `power`, vertikal zusätzlich um `vertical_boost` angehoben), ein konfigurierbarer Sound sowie ein Partikel-Burst inkl. kurzer Partikel-Spur (0,5 Sekunden) abgespielt und optional Haltbarkeit vom Item abgezogen. Anschließend startet der Cooldown; solange dieser läuft, erhält der Spieler stattdessen die konfigurierte Cooldown-Nachricht.

## Wann einsetzen?

- Mobilitäts-Waffen/-Werkzeuge im Shop (z. B. "Sprint-Schwert")
- PvP-Kits mit Ausweich-/Gap-Closer-Mechanik und Cooldown-Balancing
- Cosmetic-Items mit auffälligem Partikel-/Sound-Effekt beim Aktivieren

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.dash.power` | `double` | `2.0` | **(Pflicht, aktiviert die Mechanic)** Horizontale Geschwindigkeit des Dashs (Multiplikator auf die normalisierte Blickrichtung) |
| `Mechanics.dash.vertical_boost` | `double` | `0.5` | Zusätzlicher Y-Geschwindigkeitsanteil; positiv = nach oben, negativ = nach unten |
| `Mechanics.dash.cooldown` | `int` | `5` | Cooldown in Sekunden zwischen zwei Dashes (pro Spieler, nicht pro Item) |
| `Mechanics.dash.require_sneaking` | `bool` | `false` | Ob der Spieler beim Rechtsklick schleichen muss, damit der Dash auslöst |
| `Mechanics.dash.durability_cost` | `int` | `0` | Haltbarkeitskosten pro Dash. `0` = kein Haltbarkeitsverlust. Wirkt nur bei Items mit Haltbarkeit und nicht bei `unbreakable: true` |
| `Mechanics.dash.particle.type` | `String` (Bukkit `Particle`) | `"CLOUD"` | Partikeltyp des Dash-Effekts. Ungültiger Wert wird geloggt und ignoriert |
| `Mechanics.dash.particle.amount` | `int` | `20` | Anzahl der Partikel beim initialen Burst (die Partikel-Spur nutzt die Hälfte davon pro Tick) |
| `Mechanics.dash.sound.type` | `String` (Bukkit `Sound`) | `"ENTITY_ENDER_DRAGON_FLAP"` | Sound beim Dash. Ungültiger Wert wird geloggt und ignoriert |
| `Mechanics.dash.sound.volume` | `float` | `1.0` | Lautstärke des Dash-Sounds |
| `Mechanics.dash.sound.pitch` | `float` | `1.5` | Tonhöhe des Dash-Sounds |
| `Mechanics.dash.cooldown_message` | `String` (MiniMessage) | `"<red>Dash is on cooldown! Wait {time} seconds."` | Nachricht, wenn der Dash noch im Cooldown ist. `{time}` wird durch die verbleibenden Sekunden ersetzt |

## Beispiel

```yaml
jett_dash_sword:
  Mechanics:
    dash:
      power: 2.5
      vertical_boost: 0.3
      cooldown: 3
      require_sneaking: false
      durability_cost: 5
      particle:
        type: "SOUL_FIRE_FLAME"
        amount: 30
      sound:
        type: "ENTITY_PHANTOM_FLAP"
        volume: 1.5
        pitch: 1.8
      cooldown_message: "<red>⚡ Dash on cooldown! Wait <yellow>{time}s</yellow>"
```

## Hinweise & Besonderheiten

- **Wichtig:** `Mechanics.dash` ist im Loader (`ItemConfigUtil.java`) mit **zwei komplett unterschiedlichen Mechaniken** belegt, die sich gegenseitig ausschließen:
  - Diese hier – die einfache Sprung/Dash-Mechanic, erkennbar am Pflicht-Key `power` (`Dash.java`)
  - Die deutlich komplexere MMO-Ability-Variante mit Pflicht-Key `mode` (`DashMechanic.java`), dokumentiert in `dash_ability.md`
  
  Beide leben im selben YAML-Abschnitt `Mechanics.dash`, können aber **nicht gleichzeitig** in derselben Item-Konfiguration aktiv sein – je nachdem, welcher Pflicht-Key (`power` oder `mode`) vorhanden ist, wird nur eine der beiden Mechaniken geladen. Vor dem Konfigurieren prüfen, welche der beiden gemeint ist.
- Der Cooldown wird pro Spieler (nicht pro Item) in einer serverweiten Map verwaltet; alte Einträge werden automatisch nach einer Stunde Inaktivität bereinigt.
- Bricht das Item beim Haltbarkeitsverlust, wird der Item-Stack in der Haupthand entfernt und ein Zerbrechen-Sound abgespielt.
- Ungültige Partikel- oder Sound-Namen führen nicht zum Abbruch des Dashs, sondern werden nur mit einer Warnung im Server-Log übersprungen.
