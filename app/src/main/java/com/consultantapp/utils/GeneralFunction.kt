package com.consultantapp.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Handler
import android.provider.MediaStore
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.text.HtmlCompat
import androidx.core.text.toSpanned
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.consultantapp.BuildConfig
import com.consultantapp.R
import com.consultantapp.appClientDetails
import com.consultantapp.data.models.responses.UserData
import com.consultantapp.data.network.Config
import com.consultantapp.ui.loginSignUp.SignUpActivity
import com.consultantapp.ui.webview.WebViewActivity
import com.consultantapp.utils.DateUtils.dateFormatChange
import com.consultantapp.utils.dialogs.ProgressDialog
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.dynamiclinks.ktx.*
import com.google.firebase.ktx.Firebase
import com.stfalcon.frescoimageviewer.ImageViewer
import id.zelory.compressor.Compressor
import okhttp3.MediaType
import okhttp3.RequestBody
import java.io.File
import java.text.NumberFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


fun View.gone() {
    visibility = View.GONE
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun View.hideShowView(show: Boolean) {
    visibility = if (show) View.VISIBLE
    else View.GONE
}

fun getRequestBody(string: String?): RequestBody {
    return RequestBody.create(MediaType.parse("text/plain"), string)
}

fun View.showSnackBar(msg: String) {
    try {
        val snackBar = Snackbar.make(this, msg, Snackbar.LENGTH_LONG)
        val snackBarView = snackBar.view
        val textView =
            snackBarView.findViewById<View>(R.id.snackbar_text) as TextView
        textView.maxLines = 3
        snackBar.setAction(R.string.ok) { snackBar.dismiss() }
        snackBarView.setBackgroundColor(ContextCompat.getColor(context, R.color.colorPrimary))
        snackBar.setActionTextColor(ContextCompat.getColor(context, R.color.colorPrimary))
        snackBar.show()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun logoutUser(activity: Activity?, prefsManager: PrefsManager) {

    Log.d("logoutCalled", "clearData")

    val notificationManager =
        activity?.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.cancelAll()

    prefsManager.remove(USER_DATA)

    activity.setResult(Activity.RESULT_CANCELED)
    ActivityCompat.finishAffinity(activity)
    activity.startActivity(
        Intent(activity, SignUpActivity::class.java)
            .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
    )
}


/*Digits should be 0,1,2,3*/
fun getCountFormat(digits: Int, count: Int?): String {
    when (digits) {
        1 -> return if (count ?: 0 <= 9)
            count.toString()
        else
            String.format(Locale.ENGLISH, "%d+", 9)
        2 -> return if (count ?: 0 <= 99)
            count.toString()
        else
            String.format(Locale.ENGLISH, "%d+", 99)
        3 -> return if (count ?: 0 <= 999)
            count.toString()
        else
            String.format(Locale.ENGLISH, "%d+", 999)
        else -> return if (count ?: 0 <= 9999)
            count.toString()
        else
            String.format(Locale.ENGLISH, "%d+", 9999)
    }
}


fun Context.longToast(text: CharSequence) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

fun addFragment(fragmentManager: FragmentManager?, fragment: Fragment, id: Int) {
    fragmentManager?.beginTransaction()
        ?.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        ?.add(id, fragment)?.commit()
}

fun addFragmentAnim(fragmentManager: FragmentManager?, fragment: Fragment, id: Int) {
    fragmentManager?.beginTransaction()
        ?.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        ?.add(id, fragment)?.commit()
}

fun addFragmentToBackStack(fragmentManager: FragmentManager?, fragment: Fragment, id: Int) {
    fragmentManager?.beginTransaction()?.setCustomAnimations(0, 0, 0, 0)
        ?.add(id, fragment)?.addToBackStack("")?.commit()
}

fun replaceFragmentNoBackStack(fragmentManager: FragmentManager?, fragment: Fragment, id: Int) {
    fragmentManager?.beginTransaction()?.replace(id, fragment)?.commit()
}

fun replaceFragment(fragmentManager: FragmentManager?, fragment: Fragment, id: Int) {
    fragmentManager?.beginTransaction()
        ?.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        ?.replace(id, fragment, fragment::class.simpleName)?.addToBackStack(null)?.commit()
}

fun addTabFragment(fragmentManager: FragmentManager?, fragment: Fragment, id: Int) {
    fragmentManager?.beginTransaction()
        ?.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
        ?.add(id, fragment, fragment::class.simpleName)?.addToBackStack(null)?.commit()
}


fun replaceFragmentWithoutBackStack(
    fragmentManager: FragmentManager?,
    fragment: Fragment,
    id: Int
) {
    fragmentManager?.beginTransaction()?.setCustomAnimations(
        android.R.anim.fade_in,
        android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out
    )?.replace(id, fragment)?.commit()
}


fun replaceResultFragment(
    fragment: Fragment,
    targetFragment: Fragment,
    container: Int,
    requestCode: Int
) {
    val ft = fragment.requireActivity().supportFragmentManager.beginTransaction()
    targetFragment.setTargetFragment(fragment, requestCode)
    ft.addToBackStack("")
    ft.replace(container, targetFragment, fragment.tag)
    ft.commit()
}

fun resultFragmentIntent(
    fragment: Fragment,
    fragmentTarget: Fragment,
    requestCode: Int,
    intent: Intent?
) {
    val intentMain = intent ?: Intent(fragment.requireContext(), fragmentTarget::class.java)
    fragmentTarget.onActivityResult(requestCode, RESULT_OK, intentMain)
    fragment.activity?.supportFragmentManager?.popBackStack()
}

fun View.hideKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.hideSoftInputFromWindow(windowToken, 0)
}

fun View.showKeyboard() {
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInputFromWindow(
        applicationWindowToken,
        InputMethodManager.SHOW_FORCED, 0
    )
}

fun makeFullScreen(activity: Activity) {
    activity.window.decorView.systemUiVisibility =
        View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
    activity.window.statusBarColor = Color.TRANSPARENT
}


fun getAge(date: String?): String {
    if (date == null)
        return "NA"
    else {
        val dob = Calendar.getInstance()
        val today = Calendar.getInstance()

        val dateChange = dateFormatChange(
            DateFormat.DATE_FORMAT,
            DateFormat.DATE_FORMAT_SLASH_YEAR, date
        )
        val formatter = SimpleDateFormat(DateFormat.DATE_FORMAT_SLASH_YEAR, Locale.ENGLISH)
        try {
            dob.time = formatter.parse(dateChange)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)

        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }

        val ageInt = age

        return ageInt.toString()
    }
}

val requestOptions = RequestOptions()
    .dontAnimate()
    .dontTransform()

fun loadImage(ivImage: ImageView, image: String?, placeholder: Int) {
    val imageLink = getImageBaseUrl(ImageFolder.UPLOADS, image)
    val imageThumbnail = getImageBaseUrl(ImageFolder.THUMBS, image)

    val glide = Glide.with(ivImage.context)

    glide.load(imageLink)
        .apply(requestOptions)
        .placeholder(placeholder)
        .thumbnail(glide.load(imageThumbnail))
        .into(ivImage)
}

fun getImageBaseUrl(folderType: String, image: String?): String {
    return "${Config.imageURL}$folderType$image"
}

fun pxFromDp(context: Context, dp: Float): Float {
    return dp * context.resources.displayMetrics.density
}

fun getVersion(activity: Activity): PackageInfo {
    return activity.packageManager.getPackageInfo(activity.packageName, 0)
}

fun disableButton(btn: View?) {
    btn?.isEnabled = false

    Handler().postDelayed({
        btn?.isEnabled = true
    }, 1500)// set time as per your requirement
}

fun getDoctorName(userData: UserData?): String {
    return if (userData?.profile?.title.isNullOrEmpty())
        userData?.name ?: ""
    else
        "${userData?.profile?.title ?: ""} ${userData?.name}"
}

fun getCurrency(amount: String?): String {

    val format = NumberFormat.getCurrencyInstance()
    format.maximumFractionDigits = 2
    format.currency = Currency.getInstance(appClientDetails.currency)

    val formatFinal=if (amount.isNullOrEmpty())
        format.format(0).replace("0.00", " NA")
    else {
        format.format(amount.toDouble()).replace(".00", "")
    }

    return formatFinal.replace("CA","")
}

fun getCurrencySymbol(): String {
    val format = NumberFormat.getCurrencyInstance()
    format.currency = Currency.getInstance(appClientDetails.currency)

    return format.currency.symbol.replace("CA","")
}

fun getUnitPrice(unit: Int?, context: Context): String {
    return when {
        unit == null -> "NA"
        unit >= 3600 ->
            if (unit / 3600 == 1) context.getString(R.string.hr)
            else "${(unit / 3600)} ${context.getString(R.string.hr)}"
        else -> "${(unit / 60)} ${context.getString(R.string.min)}"
    }
}

@SuppressLint("ClickableViewAccessibility")
fun editTextScroll(editText: EditText) {
    editText.setOnTouchListener { v, event ->
        v.parent.requestDisallowInterceptTouchEvent(true)
        if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
            v.parent.requestDisallowInterceptTouchEvent(false)
        }
        false
    }
}

