package dev.celerbi.polymorphicdelight.selection;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class MajoritySelection {
  private MajoritySelection() {}

  public static <T> T choose(List<T> votes, Set<T> eligible, T remembered) {
    Map<T, Integer> totals = new LinkedHashMap<>();
    for (T choice : votes) {
      if (choice != null && eligible.contains(choice)) {
        totals.merge(choice, 1, Integer::sum);
      }
    }
    int maximum = totals.values().stream().mapToInt(Integer::intValue).max().orElse(0);
    if (maximum == 0)
      return remembered != null && eligible.contains(remembered)
          ? remembered
          : eligible.stream().findFirst().orElse(null);
    if (remembered != null && totals.getOrDefault(remembered, 0) == maximum) {
      return remembered;
    }

    for (Map.Entry<T, Integer> entry : totals.entrySet()) {
      if (entry.getValue() == maximum) return entry.getKey();
    }
    return null;
  }
}
