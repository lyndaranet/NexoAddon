# SpawnerBreak

> Erlaubt das Abbauen von Monster-Spawnern mit einem speziellen Werkzeug, mit konfigurierbarer Drop-Chance – nur in der Welt "Plots".

| | |
|---|---|
| **Config-Key** | `Mechanics.spawnerbreak` |
| **Gilt für** | Item (Werkzeug) |
| **Listener-Klasse** | `SpawnerBreak.SpawnerBreakListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle, immer aktiv wenn Werkzeug in der Hand |

## Was macht sie?

Baut ein Spieler in der Welt "Plots" mit einem SpawnerBreak-Werkzeug einen Vanilla-Spawner ab, erzeugt der Listener mit Wahrscheinlichkeit `probability` ein Spawner-Item, das über eine PersistentDataContainer-Markierung den ursprünglichen Mob-Typ des Spawners speichert. Wird dieses Item später wieder platziert, liest ein zweiter Handler die Markierung aus und stellt den Spawner mit demselben Mob-Typ wieder her. Ist `dropExperience` deaktiviert, wird die reguläre Spawner-XP beim Abbau unterdrückt.

## Wann einsetzen?

- Spezial-Werkzeuge, mit denen Spieler in Plot-Welten Spawner "einsammeln" und an anderer Stelle wieder platzieren können
- Belohnungswerkzeuge mit begrenzter Drop-Chance, um Spawner-Farming einzuschränken
- Situationen, in denen die normale Spawner-XP nicht gewünscht ist (`dropExperience: false`)

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.spawnerbreak.probability` | `double` | – | **(Pflicht, aktiviert die Mechanic)** Wahrscheinlichkeit (0.0–1.0), dass beim Abbau ein Spawner-Item gedroppt wird |
| `Mechanics.spawnerbreak.dropExperience` | `bool` | `false` | Ob beim Abbau die reguläre Spawner-Erfahrung droppt |

## Beispiel

```yaml
spawner_greifer:
  material: NETHERITE_PICKAXE
  Mechanics:
    spawnerbreak:
      probability: 0.5
      dropExperience: false
```

## Hinweise & Besonderheiten

- Funktioniert **nur in einer Welt namens "Plots"** – dieser Weltname ist im Code fest verdrahtet und nicht konfigurierbar.
- Respektiert Schutz-Plugins über `ProtectionLib` beim Abbau.
- Der Mob-Typ des Spawners wird im Item-Namen ("`<EntityType>` Spawner") und in den PersistentData gespeichert; ein unbekannter/fehlender Mob-Typ fällt auf `PIG` zurück.
- Beim Wiederplatzieren wird der gespeicherte Mob-Typ automatisch auf den neuen Spawner übertragen, unabhängig davon, welches Item konkret die Mechanic ausgelöst hat.
