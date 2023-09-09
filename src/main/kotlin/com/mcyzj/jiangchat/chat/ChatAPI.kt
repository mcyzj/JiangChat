package com.mcyzj.jiangchat.chat

import com.mcyzj.jiangchat.Main
import com.mcyzj.jiangchat.database.ChannelData
import com.mcyzj.jiangchat.tool.JiangPlayerFast
import com.mcyzj.jiangplayer.redis.RedisManager
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.UUID

object ChatAPI {
    private val database = Main.databaseApi
    fun sendChat(sender: Player, msg: String): String {
        val senderData = database.getPlayerData(sender.uniqueId)!!
        var id: Any
        id = senderData.chatChannel
        Bukkit.getConsoleSender().sendMessage(senderData.chatChannel)
        Bukkit.getConsoleSender().sendMessage(id)
        if (id == "all") {
            val sendMsg = createMsg(msg, "公共频道", "玩家", sender, Main.instance.config.getString("msg.all")!!)
            val playerList = Bukkit.getOnlinePlayers()
            for (player in playerList) {
                player.sendMessage(sendMsg)
            }
        } else {
            id = id.toInt()
            val channel = database.getChannelData(id)
            if (channel == null) {
                senderData.joinChannel.remove(id)
                senderData.chatChannel = "all"
                database.setPlayerData(senderData)
                return "该群聊已解散"
            }
            if (channel.uuidPowerMap[sender.uniqueId] == "noChat") {
                return "您没有资格在目标群聊发言"
            }
            val reader = channel.uuidPowerMap.keys
            val power = when(channel.uuidPowerMap[sender.uniqueId]){
                "owner" -> {"群主"}
                "manager" -> {"管理员"}
                "player" -> {"成员"}
                "noChat" -> {"禁言"}
                else -> {"成员"}
            }
            val sendMsg = createMsg(msg, channel.name, power, sender, Main.instance.config.getString("msg.chat")!!)
            for (listener in reader) {
                if (Main.instance.jiangPlayer) {
                    val server = RedisManager.getPlayerServerByUUID(listener) ?: continue
                    RedisManager.sandMsgToPlayer(listener, server, sendMsg)
                } else {
                    val player = Bukkit.getPlayer(listener) ?: continue
                    player.sendMessage(sendMsg)
                }
            }
        }
        return "发送成功"
    }

    private fun createMsg(msg: String, channel: String, power: String, sender: Player, template: String): String {
        var create = template.replace("{player}", sender.name)
        create = create.replace("{msg}", msg)
        create = create.replace("{channel_name}", channel)
        create = create.replace("{channel_power}", power)
        return create
    }

    fun channelCreate(owner: UUID, name: String) {
        val map = HashMap<UUID, String>()
        map[owner] = name
        val channelData = ChannelData(
            -1,
            Main.instance.config.getInt("channel.maxPlayer"),
            name,
            owner,
            false,
            arrayListOf(),
            map
        )
        val id = database.setChannelData(channelData)
        Bukkit.getConsoleSender().sendMessage(id.toString())
        val playerData = database.getPlayerData(owner)!!
        playerData.joinChannel.add(id)
        playerData.chatChannel = id.toString()
        database.setPlayerData(playerData)
    }

    fun changeChannel(player: UUID, to: Any): String {
        val playerData = database.getPlayerData(player)!!
        if (to is Int) {
            if (to !in playerData.joinChannel) {
                return "不在该群聊内"
            }
            val channelData = database.getChannelData(to)
            if (channelData == null) {
                playerData.joinChannel.remove(to)
                database.setPlayerData(playerData)
                return "该群聊已解散"
            }
            playerData.chatChannel = to.toString()
            database.setPlayerData(playerData)
            return "切换发送信框至 ${channelData.name}"
        }
        if (to == "all") {
            playerData.chatChannel = to.toString()
            database.setPlayerData(playerData)
            return "切换发送信框至公共"
        }
        val channelList = database.getChannelDataByName(to.toString()) ?: return "没有名为${to}的群聊"
        val joinList = ArrayList<ChannelData>()
        for (channel in channelList) {
            if (channel.id in playerData.joinChannel) {
                joinList.add(channel)
            }
        }
        if (joinList.size == 0) {
            return "没有名为${to}的群聊"
        }
        if (joinList.size > 1) {
            return "有${joinList.size}个名称相同的群聊，请使用id切换"
        }
        val channel = joinList[0]
        if (channel.id !in playerData.joinChannel) {
            return "不在该群聊内"
        }
        playerData.chatChannel = channel.id.toString()
        database.setPlayerData(playerData)
        return "切换发送信框至 ${channel.name}"
    }

