# Infested

> Spawnt beim Abbau eines Nexo-Custom-Blocks Vanilla-Mobs und/oder MythicMobs-Kreaturen, ähnlich dem Vanilla "Infested Stone".

| | |
|---|---|
| **Config-Key** | `Mechanics.custom_block.infested` |
| **Gilt für** | Custom Block (Nexo Noteblock-Mechanic) |
| **Listener-Klasse** | `Infested.InfestedListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle, immer aktiv |

## Was macht sie?

Wird ein Nexo-Custom-Block mit dieser Mechanic abgebaut (nicht im Creative-Modus, nur wenn `ProtectionLib` den Abbau erlaubt), würfelt der Listener mit `probability`, ob die Mechanic überhaupt auslöst. Ist `drop-loot` deaktiviert, wird der reguläre Block-Loot durch einen leeren Drop ersetzt. Optional werden Partikel (`particles`) am Block gespawnt. Danach werden abhängig von `selector` entweder **alle** konfigurierten Entities/MythicMobs (`all`) oder **eine zufällige** davon (`random`) über dem Block gespawnt. Ist `safe-spawn` aktiv, wird vorher eine sichere Spawn-Position in der Umgebung gesucht, statt immer direkt auf dem Blockzentrum zu spawnen.

## Wann einsetzen?

- Fallen-/Dungeon-Blöcke, die beim Abbau Monster oder MythicMobs-Bosse freisetzen
- Risiko-Rohstoffblöcke, bei denen der Abbau eine Chance auf einen Mob-Angriff birgt (`probability`)
- Kombination mit MythicMobs für individuelle Encounter beim Ressourcenabbau

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.custom_block.infested.entities` | `List<String>` | `[]` | Vanilla-`EntityType`-Namen, die gespawnt werden können. Mind. dieser Key **oder** `mythic-mobs` **(Pflicht, einer von beiden aktiviert die Mechanic)** |
| `Mechanics.custom_block.infested.mythic-mobs` | `List<String>` | `[]` | MythicMobs-Interne-Namen, die gespawnt werden können. Nur wirksam, wenn MythicMobs auf dem Server geladen ist – sonst wird der Key ignoriert und eine Warnung geloggt |
| `Mechanics.custom_block.infested.probability` | `double` | `1.0` | Wahrscheinlichkeit (0.0–1.0), dass beim Abbau überhaupt gespawnt wird |
| `Mechanics.custom_block.infested.selector` | `String` | `all` | `all` = alle konfigurierten Entities/Mobs spawnen, `random` = nur eine zufällige Auswahl |
| `Mechanics.custom_block.infested.particles` | `bool` | `false` | Ob am Block ein Partikeleffekt (`WHITE_SMOKE`) gespawnt wird |
| `Mechanics.custom_block.infested.drop-loot` | `bool` | `true` | Ob der normale Block-Loot zusätzlich zum Mob-Spawn gedroppt wird |
| `Mechanics.custom_block.infested.safe-spawn` | `bool` | `false` | Ob vor dem Spawn eine sichere Position in der Umgebung gesucht wird, statt direkt auf dem Block zu spawnen |

## Beispiel

```yaml
infizierter_stein:
  material: NOTE_BLOCK
  Mechanics:
    custom_block:
      infested:
        entities:
          - SILVERFISH
          - ZOMBIE
        mythic-mobs:
          - CaveTroll
        probability: 0.5
        selector: random
        particles: true
        drop-loot: true
        safe-spawn: true
```

## Hinweise & Besonderheiten

- Gilt nur für Nexo-Custom-Blocks auf Noteblock-Basis – Chorusblock-basierte Custom-Blocks lösen diese Mechanic nicht aus.
- `mythic-mobs` erfordert das MythicMobs-Plugin; ohne dieses wird der Key beim Laden ignoriert und eine Warnung im Server-Log ausgegeben.
- Ungültige Einträge in `entities` (falscher `EntityType`-Name) werden übersprungen und geloggt, ohne das Laden der Mechanic zu verhindern.
- Funktioniert nicht im Creative-Modus und respektiert Schutz-Plugins über `ProtectionLib`.
