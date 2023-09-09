package com.mcyzj.jiangchat.database

import java.util.UUID

data class PlayerData (
    var name: String,
    var uuid: UUID,
    var joinChannel: ArrayList<Int>,
    var chatChannel: String
)

data class ChannelData (
    var id: Int,
    var maxPlayer: Int,
    var name: String,//群聊名称
    var owner: UUID,//群主
    var needApply: Boolean,
    var apply: ArrayList<UUID>,//申请列表
    var uuidPowerMap: HashMap<UUID, String>//按照UUID排列的权限表
)