    fun deleteChannel(player: UUID, to: Any): String {
        val playerData = database.getPlayerData(player)!!
        if (to is Int) {
            if (to !in playerData.joinChannel) {
                return "无法找到群聊"
            }
            val channelData = database.getChannelData(to)
            if (channelData == null) {
                playerData.joinChannel.remove(to)
                database.setPlayerData(playerData)
                return "该群聊已解散"
            }
            if (player != channelData.owner) {
                return "你不是群主"
            }
            database.deleteChannelData(channelData)
            return "成功解散群聊 ${channelData.name}"
        }
        if (to == "all") {
            return "您认真的？"
        }
        val channelList = database.getChannelDataByName(to.toString()) ?: return "没有名为${to}的群聊"
        val joinList = ArrayList<ChannelData>()
        for (channel in channelList) {
            if (channel.id in playerData.joinChannel) {
                joinList.add(channel)
            }
        }
        if (joinList.size == 0) {
            return "没有名为${to}的群聊"
        }
        if (joinList.size > 1) {
            return "有${joinList.size}个名称相同的群聊，请使用id解散"
        }
        val channelData = joinList[0]
        if (channelData.id !in playerData.joinChannel) {
            return "不在该群聊内"
        }
        if (player != channelData.owner) {
            return "你不是群主"
        }
        database.deleteChannelData(channelData)
        return "成功解散群聊 ${channelData.name}"
    }

    fun kickPlayerChannel(player: UUID, to: Any, kick: String): String {
        val kickPlayer = JiangPlayerFast.getPlayer(kick) ?: return "无法找到玩家${kick}"
        val playerData = database.getPlayerData(player)!!
        if (to is Int) {
            if (to !in playerData.joinChannel) {
                return "不在该群聊内"
            }
            val channelData = database.getChannelData(to)
            if (channelData == null) {
                playerData.joinChannel.remove(to)
                database.setPlayerData(playerData)
                return "该群聊已解散"
            }
            val uuidPowerMap = channelData.uuidPowerMap
            if (uuidPowerMap[kickPlayer.uuid] == null){
                return "群内没有该玩家"
            }
            if ((player == channelData.owner).or(uuidPowerMap[player] == "manager")) {
                if ((uuidPowerMap[kickPlayer.uuid] == "noChat").or(uuidPowerMap[kickPlayer.uuid] == "player")) {
                    val kickData = database.getPlayerData(kickPlayer.uuid)!!
                    kickData.joinChannel.remove(channelData.id)
                    if (kickData.chatChannel != "all") {
                        if (kickData.chatChannel.toInt() == channelData.id) {
                            kickData.chatChannel = "all"
                        }
                    }
                    database.setPlayerData(kickData)
                    uuidPowerMap.remove(kickPlayer.uuid)
                    channelData.uuidPowerMap = uuidPowerMap
                    database.setChannelData(channelData)
                    if (kickPlayer.online){
                        Bukkit.getPlayer(kickPlayer.uuid)!!.sendMessage("你被踢出了 ${channelData.name}")
                    }
                    return "成功踢出成员 ${kickData.name}"
                } else if (uuidPowerMap[kickPlayer.uuid] == "manager") {
                    if (player != channelData.owner) {
                        return "你不是群主"
                    }
                    val kickData = database.getPlayerData(kickPlayer.uuid)!!
                    kickData.joinChannel.remove(channelData.id)
                    if (kickData.chatChannel != "all") {
                        if (kickData.chatChannel.toInt() == channelData.id) {
                            kickData.chatChannel = "all"
                        }
                    }
                    database.setPlayerData(kickData)
                    uuidPowerMap.remove(kickPlayer.uuid)
                    channelData.uuidPowerMap = uuidPowerMap
                    database.setChannelData(channelData)
                    if (kickPlayer.online){
                        Bukkit.getPlayer(kickPlayer.uuid)!!.sendMessage("你被踢出了 ${channelData.name}")
                    }
                    return "成功踢出管理员 ${kickData.name}"
                }
            } else {
                return "你不是管理员"
            }
        }
        if (to == "all") {
            return "您认真的？"
        }
        val channelList = database.getChannelDataByName(to.toString()) ?: return "没有名为${to}的群聊"
        val joinList = ArrayList<ChannelData>()
        for (channel in channelList) {
            if (channel.id in playerData.joinChannel) {
                joinList.add(channel)
            }
        }
        if (joinList.size == 0) {
            return "没有名为${to}的群聊"
        }
        if (joinList.size > 1) {
            return "有${joinList.size}个名称相同的群聊，请使用id指定群聊"
        }
        val channelData = joinList[0]
        if (channelData.id !in playerData.joinChannel) {
            return "不在该群聊内"
        }
        val uuidPowerMap = channelData.uuidPowerMap
        if (uuidPowerMap[kickPlayer.uuid] == null){
            return "群内没有该玩家"
        }
        if ((uuidPowerMap[kickPlayer.uuid] == "noChat").or(uuidPowerMap[kickPlayer.uuid] == "player")) {
            val kickData = database.getPlayerData(kickPlayer.uuid)!!
            kickData.joinChannel.remove(channelData.id)
            if (kickData.chatChannel != "all") {
                if (kickData.chatChannel.toInt() == channelData.id) {
                    kickData.chatChannel = "all"
                }
            }
            database.setPlayerData(kickData)
            uuidPowerMap.remove(kickPlayer.uuid)
            channelData.uuidPowerMap = uuidPowerMap
            database.setChannelData(channelData)
            if (kickPlayer.online){
                Bukkit.getPlayer(kickPlayer.uuid)!!.sendMessage("你被踢出了 ${channelData.name}")
            }
            return "成功踢出成员 ${kickData.name}"
        } else if (uuidPowerMap[kickPlayer.uuid] == "manager") {
            if (player != channelData.owner) {
                return "你不是群主"
            }
            val kickData = database.getPlayerData(kickPlayer.uuid)!!
            kickData.joinChannel.remove(channelData.id)
            if (kickData.chatChannel != "all") {
                if (kickData.chatChannel.toInt() == channelData.id) {
                    kickData.chatChannel = "all"
                }
            }
            database.setPlayerData(kickData)
            uuidPowerMap.remove(kickPlayer.uuid)
            channelData.uuidPowerMap = uuidPowerMap
            database.setChannelData(channelData)
            if (kickPlayer.online){
                Bukkit.getPlayer(kickPlayer.uuid)!!.sendMessage("你被踢出了 ${channelData.name}")
            }
            return "成功踢出管理员 ${kickData.name}"
        }
        return "未知错误"
    }

