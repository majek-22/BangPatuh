package com.example.data

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.example.R

enum class BadgeShape {
    HEXAGON,        // Violations
    CIRCLE,         // Legitimate (safe)
    DIAMOND_DASHED, // Traps (decoys)
    STAR            // Bonuses
}

enum class ComplianceCategory(
    val id: String,
    @get:StringRes val displayNameRes: Int,
    val isViolation: Boolean,
    val isBonus: Boolean = false,
    val isTrap: Boolean = false,
    val basePoints: Int,
    @get:StringRes val explanationRes: Int,
    @get:DrawableRes val iconRes: Int,
    val badgeColor: Long,
    val borderColor: Long,
    val glowColor: Long = borderColor
) {
    // =========================================================================
    // Violations (MUST SLICE: +10 pts * comboMultiplier) — Gold/Amber (0xFFFFC857 / 0xFF3E2805)
    // =========================================================================
    BRIBERY(
        id = "bribery",
        displayNameRes = R.string.cat_bribery,
        isViolation = true,
        basePoints = 10,
        explanationRes = R.string.exp_bribery,
        iconRes = R.drawable.bribery,
        badgeColor = 0xFF3E2805,
        borderColor = 0xFFFFC857
    ),
    FRAUD(
        id = "fraud",
        displayNameRes = R.string.cat_fraud,
        isViolation = true,
        basePoints = 10,
        explanationRes = R.string.exp_fraud,
        iconRes = R.drawable.fraud,
        badgeColor = 0xFF3E2805,
        borderColor = 0xFFFFC857
    ),
    MONEY_LAUNDERING(
        id = "money_laundering",
        displayNameRes = R.string.cat_money_laundering,
        isViolation = true,
        basePoints = 10,
        explanationRes = R.string.exp_money_laundering,
        iconRes = R.drawable.money_laundering,
        badgeColor = 0xFF3E2805,
        borderColor = 0xFFFFC857
    ),
    DATA_BREACH(
        id = "data_breach",
        displayNameRes = R.string.cat_data_breach,
        isViolation = true,
        basePoints = 10,
        explanationRes = R.string.exp_data_breach,
        iconRes = R.drawable.data_breach,
        badgeColor = 0xFF3E2805,
        borderColor = 0xFFFFC857
    ),
    SYSTEMIC_CORRUPTION(
        id = "systemic_corruption",
        displayNameRes = R.string.cat_systemic_corruption,
        isViolation = true,
        basePoints = 25, // High-value rare violation
        explanationRes = R.string.exp_systemic_corruption,
        iconRes = R.drawable.systemic_corruption,
        badgeColor = 0xFF3E2805,
        borderColor = 0xFFFFC857
    ),
    INSIDER_TRADING(
        id = "insider_trading",
        displayNameRes = R.string.cat_insider_trading,
        isViolation = true,
        basePoints = 15,
        explanationRes = R.string.exp_insider_trading,
        iconRes = R.drawable.fraud,
        badgeColor = 0xFFB45309,
        borderColor = 0xFFFBBF24
    ),
    CONFLICT_OF_INTEREST(
        id = "conflict_of_interest",
        displayNameRes = R.string.cat_conflict_of_interest,
        isViolation = true,
        basePoints = 15,
        explanationRes = R.string.exp_conflict_of_interest,
        iconRes = R.drawable.bribery,
        badgeColor = 0xFF9A3412,
        borderColor = 0xFFFB923C
    ),
    EMBEZZLEMENT(
        id = "embezzlement",
        displayNameRes = R.string.cat_embezzlement,
        isViolation = true,
        basePoints = 20,
        explanationRes = R.string.exp_embezzlement,
        iconRes = R.drawable.money_laundering,
        badgeColor = 0xFF9F1239,
        borderColor = 0xFFFB7185
    ),

    // =========================================================================
    // Legitimate / Do-not-slice (AVOID: Slicing costs 1 life) — Sky Blue (0xFF64B5F6 / 0xFF0C2C4D)
    // =========================================================================
    OFFICIAL_DOCUMENT(
        id = "official_doc",
        displayNameRes = R.string.cat_official_document,
        isViolation = false,
        basePoints = 0,
        explanationRes = R.string.exp_official_document,
        iconRes = R.drawable.official_document,
        badgeColor = 0xFF0C2C4D,
        borderColor = 0xFF64B5F6
    ),
    VERIFIED_APPROVAL(
        id = "verified_approval",
        displayNameRes = R.string.cat_verified_approval,
        isViolation = false,
        basePoints = 0,
        explanationRes = R.string.exp_verified_approval,
        iconRes = R.drawable.verified_approval,
        badgeColor = 0xFF0C2C4D,
        borderColor = 0xFF64B5F6
    ),
    VALID_PARTNERSHIP(
        id = "valid_partnership",
        displayNameRes = R.string.cat_valid_partnership,
        isViolation = false,
        basePoints = 0,
        explanationRes = R.string.exp_valid_partnership,
        iconRes = R.drawable.valid_partnership,
        badgeColor = 0xFF0C2C4D,
        borderColor = 0xFF64B5F6
    ),
    CERTIFICATION(
        id = "certification",
        displayNameRes = R.string.cat_certification,
        isViolation = false,
        basePoints = 0,
        explanationRes = R.string.exp_certification,
        iconRes = R.drawable.certification,
        badgeColor = 0xFF0C2C4D,
        borderColor = 0xFF64B5F6
    ),
    VERIFIED_INVOICE(
        id = "verified_invoice",
        displayNameRes = R.string.cat_verified_invoice,
        isViolation = false,
        basePoints = 0,
        explanationRes = R.string.exp_verified_invoice,
        iconRes = R.drawable.verified_invoice,
        badgeColor = 0xFF0C2C4D,
        borderColor = 0xFF64B5F6
    ),
    COMPLIANCE_AUDIT(
        id = "compliance_audit",
        displayNameRes = R.string.cat_compliance_audit,
        isViolation = false,
        basePoints = 0,
        explanationRes = R.string.exp_compliance_audit,
        iconRes = R.drawable.official_document,
        badgeColor = 0xFF0C2C4D,
        borderColor = 0xFF64B5F6
    ),
    ETHICS_TRAINING(
        id = "ethics_training",
        displayNameRes = R.string.cat_ethics_training,
        isViolation = false,
        basePoints = 0,
        explanationRes = R.string.exp_ethics_training,
        iconRes = R.drawable.certification,
        badgeColor = 0xFF0C2C4D,
        borderColor = 0xFF64B5F6
    ),
    TRANSPARENCY_REPORT(
        id = "transparency_report",
        displayNameRes = R.string.cat_transparency_report,
        isViolation = false,
        basePoints = 0,
        explanationRes = R.string.exp_transparency_report,
        iconRes = R.drawable.verified_approval,
        badgeColor = 0xFF0C2C4D,
        borderColor = 0xFF64B5F6
    ),

    // =========================================================================
    // Traps (Decoys - Avoid: -10 pts, resets combo, no life lost) — Red (0xFFFF5252 / 0xFF3E0E0E)
    // =========================================================================
    FALSE_ALARM(
        id = "false_alarm",
        displayNameRes = R.string.cat_false_alarm,
        isViolation = false,
        isTrap = true,
        basePoints = 0,
        explanationRes = R.string.exp_false_alarm,
        iconRes = R.drawable.false_alarm,
        badgeColor = 0xFF3E0E0E,
        borderColor = 0xFFFF5252
    ),
    UNVERIFIED_RUMOR(
        id = "unverified_rumor",
        displayNameRes = R.string.cat_unverified_rumor,
        isViolation = false,
        isTrap = true,
        basePoints = 0,
        explanationRes = R.string.exp_unverified_rumor,
        iconRes = R.drawable.unverified_rumor,
        badgeColor = 0xFF3E0E0E,
        borderColor = 0xFFFF5252
    ),
    HONEST_MISTAKE(
        id = "honest_mistake",
        displayNameRes = R.string.cat_honest_mistake,
        isViolation = false,
        isTrap = true,
        basePoints = 0,
        explanationRes = R.string.exp_honest_mistake,
        iconRes = R.drawable.honest_mistake,
        badgeColor = 0xFF3E0E0E,
        borderColor = 0xFFFF5252
    ),
    PHISHING_BAIT(
        id = "phishing_bait",
        displayNameRes = R.string.cat_phishing_bait,
        isViolation = false,
        isTrap = true,
        basePoints = 0,
        explanationRes = R.string.exp_phishing_bait,
        iconRes = R.drawable.data_breach,
        badgeColor = 0xFF3E0E0E,
        borderColor = 0xFFFF5252
    ),
    MISFILED_MEMO(
        id = "misfiled_memo",
        displayNameRes = R.string.cat_misfiled_memo,
        isViolation = false,
        isTrap = true,
        basePoints = 0,
        explanationRes = R.string.exp_misfiled_memo,
        iconRes = R.drawable.honest_mistake,
        badgeColor = 0xFF3E0E0E,
        borderColor = 0xFFFF5252
    ),

    // =========================================================================
    // Bonus Items (~5% spawn chance: +1 life, +25 pts) — Vivid Green (0xFF00E676 / 0xFF052F1A)
    // =========================================================================
    SHIELD(
        id = "shield",
        displayNameRes = R.string.cat_shield,
        isViolation = false,
        isBonus = true,
        basePoints = 25,
        explanationRes = R.string.exp_shield,
        iconRes = R.drawable.compliance_shield,
        badgeColor = 0xFF052F1A,
        borderColor = 0xFF00E676
    ),
    WHISTLEBLOWER(
        id = "whistleblower",
        displayNameRes = R.string.cat_whistleblower,
        isViolation = false,
        isBonus = true,
        basePoints = 25,
        explanationRes = R.string.exp_whistleblower,
        iconRes = R.drawable.bonus_shield,
        badgeColor = 0xFF052F1A,
        borderColor = 0xFF00E676
    ),
    STAR_AUDITOR(
        id = "star_auditor",
        displayNameRes = R.string.cat_star_auditor,
        isViolation = false,
        isBonus = true,
        basePoints = 30,
        explanationRes = R.string.exp_star_auditor,
        iconRes = R.drawable.bonus_shield,
        badgeColor = 0xFF052F1A,
        borderColor = 0xFF00E676
    ),
    BONUS_CORRUPTOR(
        id = "bonus_corruptor",
        displayNameRes = R.string.cat_bonus_corruptor,
        isViolation = false,
        isBonus = true,
        basePoints = 10,
        explanationRes = R.string.exp_bonus_corruptor,
        iconRes = R.drawable.bonus_corruptor,
        badgeColor = 0xFF052F1A,
        borderColor = 0xFF00E676
    );

    val isFreezeBonus: Boolean
        get() = this == BONUS_CORRUPTOR

    val badgeShape: BadgeShape
        get() = when {
            isBonus -> BadgeShape.STAR
            isTrap -> BadgeShape.DIAMOND_DASHED
            isViolation -> BadgeShape.HEXAGON
            else -> BadgeShape.CIRCLE
        }

    companion object {
        // Only the exact 14 categories present in RULES screen (GlossaryEntry.ALL_ENTRIES)
        val RULES_CATEGORIES = setOf(
            BRIBERY, FRAUD, MONEY_LAUNDERING, DATA_BREACH, SYSTEMIC_CORRUPTION,
            OFFICIAL_DOCUMENT, VERIFIED_APPROVAL, VALID_PARTNERSHIP, CERTIFICATION, VERIFIED_INVOICE,
            FALSE_ALARM, UNVERIFIED_RUMOR, HONEST_MISTAKE,
            SHIELD
        )

        val VIOLATIONS = entries.filter { it.isViolation && it in RULES_CATEGORIES && it != SYSTEMIC_CORRUPTION }
        val ALL_VIOLATIONS = entries.filter { it.isViolation && it in RULES_CATEGORIES }
        val LEGITIMATE = entries.filter { !it.isViolation && !it.isBonus && !it.isTrap && it in RULES_CATEGORIES }
        val TRAPS = entries.filter { it.isTrap && it in RULES_CATEGORIES }
        val BONUSES = entries.filter { it.isBonus && it in RULES_CATEGORIES }
    }

    val categoryTypeLabelRes: Int
        @StringRes get() = when {
            isBonus -> R.string.category_bonus
            isTrap -> R.string.category_trap
            isViolation -> R.string.category_violation
            else -> R.string.category_legit
        }

    fun getDisplayName(language: String): String {
        val lang = language.lowercase()
        val isId = lang == "in" || lang == "id"
        val isJa = lang == "ja"
        return when (this) {
            BRIBERY -> if (isJa) "贈収賄" else if (isId) "Suap" else "Bribery"
            FRAUD -> if (isJa) "不正・詐欺" else if (isId) "Kecurangan (Fraud)" else "Fraud"
            MONEY_LAUNDERING -> if (isJa) "資金洗浄" else if (isId) "Pencucian Uang" else "Money Laundering"
            DATA_BREACH -> if (isJa) "データ漏洩" else if (isId) "Kebocoran Data" else "Data Breach"
            SYSTEMIC_CORRUPTION -> if (isJa) "組織的腐敗" else if (isId) "Korupsi Sistemik" else "Systemic Corruption"
            INSIDER_TRADING -> if (isJa) "インサイダー取引" else if (isId) "Perdagangan Orang Dalam" else "Insider Trading"
            CONFLICT_OF_INTEREST -> if (isJa) "利益相反" else if (isId) "Benturan Kepentingan" else "Conflict of Interest"
            EMBEZZLEMENT -> if (isJa) "横領・背任" else if (isId) "Penggelapan Dana" else "Embezzlement"
            OFFICIAL_DOCUMENT -> if (isJa) "公式文書" else if (isId) "Dokumen Resmi" else "Official Document"
            VERIFIED_APPROVAL -> if (isJa) "正規決裁" else if (isId) "Persetujuan Terverifikasi" else "Verified Approval"
            VALID_PARTNERSHIP -> if (isJa) "正規提携" else if (isId) "Kemitraan Sah" else "Valid Partnership"
            CERTIFICATION -> if (isJa) "認証規格" else if (isId) "Sertifikasi" else "Certification"
            VERIFIED_INVOICE -> if (isJa) "正規請求書" else if (isId) "Faktur Terverifikasi" else "Verified Invoice"
            COMPLIANCE_AUDIT -> if (isJa) "コンプライアンス監査" else if (isId) "Audit Kepatuhan" else "Compliance Audit"
            ETHICS_TRAINING -> if (isJa) "倫理研修" else if (isId) "Pelatihan Etika" else "Ethics Training"
            TRANSPARENCY_REPORT -> if (isJa) "透明性レポート" else if (isId) "Laporan Transparansi" else "Transparency Report"
            FALSE_ALARM -> if (isJa) "誤報 (False Alarm)" else if (isId) "Alarm Palsu" else "False Alarm"
            UNVERIFIED_RUMOR -> if (isJa) "未確認の噂" else if (isId) "Rumor Tak Terverifikasi" else "Unverified Rumor"
            HONEST_MISTAKE -> if (isJa) "事務的ミス" else if (isId) "Kekhilafan Administratif" else "Honest Mistake"
            PHISHING_BAIT -> if (isJa) "フィッシング訓練メール" else if (isId) "Umpan Phishing" else "Phishing Bait"
            MISFILED_MEMO -> if (isJa) "誤分類メモ" else if (isId) "Memo Salah Simpan" else "Misfiled Memo"
            SHIELD -> if (isJa) "コンプライアンスシールド" else if (isId) "Perisai Kepatuhan" else "Compliance Shield"
            WHISTLEBLOWER -> if (isJa) "内部通報窓口 (WBS)" else if (isId) "Saluran WBS (Pelapor)" else "Whistleblower Hotline"
            STAR_AUDITOR -> if (isJa) "模範監査人" else if (isId) "Auditor Teladan" else "Star Auditor"
            BONUS_CORRUPTOR -> if (isJa) "腐敗者 (ボーナス)" else if (isId) "Koruptor (Bonus)" else "Corruptor (Bonus)"
        }
    }

    fun getExplanation(language: String): String {
        val lang = language.lowercase()
        val isId = lang == "in" || lang == "id"
        val isJa = lang == "ja"
        return when (this) {
            BRIBERY -> when {
                isJa -> "腐敗防止法規: 金品や便宜などの贈収賄・不当な便宜供与の授受は、高潔性および法令違反となります。"
                isId -> "UU No. 20 Tahun 2001 Tentang Tindak Pidana Korupsi: Memberi atau menerima suap/gratifikasi melanggar integritas dan ketentuan hukum tindak pidana korupsi."
                else -> "Law No. 20/2001 on Corruption Eradication: Giving or receiving bribes/gratuities violates integrity and anti-corruption statutory provisions."
            }

            FRAUD -> when {
                isJa -> "社内記録の改ざん、署名偽造、架空計上などは企業の真実性と組織の信用を根底から損ないます。"
                isId -> "Manipulasi catatan perusahaan, pemalsuan tanda tangan, atau pembukuan fiktif merusak kebenaran dan integritas organisasi."
                else -> "Manipulating corporate records, forging signatures, or fictitious bookkeeping damages organizational truth and integrity."
            }

            MONEY_LAUNDERING -> when {
                isJa -> "マネーロンダリング防止規則 (AML-CFT): 犯罪収益の出所を隠蔽するための不審な取引の組成を未然に防止します。"
                isId -> "Regulasi APU-PPT (Anti Pencucian Uang dan Pencegahan Pendanaan Terorisme): Menata transaksi mencurigakan untuk menyamarkan asal dana kejahatan."
                else -> "AML-CFT Regulations (Anti-Money Laundering & Countering Financing of Terrorism): Structuring suspicious transactions to disguise illicit proceeds."
            }

            DATA_BREACH -> when {
                isJa -> "個人情報保護法規: 権限のない機密情報や顧客個人情報の持ち出し・開示は重大な法令違反となります。"
                isId -> "UU No. 27 Tahun 2022 Tentang Pelindungan Data Pribadi: Membuka data rahasia/pribadi tanpa otorisasi sah melanggar hukum privasi."
                else -> "Law No. 27/2022 on Personal Data Protection: Unauthorized disclosure of confidential or personal data violates privacy laws."
            }

            SYSTEMIC_CORRUPTION -> when {
                isJa -> "組織的な権限の濫用は制度的信頼を失墜させ、企業全体に重い法的責任をもたらします。"
                isId -> "Penyalahgunaan wewenang secara terstruktur dan terlembaga merusak integritas dan membawa pertanggungjawaban pidana berat."
                else -> "Deeply entrenched abuse of entrusted power threatens institutional integrity and creates severe corporate criminal liability."
            }

            INSIDER_TRADING -> when {
                isJa -> "未公開の重要情報に基づく有価証券取引は、市場の公正性と信頼を著しく毀損します。"
                isId -> "Berdagang efek berdasarkan informasi rahasia merusak keadilan pasar dan kepercayaan."
                else -> "Trading securities based on non-public material information compromises market fairness."
            }

            CONFLICT_OF_INTEREST -> when {
                isJa -> "公私の立場を混同し個人的または親族の利益を優先することは客観的判断を阻害します。"
                isId -> "Memanfaatkan wewenang untuk keuntungan pribadi atau keluarga merusak objektivitas."
                else -> "Using official position for personal or familial gain undermines objective governance."
            }

            EMBEZZLEMENT -> when {
                isJa -> "会社から預託された資金や資産を私的流用する行為は重大な背任・犯罪行為です。"
                isId -> "Menyalahgunakan dana titipan perusahaan untuk kepentingan pribadi adalah tindak pidana berat."
                else -> "Misappropriating entrusted company assets for personal gain is a severe criminal offense."
            }

            OFFICIAL_DOCUMENT -> when {
                isJa -> "承認済みの公式記録は適正に保管され、所定のガバナンス手続きに則って処理されなければなりません。"
                isId -> "Catatan resmi perusahaan harus dijaga dan diproses melalui saluran tata kelola kepatuhan yang sah."
                else -> "Authorized corporate records must be preserved and processed through designated compliance governance channels."
            }

            VERIFIED_APPROVAL -> when {
                isJa -> "適切な二重チェックと管理職承認により、あらゆる業務判断における説明責任が担保されます。"
                isId -> "Persetujuan ganda dan tanda tangan manajemen yang sah memastikan akuntabilitas atas setiap keputusan bisnis."
                else -> "Proper dual-control sign-offs and management approvals ensure accountability and checks across all business decisions."
            }

            VALID_PARTNERSHIP -> when {
                isJa -> "厳格な反社会的勢力・腐敗防止デューデリジェンスを通過した取引先は正当なビジネスパートナーです。"
                isId -> "Mitra pihak ketiga yang telah melalui uji tuntas (due diligence) anti-korupsi adalah rekanan bisnis yang sah."
                else -> "Vetted third-party counterparties who pass rigorous anti-corruption due diligence are legitimate business partners."
            }

            CERTIFICATION -> when {
                isJa -> "公的規格およびISO認証は、国際的な安全および倫理基準の遵守を客観的に証明するものです。"
                isId -> "Sertifikasi industri membuktikan ketaatan operasional terhadap standar keselamatan dan etika internasional."
                else -> "Industry and regulatory certifications validate verified operational adherence to international safety and ethical benchmarks."
            }

            VERIFIED_INVOICE -> when {
                isJa -> "発注書（PO）と完全に一致する正規請求書は、架空請求から組織の財務健全性を保護します。"
                isId -> "Faktur sah yang cocok dengan pesanan pembelian (PO) menjaga integritas keuangan dari tagihan fiktif."
                else -> "Accurate invoices matched to legitimate purchase orders safeguard financial integrity against fraudulent billing."
            }

            COMPLIANCE_AUDIT -> when {
                isJa -> "業務プロセスが法令および社内規則に準拠しているかを検証する独立した評価手続きです。"
                isId -> "Verifikasi independen untuk memastikan kesesuaian operasional dengan standar regulasi."
                else -> "Independent verification ensuring operational adherence to regulatory benchmarks."
            }

            ETHICS_TRAINING -> when {
                isJa -> "全従業員が高潔な規範を維持し、違反リスクを主体的に予防するための継続的な啓蒙活動です。"
                isId -> "Pembekalan etika berkelanjutan agar seluruh jajaran sigap mencegah pelanggaran integritas."
                else -> "Continuous ethics development empowering staff to prevent integrity violations."
            }

            TRANSPARENCY_REPORT -> when {
                isJa -> "企業のガバナンス状況と社会的是正の取り組みを公開し、社会的説明責任を高めます。"
                isId -> "Keterbukaan informasi tata kelola yang memperkuat akuntabilitas institusi."
                else -> "Public reporting of governance performance fostering institutional accountability."
            }

            FALSE_ALARM -> when {
                isJa -> "根拠のないセキュリティ通報。事実確認を怠った拙速な対応は組織のリソースを浪費します。"
                isId -> "Laporan keamanan yang ternyata tidak berdasar. Bertindak terburu-buru tanpa verifikasi membuang sumber daya perusahaan."
                else -> "An unconfirmed security tip that turned out to be baseless. Acting hastily without verification wastes corporate resources."
            }

            UNVERIFIED_RUMOR -> when {
                isJa -> "事実の裏付けがない社内の噂話。真のコンプライアンスには憶測ではなく確証ある証拠が必要です。"
                isId -> "Gosip kantor tanpa bukti faktual. Kepatuhan profesional membutuhkan bukti dokumen, bukan spekulasi kabar angin."
                else -> "Office gossip lacking factual evidence. Professional compliance requires documented proof, not speculative hearsay."
            }

            HONEST_MISTAKE -> when {
                isJa -> "偶発的な入力ミスや形式の不備。単純な人為的ミスには厳罰ではなく丁寧な指導と再発防止が求められます。"
                isId -> "Kekhilafan ketik atau kesalahan format yang tidak disengaja. Kesalahan manusia memerlukan bimbingan, bukan sanksi pidana."
                else -> "An accidental clerical typo or formatting error. Human mistakes require guidance and coaching, not criminal sanction."
            }

            PHISHING_BAIT -> when {
                isJa -> "情報セキュリティ意識を測る擬似訓練。慌てて判断せず正規の報告手順を踏みましょう。"
                isId -> "Simulasi internal untuk menguji kewaspadaan staf. Selalu verifikasi sebelum memicu alarm."
                else -> "A simulation test designed to assess alertness. Always verify before reporting a breach."
            }

            MISFILED_MEMO -> when {
                isJa -> "正当な文書の一時的な保管ミス。過度な処分ではなく適正な再分類と整理が必要です。"
                isId -> "Kekeliruan pengarsipan dokumen sah. Cukup perbaiki penataan tanpa perlu eskalasi."
                else -> "An accidental document misplacement. Needs simple re-archiving, not disciplinary escalation."
            }

            SHIELD -> when {
                isJa -> "日常的なリスク管理意識と内部統制は、企業の評判失墜や法定制裁を防ぐ強固な盾となります。"
                isId -> "Kewaspadaan risiko dan pengendalian internal proaktif berfungsi sebagai perisai dari kerugian reputasi dan denda hukum."
                else -> "Proactive risk vigilance and internal controls act as a corporate shield against reputational damage and regulatory penalties."
            }

            WHISTLEBLOWER -> when {
                isJa -> "通報者のプライバシーを厳格に保護し、組織内の隠れた不正リスクを早期自浄する通報制度です。"
                isId -> "Saluran pelaporan rahasia yang melindungi pelapor integritas dan membongkar risiko."
                else -> "Confidential reporting channel safeguarding whistleblowers and unmasking internal risks."
            }

            STAR_AUDITOR -> when {
                isJa -> "組織の企業倫理とコンプライアンス推進に顕著な貢献を果たした捜査官への名誉表彰です。"
                isId -> "Penghargaan kehormatan atas dedikasi luar biasa dalam tata kelola etika korporat."
                else -> "Honorary commendation recognizing exemplary dedication to institutional integrity."
            }

            BONUS_CORRUPTOR -> when {
                isJa -> "特別フリーズボーナス！ゲームが5秒間停止します。連続スラッシュで不正資金を回収しボーナス点を獲得せよ！"
                isId -> "Target bonus koruptor spesial! Game membeku selama 5 detik, tebas sebanyak-banyaknya untuk melipatgandakan poin bonus."
                else -> "Special bonus corruptor target! Freezes the game for 5 seconds, slice as much as possible for massive bonus points."
            }
        }
    }
}
