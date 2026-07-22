# Shape Wave

> Trifft sofort alle lebenden Wesen innerhalb einer konfigurierbaren 3D-Form (Kegel, Zylinder, Keil, Nova, Fächer, Linie) – optional mit animierter Partikel-Ausbreitung.

| | |
|---|---|
| **Config-Key** | `Mechanics.shape_wave` |
| **Gilt für** | Item |
| **Listener-Klasse** | `ShapeWaveMechanic.ShapeWaveMechanicListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle; Auslösung per `trigger` (Rechts-/Linksklick, Shift-Rechtsklick oder `on_hit`) |

## Was macht sie?

Anders als `projectile` (fliegt über Zeit) oder `beam` (schmaler Instant-Strahl) wirkt diese Mechanic sofort auf ein ganzes Volumen: Je nach `shape` wird ein Kegel, Zylinder, Keil, eine Kugelschale (Nova, ignoriert die Blickrichtung), ein Fächer aus mehreren Kegeln oder eine schmale Linie um den Auslösepunkt aufgespannt. Alle darin liegenden Wesen (außer dem Caster) erhalten Schaden, Feuer, Knockback, Effekte und Kommandos; `self_heal`/`self_damage`/`self_effects` wirken zusätzlich einmalig auf den Caster. Optional wird das Volumen mit `fill_particles` sichtbar gemacht, entweder sofort komplett oder progressiv über `animate_ticks` wachsend (`animate: true`) – die Trefferberechnung selbst passiert immer sofort bei Auslösung, unabhängig von der Animation.

## Wann einsetzen?

- Nahkampf-Flächenwaffen mit klarer Reichweitenbegrenzung (Kegel-/Keil-Angriffe)
- Rundum-Explosionen um den Spieler (Nova), z. B. Bomben-Stäbe
- Fächerartige Multi-Treffer-Angriffe (mehrere Strahlen in einem Bogen)

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.shape_wave.trigger` | `String` | `right_click` | `right_click`, `shift_right_click`, `left_click` oder `on_hit` |
| `Mechanics.shape_wave.cooldown` | `int` (Sekunden) | `0` | Cooldown pro Spieler |
| `Mechanics.shape_wave.shape` | `String` | `cone` | `cone`, `cylinder`, `wedge`, `nova`, `fan`, `line` |
| `Mechanics.shape_wave.max_targets` | `int` | `0` (unbegrenzt) | Begrenzt die Anzahl getroffener Ziele (nächste zuerst) |
| `Mechanics.shape_wave.range` | `double` | `8.0` | Länge von Kegel/Zylinder/Keil/Fächer/Linie |
| `Mechanics.shape_wave.angle` | `double` | `30.0` | Öffnungswinkel (Kegel/Keil), auch je Strahl beim Fächer |
| `Mechanics.shape_wave.radius` | `double` | `3.0` (`0.3` bei `line`) | Zylinder-/Linienradius bzw. Nova-Außenradius |
| `Mechanics.shape_wave.height` | `double` | `1.0` | Vertikale Halbhöhe des Keils |
| `Mechanics.shape_wave.min_radius` | `double` | `0.0` | Nova-Innenradius (für Donut-Form) |
| `Mechanics.shape_wave.rays` / `arc_degrees` | `int`/`double` | `3` / `90.0` | Fächer: Anzahl Strahlen bzw. Gesamtspreizung |
| `Mechanics.shape_wave.damage` / `knockback` | `double` | `0.0` | Schaden bzw. Rückstoß pro Ziel |
| `Mechanics.shape_wave.fire_duration` | `int` (Sekunden) | `0` | Entzündet Ziele für X Sekunden |
| `Mechanics.shape_wave.effects` | `List<AbilityEffect>` | `[]` | Potion-Effekte pro Ziel: `type`, `amplifier`, `duration` (Sek.), `chance` |
| `Mechanics.shape_wave.commands` | `List<String>` | `[]` | Konsolenkommandos, Platzhalter `{player}`/`{target}` |
| `Mechanics.shape_wave.self_effects` / `self_damage` / `self_heal` | – | `[]`/`0.0`/`0.0` | Wirkung auf den Caster selbst |
| `Mechanics.shape_wave.conditions.require_sneaking` / `require_health_below` / `require_permission` / `min_targets_required` | – | `false`/`100`/`""`/`0` | Nutzungsbedingungen; bei zu wenigen Zielen verpufft die Wirkung ohne Cooldown |
| `Mechanics.shape_wave.particle_density` | `double` | `3.0` | Dichte der Partikelringe im gefüllten Volumen |
| `Mechanics.shape_wave.animate` / `animate_ticks` | `bool`/`int` | `false` / `6` | Progressive Ausbreitungsanimation statt sofortiger Darstellung |
| `Mechanics.shape_wave.fill_particles` | `List<TrailEntry>` | `[]` | Partikel zur Darstellung des Volumens: `particle`, `count`, `offset` |
| `Mechanics.shape_wave.sound` / `sound_hit` | `String` | – | Sound beim Auslösen bzw. bei jedem Treffer |

> Nur die Keys, die der Loader tatsächlich liest (`ItemConfigUtil.java`, `loadShapeWaveMechanic`). Ohne `Mechanics.shape_wave`-Abschnitt lädt die Mechanic gar nicht.

## Beispiel

```yaml
INFERNO_MACE:
  displayname: "<red>Inferno Keule"
  Mechanics:
    shape_wave:
      trigger: right_click
      cooldown: 6
      shape: cone
      range: 8.0
      angle: 30.0
      damage: 6.0
      fire_duration: 3
      fill_particles:
        - particle: FLAME
          count: 1
          offset: 0.05
        - particle: SMOKE_NORMAL
          count: 1
      animate: true
      animate_ticks: 5
      sound: ENTITY_BLAZE_SHOOT
      sound_hit: ENTITY_PLAYER_HURT
```

## Hinweise & Besonderheiten

- Sechs Formen zur Auswahl: `cone` (Kegel), `cylinder` (schmaler Strahl mit Breite), `wedge` (horizontaler Keil mit Höhenbegrenzung), `nova` (Kugelschale um den Spieler, ignoriert Blickrichtung), `fan` (mehrere Kegel-Strahlen fächerförmig), `line` (sehr schmaler Zylinder).
- Der Default für `radius` hängt von `shape` ab: `0.3` bei `line`, sonst `3.0` – bei Bedarf explizit überschreiben.
- `animate` beeinflusst nur die Partikeldarstellung; Schaden/Effekte werden immer sofort bei Auslösung berechnet.
- Bei `trigger: on_hit` liegt der Ursprung der Form auf dem getroffenen Ziel, die Ausrichtung folgt weiterhin der Blickrichtung des Casters.
