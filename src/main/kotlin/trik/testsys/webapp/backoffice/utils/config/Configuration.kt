package trik.testsys.webapp.backoffice.utils.config

import jakarta.servlet.MultipartConfigElement
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.servlet.MultipartConfigFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.EnableAspectJAutoProxy
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.security.web.SecurityFilterChain
import org.springframework.util.unit.DataSize
import org.springframework.util.unit.DataUnit
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer


@Configuration
@EnableWebSecurity
@EnableScheduling
@EnableAspectJAutoProxy
@EnableWebMvc
class Configuration(
    @Value("\${path.logos.sponsor}") private val sponsorLogosPath: String,
    @Value("\${path.logos.main}") private val mainLogoFilePath: String
) : WebMvcConfigurer {

    override fun addResourceHandlers(registry: ResourceHandlerRegistry) {
        registry.addResourceHandler(
            "/img/**",
            "/css/**",
            "/js/**",
            "/assets/**"
        ).addResourceLocations(
            "classpath:/static/img/",
            "classpath:/static/css/",
            "classpath:/static/js/",
            "classpath:/static/assets/",
            "file:$sponsorLogosPath/",
            "file:$mainLogoFilePath"
        )
    }

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http.authorizeRequests()
            .anyRequest().permitAll()
            .and()
            .csrf().disable()
        return http.build()
    }

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder(8)
    }

    @Bean
    fun multipartConfigElement(): MultipartConfigElement {
        val factory = MultipartConfigFactory()
        factory.setMaxFileSize(DataSize.of(4, DataUnit.MEGABYTES))
        factory.setMaxRequestSize(DataSize.of(4, DataUnit.MEGABYTES))
        return factory.createMultipartConfig()
    }
}