    fun upToManagerChannel(player: UUID, to: Any, manage: String): String {
        val managePlayer = JiangPlayerFast.getPlayer(manage) ?: return "无法找到玩家${manage}"
        val playerData = database.getPlayerData(player)!!
        if (to is Int) {
            if (to !in playerData.joinChannel) {
                return "不在该群聊内"
            }
            val channelData = database.getChannelData(to)
            if (channelData == null) {
                playerData.joinChannel.remove(to)
                database.setPlayerData(playerData)
                return "该群聊已解散"
            }
            val uuidPowerMap = channelData.uuidPowerMap
            if (uuidPowerMap[managePlayer.uuid] == null){
                return "群内没有该玩家"
            }
            if (uuidPowerMap[player] == "owner"){
                when(uuidPowerMap[managePlayer.uuid]){
                    "owner" ->{
                        return "你真的想给您自己降级吗？"
                    }
                    "manager" ->{
                        return "他已经是一个管理员了"
                    }
                    "player" ->{
                        uuidPowerMap[managePlayer.uuid] = "manager"
                        channelData.uuidPowerMap = uuidPowerMap
                        database.setChannelData(channelData)
                        if (managePlayer.online){
                            Bukkit.getPlayer(managePlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主任命为管理员")
                        }
                        return "成功提拔 ${managePlayer.name} 为 ${channelData.name} 的管理员"
                    }
                    "noChat" ->{
                        uuidPowerMap[managePlayer.uuid] = "manager"
                        channelData.uuidPowerMap = uuidPowerMap
                        database.setChannelData(channelData)
                        if (managePlayer.online){
                            Bukkit.getPlayer(managePlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主任命为管理员")
                        }
                        return "成功提拔 ${managePlayer.name} 为 ${channelData.name} 的管理员"
                    }
                }
            }else{
                return "你不是群主"
            }
        }
        if (to == "all") {
            return "您认真的？"
        }
        val channelList = database.getChannelDataByName(to.toString()) ?: return "没有名为${to}的群聊"
        val joinList = ArrayList<ChannelData>()
        for (channel in channelList) {
            if (channel.id in playerData.joinChannel) {
                joinList.add(channel)
            }
        }
        if (joinList.size == 0) {
            return "没有名为${to}的群聊"
        }
        if (joinList.size > 1) {
            return "有${joinList.size}个名称相同的群聊，请使用id指定群聊"
        }
        val channelData = joinList[0]
        if (channelData.id !in playerData.joinChannel) {
            return "不在该群聊内"
        }
        val uuidPowerMap = channelData.uuidPowerMap
        if (uuidPowerMap[managePlayer.uuid] == null){
            return "群内没有该玩家"
        }
        if (uuidPowerMap[player] == "owner"){
            when(uuidPowerMap[managePlayer.uuid]){
                "owner" ->{
                    return "你真的想给您自己降级吗？"
                }
                "manager" ->{
                    return "他已经是一个管理员了"
                }
                "player" ->{
                    uuidPowerMap[managePlayer.uuid] = "manager"
                    channelData.uuidPowerMap = uuidPowerMap
                    database.setChannelData(channelData)
                    if (managePlayer.online){
                        Bukkit.getPlayer(managePlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主任命为管理员")
                    }
                    return "成功提拔 ${managePlayer.name} 为 ${channelData.name} 的管理员"
                }
                "noChat" ->{
                    uuidPowerMap[managePlayer.uuid] = "manager"
                    channelData.uuidPowerMap = uuidPowerMap
                    database.setChannelData(channelData)
                    if (managePlayer.online){
                        Bukkit.getPlayer(managePlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主任命为管理员")
                    }
                    return "成功提拔 ${managePlayer.name} 为 ${channelData.name} 的管理员"
                }
            }
        }else{
            return "你不是群主"
        }
        return "未知错误"
    }

    fun upToOwnerChannel(player: UUID, to: Any, owner: String): String {
        val ownerPlayer = JiangPlayerFast.getPlayer(owner) ?: return "无法找到玩家${owner}"
        val playerData = database.getPlayerData(player)!!
        if (to is Int) {
            if (to !in playerData.joinChannel) {
                return "不在该群聊内"
            }
            val channelData = database.getChannelData(to)
            if (channelData == null) {
                playerData.joinChannel.remove(to)
                database.setPlayerData(playerData)
                return "该群聊已解散"
            }
            val uuidPowerMap = channelData.uuidPowerMap
            if (uuidPowerMap[ownerPlayer.uuid] == null){
                return "群内没有该玩家"
            }
            if (uuidPowerMap[player] == "owner"){
                when(uuidPowerMap[ownerPlayer.uuid]){
                    "owner" ->{
                        return "嘿，插件作者MC鱼子酱可不希望你这么做"
                    }
                    "manager" ->{
                        uuidPowerMap[ownerPlayer.uuid] = "owner"
                        uuidPowerMap[player] = "manager"
                        channelData.uuidPowerMap = uuidPowerMap
                        channelData.owner = ownerPlayer.uuid
                        database.setChannelData(channelData)
                        if (ownerPlayer.online){
                            Bukkit.getPlayer(ownerPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主任命为群主")
                        }
                        return "成功移交 ${channelData.name} 的群主为 ${ownerPlayer.name}"
                    }
                    "player" ->{
                        uuidPowerMap[ownerPlayer.uuid] = "owner"
                        uuidPowerMap[player] = "manager"
                        channelData.uuidPowerMap = uuidPowerMap
                        channelData.owner = ownerPlayer.uuid
                        database.setChannelData(channelData)
                        if (ownerPlayer.online){
                            Bukkit.getPlayer(ownerPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主任命为群主")
                        }
                        return "成功移交 ${channelData.name} 的群主为 ${ownerPlayer.name}"
                    }
                    "noChat" ->{
                        uuidPowerMap[ownerPlayer.uuid] = "owner"
                        uuidPowerMap[player] = "manager"
                        channelData.uuidPowerMap = uuidPowerMap
                        channelData.owner = ownerPlayer.uuid
                        database.setChannelData(channelData)
                        if (ownerPlayer.online){
                            Bukkit.getPlayer(ownerPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主任命为群主")
                        }
                        return "成功移交 ${channelData.name} 的群主为 ${ownerPlayer.name}"
                    }
                }
            }else{
                return "你不是群主"
            }
        }
        if (to == "all") {
            return "您认真的？"
        }
        val channelList = database.getChannelDataByName(to.toString()) ?: return "没有名为${to}的群聊"
        val joinList = ArrayList<ChannelData>()
        for (channel in channelList) {
            if (channel.id in playerData.joinChannel) {
                joinList.add(channel)
            }
        }
        if (joinList.size == 0) {
            return "没有名为${to}的群聊"
        }
        if (joinList.size > 1) {
            return "有${joinList.size}个名称相同的群聊，请使用id指定群聊"
        }
        val channelData = joinList[0]
        if (channelData.id !in playerData.joinChannel) {
            return "不在该群聊内"
        }
        val uuidPowerMap = channelData.uuidPowerMap
        if (uuidPowerMap[ownerPlayer.uuid] == null){
            return "群内没有该玩家"
        }
        if (uuidPowerMap[player] == "owner"){
            when(uuidPowerMap[ownerPlayer.uuid]){
                "owner" ->{
                    return "嘿，插件作者MC鱼子酱可不希望你这么做"
                }
                "manager" ->{
                    uuidPowerMap[ownerPlayer.uuid] = "owner"
                    uuidPowerMap[player] = "manager"
                    channelData.uuidPowerMap = uuidPowerMap
                    channelData.owner = ownerPlayer.uuid
                    database.setChannelData(channelData)
                    if (ownerPlayer.online){
                        Bukkit.getPlayer(ownerPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主任命为群主")
                    }
                    return "成功移交 ${channelData.name} 的群主为 ${ownerPlayer.name}"
                }
                "player" ->{
                    uuidPowerMap[ownerPlayer.uuid] = "owner"
                    uuidPowerMap[player] = "manager"
                    channelData.uuidPowerMap = uuidPowerMap
                    channelData.owner = ownerPlayer.uuid
                    database.setChannelData(channelData)
                    if (ownerPlayer.online){
                        Bukkit.getPlayer(ownerPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主任命为群主")
                    }
                    return "成功移交 ${channelData.name} 的群主为 ${ownerPlayer.name}"
                }
                "noChat" ->{
                    uuidPowerMap[ownerPlayer.uuid] = "owner"
                    uuidPowerMap[player] = "manager"
                    channelData.uuidPowerMap = uuidPowerMap
                    channelData.owner = ownerPlayer.uuid
                    database.setChannelData(channelData)
                    if (ownerPlayer.online){
                        Bukkit.getPlayer(ownerPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主任命为群主")
                    }
                    return "成功移交 ${channelData.name} 的群主为 ${ownerPlayer.name}"
                }
            }
        }else{
            return "你不是群主"
        }
        return "未知错误"
    }

    fun upToPlayerChannel(player: UUID, to: Any, manage: String): String {
        val playerPlayer = JiangPlayerFast.getPlayer(manage) ?: return "无法找到玩家${manage}"
        val playerData = database.getPlayerData(player)!!
        if (to is Int) {
            if (to !in playerData.joinChannel) {
                return "不在该群聊内"
            }
            val channelData = database.getChannelData(to)
            if (channelData == null) {
                playerData.joinChannel.remove(to)
                database.setPlayerData(playerData)
                return "该群聊已解散"
            }
            val uuidPowerMap = channelData.uuidPowerMap
            if (uuidPowerMap[playerPlayer.uuid] == null){
                return "群内没有该玩家"
            }
            if (uuidPowerMap[player] == "owner"){
                when(uuidPowerMap[playerPlayer.uuid]){
                    "owner" ->{
                        return "嘿，插件作者MC鱼子酱可不希望你这么做"
                    }
                    "manager" ->{
                        uuidPowerMap[playerPlayer.uuid] = "player"
                        channelData.uuidPowerMap = uuidPowerMap
                        database.setChannelData(channelData)
                        if (playerPlayer.online){
                            Bukkit.getPlayer(playerPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主撤销了管理员")
                        }
                        return "成功撤销 ${playerPlayer.name} 在 ${channelData.name} 的管理员"
                    }
                    "player" ->{
                        return "${channelData.name} 可不是管理员"
                    }
                    "noChat" ->{
                        uuidPowerMap[playerPlayer.uuid] = "player"
                        channelData.uuidPowerMap = uuidPowerMap
                        database.setChannelData(channelData)
                        if (playerPlayer.online){
                            Bukkit.getPlayer(playerPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主解除禁言")
                        }
                        return "成功解除 ${playerPlayer.name} 在 ${channelData.name} 的禁言"
                    }
                }
            }else{
                return "你不是群主"
            }
        }
        if (to == "all") {
            return "您认真的？"
        }
        val channelList = database.getChannelDataByName(to.toString()) ?: return "没有名为${to}的群聊"
        val joinList = ArrayList<ChannelData>()
        for (channel in channelList) {
            if (channel.id in playerData.joinChannel) {
                joinList.add(channel)
            }
        }
        if (joinList.size == 0) {
            return "没有名为${to}的群聊"
        }
        if (joinList.size > 1) {
            return "有${joinList.size}个名称相同的群聊，请使用id指定群聊"
        }
        val channelData = joinList[0]
        if (channelData.id !in playerData.joinChannel) {
            return "不在该群聊内"
        }
        val uuidPowerMap = channelData.uuidPowerMap
        if (uuidPowerMap[playerPlayer.uuid] == null){
            return "群内没有该玩家"
        }
        if (uuidPowerMap[player] == "owner"){
            when(uuidPowerMap[playerPlayer.uuid]){
                "owner" ->{
                    return "嘿，插件作者MC鱼子酱可不希望你这么做"
                }
                "manager" ->{
                    uuidPowerMap[playerPlayer.uuid] = "player"
                    channelData.uuidPowerMap = uuidPowerMap
                    database.setChannelData(channelData)
                    if (playerPlayer.online){
                        Bukkit.getPlayer(playerPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主撤销了管理员")
                    }
                    return "成功撤销 ${playerPlayer.name} 在 ${channelData.name} 的管理员"
                }
                "player" ->{
                    return "${channelData.name} 可不是管理员"
                }
                "noChat" ->{
                    uuidPowerMap[playerPlayer.uuid] = "player"
                    channelData.uuidPowerMap = uuidPowerMap
                    database.setChannelData(channelData)
                    if (playerPlayer.online){
                        Bukkit.getPlayer(playerPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主解除禁言")
                    }
                    return "成功解除 ${playerPlayer.name} 在 ${channelData.name} 的禁言"
                }
            }
        }else{
            return "你不是群主"
        }
        return "未知错误"
    }

    fun upToNoChatChannel(player: UUID, to: Any, noChat: String): String {
        val noChatPlayer = JiangPlayerFast.getPlayer(noChat) ?: return "无法找到玩家${noChat}"
        val playerData = database.getPlayerData(player)!!
        if (to is Int) {
            if (to !in playerData.joinChannel) {
                return "不在该群聊内"
            }
            val channelData = database.getChannelData(to)
            if (channelData == null) {
                playerData.joinChannel.remove(to)
                database.setPlayerData(playerData)
                return "该群聊已解散"
            }
            val uuidPowerMap = channelData.uuidPowerMap
            if (uuidPowerMap[noChatPlayer.uuid] == null){
                return "群内没有该玩家"
            }
            if (uuidPowerMap[player] == "owner"){
                when(uuidPowerMap[noChatPlayer.uuid]){
                    "owner" ->{
                        return "嘿，插件作者MC鱼子酱可不希望你这么做"
                    }
                    "manager" ->{
                        uuidPowerMap[noChatPlayer.uuid] = "noChat"
                        channelData.uuidPowerMap = uuidPowerMap
                        database.setChannelData(channelData)
                        if (noChatPlayer.online){
                            Bukkit.getPlayer(noChatPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主禁言了")
                        }
                        return "成功禁言 ${noChatPlayer.name} 在 ${channelData.name}"
                    }
                    "player" ->{
                        uuidPowerMap[noChatPlayer.uuid] = "noChat"
                        channelData.uuidPowerMap = uuidPowerMap
                        database.setChannelData(channelData)
                        if (noChatPlayer.online){
                            Bukkit.getPlayer(noChatPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主禁言了")
                        }
                        return "成功禁言 ${noChatPlayer.name} 在 ${channelData.name}"
                    }
                    "noChat" ->{
                        uuidPowerMap[noChatPlayer.uuid] = "noChat"
                        channelData.uuidPowerMap = uuidPowerMap
                        database.setChannelData(channelData)
                        if (noChatPlayer.online){
                            Bukkit.getPlayer(noChatPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主禁言了")
                        }
                        return "成功禁言 ${noChatPlayer.name} 在 ${channelData.name}"
                    }
                }
            }else if (uuidPowerMap[player] == "manager"){
                when(uuidPowerMap[noChatPlayer.uuid]){
                    "owner" ->{
                        return "这位，你是不是想造群主的反？作者MC鱼子酱可以助你一臂之力[bushi]"
                    }
                    "manager" ->{
                        return "你不是群主"
                    }
                    "player" ->{
                        uuidPowerMap[noChatPlayer.uuid] = "noChat"
                        channelData.uuidPowerMap = uuidPowerMap
                        database.setChannelData(channelData)
                        if (noChatPlayer.online){
                            Bukkit.getPlayer(noChatPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主禁言了")
                        }
                        return "成功禁言 ${noChatPlayer.name} 在 ${channelData.name}"
                    }
                    "noChat" ->{
                        uuidPowerMap[noChatPlayer.uuid] = "noChat"
                        channelData.uuidPowerMap = uuidPowerMap
                        database.setChannelData(channelData)
                        if (noChatPlayer.online){
                            Bukkit.getPlayer(noChatPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主禁言了")
                        }
                        return "成功禁言 ${noChatPlayer.name} 在 ${channelData.name}"
                    }
                }
            }else{
                return "你不是群管理"
            }
        }
        if (to == "all") {
            return "您认真的？"
        }
        val channelList = database.getChannelDataByName(to.toString()) ?: return "没有名为${to}的群聊"
        val joinList = ArrayList<ChannelData>()
        for (channel in channelList) {
            if (channel.id in playerData.joinChannel) {
                joinList.add(channel)
            }
        }
        if (joinList.size == 0) {
            return "没有名为${to}的群聊"
        }
        if (joinList.size > 1) {
            return "有${joinList.size}个名称相同的群聊，请使用id指定群聊"
        }
        val channelData = joinList[0]
        if (channelData.id !in playerData.joinChannel) {
            return "不在该群聊内"
        }
        val uuidPowerMap = channelData.uuidPowerMap
        if (uuidPowerMap[noChatPlayer.uuid] == null){
            return "群内没有该玩家"
        }
        if (uuidPowerMap[player] == "owner"){
            when(uuidPowerMap[noChatPlayer.uuid]){
                "owner" ->{
                    return "嘿，插件作者MC鱼子酱可不希望你这么做"
                }
                "manager" ->{
                    uuidPowerMap[noChatPlayer.uuid] = "noChat"
                    channelData.uuidPowerMap = uuidPowerMap
                    database.setChannelData(channelData)
                    if (noChatPlayer.online){
                        Bukkit.getPlayer(noChatPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主禁言了")
                    }
                    return "成功禁言 ${noChatPlayer.name} 在 ${channelData.name}"
                }
                "player" ->{
                    uuidPowerMap[noChatPlayer.uuid] = "noChat"
                    channelData.uuidPowerMap = uuidPowerMap
                    database.setChannelData(channelData)
                    if (noChatPlayer.online){
                        Bukkit.getPlayer(noChatPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主禁言了")
                    }
                    return "成功禁言 ${noChatPlayer.name} 在 ${channelData.name}"
                }
                "noChat" ->{
                    uuidPowerMap[noChatPlayer.uuid] = "noChat"
                    channelData.uuidPowerMap = uuidPowerMap
                    database.setChannelData(channelData)
                    if (noChatPlayer.online){
                        Bukkit.getPlayer(noChatPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主禁言了")
                    }
                    return "成功禁言 ${noChatPlayer.name} 在 ${channelData.name}"
                }
            }
        }else if (uuidPowerMap[player] == "manager"){
            when(uuidPowerMap[noChatPlayer.uuid]){
                "owner" ->{
                    return "这位，你是不是想造群主的反？作者MC鱼子酱可以助你一臂之力[bushi]"
                }
                "manager" ->{
                    return "你不是群主"
                }
                "player" ->{
                    uuidPowerMap[noChatPlayer.uuid] = "noChat"
                    channelData.uuidPowerMap = uuidPowerMap
                    database.setChannelData(channelData)
                    if (noChatPlayer.online){
                        Bukkit.getPlayer(noChatPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主禁言了")
                    }
                    return "成功禁言 ${noChatPlayer.name} 在 ${channelData.name}"
                }
                "noChat" ->{
                    uuidPowerMap[noChatPlayer.uuid] = "noChat"
                    channelData.uuidPowerMap = uuidPowerMap
                    database.setChannelData(channelData)
                    if (noChatPlayer.online){
                        Bukkit.getPlayer(noChatPlayer.uuid)!!.sendMessage("你被 ${channelData.name} 的群主禁言了")
                    }
                    return "成功禁言 ${noChatPlayer.name} 在 ${channelData.name}"
                }
            }
        }else{
            return "你不是群管理"
        }
        return "未知错误"
    }

    fun changeApplyChannel(player: UUID, to: Any): String {
        val playerData = database.getPlayerData(player)!!
        if (to is Int) {
            if (to !in playerData.joinChannel) {
                return "不在该群聊内"
            }
            val channelData = database.getChannelData(to)
            if (channelData == null) {
                playerData.joinChannel.remove(to)
                database.setPlayerData(playerData)
                return "该群聊已解散"
            }
            val uuidPowerMap = channelData.uuidPowerMap
            return if (uuidPowerMap[player] == "owner"){
                channelData.needApply = !channelData.needApply
                database.setChannelData(channelData)
                "进群申请：${channelData.needApply}"
            }else if (uuidPowerMap[player] == "manager"){
                channelData.needApply = !channelData.needApply
                database.setChannelData(channelData)
                "进群申请：${channelData.needApply}"
            }else{
                "你不是群管理"
            }
        }
        if (to == "all") {
            return "您认真的？"
        }
        val channelList = database.getChannelDataByName(to.toString()) ?: return "没有名为${to}的群聊"
        val joinList = ArrayList<ChannelData>()
        for (channel in channelList) {
            if (channel.id in playerData.joinChannel) {
                joinList.add(channel)
            }
        }
        if (joinList.size == 0) {
            return "没有名为${to}的群聊"
        }
        if (joinList.size > 1) {
            return "有${joinList.size}个名称相同的群聊，请使用id指定群聊"
        }
        val channelData = joinList[0]
        if (channelData.id !in playerData.joinChannel) {
            return "不在该群聊内"
        }
        if (to !in playerData.joinChannel) {
            return "不在该群聊内"
        }
        val uuidPowerMap = channelData.uuidPowerMap
        return if (uuidPowerMap[player] == "owner"){
            channelData.needApply = !channelData.needApply
            database.setChannelData(channelData)
            "进群申请：${channelData.needApply}"
        }else if (uuidPowerMap[player] == "manager"){
            channelData.needApply = !channelData.needApply
            database.setChannelData(channelData)
            "进群申请：${channelData.needApply}"
        }else{
            "你不是群管理"
        }
    }

    fun applyChannel(player: UUID, to: Int): String {
        val channelData = database.getChannelData(to) ?: return "没有找到群聊"
        val uuidPowerMap = channelData.uuidPowerMap
        if (uuidPowerMap[player] != null){
            return "你已经是该群成员了"
        }
        if (uuidPowerMap.size+1 > channelData.maxPlayer){
            return "该群聊已经满员了"
        }
        return if (channelData.needApply) {
            channelData.apply.remove(player)
            channelData.apply.add(player)
            database.setChannelData(channelData)
            "成功发送申请"
        }else{
            addChannel(to, player)
            "成功加入群聊 ${channelData.name}"
        }
    }

    fun changeMaxPlayerChannel(max: Int, to: Int): String {
        val channelData = database.getChannelData(to) ?: return "没有找到群聊"
        channelData.maxPlayer = max
        database.setChannelData(channelData)
        return "成功设置群聊 ${channelData.name} 的人数上限为 ${max}"
    }

    private fun addChannel(to: Int, player: UUID){
        val channelData = database.getChannelData(to) ?: return
        val playerData = database.getPlayerData(player)!!
        val uuidPowerMap = channelData.uuidPowerMap
        if (uuidPowerMap[player] != null){
            return
        }
        channelData.apply.remove(player)
        channelData.uuidPowerMap[player] = "player"
        database.setChannelData(channelData)
        playerData.joinChannel.add(channelData.id)
        database.setPlayerData(playerData)
    }

    fun passChannel(player: UUID, to: Int, pass: String): String {
        val channelData = database.getChannelData(to) ?: return "没有找到群聊"
        val passPlayer = JiangPlayerFast.getPlayer(pass) ?: return "没有找到玩家 $pass"
        val passData = database.getPlayerData(passPlayer.uuid)!!
        val uuidPowerMap = channelData.uuidPowerMap
        if ((uuidPowerMap[player] == "manager").or(uuidPowerMap[player] == "owner")){
            if (passPlayer.uuid !in channelData.apply){
                return "${passPlayer.name} 并没有发出申请进群"
            }
            if (uuidPowerMap.size+1 > channelData.maxPlayer){
                return "群聊已经满员啦"
            }
            channelData.apply.remove(passPlayer.uuid)
            uuidPowerMap[passPlayer.uuid] = "player"
            channelData.uuidPowerMap = uuidPowerMap
            database.setChannelData(channelData)
            passData.joinChannel.add(channelData.id)
            database.setPlayerData(passData)
            if (passPlayer.online){
                Bukkit.getPlayer(passPlayer.uuid)!!.sendMessage("成功加入群聊 ${channelData.name}")
            }
            return "${passPlayer.name} 成功入群"
        }
        return "没有权限"
    }

    fun refuseChannel(player: UUID, to: Int, pass: String): String {
        val channelData = database.getChannelData(to) ?: return "没有找到群聊"
        val passPlayer = JiangPlayerFast.getPlayer(pass) ?: return "没有找到玩家 $pass"
        val uuidPowerMap = channelData.uuidPowerMap
        if ((uuidPowerMap[player] == "manager").or(uuidPowerMap[player] == "owner")){
            if (passPlayer.uuid !in channelData.apply){
                return "${passPlayer.name} 并没有发出申请进群"
            }
            channelData.apply.remove(passPlayer.uuid)
            database.setChannelData(channelData)
            if (passPlayer.online){
                Bukkit.getPlayer(passPlayer.uuid)!!.sendMessage("被拒绝加入群聊 ${channelData.name}")
            }
            return "拒绝 ${passPlayer.name} 入群"
        }
        return "没有权限"
    }

    fun exitChannel(player: UUID, to: Int): String {
        val channelData = database.getChannelData(to) ?: return "没有找到群聊"
        val playerData = database.getPlayerData(player)!!
        val uuidPowerMap = channelData.uuidPowerMap
        return if (uuidPowerMap[player] == "owner") {
            "如果你不想解散群聊，请先移交你的群主权限"
        } else {
            uuidPowerMap.remove(player)
            channelData.uuidPowerMap = uuidPowerMap
            playerData.joinChannel.remove(channelData.id)
            if (playerData.chatChannel == channelData.id.toString()){
                playerData.chatChannel = "all"
            }
            database.setPlayerData(playerData)
            database.setChannelData(channelData)
            "退出群聊 ${channelData.name}"
        }
    }
}