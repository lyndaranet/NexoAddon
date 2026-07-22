# Magnet

> Zieht lose herumliegende Items automatisch zum Spieler, solange sich ein Magnet-Item irgendwo im Inventar befindet.

| | |
|---|---|
| **Config-Key** | `Mechanics.magnet` |
| **Gilt für** | Item (beliebiger Inventarplatz – ein aktiver Magnet pro Spieler) |
| **Listener-Klasse** | `Magnet.MagnetListener` |
| **Toggle/Sneak-Verhalten** | Rechtsklick (Luft oder Block) mit dem Item in der Hand schaltet AN/AUS um; Status wird im Item (PersistentDataContainer) und in der Lore gespeichert |

## Was macht sie?

Ein serverweiter Task läuft alle 2 Ticks über alle Onlinespieler und sucht in deren gesamtem Inventar nach dem ersten Item mit aktivierter Magnet-Mechanic. Ist einer aktiv, werden alle fallengelassenen Item-Entities im `radius` per gesetzter Velocity zum Spieler gezogen (ab einer Mindestdistanz von 0.5 Blöcken), optional begleitet von Partikeln pro gezogenem Item und einem Sound. Rechtsklick mit dem Item in der Hand schaltet den Aktiv-Status um und aktualisiert die passende Lore-Zeile (`active_lore`/`inactive_lore`). Optional lässt sich der Magnet über PlotSquared auf eigene/vertraute Plots beschränken oder umgekehrt nur außerhalb von Plots erlauben.

## Wann einsetzen?

- Sammel-Amulette/Ringe im Shop, die das Aufsammeln von Drops beschleunigen
- AFK-Farm-Items
- Serverbalance über `plot_only`/`wilderness_only`, um den Effekt auf bestimmte Bereiche zu begrenzen

## Konfiguration

> Schon ein leerer `Mechanics.magnet:`-Block aktiviert die Mechanic mit allen Defaults – es gibt keinen einzelnen Pflicht-Key.

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.magnet.enabled` | `bool` | `true` | Schaltet die Mechanic für dieses Item grundsätzlich ein/aus |
| `Mechanics.magnet.radius` | `int` | `5` | Radius in Blöcken, in dem Items angezogen werden |
| `Mechanics.magnet.pull_speed` | `double` | `0.3` | Zuggeschwindigkeit, alle 2 Ticks angewendet |
| `Mechanics.magnet.particle.type` | `String` | `""` (deaktiviert) | Partikel-Typ, der an jedem gezogenen Item erscheint |
| `Mechanics.magnet.particle.amount` | `int` | `3` | Partikelanzahl pro Item pro Durchlauf |
| `Mechanics.magnet.sound.type` | `String` | `""` (deaktiviert) | Sound, der abgespielt wird, sobald mindestens ein Item gezogen wurde |
| `Mechanics.magnet.sound.volume` | `float` | `0.3` | Lautstärke |
| `Mechanics.magnet.sound.pitch` | `float` | `1.5` | Tonhöhe |
| `Mechanics.magnet.active_lore` | `String` | `"<green>Magnet: AN"` | Lore-Zeile im aktiven Zustand (MiniMessage) |
| `Mechanics.magnet.inactive_lore` | `String` | `"<red>Magnet: AUS"` | Lore-Zeile im inaktiven Zustand (MiniMessage) |
| `Mechanics.magnet.plot_only` | `bool` | `false` | Magnet wirkt nur auf PlotSquared-Plots, die dem Spieler gehören |
| `Mechanics.magnet.trusted_plots` | `bool` | `false` | Falls `plot_only: true` – zusätzlich auf Plots wirksam, auf denen der Spieler als Trusted eingetragen ist |
| `Mechanics.magnet.wilderness_only` | `bool` | `false` | Magnet wirkt nur außerhalb von PlotSquared-Plots |

## Beispiel

```yaml
magnet_ring:
  lore:
    - "<green>⚡ Magnet: <bold>AN</bold>"
  Mechanics:
    magnet:
      enabled: true
      radius: 8
      pull_speed: 0.4
      active_lore: "<green>⚡ Magnet: <bold>AN</bold>"
      inactive_lore: "<red>⚡ Magnet: <bold>AUS</bold>"
      particle:
        type: "END_ROD"
        amount: 2
      sound:
        type: "BLOCK_AMETHYST_BLOCK_HIT"
        volume: 0.2
        pitch: 2.0
      plot_only: false
      trusted_plots: false
      wilderness_only: false
```

## Hinweise & Besonderheiten

- Pro Spieler wird nur ein aktiver Magnet berücksichtigt (das erste passende, aktive Item im Inventar) – mehrere gleichzeitig getragene Magnet-Items stapeln sich nicht.
- Der Task läuft alle 2 Ticks über alle Onlinespieler; ein sehr großer `radius` auf vielen gleichzeitig aktiven Magneten kann Performance kosten.
- `plot_only`/`wilderness_only` benötigen PlotSquared. Ist das Plugin nicht installiert, greift `plot_only` faktisch nie (kein Plot gefunden) und `wilderness_only` wirkt dann überall.
- Der Toggle-Status wird direkt im Item (PersistentDataContainer) gespeichert, gilt also pro Item-Stack, nicht global pro Spieler.
