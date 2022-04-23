package me.winterreisender.jkdrcom.util

import java.util.logging.Logger

// 重试maxTimes次
object Retry {
    fun<R> retry(maxTimes: Int, body :()->R) :Result<R> {

        var r = runCatching(body)
        var timesRemain = maxTimes-1;

        while (timesRemain != 0) {
            if(r.isSuccess) {
                break
            }else{
                Logger.getLogger("Retry").fine("Failed with Exception(${r.exceptionOrNull()}). Times remain $timesRemain")
                r = runCatching(body)
                timesRemain--
            }
        }

        return r;
    }

}