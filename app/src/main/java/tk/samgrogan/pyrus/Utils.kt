package tk.samgrogan.pyrus

import android.content.Context
import android.graphics.Bitmap
import android.os.ParcelFileDescriptor
import com.github.barteksc.pdfviewer.util.FileUtils
import com.shockwave.pdfium.PdfiumCore



fun pdfJpg() {



}

fun generateImageFromPdf(assetFileName: String, pageNumber: Int, width: Int, height: Int, context: Context): Bitmap? {

    val pdfiumCore = PdfiumCore(context)
    val f = FileUtils.fileFromAsset(context, assetFileName)
    val fd = ParcelFileDescriptor.open(f, ParcelFileDescriptor.MODE_READ_ONLY)
    val pdfDocument = pdfiumCore.newDocument(fd)
    pdfiumCore.openPage(pdfDocument, pageNumber)
    //int width = pdfiumCore.getPageWidthPoint(pdfDocument, pageNumber);
    //int height = pdfiumCore.getPageHeightPoint(pdfDocument, pageNumber);
    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    pdfiumCore.renderPageBitmap(pdfDocument, bmp, pageNumber, 0, 0, width, height)
    //saveImage(bmp, filena);
    pdfiumCore.closeDocument(pdfDocument)

    return bmp
}