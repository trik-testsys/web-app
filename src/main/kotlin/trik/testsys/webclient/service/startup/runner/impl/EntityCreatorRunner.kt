package trik.testsys.webclient.service.startup.runner.impl

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.context.annotation.Scope
import trik.testsys.core.entity.Entity
import trik.testsys.core.entity.user.AccessToken
import trik.testsys.core.entity.user.UserEntity
import trik.testsys.core.service.EntityService
import trik.testsys.core.service.user.UserService
import trik.testsys.webclient.entity.RegEntity
import trik.testsys.webclient.entity.impl.Group
import trik.testsys.webclient.entity.user.impl.*
import trik.testsys.webclient.service.entity.RegEntityService
import trik.testsys.webclient.service.entity.impl.GroupService
import trik.testsys.webclient.service.entity.user.impl.*
import trik.testsys.webclient.service.startup.runner.StartupRunner
import java.io.File
import java.nio.file.Files
import javax.xml.bind.annotation.XmlRootElement

/**
 * @author Roman Shishkin
 * @since 2.0.0
 **/
@org.springframework.stereotype.Service
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
class EntityCreatorRunner(
    @Value("\${path.startup.entities}") private val entitiesDirPath: String,

    private val adminService: AdminService,
    private val developerService: DeveloperService,
    private val judgeService: JudgeService,
    private val studentService: StudentService,
    private val superUserService: SuperUserService,
    private val viewerService: ViewerService,
    private val groupService: GroupService
) : StartupRunner {

    private lateinit var entitiesDir: File

    private var hasInjectedEntities = false

    private fun initEntitiesDir(): Boolean {
        logger.info("Checking entities directory by path $entitiesDirPath.")
        entitiesDir = File(entitiesDirPath)

        if (!entitiesDir.exists()) {
            logger.error("Entities directory by path $entitiesDirPath does not exist.")
            return false
        }

        if (!entitiesDir.isDirectory) {
            logger.error("Entities directory by path $entitiesDirPath is not a directory.")
            return false
        }

        logger.info("Entities directory by path $entitiesDirPath exists.")
        return true
    }

    override fun runBlocking() {
        initEntitiesDir()

        // Entities without dependencies to others. They should be created first.
        developerService.createEntitiesWithoutDependencies<Developer, DeveloperData>(DEVELOPERS_FILE_NAME)
        judgeService.createEntitiesWithoutDependencies<Judge, JudgeData>(JUDGES_FILE_NAME)
        superUserService.createEntitiesWithoutDependencies<SuperUser, SuperUserData>(SUPER_USERS_FILE_NAME)
        viewerService.createEntitiesWithoutDependencies<Viewer, ViewerData>(VIEWERS_FILE_NAME)

        // Entities with dependencies to others. They should be created after entities without dependencies.
        adminService.createEntitiesWithWebUserDependencies<Admin, AdminData>(ADMINS_FILE_NAME, viewerService) // Depends on Viewer
        groupService.createEntitiesWithWebUserDependencies<Group, GroupData>(GROUPS_FILE_NAME, adminService) // Depends on Admin
        studentService.createEntitiesWithGroupDependencies<Student, StudentData>(STUDENTS_FILE_NAME, groupService) // Depends on Group

        if (hasInjectedEntities) afterRun()
    }

    override suspend fun run() {
        TODO()
    }

    private fun afterRun() {
        logger.info("All entities were created.")
        logger.info("Moving entities files to backup directory.")

        val backupDir = File(entitiesDir, BACKUP_DIR_NAME + "_" + System.currentTimeMillis())
        if (!backupDir.exists()) backupDir.mkdir()

        entitiesDir.listFiles()?.forEach { file ->
            if (file.isDirectory) return@forEach
            Files.move(file.toPath(), File(backupDir, file.name).toPath())
        }

        logger.info("Entities files were moved to backup directory.")
    }

    private inline fun <reified E : Entity, reified D : EntityData.EntityDataWithoutDependencies<E>> EntityService<E>.createEntitiesWithoutDependencies(
        fileName: String
    ) {
        logger.info("Creating ${E::class.simpleName}s from file $fileName.")
        val file = getEntitiesFile(fileName) ?: return

        val objectMapper = createMapper()
        val entitiesData = objectMapper.getEntitiesData<E, D>(file) ?: return

        saveAll(entitiesData)
    }

    private inline fun <reified E : Entity, reified D : EntityData.EntityDataWithDependencies.EntityDataWithUserEntityDependencies<E>> EntityService<E>.createEntitiesWithWebUserDependencies(
        fileName: String, externalService: UserService<out UserEntity>
    ) {
        logger.info("Creating ${E::class.simpleName}s from file $fileName.")
        val file = getEntitiesFile(fileName) ?: return

        val objectMapper = createMapper()
        val entitiesData = objectMapper.getEntitiesData<E, D>(file) ?: return

        saveAll(entitiesData, externalService)
    }

    private inline fun <reified E : UserEntity, reified D : EntityData.EntityDataWithDependencies.EntityDataWithRegEntityDependencies<E>> EntityService<E>.createEntitiesWithGroupDependencies(
        fileName: String, externalService: RegEntityService<out RegEntity, E>
    ) {
        logger.info("Creating ${E::class.simpleName}s from file $fileName.")
        val file = getEntitiesFile(fileName) ?: return

        val objectMapper = createMapper()
        val entitiesData = objectMapper.getEntitiesData<E, D>(file) ?: return

        saveAll(entitiesData, externalService)
    }

    private fun <E : Entity, D : EntityData.EntityDataWithoutDependencies<E>> EntityService<E>.saveAll(
        entitiesData: Collection<D?>
    ) = entitiesData.forEach { entityData -> entityData?.toEntity()?.let { trySave(it) } }

    private fun <E : Entity, D : EntityData.EntityDataWithDependencies.EntityDataWithUserEntityDependencies<E>> EntityService<E>.saveAll(
        entitiesData: Collection<D?>,
        externalService: UserService<out UserEntity>
    ) = entitiesData.forEach { entityData -> entityData?.toEntity(externalService)?.let { trySave(it) } }

    private fun <E : UserEntity, D : EntityData.EntityDataWithDependencies.EntityDataWithRegEntityDependencies<E>> EntityService<E>.saveAll(
        entitiesData: Collection<D?>,
        externalService: RegEntityService<out RegEntity, E>
    ) = entitiesData.forEach { entityData -> entityData?.toEntity(externalService)?.let { trySave(it) } }

    private fun <E: Entity> EntityService<E>.trySave(entity: E) = try {
        save(entity)
        hasInjectedEntities = true
    } catch (e: Exception) {
        logger.error("Error while saving entity $entity.", e)
    }

    private inline fun <E : Entity, reified D : EntityData<E>> ObjectMapper.getEntitiesData(file: File): List<D?>? =
        try {
            readValue(file, object : TypeReference<List<D?>?>() {}) ?: run {
                logger.error("Entities data from file ${file.name} is null.")
                null
            }
        } catch (e: Exception) {
            logger.error("Error while reading entities data from file ${file.name}.", e)
            null
        }


    private fun getEntitiesFile(fileName: String): File? {
        val file = File(entitiesDir, fileName)

        if (!file.exists()) {
            logger.warn("File $fileName does not exist.")
            return null
        }

        return file
    }

    private fun createMapper(): ObjectMapper {
        val objectMapper = ObjectMapper()
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        return objectMapper
    }

    sealed interface EntityData<E : Entity> {

        interface EntityDataWithoutDependencies<E : Entity> : EntityData<E> {

            fun toEntity(): E
        }

        sealed interface EntityDataWithDependencies<E : Entity> : EntityData<E> {

            interface EntityDataWithUserEntityDependencies<E : Entity> : EntityDataWithDependencies<E> {

                fun <S : UserService<out UserEntity>> toEntity(externalService: S): E?
            }

            interface EntityDataWithRegEntityDependencies<E : UserEntity> : EntityDataWithDependencies<E> {

                fun <S : RegEntityService<out RegEntity, E>> toEntity(externalService: S): E?
            }
        }
    }

    @XmlRootElement
    private data class AdminData(
        val name: String = "",
        val accessToken: AccessToken = "",
        val viewerAccessToken: AccessToken = "",
        val additionalInfo: String? = null
    ) : EntityData.EntityDataWithDependencies.EntityDataWithUserEntityDependencies<Admin> {

        override fun <S : UserService<out UserEntity>> toEntity(externalService: S) =
            externalService.findByAccessToken(viewerAccessToken)?.let { viewer ->
                Admin(name, accessToken).also {
                    it.viewer = viewer as Viewer
                    additionalInfo?.let { addInfo -> it.additionalInfo = addInfo }
                }
            } ?: run {
                logger.error("Viewer with accessToken $viewerAccessToken not found.")
                null
            }
    }

    @XmlRootElement
    private data class DeveloperData(
        val name: String = "",
        val accessToken: AccessToken = "",
        val additionalInfo: String? = null
    ) : EntityData.EntityDataWithoutDependencies<Developer> {

        override fun toEntity() = Developer(name, accessToken).also {
            additionalInfo?.let { addInfo -> it.additionalInfo = addInfo }
        }
    }

    @XmlRootElement
    private data class JudgeData(
        val name: String = "",
        val accessToken: AccessToken = "",
        val additionalInfo: String? = null
    ) : EntityData.EntityDataWithoutDependencies<Judge> {

        override fun toEntity() = Judge(name, accessToken).also {
            additionalInfo?.let { addInfo -> it.additionalInfo = addInfo }
        }
    }

    @XmlRootElement
    private data class StudentData(
        val name: String = "",
        val accessToken: AccessToken = "",
        val groupRegToken: AccessToken = "",
        val additionalInfo: String? = null
    ) : EntityData.EntityDataWithDependencies.EntityDataWithRegEntityDependencies<Student> {


        override fun <S : RegEntityService<out RegEntity, Student>> toEntity(externalService: S): Student? =
            externalService.findByRegToken(groupRegToken)?.let { group ->
                Student(name, accessToken).also {
                    it.group = group as Group
                    additionalInfo?.let { addInfo -> it.additionalInfo = addInfo }
                }
            } ?: run {
                logger.error("Group with accessToken $groupRegToken not found.")
                null
            }
    }

    @XmlRootElement
    private data class SuperUserData(
        val name: String = "",
        val accessToken: AccessToken = "",
        val additionalInfo: String? = null
    ) : EntityData.EntityDataWithoutDependencies<SuperUser> {

        override fun toEntity() = SuperUser(name, accessToken).also {
            additionalInfo?.let { addInfo -> it.additionalInfo = addInfo }
        }
    }

    @XmlRootElement
    private data class ViewerData(
        val name: String = "",
        val accessToken: AccessToken = "",
        val regToken: String = "",
        val additionalInfo: String? = null
    ) : EntityData.EntityDataWithoutDependencies<Viewer> {

        override fun toEntity() = Viewer(name, accessToken, regToken).also {
            additionalInfo?.let { addInfo -> it.additionalInfo = addInfo }
        }
    }

    @XmlRootElement
    private data class GroupData(
        val name: String = "",
        val regToken: AccessToken = "",
        val adminAccessToken: AccessToken = "",
        val additionalInfo: String? = null
    ) : EntityData.EntityDataWithDependencies.EntityDataWithUserEntityDependencies<Group> {

        override fun <S : UserService<out UserEntity>> toEntity(externalService: S): Group? =
            externalService.findByAccessToken(adminAccessToken)?.let { admin ->
                Group(name, regToken).also {
                    it.admin = admin as Admin
                    additionalInfo?.let { addInfo -> it.additionalInfo = addInfo }
                }
            } ?: run {
                logger.error("Admin with accessToken $adminAccessToken not found.")
                null
            }
    }

    companion object {

        private val logger = LoggerFactory.getLogger(EntityCreatorRunner::class.java)

        private const val ADMINS_FILE_NAME = "admins.json"
        private const val DEVELOPERS_FILE_NAME = "developers.json"
        private const val JUDGES_FILE_NAME = "judges.json"
        private const val STUDENTS_FILE_NAME = "students.json"
        private const val SUPER_USERS_FILE_NAME = "super_users.json"
        private const val VIEWERS_FILE_NAME = "viewers.json"
        private const val GROUPS_FILE_NAME = "groups.json"

        private const val BACKUP_DIR_NAME = "backup"
    }
}