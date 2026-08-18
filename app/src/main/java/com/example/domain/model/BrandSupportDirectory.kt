package com.example.domain.model

data class BrandSupportInfo(
    val brandName: String,
    val tollFreeNumber: String? = null,
    val supportEmail: String? = null,
    val claimWebsiteUrl: String? = null,
    val standardWarrantyCoverageNote: String
)

object BrandSupportDirectory {

    private val DIRECTORY = listOf(
        BrandSupportInfo(
            brandName = "Apple",
            tollFreeNumber = "000800 1009009",
            supportEmail = "contactus.in@apple.com",
            claimWebsiteUrl = "https://checkcoverage.apple.com",
            standardWarrantyCoverageNote = "1-Year Limited Warranty + 90 days complimentary phone support. Covers manufacturing defects and battery degradation under 80%."
        ),
        BrandSupportInfo(
            brandName = "Sony",
            tollFreeNumber = "1800 103 7799",
            supportEmail = "sonyindia.care@sony.com",
            claimWebsiteUrl = "https://www.sony.co.in/electronics/support",
            standardWarrantyCoverageNote = "1 to 2 Years Comprehensive Warranty depending on model. In-home service available for TVs 40\" and above."
        ),
        BrandSupportInfo(
            brandName = "Samsung",
            tollFreeNumber = "1800 572 67864",
            supportEmail = "support.india@samsung.com",
            claimWebsiteUrl = "https://www.samsung.com/in/support/your-service/warranty-check",
            standardWarrantyCoverageNote = "1-Year standard warranty on devices, up to 10 or 20 years on Digital Inverter compressors/motors."
        ),
        BrandSupportInfo(
            brandName = "Dyson",
            tollFreeNumber = "1800 258 6688",
            supportEmail = "ask@dyson.in",
            claimWebsiteUrl = "https://www.dyson.in/support/contact-us",
            standardWarrantyCoverageNote = "2-Year official Dyson warranty covering all parts and labor with free doorstep pickup & return."
        ),
        BrandSupportInfo(
            brandName = "Bosch",
            tollFreeNumber = "1800 266 1880",
            supportEmail = "service.in@bosch-home.com",
            claimWebsiteUrl = "https://www.bosch-home.in/service/warranty",
            standardWarrantyCoverageNote = "2 to 3 Years Comprehensive Warranty + 10-Year EcoSilence motor guarantee."
        ),
        BrandSupportInfo(
            brandName = "LG",
            tollFreeNumber = "1800 315 9999",
            supportEmail = "serviceindia@lge.com",
            claimWebsiteUrl = "https://www.lg.com/in/support/warranty",
            standardWarrantyCoverageNote = "1-Year product warranty + 10-Year Smart Inverter compressor warranty."
        ),
        BrandSupportInfo(
            brandName = "Decathlon",
            tollFreeNumber = "1800 833 3330",
            supportEmail = "care.india@decathlon.com",
            claimWebsiteUrl = "https://www.decathlon.in/warranty",
            standardWarrantyCoverageNote = "Lifetime warranty on B'Twin/Triban frames, rigid forks, and handlebars; 2-Year warranty on all other equipment."
        ),
        BrandSupportInfo(
            brandName = "Croma",
            tollFreeNumber = "1800 572 7662",
            supportEmail = "customersupport@croma.com",
            claimWebsiteUrl = "https://www.croma.com/croma-service-support",
            standardWarrantyCoverageNote = "Manufacturer warranty honored at all 400+ Croma stores. Extended Croma Shield claims processed via helpline."
        ),
        BrandSupportInfo(
            brandName = "Vijay Sales",
            tollFreeNumber = "1800 890 6000",
            supportEmail = "support@vijaysales.com",
            claimWebsiteUrl = "https://www.vijaysales.com/customer-support",
            standardWarrantyCoverageNote = "Authorized warranty claim facilitation with brand authorized service centers across India."
        ),
        BrandSupportInfo(
            brandName = "Dell",
            tollFreeNumber = "1800 425 0088",
            supportEmail = "dell_care_india@dell.com",
            claimWebsiteUrl = "https://www.dell.com/support/home/en-in?app=warranty",
            standardWarrantyCoverageNote = "1 to 3 Years Onsite Next Business Day hardware service with remote diagnosis."
        ),
        BrandSupportInfo(
            brandName = "HP",
            tollFreeNumber = "1800 258 7170",
            supportEmail = "hpcare@hp.com",
            claimWebsiteUrl = "https://support.hp.com/in-en/check-warranty",
            standardWarrantyCoverageNote = "1-Year standard parts and labor with authorized HP service center network."
        ),
        BrandSupportInfo(
            brandName = "OnePlus",
            tollFreeNumber = "1800 102 8411",
            supportEmail = "onepluscare@oneplus.com",
            claimWebsiteUrl = "https://service.oneplus.com/in/warranty-check",
            standardWarrantyCoverageNote = "1-Year device warranty + 6-month battery/charging accessory warranty."
        ),
        BrandSupportInfo(
            brandName = "Philips",
            tollFreeNumber = "1800 102 2929",
            supportEmail = "customercare.india@philips.com",
            claimWebsiteUrl = "https://www.philips.co.in/c-w/support-home/warranty.html",
            standardWarrantyCoverageNote = "2-Year Worldwide Guarantee for consumer personal care and domestic appliances."
        ),
        BrandSupportInfo(
            brandName = "IKEA",
            tollFreeNumber = "1800 419 4532",
            supportEmail = "customercare.india@ikea.com",
            claimWebsiteUrl = "https://www.ikea.com/in/en/customer-service/guarantees/",
            standardWarrantyCoverageNote = "10 to 25-Year Guarantee on select office chairs, kitchen systems, and mattresses."
        )
    )

    fun findSupportFor(merchant: String, item: String): BrandSupportInfo? {
        val query = "$merchant $item".lowercase()
        return DIRECTORY.firstOrNull { info ->
            query.contains(info.brandName.lowercase())
        }
    }
}
