# DropExperience

> Lässt einen Nexo-Custom-Block beim Abbau zusätzlich Erfahrungspunkte droppen.

| | |
|---|---|
| **Config-Key** | `Mechanics.custom_block.drop` |
| **Gilt für** | Custom Block (Nexo Noteblock-Mechanic) |
| **Listener-Klasse** | `DropExperience.DropExperienceListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle, immer aktiv |

## Was macht sie?

Wird ein Nexo-Custom-Block mit dieser Mechanic abgebaut, spawnt der Listener am Block einen Erfahrungsorb mit `experience` XP. Das passiert nur, wenn das Event nicht bereits abgebrochen wurde, der Spieler sich nicht im Creative-Modus befindet und das Werkzeug in der Hand keine Verzauberung mit Verlustfluch (Silk Touch) trägt.

## Wann einsetzen?

- Custom-Erz-Blöcke, die wie Vanilla-Erze zusätzlich XP geben sollen
- Ressourcen-Blöcke im Rift-/Mining-System, die den Spieler für den Abbau belohnen
- In Kombination mit `miningtools`, um XP nur für Blöcke zu vergeben, die mit dem richtigen Werkzeug abgebaut wurden

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.custom_block.drop.experience` | `double` | `0.0` | **(Pflicht, aktiviert die Mechanic)** Menge an XP, die beim Abbau als Erfahrungsorb gespawnt wird (wird beim Spawnen abgerundet) |

## Beispiel

```yaml
erzblock_custom:
  material: NOTE_BLOCK
  Mechanics:
    custom_block:
      drop:
        experience: 5.0
```

## Hinweise & Besonderheiten

- Gilt nur für Nexo-Custom-Blocks auf Noteblock-Basis – Chorusblock-basierte Custom-Blocks lösen diese Mechanic nicht aus.
- Silk-Touch-Werkzeuge und der Creative-Modus unterdrücken den XP-Drop komplett, unabhängig vom konfigurierten Wert.
- Der XP-Wert wird beim Spawnen des Orbs auf eine ganze Zahl abgerundet (`Math.floor`).
