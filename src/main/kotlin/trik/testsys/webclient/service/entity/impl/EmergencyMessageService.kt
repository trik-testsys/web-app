package trik.testsys.webclient.service.entity.impl

import org.springframework.stereotype.Service
import trik.testsys.core.service.AbstractService
import trik.testsys.webclient.entity.impl.EmergencyMessage
import trik.testsys.webclient.entity.user.WebUser
import trik.testsys.webclient.repository.EmergencyMessageRepository

@Service
class EmergencyMessageService : AbstractService<EmergencyMessage, EmergencyMessageRepository>() {

    fun findByUserType(userType: WebUser.UserType) = repository.findByUserType(userType).firstOrNull()

    override fun save(entity: EmergencyMessage): EmergencyMessage {
        val existing = repository.findByUserType(entity.userType)

        return if (existing.isEmpty()) {
            repository.save(entity)
        } else {
            repository.deleteAll(existing)
            repository.save(entity)
        }
    }
}