package com.amiwatch.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.documentfile.provider.DocumentFile
import androidx.fragment.app.Fragment
import com.amiwatch.R
import com.amiwatch.connections.anilist.Anilist
import com.amiwatch.databinding.DialogUserAgentBinding
import com.amiwatch.databinding.FragmentLoginBinding
import com.amiwatch.openLinkInBrowser
import com.amiwatch.settings.saving.internal.PreferenceKeystore
import com.amiwatch.settings.saving.internal.PreferencePackager
import com.amiwatch.toast
import com.amiwatch.util.Logger
import com.amiwatch.util.customAlertDialog

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.loginButton.setOnClickListener { Anilist.loginIntent(requireActivity()) }
        binding.loginDiscord.setOnClickListener { openLinkInBrowser(getString(R.string.discord)) }
        binding.loginGithub.setOnClickListener { openLinkInBrowser(getString(R.string.github)) }
        binding.loginTelegram.setOnClickListener { openLinkInBrowser(getString(R.string.telegram)) }

        val openDocumentLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                if (uri != null) {
                    try {
                        val jsonString =
                            requireActivity().contentResolver.openInputStream(uri)?.readBytes()
                                ?: throw Exception("Error reading file")
                        val name =
                            DocumentFile.fromSingleUri(requireActivity(), uri)?.name ?: "settings"
                        //.sani is encrypted, .ani is not
                        if (name.endsWith(".sani")) {
                            passwordAlertDialog { password ->
                                if (password != null) {
                                    val salt = jsonString.copyOfRange(0, 16)
                                    val encrypted = jsonString.copyOfRange(16, jsonString.size)
                                    val decryptedJson = try {
                                        PreferenceKeystore.decryptWithPassword(
                                            password,
                                            encrypted,
                                            salt
                                        )
                                    } catch (e: Exception) {
                                        toast("Incorrect password")
                                        return@passwordAlertDialog
                                    }
                                    if (PreferencePackager.unpack(decryptedJson))
                                        restartApp()
                                } else {
                                    toast("Password cannot be empty")
                                }
                            }
                        } else if (name.endsWith(".ani")) {
                            val decryptedJson = jsonString.toString(Charsets.UTF_8)
                            if (PreferencePackager.unpack(decryptedJson))
                                restartApp()
                        } else {
                            toast("Invalid file type")
                        }
                    } catch (e: Exception) {
                        Logger.log(e)
                        toast("Error importing settings")
                    }
                }
            }

        binding.importSettingsButton.setOnClickListener {
            openDocumentLauncher.launch(arrayOf("*/*"))
        }
    }

    private fun passwordAlertDialog(callback: (CharArray?) -> Unit) {
        val password = CharArray(16).apply { fill('0') }

        // Inflate the dialog layout
        val dialogView = DialogUserAgentBinding.inflate(layoutInflater).apply {
            userAgentTextBox.hint = "Password"
            subtitle.visibility = View.VISIBLE
            subtitle.text = getString(R.string.enter_password_to_decrypt_file)
        }

        requireActivity().customAlertDialog().apply {
            setTitle("Enter Password")
            setCustomView(dialogView.root)
            setPosButton(R.string.ok) {
                val editText = dialogView.userAgentTextBox
                if (editText.text?.isNotBlank() == true) {
                    editText.text?.toString()?.trim()?.toCharArray(password)
                    callback(password)
                } else {
                    toast("Password cannot be empty")
                }
            }
            setNegButton(R.string.cancel) {
                password.fill('0')
                callback(null)
            }
        }.show()


    }

    private fun restartApp() {
        val intent = Intent(requireActivity(), requireActivity().javaClass)
        requireActivity().finish()
        startActivity(intent)
    }

}
