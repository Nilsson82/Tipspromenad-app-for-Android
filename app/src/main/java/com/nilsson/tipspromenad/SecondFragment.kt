package com.nilsson.tipspromenad

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.text.Html
import android.text.method.LinkMovementMethod
import androidx.navigation.fragment.findNavController
import com.nilsson.tipspromenad.databinding.FragmentSecondBinding


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private var _binding: FragmentSecondBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonBack.setOnClickListener { findNavController().popBackStack() }

        // Process app_info with HTML
        binding.textviewQuizInfo.apply {
            text = Html.fromHtml(getString(R.string.offline_info), Html.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
        }
        val version = requireContext().packageManager.getPackageInfo(requireContext().packageName, 0).versionName
        binding.textviewQuizTitle.text = "${getString(R.string.app_name)} $version"

        binding.textviewQuizFooter.apply {
            text = Html.fromHtml(getString(R.string.dev_info), Html.FROM_HTML_MODE_LEGACY)
            movementMethod = LinkMovementMethod.getInstance()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
