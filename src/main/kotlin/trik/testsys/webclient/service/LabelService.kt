package trik.testsys.webclient.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.Group
import trik.testsys.webclient.entity.Label
import trik.testsys.webclient.repository.LabelRepository

/**
 * @author Roman Shishkin
 * @since 1.1.0
 */
@Service
class LabelService @Autowired constructor(
    private val labelRepository: LabelRepository
) {

    fun getByName(name: String): Label? {
        return labelRepository.findLabelByName(name)
    }

    fun save(label: Label): Label {
        return labelRepository.save(label)
    }

    fun getAll(): List<Label> {
        return labelRepository.findAll().toList()
    }

    /**
     * @return [Set] of [Group]s that have all [Label]s from [labels] set.
     */
    fun getGroupsWithAllLabels(labels: Set<Label>): Set<Group> {
        val groups = mutableSetOf(setOf<Group>())
        labels.forEach { label ->
            groups.add(label.groups)
        }

        return groups.stream().skip(1).reduce { acc, set -> acc.intersect(set)}.get()
    }

    /**
     * @return [Set] of [Group]s that have any [Label] from [labels] set.
     */
    fun getGroupsWithAnyLabel(labels: Set<Label>): Set<Group> {
        val groups = mutableSetOf<Group>()
        labels.forEach { label ->
            groups.addAll(label.groups)
        }

        return groups
    }
}