# BedrockBreak

> Erlaubt das Abbauen von Bedrock mit einem speziellen Werkzeug über eine konfigurierbare Abbauzeit, nur in der Welt "Plots".

| | |
|---|---|
| **Config-Key** | `Mechanics.bedrockbreak` |
| **Gilt für** | Item (Werkzeug) |
| **Listener-Klasse** | `BlockHardnessHandler` (PacketEvents-Listener auf Digging-Pakete) |
| **Toggle/Sneak-Verhalten** | Kein Toggle, immer aktiv wenn Werkzeug in der Hand |

## Was macht sie?

Beginnt ein Spieler mit einem BedrockBreak-Werkzeug in der Welt "Plots" einen Bedrock-Block abzubauen, startet die Mechanic einen eigenen Fortschritts-Timer (alle 10 Ticks), der die Vanilla-Abbauanimation manuell an alle Spieler sendet. Nach `hardness` Fortschrittsschritten gilt der Block als fertig abgebaut: Er wird über ein reguläres `BlockBreakEvent` entfernt, mit `probability` als Nexo-Bedrock droppt oder nicht, und dem Werkzeug wird `durability_cost` Haltbarkeit abgezogen (bei Erreichen der maximalen Haltbarkeit wird das Werkzeug entfernt). Ist `disable_on_first_layer` aktiv, funktioniert die Mechanic nicht auf der untersten Weltebene.

## Wann einsetzen?

- Spezial-Werkzeuge für Plot-Welten, mit denen Spieler gezielt (aber begrenzt) Bedrock-Grenzen durchbrechen können
- Belohnungs-/Prestige-Werkzeuge mit hohem `hardness`-Wert als Zeit-Investment
- Werkzeuge, bei denen ein Bedrock-Drop nur mit geringer Wahrscheinlichkeit gewährt werden soll (`probability`)

## Konfiguration

| Key | Typ | Default | Beschreibung |
|---|---|---|---|
| `Mechanics.bedrockbreak.hardness` | `int` | – | **(Pflicht zusammen mit `probability`)** Anzahl Fortschrittsschritte (à 10 Ticks) bis der Block bricht – bestimmt die Abbauzeit |
| `Mechanics.bedrockbreak.probability` | `double` | – | **(Pflicht zusammen mit `hardness`)** Wahrscheinlichkeit (0.0–1.0), dass beim Abbau ein Bedrock-Item gedroppt wird |
| `Mechanics.bedrockbreak.durability_cost` | `int` | `1` | Haltbarkeitsverlust des Werkzeugs pro abgebautem Bedrock-Block |
| `Mechanics.bedrockbreak.disable_on_first_layer` | `bool` | `true` | Ob die Mechanic auf der untersten Weltebene (Bedrock-Boden) deaktiviert ist |
| `Mechanics.bedrockbreak.sound` | `String` | `block.stone.break` | Sound-Key, wird geladen, aber im aktuellen Listener nicht abgespielt |

## Beispiel

```yaml
bedrock_bohrer:
  material: NETHERITE_PICKAXE
  Mechanics:
    bedrockbreak:
      hardness: 200
      probability: 0.25
      durability_cost: 5
      disable_on_first_layer: true
```

## Hinweise & Besonderheiten

- Funktioniert **nur in einer Welt namens "Plots"** – dieser Weltname ist im Code fest verdrahtet und nicht konfigurierbar.
- Der Abbaufortschritt wird über abgefangene Digging-Pakete (PacketEvents) simuliert, nicht über den Standard-Vanilla-Abbaumechanismus – die client-seitige Break-Animation wird manuell nachgebildet.
- Funktioniert nur im Creative-Modus nicht (Spieler im Creative-Modus werden ignoriert).
- Respektiert Schutz-Plugins über `ProtectionLib` beim finalen Abbau.
- Wird die maximale Haltbarkeit erreicht, verschwindet das Werkzeug ersatzlos aus der Hand des Spielers.
