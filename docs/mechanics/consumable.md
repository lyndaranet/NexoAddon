# Consumable

> Generische Nutzungs-Mechanic für Verbrauchsgegenstände: Sofortheilung/-schaden, Potion-Effekte, Befehle, Nachrichten und Feedback bei Essen oder Rechtsklick.

| | |
|---|---|
| **Config-Key** | `Mechanics.consumable` |
| **Gilt für** | Item (Nahrung, Trank, Schriftrolle, Rune o. ä.) |
| **Listener-Klasse** | `ConsumableMechanic.ConsumableMechanicListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – Trigger-basiert (`eat`, `right_click`, `right_click_block`), pro Spieler durch Cooldown gesperrt |

`consumable` ist die generische "Nutzungs-Mechanic" des MMO-Ability-Baukastens für Verbrauchsgegenstände – mit denselben Grundbausteinen (Cooldown, Conditions, Effects) wie die right_click-Abilities, aber eigenständig statt über das gemeinsame Ability-System konfiguriert.

## Was macht sie?

Je nach `trigger` reagiert die Mechanic auf `PlayerItemConsumeEvent` (`trigger: eat` – Minecraft verzehrt das Item dabei bereits selbst) oder `PlayerInteractEvent` (`trigger: right_click`/`right_click_block`, nur Hauptvhand). Sind die `conditions` erfüllt und kein Cooldown aktiv, wird optional 1 Item vom Stack entfernt (`consume_item`), Sofortheilung/-schaden angewendet, die konfigurierten Potion-Effekte (jeweils mit eigener `chance`) vergeben, Konsolenbefehle ausgeführt, Nachrichten verschickt sowie Sound/Partikel abgespielt. Bei nicht erfüllten Bedingungen oder aktivem Cooldown wird die Aktion abgebrochen und eine ActionBar-Meldung angezeigt.

## Wann einsetzen?

- Heiltränke und Buff-Runen/Schriftrollen
- Spezial-Nahrung mit Zufallseffekt (z. B. Gamble-Items)
- Item-Belohnungen, die Coins vergeben oder Befehle auslösen

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.consumable.trigger` | `String` | `"right_click"` | Auslöser: `eat`, `right_click`, `right_click_block` |
| `Mechanics.consumable.cooldown` | `int` | `0` | Cooldown in Sekunden pro Spieler (0 = kein Cooldown) |
| `Mechanics.consumable.consume_item` | `bool` | `false` | Ob 1 Item vom Stack entfernt wird (bei `trigger: eat` ohne zusätzliche Wirkung, da Vanilla das Item bereits verzehrt) |
| `Mechanics.consumable.instant_heal` | `double` | `0.0` | Sofortige Heilung in Health-Punkten (max. 20.0 = volle Herzleiste) |
| `Mechanics.consumable.instant_damage` | `double` | `0.0` | Sofortiger Schaden in Health-Punkten |
| `Mechanics.consumable.effects` | `List<Map>` | `[]` | Potion-Effekte, s. Tabelle unten |
| `Mechanics.consumable.commands` | `List<String>` | `[]` | Konsolenbefehle (Platzhalter `{player}`), werden 1 Tick verzögert ausgeführt |
| `Mechanics.consumable.conditions.require_sneaking` | `bool` | `false` | Nur nutzbar während Schleichen (auch flach als `require_sneaking` ohne `conditions:`-Wrapper möglich) |
| `Mechanics.consumable.conditions.require_health_below` | `int` | `100` | Nur nutzbar, wenn HP in Prozent ≤ diesem Wert liegt |
| `Mechanics.consumable.conditions.require_permission` | `String` | `""` | Nur nutzbar mit dieser Permission |
| `Mechanics.consumable.sound` | `String` | keiner | Sound bei erfolgreicher Nutzung |
| `Mechanics.consumable.particle` | `String` | keiner | Partikel bei erfolgreicher Nutzung |
| `Mechanics.consumable.message_self` | `String` | `""` | Nachricht an den Nutzer (MiniMessage) |
| `Mechanics.consumable.message_broadcast` | `String` | `""` | Server-weite Broadcast-Nachricht (Platzhalter `{player}`) |

**Felder pro `effects`-Eintrag:**

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `type` | `String` | – | **(Pflicht)** PotionEffectType |
| `amplifier` | `int` | `0` | Effektstufe (0 = Stufe I) |
| `duration` | `int` | `1` | Dauer in Sekunden |
| `chance` | `double` | `1.0` | Wahrscheinlichkeit (0.0–1.0), mit der dieser Effekt vergeben wird |

## Beispiel

```yaml
HEILUNGSTRANK:
  displayname: "<red>Heilungstrank"
  Mechanics:
    consumable:
      trigger: right_click
      cooldown: 30
      consume_item: true
      instant_heal: 8.0
      conditions:
        require_health_below: 70
      sound: ENTITY_GENERIC_DRINK
      particle: HEART
      message_self: "<green>Du trinkst den Trank und spürst wie deine Wunden schließen."

VERFLUCHTES_BROT:
  displayname: "<dark_green>Verfluchtes Brot"
  Mechanics:
    consumable:
      trigger: eat
      cooldown: 0
      instant_heal: 3.0
      effects:
        - type: SATURATION
          amplifier: 0
          duration: 10
        - type: POISON
          amplifier: 1
          duration: 4
          chance: 0.25
```

## Hinweise & Besonderheiten

- `conditions` können sowohl verschachtelt (`conditions.require_sneaking`) als auch direkt auf oberster Ebene (`require_sneaking`) angegeben werden – die verschachtelte Variante hat Vorrang, falls beide gesetzt sind.
- Bei `trigger: eat` konsumiert Minecraft das Item bereits selbst; `consume_item` hat dort keine zusätzliche Wirkung.
- Cooldown und Bedingungen gelten pro Spieler global über alle `consumable`-Items hinweg (gemeinsame Cooldown-Map), nicht pro Item-Typ.
- `effects` mit `chance < 1.0` werden individuell gewürfelt – mehrere Effekte in einer Liste sind unabhängig voneinander.
