package com.ljx.example

import android.content.Context
import android.content.res.TypedArray
import android.graphics.*
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.ljx.example.R

/**
 * @author MuBai
 * @time 25-01-22
 * 自定义下划线（虚线）
 * 暂时不支持lineSpacingMultiplier
 */
class DottedLineAppCompatTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatTextView(
    context,
    attrs,
    defStyleAttr
) {

//    private val mPaint by lazy { Paint() }
//    private val mRect: Rect by lazy { Rect() }
//
//    private var mLineColor = Color.RED
//    private var mLineStrokeWidth = 1f
//    private var mLineWidth = 30f
//    private var mLineGap = 15f

    init {
//        var a: TypedArray? = null
        try {
print("123333")
////            a = context.theme.obtainStyledAttributes(
////                attrs,
////                R.styleable.DottedLineAppCompatTextView,
////                0,
////                0
////            )
////            mLineColor = a.getColor(R.styleable.DottedLineAppCompatTextView_line_color, Color.RED)
////            mLineStrokeWidth =
////                a.getDimension(R.styleable.DottedLineAppCompatTextView_line_stroke_width, 1f)
////            mLineWidth =
////                a.getDimension(R.styleable.DottedLineAppCompatTextView_line_item_width, 30f)
////            mLineGap =
////                a.getDimension(R.styleable.DottedLineAppCompatTextView_line_item_gap, 15f)
//
        }
//        catch (e:Exception){
//            e.printStackTrace()
//        }
        finally {
//            a?.recycle()
        }

//        with(mPaint) {
//            isAntiAlias = true
//            color = mLineColor
//            strokeWidth = mLineStrokeWidth
//            style = Paint.Style.STROKE
//            pathEffect = DashPathEffect(floatArrayOf(mLineWidth, mLineGap), 0f)
//        }

//        try {
            ttttst0(609.0)
//        } catch (e: Exception) {
//            e.printStackTrace()
//        }//finally { }

    }

//    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
//        setMeasuredDimension(measuredWidth, measuredHeight + lineSpacingExtra.toInt())
//    }
//
//    override fun onDraw(canvas: Canvas) {
//        super.onDraw(canvas)
//        for (i in 0 until lineCount) {
//            getLineBounds(i, mRect)
//            val bottom: Float = if (i == lineCount - 1) {
//                mRect.bottom.toFloat() + lineSpacingExtra / 2
//            } else {
//                mRect.bottom.toFloat() - lineSpacingExtra / 2
//
//            }
//            canvas.drawLine(
//                mRect.left.toFloat(),
//                bottom,
//                mRect.right.toFloat(),
//                bottom,
//                mPaint
//            )
//        }
//    }

    fun ttttst0(i: Double): Boolean {
        val y = Math.sqrt(i)
        println("Square root of PI is: $y")
        return true
    }

}