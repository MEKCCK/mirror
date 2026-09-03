// SPDX-FileCopyrightText: Copyright 2025 Eden Emulator Project
// SPDX-License-Identifier: GPL-3.0-or-later

package org.yuzu.yuzu_emu.network

import android.content.Context
import org.yuzu.yuzu_emu.R
import org.yuzu.yuzu_emu.features.settings.model.StringSetting
import java.net.Inet6Address
import java.net.InetAddress

object NetDataValidators {
    // IPv4 literal
    private val IPV4_ADDRESS_REGEX = Regex(
        "^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$"
    )

    // Hostname / domain (single label LAN names and fully qualified domains).
    // ENet resolves hostnames via getaddrinfo on the native side.
    private val HOSTNAME_REGEX = Regex(
        "^[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(\\.[a-zA-Z0-9]([a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*$"
    )
    fun roomName(s: String): Boolean {
        return s.length in 3..20
    }

    fun notEmpty(s: String): Boolean {
        return s.isNotEmpty()
    }

    fun token(s: String?): Boolean {
        return s?.matches(Regex("[a-z]{48}")) == true
    }

    fun token(): Boolean {
        return token(StringSetting.WEB_TOKEN.getString())
    }

    fun roomVisibility(s: String, context: Context): Boolean {
        if (s != context.getString(R.string.multiplayer_public_visibility)) {
            return true
        }

        return token()
    }

    fun ipAddress(s: String): Boolean {
        val input = s.trim()
        if (input.isEmpty()) {
            return false
        }

        // IPv4 literal
        if (IPV4_ADDRESS_REGEX.matches(input)) {
            return true
        }

        // Domain / hostname — ENet resolves these on the native side, so
        // allow them here without a blocking DNS lookup on the UI thread.
        if (HOSTNAME_REGEX.matches(input)) {
            return true
        }

        // IPv6 literal (validated locally, no DNS lookup for a literal)
        return try {
            val address = InetAddress.getByName(input)
            address is Inet6Address
        } catch (_: Exception) {
            false
        }
    }

    fun username(s: String?): Boolean {
        return s?.matches(Regex("^[ a-zA-Z0-9._-]{4,20}$")) == true
    }

    fun username(): Boolean {
        return username(StringSetting.WEB_USERNAME.getString())
    }

    fun port(s: String): Boolean {
        return s.toIntOrNull() in 1..65535
    }
}
