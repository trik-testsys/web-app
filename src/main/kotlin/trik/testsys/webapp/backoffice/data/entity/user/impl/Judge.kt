//package trik.testsys.webapp.backoffice.data.entity.user.impl
//
//import trik.testsys.core.entity.Entity.Companion.TABLE_PREFIX
//import trik.testsys.core.entity.user.AccessToken
//import trik.testsys.backoffice.entity.impl.SolutionVerdict
//import trik.testsys.backoffice.entity.user.WebUser
//import javax.persistence.*
//
//
//@Entity
//@Table(name = "${TABLE_PREFIX}_JUDGE")
//class Judge(
//    name: String,
//    accessToken: AccessToken
//) : WebUser(name, accessToken, UserType.JUDGE) {
//
//    @OneToMany(mappedBy = "judge")
//    val solutionVerdicts: MutableSet<SolutionVerdict> = mutableSetOf()
//}