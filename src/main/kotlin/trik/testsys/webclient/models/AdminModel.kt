package trik.testsys.webclient.models

import io.swagger.annotations.ApiModel
import io.swagger.annotations.ApiModelProperty


@ApiModel(description = "JSON with admin info.")
data class AdminModel(

    @ApiModelProperty(
        value = "Admin id",
        example = "1",
        position = 1
    )
    val id: Long,

    @ApiModelProperty(
        value = "Web user id",
        example = "1",
        position = 2
    )
    val webUserId: Long,

    @ApiModelProperty(
        value = "Access token",
        example = "cd02ffd07748e4346298154cb00d9a6f0c76c7ca37aa31391db814f0b2a2b7c2",
        position = 3
    )
    val accessToken: String
)
