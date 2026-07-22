# Stackable

> Baut einen String-Block oder ein Furniture-Objekt per Rechtsklick mit einem passenden Item schrittweise zur nächsten Stufe aus (z. B. Stapel-/Aufbau-Stadien).

| | |
|---|---|
| **Config-Key** | `Mechanics.stackable` |
| **Gilt für** | Custom Block (nur String Block) und Furniture |
| **Listener-Klasse** | `Stackable.StackableListener` |
| **Toggle/Sneak-Verhalten** | Rechtsklick mit passendem Item, kein Sneak-Unterschied |

## Was macht sie?

Interagiert ein Spieler mit einem String Block oder Furniture, das `Mechanics.stackable` besitzt, wird geprüft, ob das gehaltene Item **ebenfalls** eine `stackable`-Konfiguration mit derselben `group` besitzt. Ist das der Fall (und Bauen an der Stelle laut Schutz-Plugin erlaubt), wird der aktuelle Block/das Furniture entfernt und durch die in `next` angegebene nächste Stufe ersetzt (bei String Blocks sofort, bei Furniture per zeitlosem `ShiftBlock`-Wechsel). Es wird ein Platzier-Sound abgespielt, der Spieler schwingt die Hand, und – sofern nicht im Kreativmodus – ein Item aus dem Stack in der Hand verbraucht.

## Wann einsetzen?

- Aufbaubare Deko-Stapel (z. B. Kisten-, Heu- oder Sandsackstapel, die mit jedem Rechtsklick höher werden)
- Ressourcen-"Fülle"-Mechaniken, bei denen ein Item schrittweise in einen Block eingearbeitet wird
- Baustufen-Systeme, bei denen mehrere Items derselben Gruppe zu einer gemeinsamen Kette von Stufen gehören

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.stackable.next` | `String` | – | **(Pflicht)** Nexo-ID der nächsten Stufe, die den aktuellen Block/das Furniture ersetzt |
| `Mechanics.stackable.group` | `String` | – | **(Pflicht)** Gruppenname; Block/Furniture und das verbrauchte Item müssen dieselbe `group` verwenden, damit sie zusammenpassen |

> Sowohl der Block/das Furniture als auch das dafür genutzte Item benötigen einen eigenen `Mechanics.stackable`-Eintrag mit derselben `group`. Das `next` des Items selbst wird dabei nicht ausgewertet, muss aber trotzdem gesetzt sein, damit der Loader die Mechanic überhaupt aktiviert (z. B. auf sich selbst verweisen lassen).

## Beispiel

```yaml
hay_stack_stage_1:
  material: FURNITURE
  Mechanics:
    stackable:
      next: hay_stack_stage_2
      group: hay_stack

hay_bale_item:
  material: HAY_BLOCK
  Mechanics:
    stackable:
      next: hay_bale_item
      group: hay_stack
```

## Hinweise & Besonderheiten

- Funktioniert bei Custom Blocks ausschließlich mit **String Blocks** – Note- oder Chorus-Blöcke werden von dieser Mechanic nicht unterstützt (siehe `shiftblock` für breitere Block-Familien-Unterstützung).
- Gegenstück ist `unstackable` (siehe `unstackable.md`) – damit lassen sich Stufen wieder abbauen.
- Bei Furniture gilt ein kurzes Cooldown von 3 Ticks pro Furniture-Instanz, um Doppel-Trigger durch schnelles Klicken zu verhindern.
- Respektiert Schutz-Plugins über `ProtectionLib.canBuild`.
