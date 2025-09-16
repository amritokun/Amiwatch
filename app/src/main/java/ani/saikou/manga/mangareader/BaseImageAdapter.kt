package ani.saikou.manga.mangareader

import android.annotation.SuppressLint
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.view.GestureDetectorCompat
import androidx.core.view.updateLayoutParams
import androidx.recyclerview.widget.RecyclerView
import ani.saikou.GesturesListener
import ani.saikou.R
import ani.saikou.manga.MangaChapter
import ani.saikou.px
import ani.saikou.settings.CurrentReaderSettings
import com.alexvasilkov.gestures.views.GestureFrameLayout

abstract class BaseImageAdapter(
    val activity: MangaReaderActivity,
    chapter: MangaChapter
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    val settings = activity.settings.default
    val uiSettings = activity.uiSettings
    val images = chapter.images!!

    @SuppressLint("ClickableViewAccessibility")
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val view = holder.itemView as GestureFrameLayout
        view.controller.also {
            if (settings.layout == CurrentReaderSettings.Layouts.PAGED) {
                it.settings.enableGestures()
            }
            it.settings.isRotationEnabled = settings.rotation
        }
        if (settings.layout != CurrentReaderSettings.Layouts.PAGED) {
            if (settings.padding) {
                when (settings.direction) {
                    CurrentReaderSettings.Directions.TOP_TO_BOTTOM -> view.setPadding(0, 0, 0, 16f.px)
                    CurrentReaderSettings.Directions.LEFT_TO_RIGHT -> view.setPadding(0, 0, 16f.px, 0)
                    CurrentReaderSettings.Directions.BOTTOM_TO_TOP -> view.setPadding(0, 16f.px, 0, 0)
                    CurrentReaderSettings.Directions.RIGHT_TO_LEFT -> view.setPadding(16f.px, 0, 0, 0)
                }
            }
            view.updateLayoutParams {
                if (settings.direction != CurrentReaderSettings.Directions.LEFT_TO_RIGHT && settings.direction != CurrentReaderSettings.Directions.RIGHT_TO_LEFT) {
                    width = ViewGroup.LayoutParams.MATCH_PARENT
                    height = 480f.px
                } else {
                    width = 480f.px
                    height = ViewGroup.LayoutParams.MATCH_PARENT
                }
            }
        } else {
            val detector = GestureDetectorCompat(view.context, object : GesturesListener() {
                override fun onSingleClick(event: MotionEvent?) = activity.handleController()
            })
            view.findViewById<View>(R.id.imgProgCover).apply {
                setOnTouchListener { _, event ->
                    detector.onTouchEvent(event)
                    false
                }
                setOnLongClickListener {
                    val pos = holder.bindingAdapterPosition
                    val image = images.getOrNull(pos) ?: return@setOnLongClickListener false
                    activity.onImageLongClicked(pos,image){ dialog ->
                        loadImage(pos, view)
                        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
                        dialog.dismiss()
                    }
                }
            }
        }
        loadImage(holder.bindingAdapterPosition, view)
    }

    abstract fun loadImage(position: Int, parent: View): Boolean
}