# <Mechanic-Name>

> Ein bis zwei Sätze: Was macht diese Mechanic in einem Satz, für wen ist sie gedacht.

| | |
|---|---|
| **Config-Key** | `Mechanics.<key>` |
| **Gilt für** | Item / Custom Block / Furniture |
| **Listener-Klasse** | `<Klasse>.<InnerListener>` |
| **Toggle/Sneak-Verhalten** | z. B. Rechtsklick zum Umschalten, immer aktiv, etc. |

## Was macht sie?

Kurzer Fließtext (3-6 Sätze): Kernverhalten, Trigger (Blockbruch, Rechtsklick, Interact, passiv, ...),
was am Ende passiert (Block bricht, Effekt wird angewendet, Item wird generiert, ...).
Keine Wiederholung der Tabelle oben.

## Wann einsetzen?

- Konkreter Anwendungsfall 1 (z. B. "Premium-Werkzeug im Shop")
- Konkreter Anwendungsfall 2
- Konkreter Anwendungsfall 3 (nur so viele wie sinnvoll, keine Füll-Bullets)

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.<key>.<option>` | `int` | `10` | ... |

> Nur die Keys, die der Loader tatsächlich liest (`ItemConfigUtil.java`). Pflicht-Keys (ohne die die Mechanic gar nicht lädt) markieren mit **(Pflicht)** in der Beschreibung.

## Beispiel

```yaml
mein_item:
  material: DIAMOND_PICKAXE
  Mechanics:
    <key>:
      option: wert
```

## Hinweise & Besonderheiten

- Wichtige Randfälle, Interaktionen mit anderen Mechanics, Performance-Hinweise, bekannte Einschränkungen
- Nur Punkte, die nicht selbsterklärend aus der Tabelle folgen
