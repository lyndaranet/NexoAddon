# Signal

> Ein kabelloses, redstone-artiges Signalsystem zwischen Furniture-Objekten: ein "Sender" schaltet per Rechtsklick alle "Empfänger" im Umkreis auf demselben Kanal um.

| | |
|---|---|
| **Config-Key** | `Mechanics.furniture.signal` |
| **Gilt für** | Furniture |
| **Listener-Klasse** | `Signal.SignalListener` |
| **Toggle/Sneak-Verhalten** | Rechtsklick (nur Haupthand) auf ein Sender-Furniture; Empfänger reagieren nicht auf eigene Interaktion |

## Was macht sie?

Furniture mit `role: TRANSMITTER` löst bei Rechtsklick einen Scan aller Luftblöcke im konfigurierten `radius` um sich herum aus. Für jeden gefundenen Furniture-Block wird geprüft, ob dieser ebenfalls eine `Mechanics.furniture.signal`-Konfiguration mit `role: RECEIVER` und demselben `channel`-Wert besitzt. Trifft das zu, wird dessen Licht-Zustand umgeschaltet (an/aus) – inklusive des dazugehörigen Licht-Pakets an die Clients. Ein Sender selbst hat keinen sichtbaren Effekt an sich selbst, er wirkt nur auf passende Empfänger in Reichweite.

## Wann einsetzen?

- Kabellose Lichtschalter/Ampelsysteme auf Furniture-Basis, ohne Redstone-Leitungen verlegen zu müssen
- Gekoppelte Deko-Objekte, die gemeinsam per Knopfdruck auf/ab schalten sollen (mehrere Empfänger auf demselben Kanal)
- Rätsel- oder Event-Mechaniken, bei denen ein Schalter mehrere Effekte gleichzeitig auslösen soll

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.furniture.signal.role` | `String` | – | **(Pflicht)** `TRANSMITTER` (löst beim Interagieren aus) oder `RECEIVER` (schaltet um, wenn erreicht) |
| `Mechanics.furniture.signal.channel` | `double` | – | **(Pflicht)** Kanalnummer; Sender und Empfänger müssen denselben Wert haben, um zu koppeln |
| `Mechanics.furniture.signal.radius` | `int` | `16` | Suchradius (in Blöcken, würfelförmig um den Sender) für die Empfänger-Suche |

## Beispiel

```yaml
wireless_switch:
  material: FURNITURE
  Mechanics:
    furniture:
      signal:
        role: TRANSMITTER
        channel: 1
        radius: 20

wireless_lamp:
  material: FURNITURE
  Mechanics:
    furniture:
      signal:
        role: RECEIVER
        channel: 1
```

## Hinweise & Besonderheiten

- Die Suche prüft **jeden Luftblock** im Würfel `radius`³ um den Sender – bei großem `radius` steigt die Serverlast pro Interaktion spürbar, daher nur so groß wie nötig wählen.
- `channel` ist ein `double`-Wert; identische Zahl bei Sender und Empfänger ist ausreichend, Nachkommastellen sind möglich aber unüblich.
- Nur `RECEIVER`-Furniture reagiert auf Sender-Signale; `TRANSMITTER`-Furniture selbst schaltet kein eigenes Licht um.
- Unabhängig von `block_aura`/`aura` – kann problemlos an denselben Furniture-Objekten kombiniert werden.
