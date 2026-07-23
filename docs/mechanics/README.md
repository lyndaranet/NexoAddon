# NexoAddon – Mechanics-Dokumentation

Übersicht über alle Mechaniken des NexoAddon-Plugins. Jede Mechanic wird in einer Nexo-Item-YAML
unter einem `Mechanics:`-Abschnitt pro Item-ID konfiguriert und beim Serverstart bzw. Reload von
`ItemConfigUtil.java` eingelesen. Jede Datei in diesem Ordner deckt genau eine Mechanic ab: was sie
tut, wann man sie einsetzt, welche Config-Keys es gibt und ein einsatzbereites YAML-Beispiel.

Neue Mechanic-Doku schreiben? Struktur und Tiefe orientieren sich an [`_TEMPLATE.md`](_TEMPLATE.md)
und [`timber.md`](timber.md) als Referenzbeispiel.

## Abbau & Ressourcen

Werkzeug-Mechaniken, die das Abbau-/Ernteverhalten erweitern.

| Mechanic | Beschreibung |
|---|---|
| [BigMining](bigmining.md) | Bricht beim Abbau zusätzlich alle Blöcke in einem Radius/Tiefe-Quader um den Ursprungsblock mit ab. |
| [VeinMiner](veinminer.md) | Bricht die gesamte zusammenhängende "Ader" gleichartiger Blöcke auf einmal ab. |
| [Timber](timber.md) | Fällt beim Abbau eines Baumstamms den ganzen Baum auf einmal. |
| [BedrockBreak](bedrockbreak.md) | Erlaubt das Abbauen von Bedrock mit konfigurierbarer Abbauzeit (nur Welt "Plots"). |
| [SpawnerBreak](spawnerbreak.md) | Erlaubt das Abbauen von Monster-Spawnern mit konfigurierbarer Drop-Chance (nur Welt "Plots"). |
| [Telekinesis](telekinesis.md) | Abgebaute Blöcke wandern direkt ins Inventar statt zu droppen. |
| [SandSmelt](sandsmelt.md) | Baut Sand-Blöcke ab und droppt direkt Glas statt Sand. |
| [GlassBreaker](glassbreaker.md) | Baut Glasblöcke sofort ab, ganz ohne Abbauzeit. |
| [WideHoe](widehoe.md) | Pflügt beim Rechtsklick ein ganzes quadratisches Areal statt nur einem Block. |
| [BetterMine](better_mine.md) | Schnelleres Abbauen (Haste) und Chance auf Bonus-Drops, je mit eigener Blockliste. |

## Custom-Block & Furniture Verhalten

Wirken auf den Nexo-Block/das Furniture selbst (Note-, String-, Chorus-Block oder Furniture), nicht
auf ein Werkzeug.

| Mechanic | Beschreibung |
|---|---|
| [MiningTools](miningtools.md) | Beschränkt einen Custom Block darauf, nur mit bestimmten Werkzeugen abbaubar zu sein. |
| [DropExperience](dropexperience.md) | Lässt einen Custom Block beim Abbau zusätzlich Erfahrung droppen. |
| [Infested](infested.md) | Spawnt beim Abbau Vanilla-Mobs und/oder MythicMobs, ähnlich Vanilla "Infested Stone". |
| [Decay](decay.md) | Lässt einen Custom Block nach einer Wartezeit verfallen, wenn er nicht mit einem Basis-Material verbunden ist. |
| [Shiftblock](shiftblock.md) | Tauscht einen Block/Furniture per Platzieren/Interagieren/Abbauen temporär oder dauerhaft gegen eine andere Nexo-ID aus. |
| [Signal](signal.md) | Kabelloses, redstone-artiges Signalsystem zwischen Furniture-Objekten (Sender/Empfänger auf einem Kanal). |
| [Remember](remember.md) | Merkt sich Name/Lore des platzierten Items an einem Furniture und gibt sie beim Abbauen zurück. |
| [Stackable](stackable.md) | Baut einen String-Block/ein Furniture per Rechtsklick schrittweise zur nächsten Stufe aus. |
| [Unstackable](unstackable.md) | Gegenstück zu Stackable: baut eine Stufe zurück und gibt ein Item dafür zurück. |
| [InventoryType](inventorytype.md) | Öffnet per Rechtsklick ein virtuelles Inventar eines bestimmten Typs (Werkbank, Amboss, Endertruhe, ...). |

## Item-Ökonomie & Komfort

Utility-Mechaniken, die den Umgang mit einzelnen Items komfortabler machen.

| Mechanic | Beschreibung |
|---|---|
| [Repair](repair.md) | Repariert ein Item, wenn ein Reparatur-Item per Linksklick darauf abgelegt wird. |
| [Enchantify](enchantify.md) | Überträgt fest konfigurierte Verzauberungen auf ein anderes Item. |
| [BottledExp](bottledexp.md) | Wandelt Spieler-Erfahrung per Klick in Erfahrungsflaschen um. |
| [AutoCatch](autocatch.md) | Automatisiert das Angeln (Anschlag bei Biss, optional automatisches Neuauswerfen). |
| [UniqueId](uniqueid.md) | Vergibt jedem Item-Exemplar eine eigene, dauerhafte UUID. |
| [Kill Message](kill_message.md) | Ersetzt die Todesnachricht durch einen eigenen Text bei Kills mit diesem Item. |
| [InfiniteBucket](infinite_bucket.md) | Eimer platziert Flüssigkeit, ohne verbraucht zu werden. |
| [InfiniteFood](infinite_food.md) | Lebensmittel wird gegessen, ohne aus dem Inventar zu verschwinden. |
| [InfiniteShears](infinite_shears.md) | Verhindert Haltbarkeitsverlust einer Schere. |
| [InfiniteFluidBucket](infinite_fluid_bucket.md) | Ein Eimer-Item, das per Rechtsklick zwischen Wasser-/Lava-Modus umschaltet. |

