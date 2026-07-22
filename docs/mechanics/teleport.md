# Teleport

> Teleportiert den Spieler auf eine von mehreren Arten (Blickrichtung, zum Ziel, aufwärts, zufällig) mit Partikel-/Sound-Feedback und optionaler Ankunfts-Wirkung.

| | |
|---|---|
| **Config-Key** | `Mechanics.teleport` |
| **Gilt für** | Item |
| **Listener-Klasse** | `TeleportMechanic.TeleportMechanicListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle; Auslösung per `trigger` (Rechtsklick, Shift-Rechtsklick oder `on_hit`) |

## Was macht sie?

Beim Auslösen berechnet die Mechanic ein Ziel gemäß `mode`: `look_direction` marschiert entlang der Blickrichtung bis zur Wand und sucht darunter einen sicheren Stand, `to_surface` stoppt direkt auf der ersten getroffenen Fläche, `to_target` teleportiert neben oder (mit `behind_target`) hinter das anvisierte Lebewesen, `upward` sucht die höchste freie Stelle senkrecht über dem Spieler, `random` würfelt einen sicheren Punkt im Umkreis. Vor und nach dem Sprung werden Partikel/Sounds an Ursprungs- und Zielort gespielt, optional eine Partikelspur dazwischen gezogen. Nach der Ankunft werden Effekte auf den Spieler angewendet, optional Schaden im Umkreis um den Zielpunkt verursacht und/oder eine Launch-Velocity in Blickrichtung gesetzt. Findet sich kein sicherer Zielpunkt, passiert nichts (Fehlermeldung, kein Cooldown-Verbrauch).

## Wann einsetzen?

- Klassische Blink-/Teleportstäbe für Mobilität
- Assassinen-Waffen, die bei einem Treffer hinter das Ziel springen (Backstab-Setup)
- Fluchtitems mit zufälligem Kurzstreckenteleport oder vertikalem "Sprung-Hammer" mit Einschlagschaden

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.teleport.trigger` | `String` | `right_click` | `right_click`, `shift_right_click` oder `on_hit` |
| `Mechanics.teleport.mode` | `String` | `look_direction` | `look_direction`, `to_surface`, `to_target`, `upward`, `random` |
| `Mechanics.teleport.cooldown` | `int` (Sekunden) | `0` | Cooldown pro Spieler |
| `Mechanics.teleport.distance` | `double` | `15.0` | Max. Blink-Distanz bzw. Suchradius (je nach Modus) |
| `Mechanics.teleport.behind_target` | `bool` | `false` | Nur `to_target`: hinter (statt neben) das Ziel teleportieren |
| `Mechanics.teleport.arrive_damage_radius` / `arrive_damage` | `double` | `0.0` | Flächenschaden um den Zielpunkt (trifft nicht den Caster selbst) |
| `Mechanics.teleport.launch_velocity` | `double` | `0.0` | Schub in Blickrichtung direkt nach der Ankunft |
| `Mechanics.teleport.effects` | `List<AbilityEffect>` | `[]` | Potion-Effekte auf den Spieler nach Ankunft: `type`, `amplifier`, `duration` (Sek.) |
| `Mechanics.teleport.commands` | `List<String>` | `[]` | Konsolenkommandos, Platzhalter `{player}` |
| `Mechanics.teleport.conditions.require_sneaking` | `bool` | `false` | Nur nutzbar während des Sneakens |
| `Mechanics.teleport.conditions.require_health_below` | `int` (%) | `100` | Nur nutzbar unter X % Leben |
| `Mechanics.teleport.conditions.require_permission` | `String` | `""` | Erforderliche Permission |
| `Mechanics.teleport.conditions.require_line_of_sight` | `bool` | `false` | Nur `to_target`: Sichtlinie zum Ziel erforderlich |
| `Mechanics.teleport.origin_particles` / `destination_particles` | `List<ParticleEntry>` | `[]` | Partikel (`particle`, `count`) am Ursprungs- bzw. Zielort |
| `Mechanics.teleport.trail_particle` | `String` | – | Partikelspur zwischen Ursprung und Ziel (nur gleiche Welt) |
| `Mechanics.teleport.sound_origin` / `sound_destination` | `String` | – | Sound am Ursprungs- bzw. Zielort |

> Nur die Keys, die der Loader tatsächlich liest (`ItemConfigUtil.java`, `loadTeleportMechanic`). Ohne `Mechanics.teleport`-Abschnitt lädt die Mechanic gar nicht.

## Beispiel

```yaml
ASSASSIN_BLADE:
  displayname: "<dark_gray>Assassinen-Klinge"
  Mechanics:
    teleport:
      trigger: on_hit
      mode: to_target
      behind_target: true
      cooldown: 8
      distance: 10
      effects:
        - type: INVISIBILITY
          amplifier: 0
          duration: 2
        - type: SPEED
          amplifier: 1
          duration: 3
      conditions:
        require_line_of_sight: true
      origin_particles:
        - particle: SMOKE_NORMAL
          count: 20
      destination_particles:
        - particle: SMOKE_NORMAL
          count: 20
      trail_particle: SMOKE_NORMAL
      sound_origin: ENTITY_ENDERMAN_TELEPORT
      sound_destination: ITEM_ARMOR_EQUIP_LEATHER
```

## Hinweise & Besonderheiten

- Findet die Mechanic keinen sicheren Zielpunkt (z. B. `look_direction` über einem Abgrund ohne Boden), teleportiert sie nicht und verbraucht keinen Cooldown.
- `to_target` benötigt ein anvisiertes Lebewesen innerhalb `distance` Blöcken; ohne Ziel (oder ohne Sichtlinie bei `require_line_of_sight`) passiert nichts.
- `arrive_damage` trifft nie den Caster selbst und respektiert Schutz-Plugins über `ProtectionLib`.
- `trail_particle` wird nur gezeichnet, wenn Ursprung und Ziel in derselben Welt liegen.
