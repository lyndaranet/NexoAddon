# InfiniteBucket

> Lässt einen Wasser- oder Lava-Eimer Flüssigkeit platzieren, ohne dass der Eimer selbst verbraucht (zu einem leeren Eimer) wird.

| | |
|---|---|
| **Config-Key** | `Mechanics.infinite_bucket` |
| **Gilt für** | Item (Wasser- oder Lava-Eimer) |
| **Listener-Klasse** | `InfiniteBucket.InfiniteBucketListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – greift automatisch bei jedem Ausleeren |

## Was macht sie?

Beim Ausleeren eines Wasser- oder Lava-Eimers (`PlayerBucketEmptyEvent`) setzt die Mechanic die Flüssigkeit manuell an der Zielposition, bricht das Vanilla-Event ab und gibt dem Spieler den vollen Eimer zurück, statt ihn wie normal gegen einen leeren Eimer zu tauschen. Das Material des Eimers (`WATER_BUCKET`/`LAVA_BUCKET`) bestimmt die platzierte Flüssigkeit. Ist `uses` begrenzt, wird ein Restzähler im PersistentDataContainer des Items geführt und als Lore-Zeile angezeigt; bei 0 verbleibenden Nutzungen verhält sich der Eimer wieder wie ein normaler Eimer.

## Wann einsetzen?

- Baumeister-Werkzeuge/-Kits, bei denen ständiges Nachfüllen von Eimern stört
- Shop-/Rang-Item mit echtem Unendlich-Nutzen
- Belohnungsgegenstand mit begrenzter Anzahl an Nutzungen (`uses > 0`) als abgestufte Alternative

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.infinite_bucket` | Abschnitt | – | **(Pflicht)** Allein das Vorhandensein dieses Abschnitts (auch leer) aktiviert die Mechanic beim Laden |
| `Mechanics.infinite_bucket.enabled` | `bool` | `true` | Mechanic ein-/ausschalten, ohne die Konfiguration zu entfernen |
| `Mechanics.infinite_bucket.uses` | `int` | `-1` | Maximale Nutzungen; `-1` = wirklich unendlich. Bei positivem Wert wird der Restzähler in der Lore angezeigt |

## Beispiel

```yaml
my_infinite_water_bucket:
  Mechanics:
    infinite_bucket:
      enabled: true
      uses: -1

my_limited_water_bucket:
  Mechanics:
    infinite_bucket:
      enabled: true
      uses: 20
```

## Hinweise & Besonderheiten

- Funktioniert nur bei `WATER_BUCKET`/`LAVA_BUCKET`-Materialien; andere Eimer-Typen (z. B. Milch, Fisch-Eimer) werden ignoriert.
- Respektiert Schutz-Plugins über `ProtectionLib.canBuild` – in geschützten Bereichen passiert nichts.
- Die "Verwendungen"-Lore-Zeile wird erst nach der **ersten** Benutzung des Eimers hinzugefügt/aktualisiert, da die initiale Anzeige (`initUses`) aktuell im Code nirgends aufgerufen wird – der Zähler selbst funktioniert aber ab dem ersten Gebrauch korrekt.
- Der Restzähler wird pro Item-Stack im PersistentDataContainer gespeichert, nicht global pro Spieler.
