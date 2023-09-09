package com.mcyzj.jiangchat.database

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.j256.ormlite.dao.Dao
import com.xbaimiao.easylib.module.database.Ormlite
import java.util.*
import kotlin.collections.ArrayList


open class AbstractDatabaseApi(ormlite: Ormlite): DatabaseApi {
    private val channelTable: Dao<ChannelDao, Int> = ormlite.createDao(ChannelDao::class.java)
    private val playerTable: Dao<PlayerDao, Int> = ormlite.createDao(PlayerDao::class.java)

    override fun getPlayerData(uuid: UUID): PlayerData? {
        val queryBuilder = playerTable.queryBuilder()
        queryBuilder.where().eq("uuid", uuid)
        val playerDao = queryBuilder.queryForFirst() ?: return null
        val g = Gson()
        val data = g.fromJson(playerDao.data, JsonObject::class.java)
        val joinChannel = ArrayList<Int>()
        val joinChannelList = data.get("joinChannel").toString().replace(""""""","").split(",")
        for (str in joinChannelList){
            if (str.isEmpty()){
                continue
            }
            joinChannel.add(str.toInt())
        }
        return PlayerData(
            playerDao.name,
            uuid,
            joinChannel,
            data.get("chatChannel").toString().replace(""""""","")
        )
    }

    override fun setPlayerData(data: PlayerData) {
        val queryBuilder = playerTable.queryBuilder()
        queryBuilder.where().eq("uuid", data.uuid)
        var channelDao = queryBuilder.queryForFirst()
        val jsonData = JsonObject()
        jsonData.addProperty("joinChannel", data.joinChannel.joinToString(","))
        jsonData.addProperty("chatChannel", data.chatChannel)
        if (channelDao == null) {
            channelDao = PlayerDao()
            channelDao.name = data.name
            channelDao.uuid = data.uuid
            channelDao.data = jsonData.toString()
            playerTable.create(channelDao)
        } else {
            channelDao.name = data.name
            channelDao.data = jsonData.toString()
            playerTable.update(channelDao)
        }
    }

    override fun getChannelData(id: Int): ChannelData? {
        val queryBuilder = channelTable.queryBuilder()
        queryBuilder.where().eq("id", id)
        val channelDao = queryBuilder.queryForFirst() ?: return null
        val g = Gson()
        val data = g.fromJson(channelDao.data, JsonObject::class.java)
        val apply = ArrayList<UUID>()
        val uuidPowerMap = HashMap<UUID, String>()
        uuidPowerMap[UUID.fromString(data.get("owner").toString().replace(""""""",""))] = "owner"
        val managerList = data.get("manager").toString().replace(""""""","").split(",")
        for (uuidStr in managerList){
            if (uuidStr.isEmpty()){
                continue
            }
            uuidPowerMap[UUID.fromString(uuidStr)] = "manager"
        }
        val playerList = data.get("player").toString().replace(""""""","").split(",")
        for (uuidStr in playerList){
            if (uuidStr.isEmpty()){
                continue
            }
            uuidPowerMap[UUID.fromString(uuidStr)] = "player"
        }
        val noChatList = data.get("noChat").toString().replace(""""""","").split(",")
        for (uuidStr in noChatList){
            if (uuidStr.isEmpty()){
                continue
            }
            uuidPowerMap[UUID.fromString(uuidStr)] = "noChat"
        }
        val applyList = data.get("apply").toString().replace(""""""","").split(",")
        for (uuidStr in applyList){
            if (uuidStr.isEmpty()){
                continue
            }
            apply.add(UUID.fromString(uuidStr))
        }
        return ChannelData(
            channelDao.id,
            data.get("maxPlayer").toString().replace(""""""","").toInt(),
            channelDao.name,
            UUID.fromString(data.get("owner").toString().replace(""""""","")),
            data.get("needApply").toString().replace(""""""","").toBoolean(),
            apply,
            uuidPowerMap
        )
    }

    override fun getChannelDataByName(name: String): ArrayList<ChannelData>? {
        val queryBuilder = channelTable.queryBuilder()
        queryBuilder.where().eq("name", name)
        val channelList = queryBuilder.query() ?: return null
        val dataList = ArrayList<ChannelData>()
        for (channelDao in channelList) {
            val g = Gson()
            val data = g.fromJson(channelDao.data, JsonObject::class.java)
            val apply = ArrayList<UUID>()
            val uuidPowerMap = HashMap<UUID, String>()
            uuidPowerMap[UUID.fromString(data.get("owner").toString().replace(""""""",""))] = "owner"
            val managerList = data.get("manager").toString().replace(""""""","").split(",")
            for (uuidStr in managerList){
                if (uuidStr.isEmpty()){
                    continue
                }
                uuidPowerMap[UUID.fromString(uuidStr)] = "manager"
            }
            val playerList = data.get("player").toString().replace(""""""","").split(",")
            for (uuidStr in playerList){
                if (uuidStr.isEmpty()){
                    continue
                }
                uuidPowerMap[UUID.fromString(uuidStr)] = "player"
            }
            val noChatList = data.get("noChat").toString().replace(""""""","").split(",")
            for (uuidStr in noChatList){
                if (uuidStr.isEmpty()){
                    continue
                }
                uuidPowerMap[UUID.fromString(uuidStr)] = "noChat"
            }
            val applyList = data.get("apply").toString().replace(""""""","").split(",")
            for (uuidStr in applyList){
                if (uuidStr.isEmpty()){
                    continue
                }
                apply.add(UUID.fromString(uuidStr))
            }
            val channelData = ChannelData(
                channelDao.id,
                data.get("maxPlayer").toString().replace(""""""","").toInt(),
                channelDao.name,
                UUID.fromString(data.get("owner").toString().replace(""""""","")),
                data.get("needApply").toString().replace(""""""","").toBoolean(),
                apply,
                uuidPowerMap
            )
            dataList.add(channelData)
        }
        return dataList
    }

    override fun setChannelData(data: ChannelData):Int {
        val manager = arrayListOf<UUID>()
        val player = arrayListOf<UUID>()
        val noChat = arrayListOf<UUID>()
        val uuidPowerMap = data.uuidPowerMap
        for (key in uuidPowerMap.keys){
            when(uuidPowerMap[key]){
                "manager" ->{
                    manager.add(key)
                }
                "player" ->{
                    player.add(key)
                }
                "noChat" ->{
                    player.add(key)
                }
            }
        }
        val jsonData = JsonObject()
        jsonData.addProperty("maxPlayer", data.maxPlayer)
        jsonData.addProperty("owner", data.owner.toString())
        jsonData.addProperty("manager", manager.joinToString(","))
        jsonData.addProperty("player", player.joinToString(","))
        jsonData.addProperty("noChat", noChat.joinToString(","))
        jsonData.addProperty("needApply", data.needApply.toString())
        jsonData.addProperty("apply", data.apply.joinToString(","))
        val queryBuilder = channelTable.queryBuilder()
        queryBuilder.where().eq("id", data.id)
        var channelDao = queryBuilder.queryForFirst()
        return if (channelDao == null) {
            channelDao = ChannelDao()
            channelDao.name = data.name
            channelDao.data = jsonData.toString()
            channelTable.create(channelDao)
            channelDao.id
        } else {
            channelDao.name = data.name
            channelDao.data = jsonData.toString()
            channelTable.update(channelDao)
            channelDao.id
        }
    }
    override fun deleteChannelData(data: ChannelData) {
        val queryBuilder = channelTable.queryBuilder()
        queryBuilder.where().eq("id", data.id)
        channelTable.delete(queryBuilder.queryForFirst())
    }
}