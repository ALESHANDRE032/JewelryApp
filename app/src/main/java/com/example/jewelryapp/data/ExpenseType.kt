package com.example.jewelryapp.data

object ExpenseType {
    const val PACKAGING         = "PACKAGING"
    const val RENT              = "RENT"
    const val DELIVERY_TO_STORE = "DELIVERY_TO_STORE"
    const val COURIER           = "COURIER"
    const val STORE_COMMISSION  = "STORE_COMMISSION"

    data class Option(val key: String, val label: String)

    val ALL = listOf(
        Option(PACKAGING,         "Упаковка"),
        Option(RENT,              "Аренда"),
        Option(DELIVERY_TO_STORE, "Доставка в магазин"),
        Option(COURIER,           "Курьер / доставка клиенту"),
        Option(STORE_COMMISSION,  "Комиссия магазина")
    )

    fun labelFor(key: String): String = ALL.find { it.key == key }?.label ?: key
}
