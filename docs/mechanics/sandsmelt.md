# SandSmelt

> Lässt ein Werkzeug beim Abbau von Sand-Blöcken statt Sand direkt Glas droppen – simuliertes "Schmelzen ohne Ofen".

| | |
|---|---|
| **Config-Key** | `Mechanics.sandsmelt` |
| **Gilt für** | Item (Werkzeug, i. d. R. Schaufel) |
| **Listener-Klasse** | `SandSmelt.SandSmeltListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – immer aktiv, solange `enabled: true` |

## Was macht sie?

Bricht der Spieler mit einem SandSmelt-Werkzeug in der Haupthand einen Block, dessen Material in `sand_types` gelistet ist, wird der normale Block-Drop unterdrückt und stattdessen genau ein `GLASS`-Item gedroppt. Die Mechanic greift nur, wenn das Werkzeug keinen Silk-Touch-Enchant trägt, der Spieler nicht im Creative-Modus ist und ein Zufalls-Wurf gegen `probability` erfolgreich ist.

## Wann einsetzen?

- Themen-Werkzeug für Wüsten-/Strand-Kits ("Sand-Schmelzer")
- Ressourcen-Shortcut, um Glas ohne Ofen/Brennstoff herzustellen
- Belohnungs- oder Event-Tool mit reduzierter Erfolgswahrscheinlichkeit (`probability < 1.0`)

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.sandsmelt` | Abschnitt | – | **(Pflicht)** Allein das Vorhandensein dieses Abschnitts (auch leer) aktiviert die Mechanic beim Laden |
| `Mechanics.sandsmelt.enabled` | `bool` | `true` | Mechanic ein-/ausschalten, ohne die Konfiguration zu entfernen |
| `Mechanics.sandsmelt.probability` | `double` | `1.0` | Wahrscheinlichkeit (0.0–1.0), dass beim Abbau Glas gedroppt wird |
| `Mechanics.sandsmelt.sand_types` | `List<Material>` | `[SAND, RED_SAND]` | Vanilla-Materialien, die als "Sand" gelten. Nicht gesetzt/leer → Default `SAND` + `RED_SAND` |

## Beispiel

```yaml
my_sand_shovel:
  material: DIAMOND_SHOVEL
  Mechanics:
    sandsmelt:
      enabled: true
      probability: 1.0
      sand_types:
        - SAND
        - RED_SAND
```

## Hinweise & Besonderheiten

- Silk Touch am Werkzeug deaktiviert den Effekt komplett – der Block droppt sich dann normal selbst (Sand statt Glas).
- Spieler im Creative-Modus sind von der Mechanic ausgenommen.
- Es werden nur **Vanilla-Materialtypen** über `block.getType()` geprüft – Nexo-Custom-Blöcke werden trotz anderslautendem Code-Kommentar nicht unterstützt.
- `sand_types` akzeptiert beliebige Vanilla-Materialien (z. B. `SOUL_SAND`), nicht nur echten Sand.
- Der unterdrückte Drop betrifft *alle* normalen Drops des Blocks; es wird immer genau ein `GLASS`-Item gedroppt, unabhängig von Fortune o. Ä.
