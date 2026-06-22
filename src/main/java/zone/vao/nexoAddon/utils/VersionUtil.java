package zone.vao.nexoAddon.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class VersionUtil {

  public static boolean isVersionLessThan(String targetVersion) {
    String version = Bukkit.getBukkitVersion();
    return checkVersion(targetVersion, version);
  }

  public static boolean nexoVersionLessThan(String targetVersion) {
    Plugin nexo = Bukkit.getPluginManager().getPlugin("Nexo");

    if (nexo == null || nexo.getDescription() == null) {
      return false;
    }

    String version = nexo.getDescription().getVersion();
    return checkVersion(targetVersion, version);
  }

  private static boolean checkVersion(String targetVersion, String version) {
    List<Integer> currentVersionNumbers = extractVersionNumbers(version);
    List<Integer> targetVersionNumbers = extractVersionNumbers(targetVersion);

    int length = Math.max(currentVersionNumbers.size(), targetVersionNumbers.size());

    for (int i = 0; i < length; i++) {
      int current = i < currentVersionNumbers.size() ? currentVersionNumbers.get(i) : 0;
      int target = i < targetVersionNumbers.size() ? targetVersionNumbers.get(i) : 0;

      if (current < target) {
        return true;
      } else if (current > target) {
        return false;
      }
    }

    return false;
  }

  private static List<Integer> extractVersionNumbers(String version) {
    List<Integer> numbers = new ArrayList<>();

    if (version == null || version.isBlank()) {
      return numbers;
    }

    String cleanVersion = version.split(" ")[0];
    cleanVersion = cleanVersion.split("-")[0];

    Matcher matcher = Pattern.compile("\\d+").matcher(cleanVersion);

    while (matcher.find()) {
      numbers.add(Integer.parseInt(matcher.group()));
    }

    return numbers;
  }
}