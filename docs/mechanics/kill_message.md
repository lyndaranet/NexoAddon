# Kill Message

> Ersetzt die Todesnachricht durch einen eigenen Text, wenn der Spieler mit diesem Item getötet wurde.

| | |
|---|---|
| **Config-Key** | `Mechanics.kill_message` |
| **Gilt für** | Item (Waffe) |
| **Listener-Klasse** | `KillMessage.KillMessageListener` |
| **Toggle/Sneak-Verhalten** | Keins – passiv, greift automatisch bei jedem Kill mit diesem Item |

## Was macht sie?

Beim Tod eines Spielers (`PlayerDeathEvent`) prüft die Mechanic, ob ein Killer existiert und welches Item dieser in der Haupthand hält. Trägt dieses Item eine `Mechanics.kill_message`-Konfiguration, wird die Standard-Todesnachricht durch den konfigurierten Text ersetzt. Die Platzhalter `<player>` (Opfer) und `<killer>` (Angreifer) werden durch die jeweiligen Spielernamen ersetzt, der Text wird anschließend als MiniMessage geparst (Farben/Formatierung möglich).

## Wann einsetzen?

- Signature-Waffen mit eigenem, thematischem Death-Message-Flair (z. B. "`<player> wurde von <killer>s Klinge zerschnitten`")
- Event-/Boss-Waffen, die Kills sichtbar hervorheben sollen
- Rollenspiel-Server, die Item-spezifische Immersion in den Chat bringen wollen

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.kill_message` | `String` | – | **(Pflicht)** Todesnachricht mit MiniMessage-Formatierung; unterstützt `<player>` und `<killer>` als Platzhalter |

## Beispiel

```yaml
frost_blade:
  material: DIAMOND_SWORD
  Mechanics:
    kill_message: "<gray><player> <white>wurde von <killer>s <blue>Frostklinge<white> eingefroren"
```

## Hinweise & Besonderheiten

- Greift nur, wenn der Killer existiert (z. B. nicht bei Fall- oder Umgebungsschaden) und das Item in der Haupthand zum Todeszeitpunkt eine Nexo-ID besitzt.
- Es wird immer die Waffe des **Killers** ausgewertet, nicht das Item des Opfers.
- Andere Death-Message-Plugins, die ebenfalls `PlayerDeathEvent#deathMessage()` setzen, können sich je nach Listener-Priorität gegenseitig überschreiben.
