# Timber

> Fällt beim Abbau eines Baumstamms den ganzen Baum auf einmal – ein Blockbruch, der ganze Baum fällt.

| | |
|---|---|
| **Config-Key** | `Mechanics.timber` |
| **Gilt für** | Item (Werkzeug, i. d. R. Axt) |
| **Listener-Klasse** | `Timber.TimberListener` |
| **Toggle/Sneak-Verhalten** | Optional per Rechtsklick (in die Luft) an-/ausschaltbar, wenn `toggleable: true` |

## Was macht sie?

Bricht der Spieler mit einem Timber-Werkzeug einen Log-Block, sucht die Mechanic von diesem Block ausgehend alle direkt und diagonal verbundenen Logs desselben Typs (vertikal und horizontal) und bricht sie zusammen mit dem Ursprungsblock ab. Optional werden zusätzlich die Blätter im Umkreis mitgenommen. Die Suche bricht ab, sobald `limit` erreicht oder die maximale Höhendifferenz `max_height` überschritten ist. Jeder zusätzliche Blockbruch feuert ein reguläres `BlockBreakEvent`, wodurch Drops, Schutz-Plugins (WorldGuard, GriefPrevention, PlotSquared via ProtectionLib) und andere Listener normal greifen.

## Wann einsetzen?

- Holzfäller-Äxte im Shop/Kit, die das Abholzen großer Baumfarmen beschleunigen
- Ressourcenwerkzeuge, die sich per Rechtsklick ein-/ausschalten lassen, damit Spieler auch gezielt einzelne Logs abbauen können
- Kombination mit eigenen Nexo-Log-Blöcken (z. B. Fantasy-Bäume) über die Nexo-ID-Whitelist

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.timber.limit` | `int` | `100` | **(Pflicht, aktiviert die Mechanic)** Maximale Anzahl an Blöcken (Logs + Blätter), die auf einmal gebrochen werden |
| `Mechanics.timber.max_height` | `int` | `32` | Maximaler Höhenunterschied zum Ursprungsblock, den die Suche noch verfolgt |
| `Mechanics.timber.toggleable` | `bool` | `false` | Ob der Spieler die Mechanic per Rechtsklick (Luft) an/aus schalten kann |
| `Mechanics.timber.break_leaves` | `bool` | `false` | Ob Blätter im Umkreis von 5 Blöcken um jeden gefundenen Log mit abgebrochen werden |
| `Mechanics.timber.whitelist` | `List<String>` | `[]` (leer) | Vanilla-Materials oder Nexo-Block-IDs, die als "Log" zählen. Leer = alle Standard-Baumstämme (Oak, Spruce, Birch, Jungle, Acacia, Dark Oak, Mangrove, Cherry, Crimson/Warped Stem inkl. gestrippter Varianten) |

## Beispiel

```yaml
diamond_axe:
  material: DIAMOND_AXE
  Mechanics:
    timber:
      limit: 100
      max_height: 32
      toggleable: true
      break_leaves: false
      whitelist:
        - OAK_LOG
        - SPRUCE_LOG
        - BIRCH_LOG
        - custom_nexo_log
```

## Hinweise & Besonderheiten

- Nur Logs **desselben Typs** wie der ursprünglich abgebaute Block werden mitgenommen (Vanilla-Material bzw. Nexo-ID müssen übereinstimmen) – gemischte Wälder brechen nicht komplett durch.
- Respektiert Schutz-Plugins über `ProtectionLib`: Blöcke außerhalb geschützter Bereiche werden übersprungen, nicht die ganze Aktion abgebrochen.
- Flüssigkeiten und als "unbreakable" markierte Blöcke werden nie mitgebrochen.
- Der Toggle-Status wird direkt im Item (PersistentDataContainer) gespeichert, gilt also pro Item-Stack, nicht global pro Spieler.
- Die Toggle-Meldungen ("Timber enabled/disabled") kommen aus `config.yml` (`messages.timber.enabled` / `.disabled`), nicht aus dieser Mechanic-Konfiguration selbst.
