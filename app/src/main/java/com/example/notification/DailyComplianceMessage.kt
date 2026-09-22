package com.example.notification

import android.content.Context
import android.content.SharedPreferences
import com.example.R
import java.util.Calendar

data class DailyComplianceMessage(
    val id: Int,
    val categoryBadge: String,
    val categoryBadgeEn: String,
    val categoryBadgeJa: String,
    val title: String,
    val titleEn: String,
    val titleJa: String,
    val pushSummary: String,
    val pushSummaryEn: String,
    val pushSummaryJa: String,
    val fullMessage: String,
    val fullMessageEn: String,
    val fullMessageJa: String,
    val goldenRules: List<String>,
    val goldenRulesEn: List<String>,
    val goldenRulesJa: List<String>,
    val consequences: String,
    val consequencesEn: String,
    val consequencesJa: String,
    val iconRes: Int
) {
    fun getLocalizedTitle(lang: String): String = when (lang.lowercase()) {
        "en" -> titleEn
        "ja" -> titleJa
        else -> title
    }

    fun getLocalizedBadge(lang: String): String = when (lang.lowercase()) {
        "en" -> categoryBadgeEn
        "ja" -> categoryBadgeJa
        else -> categoryBadge
    }

    fun getLocalizedPushSummary(lang: String): String = when (lang.lowercase()) {
        "en" -> pushSummaryEn
        "ja" -> pushSummaryJa
        else -> pushSummary
    }

    fun getLocalizedFullMessage(lang: String): String = when (lang.lowercase()) {
        "en" -> fullMessageEn
        "ja" -> fullMessageJa
        else -> fullMessage
    }

    fun getLocalizedGoldenRules(lang: String): List<String> = when (lang.lowercase()) {
        "en" -> goldenRulesEn
        "ja" -> goldenRulesJa
        else -> goldenRules
    }

    fun getLocalizedConsequences(lang: String): String = when (lang.lowercase()) {
        "en" -> consequencesEn
        "ja" -> consequencesJa
        else -> consequences
    }
}

object DailyComplianceRepository {

    private const val PREFS_NAME = "compliance_daily_notif_prefs"
    private const val KEY_LAST_READ_DAY_OF_YEAR = "last_read_day_of_year"
    private const val KEY_LAST_READ_YEAR = "last_read_year"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun isTodayRead(context: Context): Boolean {
        val prefs = getPrefs(context)
        val cal = Calendar.getInstance()
        val currentDayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val currentYear = cal.get(Calendar.YEAR)
        val savedDay = prefs.getInt(KEY_LAST_READ_DAY_OF_YEAR, -1)
        val savedYear = prefs.getInt(KEY_LAST_READ_YEAR, -1)
        return currentDayOfYear == savedDay && currentYear == savedYear
    }

    fun markTodayRead(context: Context) {
        val prefs = getPrefs(context)
        val cal = Calendar.getInstance()
        prefs.edit()
            .putInt(KEY_LAST_READ_DAY_OF_YEAR, cal.get(Calendar.DAY_OF_YEAR))
            .putInt(KEY_LAST_READ_YEAR, cal.get(Calendar.YEAR))
            .apply()
    }

