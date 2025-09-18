package com.amiwatch.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.LayoutAnimationController
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.amiwatch.R
import com.amiwatch.connections.anilist.ProfileViewModel
import com.amiwatch.connections.anilist.api.Query
import com.amiwatch.databinding.FragmentProfileBinding
import com.amiwatch.media.Author
import com.amiwatch.media.AuthorAdapter
import com.amiwatch.media.Character
import com.amiwatch.media.CharacterAdapter
import com.amiwatch.media.Media
import com.amiwatch.media.MediaAdaptor
import com.amiwatch.openOrCopyAnilistLink
import com.amiwatch.setBaseline
import com.amiwatch.setSlideIn
import com.amiwatch.setSlideUp
import com.amiwatch.util.AniMarkdown.Companion.getFullAniHTML
import eu.kanade.tachiyomi.util.system.getSerializableCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class ProfileFragment : Fragment() {
    lateinit var binding: FragmentProfileBinding
    private lateinit var activity: ProfileActivity
    private lateinit var user: Query.UserProfile
    private val favStaff = arrayListOf<Author>()
    private val favCharacter = arrayListOf<Character>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    val model: ProfileViewModel by activityViewModels()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        activity = requireActivity() as ProfileActivity

        binding.root.setBaseline(activity.navBar)

        user = arguments?.getSerializableCompat<Query.UserProfile>("user") as Query.UserProfile
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            model.setData(user.id)
        }
        binding.profileUserBio.settings.loadWithOverviewMode = true
        binding.profileUserBio.settings.useWideViewPort = true
        binding.profileUserBio.setInitialScale(1)
        val styledHtml = getFullAniHTML(
            user.about ?: "",
            ContextCompat.getColor(activity, R.color.bg_opp)
        )
        binding.profileUserBio.loadDataWithBaseURL(
            null,
            styledHtml,
            "text/html; charset=utf-8",
            "UTF-8",
            null
        )
        binding.profileUserBio.setBackgroundColor(
            ContextCompat.getColor(
                activity,
                android.R.color.transparent
            )
        )
        binding.profileUserBio.setLayerType(View.LAYER_TYPE_HARDWARE, null)
        binding.profileUserBio.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                binding.profileUserBio.setBackgroundColor(
                    ContextCompat.getColor(
                        activity,
                        android.R.color.transparent
                    )
                )
            }

            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                openOrCopyAnilistLink(request?.url.toString())
                return true
            }
        }

        binding.userInfoContainer.isVisible = user.about != null


        binding.statsEpisodesWatched.text = user.statistics.anime.episodesWatched.toString()
        binding.statsDaysWatched.text =
            (user.statistics.anime.minutesWatched / (24 * 60)).toString()
        binding.statsAnimeMeanScore.text = user.statistics.anime.meanScore.toString()
        binding.statsChaptersRead.text = user.statistics.manga.chaptersRead.toString()
        binding.statsVolumeRead.text = (user.statistics.manga.volumesRead).toString()
        binding.statsMangaMeanScore.text = user.statistics.manga.meanScore.toString()
        initRecyclerView(
            model.getAnimeFav(),
            binding.profileFavAnimeContainer,
            binding.profileFavAnimeRecyclerView,
            binding.profileFavAnimeProgressBar,
            binding.profileFavAnime
        )

        initRecyclerView(
            model.getMangaFav(),
            binding.profileFavMangaContainer,
            binding.profileFavMangaRecyclerView,
            binding.profileFavMangaProgressBar,
            binding.profileFavManga
        )

        user.favourites?.characters?.nodes?.forEach { i ->
            favCharacter.add(Character(i.id, i.name.full, i.image.large, i.image.large, "", true))
        }

        user.favourites?.staff?.nodes?.forEach { i ->
            favStaff.add(Author(i.id, i.name.full, i.image.large, ""))
        }

        setFavPeople()
    }

    override fun onResume() {
        super.onResume()
        if (this::binding.isInitialized) {
            binding.root.requestLayout()
            binding.root.setBaseline(activity.navBar)
        }
    }

    private fun setFavPeople() {
        if (favStaff.isEmpty()) {
            binding.profileFavStaffContainer.visibility = View.GONE
        } else {
            binding.profileFavStaffRecycler.adapter = AuthorAdapter(favStaff)
            binding.profileFavStaffRecycler.layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.HORIZONTAL, false
            )
            binding.profileFavStaffRecycler.layoutAnimation =
                LayoutAnimationController(setSlideIn(), 0.25f)
        }

        if (favCharacter.isEmpty()) {
            binding.profileFavCharactersContainer.visibility = View.GONE
        } else {
            binding.profileFavCharactersRecycler.adapter = CharacterAdapter(favCharacter)
            binding.profileFavCharactersRecycler.layoutManager = LinearLayoutManager(
                activity, LinearLayoutManager.HORIZONTAL, false
            )
            binding.profileFavCharactersRecycler.layoutAnimation =
                LayoutAnimationController(setSlideIn(), 0.25f)
        }
    }

    private fun initRecyclerView(
        mode: LiveData<ArrayList<Media>>,
        container: View,
        recyclerView: RecyclerView,
        progress: View,
        title: View
    ) {
        container.visibility = View.VISIBLE
        progress.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        title.visibility = View.INVISIBLE

        mode.observe(viewLifecycleOwner) {
            recyclerView.visibility = View.GONE
            if (it != null) {
                if (it.isNotEmpty()) {
                    recyclerView.adapter = MediaAdaptor(0, it, activity, fav = true)
                    recyclerView.layoutManager = LinearLayoutManager(
                        activity,
                        LinearLayoutManager.HORIZONTAL,
                        false
                    )
                    recyclerView.visibility = View.VISIBLE
                    recyclerView.layoutAnimation =
                        LayoutAnimationController(setSlideIn(), 0.25f)

                } else {
                    container.visibility = View.GONE
                }
                title.visibility = View.VISIBLE
                title.startAnimation(setSlideUp())
                progress.visibility = View.GONE
            }
        }
    }

    companion object {
        fun newInstance(query: Query.UserProfile): ProfileFragment {
            val args = Bundle().apply {
                putSerializable("user", query)
            }
            return ProfileFragment().apply {
                arguments = args
            }
        }
    }

}
