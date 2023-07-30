package trik.testsys.webclient.models

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty


@ApiModel(description = "JSON with response status and message.")
data class ResponseMessage(

    @ApiModelProperty(
        value = "Response status", example = "200",
        required = true, position = 1
    )
    val status: Long,

    @ApiModelProperty(
        value = "Response message", example = "Access granted.",
        required = true, position = 2
    )
    val message: String
)