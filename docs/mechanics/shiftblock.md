# Shiftblock

> Tauscht einen Custom Block oder ein Furniture-Objekt temporär (oder dauerhaft) gegen eine andere Nexo-ID aus, ausgelöst durch Platzieren, Interagieren oder Abbauen.

| | |
|---|---|
| **Config-Key** | `Mechanics.shiftblock` |
| **Gilt für** | Custom Block (Note-, String- und Chorus-Block) sowie Furniture |
| **Listener-Klasse** | `ShiftBlock.ShiftBlockListener` |
| **Toggle/Sneak-Verhalten** | Kein Sneak-Unterschied; Auslöser sind Platzieren/Interagieren/Abbauen, je nach Konfiguration |

## Was macht sie?

Löst einer der aktivierten Trigger (`on_place`, `on_interact`, `on_break`) aus, wird der Block/das Furniture sofort durch die in `replace_to` angegebene Nexo-ID ersetzt (bei Interaktion und Abbau wird das Standard-Event zusätzlich abgebrochen, damit der Original-Block nicht regulär abgebaut/gedroppt wird). Ist `time` größer als 0, wird nach dieser Zeit (in Sekunden) automatisch zurück zur ursprünglichen Block-ID gewechselt; ist `time` gleich 0, bleibt der Tausch dauerhaft bestehen. Über `items` lässt sich optional festlegen, dass der Wechsel nur mit bestimmten Werkzeugen/Items in der Hand ausgelöst wird.

## Wann einsetzen?

- Temporäre Effekt-Blöcke: z. B. eine Falle/Druckplatte, die sich kurzzeitig in einen anderen Zustand verwandelt und danach zurückwechselt
- Interaktive Deko wie Schalter, Hebel oder Türen auf Custom-Block-/Furniture-Basis, die per Rechtsklick optisch umschalten
- Grundbaustein für andere Mechaniken im Addon (`stackable`/`unstackable` nutzen intern dieselbe Wechsel-Logik)

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.shiftblock.replace_to` | `String` | – | **(Pflicht)** Nexo-ID, zu der beim Auslösen gewechselt wird |
| `Mechanics.shiftblock.time` | `int` | `200` | **(Pflicht, aktiviert die Mechanic zusammen mit `replace_to`)** Zeit in Sekunden bis zum Zurückwechseln; `0` = dauerhafter Wechsel ohne Rückwechsel |
| `Mechanics.shiftblock.items` | `List<String>` | `[]` (leer) | Vanilla-Materials oder Nexo-IDs, die zum Auslösen in der Hand gehalten werden müssen; leer = keine Einschränkung |
| `Mechanics.shiftblock.on_interact` | `bool` | `true` | Ob Rechtsklick (Interact, nur Haupthand) den Wechsel auslöst |
| `Mechanics.shiftblock.on_break` | `bool` | `false` | Ob Abbauen des Blocks/Furnitures den Wechsel auslöst (statt es regulär zu zerstören) |
| `Mechanics.shiftblock.on_place` | `bool` | `false` | Ob das Platzieren den Wechsel auslöst |

## Beispiel

```yaml
secret_lever:
  material: FURNITURE
  Mechanics:
    shiftblock:
      replace_to: secret_lever_active
      time: 5
      on_interact: true
      on_break: false
      on_place: false
```

## Hinweise & Besonderheiten

- Deckt anders als `stackable`/`unstackable` alle drei Custom-Block-Familien ab: Note-, String- und Chorus-Blöcke sowie Furniture.
- Während ein Wechsel läuft, wird der Standort intern als "in Bearbeitung" markiert (`processedShiftblocks`), damit derselbe Block/Furniture nicht mehrfach gleichzeitig getriggert wird.
- Bricht der ursprüngliche Trigger-Block zwischenzeitlich weg oder wird anderweitig verändert, wird der geplante Rückwechsel automatisch abgebrochen, statt einen falschen Block zu platzieren.
- `stackable` und `unstackable` verwenden für ihre eigenen Block-/Furniture-Wechsel intern dieselbe Utility-Funktion wie `shiftblock`, sind aber eigenständige, separat konfigurierbare Mechaniken.
- **Bekannte Einschränkung:** Die Werkzeug-Prüfung über `items` verknüpft Material- und Nexo-ID-Treffer aktuell mit UND statt ODER. Praktisch bedeutet das: Eine reine Vanilla-Material-Liste (ohne Nexo-IDs) oder eine reine Nexo-ID-Liste (ohne Materials) wird derzeit **nicht durchgesetzt** – die Aktion löst dann unabhängig vom gehaltenen Item aus. Zuverlässig eingeschränkt wird nur, wenn `items` sowohl mindestens ein Vanilla-Material als auch mindestens eine Nexo-ID enthält. Für eine harte Werkzeug-Bindung im Zweifel lieber testen oder das Entwicklerteam auf `ShiftBlockListener` ansprechen.
