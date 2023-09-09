package com.mcyzj.jiangchat.database

import com.j256.ormlite.field.DataType
import com.j256.ormlite.field.DatabaseField
import com.j256.ormlite.table.DatabaseTable
import java.util.*

@DatabaseTable(tableName = "JiangChat_channel")
class ChannelDao {
    @DatabaseField(generatedId = true)
    var id: Int = 0
    //群聊名称
    @DatabaseField(dataType = DataType.LONG_STRING, canBeNull = false, columnName = "name")
    lateinit var name: String
    //群聊信息，使用json
    @DatabaseField(dataType = DataType.LONG_STRING, canBeNull = false, columnName = "data")
    lateinit var data: String
}

@DatabaseTable(tableName = "JiangChat_player")
class PlayerDao {
    @DatabaseField(generatedId = true)
    var id: Int = 0
    //玩家名称
    @DatabaseField(dataType = DataType.LONG_STRING, canBeNull = false, columnName = "name")
    lateinit var name: String
    //玩家uuid
    @DatabaseField(dataType = DataType.UUID, canBeNull = false, columnName = "uuid")
    lateinit var uuid: UUID
    //玩家数据
    @DatabaseField(dataType = DataType.LONG_STRING,canBeNull = false, columnName = "data")
    lateinit var data: String
}