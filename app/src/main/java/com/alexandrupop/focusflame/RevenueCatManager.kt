package com.alexandrupop.focusflame

import android.app.Activity
import android.app.Application
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Package as RcPackage
import com.revenuecat.purchases.PurchaseParams
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesConfiguration
import com.revenuecat.purchases.getCustomerInfoWith
import com.revenuecat.purchases.getOfferingsWith
import com.revenuecat.purchases.purchaseWith
import com.revenuecat.purchases.restorePurchasesWith
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update

class RevenueCatManager(private val app: Application) {
    private val _state = MutableStateFlow(BillingState())
    val state: StateFlow<BillingState> = _state
    private var packageToBuy: RcPackage? = null

    fun configure() {
        val key = BuildConfig.REVENUECAT_PUBLIC_SDK_KEY.trim()
        if (key.isBlank()) {
            _state.value = BillingState(status = "Add REVENUECAT_PUBLIC_SDK_KEY to local.properties for Pro testing")
            return
        }

        runCatching {
            Purchases.logLevel = LogLevel.DEBUG
            Purchases.configure(PurchasesConfiguration.Builder(app, key).build())
        }.onSuccess {
            _state.update { it.copy(configured = true, loading = true, status = "Checking Pro access") }
            refreshCustomerInfo()
            refreshOfferings()
        }.onFailure { error ->
            _state.value = BillingState(status = "RevenueCat unavailable: ${error.message.orEmpty()}")
        }
    }

    fun refreshCustomerInfo() {
        if (!_state.value.configured) return
        Purchases.sharedInstance.getCustomerInfoWith(
            onError = { error ->
                _state.update {
                    it.copy(loading = false, status = "RevenueCat unavailable: ${error.message}")
                }
            },
            onSuccess = { customerInfo ->
                val pro = customerInfo.entitlements.active[PRO_ENTITLEMENT]?.isActive == true
                _state.update {
                    it.copy(
                        pro = pro,
                        loading = false,
                        status = if (pro) "Pro unlocked" else "Free mode"
                    )
                }
            }
        )
    }

    fun refreshOfferings() {
        if (!_state.value.configured) return
        Purchases.sharedInstance.getOfferingsWith(
            onError = { error ->
                _state.update {
                    it.copy(packageReady = false, status = "No Test Store package: ${error.message}")
                }
            },
            onSuccess = { offerings ->
                packageToBuy = offerings.current?.availablePackages?.firstOrNull()
                _state.update {
                    it.copy(
                        packageReady = packageToBuy != null,
                        status = when {
                            it.pro -> "Pro unlocked"
                            packageToBuy != null -> "Test Store package ready"
                            else -> "No packages in current offering"
                        }
                    )
                }
            }
        )
    }

    fun purchase(activity: Activity) {
        val packageForPurchase = packageToBuy
        if (!_state.value.configured || packageForPurchase == null) {
            _state.update { it.copy(status = "Connect a RevenueCat Test Store offering first") }
            return
        }

        _state.update { it.copy(loading = true, status = "Opening purchase sheet") }
        val params = PurchaseParams.Builder(activity, packageForPurchase).build()
        Purchases.sharedInstance.purchaseWith(
            purchaseParams = params,
            onError = { error, userCancelled ->
                _state.update {
                    it.copy(
                        loading = false,
                        status = if (userCancelled) "Purchase cancelled" else "Purchase failed: ${error.message}"
                    )
                }
            },
            onSuccess = { _, customerInfo ->
                val pro = customerInfo.entitlements.active[PRO_ENTITLEMENT]?.isActive == true
                _state.update {
                    it.copy(
                        pro = pro,
                        loading = false,
                        status = if (pro) "Pro unlocked" else "Purchase complete, entitlement not active yet"
                    )
                }
            }
        )
    }

    fun restore() {
        if (!_state.value.configured) {
            _state.update { it.copy(status = "RevenueCat key not configured") }
            return
        }

        _state.update { it.copy(loading = true, status = "Restoring purchases") }
        Purchases.sharedInstance.restorePurchasesWith(
            onError = { error ->
                _state.update { it.copy(loading = false, status = "Restore failed: ${error.message}") }
            },
            onSuccess = { customerInfo ->
                val pro = customerInfo.entitlements.active[PRO_ENTITLEMENT]?.isActive == true
                _state.update {
                    it.copy(
                        pro = pro,
                        loading = false,
                        status = if (pro) "Pro restored" else "No Pro purchase found"
                    )
                }
            }
        )
    }
}
