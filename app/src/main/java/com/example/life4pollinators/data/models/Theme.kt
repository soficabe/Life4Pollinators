package com.example.life4pollinators.data.models

import androidx.annotation.StringRes
import com.example.life4pollinators.R

enum class Theme(@StringRes val themeName: Int) {
    Light(R.string.light),
    Dark(R.string.dark),
    System(R.string.system)
}