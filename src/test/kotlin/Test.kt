/*
 * Copyright (C) 2022 Winterreisender.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>
 */

import io.github.winterreisender.jkdrcom.core.util.IPUtil
import io.github.winterreisender.jkdrcom.gui.Utils
import kotlin.test.Test


internal class Test {

    @Test fun netWindowSwing() {
        Utils.showNetWindow("http://login.jlu.edu.cn/notice.php")

        Thread.sleep(50000L)
    }


    @Test fun hostInfo() {
        IPUtil.getHostInfo().forEach(::println)
    }

}