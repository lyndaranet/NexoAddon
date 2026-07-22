# InfiniteFood

> Lässt ein Lebensmittel-Item normal gegessen werden (Hunger/Sättigung, Effekte), ohne dass es aus dem Inventar verschwindet.

| | |
|---|---|
| **Config-Key** | `Mechanics.infinite_food` |
| **Gilt für** | Item (Nahrungsmittel) |
| **Listener-Klasse** | `InfiniteFood.InfiniteFoodListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – greift automatisch beim Essen |

## Was macht sie?

Beim Verzehr eines Items (`PlayerItemConsumeEvent`) ersetzt die Mechanic das konsumierte Item nach dem Essen durch eine Kopie des ursprünglichen Stacks (`event.setReplacement(...)`), sodass Hunger/Sättigung und alle Vanilla-Effekte normal angewendet werden, das Item selbst aber erhalten bleibt. Ist `uses` begrenzt, wird ein Restzähler im PersistentDataContainer geführt und als Lore-Zeile angezeigt; bei 0 verbleibenden Nutzungen wird das Item beim nächsten Essen ganz normal verbraucht.

## Wann einsetzen?

- Premium-Nahrung im Shop, die nie ausgeht (z. B. "unendlicher Goldapfel")
- Event-/Quest-Belohnung mit begrenzter Anzahl an Nutzungen
- Testing-/Admin-Items zum schnellen Wiederauffüllen von Hunger ohne Inventar-Verwaltung

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.infinite_food` | Abschnitt | – | **(Pflicht)** Allein das Vorhandensein dieses Abschnitts (auch leer) aktiviert die Mechanic beim Laden |
| `Mechanics.infinite_food.enabled` | `bool` | `true` | Mechanic ein-/ausschalten, ohne die Konfiguration zu entfernen |
| `Mechanics.infinite_food.uses` | `int` | `-1` | Maximale Nutzungen; `-1` = wirklich unendlich. Bei positivem Wert wird der Restzähler in der Lore angezeigt |

## Beispiel

```yaml
my_infinite_steak:
  Mechanics:
    infinite_food:
      enabled: true
      uses: -1

my_limited_golden_apple:
  Mechanics:
    infinite_food:
      enabled: true
      uses: 5
```

## Hinweise & Besonderheiten

- Funktioniert für Haupt- **und** Nebenhand (`EquipmentSlot.HAND`/`OFF_HAND`).
- Der Ersatz erfolgt synchron im selben Tick (`setReplacement`), um Race Conditions bei Server-Wechseln (z. B. Velocity) zu vermeiden.
- Die "Verwendungen"-Lore-Zeile wird erst nach der **ersten** Benutzung aktualisiert/hinzugefügt, da die initiale Anzeige (`initUses`) aktuell im Code nirgends aufgerufen wird – der Zähler selbst funktioniert aber ab dem ersten Gebrauch korrekt.
- Der Restzähler wird pro Item-Stack im PersistentDataContainer gespeichert, nicht global pro Spieler.
