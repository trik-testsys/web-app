package trik.testsys.webclient.configs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.servlet.config.annotation.EnableWebMvc
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer

import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.service.Contact
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket

@Configuration
@EnableWebMvc
class SwaggerConfiguration : WebMvcConfigurer {
    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2).select()
            .apis(RequestHandlerSelectors.basePackage("com.companyname.app"))
            .paths(PathSelectors.regex("/.*"))
            .build().apiInfo(apiInfoMetaData())
    }

    private fun apiInfoMetaData(): ApiInfo {
        return ApiInfoBuilder()
            .title("NAME OF SERVICE")
            .description("API Endpoint Decoration")
            .contact(Contact("Dev-Team", "https://www.dev-team.com/", "dev-team@gmail.com"))
            .license("Apache 2.0")
            .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0.html")
            .version("1.0.0")
            .build()
    }
}