package com.example.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.R

enum class GlossarySection(
    @get:StringRes val titleRes: Int,
    @get:StringRes val descriptionRes: Int
) {
    VIOLATIONS(
        titleRes = R.string.glossary_section_violations,
        descriptionRes = R.string.glossary_desc_violations
    ),
    LEGITIMATE(
        titleRes = R.string.glossary_section_legitimate,
        descriptionRes = R.string.glossary_desc_legitimate
    ),
    TRAPS(
        titleRes = R.string.glossary_section_traps,
        descriptionRes = R.string.glossary_desc_traps
    ),
    BONUS(
        titleRes = R.string.glossary_section_bonus,
        descriptionRes = R.string.glossary_desc_bonus
    )
}

data class GlossaryEntry(
    val category: ComplianceCategory,
    val section: GlossarySection,
    @get:StringRes val nameRes: Int,
    @get:StringRes val explanationRes: Int,
    @get:DrawableRes val iconRes: Int,
    val badgeColor: Long,
    val borderColor: Long
) {
    companion object {
        val ALL_ENTRIES: List<GlossaryEntry> = listOf(
            // Violations
            GlossaryEntry(
                category = ComplianceCategory.BRIBERY,
                section = GlossarySection.VIOLATIONS,
                nameRes = ComplianceCategory.BRIBERY.displayNameRes,
                explanationRes = ComplianceCategory.BRIBERY.explanationRes,
                iconRes = ComplianceCategory.BRIBERY.iconRes,
                badgeColor = ComplianceCategory.BRIBERY.badgeColor,
                borderColor = ComplianceCategory.BRIBERY.borderColor
            ),
            GlossaryEntry(
                category = ComplianceCategory.FRAUD,
                section = GlossarySection.VIOLATIONS,
                nameRes = ComplianceCategory.FRAUD.displayNameRes,
                explanationRes = ComplianceCategory.FRAUD.explanationRes,
                iconRes = ComplianceCategory.FRAUD.iconRes,
                badgeColor = ComplianceCategory.FRAUD.badgeColor,
                borderColor = ComplianceCategory.FRAUD.borderColor
            ),
            GlossaryEntry(
                category = ComplianceCategory.MONEY_LAUNDERING,
                section = GlossarySection.VIOLATIONS,
                nameRes = ComplianceCategory.MONEY_LAUNDERING.displayNameRes,
                explanationRes = ComplianceCategory.MONEY_LAUNDERING.explanationRes,
                iconRes = ComplianceCategory.MONEY_LAUNDERING.iconRes,
                badgeColor = ComplianceCategory.MONEY_LAUNDERING.badgeColor,
                borderColor = ComplianceCategory.MONEY_LAUNDERING.borderColor
            ),
            GlossaryEntry(
                category = ComplianceCategory.DATA_BREACH,
                section = GlossarySection.VIOLATIONS,
                nameRes = ComplianceCategory.DATA_BREACH.displayNameRes,
                explanationRes = ComplianceCategory.DATA_BREACH.explanationRes,
                iconRes = ComplianceCategory.DATA_BREACH.iconRes,
                badgeColor = ComplianceCategory.DATA_BREACH.badgeColor,
                borderColor = ComplianceCategory.DATA_BREACH.borderColor
            ),
            GlossaryEntry(
                category = ComplianceCategory.SYSTEMIC_CORRUPTION,
                section = GlossarySection.VIOLATIONS,
                nameRes = ComplianceCategory.SYSTEMIC_CORRUPTION.displayNameRes,
                explanationRes = ComplianceCategory.SYSTEMIC_CORRUPTION.explanationRes,
                iconRes = ComplianceCategory.SYSTEMIC_CORRUPTION.iconRes,
                badgeColor = ComplianceCategory.SYSTEMIC_CORRUPTION.badgeColor,
                borderColor = ComplianceCategory.SYSTEMIC_CORRUPTION.borderColor
            ),

            // Legitimate Procedures
            GlossaryEntry(
                category = ComplianceCategory.OFFICIAL_DOCUMENT,
                section = GlossarySection.LEGITIMATE,
                nameRes = ComplianceCategory.OFFICIAL_DOCUMENT.displayNameRes,
                explanationRes = ComplianceCategory.OFFICIAL_DOCUMENT.explanationRes,
                iconRes = ComplianceCategory.OFFICIAL_DOCUMENT.iconRes,
                badgeColor = ComplianceCategory.OFFICIAL_DOCUMENT.badgeColor,
                borderColor = ComplianceCategory.OFFICIAL_DOCUMENT.borderColor
            ),
            GlossaryEntry(
                category = ComplianceCategory.VERIFIED_APPROVAL,
                section = GlossarySection.LEGITIMATE,
                nameRes = ComplianceCategory.VERIFIED_APPROVAL.displayNameRes,
                explanationRes = ComplianceCategory.VERIFIED_APPROVAL.explanationRes,
                iconRes = ComplianceCategory.VERIFIED_APPROVAL.iconRes,
                badgeColor = ComplianceCategory.VERIFIED_APPROVAL.badgeColor,
                borderColor = ComplianceCategory.VERIFIED_APPROVAL.borderColor
            ),
            GlossaryEntry(
                category = ComplianceCategory.VALID_PARTNERSHIP,
                section = GlossarySection.LEGITIMATE,
                nameRes = ComplianceCategory.VALID_PARTNERSHIP.displayNameRes,
                explanationRes = ComplianceCategory.VALID_PARTNERSHIP.explanationRes,
                iconRes = ComplianceCategory.VALID_PARTNERSHIP.iconRes,
                badgeColor = ComplianceCategory.VALID_PARTNERSHIP.badgeColor,
                borderColor = ComplianceCategory.VALID_PARTNERSHIP.borderColor
            ),
            GlossaryEntry(
                category = ComplianceCategory.CERTIFICATION,
                section = GlossarySection.LEGITIMATE,
                nameRes = ComplianceCategory.CERTIFICATION.displayNameRes,
                explanationRes = ComplianceCategory.CERTIFICATION.explanationRes,
                iconRes = ComplianceCategory.CERTIFICATION.iconRes,
                badgeColor = ComplianceCategory.CERTIFICATION.badgeColor,
                borderColor = ComplianceCategory.CERTIFICATION.borderColor
            ),
            GlossaryEntry(
                category = ComplianceCategory.VERIFIED_INVOICE,
                section = GlossarySection.LEGITIMATE,
                nameRes = ComplianceCategory.VERIFIED_INVOICE.displayNameRes,
                explanationRes = ComplianceCategory.VERIFIED_INVOICE.explanationRes,
                iconRes = ComplianceCategory.VERIFIED_INVOICE.iconRes,
                badgeColor = ComplianceCategory.VERIFIED_INVOICE.badgeColor,
                borderColor = ComplianceCategory.VERIFIED_INVOICE.borderColor
            ),

            // Traps
            GlossaryEntry(
                category = ComplianceCategory.FALSE_ALARM,
                section = GlossarySection.TRAPS,
                nameRes = ComplianceCategory.FALSE_ALARM.displayNameRes,
                explanationRes = ComplianceCategory.FALSE_ALARM.explanationRes,
                iconRes = ComplianceCategory.FALSE_ALARM.iconRes,
                badgeColor = ComplianceCategory.FALSE_ALARM.badgeColor,
                borderColor = ComplianceCategory.FALSE_ALARM.borderColor
            ),
            GlossaryEntry(
                category = ComplianceCategory.UNVERIFIED_RUMOR,
                section = GlossarySection.TRAPS,
                nameRes = ComplianceCategory.UNVERIFIED_RUMOR.displayNameRes,
                explanationRes = ComplianceCategory.UNVERIFIED_RUMOR.explanationRes,
                iconRes = ComplianceCategory.UNVERIFIED_RUMOR.iconRes,
                badgeColor = ComplianceCategory.UNVERIFIED_RUMOR.badgeColor,
                borderColor = ComplianceCategory.UNVERIFIED_RUMOR.borderColor
            ),
            GlossaryEntry(
                category = ComplianceCategory.HONEST_MISTAKE,
                section = GlossarySection.TRAPS,
                nameRes = ComplianceCategory.HONEST_MISTAKE.displayNameRes,
                explanationRes = ComplianceCategory.HONEST_MISTAKE.explanationRes,
                iconRes = ComplianceCategory.HONEST_MISTAKE.iconRes,
                badgeColor = ComplianceCategory.HONEST_MISTAKE.badgeColor,
                borderColor = ComplianceCategory.HONEST_MISTAKE.borderColor
            ),

            // Bonus
            GlossaryEntry(
                category = ComplianceCategory.SHIELD,
                section = GlossarySection.BONUS,
                nameRes = ComplianceCategory.SHIELD.displayNameRes,
                explanationRes = ComplianceCategory.SHIELD.explanationRes,
                iconRes = ComplianceCategory.SHIELD.iconRes,
                badgeColor = ComplianceCategory.SHIELD.badgeColor,
                borderColor = ComplianceCategory.SHIELD.borderColor
            ),
            GlossaryEntry(
                category = ComplianceCategory.BONUS_CORRUPTOR,
                section = GlossarySection.BONUS,
                nameRes = ComplianceCategory.BONUS_CORRUPTOR.displayNameRes,
                explanationRes = ComplianceCategory.BONUS_CORRUPTOR.explanationRes,
                iconRes = ComplianceCategory.BONUS_CORRUPTOR.iconRes,
                badgeColor = ComplianceCategory.BONUS_CORRUPTOR.badgeColor,
                borderColor = ComplianceCategory.BONUS_CORRUPTOR.borderColor
            )
        )
    }
}
