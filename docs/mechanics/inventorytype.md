# InventoryType

> Öffnet beim Rechtsklick auf einen Nexo Custom Block oder ein Nexo Furniture ein virtuelles Inventar eines bestimmten Typs (z. B. Werkbank, Amboss, Endertruhe).

| | |
|---|---|
| **Config-Key** | `Mechanics.inventoryType` |
| **Gilt für** | Custom Block / Furniture |
| **Listener-Klasse** | `InventoryType.InventoryTypeListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – jeder Rechtsklick (Haupthand) öffnet das Inventar neu |

## Was macht sie?

Interagiert ein Spieler mit der Haupthand mit einem Nexo Custom Block (Rechtsklick auf den Block) oder einem Nexo Furniture, das diese Mechanic besitzt, öffnet sich für ihn ein Inventar des konfigurierten `InventoryType`. Bei den Typen `ENDER_CHEST`, `WORKBENCH`, `CARTOGRAPHY`, `GRINDSTONE`, `ANVIL`, `ENCHANTING`, `SMITHING` und `STONECUTTER` wird das **echte, funktionsfähige** Vanilla-Menü an der Spielerposition geöffnet (inkl. Enderkiste des Spielers). Bei allen anderen `InventoryType`-Werten wird stattdessen eine leere, generische Inventar-GUI mit diesem Typ erstellt – rein für eigene Zwecke, ohne Vanilla-Funktionalität dahinter. Der Titel wird, sofern serverseitig unterstützt, nachträglich gesetzt.

## Wann einsetzen?

- Deko-Blöcke oder Möbel, die wie eine echte Werkbank, ein Amboss oder eine Schmelz-/Schneidestation nutzbar sein sollen, ohne einen echten Block dieses Typs zu platzieren
- Fantasy-Möbel (z. B. "Zauberpult"), die optisch anders aussehen, aber die Funktion eines Enchanting-Tisches übernehmen sollen
- Individuelle Menüs auf Basis eines generischen `InventoryType` (z. B. `CHEST`, `HOPPER`) als Grundlage für eigene GUIs

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.inventoryType.type` | `String` (Bukkit `InventoryType`) | `WORKBENCH` | **(Pflicht, aktiviert die Mechanic)** Name des zu öffnenden Inventar-Typs, z. B. `WORKBENCH`, `ANVIL`, `ENDER_CHEST`, `GRINDSTONE`, `CHEST`. Ungültiger Wert verhindert das Laden der Mechanic (Warnung im Log) |
| `Mechanics.inventoryType.title` | `String` (MiniMessage) | Standardtitel des Typs | Titel des geöffneten Inventars |

## Beispiel

```yaml
fantasy_workbench:
  Mechanics:
    inventoryType:
      type: WORKBENCH
      title: "<gold>Zauberwerkbank"

fantasy_enderchest:
  Mechanics:
    inventoryType:
      type: ENDER_CHEST
```

## Hinweise & Besonderheiten

- Bei Custom Blocks reagiert die Mechanic nur auf `RIGHT_CLICK_BLOCK` mit der Haupthand; bei Furniture auf jede Interaktion mit der Haupthand.
- Nur bei den genannten "echten" Typen (Werkbank, Amboss usw.) funktionieren Crafting/Reparatur/Verzaubern tatsächlich wie gewohnt – alle anderen Typen liefern nur eine leere GUI ohne Hintergrundlogik.
- Die Titel-Anpassung funktioniert nur, sofern die Serverversion das nachträgliche Setzen des Inventartitels unterstützt; andernfalls bleibt der Standardtitel des Inventartyps sichtbar.
