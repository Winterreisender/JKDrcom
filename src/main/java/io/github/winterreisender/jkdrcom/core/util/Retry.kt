package io.github.winterreisender.jkdrcom.core.util

import java.util.logging.Logger

// 重试maxTimes次
object Retry {
    /**
     * 重试maxTimes次body,每次遇到异常后运行cleanup :剩余次数,异常->Unit
     *
     * @param maxTimes max retry times
     * @param cleanup actions on failed, (timesRemain :Int, exception :Throwable?)->Unit
     * @param body main body of retry
     * @return Result(body()) if success finally and Result(last exception) if all failed
     */
    fun<R> retry(maxTimes: Int, cleanup :(Int,Throwable?)->Unit ,body :()->R) :Result<R> {

        var timesRemain = maxTimes-1;
        var r = runCatching { body() }

        while (timesRemain != 0) {
            if(r.isSuccess) {
                break
            }else{
                Logger.getLogger("Retry").warning("Failed with Exception(${r.exceptionOrNull()}). Times remain $timesRemain")
                cleanup(timesRemain,r.exceptionOrNull())
                r = runCatching { body() }
                timesRemain--
            }
        }

        return r;
    }

}