package trik.testsys.webclient.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import trik.testsys.webclient.entity.Label
import trik.testsys.webclient.repository.LabelRepository

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
}