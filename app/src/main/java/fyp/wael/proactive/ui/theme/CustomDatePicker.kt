package fyp.wael.proactive.ui.theme

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.os.Build
import android.util.AttributeSet
import android.widget.DatePicker


class CustomDatePicker : DatePicker {
    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    ) {
        init()
    }

    private fun init() {
        // Set the color of the date numbers
        setNumberTextColor(Color.RED)
    }

    fun setNumberTextColor(color: Int) {
        try {
            // Use reflection to access the mDelegate field, which holds the underlying DatePickerDelegate instance
            val delegateField = DatePicker::class.java.getDeclaredField("mDelegate")
            delegateField.isAccessible = true
            val delegate = delegateField[this]

            // Use reflection to access the mDayPicker field, which holds the DayPicker instance
            val dayPickerField = delegate.javaClass.getDeclaredField("mDayPicker")
            dayPickerField.isAccessible = true
            val dayPicker = dayPickerField[delegate]

            // Use reflection to access the mDayNumbersPaint field, which holds the Paint instance used for drawing the date numbers
            val dayNumbersPaintField = dayPicker.javaClass.getDeclaredField("mDayNumbersPaint")
            dayNumbersPaintField.isAccessible = true
            val dayNumbersPaint = dayNumbersPaintField[dayPicker]

            // Set the color of the Paint instance
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                (dayNumbersPaint as Paint).color = ColorStateList.valueOf(color).defaultColor
            } else {
                (dayNumbersPaint as Paint).color = color
            }

            // Invalidate the DatePicker to reflect the changes
            invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
