/*
 * Copyright (C) 2022 Winterreisender.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 */

package io.github.winterreisender.jkdrcom.gui

//用来存放常量。这种程度的项目其实用不着写个Constants,然后把所有的文本等等写在一个文件这种Over-Engineering设计.
// TODO: i18n方案: 1. Java内置的ResourceBundle(XML,Properties) 2. **JSON** 3. GNU GetText
object Constants {
    // 1. 应用信息
    const val AppHomepage = "https://github.com/Winterreisender/JKDrcom"
    const val AppFeedback = "https://github.com/Winterreisender/JKDrcom/discussions"
    const val AppName = "JKDrcom"
    const val AppVersion = "v1.1.0-dev"
    const val AppCodeName = "Endless August"
    const val AppDescription = "使用Kotlin的Drcom开源实现."
    const val AppCopyright = """
        JKDrcom 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU Affero 通用公共许可证修改之，仅版本 3 许可证。
        发布 JKDrcom 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU Affero 通用公共许可证，了解详情。
        你应该随程序获得一份 GNU Affero 通用公共许可证的复本。如果没有，请看 <https://www.gnu.org/licenses/>。
    """
    const val AppCredits = "JKDrcom离不开jlu-drcom-java以及其他开源软件."

    // 解决空格的(最佳?)实践: 用const val定义,用AppAbout.trimIndent()调用
    const val AppAbout = """
        $AppName $AppVersion "$AppCodeName"
        $AppDescription
        $AppCopyright
        $AppCredits
        详情见主页: $AppHomepage
    """

    // 2. GUI相关
    object UIText{
        const val Logout       = "注销"
        const val LoggingOut   = "注销中"
        const val Login        = "登录"
        const val Username     = "用户名"
        const val Password     = "密码"
        const val MacAddress   = "MAC地址"
        const val HostName     = "计算机名称"
        const val DetectMac    = "加载Mac"
        const val AutoLogin    = "自动登录"
        const val SavePassword = "记住密码"

        const val Connected    = "已连接"
        const val Disconnected = "已断开"
        val Retrying = {timesRemain :Int -> "重试中(剩余 $timesRemain 次)"} // 更灵活,便于处理语序不同问题,避免"微软式中文" val Retrying = {timesRemain :Int -> "Retrying... Remains Times: $timesRemain"}
    }
    val DefaultPrimaryColor = Utils.WebColor(0x39,0xc5,0xbb)


    object MenuText {
        const val Function = "功能"
        const val Function_SchoolNetWindow        = "校园网之窗"
        const val Function_SchoolNetInfo          = "上网信息窗"
        const val Function_JLUTestLogin           = "登录JLU.TEST"
        const val Function_SetMaxRetry            = "重试次数"
        const val Function_SetMaxRetry_NeedNum    = "请输入1~128的数字"
        const val Function_HideWindow             = "隐藏到托盘"
        const val Function_ResetConfig            = "恢复默认配置"
        const val Function_SetThemeColor          = "设置主题色"
        const val Function_ResetConfig_Done       = "已恢复默认配置" //val Function_ResetConfig_Done = {cfg :String -> "已恢复默认配置:\n $cfg" }
        const val Function_SaveConfig             = "保存配置"
        const val Function_SaveConfig_Done        = "保存成功"
        const val Function_SaveConfig_Failed      = "保存失败!"
        const val Function_EditConfig             = "打开配置文件"
        const val Function_NetWindowType          = "校园网之窗打开方式"
        const val Function_CloseAfterSecs         = "自动隐藏时间"
        const val Function_CloseAfterSecs_Text    = "设置自动隐藏时间(秒). -1表示不设置"
        const val Function_CloseAfterSecs_NeedNum = "请输入1~128的数字"

        const val Settings = "设置"

        const val Help = "帮助"
        const val Help_About = "关于"
        const val Help_HomePage = "主页"
        const val Help_Feedback = "讨论与反馈"

        const val Tray_Show = "显示 (Show)"
        const val Tray_Hide = "隐藏 (Hide)"
        const val Tray_Exit = "退出 (Exit)"

    }

    // 3.其他
    val PwdCryptoKey = byteArrayOf(0x3b, 0x79, 0x40, 0x00, 0x7c, 0x1d, 0x6C, 0x78,0x1E, 0x37,0x33, 0x58, 0x05, 0x3e, 0x11, 0x02) //淦 Java 怎么会有 byte 必须是有符号数这种zz规定啊
    const val SchoolNetWindowURL = "http://login.jlu.edu.cn/notice.php" // 校园网之窗URL
    const val SchoolNetInfoURL   = "http://10.100.61.3/"                // 校园网信息流量URL
    //JLU.TEST登录URL,第一个%s为用户名,第二个为密码
    const val JluTestLoginURL    = "https://net.jlu.edu.cn/login?DDDDD=%s&upass=%s&R1=0&R2=&R3=0&R6=1&para=00&0MKKey=123456&buttonClicked=&redirect_url=&err_flag=&username=&password=&user=&cmd=&Login=&v6ip="

}