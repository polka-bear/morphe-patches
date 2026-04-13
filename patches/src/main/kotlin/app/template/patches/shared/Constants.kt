package app.template.patches.shared

import app.morphe.patcher.patch.ApkFileType
import app.morphe.patcher.patch.AppTarget
import app.morphe.patcher.patch.Compatibility

object Constants {
    val COMPATIBILITY_CALL_BLACKLIST = Compatibility(
        name = "Call Blacklist",
        packageName = "com.vladlee.easyblacklist",
        apkFileType = ApkFileType.APK, 
        appIconColor = 0xe53935,
        targets = listOf(
            AppTarget(version = "4.8.20")
        )
    )
}
