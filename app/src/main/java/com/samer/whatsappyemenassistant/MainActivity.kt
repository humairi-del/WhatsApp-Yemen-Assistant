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
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
 private lateinit var result: TextView
 private var lastReport = "لم يتم إجراء فحص بعد."
 private var pendingRecheck = false
 private val supportWhatsApp = "967775188338"
 private val obWhatsAppUpdateUrl = "https://www.omar-yemen.com/p/obwhatsapp.html"
 private val obWhatsAppTelegramUrl = "https://t.me/Whatsapp_OB"
 private val prefs by lazy { getSharedPreferences("repair_state", Context.MODE_PRIVATE) }
 private fun button(t:String, action:()->Unit)=Button(this).apply{text=t;setOnClickListener{action()}}

 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL;setPadding(28,42,28,36)}
  root.addView(TextView(this).apply{text="🇾🇪 مساعد واتساب اليمن";textSize=27f;gravity=Gravity.CENTER})
  root.addView(TextView(this).apply{text="تطوير: سامر حوشب | Samer Hoshab";textSize=15f;gravity=Gravity.CENTER;setPadding(0,6,0,20)})
  root.addView(TextView(this).apply{text="فحص عميق → تشخيص → إصلاح → تحقق";textSize=17f;gravity=Gravity.CENTER;setPadding(0,0,0,18)})
  root.addView(button("🚀 الفحص العميق والإصلاح التلقائي"){requestAndScan()},LinearLayout.LayoutParams(-1,-2))
  result=TextView(this).apply{text="يفحص واتساب والجهاز ومصدر التثبيت والتوقيع والتعارضات والشبكة والوقت، ثم يختار أقوى مسار إصلاح متاح. الخطوات المحمية في Android ستطلب موافقتك.";textSize=16f;setPadding(8,20,8,20)};root.addView(result)
  root.addView(button("🛡️ واتساب يقول: نزّل التطبيق الرسمي"){officialRejectedFlow()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🧹 الإصلاح الكامل وإعادة تثبيت واتساب"){fullReinstallFlow()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🔄 إعادة الفحص والتحقق"){deepScan(true)},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🚫 الحساب محظور / طلب مراجعة"){showBanHelp()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("📩 رمز التفعيل لا يصل"){showCodeHelp()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("⚙️ إعدادات واتساب"){openWhatsAppSettings()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🛒 واتساب الرسمي في Google Play"){openPlay()},LinearLayout.LayoutParams(-1,-2))
  root.addView(TextView(this).apply{text="خيارات خارجية عند استمرار مشكلة الرسمي";textSize=17f;gravity=Gravity.CENTER;setPadding(0,24,0,8)})
  root.addView(button("🍷 تحديث واتساب عمر العنابي (OBWhatsApp)"){openObWhatsAppUpdate()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("📢 قناة تحديثات واتساب عمر"){openObWhatsAppTelegram()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("📤 إرسال تقرير الفحص إلى سامر"){sendReportToSupport()},LinearLayout.LayoutParams(-1,-2))
  root.addView(TextView(this).apply{text="مهم: قبل مسح بيانات واتساب أو حذفه قد تفقد المحادثات غير المنسوخة احتياطيًا. Android لا يسمح للمساعد بمسح بيانات تطبيق آخر أو حذفه أو تثبيته بصمت؛ لذلك يفتح شاشة النظام الصحيحة وتؤكد أنت العملية. المساعد لا يتجاوز حظر واتساب أو التحقق من الخادم. واتساب عمر تطبيق معدل غير تابع لواتساب أو Meta وقد يحمل مخاطر حظر أو خصوصية.";textSize=14f;gravity=Gravity.CENTER;setPadding(4,22,4,8)})
  setContentView(ScrollView(this).apply{addView(root)})
 }

 override fun onResume(){
  super.onResume()
  when(prefs.getString("stage", "")){
   "uninstall" -> if(!installed("com.whatsapp")){prefs.edit().putString("stage","install").apply();result.text="✅ تمت إزالة واتساب. الخطوة التالية: تثبيت النسخة الرسمية من Google Play.";result.postDelayed({openPlay()},500)}
   "install" -> if(installed("com.whatsapp")){prefs.edit().remove("stage").apply();result.text="✅ تم العثور على واتساب بعد إعادة التثبيت. جارٍ إعادة الفحص...";result.postDelayed({deepScan(false)},700)}
  }
  if(pendingRecheck){pendingRecheck=false;result.postDelayed({deepScan(false)},500)}
 }

 private fun requestAndScan(){if(Build.VERSION.SDK_INT>=23&&ActivityCompat.checkSelfPermission(this,Manifest.permission.READ_PHONE_STATE)!=PackageManager.PERMISSION_GRANTED)ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.READ_PHONE_STATE),50) else deepScan(true)}
 override fun onRequestPermissionsResult(r:Int,p:Array<out String>,g:IntArray){super.onRequestPermissionsResult(r,p,g);if(r==50)deepScan(true)}
 private fun installed(p:String)=try{packageManager.getPackageInfo(p,0);true}catch(e:Exception){false}
 private fun pkgVersion(p:String):String?=try{packageManager.getPackageInfo(p,0).versionName}catch(e:Exception){null}
 private fun installer(p:String):String?=try{if(Build.VERSION.SDK_INT>=30)packageManager.getInstallSourceInfo(p).installingPackageName else @Suppress("DEPRECATION") packageManager.getInstallerPackageName(p)}catch(e:Exception){null}
 private fun enabled(p:String):Boolean=try{packageManager.getApplicationInfo(p,0).enabled}catch(e:Exception){false}
 private fun isOnline():Boolean{val cm=getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager;@Suppress("DEPRECATION") return cm.activeNetworkInfo?.isConnected==true}
 private fun autoTime():Boolean=try{Settings.Global.getInt(contentResolver,Settings.Global.AUTO_TIME,0)==1}catch(e:Exception){false}
 private fun rooted():Boolean=listOf("/system/bin/su","/system/xbin/su","/sbin/su","/su/bin/su","/data/local/bin/su","/system/app/Superuser.apk").any{java.io.File(it).exists()}||Build.TAGS?.contains("test-keys")==true
 private fun signatureSha256(p:String):String?=try{
  val bytes=if(Build.VERSION.SDK_INT>=28){val pi=packageManager.getPackageInfo(p,PackageManager.GET_SIGNING_CERTIFICATES);pi.signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()}else{@Suppress("DEPRECATION") val pi=packageManager.getPackageInfo(p,PackageManager.GET_SIGNATURES);@Suppress("DEPRECATION") pi.signatures?.firstOrNull()?.toByteArray()}
  bytes?.let{MessageDigest.getInstance("SHA-256").digest(it).joinToString(""){b->"%02X".format(b)}}
 }catch(e:Exception){null}
 private fun conflictingPackages():List<String>{
  val known=listOf("com.whatsapp.w4b","com.gbwhatsapp","com.gbwhatsapp3","com.obwhatsapp","com.yowhatsapp","com.fmwhatsapp","com.whatsapp.plus")
  return known.filter{installed(it)}
 }

 private fun deepScan(autoRepair:Boolean){
  val wa=installed("com.whatsapp");val gms=installed("com.google.android.gms");val play=installed("com.android.vending");val src=if(wa)installer("com.whatsapp") else null;val online=isOnline();val time=autoTime();val root=rooted();val conflicts=conflictingPackages();val sig=if(wa)signatureSha256("com.whatsapp") else null;var problems=0
  val report=buildString{
   append("📋 تقرير مساعد واتساب اليمن — فحص عميق\nتطوير: سامر حوشب\n\n📱 ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\nWhatsApp: ${pkgVersion("com.whatsapp")?:"غير مثبت"}\n")
   if(wa)append("✅ واتساب مثبت ومفعّل: ${enabled("com.whatsapp")}.\n")else{append("❌ واتساب غير مثبت.\n");problems++}
   if(wa&&src=="com.android.vending")append("✅ مصدر التثبيت المسجل: Google Play.\n")else if(wa){append("⚠️ مصدر التثبيت: ${src?:"غير معروف"}.\n");problems++}
   if(sig!=null)append("🔐 SHA-256 لتوقيع الحزمة: $sig\n")else if(wa)append("⚠️ تعذر قراءة بصمة توقيع الحزمة.\n")
   if(gms)append("✅ Google Play Services موجودة.\n")else{append("⚠️ Google Play Services غير ظاهرة.\n");problems++}
   if(play)append("✅ متجر Google Play موجود.\n")else{append("⚠️ متجر Google Play غير ظاهر.\n");problems++}
   if(online)append("✅ اتصال الشبكة نشط.\n")else{append("❌ لا يوجد اتصال شبكة نشط.\n");problems++}
   if(time)append("✅ الوقت التلقائي مفعّل.\n")else{append("⚠️ الوقت التلقائي غير مفعّل.\n");problems++}
   if(root){append("⚠️ مؤشر Root/نظام معدل ظاهر.\n");problems++}else append("✅ لم يظهر مؤشر Root أساسي.\n")
   if(conflicts.isNotEmpty()){append("⚠️ توجد حزم واتساب إضافية/معدلة: ${conflicts.joinToString()}.\n");problems++}else append("✅ لم تظهر حزم واتساب معدلة معروفة.\n")
   if(Build.VERSION.SDK_INT<=24)append("ℹ️ الجهاز يعمل بإصدار Android قديم نسبيًا؛ قد تؤثر سلامة النظام/اعتماد الجهاز على تحقق الخدمات الحديثة.\n")
   append("\n🎯 النتيجة: ")
   append(if(problems==0)"الفحوص المحلية سليمة، لكن هذا لا يثبت قبول خوادم واتساب لسلامة الجهاز أو الحزمة." else "تم اكتشاف $problems نقطة محلية تحتاج معالجة.")
   append("\nإذا كان واتساب نفسه يعرض «يجب تنزيل تطبيق واتساب الرسمي»، استخدم زر المشكلة المخصص ليبدأ مسار الإصلاح الكامل.")
   if(autoRepair)append("\n🔧 جارٍ اختيار الإصلاح المناسب...")
  }
  lastReport=report;result.text=report;if(autoRepair)result.postDelayed({smartRepair()},700)
 }

 private fun smartRepair(){val wa=installed("com.whatsapp");val src=if(wa)installer("com.whatsapp") else null;when{
  !isOnline()->openProtectedFix("إصلاح الشبكة","لا توجد شبكة نشطة. سيتم فتح إعدادات الشبكة. اتصل بالإنترنت ثم ارجع وسيعاد الفحص.",Intent(Settings.ACTION_WIRELESS_SETTINGS))
  !autoTime()->openProtectedFix("إصلاح الوقت","فعّل التاريخ والوقت التلقائيين ثم ارجع وسيعاد الفحص.",Intent(Settings.ACTION_DATE_SETTINGS))
  !wa->{dialog("تثبيت واتساب الرسمي","لم أجد واتساب. سيتم فتح Google Play.");openPlay()}
  src!="com.android.vending"->fullReinstallFlow()
  rooted()->dialog("سلامة النظام تحتاج تدخلك","ظهر مؤشر Root أو نظام معدل. لا يمكن للمساعد إزالة Root بأمان من داخل Android. أعد الجهاز إلى نظام رسمي إذا استمر رفض واتساب.")
  else->AlertDialog.Builder(this).setTitle("الفحص المحلي سليم").setMessage("إذا كان واتساب يعرض رسالة «يجب تنزيل تطبيق واتساب الرسمي» رغم ذلك، اضغط «إصلاح مشكلة الرسمي» لبدء إعادة التثبيت النظيفة ثم إعادة الفحص.").setPositiveButton("إصلاح مشكلة الرسمي"){_,_->officialRejectedFlow()}.setNegativeButton("إرسال التقرير"){_,_->sendReportToSupport()}.show()
 }}

 private fun officialRejectedFlow(){
  AlertDialog.Builder(this).setTitle("🛡️ رفض التحقق من واتساب الرسمي").setMessage("هذه الحالة قد تحدث حتى عندما يظهر أن واتساب من Google Play. سنستخدم المسار الأقوى المتاح على الهاتف: إعدادات واتساب لمسح البيانات إن أردت، ثم إزالة واتساب، إعادة تثبيته من Google Play، وإعادة الفحص. قد تفقد المحادثات غير المنسوخة احتياطيًا.").setPositiveButton("ابدأ الإصلاح الكامل"){_,_->fullReinstallFlow()}.setNegativeButton("إرسال التقرير"){_,_->sendReportToSupport()}.show()
 }
 private fun fullReinstallFlow(){
  if(!installed("com.whatsapp")){prefs.edit().putString("stage","install").apply();openPlay();return}
  AlertDialog.Builder(this).setTitle("الإصلاح الكامل").setMessage("1) يمكن فتح معلومات واتساب لمسح التخزين/البيانات.\n2) بعدها ارجع للمساعد واضغط الإصلاح الكامل مرة أخرى لاختيار إزالة واتساب.\n3) سيقودك المساعد إلى Google Play لإعادة تثبيته ثم يعيد الفحص.\n\nتحذير: مسح البيانات أو الإزالة قد يحذف المحادثات المحلية غير المنسوخة.").setPositiveButton("فتح إعدادات واتساب ومسح البيانات"){_,_->pendingRecheck=true;openWhatsAppSettings()}.setNeutralButton("تجاوز المسح وإزالة واتساب"){_,_->uninstallWhatsApp()}.setNegativeButton("إلغاء",null).show()
 }
 private fun uninstallWhatsApp(){prefs.edit().putString("stage","uninstall").apply();try{startActivity(Intent(Intent.ACTION_DELETE,Uri.parse("package:com.whatsapp")))}catch(e:Exception){prefs.edit().remove("stage").apply();openWhatsAppSettings()}}

 private fun sendReportToSupport(){
  if(lastReport=="لم يتم إجراء فحص بعد."){dialog("أجرِ الفحص أولًا","اضغط الفحص العميق أولًا لإنشاء تقرير حقيقي.");return}
  val msg="السلام عليكم سامر، لدي مشكلة في واتساب. هذا تقرير الفحص من تطبيق مساعد واتساب اليمن:\n\n$lastReport\n\nأحتاج تشخيص المشكلة والحل المناسب."
  val uri=Uri.parse("https://wa.me/$supportWhatsApp?text=${Uri.encode(msg)}")
  try{startActivity(Intent(Intent.ACTION_VIEW,uri).apply{setPackage("com.whatsapp")})}catch(e:Exception){try{startActivity(Intent(Intent.ACTION_VIEW,uri))}catch(x:Exception){dialog("تعذر فتح واتساب","تعذر فتح واتساب لإرسال التقرير.")}}
 }
 private fun openObWhatsAppUpdate(){AlertDialog.Builder(this).setTitle("واتساب عمر العنابي (OBWhatsApp)").setMessage("تطبيق معدل وغير تابع لواتساب أو Meta، وقد يعرّض الحساب للحظر أو مخاطر الخصوصية. سيفتح المساعد صفحة التحديث الخارجية فقط ولن يثبت APK تلقائيًا.").setPositiveButton("فتح صفحة التحديث"){_,_->openUrl(obWhatsAppUpdateUrl)}.setNegativeButton("إلغاء",null).show()}
 private fun openObWhatsAppTelegram(){AlertDialog.Builder(this).setTitle("قناة تحديثات واتساب عمر").setMessage("سيتم فتح قناة التحديثات الخارجية. تحقق من المصدر قبل تثبيت أي ملف.").setPositiveButton("فتح القناة"){_,_->openUrl(obWhatsAppTelegramUrl)}.setNegativeButton("إلغاء",null).show()}
 private fun openUrl(url:String){try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}catch(e:Exception){dialog("تعذر فتح الرابط","لا يوجد تطبيق قادر على فتح الرابط حاليًا.")}}
 private fun openProtectedFix(t:String,m:String,i:Intent){AlertDialog.Builder(this).setTitle(t).setMessage(m).setPositiveButton("ابدأ الإصلاح"){_,_->pendingRecheck=true;safeStart(i)}.setNegativeButton("إلغاء",null).show()}
 private fun dialog(t:String,m:String)=AlertDialog.Builder(this).setTitle(t).setMessage(m).setPositiveButton("حسنًا",null).show()
 private fun showBanHelp()=dialog("الحساب المحظور","إذا كان الحظر صادرًا من خوادم واتساب فلا يمكن فكه من الهاتف. استخدم «طلب مراجعة» داخل واتساب الرسمي عند ظهوره.")
 private fun showCodeHelp()=dialog("رمز التفعيل في اليمن","استخدم +967 بدون صفر إضافي، وتأكد من استقبال SMS والمكالمات ومن الوقت التلقائي وانتظر عداد واتساب. التطبيق لا يقرأ رمز التفعيل ولا يولده.")
 private fun safeStart(i:Intent){try{startActivity(i)}catch(e:Exception){startActivity(Intent(Settings.ACTION_SETTINGS))}}
 private fun openPlay(){try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.whatsapp")))}catch(e:Exception){startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")))}}
 private fun openWhatsAppSettings(){safeStart(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:com.whatsapp")))}
}
