package io.github.winterreisender.jkdrcom.util

import java.util.logging.Logger

// 重试maxTimes次
object Retry {
    fun<R> retry(maxTimes: Int, cleanup :()->Unit ,body :()->R) :Result<R> {

        var r = runCatching(body)
        var timesRemain = maxTimes-1;

        while (timesRemain != 0) {
            if(r.isSuccess) {
                break
            }else{
                Logger.getLogger("Retry").warning("Failed with Exception(${r.exceptionOrNull()}). Times remain $timesRemain")
                cleanup()
                r = runCatching(body)
                timesRemain--
            }
        }

        return r;
    }

}