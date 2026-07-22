# Area Ability

> Trifft alle lebenden Wesen in einem Radius um den Auslöser mit Heilung, Schaden, Effekten, Launch und Kommandos – plus optionale Selbst-Wirkung auf den Caster.

| | |
|---|---|
| **Config-Key** | `Mechanics.area_ability` |
| **Gilt für** | Item |
| **Listener-Klasse** | `AreaAbilityMechanic.AreaAbilityMechanicListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle; Auslösung per `trigger` (Rechts-/Linksklick, Shift-Rechtsklick oder `on_hit`) |

## Was macht sie?

Beim Auslösen (Klick oder Treffer, je nach `trigger`) sammelt die Mechanic alle lebenden Wesen im `radius` um den Auslösepunkt, filtert sie nach `targets` (`players`, `allies`, `enemies`, `all`; Team-Zugehörigkeit über das Scoreboard), sortiert nach Entfernung und begrenzt optional auf `max_targets`. Jedes Ziel erhält Heilung/Schaden/Effekte/Launch-Velocity/Kommandos plus Partikel/Sound; danach wirken `self_heal`/`self_damage`/`self_effects` auf den Caster selbst. Zusätzlich kann eine expandierende Partikelwelle (`wave_particle`) abgespielt werden. Bei `trigger: on_hit` liegt der Mittelpunkt auf dem getroffenen Ziel, nicht auf dem Caster.

## Wann einsetzen?

- Heil-/Buff-Items für Support-Klassen (z. B. Paladin-Schild, das Verbündete in Reichweite heilt)
- Flächenschaden-Waffen, die bei jedem Treffer alle Gegner in der Nähe zusätzlich treffen (Sturmhammer)
- Debuff-Stäbe, die alle Wesen in Reichweite vergiften und dabei den Caster selbst schwächen

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.area_ability.trigger` | `String` | `right_click` | `right_click`, `shift_right_click`, `left_click` oder `on_hit` |
| `Mechanics.area_ability.cooldown` | `int` (Sekunden) | `0` | Cooldown pro Spieler |
| `Mechanics.area_ability.radius` | `double` | `5.0` | Suchradius um den Auslösepunkt |
| `Mechanics.area_ability.targets` | `String` | `players` | `players`, `allies` (gleiches Scoreboard-Team), `enemies`, `all` |
| `Mechanics.area_ability.include_self` | `bool` | `true` | Ob der Caster selbst als Ziel zählt |
| `Mechanics.area_ability.max_targets` | `int` | `0` (unbegrenzt) | Begrenzt die Anzahl getroffener Ziele (nächste zuerst) |
| `Mechanics.area_ability.heal_amount` / `damage_amount` | `double` | `0.0` | Heilung bzw. Schaden pro Ziel |
| `Mechanics.area_ability.launch_velocity` | `double` | `0.0` | Schubkraft von Caster weg für jedes Ziel |
| `Mechanics.area_ability.effects` | `List<AbilityEffect>` | `[]` | Potion-Effekte pro Ziel: `type`, `amplifier`, `duration` (Sek.), `chance` (0–1) |
| `Mechanics.area_ability.commands` | `List<String>` | `[]` | Konsolenkommandos pro Ziel, Platzhalter `{player}`/`{target}` |
| `Mechanics.area_ability.self_heal` / `self_damage` | `double` | `0.0` | Wirkung auf den Caster selbst |
| `Mechanics.area_ability.self_effects` | `List<AbilityEffect>` | `[]` | Effekte nur auf den Caster |
| `Mechanics.area_ability.conditions.require_sneaking` | `bool` | `false` | Nur nutzbar während des Sneakens |
| `Mechanics.area_ability.conditions.require_health_below` | `int` (%) | `100` | Nur nutzbar unter X % Leben |
| `Mechanics.area_ability.conditions.require_permission` | `String` | `""` | Erforderliche Permission |
| `Mechanics.area_ability.conditions.min_targets_required` | `int` | `0` | Mindestanzahl gefundener Ziele, sonst verpufft die Wirkung ohne Cooldown |
| `Mechanics.area_ability.particle` | `String` | – | Partikel an jedem getroffenen Ziel |
| `Mechanics.area_ability.wave_particle` | `String` | – | Optionale expandierende Partikelwelle vom Auslösepunkt |
| `Mechanics.area_ability.sound` / `sound_target` | `String` | – | Sound am Caster bzw. an jedem Ziel |

> Nur die Keys, die der Loader tatsächlich liest (`ItemConfigUtil.java`, `loadAreaAbilityMechanic`). Ohne `Mechanics.area_ability`-Abschnitt lädt die Mechanic gar nicht.

## Beispiel

```yaml
PALADIN_SHIELD:
  displayname: "<yellow>Paladin-Schild"
  Mechanics:
    area_ability:
      trigger: shift_right_click
      cooldown: 15
      radius: 6.0
      targets: allies
      include_self: false
      max_targets: 5
      heal_amount: 6.0
      effects:
        - type: ABSORPTION
          amplifier: 0
          duration: 8
      self_effects:
        - type: SLOW
          amplifier: 1
          duration: 4
      conditions:
        min_targets_required: 1
      particle: HEART
      sound: BLOCK_BEACON_ACTIVATE
```

## Hinweise & Besonderheiten

- Bei `trigger: on_hit` liegt der Mittelpunkt des Radius auf dem getroffenen Ziel, nicht auf dem Caster; ein Re-Entrancy-Schutz verhindert, dass eigene Flächenschäden die Ability erneut auslösen.
- `min_targets_required` lässt die Ability bei zu wenigen gültigen Zielen still verpuffen – der Cooldown wird dabei **nicht** verbraucht.
- `allies`/`enemies` basieren auf Scoreboard-Teams; ohne Team gilt niemand als Verbündeter.
- `wave_particle` ist eine zusätzliche, rein optische Ringanimation und unabhängig vom Ziel-Partikel `particle`.
