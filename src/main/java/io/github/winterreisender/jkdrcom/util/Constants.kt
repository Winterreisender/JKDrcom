package io.github.winterreisender.jkdrcom.util

import java.io.File

/**
 * Created by lin on 2017-01-09-009.
 * 常量
 */
object Constants {
    const val LOGO_URL = "/dr-logo.png"
    const val LOADING_URL = "/loading.gif"
    const val AUTH_SERVER = "auth.jlu.edu.cn" //10.100.61.3
    const val COPYRIGHT_YEAR_START = "2017"
    const val PORT = 61440
    const val TIMEOUT = 10000 //10s
    const val ARTICLE_URL = "http://youthlin.com/?p=1391"
    const val PROJECT_HOME = "https://github.com/YouthLin/jlu-drcom-client/tree/master/jlu-drcom-java"
    const val NOTICE_URL = "http://login.jlu.edu.cn/notice.php"
    const val NOTICE_W = 592.0
    const val NOTICE_H = 458.0
    val DATA_HOME = System.getProperty("user.home", ".") + File.separator + ".drcom"
    val CONF_HOME = DATA_HOME + File.separator + "conf"
    const val LOCK_FILE_NAME = "drcom.java.lock"
    const val CONF_FILE_NAME = "drcom.java.conf"
    const val KEY_USERNAME = "username"
    const val KEY_PASSWORD = "password"
    const val KEY_DASH_MAC = "dash_mac"
    const val KEY_HOSTNAME = "hostname"
    const val KEY_REMEMBER = "remember"
    const val KEY_AUTO_LOGIN = "auto_login"
    const val KEY_VERSION = "version"
    const val VER_1 = 1 //添加 3DES 加密： conf, lock 文件
    const val MAX_RETRY = 8 //最大重试次数
}