    fun getTodayMessage(): DailyComplianceMessage {
        val cal = Calendar.getInstance()
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)
        val index = (dayOfYear - 1).coerceAtLeast(0) % ALL_MESSAGES.size
        return ALL_MESSAGES[index]
    }

    fun getMessageById(id: Int): DailyComplianceMessage {
        return ALL_MESSAGES.find { it.id == id } ?: ALL_MESSAGES[0]
    }

    val ALL_MESSAGES: List<DailyComplianceMessage> = listOf(
        // Day 1: Fraud - Manipulasi Laporan & Faktur Fiktif
        DailyComplianceMessage(
            id = 1,
            categoryBadge = "🚨 BAHAYA FRAUD",
            categoryBadgeEn = "🚨 FRAUD HAZARD",
            categoryBadgeJa = "🚨 不正・詐欺の危険",
            title = "Manipulasi Laporan & Faktur Fiktif Merugikan Kita Semua!",
            titleEn = "Report Manipulation & Fake Invoices Harm Everyone!",
            titleJa = "報告書の改ざんと架空請求は全員に害を及ぼします！",
            pushSummary = "Pagi rekan kerja! Ingat: Merekayasa kuitansi atau memalsukan laporan keuangan adalah tindak pidana fraud yang merusak karir seketika.",
            pushSummaryEn = "Good morning team! Altering receipts or falsifying financial reports is a severe fraud crime destroying careers instantly.",
            pushSummaryJa = "おはようございます！領収書の偽造や財務報告の改ざんは重大な不正行為です。",
            fullMessage = "Fraud finansial seperti pembuatan faktur fiktif, rekayasa kwitansi reimburse, dan mark-up anggaran bukan jalan pintas yang cerdas, melainkan kejahatan serius. Dampak tindakan ini tidak hanya merusak integritas perusahaan, tetapi juga menyeret pelakunya ke proses pidana hukum dan pemecatan tidak terhormat.",
            fullMessageEn = "Financial fraud such as fictitious billing, receipt tampering, and budget markup is never a shortcut—it is a grave criminal offence that ruins your professional career and subjects you to criminal prosecution.",
            fullMessageJa = "架空請求や領収書の改ざん、予算の水増しなどの財務不正はキャリアを一瞬で破滅させ、刑事責任と懲戒解雇につながります。",
            goldenRules = listOf(
                "Selalu lampirkan bukti transaksi riil dan absah untuk setiap pengeluaran.",
                "Tolak instruksi lisan untuk mengubah angka laporan tanpa dokumen pendukung.",
                "Laporkan indikasi mark-up atau penggelembungan dana melalui Whistleblowing System."
            ),
            goldenRulesEn = listOf(
                "Always attach authentic proof of actual transactions for every reimbursement.",
                "Refuse verbal instructions to adjust numbers without verifiable paperwork.",
                "Report suspicious markup or forged receipts through the official Whistleblowing Channel."
            ),
            goldenRulesJa = listOf(
                "すべての経費精算には必ず正規の取引証明を添付してください。",
                "裏付け書類のない数値変更の指示には決して従わないでください。",
                "不正な水増し請求を発見した場合は内部通報窓口へ報告してください。"
            ),
            consequences = "Pasal 378 KUHP (Penipuan) & Pasal 374 KUHP (Penggelapan dalam Jabatan), pidana penjara hingga 5 tahun, Pemutusan Hubungan Kerja (PHK) tanpa pesangon, dan blacklist sektor industri.",
            consequencesEn = "Criminal fraud charges, up to 5 years imprisonment, immediate termination without severance, and corporate industry blacklist.",
            consequencesJa = "懲戒解雇、損害賠償請求、および最長5年の刑事罰（詐欺・業務上横領罪）。",
            iconRes = R.drawable.fraud
        ),

        // Day 2: Money Laundering - Waspada Rekening Pinjaman (Smurfing & Mule)
        DailyComplianceMessage(
            id = 2,
            categoryBadge = "💰 BAHAYA MONEY LAUNDERING",
            categoryBadgeEn = "💰 ANTI-MONEY LAUNDERING",
            categoryBadgeJa = "💰 マネーロンダリングの脅威",
            title = "Waspadai Modus Pinjam Rekening (Money Mule) & TPPU!",
            titleEn = "Beware of Account Borrowing (Money Mule) & AML Risks!",
            titleJa = "口座貸し出し（マネーミュール）と資金洗浄の罠！",
            pushSummary = "Jangan pernah meminjamkan rekening bank pribadi atau kantor untuk transaksi mencurigakan. Anda bisa dijerat UU Tindak Pidana Pencucian Uang!",
            pushSummaryEn = "Never lend your personal or company bank account for suspicious transfers. You could face Anti-Money Laundering criminal charges!",
            pushSummaryJa = "不審な送金のために個人や会社の口座を貸さないでください。資金洗浄幇助罪に問われます！",
            fullMessage = "Pencucian uang (TPPU) adalah upaya menyamarkan asal-usul kekayaan hasil kejahatan seperti narkotika, korupsi, atau penipuan agar tampak sah. Pelaku kerap memanfaatkan karyawan sebagai 'Money Mule' dengan iming-iming komisi untuk mentransfer dana bertahap (structuring/smurfing). Jika Anda terlibat, Anda dapat dipidana sebagai pihak yang turut serta.",
            fullMessageEn = "Money Laundering disguises proceeds of crime to make illicit funds appear clean. Criminal syndicates often recruit employees as 'Money Mules' offering commissions. Participating in account lending makes you criminally liable as a co-conspirator.",
            fullMessageJa = "資金洗浄（マネーロンダリング）は犯罪収益の出所を偽装する行為です。手数料目当てで口座を他人に利用させると、共犯として重い処罰を受けます。",
            goldenRules = listOf(
                "Terapkan Know Your Customer (KYC) dan Kenali Rekan Transaksi secara menyeluruh.",
                "Jangan pernah mengizinkan pihak manapun menggunakan rekening bank Anda untuk lalu lintas dana.",
                "Segera laporkan transaksi tunai mencurigakan yang tidak sesuai profil nasabah atau vendor."
            ),
            goldenRulesEn = listOf(
                "Strictly adhere to Know Your Customer (KYC) and counterparty verification.",
                "Never allow third parties to route unexplained funds through your accounts.",
                "Promptly flag structured deposits or unusual transfers lacking commercial rationale."
            ),
            goldenRulesJa = listOf(
                "徹底した本人確認（KYC）と取引先の精査を行ってください。",
                "第三者に自分の銀行口座を使わせることは絶対に禁止です。",
                "事業実態のない不審な送金や分割入金は直ちにコンプライアンスへ報告してください。"
            ),
            consequences = "UU No. 8 Tahun 2010 tentang Pencegahan dan Pemberantasan Tindak Pidana Pencucian Uang (TPPU), pidana penjara maksimal 20 tahun dan denda hingga Rp 10 Miliar.",
            consequencesEn = "Anti-Money Laundering statutory prosecution, up to 20 years imprisonment, multi-million dollar fines, and permanent financial sector disqualification.",
            consequencesJa = "最長20年の懲役、巨額の罰金、および金融・ビジネス界からの永久追放。",
            iconRes = R.drawable.money_laundering
        ),

        // Day 3: Korupsi & Suap - Bahaya Gratifikasi Terselubung
        DailyComplianceMessage(
            id = 3,
            categoryBadge = "⚖️ BAHAYA KORUPSI & SUAP",
            categoryBadgeEn = "⚖️ ANTI-CORRUPTION & BRIBERY",
            categoryBadgeJa = "⚖️ 汚職・贈収賄の禁止",
            title = "Gratifikasi Bukan Hadiah, Itu Umpan Korupsi!",
            titleEn = "Gratification Is Not a Gift, It's Bait for Corruption!",
            titleJa = "贈答品は親善ではなく、汚職への罠です！",
            pushSummary = "Menerima 'hadiah terimakasih', komisi gelap, atau fasilitas mewah dari vendor adalah bentuk suap yang merusak netralitas keputusan Anda.",
            pushSummaryEn = "Accepting undisclosed gifts, kickbacks, or luxury perks from vendors is bribery destroying objective decision-making.",
            pushSummaryJa = "取引先からの金品や過剰な接待は、公正な職務遂行を妨げる贈収賄行為です。",
            fullMessage = "Korupsi dan suap berakar dari penerimaan gratifikasi kecil yang dibiarkan: jamuan makan berlebihan, tiket liburan, hingga potongan komisi (kickback). Begitu Anda menerima pemberian dari pihak yang memiliki kepentingan bisnis, Anda terikat hutang budi dan kehilangan objektivitas dalam pengadaan atau audit, merugikan korporasi serta bangsa.",
            fullMessageEn = "Corruption begins with seemingly harmless perks: excessive hospitality, vacation vouchers, or undisclosed kickbacks. Once accepted, your loyalty is compromised, creating conflicts of interest that destroy fair competition and corporate integrity.",
            fullMessageJa = "汚職は小さな贈り物や過度な接待から始まります。利害関係者からの利益供与を受け入れると公平性を失い、背任や贈収賄の罪に問われます。",
            goldenRules = listOf(
                "Tolak secara santun segala bentuk uang, bingkisan berharga, atau fasilitas pribadi dari mitra bisnis.",
                "Laporkan setiap pemberian hadiah dalam kurun waktu 30 hari ke Unit Pengendalian Gratifikasi.",
                "Jaga transparansi mutlak dalam proses tender, evaluasi vendor, dan penetapan kontrak."
            ),
            goldenRulesEn = listOf(
                "Politely reject all cash, valuable gifts, or personal perks from business partners.",
                "Register any gift declarations to the Compliance/Gratification Unit within the statutory period.",
                "Maintain absolute fairness and objective criteria in vendor procurement and contracting."
            ),
            goldenRulesJa = listOf(
                "取引先からの金銭、高額な贈答品、個人的な便宜供与は丁重に辞退してください。",
                "受領した物品や接待は社内規定に従いコンプライアンス部門へ申告してください。",
                "調達・選定プロセスは常に透明で公平な基準で行ってください。"
            ),
            consequences = "UU Tindak Pidana Korupsi (Tipikor), ancaman pidana seumur hidup atau penjara minimal 4 tahun, denda miliaran rupiah, dan penyitaan aset pribadi.",
            consequencesEn = "Anti-Corruption Act prosecution, minimum 4 years to life imprisonment, severe asset forfeiture, and catastrophic public disgrace.",
            consequencesJa = "贈収賄罪による逮捕・起訴、懲役刑、損害賠償、および社会的信用の完全な失墜。",
            iconRes = R.drawable.systemic_corruption
        ),

        // Day 4: Konflik Kepentingan - Bisnis Pribadi & Nepotisme
        DailyComplianceMessage(
            id = 4,
            categoryBadge = "👥 KONFLIK KEPENTINGAN",
            categoryBadgeEn = "👥 CONFLICT OF INTEREST",
            categoryBadgeJa = "👥 利益相反の防止",
            title = "Utamakan Kepentingan Perusahaan, Hindari Nepotisme!",
            titleEn = "Put Corporate Interest First, Prevent Nepotism!",
            titleJa = "会社の利益を最優先にし、身内びいきを排除しましょう！",
            pushSummary = "Menunjuk vendor milik keluarga atau memanfaatkan fasilitas kantor untuk bisnis sampingan pribadi melanggar Kode Etik Kepatuhan.",
            pushSummaryEn = "Appointing family-owned suppliers or using company resources for personal sidelines breaches our Code of Conduct.",
            pushSummaryJa = "親族の会社への便宜供与や会社の資産を私的副業に流用することは固く禁じられています。",
            fullMessage = "Konflik kepentingan terjadi ketika keputusan profesional Anda dipengaruhi oleh kepentingan pribadi, keluarga, atau bisnis sampingan. Menunjuk rekan dekat tanpa tender yang adil atau menyewakan aset perusahaan demi keuntungan pribadi menciptakan celah kecurangan yang merusak iklim kerja yang sehat dan adil.",
            fullMessageEn = "A conflict of interest arises when personal gains, family ties, or secondary business interests compromise professional judgments. Bypassing competitive bidding to favor relatives damages trust and violates corporate fiduciary duty.",
            fullMessageJa = "利益相反とは、私利私欲や身内の利益のために公正な職務判断が歪められる状態です。私的な便宜供与は公平な競争環境を破壊します。",
            goldenRules = listOf(
                "Deklarasikan hubungan keluarga atau keterikatan finansial dengan vendor calon mitra.",
                "Mengundurkan diri (recuse) dari tim penilai jika memiliki hubungan personal dengan kandidat vendor.",
                "Jangan gunakan jam kerja, data, atau fasilitas kantor untuk kepentingan usaha pribadi."
            ),
            goldenRulesEn = listOf(
                "Formally disclose any familial or financial affiliations with participating vendors.",
                "Recuse yourself from evaluation committees whenever an affiliate is participating.",
                "Never utilize corporate working hours, intellectual assets, or facilities for personal ventures."
            ),
            goldenRulesJa = listOf(
                "取引先との親族関係や利害関係がある場合は事前に開示・申告してください。",
                "関係者が関わる選定・審査には直接関与せず、担当を辞退してください。",
                "会社の機密情報やリソースを私的な副業に利用しないでください。"
            ),
            consequences = "Pembatalan kontrak sepihak, audit investigasi internal, surat peringatan keras hingga pemecatan, serta tuntutan ganti rugi materiil.",
            consequencesEn = "Contract nullification, forensic internal audits, immediate dismissal, and civil recovery lawsuits for financial losses.",
            consequencesJa = "契約の無効化、内部監査による追及、懲戒解雇、および損害賠償請求。",
            iconRes = R.drawable.ic_laurel_shield
        ),

        // Day 5: Pembocoran Data & Kerahasiaan Perusahaan
        DailyComplianceMessage(
            id = 5,
            categoryBadge = "🔒 KERAHASIAAN & DATA PRIVACY",
            categoryBadgeEn = "🔒 DATA PRIVACY & SECRECY",
            categoryBadgeJa = "🔒 機密保持と個人情報保護",
            title = "Data Nasabah & Rahasia Perusahaan Adalah Amanah!",
            titleEn = "Customer Data & Corporate Secrets Are Sacred Trusts!",
            titleJa = "顧客データと社内機密は厳重に守るべき財産です！",
            pushSummary = "Membocorkan data nasabah atau informasi rahasia ke pihak luar berisiko tuntutan pidana UU PDP dan menghancurkan reputasi perusahaan.",
            pushSummaryEn = "Leaking sensitive client records or trade secrets risks criminal prosecution under Data Protection laws and destroys public trust.",
            pushSummaryJa = "顧客情報の漏洩や社外への無断持ち出しは法律違反であり、企業の存続を脅かします。",
            fullMessage = "Di era digital, data pelanggan, strategi penawaran harga, dan laporan keuangan sebelum rilis adalah aset paling berharga. Berbagi dokumen rahasia melalui aplikasi perpesanan pribadi, menjual database nasabah, atau membeberkan informasi internal kepada pesaing adalah pelanggaran berat UU Pelindungan Data Pribadi (UU PDP).",
            fullMessageEn = "Client databases, pricing models, and unreleased financial metrics are our most vulnerable assets. Exporting sensitive records to personal drives, selling contact lists, or sharing intel with competitors violates Personal Data Protection regulations.",
            fullMessageJa = "顧客名簿や価格戦略、未公開財務データなどの機密情報を私用チャットで送信したり社外へ持ち出すことは重大な守秘義務違反です。",
            goldenRules = listOf(
                "Jangan pernah mengirim data pekerjaan ke email atau cloud storage pribadi.",
                "Kunci layar komputer (Win+L) setiap kali meninggalkan meja kerja.",
                "Terapkan prinsip Clean Desk & Clean Screen: amankan dokumen fisik di laci terkunci."
            ),
            goldenRulesEn = listOf(
                "Never forward confidential company datasets to personal mailboxes or cloud drives.",
                "Always lock your computer workstation (Win+L) when leaving your desk.",
                "Enforce Clean Desk & Clean Screen policies: lock away paper files before leaving."
            ),
            goldenRulesJa = listOf(
                "業務データを私用のメールやクラウドストレージに絶対に転送しないでください。",
                "離席する際は必ずPC画面をロック（Win+L）してください。",
                "重要書類は机の上に放置せず、鍵のかかるキャビネットに保管してください。"
            ),
            consequences = "UU No. 27 Tahun 2022 tentang Pelindungan Data Pribadi (UU PDP), pidana penjara hingga 5 tahun dan denda hingga puluhan miliar rupiah bagi pelaku pembocoran.",
            consequencesEn = "Data Protection Act violations, up to 5 years imprisonment, statutory administrative penalties, and immediate termination.",
            consequencesJa = "個人情報保護法違反による刑事告発、最大5年の懲役、巨額の過料、および懲戒解雇。",
            iconRes = R.drawable.data_breach
        ),

        // Day 6: Pengabaian Prosedur & Bypass SOP
        DailyComplianceMessage(
            id = 6,
            categoryBadge = "🛡️ KEPATUHAN SISTEM & SOP",
            categoryBadgeEn = "🛡️ INTERNAL CONTROLS & SOP",
            categoryBadgeJa = "🛡️ 内部統制とSOPの遵守",
            title = "SOP Bukan Hambatan, SOP Adalah Perisai Perlindungan!",
            titleEn = "SOP Is Not An Obstacle, It Is Your Safety Shield!",
            titleJa = "業務規程（SOP）は障害ではなく、あなたを守る盾です！",
            pushSummary = "Melewati otorisasi berlapis (Bypass SOP) demi 'kecepatan' membuka pintu lebar bagi fraud dan kegagalan sistem.",
            pushSummaryEn = "Bypassing dual-authorization checks for 'speed' exposes the organization to massive fraud and operational chaos.",
            pushSummaryJa = "迅速さを口実に多重承認（SOP）を省略すると、不正や重大事故を招く危険があります。",
            fullMessage = "Prinsip 'Four-Eyes Principle' (pemeriksaan ganda) dan prosedur otorisasi berjenjang dibuat bukan untuk memperlambat pekerjaan, melainkan melindungi karyawan dan perusahaan dari kesalahan fatal. Membagi-bagikan password akun operasional atau menandatangani dokumen kosong adalah pelanggaran fatal terhadap pengendalian internal.",
            fullMessageEn = "The Four-Eyes principle and tiered approval workflows are calibrated to shield staff from fatal liability. Sharing operational passwords, lending digital signatures, or pre-signing blank approval vouchers destroys internal controls.",
            fullMessageJa = "多重チェック体制はミスや不正から従業員自身を守るために設計されています。ID・パスワードの使い回しや白紙への事前サインは厳禁です。",
            goldenRules = listOf(
                "Jangan pernah membagikan kata sandi (password) atau token otentikasi kepada rekan sekerja.",
                "Pastikan setiap transaksi memiliki persetujuan pejabat berwenang sebelum dieksekusi.",
                "Tolak menandatangani formulir kosong atau memberi persetujuan 'titipan' tanpa verifikasi."
            ),
            goldenRulesEn = listOf(
                "Never share your enterprise passwords, OTP tokens, or credential keys with colleagues.",
                "Ensure every transaction receives formal authorization prior to execution.",
                "Never sign blank forms or blind approvals without physically inspecting documentation."
            ),
            goldenRulesJa = listOf(
                "業務システムのパスワードや認証トークンを他人に共有しないでください。",
                "必ず正規の承認ルートを経てから取引や手続きを実行してください。",
                "内容未確認の書類や白紙の申請書にサインしないでください。"
            ),
            consequences = "Pencabutan wewenang sistem, penurunan pangkat (demosi), pertanggungjawaban ganti rugi selisih saldo, dan teguran tertulis.",
            consequencesEn = "System access revocation, formal demotion, full financial liability for shortfalls, and disciplinary proceedings.",
            consequencesJa = "システム権限の剥奪、降格処分、過失による損失の賠償責任、および厳しい懲戒。",
            iconRes = R.drawable.compliance_shield
        ),

        // Day 7: Cyber Fraud & Phishing
        DailyComplianceMessage(
            id = 7,
            categoryBadge = "💻 CYBER INTEGRITY & ANTI-PHISHING",
            categoryBadgeEn = "💻 CYBER FRAUD & PHISHING",
            categoryBadgeJa = "💻 サイバー詐欺・標的型攻撃対策",
            title = "Waspadai Phishing Email & Rekayasa Sosial!",
            titleEn = "Beware of Email Phishing & Social Engineering!",
            titleJa = "フィッシング詐欺と標的型メール攻撃に警戒せよ！",
            pushSummary = "Jangan mudah percaya email mendesak yang meminta transfer darurat atau klik tautan asing. Periksa keaslian pengirim!",
            pushSummaryEn = "Do not trust urgent emails requesting emergency funds or unfamiliar links. Verify sender identities carefully!",
            pushSummaryJa = "緊急の送金を装うメールや不審なリンクを安易に開かないでください！",
            fullMessage = "Serangan Business Email Compromise (BEC) menargetkan bagian keuangan dengan menyamar sebagai direksi atau pimpinan, menuntut transfer dana mendesak dengan rekening penampung baru. Selalu lakukan verifikasi dua arah (two-way confirmation) melalui panggilan resmi sebelum mengubah nomor rekening pembayaran vendor.",
            fullMessageEn = "Business Email Compromise attacks impersonate executives demanding urgent wire transfers to alternate bank accounts. Always enforce two-way out-of-band verification before altering vendor banking details.",
            fullMessageJa = "役員や取引先になりすまして至急の送金を要求する詐欺メールが多発しています。口座情報の変更要請には必ず電話等で直接確認を行ってください。",
            goldenRules = listOf(
                "Cek domain pengirim email dengan teliti: waspadai huruf yang mirip atau tipuan alamat.",
                "Lakukan konfirmasi telepon langsung jika ada perubahan nomor rekening vendor secara tiba-tiba.",
                "Jangan mengklik lampiran mencurigakan (.exe, .scr, zip mencurigakan) di komputer kantor."
            ),
            goldenRulesEn = listOf(
                "Scrutinize sender domains carefully: check for spoofed letters and typosquatting.",
                "Execute an independent voice call verification whenever vendor banking details change.",
                "Never click unvetted attachments or macro-enabled spreadsheets from unfamiliar senders."
            ),
            goldenRulesJa = listOf(
                "送信者のメールアドレスのドメインが本物であるか注意深く確認してください。",
                "取引先の振込先変更は、必ず正規の連絡先へ電話で再確認してください。",
                "不審な添付ファイルやURLを会社の端末で開かないでください。"
            ),
            consequences = "Kerugian finansial masif, penghentian operasional akibat malware/ransomware, investigasi forensik, dan tindakan disipliner berat.",
            consequencesEn = "Catastrophic corporate loss, ransomware system lockout, digital forensics inquiry, and immediate liability sanctions.",
            consequencesJa = "甚大な金銭的被害、ランサムウェア感染による業務停止、および過失に対する重い処分。",
            iconRes = R.drawable.bonus_corruptor
        )
    )
}
