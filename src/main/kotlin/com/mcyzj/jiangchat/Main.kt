package com.mcyzj.jiangchat

import com.mcyzj.jiangchat.chat.Command
import com.mcyzj.jiangchat.chat.Listener
import com.mcyzj.jiangchat.database.DatabaseApi
import com.mcyzj.jiangchat.database.MysqlDatabaseApi
import com.mcyzj.jiangchat.database.SQLiteDatabaseApi
import com.xbaimiao.easylib.EasyPlugin
import com.xbaimiao.easylib.module.chat.BuiltInConfiguration
import com.xbaimiao.easylib.module.utils.registerListener
import com.xbaimiao.easylib.task.EasyLibTask
import org.bukkit.Bukkit
import redis.clients.jedis.JedisPool

@Suppress("unused")
class Main : EasyPlugin(){

    companion object {
        lateinit var databaseApi: DatabaseApi
        lateinit var instance: Main
        lateinit var jedisPool: JedisPool
        lateinit var subscribeTask: EasyLibTask
        const val channel = "JiangPlayer"
    }

    private var config = BuiltInConfiguration("config.yml")
    var jiangPlayer = false

    override fun onLoad() {

    }

    override fun onEnable() {
        Bukkit.getConsoleSender().sendMessage("§aJiangChat 加载中....祈祷成功")
        saveDefaultConfig()
        instance = this

        config.options().copyDefaults(true)
        saveConfig()

        //加载数据库
        if (config.getString("Database").equals("db", true)) {
            databaseApi = SQLiteDatabaseApi()
        }else if (config.getString("Database").equals("mysql", true)) {
            databaseApi = MysqlDatabaseApi()
        }else{
            Bukkit.getConsoleSender().sendMessage("配置文件里似乎混入了奇奇怪怪的数据库类型")
        }

        //注册监听事件
        registerListener(Listener())
        //Papi.register()
        //注册指令
        Command().register()
        //挂钩提示
        if (Bukkit.getPluginManager().isPluginEnabled("JiangPlayer")) {
            Bukkit.getConsoleSender().sendMessage("§aJiangChat 检测到JiangPlayer，启用跨服聊天")
            jiangPlayer = true
        }
    }

    override fun onDisable() {

    }

    override fun reloadConfig() {
        super.reloadConfig()
    }
}