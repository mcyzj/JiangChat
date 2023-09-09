package com.mcyzj.jiangchat.tool

import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import java.util.UUID

object JiangPlayerFast {
    fun getPlayer(arg: String): JiangPlayer? {
        val onlinePlayer = getOnlinePlayer(arg)
        if(onlinePlayer != null){
            return JiangPlayer(
                true,
                onlinePlayer.name,
                onlinePlayer.uniqueId
            )
        }
        val offlinePlayer = getOfflinePlayer(arg)
        if(offlinePlayer != null){
            return JiangPlayer(
                false,
                offlinePlayer.name!!,
                offlinePlayer.uniqueId
            )
        }
        return null
    }
    private fun getOnlinePlayer(arg: String): Player?{
        var player = Bukkit.getPlayer(arg)
        if(player != null){
            return player
        }
        try{
            val uuid = UUID.fromString(arg)
            player = Bukkit.getPlayer(uuid)
            if(player != null){
                return player
            }
        }catch (_:Exception){}
        return null
    }
    private fun getOfflinePlayer(arg: String): OfflinePlayer? {
        val offlinePlayerList = Bukkit.getOfflinePlayers()
        try{
            for(offline in offlinePlayerList){
                if(offline.name == arg){
                    return offline
                }
            }
        }catch (e:Exception){
            throw e
        }
        try{
            val uuid = UUID.fromString(arg)
            for(offline in offlinePlayerList){
                if(offline.uniqueId == uuid){
                    return offline as Player
                }
            }
        }catch (_:Exception){}
        return null
    }

    fun getPlayerByUUID(arg: UUID): JiangPlayer? {
        val onlinePlayer = getOnlinePlayerByUUID(arg)
        if(onlinePlayer != null){
            return JiangPlayer(
                true,
                onlinePlayer.name,
                onlinePlayer.uniqueId
            )
        }
        val offlinePlayer = getOfflinePlayerByUUID(arg)
        if(offlinePlayer != null){
            return JiangPlayer(
                false,
                offlinePlayer.name!!,
                offlinePlayer.uniqueId
            )
        }
        return null
    }
    private fun getOnlinePlayerByUUID(arg: UUID): Player?{
        try{
            val player = Bukkit.getPlayer(arg)
            if(player != null){
                return player
            }
        }catch (_:Exception){}
        return null
    }
    private fun getOfflinePlayerByUUID(arg: UUID): OfflinePlayer? {
        val offlinePlayerList = Bukkit.getOfflinePlayers()
        try{
            for(offline in offlinePlayerList){
                if(offline.uniqueId == arg){
                    return offline as Player
                }
            }
        }catch (_:Exception){}
        return null
    }
}

data class JiangPlayer (
    val online: Boolean,

    val name: String,
    val uuid: UUID
)