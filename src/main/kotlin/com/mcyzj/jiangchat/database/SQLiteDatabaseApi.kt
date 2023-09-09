package com.mcyzj.jiangchat.database

import com.xbaimiao.easylib.module.database.OrmliteSQLite

class SQLiteDatabaseApi : AbstractDatabaseApi(OrmliteSQLite("database.db"))