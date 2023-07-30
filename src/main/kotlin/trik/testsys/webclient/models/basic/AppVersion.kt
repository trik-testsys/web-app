package trik.testsys.webclient.models.basic

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty

@ApiModel(description = "Version JSON")
data class AppVersion(

    @ApiModelProperty(value = "Application version", example = "1.0.0")
    val version: String
)