fun getUserRating(rating: String?): String {
    val ratingNew = rating ?: "0.0"

    return String.format(Locale.ENGLISH, "%.1f", ratingNew.toFloat())
}

fun compressImage(activity: Activity?, actualImageFile: File?): File {
    Log.e("File Size", actualImageFile?.length().toString())

    /*mb approximate*/
    val resultFile: File? = when {
        actualImageFile?.length() ?: 0 < (1 * 1024 * 1024) -> actualImageFile
        actualImageFile?.length() ?: 0 < (3 * 1024 * 1024) -> {
            Compressor(activity)
                .setQuality(70)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .compressToFile(actualImageFile)
        }
        else -> {
            Compressor(activity)
                .setQuality(50)
                .setCompressFormat(Bitmap.CompressFormat.JPEG)
                .compressToFile(actualImageFile)
        }
    }

    Log.e("File Size New", resultFile?.length().toString())

    return resultFile ?: File("")
}


fun viewImageFull(activity: Activity, itemsImage: ArrayList<String>, pos: Int) {

    val hierarchyBuilder = GenericDraweeHierarchyBuilder
        .newInstance(activity.resources)
        .setFailureImage(R.drawable.image_placeholder)
        .setProgressBarImage(R.drawable.image_placeholder)
        .setPlaceholderImage(R.drawable.image_placeholder)

    ImageViewer.Builder(activity, itemsImage)
        .setStartPosition(pos)
        .hideStatusBar(false)
        .setCustomDraweeHierarchyBuilder(hierarchyBuilder)
        .show()
}

