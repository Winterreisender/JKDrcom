/*
 * Copyright (C) 2022  Winterreisender
 *
 * This file is part of JKDrcom.
 *
 * JKDrcom is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, only version 3 of the License.
 *
 * JKDrcom is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * 本文件是 JKDrcom 的一部分。
 * JKDrcom 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU Affero 通用公共许可证修改之，仅版本 3 许可证。
 * 发布 JKDrcom 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU Affero 通用公共许可证，了解详情。
 * 你应该随程序获得一份 GNU Affero 通用公共许可证的复本。如果没有，请看 <https://www.gnu.org/licenses/>。
 */

package io.github.winterreisender.jkdrcom.gui

//用来存放常量。这种程度的项目其实用不着写个Constants,然后把所有的文本等等写在一个文件这种Over-Engineering设计.
object Constants {
    const val AppHomepage = "https://github.com/Winterreisender/JKDrcom"
    const val AppFeedback = "https://github.com/Winterreisender/JKDrcom/discussions"
    const val AppName = "JKDrcom"
    const val AppVersion = "v0.4.0-dev"
    const val AppDescription = "使用Kotlin的Drcom开源实现."
    const val AppCopyright = """
        JKDrcom 是自由软件：你可以再分发之和/或依照由自由软件基金会发布的 GNU Affero 通用公共许可证修改之，仅版本 3 许可证。
        发布 JKDrcom 是希望它能有用，但是并无保障;甚至连可销售和符合某个特定的目的都不保证。请参看 GNU Affero 通用公共许可证，了解详情。
        你应该随程序获得一份 GNU Affero 通用公共许可证的复本。如果没有，请看 <https://www.gnu.org/licenses/>。
    """
    const val AppCredits = "JKDrcom离不开DrcomJava以及其他开源软件."

    // 解决空格的最佳实践: 用const val定义,用AppAbout.trimIndent()调用
    const val AppAbout = """
        $AppName $AppVersion
        $AppDescription
        $AppCopyright
        $AppCredits
        详情见主页: $AppHomepage
    """

    object UIText{
        const val Logout = "注销"
        const val Login = "登录"
        const val Username = "用户名"
        const val Password = "密码"
        const val MacAddress = "MAC地址"
        const val HostName = "计算机名称"
        const val DetectMac = "加载Mac"
        const val AutoLogin = "自动登录"
        const val SavePassword = "记住密码"

        const val Connected = "已连接"
        const val Disconnected = "已断开"
        val Retrying = {timesRemain :Int -> "重试中(剩余 $timesRemain 次)"} // 更灵活,便于处理语序不同问题,避免"微软式中文" val Retrying = {timesRemain :Int -> "Retrying... Remains Times: $timesRemain"}
    }

    object MenuText {
        const val Function = "功能"
        const val Function_SchoolNetWindow = "校园网之窗"
        const val Function_SetMaxRetry = "设置重试次数"
        const val Function_SetMaxRetry_NeedNum = "请输入1~128的数字"
        const val Function_HideWindow = "隐藏到托盘"
        const val Function_ResetConfig = "恢复默认配置"
        val Function_ResetConfig_Done = {cfg :String -> "已恢复默认配置:\n $cfg" }
        const val Function_SaveConfig = "保存配置"
        const val Function_SaveConfig_Done = "保存成功"
        const val Function_SaveConfig_Failed = "保存失败!"

        const val Help = "帮助"
        const val Help_About = "关于"
        const val Help_HomePage = "主页"
        const val Help_Feedback = "讨论与反馈"

        const val Tray_Show = "显示 (Show)"
        const val Tray_Hide = "隐藏 (Hide)"
        const val Tray_Exit = "退出 (Exit)"

    }

}