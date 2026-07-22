# Bow

> Erweitert einen Bogen um zwei unabhängige Teile: passive Pfeil-Modifikationen für jeden Schuss und eine separate Shift-Rechtsklick-Spezialfähigkeit.

| | |
|---|---|
| **Config-Key** | `Mechanics.bow` |
| **Gilt für** | Item (Bogen) |
| **Listener-Klasse** | `BowMechanic.BowMechanicListener` |
| **Toggle/Sneak-Verhalten** | `arrow_passive` wirkt automatisch bei jedem Schuss; `special_shot` ist fest an Shift-Rechtsklick gebunden |

## Was macht sie?

Zwei komplett unabhängige, optionale Config-Blöcke:

- **`arrow_passive`** modifiziert jeden aus dem Bogen abgefeuerten Pfeil (Feuer, Schadens-/Geschwindigkeitsmultiplikator, Knockback, Durchdringung, kritisch, glühend) und löst bei Pfeiltreffer zusätzlichen Bonus-Schaden, Effekte, Feuer, Explosion und Kommandos aus, inklusive eigener Abschuss-/Treffer-Partikel und -Sounds.
- **`special_shot`** ist eine eigenständige Shift-Rechtsklick-Fähigkeit mit eigenem Cooldown, die statt eines normalen Pfeils einen von fünf Spezial-Typen abfeuert: `fireball` (echte Fireball-Entity), `lightning` (Blitzschlag per Ray-Trace + Flächenschaden), `cluster` (mehrere gestreute Pfeile), `explosive_arrow` (ein Pfeil, der beim Einschlag explodiert) oder `projectile` (delegiert komplett an eine eingebettete `projectile`-Konfiguration).

## Wann einsetzen?

- Elementarbögen, deren Pfeile automatisch Feuer/Wither/Gift verursachen (Höllenbogen, Seelenbogen)
- Bögen mit mächtiger, cooldown-basierter Spezialfähigkeit (Feuerball, Blitz, Explosivpfeil)
- Multi-Schuss-Bögen (Streuschuss/Cluster) als Ultimate-artige Fähigkeit

## Konfiguration

