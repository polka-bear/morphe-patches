package app.template.patches.stickerly

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.stickerly.Fingerprints.SubscriptionStateCheckFingerprint
import app.template.patches.stickerly.Fingerprints.PaywallPopupFingerprint
import app.template.patches.stickerly.Fingerprints.RewardAdCheckFingerprint
import app.template.patches.shared.Constants.COMPATIBILITY_STICKERLY

@Suppress("unused")
val stickerlyUnlockPremiumPatch = bytecodePatch(
    name = "Unlock Premium",
    description = "Unlocks all premium features by forcing subscription state to active.",
    default = true
) {
    compatibleWith(COMPATIBILITY_STICKERLY)

    execute {
        // 1. Force SubscriptionStateManager.a() to return true
        // This is the central gatekeeper checked by 20+ fragments/view models.
        // When true, all premium features are unlocked and ads are suppressed.
        // Inject at index 0 — original code is preserved but unreachable.
        val stateManagerClass = classDefBy(SubscriptionStateCheckFingerprint.definingClass!!)
        val stateMatch = SubscriptionStateCheckFingerprint.match(stateManagerClass)
        stateMatch.method.addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        // 2. Force PaywallPopupManager.a() to return Boolean.FALSE (don't show paywall)
        // This is a coroutine method with try-catch — must NOT remove instructions
        // or the exception handler table becomes invalid. Injecting at index 0 is safe.
        val paywallClass = classDefBy(PaywallPopupFingerprint.definingClass!!)
        val paywallMatch = PaywallPopupFingerprint.match(paywallClass)
        paywallMatch.method.addInstructions(
            0,
            "sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;\nreturn-object v0"
        )
    }
}

@Suppress("unused")
val stickerlyRemoveAdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Disables reward ad display by forcing the ad gatekeeper to always decline.",
    default = true
) {
    compatibleWith(COMPATIBILITY_STICKERLY)

    execute {
        // Force RewardAdCheck.a(Referrer) to return false
        // Inject at index 0 — original code is preserved but unreachable.
        val adCheckClass = classDefBy(RewardAdCheckFingerprint.definingClass!!)
        val adMatch = RewardAdCheckFingerprint.match(adCheckClass)
        adMatch.method.addInstructions(0, "const/4 v0, 0x0\nreturn v0")
    }
}
