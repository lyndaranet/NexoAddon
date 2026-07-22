# Telekinesis

> Lässt abgebaute Blöcke direkt ins Spielerinventar wandern, statt am Boden zu droppen.

| | |
|---|---|
| **Config-Key** | `Mechanics.telekinesis` |
| **Gilt für** | Item (Werkzeug) |
| **Listener-Klasse** | `Telekinesis.TelekinesisListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – immer aktiv, solange `enabled: true` |

## Was macht sie?

Baut ein Spieler mit einem Telekinesis-Werkzeug einen Block ab, greift die Mechanic mit höchster Event-Priorität in das `BlockBreakEvent` ein (nur wenn der Block regulär Drops erzeugen würde). Statt die berechneten Drops am Boden zu droppen, werden sie direkt dem Spielerinventar hinzugefügt; ist das Inventar voll, fallen nur die überschüssigen Items regulär am Block-Standort zu Boden. Über eine optionale Whitelist (`materials`/`nexo_ids`) lässt sich Telekinesis auf bestimmte Blocktypen beschränken – sind beide Listen leer, greift die Mechanic für alle abgebauten Blöcke.

## Wann einsetzen?

- Premium-Werkzeuge, die das Aufsammeln von Drops überflüssig machen
- Kombination mit BigMining/VeinMiner/Timber, um zu verhindern, dass Blöcke außerhalb der eigentlichen Whitelist (z. B. Nachbarblöcke im Radius) versehentlich mit ins Inventar wandern
- Abbau in gefährlichem Terrain (Lava, Wasser, Abgründe), wo Drops sonst verloren gehen könnten

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.telekinesis.enabled` | `bool` | `true` | **(Pflicht, Abschnitt `Mechanics.telekinesis` muss vorhanden sein)** Ob Telekinesis aktiv ist |
| `Mechanics.telekinesis.materials` | `List<String>` | `[]` (leer) | Vanilla-Blocktypen, für die Telekinesis greift. Leer (zusammen mit `nexo_ids`) = alle Blöcke |
| `Mechanics.telekinesis.nexo_ids` | `List<String>` | `[]` (leer) | Nexo-Custom-Block-IDs, für die Telekinesis greift |

## Beispiel

```yaml
super_pickaxe:
  material: NETHERITE_PICKAXE
  Mechanics:
    telekinesis:
      enabled: true
      materials:
        - IRON_ORE
        - DEEPSLATE_IRON_ORE
      nexo_ids:
        - nexo_custom_ore
```

## Hinweise & Besonderheiten

- Wird `event.setDropItems(false)` bereits durch eine andere Mechanic/ein anderes Plugin vorher gesetzt (z. B. Silk-Touch-Ersatzlogik), greift Telekinesis nicht, da keine Drops mehr vorhanden sind.
- Ist eine Whitelist gesetzt, wird zuerst gegen `materials` geprüft; nur wenn `nexo_ids` zusätzlich gefüllt ist, wird für Nexo-Custom-Blöcke zusätzlich deren Block-ID geprüft.
- Kombiniert sich problemlos mit BigMining, VeinMiner und Timber, da jede zusätzlich gebrochene Blockfläche ihr eigenes reguläres `BlockBreakEvent` auslöst und somit ebenfalls von Telekinesis erfasst wird.
