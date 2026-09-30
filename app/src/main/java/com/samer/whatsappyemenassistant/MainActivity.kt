package com.samer.whatsappyemenassistant

import android.Manifest
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
 private var pendingRecheck = false
 private val supportWhatsApp = "967775188338"
 private val obWhatsAppUpdateUrl = "https://www.omar-yemen.com/p/obwhatsapp.html"
 private val obWhatsAppTelegramUrl = "https://t.me/Whatsapp_OB"
 private fun button(t:String, action:()->Unit)=Button(this).apply{text=t;setOnClickListener{action()}}
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL;setPadding(28,42,28,36)}
  root.addView(TextView(this).apply{text="🇾🇪 مساعد واتساب اليمن";textSize=27f;gravity=Gravity.CENTER})
  root.addView(TextView(this).apply{text="تطوير: سامر حوشب | Samer Hoshab";textSize=15f;gravity=Gravity.CENTER;setPadding(0,6,0,20)})
  root.addView(TextView(this).apply{text="فحص → تشخيص → إصلاح تلقائي آمن → تحقق";textSize=17f;gravity=Gravity.CENTER;setPadding(0,0,0,18)})
  root.addView(button("🚀 الفحص والإصلاح التلقائي"){requestAndScan()},LinearLayout.LayoutParams(-1,-2))
  result=TextView(this).apply{text="اضغط الزر مرة واحدة. سيجري الفحص ثم يبدأ مسار الإصلاح المناسب تلقائيًا، دون قراءة رسائلك أو رمز التفعيل ودون حذف بيانات واتساب.";textSize=16f;setPadding(8,20,8,20)};root.addView(result)
  root.addView(button("🔄 إعادة الفحص والتحقق"){deepScan(true)},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🚫 الحساب محظور / طلب مراجعة"){showBanHelp()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🛡️ مشكلة: استخدم واتساب الرسمي"){showOfficialHelp()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("📩 رمز التفعيل لا يصل"){showCodeHelp()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("⚙️ إعدادات واتساب"){openWhatsAppSettings()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🛒 تحديث واتساب الرسمي"){openPlay()},LinearLayout.LayoutParams(-1,-2))
  root.addView(TextView(this).apply{text="خيارات بديلة عند استمرار مشكلة واتساب الرسمي";textSize=17f;gravity=Gravity.CENTER;setPadding(0,24,0,8)})
  root.addView(button("🍷 تحديث واتساب عمر العنابي (OBWhatsApp)"){openObWhatsAppUpdate()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("📢 قناة تحديثات واتساب عمر"){openObWhatsAppTelegram()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("📤 إرسال تقرير الفحص إلى سامر"){sendReportToSupport()},LinearLayout.LayoutParams(-1,-2))
  root.addView(TextView(this).apply{text="تنبيه: واتساب عمر العنابي تطبيق معدل غير تابع لواتساب أو Meta. لا يضمن هذا المساعد عدم الحظر أو سلامة النسخ المعدلة. استخدمه على مسؤوليتك ومن صفحة التحديث المعتمدة فقط. الإعدادات التي يحميها Android تحتاج موافقتك، والتطبيق لا يفك حظر خوادم واتساب ولا يتجاوز التحقق أو رمز التفعيل.";textSize=14f;gravity=Gravity.CENTER;setPadding(4,22,4,8)})
  setContentView(ScrollView(this).apply{addView(root)})
 }
 override fun onResume(){super.onResume();if(pendingRecheck){pendingRecheck=false;result.postDelayed({deepScan(false)},500)}}
 private fun requestAndScan(){if(Build.VERSION.SDK_INT>=23&&ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED)ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_PHONE_STATE),50) else deepScan(true)}
 override fun onRequestPermissionsResult(r:Int,p:Array<out String>,g:IntArray){super.onRequestPermissionsResult(r,p,g);if(r==50)deepScan(true)}
 private fun installed(p:String)=try{packageManager.getPackageInfo(p,0);true}catch(e:Exception){false}
 private fun pkgVersion(p:String):String?=try{packageManager.getPackageInfo(p,0).versionName}catch(e:Exception){null}
 private fun installer(p:String):String?=try{if(Build.VERSION.SDK_INT>=30)packageManager.getInstallSourceInfo(p).installingPackageName else @Suppress("DEPRECATION") packageManager.getInstallerPackageName(p)}catch(e:Exception){null}
 private fun isOnline():Boolean{val cm=getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager;@Suppress("DEPRECATION") return cm.activeNetworkInfo?.isConnected==true}
 private fun autoTime():Boolean=try{Settings.Global.getInt(contentResolver,Settings.Global.AUTO_TIME,0)==1}catch(e:Exception){false}
 private fun rooted():Boolean=listOf("/system/bin/su","/system/xbin/su","/sbin/su","/su/bin/su","/data/local/bin/su").any{java.io.File(it).exists()}||Build.TAGS?.contains("test-keys")==true
 private fun deepScan(autoRepair:Boolean){
  val wa=installed("com.whatsapp");val gms=installed("com.google.android.gms");val play=installed("com.android.vending");val business=installed("com.whatsapp.w4b");val src=if(wa)installer("com.whatsapp") else null;val online=isOnline();val time=autoTime();val root=rooted();var problems=0
  val report=buildString{append("📋 تقرير مساعد واتساب اليمن\nتطوير: سامر حوشب\n\n📱 ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\nWhatsApp: ${pkgVersion("com.whatsapp")?:"غير مثبت"}\n\n");if(wa)append("✅ واتساب مثبت.\n")else{append("❌ واتساب غير مثبت.\n");problems++};if(wa&&src=="com.android.vending")append("✅ مصدر واتساب: Google Play.\n")else if(wa){append("⚠️ مصدر واتساب: ${src?:"غير معروف"}.\n");problems++};if(gms)append("✅ Google Play Services موجودة.\n")else{append("⚠️ Google Play Services غير ظاهرة.\n");problems++};if(play)append("✅ متجر Google Play موجود.\n")else{append("⚠️ متجر Google Play غير ظاهر.\n");problems++};if(online)append("✅ اتصال الشبكة نشط.\n")else{append("❌ لا يوجد اتصال شبكة نشط.\n");problems++};if(time)append("✅ الوقت التلقائي مفعّل.\n")else{append("⚠️ الوقت التلقائي غير مفعّل.\n");problems++};if(root){append("⚠️ مؤشر Root/نظام معدل ظاهر.\n");problems++}else append("✅ لم يظهر مؤشر Root أساسي.\n");if(business)append("ℹ️ WhatsApp Business مثبت أيضًا.\n");append("\n🎯 النتيجة: ");append(if(problems==0)"الفحوص المحلية الأساسية سليمة." else "تم اكتشاف $problems نقطة تحتاج معالجة.");if(autoRepair)append("\n🔧 بدأ اختيار الإصلاح المناسب تلقائيًا...")}
  lastReport=report;result.text=report;if(autoRepair)result.postDelayed({smartRepair()},700)
 }
 private fun smartRepair(){val wa=installed("com.whatsapp");val src=if(wa)installer("com.whatsapp") else null;when{!isOnline()->openProtectedFix("إصلاح الشبكة","لا توجد شبكة نشطة. سيتم فتح إعدادات الشبكة الآن. اتصل بالإنترنت ثم ارجع للتطبيق وسيعيد الفحص تلقائيًا.",Intent(Settings.ACTION_WIRELESS_SETTINGS));!autoTime()->openProtectedFix("إصلاح الوقت","الوقت التلقائي غير مفعّل. سيتم فتح إعدادات التاريخ والوقت. فعّل الضبط التلقائي ثم ارجع وسيعاد الفحص تلقائيًا.",Intent(Settings.ACTION_DATE_SETTINGS));!wa->{dialog("تثبيت واتساب الرسمي","لم أجد واتساب. سيتم فتح النسخة الرسمية في Google Play.");openPlay()};src!="com.android.vending"->{dialog("مصدر التثبيت يحتاج معالجة","لن أحذف واتساب أو بياناته تلقائيًا. احفظ النسخة الاحتياطية أولًا؛ سأفتح النسخة الرسمية في Google Play.");openPlay()};rooted()->dialog("سلامة النظام تحتاج تدخلك","ظهر مؤشر Root/نظام معدل. إزالة Root أو تغيير النظام تلقائيًا قد تتلف الجهاز أو البيانات، لذلك لن ينفذها التطبيق.");else->AlertDialog.Builder(this).setTitle("✅ الفحص المحلي سليم").setMessage("لم يظهر خلل محلي يمكن إصلاحه تلقائيًا. جرّب واتساب الرسمي أولًا. إذا استمرت المشكلة يمكنك إرسال التقرير إلى سامر أو فتح الخيارات البديلة من الشاشة الرئيسية.").setPositiveButton("إرسال التقرير"){_,_->sendReportToSupport()}.setNegativeButton("فتح واتساب"){_,_->openWhatsApp()}.show()}}
 private fun sendReportToSupport(){
  if(lastReport=="لم يتم إجراء فحص بعد."){dialog("أجرِ الفحص أولًا","اضغط «الفحص والإصلاح التلقائي» أولًا حتى يتم إنشاء تقرير حقيقي للجهاز.");return}
  val msg="السلام عليكم سامر، لدي مشكلة في واتساب. هذا تقرير الفحص من تطبيق مساعد واتساب اليمن:\n\n$lastReport\n\nأحتاج تشخيص المشكلة والحل المناسب."
  val uri=Uri.parse("https://wa.me/$supportWhatsApp?text=${Uri.encode(msg)}")
  try{startActivity(Intent(Intent.ACTION_VIEW,uri).apply{setPackage("com.whatsapp")})}catch(e:Exception){try{startActivity(Intent(Intent.ACTION_VIEW,uri))}catch(x:Exception){dialog("تعذر فتح واتساب","انسخ التقرير من الشاشة وأرسله إلى الدعم يدويًا.")}}
 }
 private fun openObWhatsAppUpdate(){AlertDialog.Builder(this).setTitle("واتساب عمر العنابي (OBWhatsApp)").setMessage("هذا تطبيق معدل وغير تابع لواتساب أو Meta. لا يوجد ضمان لعدم الحظر أو سلامة النسخة. سيتم فتح صفحة التحديث الخارجية فقط ولن يثبت المساعد أي APK تلقائيًا.").setPositiveButton("فتح صفحة التحديث"){_,_->openUrl(obWhatsAppUpdateUrl)}.setNegativeButton("إلغاء",null).show()}
 private fun openObWhatsAppTelegram(){AlertDialog.Builder(this).setTitle("قناة تحديثات واتساب عمر").setMessage("سيتم فتح قناة التحديثات الخارجية. تحقق من الإصدار والمصدر قبل تثبيت أي ملف.").setPositiveButton("فتح القناة"){_,_->openUrl(obWhatsAppTelegramUrl)}.setNegativeButton("إلغاء",null).show()}
 private fun openUrl(url:String){try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}catch(e:Exception){dialog("تعذر فتح الرابط","لا يوجد تطبيق قادر على فتح الرابط حاليًا.")}}
 private fun openProtectedFix(t:String,m:String,i:Intent){AlertDialog.Builder(this).setTitle(t).setMessage(m).setPositiveButton("ابدأ الإصلاح"){_,_->pendingRecheck=true;safeStart(i)}.setNegativeButton("إلغاء",null).show()}
 private fun dialog(t:String,m:String)=AlertDialog.Builder(this).setTitle(t).setMessage(m).setPositiveButton("حسنًا",null).show()
 private fun showBanHelp()=dialog("الحساب المحظور","إذا كان الحظر صادرًا من خوادم واتساب فلا يمكن فكه من الهاتف. استخدم «طلب مراجعة» داخل واتساب الرسمي عند ظهوره.")
 private fun showOfficialHelp()=dialog("استخدم واتساب الرسمي","شغّل «الفحص والإصلاح التلقائي». سيعالج المشاكل المحلية المسموح بها أو يفتح إعداد النظام المحدد مباشرة.")
 private fun showCodeHelp()=dialog("رمز التفعيل في اليمن","استخدم +967 بدون صفر إضافي، وتأكد من استقبال SMS والمكالمات ومن الوقت التلقائي وانتظر عداد واتساب. التطبيق لا يقرأ رمز التفعيل ولا يولده.")
 private fun safeStart(i:Intent){try{startActivity(i)}catch(e:Exception){startActivity(Intent(Settings.ACTION_SETTINGS))}}
 private fun openWhatsApp(){packageManager.getLaunchIntentForPackage("com.whatsapp")?.let{startActivity(it)}?:openPlay()}
 private fun openPlay(){try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.whatsapp")))}catch(e:Exception){startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")))}}
 private fun openWhatsAppSettings(){safeStart(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:com.whatsapp")))}
}
