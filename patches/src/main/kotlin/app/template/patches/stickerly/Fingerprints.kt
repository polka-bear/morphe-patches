package app.template.patches.stickerly

import app.morphe.patcher.Fingerprint

object Fingerprints {
    // droom.daro.a.Vh.n -> a() : boolean — primary subscription state check
    // Returns true when user has active subscription, false when free
    // Called by 20+ fragments/view models to gate premium features
    val SubscriptionStateCheckFingerprint = Fingerprint(
        definingClass = "Ldroom/daro/a/Vh/n;",
        name = "a"
    )

    // droom.daro.a.Zj.e -> a(long, AbstractC7326c) : Object — paywall popup trigger
    // Coroutine method that returns Boolean: true = show paywall, false = suppress
    // Bypasses n.a() by reading SubscriptionModel directly via n.b()
    val PaywallPopupFingerprint = Fingerprint(
        definingClass = "Ldroom/daro/a/Zj/e;",
        name = "a"
    )

    // droom.daro.a.ah.n -> a(Referrer) : boolean — reward ad display gatekeeper
    // Returns false when subscribed (no ads), true when ad should show
    // Checks subscription state then Firebase Remote Config for ad type
    val RewardAdCheckFingerprint = Fingerprint(
        definingClass = "Ldroom/daro/a/ah/n;",
        name = "a"
    )
}
