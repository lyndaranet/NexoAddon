# Area Mining

> Baut beim Abbau eines Blocks zusätzliche Blöcke in einer konfigurierten Form (Linie, Ader, Fläche) mit ab.

| | |
|---|---|
| **Config-Key** | `Mechanics.area_mining` |
| **Gilt für** | Item (Werkzeug) |
| **Listener-Klasse** | `AreaMiningMechanic.AreaMiningMechanicListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – wirkt bei jedem passenden Blockabbau automatisch, optional gefiltert über `tool_types` |

## Was macht sie?

Bricht ein Spieler mit einem `area_mining`-Werkzeug einen Block, berechnet die Mechanic abhängig von `shape` zusätzliche Blöcke und baut sie direkt über `Block#breakNaturally(tool)` ab: **LINE** – `length` Blöcke entlang der Blickrichtung in den getroffenen Block hinein; **VEIN** – Breitensuche über direkt verbundene Blöcke desselben Materials, bis `max_blocks` erreicht ist; **CUBE** – eine `radius`×`radius`-Fläche senkrecht zur getroffenen Blockseite, `depth` Schichten tief in Blickrichtung. Luft, Blöcke mit negativer Härte (unzerstörbar) sowie in `denied_blocks` gelistete Materialien werden übersprungen. Optional wird pro Zusatzblock 1 Haltbarkeitspunkt verbraucht.

## Wann einsetzen?

- 3×3-Hammer für schnelleren Flächenabbau
- Vein-Miner-Axt, die zusammenhängende Baumstämme/Erzadern komplett mitnimmt
- Tunnel-Werkzeuge (Spitzhacke/Schaufel) mit fester Reichweite entlang der Blickrichtung

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.area_mining.shape` | `String` (Enum) | `LINE` | Abbauform: `LINE`, `VEIN`, `CUBE`. Ein ungültiger Wert lässt die gesamte Mechanic nicht laden (Log-Warnung) |
| `Mechanics.area_mining.consume_durability` | `bool` | `false` | Ob pro zusätzlichem Block 1 Haltbarkeitspunkt verbraucht wird |
| `Mechanics.area_mining.length` | `int` | `1` | Nur `shape: LINE` – Anzahl Blöcke entlang der Blickrichtung |
| `Mechanics.area_mining.max_blocks` | `int` | `64` | Nur `shape: VEIN` – maximale Gesamtzahl verbundener Blöcke gleichen Typs (inkl. Ursprungsblock) |
| `Mechanics.area_mining.radius` | `int` | `1` | Nur `shape: CUBE` – Radius der Fläche senkrecht zur getroffenen Seite |
| `Mechanics.area_mining.depth` | `int` | `1` | Nur `shape: CUBE` – Tiefe in Blickrichtung |
| `Mechanics.area_mining.tool_types` | `List<String>` | nicht gesetzt (kein Filter) | Substring-Filter auf den (kleingeschriebenen) Materialnamen des Werkzeugs, z. B. `pickaxe`, `axe`, `shovel` |
| `Mechanics.area_mining.denied_blocks` | `List<Material>` | nicht gesetzt | Vanilla-Materialien, die nie zusätzlich mit abgebaut werden (z. B. Obsidian) |

## Beispiel

```yaml
HAMMER:
  displayname: "<dark_gray>Hammer"
  Mechanics:
    area_mining:
      shape: cube
      radius: 1
      depth: 1
      consume_durability: true
      denied_blocks:
        - OBSIDIAN
        - CRYING_OBSIDIAN
        - BEDROCK

VEIN_AXE:
  displayname: "<gold>Vein Axt"
  Mechanics:
    area_mining:
      shape: vein
      max_blocks: 32
      consume_durability: true
      tool_types:
        - axe
```

## Hinweise & Besonderheiten

- Nur der ursprünglich abgebaute Block läuft über das reguläre `BlockBreakEvent`. Die Zusatzblöcke werden per `breakNaturally()` direkt abgebaut – dabei feuert **kein** `BlockBreakEvent`, anders als bei Timber. Schutz-Plugins, die ausschließlich auf `BlockBreakEvent` reagieren, greifen für die Zusatzblöcke also nicht.
- Es gibt keine eingebaute `ProtectionLib`-Prüfung in dieser Mechanic – die Region-Absicherung muss ggf. über andere Wege (z. B. WorldGuard-Flag-Listener auf `breakNaturally`) sichergestellt werden.
- `tool_types` ist ein reiner Substring-Match auf den Materialnamen; `pickaxe` matcht z. B. automatisch `DIAMOND_PICKAXE` und `NETHERITE_PICKAXE`.
- Bei `shape: VEIN` zählt der Ursprungsblock zur `max_blocks`-Grenze mit dazu.
