package de.birk.calory.usecase.food.importer;

/**
 * Lifecycle state of a CSV import job.
 *
 * @author Marius Birk
 */
public enum ImportJobState {
  RUNNING,
  COMPLETED,
  FAILED
}
