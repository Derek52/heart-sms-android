package xyz.heart.sms.shared.service

import xyz.klinker.sms.shared.util.billing.ProductPurchased

class SimpleLifetimeSubscriptionCheckService : SimpleSubscriptionCheckService() {
    override fun handleBestProduct(best: ProductPurchased) {
        if (best.productId == "lifetime") {
            writeLifetimeSubscriber()
        }
    }
}
