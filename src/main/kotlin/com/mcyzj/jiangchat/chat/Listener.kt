package com.mcyzj.jiangchat.chat

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mcyzj.jiangchat.Main
import com.mcyzj.jiangchat.database.PlayerData
import com.xbaimiao.easylib.module.utils.setProperty
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.spigotmc.event.player.PlayerSpawnLocationEvent
import java.util.*
import kotlin.collections.ArrayList

class Listener : Listener {

    @EventHandler
    fun onPlayerJoin(e: PlayerJoinEvent) {
        val player = e.player
        var playerData = Main.databaseApi.getPlayerData(player.uniqueId)
        if (playerData == null){
            playerData = PlayerData(
                player.name,
                player.uniqueId,
                arrayListOf(),
                "all"
            )
            Main.databaseApi.setPlayerData(playerData)
        }
        playerData.chatChannel = "all"
        Main.databaseApi.setPlayerData(playerData)
    }
    @EventHandler
    fun playerChat(e: AsyncPlayerChatEvent) {
        e.isCancelled = true
        e.player.sendMessage(ChatAPI.sendChat(e.player, e.message))
    }
}