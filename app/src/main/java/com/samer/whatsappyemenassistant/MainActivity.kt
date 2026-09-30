package com.samer.whatsappyemenassistant

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class MainActivity : AppCompatActivity() {
 private lateinit var result: TextView
 private var lastReport = "لم يتم إجراء فحص بعد."
 private fun button(t:String, action:()->Unit)=Button(this).apply{text=t;setOnClickListener{action()}}
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL;setPadding(28,42,28,36)}
  root.addView(TextView(this).apply{text="🇾🇪 مساعد واتساب اليمن";textSize=27f;gravity=Gravity.CENTER})
  root.addView(TextView(this).apply{text="تطوير: سامر حوشب | Samer Hoshab";textSize=15f;gravity=Gravity.CENTER;setPadding(0,6,0,20)})
  root.addView(TextView(this).apply{text="فحص → تشخيص → إصلاح آمن → إعادة تحقق";textSize=17f;gravity=Gravity.CENTER;setPadding(0,0,0,18)})
  root.addView(button("🧠 الفحص الذكي العميق"){requestAndScan()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🔧 الإصلاح الذكي"){smartRepair()},LinearLayout.LayoutParams(-1,-2))
  result=TextView(this).apply{text="ابدأ بالفحص الذكي. سيحدد التطبيق المشاكل التي يستطيع Android كشفها دون قراءة رسائلك أو رمز التفعيل.";textSize=16f;setPadding(8,20,8,20)};root.addView(result)
  root.addView(button("🔄 إعادة الفحص بعد الإصلاح"){deepScan()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🚫 الحساب محظور / طلب مراجعة"){showBanHelp()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🛡️ مشكلة: استخدم واتساب الرسمي"){showOfficialHelp()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("📩 رمز التفعيل لا يصل"){showCodeHelp()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🕒 ضبط الوقت والتاريخ"){safeStart(Intent(Settings.ACTION_DATE_SETTINGS))},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🌐 إعدادات الشبكة"){safeStart(Intent(Settings.ACTION_WIRELESS_SETTINGS))},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("⚙️ إعدادات واتساب"){openWhatsAppSettings()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🛒 تحديث واتساب الرسمي"){openPlay()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("▶️ اختبار فتح واتساب"){openWhatsApp()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("📤 مشاركة تقرير الفحص"){shareReport()},LinearLayout.LayoutParams(-1,-2))
  root.addView(TextView(this).apply{text="الإصلاح الذكي لا يمسح بيانات واتساب ولا يقرأ المحادثات. الإعدادات المحمية يفتح التطبيق شاشتها لتأكيدك لأن Android يمنع تغييرها سرًا.";textSize=14f;gravity=Gravity.CENTER;setPadding(4,22,4,8)})
  setContentView(ScrollView(this).apply{addView(root)})
 }
 private fun requestAndScan(){
  if(Build.VERSION.SDK_INT>=23 && ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED){
   ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_PHONE_STATE),50)
  } else deepScan()
 }
 override fun onRequestPermissionsResult(r:Int,p:Array<out String>,g:IntArray){super.onRequestPermissionsResult(r,p,g);if(r==50)deepScan()}
 private fun installed(p:String)=try{packageManager.getPackageInfo(p,0);true}catch(e:Exception){false}
 private fun pkgVersion(p:String):String?=try{packageManager.getPackageInfo(p,0).versionName}catch(e:Exception){null}
 private fun installer(p:String):String?=try{if(Build.VERSION.SDK_INT>=30)packageManager.getInstallSourceInfo(p).installingPackageName else @Suppress("DEPRECATION") packageManager.getInstallerPackageName(p)}catch(e:Exception){null}
 private fun isOnline():Boolean{val cm=getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager;@Suppress("DEPRECATION") return cm.activeNetworkInfo?.isConnected==true}
 private fun autoTime():Boolean=try{Settings.Global.getInt(contentResolver,Settings.Global.AUTO_TIME,0)==1}catch(e:Exception){false}
 private fun developerOptions():Boolean=try{Settings.Global.getInt(contentResolver,Settings.Global.DEVELOPMENT_SETTINGS_ENABLED,0)==1}catch(e:Exception){false}
 private fun rooted():Boolean=listOf("/system/bin/su","/system/xbin/su","/sbin/su","/su/bin/su","/data/local/bin/su").any{java.io.File(it).exists()} || Build.TAGS?.contains("test-keys")==true
 private fun deepScan(){
  val wa=installed("com.whatsapp"); val business=installed("com.whatsapp.w4b"); val gms=installed("com.google.android.gms"); val play=installed("com.android.vending")
  val src=if(wa)installer("com.whatsapp") else null; val online=isOnline(); val root=rooted(); val time=autoTime(); val dev=developerOptions()
  var problems=0
  val report=buildString{
   append("📋 تقرير الفحص الذكي\n\n📱 الجهاز: ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
   append("WhatsApp: ${pkgVersion("com.whatsapp")?:"غير مثبت"}\n\n")
   if(wa) append("✅ واتساب مثبت.\n") else {append("❌ واتساب غير مثبت.\n");problems++}
   if(wa && src=="com.android.vending") append("✅ مصدر واتساب: Google Play.\n") else if(wa){append("⚠️ مصدر واتساب: ${src?:"غير معروف"}.\n");problems++}
   if(gms) append("✅ Google Play Services موجودة.\n") else {append("⚠️ Google Play Services غير ظاهرة.\n");problems++}
   if(play) append("✅ متجر Google Play موجود.\n") else {append("⚠️ متجر Google Play غير ظاهر.\n");problems++}
   if(online) append("✅ يوجد اتصال شبكة نشط.\n") else {append("❌ لا يوجد اتصال شبكة نشط.\n");problems++}
   if(time) append("✅ الوقت التلقائي مفعّل.\n") else {append("⚠️ الوقت التلقائي غير مفعّل.\n");problems++}
   if(root){append("⚠️ مؤشر Root/نظام معدل ظاهر.\n");problems++} else append("✅ لم يظهر مؤشر Root أساسي.\n")
   if(dev) append("ℹ️ خيارات المطور مفعّلة؛ ليست مشكلة بحد ذاتها.\n")
   if(business) append("ℹ️ WhatsApp Business مثبت أيضًا.\n")
   append("\n🎯 النتيجة: ");append(if(problems==0)"لم يكتشف الفحص المحلي سببًا واضحًا. إذا بقيت رسالة واتساب الرسمي فالمشكلة قد تكون في اعتماد/سلامة الجهاز أو تحقق واتساب نفسه." else "تم اكتشاف $problems نقطة تحتاج معالجة.")
   append("\n\n🔧 اضغط «الإصلاح الذكي» لفتح مسار الإصلاح المناسب دون حذف بيانات واتساب.")
  }
  lastReport=report;result.text=report
 }
 private fun smartRepair(){
  val wa=installed("com.whatsapp"); val src=if(wa)installer("com.whatsapp") else null
  when {
   !isOnline() -> {dialog("الإصلاح 1/1","لا توجد شبكة نشطة. سأفتح إعدادات الشبكة؛ اتصل بالإنترنت ثم ارجع واضغط إعادة الفحص.");safeStart(Intent(Settings.ACTION_WIRELESS_SETTINGS))}
   !autoTime() -> {dialog("الإصلاح 1/1","الوقت التلقائي غير مفعّل. سأفتح إعدادات التاريخ والوقت؛ فعّل الوقت والتاريخ التلقائي ثم أعد الفحص.");safeStart(Intent(Settings.ACTION_DATE_SETTINGS))}
   !wa -> {dialog("الإصلاح 1/1","واتساب غير مثبت. سأفتح النسخة الرسمية في Google Play.");openPlay()}
   src!="com.android.vending" -> {dialog("الإصلاح الآمن","مصدر تثبيت واتساب ليس Google Play أو تعذر إثباته. لن أحذف بياناتك تلقائيًا. احفظ النسخة الاحتياطية أولًا ثم استخدم النسخة الرسمية.");openPlay()}
   rooted() -> dialog("مؤشر سلامة الجهاز","ظهر مؤشر Root/نظام معدل. لا يمكن للتطبيق إزالة Root بأمان تلقائيًا؛ إعادة الجهاز إلى نظام رسمي موثوق هي المعالجة الآمنة قبل إعادة اختبار واتساب.")
   else -> AlertDialog.Builder(this).setTitle("الفحص المحلي سليم").setMessage("واتساب مثبت من Google Play والشبكة والوقت والفحوص المحلية الأساسية سليمة. الخطوة التالية هي تحديث واتساب ثم اختبار فتحه. إذا بقيت رسالة «استخدم واتساب الرسمي»، افحص اعتماد Play Protect/سلامة النظام؛ هذه نتيجة لا يستطيع تطبيق عادي تغييرها سرًا.").setPositiveButton("تحديث واتساب"){_,_->openPlay()}.setNegativeButton("إعدادات واتساب"){_,_->openWhatsAppSettings()}.show()
  }
 }
 private fun dialog(t:String,m:String)=AlertDialog.Builder(this).setTitle(t).setMessage(m).setPositiveButton("حسنًا",null).show()
 private fun showBanHelp()=dialog("الحساب المحظور","الحظر الصادر من خوادم واتساب لا يمكن فكه من الهاتف. افتح واتساب الرسمي واستخدم «طلب مراجعة» إن ظهر. لا تستخدم نسخًا معدلة أو أدوات إرسال مزعج، ثم انتظر قرار المراجعة.")
 private fun showOfficialHelp()=dialog("استخدم واتساب الرسمي","شغّل الفحص الذكي أولًا. التطبيق يفحص مصدر التثبيت وGoogle Play والشبكة والوقت ومؤشرات Root. إذا كانت كلها سليمة وبقيت الرسالة، فانتقل لفحص اعتماد Play Protect/سلامة النظام؛ لا يمكن تجاوز تحقق واتساب من تطبيق آخر.")
 private fun showCodeHelp()=dialog("رمز التفعيل في اليمن","استخدم +967 بدون صفر إضافي، تأكد أن الشريحة تستقبل SMS والمكالمات، فعّل الوقت التلقائي وانتظر عداد واتساب قبل إعادة الطلب. لا يقرأ هذا التطبيق رمز التفعيل ولا يطلبه.")
 private fun safeStart(i:Intent){try{startActivity(i)}catch(e:Exception){startActivity(Intent(Settings.ACTION_SETTINGS))}}
 private fun openWhatsApp(){packageManager.getLaunchIntentForPackage("com.whatsapp")?.let{startActivity(it)}?:openPlay()}
 private fun openPlay(){try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.whatsapp")))}catch(e:Exception){startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")))}}
 private fun openWhatsAppSettings(){safeStart(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:com.whatsapp")))}
 private fun shareReport(){startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_SUBJECT,"تقرير مساعد واتساب اليمن");putExtra(Intent.EXTRA_TEXT,lastReport)},"مشاركة التقرير"))}
}