fun placePicker(fragment: Fragment?, activityMain: Activity) {
    val activity: Activity = if (fragment != null)
        fragment.activity as Activity
    else
        activityMain

    val fields =
        listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
    val intent = Autocomplete.IntentBuilder(
        AutocompleteActivityMode.FULLSCREEN, fields
    )
        .build(activity)

    if (fragment == null)
        activity.startActivityForResult(intent, AppRequestCode.AUTOCOMPLETE_REQUEST_CODE)
    else
        fragment.startActivityForResult(intent, AppRequestCode.AUTOCOMPLETE_REQUEST_CODE)
}

fun getAddress(place: Place): String {
    val finalAddress: String
    val name = place.name.toString()
    val placeAddress = place.address.toString()

    if (place.address?.contains(name) == true) {
        finalAddress = placeAddress
    } else {
        finalAddress = "$name, $placeAddress"
    }
    return finalAddress
}

fun getPathUri(context: Context, uri: Uri): String? {
    val projection =
        arrayOf(MediaStore.Images.Media.DATA)
    val cursor = context.contentResolver.query(uri, projection, null, null, null)
        ?: return ""
    val column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
    cursor.moveToFirst()
    val result = cursor.getString(column_index)
    cursor.close()
    return result
}


fun setAcceptTerms(activity: Activity): SpannableString {
    val term = String.format(
        "%s %s %s %s",
        activity.getString(R.string.you_agree_to_our_terms),
        activity.getString(R.string.terms),
        activity.getString(R.string.and),
        activity.getString(R.string.privacy)
    )

    val string = SpannableString.valueOf(term)
    string.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(activity, R.color.colorPrimary)),
        term.indexOf(activity.getString(R.string.terms)),
        term.indexOf(" " + activity.getString(R.string.and) + " "), 0
    )

    string.setSpan(
        ForegroundColorSpan(ContextCompat.getColor(activity, R.color.colorPrimary)),
        term.indexOf(activity.getString(R.string.privacy)), term.length, 0
    )

    string.setSpan(
        Terms(), term.indexOf(activity.getString(R.string.terms)),
        term.indexOf(" " + activity.getString(R.string.and) + " "), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    string.setSpan(
        Privacy(), term.indexOf(activity.getString(R.string.terms)),
        term.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
    )

    return string
}

class Privacy : ClickableSpan() {

    override fun onClick(tv: View) {
        // tv.context.longToast("Clicked")

        tv.context.startActivity(
            Intent(tv.context, WebViewActivity::class.java)
                .putExtra(WebViewActivity.LINK_TITLE, tv.context.getString(R.string.privacy))
                .putExtra(WebViewActivity.LINK_URL, PageLink.PRIVACY_POLICY)
        )
    }

