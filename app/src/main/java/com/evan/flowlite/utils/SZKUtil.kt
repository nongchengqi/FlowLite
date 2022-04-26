package com.evan.flowlite.utils

import android.content.pm.PackageManager
import rikka.shizuku.Shizuku
import java.io.InputStreamReader

/**
 * shizuku工具
 */
class SZKUtil {
    companion object {

        fun checkPermission(): Boolean {
            if (Shizuku.isPreV11()) {
                return false
            }
            try {
                return when {
                    Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED -> {
                        true
                    }
                    Shizuku.shouldShowRequestPermissionRationale() -> {
                        false
                    }
                    else -> {
                        false
                    }
                }
            } catch (e: IllegalStateException) {
                e.printStackTrace()
                return false
            }
        }

        fun runCmd(cmdStr: String): Boolean {
            if (checkPermission()) {
                val cmd = arrayOf("sh", "-c", cmdStr)
                val process = Shizuku.newProcess(cmd, null, null)
                val inputStreamReaderError = InputStreamReader(process.errorStream)
                return inputStreamReaderError.read() == -1
            } else return false
        }
    }
}