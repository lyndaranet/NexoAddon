# BigMining

> Bricht beim Abbau eines Blocks zusätzlich alle Blöcke in einem Radius/Tiefe-Quader um den Ursprungsblock in Richtung des Anvisierens.

| | |
|---|---|
| **Config-Key** | `Mechanics.bigmining` |
| **Gilt für** | Item (Werkzeug, i. d. R. Spitzhacke) |
| **Listener-Klasse** | `BigMining.BigMiningListener` |
| **Toggle/Sneak-Verhalten** | Optional per Rechtsklick (in die Luft) an-/ausschaltbar, wenn `switchable: true` |

## Was macht sie?

Bricht der Spieler mit einem BigMining-Werkzeug einen Block, ermittelt die Mechanic anhand der letzten zwei anvisierten Blöcke die Blickrichtung (Face) und bricht zusätzlich alle Blöcke in einem quadratischen Bereich (`radius` x `radius`) senkrecht zu dieser Richtung, über `depth` Blöcke in die Tiefe fortgesetzt. Jeder zusätzliche Block wird asynchron geprüft (Flüssigkeit, unzerstörbare Blöcke, Schutz-Plugins) und dann über ein reguläres `BlockBreakEvent` abgebaut, sodass Drops und andere Plugins normal greifen. Ist `materials` gesetzt, werden nur Blöcke dieser Vanilla-Typen mit abgebaut; ist die Liste leer, wird jeder Blocktyp im Bereich mitgenommen. Beim ersten Blockbruch wird optional ein Sound abgespielt.

## Wann einsetzen?

- Premium-Spitzhacken im Shop, die größere Flächen auf einmal abbauen (z. B. 3x3- oder 5x5-Tunnel)
- Abbau-Werkzeuge, die sich per Rechtsklick ein-/ausschalten lassen, damit Spieler wahlweise normal oder im Großformat abbauen
- Begrenzung auf bestimmte Materialien (z. B. nur Stein-Varianten), damit Erze nicht "versehentlich" durch die erweiterte Fläche mit abgebaut werden

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.bigmining.radius` | `int` | `1` | **(Pflicht zusammen mit `depth`)** Kantenlänge des quadratischen Abbaubereichs um den Ursprungsblock |
| `Mechanics.bigmining.depth` | `int` | `1` | **(Pflicht zusammen mit `radius`)** Anzahl Blöcke, die in Blickrichtung zusätzlich abgebaut werden |
| `Mechanics.bigmining.switchable` | `bool` | `false` | Ob der Spieler die Mechanic per Rechtsklick (Luft) an/aus schalten kann |
| `Mechanics.bigmining.sound` | `String` | `block.stone.break` | Sound-Key (z. B. `block.stone.break` oder `namespace:key`), der beim Abbau am Ursprungsblock abgespielt wird |
| `Mechanics.bigmining.materials` | `List<String>` | `[]` (leer) | Vanilla-Materials, die im Bereich mit abgebaut werden dürfen. Leer = alle Blocktypen im Bereich zählen |

> Nur Vanilla-Materials werden unterstützt, keine Nexo-Custom-Block-IDs.

## Beispiel

```yaml
runenbrecher_v1:
  material: NETHERITE_PICKAXE
  Mechanics:
    bigmining:
      radius: 2
      depth: 1
      switchable: true
      sound: block.stone.break
      materials:
        - STONE
        - COBBLESTONE
        - DEEPSLATE
```

## Hinweise & Besonderheiten

- Nur Vanilla-Materials in `materials` möglich – Nexo-Custom-Block-IDs werden hier (anders als bei Timber/VeinMiner) nicht ausgewertet.
- Respektiert Schutz-Plugins über `ProtectionLib`: geschützte Blöcke im Bereich werden übersprungen, nicht die ganze Aktion abgebrochen.
- Flüssigkeiten und als "unbreakable" markierte Blöcke werden nie mitgebrochen.
- Der Toggle-Status wird im Item (PersistentDataContainer) gespeichert, gilt also pro Item-Stack, nicht global pro Spieler.
- Die Toggle-Meldungen kommen aus `config.yml` (`messages.bigmining.enabled` / `.disabled`), nicht aus dieser Mechanic-Konfiguration selbst.
