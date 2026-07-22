# Unstackable

> Baut einen String-Block oder ein Furniture-Objekt per Rechtsklick wieder eine Stufe ab und gibt dem Spieler dafür ein Item zurück.

| | |
|---|---|
| **Config-Key** | `Mechanics.unstackable` |
| **Gilt für** | Custom Block (nur String Block) und Furniture |
| **Listener-Klasse** | `Unstackable.UnstackableListener` |
| **Toggle/Sneak-Verhalten** | Rechtsklick in der Haupthand, kein Sneak-Unterschied |

## Was macht sie?

Interagiert ein Spieler (nur Haupthand) mit einem String Block oder Furniture, das `Mechanics.unstackable` besitzt, wird die aktuelle Stufe entfernt und – sofern `next` nicht `"stop"` ist – durch die vorherige/niedrigere Stufe aus `next` ersetzt. Anschließend erhält der Spieler das in `give` konfigurierte Item (Vanilla-Material oder Nexo-Item) in sein Inventar. Optional kann über `items` eingeschränkt werden, welches Werkzeug/Item der Spieler dafür in der Hand halten muss. Die Aktion wird zusätzlich verweigert, wenn der Spieler gerade ein Item derselben `group` wie das eigene `stackable` dieses Blocks hält, um Konflikte mit der Aufbau-Interaktion (`stackable`) zu vermeiden.

## Wann einsetzen?

- Gegenstück zu `stackable`-Stapeln (siehe `stackable.md`), um Baustufen wieder Schritt für Schritt abzubauen und das ursprüngliche Item zurückzugeben
- Abbaubare Ressourcen-"Entnahme" aus einem mehrstufigen Block/Furniture (z. B. Heuballen einzeln vom Stapel nehmen)
- Werkzeuggebundenes Abbauen einzelner Stufen, wenn nur bestimmte Items/Werkzeuge das Abbauen auslösen dürfen

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.unstackable.next` | `String` | – | **(Pflicht)** Nexo-ID der nächsten (niedrigeren) Stufe, oder `"stop"` um den Block/das Furniture ganz zu entfernen ohne neue Stufe zu platzieren |
| `Mechanics.unstackable.give` | `String` | – | **(Pflicht)** Vanilla-Material oder Nexo-Item-ID, das dem Spieler beim Abbauen gegeben wird |
| `Mechanics.unstackable.items` | `List<String>` | `[]` (leer) | Vanilla-Materials oder Nexo-IDs, die der Spieler in der Haupthand halten muss, um die Aktion auszulösen; leer = kein Werkzeugzwang |

## Beispiel

```yaml
hay_stack_stage_2:
  material: FURNITURE
  Mechanics:
    unstackable:
      next: hay_stack_stage_1
      give: hay_bale_item
      items:
        - SHEARS
```

## Hinweise & Besonderheiten

- Funktioniert bei Custom Blocks ausschließlich mit **String Blocks** – Note- oder Chorus-Blöcke werden nicht unterstützt.
- Wird der Block/das Furniture gerade mit einem Item derselben `stackable`-Gruppe angeklickt, wird `unstackable` ignoriert, damit sich Auf- und Abbau nicht gegenseitig blockieren.
- Bei Furniture gilt ein kurzes Cooldown von 3 Ticks pro Instanz gegen Doppel-Trigger.
- Respektiert Schutz-Plugins über `ProtectionLib.canInteract`.
