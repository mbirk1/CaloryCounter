package de.birk.calory.domain.user;

/**
 * Physical activity level of a user, used to compute the total daily energy
 * expenditure (TDEE) on top of the base metabolic rate.
 *
 * @author Marius Birk
 */
public enum ActivityLevel {
  SEDENTARY,
  LIGHTLY_ACTIVE,
  MODERATELY_ACTIVE,
  VERY_ACTIVE,
  EXTRA_ACTIVE
}
