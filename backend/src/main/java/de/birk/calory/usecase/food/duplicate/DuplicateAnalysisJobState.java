package de.birk.calory.usecase.food.duplicate;

/**
 * Lifecycle state of a duplicate-detection analysis job.
 *
 * @author Marius Birk
 */
public enum DuplicateAnalysisJobState {
  RUNNING,
  COMPLETED,
  FAILED
}
