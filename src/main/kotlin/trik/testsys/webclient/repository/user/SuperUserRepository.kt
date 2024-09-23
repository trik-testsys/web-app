package trik.testsys.webclient.repository.user

import org.springframework.stereotype.Repository
import trik.testsys.core.repository.user.UserRepository
import trik.testsys.webclient.entity.impl.user.SuperUser

@Repository
interface SuperUserRepository: UserRepository<SuperUser>