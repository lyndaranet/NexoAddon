# Beam

> Feuert einen sofortigen, sichtbaren Partikelstrahl in Blickrichtung, der Wesen entlang des Pfades trifft – optional durchdringend.

| | |
|---|---|
| **Config-Key** | `Mechanics.beam` |
| **Gilt für** | Item |
| **Listener-Klasse** | `BeamMechanic.BeamMechanicListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle; Auslösung per `trigger` (Rechts-/Linksklick, Shift-Rechtsklick oder `on_hit`) |

## Was macht sie?

Beim Auslösen marschiert die Mechanic sofort (kein Flugweg über Zeit) entlang der Blickrichtung bis `range`, prüft dabei in einer Bounding-Box (`width`/`height`) auf lebende Wesen und stoppt an soliden Blöcken, sofern `pierce_blocks` nicht gesetzt ist. Jedes getroffene Wesen wird genau einmal getroffen und erhält Schaden, Knockback, Effekte und Kommandos plus Partikel/Sound; ist `pierce` deaktiviert, stoppt der Strahl beim ersten Treffer. Vor dem Feuern werden `self_effects`/`self_damage` einmalig auf den Caster angewendet. `beam_segments` erlauben es, entlang des Strahlverlaufs (in Prozent der Reichweite) unterschiedliche Partikel darzustellen, z. B. für einen Farbverlauf. Bei `trigger: on_hit` startet der Strahl auf Augenhöhe des getroffenen Ziels, weiterhin in Blickrichtung des Casters.

## Wann einsetzen?

- Instant-Hit-Waffen (Laser, Peitschen), bei denen Trefferzeitpunkt und Sichtlinie exakt übereinstimmen sollen
- Durchdringende Strahlwaffen, die eine ganze Reihe von Gegnern auf einmal treffen
- Elementare Effektwaffen mit visuellem Farbverlauf entlang des Strahls (`beam_segments`)

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.beam.trigger` | `String` | `right_click` | `right_click`, `shift_right_click`, `left_click` oder `on_hit` |
| `Mechanics.beam.cooldown` | `int` (Sekunden) | `0` | Cooldown pro Spieler |
| `Mechanics.beam.range` | `double` | `10.0` | Maximale Reichweite |
| `Mechanics.beam.width` / `height` | `double` | `0.4` | Breite/Höhe der Trefferbox entlang des Strahls |
| `Mechanics.beam.pierce` | `bool` | `true` | Mehrere Wesen treffen (statt beim ersten zu stoppen) |
| `Mechanics.beam.pierce_blocks` | `bool` | `false` | Durch solide Blöcke hindurch weiterlaufen |
| `Mechanics.beam.damage` / `knockback` | `double` | `0.0` | Schaden bzw. Rückstoß pro getroffenem Wesen |
| `Mechanics.beam.effects` | `List<AbilityEffect>` | `[]` | Potion-Effekte pro Treffer: `type`, `amplifier`, `duration` (Sek.), `chance` |
| `Mechanics.beam.commands` | `List<String>` | `[]` | Konsolenkommandos, Platzhalter `{player}`/`{target}` |
| `Mechanics.beam.self_effects` / `self_damage` | `double`/`List` | `0.0`/`[]` | Wirkung auf den Caster, einmalig pro Auslösung |
| `Mechanics.beam.conditions.require_sneaking` | `bool` | `false` | Nur nutzbar während des Sneakens |
| `Mechanics.beam.conditions.require_health_below` | `int` (%) | `100` | Nur nutzbar unter X % Leben |
| `Mechanics.beam.conditions.require_permission` | `String` | `""` | Erforderliche Permission |
| `Mechanics.beam.beam_segments` | `List<BeamSegment>` | Ein CRIT-Segment über 0–100 % | Partikel je Abschnitt des Strahls: `from_pct`, `to_pct`, `particle`, `count` |
| `Mechanics.beam.hit_particle` | `String` | – | Partikel an jedem getroffenen Wesen |
| `Mechanics.beam.sound` / `sound_hit` | `String` | – | Sound beim Abfeuern bzw. bei jedem Treffer |

> Nur die Keys, die der Loader tatsächlich liest (`ItemConfigUtil.java`, `loadBeamMechanic`). Ohne `Mechanics.beam`-Abschnitt lädt die Mechanic gar nicht.

## Beispiel

```yaml
FLAMMENSTRAHL:
  displayname: "<red>Flammenstrahl"
  Mechanics:
    beam:
      trigger: shift_right_click
      cooldown: 10
      range: 12
      width: 0.6
      pierce: false
      pierce_blocks: true
      damage: 4.0
      effects:
        - type: FIRE_RESISTANCE
          amplifier: 0
          duration: 3
          chance: 1.0
        - type: SLOW
          amplifier: 1
          duration: 2
          chance: 0.75
      beam_segments:
        - from_pct: 0
          to_pct: 60
          particle: FLAME
          count: 2
        - from_pct: 60
          to_pct: 100
          particle: SMOKE_NORMAL
          count: 1
      hit_particle: LAVA
      sound: ITEM_FLINTANDSTEEL_USE
      sound_hit: ENTITY_BLAZE_HURT
```

## Hinweise & Besonderheiten

- Der Beam ist ein sofortiger Strahl, kein fliegendes Projektil über Zeit (siehe dafür `projectile.md`) – die gesamte Reichweite wird in einem Tick abgearbeitet.
- Jedes Wesen wird pro Auslösung nur einmal getroffen, auch wenn `pierce: true` gesetzt ist.
- `beam_segments` sind rein visuell und beeinflussen weder Trefferberechnung noch Schaden.
- Ohne definierte `beam_segments` wird automatisch ein einzelnes CRIT-Partikel über die volle Länge angezeigt.
