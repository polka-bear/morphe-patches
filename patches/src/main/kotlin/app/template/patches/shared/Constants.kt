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

    val COMPATIBILITY_TRADINGVIEW = Compatibility(
        name = "TradingView",
        packageName = "com.tradingview.tradingviewapp",
        apkFileType = ApkFileType.APK,
        appIconColor = 0x2962FF,  // TradingView brand blue
        targets = listOf(
            // Uses non-obfuscated class names — may work across versions,
            // but test after each app update as method signatures can change
            AppTarget(version = "1.20.71.1")
        )
    )
    
    val COMPATIBILITY_STICKERLY = Compatibility(
        name = "Stickerly",
        packageName = "com.snowcorp.stickerly.android",
        apkFileType = ApkFileType.APK,
        appIconColor = 0xFF4081,  // TODO: verify exact brand color from app icon
        targets = listOf(
            AppTarget(version = "3.23.1")  // version-locked: uses obfuscated class names
        )
    )
    
    val COMPATIBILITY_EXCEL = Compatibility(
    name = "Microsoft Excel",
    packageName = "com.microsoft.office.excel",
    apkFileType = ApkFileType.APK,
    appIconColor = 0x217346,  // Excel green
    targets = listOf(
        AppTarget(version = null)  // Version-independent: all fingerprints use non-obfuscated class names
    )
)
}
