package com.amiwatch.settings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.amiwatch.databinding.ItemDeveloperBinding
import com.amiwatch.loadData
import com.amiwatch.loadImage
import com.amiwatch.openLinkInBrowser
import com.amiwatch.setAnimation

class DevelopersAdapter(private val developers: Array<Developer>) :
    RecyclerView.Adapter<DevelopersAdapter.DeveloperViewHolder>() {
    private val uiSettings = loadData<UserInterfaceSettings>("ui_settings") ?: UserInterfaceSettings()

    inner class DeveloperViewHolder(val binding: ItemDeveloperBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                openLinkInBrowser(developers[bindingAdapterPosition].url)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DeveloperViewHolder {
        return DeveloperViewHolder(ItemDeveloperBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    }

    override fun onBindViewHolder(holder: DeveloperViewHolder, position: Int) {
        val b = holder.binding
        setAnimation(b.root.context, b.root, uiSettings)
        val dev = developers[position]
        b.devName.text = dev.name
        b.devProfile.loadImage(dev.pfp)
        b.devRole.text = dev.role
    }

    override fun getItemCount(): Int = developers.size
}
