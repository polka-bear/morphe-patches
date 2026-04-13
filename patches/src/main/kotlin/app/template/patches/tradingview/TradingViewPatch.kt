package app.template.patches.tradingview

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.extensions.InstructionExtensions.removeInstructions
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
        // 1. Force hasProPlanFeature() to return true
        // This is the central feature availability gate. Returning true enables:
        // - Volume profile indicators
        // - Custom time intervals / Custom range bars
        // - Multiple enhanced watchlists
        // - Bar replay on intraday bars
        // - Indicators on indicators
        // - Ad-free mode
        // - Chart data export
        // - Intraday exotic charts
        // - Chart custom formulas
        // - Saved chart layouts
        // Note: Server-enforced numeric limits (alert count, connection count)
        // cannot be bypassed client-side.
        val benefitsClass = classDefBy(HasProPlanFeatureFingerprint.definingClass!!)
        val benefitsMatch = HasProPlanFeatureFingerprint.match(benefitsClass)
        benefitsMatch.method.apply {
            implementation?.let { impl ->
                removeInstructions(0, impl.instructions.count())
                addInstructions(0, "const/4 v0, 0x1\nreturn v0")
            }
        }

        // 2. Force CurrentUser.isFree() to return false
        // Prevents the app from treating the user as a free-tier user.
        // Suppresses GoPro badges, upgrade prompts, and free-tier UI.
        val currentUserClass = classDefBy(CurrentUserIsFreeFingerprint.definingClass!!)
        val isFreeMatch = CurrentUserIsFreeFingerprint.match(currentUserClass)
        isFreeMatch.method.apply {
            implementation?.let { impl ->
                removeInstructions(0, impl.instructions.count())
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
        }

        // 3. Force getBenefitsPlanLevel() to return PRO_ULTIMATE
        // Makes the app treat the user as Ultimate tier for benefit lookups.
        // BenefitsPlanLevel.PRO_ULTIMATE is enum ordinal 5.
        val planLevelClass = classDefBy(GetBenefitsPlanLevelFingerprint.definingClass!!)
        val planLevelMatch = GetBenefitsPlanLevelFingerprint.match(planLevelClass)
        planLevelMatch.method.apply {
            implementation?.let { impl ->
                removeInstructions(0, impl.instructions.count())
                addInstructions(
                    0,
                    """
                    sget-object v0, Lcom/tradingview/tradingviewapp/gopro/model/benefits/BenefitsPlanLevel;->PRO_ULTIMATE:Lcom/tradingview/tradingviewapp/gopro/model/benefits/BenefitsPlanLevel;
                    return-object v0
                    """.trimIndent()
                )
            }
        }

        // 4. Force UserPlanInfo.getIsFree() to return false
        // Suppresses GoPro/paywall flow triggers in the subscription interactor.
        val userPlanClass = classDefBy(UserPlanInfoIsFreeFingerprint.definingClass!!)
        val userPlanMatch = UserPlanInfoIsFreeFingerprint.match(userPlanClass)
        userPlanMatch.method.apply {
            implementation?.let { impl ->
                removeInstructions(0, impl.instructions.count())
                addInstructions(0, "const/4 v0, 0x0\nreturn v0")
            }
        }
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
        // This prevents Google Ad Manager banners and GoPro ads from loading.
        // Ad unit suppressed: /21803441042/symbol_page_300x250
        val adContainerClass = classDefBy(AdContainerOnBindFingerprint.definingClass!!)
        val adMatch = AdContainerOnBindFingerprint.match(adContainerClass)
        adMatch.method.apply {
            implementation?.let { impl ->
                removeInstructions(0, impl.instructions.count())
                addInstructions(0, "return-void")
            }
        }
    }
}
