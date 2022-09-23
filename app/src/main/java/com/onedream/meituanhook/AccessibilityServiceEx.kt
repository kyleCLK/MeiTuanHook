package com.onedream.meituanhook

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.content.Context
import android.content.Intent
import android.graphics.Path
import android.provider.Settings
import android.util.Log

fun AccessibilityService.gesture(x: Float, y: Float, xOffset : Float, yOffset :Float, duration : Long) {
    val builder = GestureDescription.Builder()
    val path = Path()
    path.moveTo(x, y)
    path.lineTo(x + xOffset, y + yOffset)
    builder.addStroke(GestureDescription.StrokeDescription(path, 0, duration))//也就是 1ms 以内完成从 (x, y) 坐标移动到 (x, y) 坐标。
    val gesture = builder.build()
    this.dispatchGesture(
        gesture,
        object : AccessibilityService.GestureResultCallback() {
            override fun onCancelled(gestureDescription: GestureDescription) {
                super.onCancelled(gestureDescription)
            }

            override fun onCompleted(gestureDescription: GestureDescription) {
                super.onCompleted(gestureDescription)
            }
        }, null
    )
}

fun AccessibilityService.click(x: Float, y: Float, duration : Long) {
    Log.d("ATU", "click: ($x, $y)")
    this.gesture( x, y, 0.0f, 0.0f, duration)
}

fun AccessibilityService.swipe(x: Float, y: Float, xOffset : Float, yOffset :Float, duration : Long) {
    Log.d("ATU", "swipe: ($x, $y) to (${x + xOffset}, ${y + yOffset})")
    this.gesture(x, y, xOffset, yOffset, duration)
}


fun Context.jumpToAccessibilitySetting() {
    val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
    startActivity(intent)
}
