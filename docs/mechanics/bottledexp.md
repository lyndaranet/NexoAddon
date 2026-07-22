# BottledExp

> Wandelt die gesamte Erfahrung des Spielers per Klick in Erfahrungsflaschen um, die am Boden landen.

| | |
|---|---|
| **Config-Key** | `Mechanics.bottledexp` |
| **Gilt für** | Item (Werkzeug, das in der Haupthand gehalten wird) |
| **Listener-Klasse** | `BottledExp.BottledExpListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – jeder Links-/Rechtsklick (Luft oder Block) mit dem Item in der Haupthand löst die Umwandlung sofort aus |

## Was macht sie?

Klickt der Spieler mit dem Item in der Haupthand (Linksklick Luft, Rechtsklick Luft oder Rechtsklick Block; Nebenhand wird ignoriert), berechnet die Mechanic aus dem aktuellen Level und XP-Fortschritt des Spielers über die Vanilla-XP-Formel eine Gesamterfahrung und rechnet diese mit dem konfigurierten `ratio` in eine Anzahl Erfahrungsflaschen (`EXPERIENCE_BOTTLE`) um. Die Flaschen werden am Standort des Spielers gedroppt, Level und XP des Spielers werden danach auf `0` zurückgesetzt. Ist das Ergebnis `0` oder weniger (zu wenig Erfahrung), passiert nichts außer einer Info-Meldung an den Spieler. Bei erfolgreicher Umwandlung wird zusätzlich, sofern `cost` größer `0` ist, die Durability des Items um `cost` erhöht.

## Wann einsetzen?

- "XP-Flaschenzieher"-Werkzeuge, mit denen Spieler ihre gesammelte Erfahrung schnell in handelbare Flaschen umwandeln können
- Shop-Items, die das Horten/Verkaufen von Erfahrung als Ressource ermöglichen
- Alternative zu Enchanting-Tischen, wenn Erfahrung als Wirtschaftsgut im Server-Konzept dienen soll

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.bottledexp.ratio` | `double` | `0.5` | **(Pflicht, aktiviert die Mechanic)** Umrechnungsverhältnis von Erfahrung zu Flaschenanzahl. Höherer Wert = mehr Flaschen pro Erfahrungspunkt |
| `Mechanics.bottledexp.cost` | `int` | `1` | Durability-Kosten pro erfolgreicher Umwandlung. `0` = kein Verschleiß des Items |

## Beispiel

```yaml
xp_extractor:
  material: GLASS_BOTTLE
  Mechanics:
    bottledexp:
      ratio: 0.5
      cost: 1
```

## Hinweise & Besonderheiten

- Die Meldung bei zu wenig Erfahrung stammt aus `config.yml` (`messages.bottledexp.not_enough_exp`), nicht aus dieser Mechanic-Konfiguration.
- Level und XP-Fortschritt werden bei jeder erfolgreichen Umwandlung komplett auf `0` gesetzt – es wird immer die gesamte aktuelle Erfahrung verwandelt, kein Teilbetrag.
- Der Durability-Schaden wird über ein reguläres `PlayerItemDamageEvent` ausgelöst und respektiert damit z. B. Unbreaking-Verzauberungen wie gewohnt.
