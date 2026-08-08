package de.birk.calory.domain.food;

/**
 * A food item's diet compatibility, restricted to the vegan/vegetarian axis by design - other
 * dietary properties (gluten-free, halal, lactose-free, ...) are out of scope for now, but a
 * separate field per property keeps adding one later straightforward.
 *
 * @author Marius Birk
 */
public enum Diet {
  VEGAN,
  VEGETARIAN,
  NON_VEGETARIAN,
  UNKNOWN
}
