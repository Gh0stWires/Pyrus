package tk.samgrogan.pyrus


import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.improvelectronics.sync.android.SyncFtpListener
import com.improvelectronics.sync.android.SyncFtpService
import com.improvelectronics.sync.android.SyncStreamingService
import com.improvelectronics.sync.obex.OBEXFtpFolderListingItem
import kotlinx.android.synthetic.main.fragment_file.*


class FileFragment : Fragment(), SyncFtpListener {

    private lateinit var ftpService: SyncFtpService
    private var ftpServiceBound: Boolean = false
    var connectedToFtp: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val intent = Intent(activity, SyncFtpService::class.java)
        activity?.bindService(intent, mConnection, Context.BIND_AUTO_CREATE)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fileRecycler.layoutManager = LinearLayoutManager(context)
        fileRecycler.adapter = FileAdapter {
            if (it.name.endsWith("pdf", ignoreCase = true)) {
                ftpService.getFile(it)

            } else {
                ftpService.changeFolder(it.name)
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        if (ftpServiceBound) {
            // Be sure to send a disconnect if the server was still connected.
            if (connectedToFtp) ftpService.disconnect()

            // Don't forget to remove the listener and unbind from the service.
            ftpService.removeListener(this@FileFragment)
            activity?.unbindService(mConnection)
        }
    }
    
    override fun onChangeFolderComplete(uri: Uri?, result: Int) {
        ftpService.listFolder()
    }

    override fun onFtpDeviceStateChange(prevState: Int, newState: Int) {
        if (newState == SyncFtpService.STATE_CONNECTED) {
            ftpService.connect()
        }
    }

    override fun onConnectComplete(result: Int) {
        if (result == SyncFtpService.RESULT_OK) {
            connectedToFtp = true
            ftpService.changeFolder("")
        } else {
            Toast.makeText(activity, "Failed to connect", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDeleteComplete(file: OBEXFtpFolderListingItem?, result: Int) {

    }

    override fun onDisconnectComplete(result: Int) {
        connectedToFtp = false
    }

    override fun onFolderListingComplete(items: List<OBEXFtpFolderListingItem>, result: Int) {
        if (result == SyncFtpService.RESULT_OK) {
            (fileRecycler.adapter as FileAdapter).swapData(items)
        } else {
            Toast.makeText(activity, "Failed to retrieve folder listing", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onGetFileComplete(file: OBEXFtpFolderListingItem?, result: Int) {
        val data = file?.data
        val image = data
        val options = BitmapFactory.Options()
        options.outWidth = 480
        options.outHeight = 360
        val bitmap = image?.size.let { image?.count()?.let { it1 ->
            BitmapFactory.decodeByteArray(image, 0,
                it1, options)
        } }

        val fireImage = bitmap?.let { FirebaseVisionImage.fromBitmap(it) }
        val detector = FirebaseVision.getInstance().onDeviceTextRecognizer
                fireImage?.let { it1 ->
                    detector.processImage(it1)
                        .addOnSuccessListener { firebaseVisionText ->
                            startActivity(context?.let { it2 -> TextActivity.newIntent(it2, firebaseVisionText.text) })
                        }
                        .addOnFailureListener {
                            Log.d("Here is why:", it.toString())
                            // Task failed with an exception
                            // ...
                        }
                }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_file, container, false)
    }


    private val mConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // Set up the service
            ftpServiceBound = true
            val binder = service as SyncFtpService.SyncFtpBinder
            ftpService = binder.service
            ftpService.addListener(this@FileFragment)// Add listener to retrieve events from ftp service.

            if (ftpService.state == SyncStreamingService.STATE_CONNECTED) {
                // Connect to the ftp server.
                ftpService.connect()
            }
        }

        override fun onServiceDisconnected(name: ComponentName) {
            ftpService.disconnect()
            ftpServiceBound = false
        }
    }
}
