# UniqueId

> Vergibt jedem Exemplar dieses Items automatisch eine eigene, dauerhafte UUID.

| | |
|---|---|
| **Config-Key** | `Mechanics.uniqueid` |
| **Gilt für** | Item |
| **Listener-Klasse** | `UniqueId.UniqueIdListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – läuft passiv im Hintergrund, solange `enabled: true` |

## Was macht sie?

Sobald ein Item mit dieser Mechanic in einem Spielerinventar auftaucht – durch Aufheben vom Boden, durch einen Slot-Wechsel (z. B. Crafting-Ergebnis, Verschieben) oder durch Klicken/Ziehen in einem Inventar – prüft die Mechanic, ob das Item bereits eine UUID im PersistentDataContainer (`unique_id`) trägt. Fehlt sie, wird eine neue, zufällige UUID vergeben und dauerhaft im Item gespeichert. Bereits getaggte Items werden nicht verändert. Dadurch erhält jedes einzelne Exemplar eines Items eine unverwechselbare, technische Kennung.

## Wann einsetzen?

- Items, die pro Spieler oder pro Ausstellungsstück eindeutig nachverfolgbar sein müssen (z. B. Auktionshaus, Trophäen, signierte Items)
- Anti-Dupe-/Tracking-Zwecke, bei denen einzelne Item-Instanzen unterschieden werden müssen
- Quest- oder Event-Items, die an eine bestimmte Instanz gebunden werden sollen statt an den Item-Typ allgemein

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.uniqueid.enabled` | `bool` | `true` | Ob eine UUID vergeben werden soll. `false` deaktiviert die Vergabe, obwohl der Abschnitt existiert |

> Der Abschnitt `Mechanics.uniqueid` muss lediglich existieren, damit die Mechanic geladen wird (der Wert von `enabled` entscheidet danach, ob sie aktiv ist).

## Beispiel

```yaml
signed_trophy:
  material: NETHERITE_INGOT
  Mechanics:
    uniqueid:
      enabled: true
```

## Hinweise & Besonderheiten

- Da jedes Exemplar eine unterschiedliche UUID im PersistentDataContainer trägt, stapeln sich einzelne Items dieses Typs in der Regel **nicht** mehr im Inventar, sobald sie getaggt wurden – wichtig bei Items, die normalerweise stapelbar wären.
- Die UUID wird nie überschrieben oder neu vergeben, sobald sie einmal gesetzt ist – auch nicht nach Serverneustart.
- Die Vergabe passiert an mehreren Stellen (Aufheben, Slot-Wechsel, Klick, Drag), um möglichst sicherzustellen, dass kein Exemplar ohne UUID durchrutscht.
