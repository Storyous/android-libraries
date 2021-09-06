package com.storyous.commonutils.extensions

import android.widget.Button
import androidx.appcompat.app.AlertDialog

val AlertDialog.positiveButton: Button? get() = getButton(AlertDialog.BUTTON_POSITIVE)

val AlertDialog.negativeButton: Button? get() = getButton(AlertDialog.BUTTON_NEGATIVE)

val AlertDialog.neutralButton: Button? get() = getButton(AlertDialog.BUTTON_NEUTRAL)