    override fun updateDrawState(ds: TextPaint) {// override updateDrawState
        ds.isUnderlineText = false // set to false to remove underline
    }
}

class Terms : ClickableSpan() {

    override fun onClick(tv: View) {
        //viewPage(PageLink.TERMS)

        tv.context.startActivity(
            Intent(tv.context, WebViewActivity::class.java).putExtra(
                WebViewActivity.LINK_TITLE,
                tv.context.getString(R.string.terms_and_conditions)
            )
                .putExtra(WebViewActivity.LINK_URL, PageLink.TERMS_CONDITIONS)
        )
    }

    override fun updateDrawState(ds: TextPaint) {// override updateDrawState
        ds.isUnderlineText = false // set to false to remove underline
    }
}

fun getHtmlText(string: String): Spanned {
    return HtmlCompat.fromHtml(string, HtmlCompat.FROM_HTML_MODE_LEGACY).toSpanned()
}


/*Share*/
fun shareDeepLink(deepLink: String, activity: Activity, userData: UserData?) {
    val progressDialog = ProgressDialog(activity)
    progressDialog.setLoading(true)

    var longLink = ""
    var titleM = ""
    var descriptionM = ""
    var imageUrlM = Uri.parse("")
    when (deepLink) {
        DeepLink.USER_PROFILE -> {
            longLink = "${Config.baseURL}${deepLink}?id=${userData?.id}"

            titleM = "${userData?.categoryData?.name} | ${userData?.name}"
            descriptionM = userData?.profile?.bio ?: ""
            imageUrlM =
                Uri.parse(getImageBaseUrl(ImageFolder.UPLOADS, userData?.profile_image ?: ""))
        }
        DeepLink.INVITE -> {
            longLink = "${Config.baseURL}${deepLink}"

            titleM = activity.getString(R.string.app_name)
            descriptionM = activity.getString(R.string.invite_text)
            imageUrlM =
                Uri.parse(getImageBaseUrl(ImageFolder.UPLOADS, appClientDetails.applogo ?: ""))
        }
    }

    val shortLinkTask = Firebase.dynamicLinks.shortLinkAsync {
        link = Uri.parse(longLink)
        domainUriPrefix = "https://${activity.getString(R.string.deep_link_url)}"
        // Open links with this app on Android
        androidParameters(BuildConfig.APPLICATION_ID) { }
        // Open links with com.example.ios on iOS
        iosParameters(activity.getString(R.string.deep_link_ios_bundle)) { }

        socialMetaTagParameters {
            title = titleM
            description = descriptionM
            imageUrl = imageUrlM
        }
    }.addOnSuccessListener { result ->
        progressDialog.setLoading(false)

        // Short link created
        val shortLink = result.shortLink

        /*Share Intent*/
        val shareIntent = Intent()
        shareIntent.action = Intent.ACTION_SEND
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, activity.getString(R.string.share))
        shareIntent.putExtra(
            Intent.EXTRA_TEXT,
            "${activity.getString(R.string.app_name)}\n$shortLink"
        )

        shareIntent.type = "text/plain"
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        activity.startActivity(
            Intent.createChooser(
                shareIntent,
                activity.getString(R.string.share)
            )
        )

    }.addOnFailureListener {
        // Error
        //ivShare.showSnackBar(getString(R.string.error))
        progressDialog.setLoading(false)
    }
}

fun slideItem(viewToAnimate: View, context: Context) {
    val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.up_to_down)
    viewToAnimate.startAnimation(animation)
}


fun slideRecyclerItem(viewToAnimate: View, context: Context) {
    val animation: Animation = AnimationUtils.loadAnimation(context, R.anim.slide_out_bottom)
    viewToAnimate.startAnimation(animation)
}


fun mapIntent(activity: Activity, name: String, lat: Double, lng: Double) {
    try {
        val url = "http://maps.google.com/maps?daddr=$lat,$lng($name)"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        intent.setPackage("com.google.android.apps.maps")
        activity.startActivity(intent)
    } catch (e: Exception) {
        activity.longToast(activity.getString(R.string.map_not_found))
    }
}

fun getDatesComma(date: String?): String {
    var newList = ""
    if (!date.isNullOrEmpty()) {
        val list = date.split(",")
        list.forEach {
            newList += "${
                dateFormatChange(
                    DateFormat.DATE_FORMAT,
                    DateFormat.MON_YEAR_FORMAT,
                    it
                )
            } | "
        }
    }
    return newList.removeSuffix(" | ")
}