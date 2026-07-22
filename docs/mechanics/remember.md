# Remember

> Merkt sich Anzeigename und Lore des platzierten Items an einem Furniture-Objekt und gibt beim Abbauen ein Item mit denselben Daten zurück, statt des generischen Standard-Drops.

| | |
|---|---|
| **Config-Key** | `Mechanics.furniture.remember` |
| **Gilt für** | Furniture |
| **Listener-Klasse** | `Remember.RememberListener` |
| **Toggle/Sneak-Verhalten** | Keins – passiv, greift automatisch bei Platzieren und Abbauen |

## Was macht sie?

Beim Platzieren eines Furnitures mit `Mechanics.furniture.remember` werden Anzeigename und Lore des platzierten Items (als MiniMessage-String) im PersistentDataContainer des Furniture-Objekts gespeichert. Wird das Furniture später abgebaut, wird der reguläre Drop unterdrückt; stattdessen wird ein neues Item derselben Nexo-ID erzeugt, dem Name und Lore aus dem PDC wieder zugewiesen werden, bevor es als Loot gedroppt wird.

## Wann einsetzen?

- Individualisierbare/umbenennbare Deko-Furniture (z. B. beschriftete Schilder, Trophäen, personalisierte Möbel), deren Beschriftung beim Umsetzen erhalten bleiben soll
- Items mit wertvoller oder seltener Lore (z. B. Event-Editionen), die nach dem Platzieren als Furniture nicht ihre Identität verlieren dürfen
- Server mit Umzugs-/Housing-Systemen, bei denen Spieler Möbel versetzen und dabei individuelle Beschriftungen behalten sollen

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.furniture.remember` | `bool` | `true` | **(Pflicht zum Aktivieren, sobald der Key gesetzt ist)** Ob Name/Lore beim Platzieren gespeichert und beim Abbauen wiederhergestellt werden |

## Beispiel

```yaml
memorial_statue:
  material: FURNITURE
  Mechanics:
    furniture:
      remember: true
```

## Hinweise & Besonderheiten

- Gespeichert werden nur Anzeigename und Lore – andere Item-Metadaten (Enchantments, Custom-Model-Data, NBT abseits von Name/Lore) werden nicht übernommen.
- Hat das platzierte Item keinen eigenen Namen bzw. keine Lore, wird für den jeweiligen Wert schlicht nichts gespeichert; das zurückgegebene Item nutzt dann die Standardwerte der Nexo-ID.
- Funktioniert ausschließlich mit Furniture, nicht mit Custom Blocks.
