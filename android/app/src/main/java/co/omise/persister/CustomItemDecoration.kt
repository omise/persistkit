package co.omise.persister

import android.content.Context
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView

class CustomItemDecoration(context: Context) : RecyclerView.ItemDecoration() {
    private val divider: Drawable? = AppCompatResources.getDrawable(context, R.drawable.dashboard_item_divider)

    override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDrawOver(c, parent, state)

        val divider = divider ?: return

        val childCount = parent.childCount

        loop@ for (i in 0..childCount.minus(1)) {
            val view = parent.getChildAt(i)
            val pos = parent.getChildAdapterPosition(view)

            if (pos == RecyclerView.NO_POSITION) {
                continue@loop
            }

            val params = view.layoutParams as RecyclerView.LayoutParams

            val left = 0
            val right = parent.width
            val top = view.bottom + params.bottomMargin
            val bottom = top + divider.intrinsicHeight
            divider.setBounds(left, top, right, bottom)
            divider.draw(c)
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)

        val divider = divider ?: return

        val pos = parent.getChildAdapterPosition(view)
        if (pos == RecyclerView.NO_POSITION) {
            return
        }

        outRect.bottom = divider.intrinsicHeight
    }
}