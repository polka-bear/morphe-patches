package app.template.patches.stickerly

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
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
        val stateManagerClass = classDefBy(SubscriptionStateCheckFingerprint.definingClass!!)
        val stateMatch = SubscriptionStateCheckFingerprint.match(stateManagerClass)
        stateMatch.method.apply {
            implementation?.let { impl ->
                removeInstructions(0, impl.instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // 2. Force PaywallPopupManager.a() to return Boolean.FALSE (don't show paywall)
        // This coroutine method bypasses n.a() by reading SubscriptionModel.a directly
        // via n.b(). Returning FALSE prevents the paywall dialog from ever appearing.
        val paywallClass = classDefBy(PaywallPopupFingerprint.definingClass!!)
        val paywallMatch = PaywallPopupFingerprint.match(paywallClass)
        paywallMatch.method.apply {
            implementation?.let { impl ->
                removeInstructions(0, impl.instructions.count())
                addInstructions(
                    0,
                    "sget-object v0, Ljava/lang/Boolean;->FALSE:Ljava/lang/Boolean;\nreturn-object v0"
                )
            }
        }
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
        // This method checks subscription state and Firebase Remote Config to decide
        // whether to show reward ads. Returning false = never show reward ads.
        // The premium patch already suppresses ads via subscription state, but this
        // provides a direct safety net at the ad display layer.
        val adCheckClass = classDefBy(RewardAdCheckFingerprint.definingClass!!)
        val adMatch = RewardAdCheckFingerprint.match(adCheckClass)
        adMatch.method.apply {
            implementation?.let { impl ->
                removeInstructions(0, impl.instructions.count())
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
        }
    }
}
