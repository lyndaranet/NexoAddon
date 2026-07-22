# VeinMiner

> Bricht beim Abbau eines Blocks die gesamte zusammenhängende "Ader" gleichartiger Blöcke auf einmal ab.

| | |
|---|---|
| **Config-Key** | `Mechanics.veinminer` |
| **Gilt für** | Item (Werkzeug) |
| **Listener-Klasse** | `VeinMiner.VeinMinerListener` |
| **Toggle/Sneak-Verhalten** | Optional per Rechtsklick (in die Luft) an-/ausschaltbar, wenn `toggleable: true` |

## Was macht sie?

Bricht der Spieler mit einem VeinMiner-Werkzeug einen Block, der in `whitelist` (Vanilla-Material oder Nexo-Block-ID) enthalten ist, sucht die Mechanic von diesem Block ausgehend per Breitensuche alle direkt und diagonal angrenzenden Blöcke, die ebenfalls in der Whitelist stehen. Ist `same_material` aktiv, werden nur Blöcke desselben Typs/derselben Nexo-ID wie der Ursprungsblock mitgenommen, sonst jeder Whitelist-Block in Reichweite. Die Suche stoppt, sobald `limit` erreicht ist oder ein Block weiter als `distance` (quadrierte Distanz) vom Ursprung entfernt liegt. Alle gefundenen Blöcke werden anschließend einzeln über ein reguläres `BlockBreakEvent` abgebaut.

## Wann einsetzen?

- Erz-Spitzhacken, die ganze Erzadern auf einen Schlag abbauen
- Kombination mit Nexo-Custom-Blöcken (z. B. eigene Erz-Varianten) über die Nexo-ID-Whitelist
- Werkzeuge, die sich per Rechtsklick ein-/ausschalten lassen, damit Spieler auch gezielt einzelne Blöcke abbauen können

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.veinminer.distance` | `int` | `10` | **(Pflicht, aktiviert die Mechanic)** Maximale (quadrierte) Distanz vom Ursprungsblock, bis zu der die Suche noch Blöcke aufnimmt |
| `Mechanics.veinminer.toggleable` | `bool` | `false` | Ob der Spieler die Mechanic per Rechtsklick (Luft) an/aus schalten kann |
| `Mechanics.veinminer.same_material` | `bool` | `true` | Ob nur Blöcke mit exakt demselben Material/derselben Nexo-ID wie der Ursprungsblock mitgenommen werden |
| `Mechanics.veinminer.limit` | `int` | `10` | Maximale Anzahl an Blöcken, die auf einmal abgebaut werden |
| `Mechanics.veinminer.whitelist` | `List<String>` | `[]` (leer) | Vanilla-Materials oder Nexo-Block-IDs, die überhaupt als vein-minebar gelten. Leer = keine Blöcke gelten als gültig |

## Beispiel

```yaml
erzspitzhacke:
  material: IRON_PICKAXE
  Mechanics:
    veinminer:
      distance: 10
      toggleable: true
      same_material: true
      limit: 32
      whitelist:
        - IRON_ORE
        - DEEPSLATE_IRON_ORE
        - custom_nexo_ore
```

## Hinweise & Besonderheiten

- Ohne Einträge in `whitelist` findet die Mechanic keine gültigen Startblöcke und tut effektiv nichts.
- Respektiert Schutz-Plugins über `ProtectionLib`: geschützte Blöcke werden übersprungen, nicht die ganze Aktion abgebrochen.
- Flüssigkeiten und als "unbreakable" markierte Blöcke werden nie mitgebrochen.
- Der Toggle-Status wird im Item (PersistentDataContainer) gespeichert, gilt also pro Item-Stack, nicht global pro Spieler.
- Die Toggle-Meldungen kommen aus `config.yml` (`messages.veinminer.enabled` / `.disabled`), nicht aus dieser Mechanic-Konfiguration selbst.
