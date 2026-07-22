# MiningTools

> Beschränkt einen Nexo-Custom-Block darauf, nur mit bestimmten Werkzeugen abgebaut werden zu können.

| | |
|---|---|
| **Config-Key** | `Mechanics.custom_block.miningtools` |
| **Gilt für** | Custom Block (Nexo Noteblock- und Chorusblock-Mechanic) |
| **Listener-Klasse** | `MiningTools.MiningToolsListener` |
| **Toggle/Sneak-Verhalten** | Kein Toggle, immer aktiv |

## Was macht sie?

Wird ein Nexo-Custom-Block mit dieser Mechanic abgebaut, prüft der Listener, ob das in der Hand gehaltene Werkzeug in `items` (Vanilla-Material oder Nexo-Item-ID) enthalten ist. Ist das der Fall, passiert nichts Weiteres – der Block bricht normal. Ist das Werkzeug **nicht** erlaubt, wird das Abbau-Event abgebrochen. Steht `type` auf `CANCEL_DROP`, wird der Block zusätzlich manuell ohne jeglichen Loot entfernt und dem Werkzeug wird 1 Haltbarkeitspunkt abgezogen; bei `CANCEL_EVENT` (Standard) bleibt der Block einfach unversehrt stehen.

## Wann einsetzen?

- Custom-Blöcke, die nur mit einem bestimmten Spezialwerkzeug (z. B. eigener Nexo-Bohrer) abbaubar sein sollen
- Ressourcen-Blöcke, bei denen falsches Werkzeug den Block "zerstören" soll ohne Drop, statt ihn einfach stehen zu lassen (`CANCEL_DROP`)
- Zugangsbeschränkung für High-Tier-Blöcke im Shop-/Rift-System

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.custom_block.miningtools.items` | `List<String>` | – | **(Pflicht, aktiviert die Mechanic)** Vanilla-Materials oder Nexo-Item-IDs, die als Werkzeug zum Abbau erlaubt sind |
| `Mechanics.custom_block.miningtools.type` | `String` | `CANCEL_EVENT` | Verhalten bei falschem Werkzeug: `CANCEL_EVENT` (Block bleibt einfach stehen) oder `CANCEL_DROP` (Block wird ohne Loot entfernt, Werkzeug verliert 1 Haltbarkeit) |

## Beispiel

```yaml
verstaerkter_erzblock:
  material: NOTE_BLOCK
  Mechanics:
    custom_block:
      miningtools:
        items:
          - DIAMOND_PICKAXE
          - NETHERITE_PICKAXE
          - custom_nexo_bohrer
        type: CANCEL_EVENT
```

## Hinweise & Besonderheiten

- Gilt nur für Nexo-Custom-Blocks (Noteblock- und Chorusblock-Mechanic), nicht für Vanilla-Blöcke oder Items.
- Es gibt keine Spieler-Rückmeldung (Nachricht/Actionbar) bei falschem Werkzeug – der Block bleibt bei `CANCEL_EVENT` einfach kommentarlos stehen.
- Bei `CANCEL_DROP` wird der Block über `NexoBlocks.remove(...)` mit einem leeren Loot-Drop entfernt, unabhängig von Schutz-Plugins wird dies nur ausgeführt, wenn `ProtectionLib.canBreak` zustimmt.
