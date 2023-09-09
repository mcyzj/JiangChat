package com.mcyzj.jiangchat.database

import com.mcyzj.jiangchat.Main
import com.xbaimiao.easylib.module.database.OrmliteMysql

class MysqlDatabaseApi : AbstractDatabaseApi(OrmliteMysql(
    Main.instance.config.getConfigurationSection("Mysql")!!,
    Main.instance.config.getBoolean("Mysql.HikariCP")
)
)