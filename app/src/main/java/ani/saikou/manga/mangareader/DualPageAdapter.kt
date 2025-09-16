package ani.saikou.manga.mangareader

import android.animation.ObjectAnimator
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ani.saikou.R
import ani.saikou.databinding.ItemDualPageBinding
import ani.saikou.manga.MangaChapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.request.target.CustomViewTarget
import com.bumptech.glide.request.target.Target
import com.bumptech.glide.request.transition.Transition
import com.davemorrissey.labs.subscaleview.ImageSource
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView
import java.io.File

class DualPageAdapter(
    activity: MangaReaderActivity,
    chapter: MangaChapter
) : BaseImageAdapter(activity, chapter) {

    private val pages = chapter.dualPages()
    override fun loadImage(position: Int, parent: View): Boolean {

        val imageView1 = parent.findViewById<SubsamplingScaleImageView>(R.id.imgProgImageNoGestures1) ?: return false
        val progress1 = parent.findViewById<View>(R.id.imgProgProgress1) ?: return false

        val imageView2 = parent.findViewById<SubsamplingScaleImageView>(R.id.imgProgImageNoGestures2) ?: return false
        val progress2 = parent.findViewById<View>(R.id.imgProgProgress2) ?: return false

        fun apply(position: Int, imageView: SubsamplingScaleImageView, progress: View): Boolean {
            imageView.recycle()
            imageView.visibility = View.GONE


            val link = images.getOrNull(position)?.url ?: return true
            val trans = activity.getTransformation(images[position])

            if (link.url.isEmpty()) return true
            Glide.with(imageView).download(GlideUrl(link.url) { link.headers })
                .override(Target.SIZE_ORIGINAL)
                .apply {
                    val target = object : CustomViewTarget<SubsamplingScaleImageView, File>(imageView) {
                        override fun onLoadFailed(errorDrawable: Drawable?) {
                            progress.visibility = View.GONE
                        }

                        override fun onResourceCleared(placeholder: Drawable?) {}

                        override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                            imageView.visibility = View.VISIBLE
                            view.setImage(ImageSource.uri(Uri.fromFile(resource)))
                            ObjectAnimator.ofFloat(parent, "alpha", 0f, 1f)
                                .setDuration((400 * uiSettings.animationSpeed).toLong())
                                .start()
                            progress.visibility = View.GONE
                        }
                    }
                    if (trans != null)
                        transform(File::class.java, trans).into(target)
                    else
                        into(target)
                }
            return true
        }

        return apply(position * 2, imageView1, progress1) && apply(position * 2 + 1, imageView2, progress2)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DualImageViewHolder {
        val binding = ItemDualPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DualImageViewHolder(binding)

    }

    inner class DualImageViewHolder(binding: ItemDualPageBinding) : RecyclerView.ViewHolder(binding.root)


    override fun getItemCount(): Int = pages.size
}