# InfiniteFluidBucket

> Ein einzelnes Item, das per Rechtsklick zwischen Wasser- und Lava-Modus umschaltet und die aktuelle Flüssigkeit unendlich (oder begrenzt) platziert.

| | |
|---|---|
| **Config-Key** | `Mechanics.infinite_fluid_bucket` |
| **Gilt für** | Item (kombinierter Wasser-/Lava-Eimer) |
| **Listener-Klasse** | `InfiniteFluidBucket.InfiniteFluidBucketListener` |
| **Toggle/Sneak-Verhalten** | Rechtsklick in die Luft schaltet zwischen Wasser- und Lava-Modus um |

## Was macht sie?

Rechtsklick in die Luft wechselt den im PersistentDataContainer gespeicherten Modus (`WATER`/`LAVA`) und aktualisiert die entsprechende Lore-Zeile (`water_lore`/`lava_lore`) am Item. Rechtsklick auf einen Block platziert die aktuell gewählte Flüssigkeit auf der Zielfläche (nur wenn diese Luft, Wasser oder Lava ist), ohne dass ein separater Eimer benötigt wird. Ist `uses` begrenzt, wird bei jedem Platzieren ein Restzähler im PersistentDataContainer heruntergezählt und in der Lore angezeigt.

## Wann einsetzen?

- Allrounder-Werkzeug für Baumeister, die sich Slots für separate Wasser-/Lava-Eimer sparen wollen
- Shop-Item mit unbegrenzten Nutzungen als Premium-Alternative zu den Einzel-Eimer-Mechaniken
- Event-/Belohnungsgegenstand mit begrenzter Nutzungszahl

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.infinite_fluid_bucket` | Abschnitt | – | **(Pflicht)** Allein das Vorhandensein dieses Abschnitts (auch leer) aktiviert die Mechanic beim Laden |
| `Mechanics.infinite_fluid_bucket.enabled` | `bool` | `true` | Mechanic ein-/ausschalten, ohne die Konfiguration zu entfernen |
| `Mechanics.infinite_fluid_bucket.uses` | `int` | `-1` | Maximale Nutzungen; `-1` = wirklich unendlich. Bei positivem Wert wird der Restzähler in der Lore angezeigt |
| `Mechanics.infinite_fluid_bucket.water_lore` | `String` (MiniMessage) | `"<aqua>Modus: Wasser"` | Lore-Zeile, die angezeigt wird, solange das Item im Wasser-Modus ist |
| `Mechanics.infinite_fluid_bucket.lava_lore` | `String` (MiniMessage) | `"<red>Modus: Lava"` | Lore-Zeile, die angezeigt wird, solange das Item im Lava-Modus ist |

## Beispiel

```yaml
my_infinite_fluid_bucket:
  lore:
    - "<aqua>Modus: Wasser"
  Mechanics:
    infinite_fluid_bucket:
      enabled: true
      uses: -1
      water_lore: "<aqua>Modus: Wasser"
      lava_lore: "<red>Modus: Lava"
```

## Hinweise & Besonderheiten

- Standard-Modus eines neuen Items ist immer `WATER`, solange noch kein Modus im PersistentDataContainer gespeichert wurde.
- Der Moduswechsel ersetzt die passende Lore-Zeile in-place: Ist bereits eine `water_lore`- oder `lava_lore`-Zeile vorhanden, wird diese ausgetauscht; sonst wird eine neue Zeile angehängt. Es ist daher sinnvoll, eine der beiden Lore-Zeilen direkt im Nexo-Item als Standard-Lore zu hinterlegen (siehe Beispiel).
- Rechtsklick auf einen Block funktioniert nur, wenn die Zielfläche (Block in Blickrichtung vom angeklickten Block aus) Luft, Wasser oder Lava ist – auf soliden Blöcken passiert nichts.
- Respektiert Schutz-Plugins über `ProtectionLib.canBuild`.
- Die "Verwendungen"-Lore-Zeile wird erst nach der **ersten** Platzierung aktualisiert/hinzugefügt, da die initiale Anzeige (`initUses`) aktuell im Code nirgends aufgerufen wird – der Zähler selbst funktioniert aber ab der ersten Nutzung korrekt.
