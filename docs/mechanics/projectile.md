# Projectile

> Feuert ein simuliertes Projektil in Blickrichtung ab, das über Zeit fliegt – mit optionaler Schwerkraft, Homing, Abprallen, Durchdringung und Einschlagswirkung (inkl. Explosion).

| | |
|---|---|
| **Config-Key** | `Mechanics.projectile` |
| **Gilt für** | Item |
| **Listener-Klasse** | `ProjectileMechanic.ProjectileMechanicListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle; Auslösung per `trigger` (Rechts-/Linksklick, Shift-Rechtsklick oder `on_hit`) |

## Was macht sie?

Im Gegensatz zu `beam` (sofortiger Strahl) fliegt dieses Projektil tickweise über eine gemeinsame, serverweite Task (kein echtes Bukkit-Projectile-Entity). Es kann Schwerkraft (`gravity`) haben, an soliden Blöcken abprallen (`bounces`), mehrere Wesen durchdringen (`pierce`) und sich automatisch auf das nächste Wesen im Umkreis einlenken (`homing`). Beim Aufprall auf ein Wesen werden Schaden, Knockback, Effekte und Kommandos angewendet, beim Aufprall auf einen Block stattdessen `block_commands`. Optional löst der Einschlag zusätzlich eine Explosion (Radius, Schaden, Effekte, Partikel) aus. `max_active` begrenzt gleichzeitig fliegende Projektile pro Spieler – wird die Grenze überschritten, verschwindet das älteste sofort. Die Mechanic wird intern auch von `bow` (`special_shot: type: projectile`) wiederverwendet.

## Wann einsetzen?

- Zauberstäbe/Zauberbücher mit sichtbarem, physikalisch fliegendem Geschoss statt Instant-Hit
- Homing-Projektile, die Ziele automatisch verfolgen (Seelenpfeil)
- Feuerbälle mit Bogenwurf (Schwerkraft) und Flächenexplosion beim Einschlag

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.projectile.trigger` | `String` | `right_click` | `right_click`, `shift_right_click`, `left_click` oder `on_hit` |
| `Mechanics.projectile.cooldown` | `int` (Sekunden) | `0` | Cooldown pro Spieler |
| `Mechanics.projectile.range` | `double` | `20.0` | Maximale Flugdistanz vor dem Verschwinden |
| `Mechanics.projectile.speed` | `double` | `1.5` | Blöcke pro Tick |
| `Mechanics.projectile.hit_radius` | `double` | `0.5` | Suchradius für Entity-Kollision je Tick |
| `Mechanics.projectile.max_active` | `int` | `1` | Gleichzeitig fliegende Projektile pro Spieler |
| `Mechanics.projectile.gravity` | `double` | `0.0` | Abwärtsbeschleunigung pro Tick |
| `Mechanics.projectile.bounces` | `int` | `0` | Anzahl Abpraller an Blöcken vor dem finalen Einschlag |
| `Mechanics.projectile.pierce` | `int` | `0` | Zusätzliche Treffer nach dem ersten, bevor das Projektil verschwindet |
| `Mechanics.projectile.homing` / `homing_radius` / `homing_strength` | `bool`/`double` | `false` / `8.0` / `0.08` | Automatisches Einlenken auf das nächste Wesen im Radius |
| `Mechanics.projectile.damage` / `knockback` | `double` | `0.0` | Schaden bzw. Rückstoß bei Einschlag auf ein Wesen |
| `Mechanics.projectile.effects` | `List<AbilityEffect>` | `[]` | Potion-Effekte bei Treffer: `type`, `amplifier`, `duration` (Sek.), `chance` |
| `Mechanics.projectile.commands` / `block_commands` | `List<String>` | `[]` | Kommandos bei Entity- bzw. Block-Treffer (`{player}`/`{target}` bzw. `{player}`/`{x}`/`{y}`/`{z}`) |
| `Mechanics.projectile.explosion_radius` / `explosion_damage` / `explosion_effects` / `explosion_particle` | – | `0.0`/`0.0`/`[]`/– | Optionale Flächenexplosion am Einschlagspunkt |
| `Mechanics.projectile.conditions.require_sneaking` / `require_health_below` / `require_permission` | – | `false`/`100`/`""` | Nutzungsbedingungen |
| `Mechanics.projectile.trail` | `List<TrailEntry>` | `[]` | Partikel während des Flugs (jeden Tick): `particle`, `count`, `offset` |
| `Mechanics.projectile.impact_particles` | `List<ParticleEntry>` | `[]` | Partikel einmalig am Einschlagspunkt: `particle`, `count` |
| `Mechanics.projectile.sound_launch` / `sound_impact` | `String` | – | Sound beim Abfeuern bzw. beim Einschlag |

> Nur die Keys, die der Loader tatsächlich liest (`ItemConfigUtil.java`, `loadProjectileMechanic`/`parseProjectile`). Ohne `Mechanics.projectile`-Abschnitt lädt die Mechanic gar nicht.

## Beispiel

```yaml
FEUERKUGEL:
  displayname: "<red>Feuerkugel"
  Mechanics:
    projectile:
      trigger: right_click
      cooldown: 6
      range: 25
      speed: 1.2
      gravity: 0.04
      damage: 4.0
      explosion_radius: 3.0
      explosion_damage: 6.0
      explosion_effects:
        - type: FIRE_RESISTANCE
          amplifier: 0
          duration: 3
          chance: 1.0
      trail:
        - particle: FLAME
          count: 3
          offset: 0.05
        - particle: SMOKE_NORMAL
          count: 1
      impact_particles:
        - particle: FLAME
          count: 40
        - particle: EXPLOSION_NORMAL
          count: 10
      sound_launch: ENTITY_BLAZE_SHOOT
      sound_impact: ENTITY_GENERIC_EXPLODE
```

## Hinweise & Besonderheiten

- Alle aktiven Projektile laufen über eine gemeinsame serverweite Tick-Task, nicht über echte Bukkit-Projectile-Entities – performant auch bei vielen gleichzeitigen Nutzern.
- `pierce` zählt zusätzliche Treffer **nach** dem ersten (`pierce: 2` = trifft insgesamt bis zu 3 Wesen).
- `bounces` werden vor dem finalen Einschlag "verbraucht"; ist das Limit erreicht, löst der nächste Blocktreffer den Einschlag (inkl. Explosion) aus.
- Wird intern auch von der `bow`-Mechanic (`special_shot: type: projectile`) über `projectile_config` wiederverwendet – siehe `bow.md`.
