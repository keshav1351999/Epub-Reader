package com.example.epub

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mertakdut.BookSection
import com.github.mertakdut.exception.OutOfPagesException
import com.github.mertakdut.exception.ReadingException
import com.google.android.material.floatingactionbutton.FloatingActionButton
import nl.siegmann.epublib.domain.Author
import nl.siegmann.epublib.domain.Book
import nl.siegmann.epublib.domain.TOCReference
import nl.siegmann.epublib.epub.EpubReader
import java.io.*
import java.util.*

class MainActivity : AppCompatActivity() {

    var recyclerView: RecyclerView? = null
    var adapter: RecyclerView.Adapter<*>? = null
    var layoutManager: RecyclerView.LayoutManager? = null
    var xHTMLList: ArrayList<String>? = null
    var textList: ArrayList<String>? = null
    var fBtnSpeak: FloatingActionButton? = null
    var fBtnNext: FloatingActionButton? = null
    var textToSpeech: TextToSpeech? = null
    var counter = 0

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
               storagePermissionGranted()
            } else {
                Log.i("Permission: ", "Denied")
            }
        }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

/*        textView = findViewById(R.id.txt_view);
        imageView = findViewById(R.id.iv_cover);*/
        recyclerView = findViewById(R.id.recyclerView)
        xHTMLList = ArrayList()
        textList = ArrayList()
        adapter = WebViewAdapter(xHTMLList!!)
        layoutManager = LinearLayoutManager(this)
        recyclerView?.adapter = adapter
        recyclerView?.layoutManager = layoutManager
        fBtnSpeak = findViewById(R.id.fBtnSpeak)
        fBtnNext = findViewById(R.id.fBtnNext)

        checkStoragePermission()






/*        textView.setText(""+author + "\n"+title)*/
        //System.out.println("book title:"+(titles.isEmpty() ? "book has no title" :titles.get(0)));
        textToSpeech = TextToSpeech(
            applicationContext
        ) { i ->
            if (i == TextToSpeech.SUCCESS) {
                val lang = textToSpeech!!.setLanguage(Locale.ENGLISH)
            }
        }
        fBtnSpeak?.setOnClickListener(View.OnClickListener {
            Log.d("fsdfsdf", "xHTMLList.get(0): " + textList!![counter])
            textToSpeech!!.speak(textList!![counter], TextToSpeech.QUEUE_FLUSH, null)
            recyclerView?.scrollToPosition(counter)
        })

        fBtnNext?.setOnClickListener{
            counter++
            recyclerView?.scrollToPosition(counter)
        }
    }

    private fun checkStoragePermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            storagePermissionGranted()
        }
        else{
            requestPermissionLauncher.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }

    private fun storagePermissionGranted() {
        val fl = File(Environment.getExternalStorageDirectory(), "Epub")
        val actualFile = File(fl, "TheSilverChair.epub")
        // read epub file
        val epubReader = EpubReader()
        var book: Book? = null
        try {
            book = epubReader.readEpub(FileInputStream(actualFile))
            val coverImage = BitmapFactory.decodeStream(
                book.getCoverImage()
                    .getInputStream()
            )

            val `is`: InputStream =
                book.getSpine().getSpineReferences().get(0).getResource().getInputStream()
            val reader = BufferedReader(InputStreamReader(`is`))
            val sb = StringBuilder()
            var line: String? = null
            while (reader.readLine().also { line = it } != null) {
                sb.append(
                    """
                        $line
                        
                        """.trimIndent()
                )
            }
            `is`.close()
            Log.e("hdjsdfg", "dsgsdg$sb")
            ReadEpub(actualFile)
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d("hfhgjygj", "onCreate: $e")
        } catch (e: ReadingException) {
            e.printStackTrace()
            Log.d("hfhgjygj", "onCreate: $e")
        } catch (e: OutOfPagesException) {
            e.printStackTrace()
            Log.d("hfhgjygj", "onCreate: $e")
        }

        // print the first title
        val titles: List<String> = book?.metadata!!.titles
        val authors: List<Author> = book.metadata!!.authors
        val title = "book title:" + if (titles.isEmpty()) "book has no title" else titles[0]
        val author =
            "book author:" + if (authors.isEmpty()) "book has no title" else authors[0].toString()

    }

    @Throws(ReadingException::class, OutOfPagesException::class)
    private fun ReadEpub(actualFile: File) {
        val reader = com.github.mertakdut.Reader()
        reader.setMaxContentPerSection(1000) // Max string length for the current page.
        reader.setIsIncludingTextContent(true) // Optional, to return the tags-excluded version.
        reader.setFullContent(actualFile.getAbsolutePath()); // Must call before readSection.


        for (i in 1..50) {
            val bookSection: BookSection = reader.readSection(i)
            val sectionContent: String = bookSection.getSectionContent() // Returns content as html.
            val sectionTextContent: String =
                bookSection.getSectionTextContent() // Excludes html tags.
            xHTMLList!!.add(sectionContent)
            textList!!.add(sectionTextContent)
            Log.d("sdfsdf", "sectionContent: $sectionContent")
            Log.d("sdfsdf", "sectionTextContent: $sectionTextContent")
        }
        adapter!!.notifyDataSetChanged()
        //webView.loadData(sectionContent , "application/xhtml+xml; charset=UTF-8", null);
        //recyclerView.loadDataWithBaseURL(null, sectionContent, "application/xhtml+xml", "utf-8", null);
    }

    @Throws(IOException::class)
    fun getFileFromAssets(context: Context, fileName: String): File = File(context.cacheDir, fileName)
        .also {
            if (!it.exists()) {
                it.outputStream().use { cache ->
                    context.assets.open(fileName).use { inputStream ->
                        inputStream.copyTo(cache)
                    }
                }
            }
        }

    override fun onDestroy() {
        super.onDestroy()

        if(textToSpeech != null){
            textToSpeech?.shutdown();
        }
    }
}