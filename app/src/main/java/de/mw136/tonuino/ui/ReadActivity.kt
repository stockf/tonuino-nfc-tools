package de.mw136.tonuino.ui

import android.content.Intent
import android.nfc.Tag
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import de.mw136.tonuino.R
import de.mw136.tonuino.byteArrayToHex
import de.mw136.tonuino.nfc.NfcIntentActivity
import de.mw136.tonuino.nfc.TagData
import de.mw136.tonuino.nfc.readFromTag
import de.mw136.tonuino.nfc.tagIdAsString

@ExperimentalUnsignedTypes
class ReadActivity() : NfcIntentActivity() {
    override val TAG = "ReadActivity"

    lateinit var tag: Tag
    lateinit var tagData: TagData

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_read)

        tag = intent.getParcelableExtra<Tag>(PARCEL_TAG)
        tagData = intent.getParcelableExtra<TagData>(PARCEL_TAGDATA)

        displayTonuinoInfo(tag, tagData)
    }

    fun displayTonuinoInfo(tag: Tag, data: TagData) {
        val tagId = tagIdAsString(tag)
        Log.i("$TAG.displayTonuinoInfo", "Tag $tagId")
        supportActionBar?.title = getString(R.string.read_title, tagId)

        setText(R.id.cookie, data.cookie)
        setText(R.id.version, data.version)

        setText(R.id.folder, data.folder)
        findViewById<TextView>(R.id.folder_description).text =
            getString(R.string.edit_ext_folder_description, data.folder.toInt())

        setText(R.id.mode, data.mode)
        val edit_mode_array = resources.getStringArray(R.array.edit_mode_2_1)
        findViewById<TextView>(R.id.mode_description).apply {
            val mode = data.mode.toInt()
            if (mode in 1..edit_mode_array.size) {
                val str = edit_mode_array[mode - 1] + ": " +
                        resources.getStringArray(R.array.edit_mode_description_2_1)[mode - 1]
                text = str
            } else {
                Log.w("$TAG:displayTonuinoInfo", "Cannot display a description for unknown mode '$mode'.")
                text = resources.getString(R.string.edit_mode_unknown, mode)
            }
        }

        setText(R.id.special, data.special)
        findViewById<TextView>(R.id.special_description).visibility = View.GONE
        setText(R.id.special2, data.special2)
        findViewById<TextView>(R.id.special2_description).visibility = View.GONE
    }

    private fun setText(id: Int, bytes: UByteArray) {
        findViewById<TextView>(id).text = byteArrayToHex(bytes).joinToString(" ")
    }

    private fun setText(id: Int, byte: UByte) {
        findViewById<TextView>(id).text = byte.toString()
    }

    @Suppress("UNUSED_PARAMETER")
    fun gotoEditActivity(view: View) {
        val intent = Intent(this, EditActivity::class.java).apply {
            putExtra(PARCEL_TAG, tag)
            putExtra(PARCEL_TAGDATA, tagData)
        }
        startActivity(intent)
    }

    override fun onNfcTag(tag: Tag) {
        val bytes = readFromTag(tag)
        Log.d(TAG, "bytes: ${byteArrayToHex(bytes).joinToString(" ")}")

        if (bytes.isNotEmpty()) {
            displayTonuinoInfo(tag, TagData(bytes))
        } else {
            showReadErrorModalDialog(tag)
        }
    }
}
