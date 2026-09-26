package com.readerlauncher.app.reader

import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.readerlauncher.app.databinding.FragmentPdfReaderBinding

class PdfReaderFragment : Fragment() {

    private var _binding: FragmentPdfReaderBinding? = null
    private val binding get() = _binding!!

    private var renderer: PdfRenderer? = null
    private var fileDescriptor: ParcelFileDescriptor? = null
    private var onPageChanged: ((page: Int, total: Int) -> Unit)? = null
    private var pendingOpen: Pair<ParcelFileDescriptor, Int>? = null

    fun setOnPageChangedListener(listener: (page: Int, total: Int) -> Unit) {
        onPageChanged = listener
    }

    fun open(pfd: ParcelFileDescriptor, startPage: Int) {
        if (_binding == null) {
            pendingOpen = pfd to startPage
            return
        }
        fileDescriptor = pfd
        val r = PdfRenderer(pfd)
        renderer = r
        binding.pdfPager.adapter = PdfPageAdapter(r)
        binding.pdfPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                onPageChanged?.invoke(position, r.pageCount)
            }
        })
        binding.pdfPager.setCurrentItem(startPage.coerceIn(0, (r.pageCount - 1).coerceAtLeast(0)), false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPdfReaderBinding.inflate(inflater, container, false)
        pendingOpen?.let { (pfd, page) -> open(pfd, page) }
        pendingOpen = null
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        renderer?.close()
        fileDescriptor?.close()
        _binding = null
    }
}
