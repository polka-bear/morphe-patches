package app.template.patches.tradingview

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.patch.bytecodePatch
import app.template.patches.tradingview.Fingerprints.HasProPlanFeatureFingerprint
import app.template.patches.tradingview.Fingerprints.CurrentUserIsFreeFingerprint
import app.template.patches.tradingview.Fingerprints.GetBenefitsPlanLevelFingerprint
import app.template.patches.tradingview.Fingerprints.UserPlanInfoIsFreeFingerprint
import app.template.patches.tradingview.Fingerprints.AdContainerOnBindFingerprint
import app.template.patches.shared.Constants.COMPATIBILITY_TRADINGVIEW

@Suppress("unused")
val tradingViewUnlockProPatch = bytecodePatch(
    name = "Unlock Pro Features",
    description = "Enables all Pro plan features by bypassing client-side feature gates.",
    default = true
) {
    compatibleWith(COMPATIBILITY_TRADINGVIEW)

    execute {
        // All patches inject at index 0 — original code is preserved but unreachable.
        // This avoids corrupting exception handler tables in methods with try-catch.

        // 1. Force hasProPlanFeature() to return true
        // Central feature gate: volume profile, custom timeframes, bar replay, etc.
        val benefitsClass = classDefBy(HasProPlanFeatureFingerprint.definingClass!!)
        HasProPlanFeatureFingerprint.match(benefitsClass).method
            .addInstructions(0, "const/4 v0, 0x1\nreturn v0")

        // 2. Force CurrentUser.isFree() to return false
        // Suppresses GoPro badges, upgrade prompts, and free-tier UI.
        val currentUserClass = classDefBy(CurrentUserIsFreeFingerprint.definingClass!!)
        CurrentUserIsFreeFingerprint.match(currentUserClass).method
            .addInstructions(0, "const/4 v0, 0x0\nreturn v0")

        // 3. Force getBenefitsPlanLevel() to return PRO_ULTIMATE
        // Makes the app treat the user as Ultimate tier for benefit lookups.
        val planLevelClass = classDefBy(GetBenefitsPlanLevelFingerprint.definingClass!!)
        GetBenefitsPlanLevelFingerprint.match(planLevelClass).method
            .addInstructions(
                0,
                "sget-object v0, Lcom/tradingview/tradingviewapp/gopro/model/benefits/BenefitsPlanLevel;->PRO_ULTIMATE:Lcom/tradingview/tradingviewapp/gopro/model/benefits/BenefitsPlanLevel;\nreturn-object v0"
            )

        // 4. Force UserPlanInfo.isFree() to return false
        // Suppresses GoPro/paywall flow triggers.
        // Note: bytecode name is "isFree", not "getIsFree" (JADX renames it for display).
        val userPlanClass = classDefBy(UserPlanInfoIsFreeFingerprint.definingClass!!)
        UserPlanInfoIsFreeFingerprint.match(userPlanClass).method
            .addInstructions(0, "const/4 v0, 0x0\nreturn v0")
    }
}

@Suppress("unused")
val tradingViewRemoveAdsPatch = bytecodePatch(
    name = "Remove Ads",
    description = "Disables banner ad loading on symbol pages.",
    default = true
) {
    compatibleWith(COMPATIBILITY_TRADINGVIEW)

    execute {
        // Force AdvertisementContainerView.onBind() to do nothing.
        // Inject at index 0 — original code preserved but unreachable.
        val adContainerClass = classDefBy(AdContainerOnBindFingerprint.definingClass!!)
        AdContainerOnBindFingerprint.match(adContainerClass).method
            .addInstructions(0, "return-void")
    }
}
