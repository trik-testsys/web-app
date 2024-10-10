package trik.testsys.webclient.repository

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.EntityRepository
import trik.testsys.webclient.entity.impl.EmergencyMessage
import trik.testsys.webclient.entity.user.WebUser

@Repository
interface EmergencyMessageRepository : EntityRepository<EmergencyMessage> {

    fun findByUserType(userType: WebUser.UserType): List<EmergencyMessage>
}