package com.example.data

import androidx.annotation.DrawableRes
import com.example.R
import kotlin.math.abs

object AvatarHelper {
    val AVATAR_RES_LIST = listOf(
        R.drawable.ic_avatar_1,
        R.drawable.ic_avatar_2,
        R.drawable.ic_avatar_3,
        R.drawable.ic_avatar_4,
        R.drawable.ic_avatar_5,
        R.drawable.ic_avatar_6,
        R.drawable.ic_avatar_7,
        R.drawable.ic_avatar_8,
        R.drawable.ic_avatar_9,
        R.drawable.ic_avatar_10
    )

    val AVATAR_TITLES = listOf(
        "Ethics Inspector",
        "Lead Investigator",
        "Senior Auditor",
        "AML Specialist",
        "Chief Compliance Officer",
        "Forensic Analyst",
        "Risk Governance Director",
        "Data Privacy Officer",
        "Whistleblower Advocate",
        "Compliance Grandmaster"
    )

    @DrawableRes
    fun getAvatarRes(avatarId: Int, username: String = ""): Int {
        if (avatarId in 1..AVATAR_RES_LIST.size) {
            return AVATAR_RES_LIST[avatarId - 1]
        }
        if (username.isNotEmpty()) {
            val index = abs(username.hashCode()) % AVATAR_RES_LIST.size
            return AVATAR_RES_LIST[index]
        }
        return AVATAR_RES_LIST[0]
    }

    fun getAvatarTitle(avatarId: Int, username: String = ""): String {
        if (avatarId in 1..AVATAR_TITLES.size) {
            return AVATAR_TITLES[avatarId - 1]
        }
        if (username.isNotEmpty()) {
            val index = abs(username.hashCode()) % AVATAR_TITLES.size
            return AVATAR_TITLES[index]
        }
        return AVATAR_TITLES[0]
    }
}
