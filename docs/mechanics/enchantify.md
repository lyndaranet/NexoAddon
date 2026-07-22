# Enchantify

> Überträgt fest konfigurierte Verzauberungen auf ein anderes Item, indem der Spieler das Enchantify-Item per Linksklick im Inventar darauf "ablegt".

| | |
|---|---|
| **Config-Key** | `Mechanics.enchantify` |
| **Gilt für** | Item (das Verzauberungs-Medium, nicht das Ziel-Item) |
| **Listener-Klasse** | `Enchantify.EnchantifyListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle – Linksklick im Inventar mit dem Enchantify-Item auf dem Cursor über einem Ziel-Item löst die Verzauberung direkt aus |

## Was macht sie?

Die Mechanic hängt an einem Verzauberungs-Item (z. B. eine "Verzauberungsessenz"). Hält der Spieler dieses Item mit dem Cursor in einem Inventar und klickt links auf ein anderes Item, werden die konfigurierten Verzauberungen auf das Ziel-Item angewendet – dabei werden Level addiert (bestehendes Level + konfiguriertes Level), optional gedeckelt durch ein pro Verzauberung konfigurierbares `limit`. Die Verzauberung wird ohne Rücksicht auf Vanilla-Levelgrenzen aufgetragen (Safe-Enchant-Beschränkungen werden ignoriert). Das Enchantify-Item auf dem Cursor wird dabei immer um 1 Stück reduziert (unabhängig von eigener Durability).

## Wann einsetzen?

- Verzauberungsessenzen/-kristalle im Shop, die gezielt eine oder mehrere feste Verzauberungen aufbringen, ohne Verzauberungstisch oder Amboss
- Progressions-Items, die eine Verzauberung schrittweise bis zu einem Maximal-Level steigern (`limit`)
- Einschränkung, auf welche Item-Typen die Verzauberung überhaupt anwendbar ist (Whitelist/Blacklist)

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.enchantify.enchants` | `List<Map>` | – | **(Pflicht, aktiviert die Mechanic)** Liste der aufzubringenden Verzauberungen. Jeder Eintrag hat die Felder `enchant` (Name, z. B. `sharpness`, ohne Namespace-Präfix wird automatisch `minecraft:` vorangestellt), `level` (Level, das addiert wird; Einträge mit `level <= 0` werden ignoriert) und optional `limit` (maximales Gesamt-Level für diese Verzauberung) |
| `Mechanics.enchantify.whitelist` | `List<String>` | `[]` (leer) | Vanilla-Materials oder Nexo-IDs, auf die die Verzauberung angewendet werden darf. Leer = alle Items erlaubt (sofern nicht per Blacklist ausgeschlossen) |
| `Mechanics.enchantify.blacklist` | `List<String>` | `[]` (leer) | Vanilla-Materials oder Nexo-IDs, die von der Verzauberung ausgeschlossen sind. Hat Vorrang vor der Whitelist |

## Beispiel

```yaml
sharpness_essence:
  material: NETHER_STAR
  Mechanics:
    enchantify:
      enchants:
        - enchant: sharpness
          level: 1
          limit: 5
        - enchant: unbreaking
          level: 1
          limit: 3
      whitelist:
        - DIAMOND_SWORD
        - NETHERITE_SWORD
```

## Hinweise & Besonderheiten

- Das Ziel-Item darf selbst **kein** eigenes `enchantify`-Mechanic besitzen – so lässt sich kein Verzauberungs-Item auf ein anderes anwenden.
- Ohne `limit` für eine Verzauberung ist das erreichbare Level unbegrenzt – bei wiederholter Anwendung steigt das Level immer weiter.
- Nur Linksklicks lösen die Übertragung aus; die Verzauberung greift in jedem Inventar mit `InventoryClickEvent`, nicht nur im Spielerinventar.
