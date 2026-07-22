# Block Trigger Launch

> Zweistufige Mechanic: Rechtsklick aktiviert einen Zeit-Buff, danach löst jeder Schritt auf einen konfigurierten Block (z. B. Wasser, Lava, Pulverschnee) einen Launch aus.

| | |
|---|---|
| **Config-Key** | `Mechanics.block_trigger_launch` |
| **Gilt für** | Item |
| **Listener-Klasse** | `BlockTriggerLaunchMechanic.BlockTriggerLaunchMechanicListener` |
| **Toggle/Sneak-Verhalten** | Aktivierung fest per Rechtsklick; Launch löst automatisch beim Betreten eines Trigger-Blocks aus |

## Was macht sie?

Ein Rechtsklick aktiviert einen zeitlich begrenzten Buff (`duration`), der `active_effects` gewährt und Aktivierungs-Partikel/-Sound abspielt; eine erneute Aktivierung ist erst nach `activation_cooldown` bzw. nach Ablauf des Buffs möglich. Solange der Buff aktiv ist, prüft die Mechanic bei jeder Blockänderung unter den Füßen (verschoben um `check_block_offset`), ob dort einer der `trigger_blocks` liegt – ist das der Fall, wird der Spieler mit vertikaler `launch_power` und optionaler horizontaler `horizontal_power` (Richtung aus Bewegung oder Blickrichtung) hochgeschleudert, erhält `launch_effects` und optional Fallschaden-Immunität für `no_fall_damage_ticks`. Jeder Launch hat seinen eigenen, meist deutlich kürzeren `per_launch_cooldown`, sodass wiederholtes Aufkommen auf dem Trigger-Block (z. B. Wasserhüpfen) mehrfach hintereinander funktioniert. Während der Buff aktiv ist, rendert eine wiederkehrende Task optional eine Partikel-Aura (`aura_layers`) um den Spieler und zeigt bei `action_bar: true` einen Countdown an.

## Wann einsetzen?

- Wasser-/Lava-Stäbe, die Spieler beim Sprung in die Flüssigkeit hochschleudern (Surfen/Bounce-Gameplay)
- Trampolin-Items auf Pulverschnee/Schnee mit hoher Wiederholrate
- Mobilitäts-Items, deren Wirkung an einen zeitlich begrenzten Buff gekoppelt ist statt permanent zu sein

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.block_trigger_launch.activation_cooldown` | `double` (Sekunden) | `0` | Cooldown bis zur erneuten Aktivierung des Buffs |
| `Mechanics.block_trigger_launch.duration` | `double` (Sekunden) | `8` | Dauer des aktiven Buff-Fensters |
| `Mechanics.block_trigger_launch.per_launch_cooldown` | `double` (Sekunden) | `0.3` | Cooldown zwischen einzelnen Launches innerhalb des Buff-Fensters |
| `Mechanics.block_trigger_launch.trigger_blocks` | `List<Material>` | `[]` | Blöcke, die (unter den Füßen) einen Launch auslösen, z. B. `WATER`, `LAVA`, `POWDER_SNOW` |
| `Mechanics.block_trigger_launch.check_block_offset` | `int` | `0` | Versatz nach unten relativ zu den Füßen, an dem der Trigger-Block geprüft wird |
| `Mechanics.block_trigger_launch.launch_power` | `double` | `1.0` | Vertikale Launch-Geschwindigkeit |
| `Mechanics.block_trigger_launch.horizontal_power` | `double` | `0.0` | Horizontale Launch-Komponente |
| `Mechanics.block_trigger_launch.horizontal_source` | `String` | `movement` | `movement` (Bewegungsrichtung) oder `look` (Blickrichtung) |
| `Mechanics.block_trigger_launch.active_effects` | `List<AbilityEffect>` | `[]` | Effekte während des Buffs, werden beim Ablauf automatisch entfernt |
| `Mechanics.block_trigger_launch.launch_effects` | `List<AbilityEffect>` | `[]` | Effekte bei jedem einzelnen Launch |
| `Mechanics.block_trigger_launch.no_fall_damage_ticks` | `int` | `0` | Fallschaden-Immunität nach dem Launch (Server-Ticks) |
| `Mechanics.block_trigger_launch.aura_layers` | `List<AuraLayer>` | `[]` | Partikel-Aura während des Buffs; Felder wie bei `particle_aura` (`shape`, `particle`, `radius`, `count`, `rotation_speed`, …) |
| `Mechanics.block_trigger_launch.launch_particles` / `activate_particles` | `List<LaunchParticle>` | `[]` | Partikel bei Launch bzw. Aktivierung: `particle`, `count`, `offset` |
| `Mechanics.block_trigger_launch.launch_sound` / `activate_sound` | `String` | – | Sound bei Launch bzw. Aktivierung |
| `Mechanics.block_trigger_launch.action_bar` | `bool` | `true` | Zeigt Cooldown- bzw. Restzeit-Countdown in der Actionbar |

> Nur die Keys, die der Loader tatsächlich liest (`ItemConfigUtil.java`, `loadBlockTriggerLaunchMechanic`). Ohne `Mechanics.block_trigger_launch`-Abschnitt lädt die Mechanic gar nicht.

## Beispiel

```yaml
WASSERSTAB:
  displayname: "<aqua>Wasserstab"
  Mechanics:
    block_trigger_launch:
      activation_cooldown: 15
      duration: 8
      per_launch_cooldown: 0.3
      action_bar: true
      trigger_blocks:
        - WATER
      launch_power: 1.2
      active_effects:
        - type: WATER_BREATHING
          amplifier: 0
          duration: 8
      launch_effects:
        - type: SLOW_FALLING
          amplifier: 0
          duration: 3
      no_fall_damage_ticks: 60
      aura_layers:
        - shape: random
          particle: WATER_SPLASH
          radius: 0.6
          count: 3
      launch_particles:
        - particle: WATER_SPLASH
          count: 20
          offset: 0.2
        - particle: BUBBLE_COLUMN_UP
          count: 15
      launch_sound: ENTITY_DOLPHIN_JUMP
      activate_particles:
        - particle: WATER_SPLASH
          count: 10
      activate_sound: ITEM_BUCKET_FILL
```

## Hinweise & Besonderheiten

- Zweistufiger Ablauf: Erst der Rechtsklick aktiviert den Buff (eigener `activation_cooldown`), danach löst jeder Schritt auf einen `trigger_blocks`-Block einen Launch aus (eigener, meist deutlich kürzerer `per_launch_cooldown`) – so lassen sich z. B. mehrere Sprünge auf Wasser hintereinander realisieren.
- `check_block_offset` verschiebt den geprüften Block relativ zu den Füßen nach unten (`0` = Block auf Fußhöhe).
- `aura_layers` teilt sich die Datenstruktur mit der eigenständigen `particle_aura`-Mechanic (gleiche Felder), wird hier aber nur während des aktiven Buff-Fensters gerendert.
- Der Buff läuft über eine einzelne wiederkehrende Task pro Spieler, die Aura-Rendering, Actionbar-Countdown und automatisches Ablaufen übernimmt; `active_effects` werden beim Ablauf automatisch wieder entfernt.
- Solange der Buff bereits aktiv ist, wird ein erneuter Aktivierungs-Rechtsklick einfach ignoriert (kein Fehler, kein Reset).