**`arrow_passive` – wirkt auf jeden normalen Pfeil:**

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.bow.arrow_passive.fire_ticks` | `int` | `0` | Entzündet den Pfeil (Trail) |
| `Mechanics.bow.arrow_passive.damage_multiplier` / `velocity_multiplier` | `double` | `1.0` | Multiplikator auf Pfeilschaden bzw. -geschwindigkeit |
| `Mechanics.bow.arrow_passive.knockback` / `piercing` | `int` | `0` | Zusätzlicher Knockback- bzw. Durchdringungslevel |
| `Mechanics.bow.arrow_passive.critical` / `glowing` | `bool` | `false` | Pfeil immer kritisch bzw. leuchtend |
| `Mechanics.bow.arrow_passive.hit_effects` | `List<AbilityEffect>` | `[]` | Effekte bei Treffer: `type`, `amplifier`, `duration` (Sek.), `chance` |
| `Mechanics.bow.arrow_passive.hit_damage_bonus` / `hit_fire_duration` | `double`/`int` | `0.0`/`0` | Bonusschaden bzw. Brenndauer (Sek.) bei Treffer |
| `Mechanics.bow.arrow_passive.hit_explosion_radius` / `hit_explosion_damage` | `double` | `0.0` | Explosion am Treffer-/Einschlagpunkt |
| `Mechanics.bow.arrow_passive.hit_commands` | `List<String>` | `[]` | Kommandos bei Treffer, `{player}`/`{target}` |
| `Mechanics.bow.arrow_passive.hit_particles` / `launch_particles` | `List<TrailEntry>` | `[]` | Partikel bei Treffer bzw. beim Abschuss |
| `Mechanics.bow.arrow_passive.hit_sound` / `launch_sound` | `String` | – | Sound bei Treffer bzw. beim Abschuss |

**`special_shot` – separate Shift-Rechtsklick-Fähigkeit:**

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.bow.special_shot.cooldown` | `int` (Sekunden) | `0` | Cooldown der Spezialfähigkeit |
| `Mechanics.bow.special_shot.type` | `String` | `fireball` | `fireball`, `lightning`, `cluster`, `explosive_arrow`, `projectile` |
| `Mechanics.bow.special_shot.yield` | `double` | `2.0` | Fireball-Explosionsstärke bzw. Explosivpfeil-Radius |
| `Mechanics.bow.special_shot.incendiary` | `bool` | `false` | Fireball entzündet beim Einschlag |
| `Mechanics.bow.special_shot.speed` | `double` | `1.5` | Geschwindigkeit von Fireball/Cluster-Pfeilen |
| `Mechanics.bow.special_shot.range` | `double` | `30.0` | Ray-Trace-Reichweite für `lightning` |
| `Mechanics.bow.special_shot.damage_lightning` | `double` | `1.0` | Multiplikator auf die Basis-Blitzschaden (5.0) |
| `Mechanics.bow.special_shot.count` / `spread_angle` | `int`/`double` | `5`/`15.0` | Anzahl bzw. Streuung der `cluster`-Pfeile |
| `Mechanics.bow.special_shot.projectile_config` | Verschachtelter `projectile`-Block | – | Nur bei `type: projectile`; identische Keys wie in `projectile.md` |
| `Mechanics.bow.special_shot.conditions.require_permission` / `require_arrows` / `require_health_below` | – | `""`/`false`/`100` | Nutzungsbedingungen |
| `Mechanics.bow.special_shot.special_launch_particles` | `List<TrailEntry>` | `[]` | Partikel beim Auslösen der Spezialfähigkeit |
| `Mechanics.bow.special_shot.special_launch_sound` | `String` | – | Sound beim Auslösen |
| `Mechanics.bow.special_shot.special_cooldown_message` | `String` | `<red>Noch <bold>{remaining}s</bold> Cooldown!` | Actionbar-Text bei aktivem Cooldown |

> Nur die Keys, die der Loader tatsächlich liest (`ItemConfigUtil.java`, `loadBowMechanic`). Beide Blöcke sind optional – Bögen benötigen mindestens einen der beiden, um die Mechanic zu laden.

## Beispiel

```yaml
HOELLENBOGEN:
  displayname: "<red>Höllenbogen"
  Mechanics:
    bow:
      arrow_passive:
        fire_ticks: 100
        damage_multiplier: 1.2
        hit_fire_duration: 3
        hit_effects:
          - type: SLOW
            amplifier: 0
            duration: 2
        hit_particles:
          - particle: FLAME
            count: 8
            offset: 0.1
        launch_particles:
          - particle: FLAME
            count: 10
            offset: 0.15
      special_shot:
        cooldown: 12
        type: fireball
        yield: 2.5
        incendiary: true
        speed: 1.5
        special_launch_particles:
          - particle: FLAME
            count: 25
            offset: 0.2
          - particle: SMOKE_LARGE
            count: 12
        special_launch_sound: ENTITY_BLAZE_SHOOT
```

## Hinweise & Besonderheiten

- `arrow_passive` und `special_shot` sind vollständig unabhängig – ein Bogen kann nur den einen, nur den anderen, oder beide gleichzeitig verwenden.
- `special_shot` ist fest an Shift-Rechtsklick gebunden (kein konfigurierbares `trigger`-Feld) und cancelt bei erfolgreichem Auslösen das normale Pfeilspannen.
- `type: projectile` delegiert vollständig an die `projectile`-Mechanic über `projectile_config` – alle dortigen Keys gelten 1:1 (siehe `projectile.md`).
- Sind sowohl `arrow_passive.hit_explosion_radius` als auch `special_shot.yield` (bei `explosive_arrow`) gesetzt, hat der `arrow_passive`-Wert Vorrang für Radius und Schaden der Pfeilexplosion.
