package app.template.patches.tradingview

import app.morphe.patcher.Fingerprint

object Fingerprints {
    // BenefitsInteractorImpl.hasProPlanFeature(ProPlanFeature, ProPlan) : boolean
    // Central gate for all feature availability checks. Returns true if the user's
    // plan includes the specified feature. Covers: volume profile, custom timeframes,
    // bar replay, indicators on indicators, multiple watchlists, ad-free, etc.
    val HasProPlanFeatureFingerprint = Fingerprint(
        definingClass = "Lcom/tradingview/tradingviewapp/gopro/impl/gopro/interactor/BenefitsInteractorImpl;",
        name = "hasProPlanFeature"
    )

    // CurrentUser.isFree() : boolean
    // Returns true when user has no recognized ProPlan. Controls UI elements,
    // upgrade prompts, and free-tier treatment across the app.
    val CurrentUserIsFreeFingerprint = Fingerprint(
        definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/model/user/CurrentUser;",
        name = "isFree"
    )

    // CurrentUserKt.getBenefitsPlanLevel(CurrentUser) : BenefitsPlanLevel
    // Extension function that maps user's plan string to BenefitsPlanLevel enum.
    // Determines which benefits tier applies when looking up features.
    val GetBenefitsPlanLevelFingerprint = Fingerprint(
        definingClass = "Lcom/tradingview/tradingviewapp/feature/profile/model/user/CurrentUserKt;",
        name = "getBenefitsPlanLevel"
    )

    // UserPlanInfo.isFree() : boolean
    // Simple boolean getter used in GoPro/paywall decision flow.
    // When true, triggers native GoPro purchase or browser paywall.
    // Note: JADX displays this as "getIsFree" but the actual bytecode name is "isFree".
    val UserPlanInfoIsFreeFingerprint = Fingerprint(
        definingClass = "Lcom/tradingview/tradingviewapp/gopro/impl/core/model/UserPlanInfo;",
        name = "isFree"
    )

    // AdvertisementContainerView.onBind(AdvertisementEventsListener, AdsContainerSettings) : void
    // Loads Google Ad Manager banner or GoPro ad into the container.
    // Ad unit: /21803441042/symbol_page_300x250
    val AdContainerOnBindFingerprint = Fingerprint(
        definingClass = "Lcom/tradingview/tradingviewapp/feature/ads/api/view/AdvertisementContainerView;",
        name = "onBind"
    )
}
