package com.mcyzj.jiangchat.chat

import com.mcyzj.jiangchat.Main
import com.xbaimiao.easylib.module.command.command
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

class Command {
    private val mainCommand = Main.instance.config.getString("command.main")?: "chat"
    private val changeCommand = Main.instance.config.getString("command.fast")
    private val backCommand = Main.instance.config.getString("command.back")

    private val channelCreate = command<CommandSender>("create") {
        permission = "JiangChat.command.channel.create"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            if (args[0].isEmpty()){
                sender.sendMessage("群聊名称不能为空")
                sender.sendMessage("/$mainCommand channel create <群聊名称>")
                return@exec
            }
            ChatAPI.channelCreate((sender as Player).uniqueId, args[0])
            sender.sendMessage("创建群聊 ${args[0]}")
        }
    }

    private val channelChange = command<CommandSender>("change") {
        permission = "JiangChat.command.channel.change"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            if (args[0].isEmpty()){
                sender.sendMessage("群聊不能为空")
                sender.sendMessage("/$mainCommand channel change <群聊ID/名称>")
                if (changeCommand != null) {
                    sender.sendMessage("/$changeCommand <群聊名称>")
                }
                return@exec
            }
            val channel = try{
                args[0].toInt()
            }catch (_:Exception){
                args[0]
            }
            sender.sendMessage(ChatAPI.changeChannel((sender as Player).uniqueId, channel))
        }
    }

    private val channelDelete = command<CommandSender>("delete") {
        permission = "JiangChat.command.channel.delete"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            if (args[0].isEmpty()){
                sender.sendMessage("群聊名称不能为空")
                sender.sendMessage("/$mainCommand channel delete <群聊名称>")
                return@exec
            }
            val channel = try{
                args[0].toInt()
            }catch (_:Exception){
                args[0]
            }
            sender.sendMessage(ChatAPI.deleteChannel((sender as Player).uniqueId, channel))
        }
    }

    private val channelKick = command<CommandSender>("kick") {
        permission = "JiangChat.command.channel.kick"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            if (args[0].isEmpty()){
                sender.sendMessage("玩家不能为空")
                sender.sendMessage("/$mainCommand channel kick <玩家名称/UUID>")
                return@exec
            }
            val channel = try{
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel.toInt()
            }catch (_:Exception){
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel
            }
            sender.sendMessage(ChatAPI.kickPlayerChannel((sender as Player).uniqueId, channel, args[0]))
        }
    }

    private val upToManagerChannel = command<CommandSender>("manager") {
        permission = "JiangChat.command.channel.manager"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            if (args[0].isEmpty()){
                sender.sendMessage("玩家不能为空")
                sender.sendMessage("/$mainCommand channel manager <玩家名称/UUID>")
                return@exec
            }
            val channel = try{
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel.toInt()
            }catch (_:Exception){
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel
            }
            sender.sendMessage(ChatAPI.upToManagerChannel((sender as Player).uniqueId, channel, args[0]))
        }
    }

    private val upToOwnerChannel = command<CommandSender>("owner") {
        permission = "JiangChat.command.channel.owner"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            if (args[0].isEmpty()){
                sender.sendMessage("玩家不能为空")
                sender.sendMessage("/$mainCommand channel owner <玩家名称/UUID>")
                return@exec
            }
            val channel = try{
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel.toInt()
            }catch (_:Exception){
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel
            }
            sender.sendMessage(ChatAPI.upToOwnerChannel((sender as Player).uniqueId, channel, args[0]))
        }
    }

    private val upToPlayerChannel = command<CommandSender>("player") {
        permission = "JiangChat.command.channel.owner"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            if (args[0].isEmpty()){
                sender.sendMessage("玩家不能为空")
                sender.sendMessage("/$mainCommand channel player <玩家名称/UUID>")
                return@exec
            }
            val channel = try{
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel.toInt()
            }catch (_:Exception){
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel
            }
            sender.sendMessage(ChatAPI.upToPlayerChannel((sender as Player).uniqueId, channel, args[0]))
        }
    }

    private val upToNoChatChannel = command<CommandSender>("noChat") {
        permission = "JiangChat.command.channel.owner"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            if (args[0].isEmpty()){
                sender.sendMessage("玩家不能为空")
                sender.sendMessage("/$mainCommand channel noChat <玩家名称/UUID>")
                return@exec
            }
            val channel = try{
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel.toInt()
            }catch (_:Exception){
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel
            }
            sender.sendMessage(ChatAPI.upToNoChatChannel((sender as Player).uniqueId, channel, args[0]))
        }
    }

    private val changeApplyMode = command<CommandSender>("applyMode") {
        permission = "JiangChat.command.channel.applyMode"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            val channel = try{
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel.toInt()
            }catch (_:Exception){
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel
            }
            sender.sendMessage(ChatAPI.changeApplyChannel((sender as Player).uniqueId, channel))
        }
    }

    private val addChannel = command<CommandSender>("add") {
        permission = "JiangChat.command.channel.add"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            if (args[0].isEmpty()){
                sender.sendMessage("群聊ID不能为空")
                sender.sendMessage("/$mainCommand channel applyMode <群聊ID>")
                return@exec
            }
            val channel = try{
                args[0].toInt()
            }catch (_:Exception){
                sender.sendMessage("请确保您输入的是群聊ID")
                return@exec
            }
            sender.sendMessage(ChatAPI.applyChannel((sender as Player).uniqueId, channel))
        }
    }

    private val passApply = command<CommandSender>("passApply") {
        permission = "JiangChat.command.channel.passApply"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            if (args[0].isEmpty()){
                sender.sendMessage("玩家不能为空")
                sender.sendMessage("/$mainCommand channel passApply <玩家名称/UUID>")
                return@exec
            }
            val channel = try{
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel.toInt()
            }catch (_:Exception){
                sender.sendMessage("请确保你的聊天框指向为群聊")
                return@exec
            }
            sender.sendMessage(ChatAPI.passChannel((sender as Player).uniqueId, channel, args[0]))
        }
    }

    private val refuseApply = command<CommandSender>("refuseApply") {
        permission = "JiangChat.command.channel.refuseApply"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            if (args[0].isEmpty()){
                sender.sendMessage("玩家不能为空")
                sender.sendMessage("/$mainCommand channel passApply <玩家名称/UUID>")
                return@exec
            }
            val channel = try{
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel.toInt()
            }catch (_:Exception){
                sender.sendMessage("请确保你的聊天框指向为群聊")
                return@exec
            }
            sender.sendMessage(ChatAPI.refuseChannel((sender as Player).uniqueId, channel, args[0]))
        }
    }

    private val exitChannel = command<CommandSender>("exit") {
        permission = "JiangChat.command.channel.exit"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            val channel = try{
                Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!.chatChannel.toInt()
            }catch (_:Exception){
                sender.sendMessage("请确保你的聊天框指向为群聊")
                return@exec
            }
            sender.sendMessage(ChatAPI.exitChannel((sender as Player).uniqueId, channel))
        }
    }

    private val channel = command<CommandSender>("channel") {
        permission = "JiangChat.command.channel"
        exec {
            sub(channelCreate)
            sub(channelChange)
            sub(channelDelete)
            sub(channelKick)
            sub(upToOwnerChannel)
            sub(upToManagerChannel)
            sub(upToPlayerChannel)
            sub(upToNoChatChannel)
            sub(changeApplyMode)
            sub(addChannel)
            sub(passApply)
            sub(refuseApply)
            sub(exitChannel)
        }
    }

    private val setMaxPlayerChannel = command<CommandSender>("maxPlayer") {
        permission = "JiangChat.command.admin.maxPlayer"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            if (args.size < 1){
                sender.sendMessage("群聊ID不能为空")
                sender.sendMessage("/$mainCommand admin maxPlayer <群聊ID> <最大玩家数量>")
                return@exec
            }
            if (args.size < 2){
                sender.sendMessage("数量不能为空")
                sender.sendMessage("/$mainCommand admin maxPlayer <群聊ID> <最大玩家数量>")
                return@exec
            }
            val channel = try{
                args[0].toInt()
            }catch (_:Exception){
                sender.sendMessage("请确保您输入的是群聊ID")
                return@exec
            }
            val value = try{
                args[1].toInt()
            }catch (_:Exception){
                sender.sendMessage("请确保您输入的是数字")
                return@exec
            }
            sender.sendMessage(ChatAPI.changeMaxPlayerChannel(value, channel))
        }
    }

    private val admin = command<CommandSender>("admin") {
        permission = "JiangChat.command.admin"
        exec {
            sub(setMaxPlayerChannel)
        }
    }

    private val showChatId = command<CommandSender>("chatId") {
        permission = "JiangChat.command.show.chatId"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            val playerData = Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!
            sender.sendMessage(playerData.chatChannel)
        }
    }

    private val showChatName = command<CommandSender>("chatName") {
        permission = "JiangChat.command.show.chatName"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            val playerData = Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!
            if (playerData.chatChannel == "all"){
                sender.sendMessage("all")
            }
            val channelData = Main.databaseApi.getChannelData(playerData.chatChannel.toInt())!!
            sender.sendMessage(channelData.name)
        }
    }

    private val showChatOwner = command<CommandSender>("chatOwner") {
        permission = "JiangChat.command.show.chatOwner"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            val playerData = Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!
            if (playerData.chatChannel == "all"){
                sender.sendMessage("server")
            }
            val channelData = Main.databaseApi.getChannelData(playerData.chatChannel.toInt())!!
            sender.sendMessage(channelData.owner.toString())
            if (Main.instance.jiangPlayer){
                sender.sendMessage(com.mcyzj.jiangplayer.Main.databaseApi.getPlayerName(channelData.owner)?:channelData.owner.toString())
            } else {
                sender.sendMessage(Bukkit.getOfflinePlayer(channelData.owner).name)

            }
        }
    }

    private val showChatManager = command<CommandSender>("chatManager") {
        permission = "JiangChat.command.show.chatManager"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            val playerData = Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!
            if (playerData.chatChannel == "all"){
                sender.sendMessage("op")
            }
            val channelData = Main.databaseApi.getChannelData(playerData.chatChannel.toInt())!!
            if (Main.instance.jiangPlayer){
                val manager = arrayListOf<String>()
                for (uuid in channelData.uuidPowerMap.keys){
                    if (channelData.uuidPowerMap[uuid] == "manager") {
                        manager.add(com.mcyzj.jiangplayer.Main.databaseApi.getPlayerName(uuid) ?: uuid.toString())
                    }
                }
                sender.sendMessage(manager.joinToString(","))
            } else {
                val manager = arrayListOf<String>()
                for (uuid in channelData.uuidPowerMap.keys) {
                    if (channelData.uuidPowerMap[uuid] == "manager") {
                        manager.add(Bukkit.getOfflinePlayer(uuid).name ?: uuid.toString())
                    }
                }
                sender.sendMessage(manager.joinToString(","))
            }
        }
    }

    private val showChatPlayer = command<CommandSender>("chatPlayer") {
        permission = "JiangChat.command.show.chatPlayer"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            val playerData = Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!
            if (playerData.chatChannel == "all"){
                sender.sendMessage("op")
            }
            val channelData = Main.databaseApi.getChannelData(playerData.chatChannel.toInt())!!
            if (Main.instance.jiangPlayer){
                val player = arrayListOf<String>()
                for (uuid in channelData.uuidPowerMap.keys) {
                    if (channelData.uuidPowerMap[uuid] == "player") {
                        player.add(com.mcyzj.jiangplayer.Main.databaseApi.getPlayerName(uuid) ?: uuid.toString())
                    }
                }
                sender.sendMessage(player.joinToString(","))
            } else {
                val player = arrayListOf<String>()
                for (uuid in channelData.uuidPowerMap.keys) {
                    if (channelData.uuidPowerMap[uuid] == "player") {
                        player.add(Bukkit.getOfflinePlayer(uuid).name ?: uuid.toString())
                    }
                }
                sender.sendMessage(player.joinToString(","))
            }
        }
    }

    private val showChatNoChat = command<CommandSender>("chatNoChat") {
        permission = "JiangChat.command.show.chatNoChat"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            val playerData = Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!
            if (playerData.chatChannel == "all"){
                sender.sendMessage("op")
            }
            val channelData = Main.databaseApi.getChannelData(playerData.chatChannel.toInt())!!
            if (Main.instance.jiangPlayer){
                val noChat = arrayListOf<String>()
                for (uuid in channelData.uuidPowerMap.keys) {
                    if (channelData.uuidPowerMap[uuid] == "noChat") {
                        noChat.add(com.mcyzj.jiangplayer.Main.databaseApi.getPlayerName(uuid) ?: uuid.toString())
                    }
                }
                sender.sendMessage(noChat.joinToString(","))
            } else {
                val noChat = arrayListOf<String>()
                for (uuid in channelData.uuidPowerMap.keys){
                    if (channelData.uuidPowerMap[uuid] == "noChat") {
                        noChat.add(Bukkit.getOfflinePlayer(uuid).name ?: uuid.toString())
                    }
                }
                sender.sendMessage(noChat.joinToString(","))
            }
        }
    }

    private val showChatApply = command<CommandSender>("chatApply") {
        permission = "JiangChat.command.show.chatApply"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            val playerData = Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!
            if (playerData.chatChannel == "all"){
                sender.sendMessage("op")
            }
            val channelData = Main.databaseApi.getChannelData(playerData.chatChannel.toInt())!!
            if (Main.instance.jiangPlayer){
                val apply = arrayListOf<String>()
                for (uuid in channelData.apply){
                    apply.add(com.mcyzj.jiangplayer.Main.databaseApi.getPlayerName(uuid)?:uuid.toString())
                }
                sender.sendMessage(apply.joinToString(","))
            } else {
                val apply = arrayListOf<String>()
                for (uuid in channelData.apply){
                    apply.add(Bukkit.getOfflinePlayer(uuid).name?:uuid.toString())
                }
                sender.sendMessage(apply.joinToString(","))
            }
        }
    }

    private val showChatAllPlayer = command<CommandSender>("chatAllPlayer") {
        permission = "JiangChat.command.show.chatAllPlayer"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            val playerData = Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!
            if (playerData.chatChannel == "all"){
                sender.sendMessage("all")
            }
            val channelData = Main.databaseApi.getChannelData(playerData.chatChannel.toInt())!!
            val player = channelData.uuidPowerMap.keys
            if (Main.instance.jiangPlayer){
                val allPlayer = arrayListOf<String>()
                for (uuid in player){
                    allPlayer.add(com.mcyzj.jiangplayer.Main.databaseApi.getPlayerName(uuid)?:uuid.toString())
                }
                sender.sendMessage("-------[最大人数: ${channelData.maxPlayer}|当前人数: ${player.size}]------")
                sender.sendMessage(allPlayer.joinToString(","))
                sender.sendMessage("-----------------------")
            } else {
                val allPlayer = arrayListOf<String>()
                for (uuid in player){
                    allPlayer.add(Bukkit.getOfflinePlayer(uuid).name?:uuid.toString())
                }
                sender.sendMessage("-------[最大人数: ${channelData.maxPlayer}|当前人数: ${player.size}]------")
                sender.sendMessage(allPlayer.joinToString(","))
                sender.sendMessage("-----------------------")
            }
        }
    }

    private val showChatJoin = command<CommandSender>("chatJoin") {
        permission = "JiangChat.command.show.chatJoin"
        exec {
            if (sender !is Player){
                sender.sendMessage("需要玩家操作")
                return@exec
            }
            val playerData = Main.databaseApi.getPlayerData((sender as Player).uniqueId)!!
            val joinList = playerData.joinChannel
            val nameList = ArrayList<String>()
            for (join in joinList){
                val channel = Main.databaseApi.getChannelData(join)
                if (channel == null){
                    joinList.remove(join)
                    continue
                }
                nameList.add("${channel.name}[${join}]")
            }
            playerData.joinChannel = joinList
            Main.databaseApi.setPlayerData(playerData)
            sender.sendMessage("你加入了以下群聊")
            sender.sendMessage(nameList.joinToString(","))
        }
    }

    private val reload = command<CommandSender>("reload") {
        permission = "JiangChat.command.admin"
        exec {
            Main.instance.reloadConfig()
            sender.sendMessage("JiangChat重载配置")
        }
    }

    private val show = command<CommandSender>("show") {
        permission = "JiangChat.command.show"
        exec {
            sub(showChatId)
            sub(showChatName)
            sub(showChatAllPlayer)
            sub(showChatOwner)
            sub(showChatManager)
            sub(showChatPlayer)
            sub(showChatNoChat)
            sub(showChatApply)
            sub(showChatJoin)
        }
    }

    private val commandRoot = command<CommandSender>(mainCommand) {
        permission = "JiangChat.command"
        sub(admin)
        sub(channel)
        sub(reload)
        sub(show)
    }

    fun register(){
        commandRoot.register()
        if (changeCommand != null){
            command<CommandSender>(changeCommand) {
                permission = "JiangChat.command.fast.change"
                exec {
                    if (sender !is Player){
                        sender.sendMessage("需要玩家操作")
                        return@exec
                    }
                    if (args[0].isEmpty()){
                        sender.sendMessage("群聊名称不能为空")
                        sender.sendMessage("/$mainCommand channel change <群聊名称>")
                        sender.sendMessage("/$changeCommand <群聊名称>")
                        return@exec
                    }
                    val channel = try{
                        args[0].toInt()
                    }catch (_:Exception){
                        args[0]
                    }
                    sender.sendMessage(ChatAPI.changeChannel((sender as Player).uniqueId, channel))
                }
            }.register()
        }
        if (backCommand != null){
            command<CommandSender>(backCommand) {
                permission = "JiangChat.command.fast.back"
                exec {
                    if (sender !is Player){
                        sender.sendMessage("需要玩家操作")
                        return@exec
                    }
                    sender.sendMessage(ChatAPI.changeChannel((sender as Player).uniqueId, "all"))
                }
            }.register()
        }
    }
}