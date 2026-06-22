package zone.vao.nexoAddon.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import zone.vao.nexoAddon.NexoAddon;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HologramUtil {

  private static final Map<UUID, TextDisplay> holograms = new HashMap<>();

  public static void displayProgressBar(Entity entity, double progress, Player player) {
    if (entity == null || progress < 0.0 || progress > 1.0) return;

    NexoAddon.getInstance().foliaLib.getScheduler().runAsync(startDisplay -> {
      World world = entity.getWorld();
      Location entityLocation = entity.getLocation().clone();
      Location hologramLocation = entityLocation.add(0, 0.5, 0);

      Component progressBar = getProgressBar(progress, 10);

      NexoAddon.getInstance().foliaLib.getScheduler().runNextTick(nextTick -> {
        if (player != null && holograms.containsKey(player.getUniqueId())) {
          TextDisplay existingHologram = holograms.get(player.getUniqueId());
          existingHologram.remove();
          holograms.remove(player.getUniqueId());
        }

        TextDisplay hologram = world.spawn(hologramLocation, TextDisplay.class, holo -> {
          holo.customName(progressBar);
          holo.setCustomNameVisible(true);
          holo.setGravity(false);
          holo.setInvisible(true);
          holo.setBillboard(Display.Billboard.CENTER);
          if (player != null) {
            holo.setVisibleByDefault(false);
            player.showEntity(NexoAddon.getInstance(), holo);
          }
        });

        if (player != null)
          holograms.put(player.getUniqueId(), hologram);

        NexoAddon.getInstance().foliaLib.getScheduler().runLater(() -> {
          hologram.remove();
          if (player != null)
            holograms.remove(player.getUniqueId());
        }, 60);
      });
    });
  }

  private static Component getProgressBar(double progress, int length) {
    int filledLength = (int) (progress * length);
    int emptyLength = length - filledLength;

    Component filledPart = Component.text("█".repeat(filledLength)).color(NamedTextColor.DARK_GREEN);
    Component emptyPart = Component.text("█".repeat(emptyLength)).color(NamedTextColor.RED);

    return filledPart.append(emptyPart);
  }

}
