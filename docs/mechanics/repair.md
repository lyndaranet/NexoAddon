# Repair

> Repariert ein beschädigtes Item, indem der Spieler ein Reparatur-Item aus der Cursor-Hand per Linksklick auf das Ziel-Item im Inventar "ablegt".

| | |
|---|---|
| **Config-Key** | `Mechanics.repair` |
| **Gilt für** | Item (das Reparatur-Material/-Werkzeug, nicht das zu reparierende Item) |
| **Listener-Klasse** | `Repair.RepairListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – Linksklick im Inventar mit dem Repair-Item auf dem Cursor über einem beschädigten Item löst die Reparatur direkt aus |

## Was macht sie?

Die Mechanic hängt am **Reparatur-Item** (z. B. ein "Reparatur-Kit" oder ein Material). Hält der Spieler dieses Item mit dem Mauszeiger (Cursor) in einem beliebigen Inventar und klickt links auf ein anderes, beschädigtes Item im selben Inventar, wird der Klick abgefangen (`InventoryClickEvent` wird gecancelt) und die Reparatur ausgeführt. Je nach Konfiguration wird entweder ein Prozentsatz (`ratio`) des aktuellen Schadens abgezogen oder ein fester Wert (`fixed_amount`) an Durability wiederhergestellt. Das Ziel-Item muss beschädigbar sein und bereits Schaden haben, sonst passiert nichts. Das Reparatur-Item auf dem Cursor wird dabei verbraucht: Hat es selbst eine maximale Durability (z. B. ein Nexo-Werkzeug mit eigener Haltbarkeit), nimmt es pro Reparatur 1 Schadenspunkt und wird erst entfernt, wenn es selbst voll verschlissen ist. Hat es keine Durability (z. B. ein einfaches Material), wird direkt 1 Stück vom Stack abgezogen.

## Wann einsetzen?

- Reparatur-Kits im Shop, die pro Nutzung nur einen Teil des Schadens beheben (Ratio-basiert)
- Verbrauchsmaterialien, die eine feste Anzahl Durability-Punkte auffüllen (z. B. "Eisenbarren repariert 50 Punkte")
- Gezielte Beschränkung, welche Werkzeuge/Rüstungen mit einem bestimmten Reparatur-Item repariert werden dürfen (Whitelist/Blacklist)

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.repair.ratio` | `double` | `0.0` | Anteil (0.0–1.0) des aktuellen Schadens, der pro Reparatur entfernt wird, z. B. `0.5` = 50 % des Restschadens. Hat Vorrang vor `fixed_amount`, sobald größer als 0 |
| `Mechanics.repair.fixed_amount` | `int` | `0` | Feste Anzahl an Durability-Punkten, die pro Reparatur entfernt wird (greift nur, wenn `ratio` nicht gesetzt bzw. `0` ist) |
| `Mechanics.repair.whitelist` | `List<String>` | `[]` (leer) | Vanilla-Materials oder Nexo-IDs, die als Ziel-Item repariert werden dürfen. Leer = alle Items erlaubt (sofern nicht per Blacklist ausgeschlossen) |
| `Mechanics.repair.blacklist` | `List<String>` | `[]` (leer) | Vanilla-Materials oder Nexo-IDs, die **nicht** repariert werden dürfen. Hat Vorrang vor der Whitelist |

> Mindestens `ratio` oder `fixed_amount` muss im YAML vorhanden sein, damit die Mechanic überhaupt geladen wird.

## Beispiel

```yaml
repair_kit_diamond:
  material: DIAMOND
  Mechanics:
    repair:
      ratio: 0.5
      whitelist:
        - DIAMOND_PICKAXE
        - DIAMOND_SWORD
        - custom_nexo_tool

repair_kit_fixed:
  material: IRON_INGOT
  Mechanics:
    repair:
      fixed_amount: 50
      blacklist:
        - NETHERITE_SWORD
```

## Hinweise & Besonderheiten

- Das Ziel-Item darf selbst **kein** eigenes `repair`-Mechanic besitzen – so kann ein Reparatur-Item nicht auf ein anderes Reparatur-Item angewendet werden.
- Die Reparatur funktioniert in jedem Inventar, in dem ein `InventoryClickEvent` ausgelöst wird (Spielerinventar, Kisten, Custom-GUIs usw.), nicht nur im eigenen Inventar.
- Nur Linksklicks lösen die Reparatur aus; Rechtsklick, Shift-Klick usw. werden ignoriert.
- Whitelist/Blacklist beziehen sich auf das **Ziel-Item** (das beschädigte Item), nicht auf das Reparatur-Item selbst.
