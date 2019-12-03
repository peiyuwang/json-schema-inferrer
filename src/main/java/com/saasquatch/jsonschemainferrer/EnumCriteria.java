package com.saasquatch.jsonschemainferrer;

import static com.saasquatch.jsonschemainferrer.JunkDrawer.isValidEnumValue;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nonnegative;
import javax.annotation.Nonnull;

/**
 * Utilities for {@link EnumCriterion}
 *
 * @author sli
 */
public final class EnumCriteria {

  private EnumCriteria() {}

  /**
   * @return A singleton {@link EnumCriterion} that always returns false
   */
  public static EnumCriterion noOp() {
    return input -> false;
  }

  /**
   * @return An {@link EnumCriterion} where the samples are part of an enum if the sample size is
   *         less than or equal to the limit.
   */
  public static EnumCriterion limit(@Nonnegative int limit) {
    return input -> input.getSamples().size() <= limit;
  }

  /**
   * @return An {@link EnumCriterion} that returns true if all the samples are valid enum values of
   *         a Java enum.
   */
  public static <E extends Enum<E>> EnumCriterion isValidEnum(@Nonnull Class<E> enumClass) {
    Objects.requireNonNull(enumClass);
    return input -> {
      return input.getSamples().stream()
          .allMatch(j -> isValidEnumValue(enumClass, j.textValue()));
    };
  }

  /**
   * Convenience method for {@link #or(List)}.
   */
  public static EnumCriterion or(@Nonnull EnumCriterion... criteria) {
    return or(Arrays.asList(criteria));
  }

  /**
   * @return An {@link EnumCriterion} that is a logical or of the given criteria
   * @throws NullPointerException if the input has null elements
   * @throws IllegalArgumentException if the input is empty
   */
  public static EnumCriterion or(@Nonnull List<EnumCriterion> criteria) {
    for (EnumCriterion criterion : criteria) {
      Objects.requireNonNull(criterion);
    }
    switch (criteria.size()) {
      case 0:
        throw new IllegalArgumentException("Empty criteria");
      case 1:
        return criteria.get(0);
      default:
        break;
    }
    // Defensive copy
    final EnumCriterion[] criteriaArray = criteria.toArray(new EnumCriterion[0]);
    return input -> {
      for (EnumCriterion criterion : criteriaArray) {
        if (criterion.isEnum(input)) {
          return true;
        }
      }
      return false;
    };
  }

}