## Partikel & Ambiente

Drei ähnlich benannte, aber unterschiedliche Mechaniken – Abgrenzung siehe jeweilige Doku.

| Mechanic | Beschreibung |
|---|---|
| [Aura](aura.md) | Item erzeugt permanent Partikel um den Spieler, solange gehalten/getragen. |
| [Block Aura](block_aura.md) | Platzierter Custom Block/Furniture erzeugt dauerhaft Partikel an seinem Standort. |
| [Particle Aura](particle_aura.md) | Mehrschichtige, animierte Partikel-Aura (mehrere unabhängige "Layer") für ein Item in einem Ausrüstungsslot. |

## Bewegung

| Mechanic | Beschreibung |
|---|---|
| [Dash](dash.md) | Einfacher Sprint-Dash per Rechtsklick mit Cooldown, Partikeln und Sound. |
| [Magnet](magnet.md) | Zieht lose Items automatisch zum Spieler. |
| [Grappling Hook](grappling_hook.md) | Enterhaken zieht den Spieler mit Partikel-Seil zum anvisierten Block. |
| [Spiderman](spiderman.md) | Wandklettern per Schleichen + Netz-Schwung per Rechtsklick. |

> `Dash` (`Mechanics.dash.power`) und die MMO-Fähigkeit `Dash Ability` (`Mechanics.dash.mode`, siehe
> unten) teilen sich denselben YAML-Abschnitt `Mechanics.dash`, schließen sich aber gegenseitig aus –
> nur eine der beiden kann pro Item aktiv sein.

## Kampf-Utility

Einfache, nicht Trigger-basierte Kampf-Ergänzungen.

| Mechanic | Beschreibung |
|---|---|
| [LifeSteal](lifesteal.md) | Heilt bei Treffern um einen Prozentsatz des verursachten Schadens. |
| [On Hit](on_hit.md) | Verpasst dem Ziel bei Nahkampftreffern konfigurierte Statuseffekte. |
| [Area Mining](area_mining.md) | Baut beim Abbau zusätzliche Blöcke in einer konfigurierten Form (Linie, Ader, Fläche) mit ab. |

## MMO-Ability-Baukasten

Gemeinsamer Fähigkeiten-Baukasten mit Trigger (z. B. `right_click`, `sneak`), Cooldown, Conditions
(Sneak/Health/Permission ...) und wiederverwendbaren Effect-/Partikel-/Sound-Bausteinen. Ideal für
Custom-Waffen und Skill-Items.

| Mechanic | Beschreibung |
|---|---|
| [Passive Effect](passive_effect.md) | Dauerhafte Potion-Effekte/Attribut-Boni, solange ein Item im Ausrüstungsslot getragen wird. |
| [Consumable](consumable.md) | Sofortheilung/-schaden, Effekte, Befehle und Nachrichten beim Essen oder Rechtsklick. |
| [Area Ability](area_ability.md) | Trifft alle Wesen in einem Radius mit Heilung, Schaden, Effekten, Launch und Kommandos. |
| [Teleport](teleport.md) | Teleportiert den Spieler (Blickrichtung, zum Ziel, aufwärts, zufällig) mit Feedback. |
| [Beam](beam.md) | Sofortiger, sichtbarer Partikelstrahl in Blickrichtung, optional durchdringend. |
| [Projectile](projectile.md) | Simuliertes Projektil mit Schwerkraft, Homing, Abprallen, Durchdringung und Explosion. |
| [Shape Wave](shape_wave.md) | Trifft alle Wesen in einer 3D-Form (Kegel, Zylinder, Keil, Nova, Fächer, Linie). |
| [Bow](bow.md) | Erweitert einen Bogen um passive Pfeil-Modifikationen und eine Shift-Rechtsklick-Spezialfähigkeit. |
| [Dash Ability](dash_ability.md) | Dash/Gleit-/Flug-/Blink-Modus mit Phasen-Effekten, Invincibility-Fenster und Einschlag. |
| [Block Trigger Launch](block_trigger_launch.md) | Aktivierter Zeit-Buff: jeder Schritt auf einen konfigurierten Block löst einen Launch aus. |
| [Wither Skull](witherskull.md) | Schießt per Trigger einen (optional aufgeladenen) Witherkopf in Blickrichtung, mit Cooldown. |
| [Thor](thor.md) | Schlägt per Trigger einen oder mehrere Blitze an der anvisierten Stelle ein, mit Streuung und Cooldown. |

---

**Alle 52 Mechaniken** dieses Ordners sind 1:1 aus den Loader-Methoden in
`src/main/java/zone/vao/nexoAddon/utils/ItemConfigUtil.java` sowie den zugehörigen Klassen in
`src/main/java/zone/vao/nexoAddon/items/mechanics/` dokumentiert – Config-Keys und Defaults sind
daher verbindlich. Bekannte Code-Eigenheiten (z. B. tote Config-Keys, nicht durchgesetzte Filter)
sind in den jeweiligen "Hinweise & Besonderheiten"-Abschnitten vermerkt, nicht verschwiegen.
