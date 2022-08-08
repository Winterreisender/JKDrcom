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
import javax.swing.JOptionPane
import kotlin.test.Test

internal class Test {
    @Test fun netWindow() {
        Utils.showNetWindow(closeAfterSecs = 5)
    }

    @Test fun hostInfo() {
        IPUtil.getHostInfo().forEach(::println)
    }

    @Test fun choiceBox() {
        JOptionPane.showInputDialog(null,"Test",null,JOptionPane.INFORMATION_MESSAGE,null, arrayOf(5,6,7),5).let { println(it) }
    }
}