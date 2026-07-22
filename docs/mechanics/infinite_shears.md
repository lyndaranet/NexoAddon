# InfiniteShears

> Verhindert den Haltbarkeitsverlust einer Schere bei jeder Aktion, die sie normalerweise beschädigen würde.

| | |
|---|---|
| **Config-Key** | `Mechanics.infinite_shears` |
| **Gilt für** | Item (Schere) |
| **Listener-Klasse** | `InfiniteShears.InfiniteShearsListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – greift automatisch bei jeder Beschädigung |

## Was macht sie?

Immer wenn das Item Haltbarkeit verlieren würde (`PlayerItemDamageEvent` – z. B. Schafe scheren, Spinnennetze/Stolperdraht durchtrennen), bricht die Mechanic das Event ab, sodass kein Durabilitätsschaden entsteht. Ist `uses` begrenzt, wird stattdessen ein Restzähler im PersistentDataContainer geführt und als Lore-Zeile angezeigt; bei 0 verbleibenden Nutzungen verliert die Schere wieder normal Haltbarkeit.

## Wann einsetzen?

- Farm-Werkzeuge (Schafscheren, Netz-Farmen), die dauerhaft einsatzbereit bleiben sollen
- Shop-/Rang-Item mit echtem Unendlich-Nutzen
- Abgestufte Belohnung mit begrenzter Nutzungszahl (`uses > 0`)

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.infinite_shears` | Abschnitt | – | **(Pflicht)** Allein das Vorhandensein dieses Abschnitts (auch leer) aktiviert die Mechanic beim Laden |
| `Mechanics.infinite_shears.enabled` | `bool` | `true` | Mechanic ein-/ausschalten, ohne die Konfiguration zu entfernen |
| `Mechanics.infinite_shears.uses` | `int` | `-1` | Maximale Nutzungen; `-1` = wirklich unendlich. Bei positivem Wert wird der Restzähler in der Lore angezeigt |

## Beispiel

```yaml
my_infinite_shears:
  Mechanics:
    infinite_shears:
      enabled: true
      uses: -1

my_limited_shears:
  Mechanics:
    infinite_shears:
      enabled: true
      uses: 50
```

## Hinweise & Besonderheiten

- Greift generisch bei jedem `PlayerItemDamageEvent` des Items – nicht nur beim Scheren von Schafen, sondern bei jeder Aktion, die Haltbarkeit kostet.
- Die "Verwendungen"-Lore-Zeile wird erst nach der **ersten** Benutzung aktualisiert/hinzugefügt, da die initiale Anzeige (`initUses`) aktuell im Code nirgends aufgerufen wird – der Zähler selbst funktioniert aber ab dem ersten Gebrauch korrekt.
- Der Restzähler wird pro Item-Stack im PersistentDataContainer gespeichert, nicht global pro Spieler.
