package com.mcyzj.jiangchat.database

import java.util.*

interface DatabaseApi {
    fun getPlayerData(uuid: UUID): PlayerData?
    fun setPlayerData(data: PlayerData)
    fun getChannelData(id: Int): ChannelData?
    fun getChannelDataByName(name: String): ArrayList<ChannelData>?
    fun setChannelData(data: ChannelData): Int
    fun deleteChannelData(data: ChannelData)
}