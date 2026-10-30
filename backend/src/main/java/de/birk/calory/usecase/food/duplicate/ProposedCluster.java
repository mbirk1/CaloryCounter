package de.birk.calory.usecase.food.duplicate;

import java.util.List;

import de.birk.calory.domain.food.duplicate.DuplicateClusterMember;

/**
 * A group of food items {@link DuplicateClusterBuilder} considers duplicates of one another,
 * before it is assigned an id, {@code PENDING} status and creation timestamp by the usecase that
 * persists it as a {@link de.birk.calory.domain.food.duplicate.DuplicateCluster}.
 *
 * @param members the proposed cluster's members, in ascending id order
 * @author Marius Birk
 */
public record ProposedCluster(List<DuplicateClusterMember> members) {
}
