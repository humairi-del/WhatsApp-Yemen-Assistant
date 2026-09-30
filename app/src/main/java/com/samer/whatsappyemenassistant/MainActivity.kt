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
import java.io.File
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
  result=TextView(this).apply{text="يفحص واتساب والجهاز ومصدر التثبيت والتوقيع وسلامة النظام والتعارضات والشبكة والوقت، ثم يختار مسار الإصلاح المناسب.";textSize=16f;setPadding(8,20,8,20)};root.addView(result)
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
  root.addView(TextView(this).apply{text="مهم: مسح البيانات أو حذف واتساب قد يفقد المحادثات غير المنسوخة احتياطيًا. المساعد لا يتجاوز تحقق خوادم واتساب ولا يزيل Root تلقائيًا. واتساب عمر تطبيق معدل غير تابع لواتساب أو Meta وقد يحمل مخاطر حظر أو خصوصية.";textSize=14f;gravity=Gravity.CENTER;setPadding(4,22,4,8)})
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

 private fun rootEvidence():List<String>{
  val evidence=mutableListOf<String>()
  val paths=listOf("/system/bin/su","/system/xbin/su","/sbin/su","/su/bin/su","/data/local/bin/su","/data/local/xbin/su","/system/app/Superuser.apk","/system/app/SuperSU.apk","/system/xbin/daemonsu","/sbin/.magisk","/data/adb/magisk")
  paths.filter{File(it).exists()}.forEach{evidence.add("ملف Root: $it")}
  if(Build.TAGS?.contains("test-keys",true)==true)evidence.add("Build tags: test-keys")
  val rootApps=listOf("com.topjohnwu.magisk","eu.chainfire.supersu","com.noshufou.android.su","com.koushikdutta.superuser","com.kingroot.kinguser","com.kingo.root")
  rootApps.filter{installed(it)}.forEach{evidence.add("تطبيق إدارة Root: $it")}
  return evidence.distinct()
 }
 private fun systemIntegrityStatus():Pair<String,List<String>>{
  val e=rootEvidence()
  return when{
   e.isNotEmpty()->"ROOT_INDICATORS" to e
   Build.TAGS.isNullOrBlank()->"UNKNOWN" to listOf("تعذر تأكيد سلامة النظام محليًا")
   else->"NO_LOCAL_INDICATOR" to emptyList()
  }
 }
 private fun signatureSha256(p:String):String?=try{
  val bytes=if(Build.VERSION.SDK_INT>=28){val pi=packageManager.getPackageInfo(p,PackageManager.GET_SIGNING_CERTIFICATES);pi.signingInfo?.apkContentsSigners?.firstOrNull()?.toByteArray()}else{@Suppress("DEPRECATION") val pi=packageManager.getPackageInfo(p,PackageManager.GET_SIGNATURES);@Suppress("DEPRECATION") pi.signatures?.firstOrNull()?.toByteArray()}
  bytes?.let{MessageDigest.getInstance("SHA-256").digest(it).joinToString(""){b->"%02X".format(b)}}
 }catch(e:Exception){null}
 private fun modifiedWhatsAppPackages():List<String>{
  val known=listOf("com.gbwhatsapp","com.gbwhatsapp3","com.obwhatsapp","com.yowhatsapp","com.fmwhatsapp","com.whatsapp.plus")
  return known.filter{installed(it)}
 }

 private fun deepScan(autoRepair:Boolean){
  val wa=installed("com.whatsapp");val business=installed("com.whatsapp.w4b");val gms=installed("com.google.android.gms");val play=installed("com.android.vending");val src=if(wa)installer("com.whatsapp") else null;val online=isOnline();val time=autoTime();val integrity=systemIntegrityStatus();val mods=modifiedWhatsAppPackages();val sig=if(wa)signatureSha256("com.whatsapp") else null;var problems=0
  val report=buildString{
   append("📋 تقرير مساعد واتساب اليمن — فحص عميق\nتطوير: سامر حوشب\n\n📱 ${Build.MANUFACTURER} ${Build.MODEL}\nAndroid ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\nWhatsApp: ${pkgVersion("com.whatsapp")?:"غير مثبت"}\n")
   if(wa)append("✅ واتساب مثبت ومفعّل: ${enabled("com.whatsapp")}.\n")else{append("❌ واتساب غير مثبت.\n");problems++}
   if(wa&&src=="com.android.vending")append("✅ مصدر التثبيت المسجل: Google Play.\n")else if(wa){append("⚠️ مصدر التثبيت: ${src?:"غير معروف"}.\n");problems++}
   if(sig!=null)append("🔐 SHA-256 لتوقيع الحزمة: $sig\n")else if(wa)append("⚠️ تعذر قراءة بصمة توقيع الحزمة.\n")
   if(business)append("ℹ️ WhatsApp Business الرسمي مثبت أيضًا؛ لا يُصنّف كنسخة معدلة.\n")
   if(gms)append("✅ Google Play Services موجودة.\n")else{append("⚠️ Google Play Services غير ظاهرة.\n");problems++}
   if(play)append("✅ متجر Google Play موجود.\n")else{append("⚠️ متجر Google Play غير ظاهر.\n");problems++}
   if(online)append("✅ اتصال الشبكة نشط.\n")else{append("❌ لا يوجد اتصال شبكة نشط.\n");problems++}
   if(time)append("✅ الوقت التلقائي مفعّل.\n")else{append("⚠️ الوقت التلقائي غير مفعّل.\n");problems++}
   when(integrity.first){
    "ROOT_INDICATORS"->{append("⚠️ ظهرت مؤشرات Root/تعديل نظام محلية:\n");integrity.second.forEach{append(" • $it\n")};problems++}
    "UNKNOWN"->{append("⚠️ لم يتمكن الفحص المحلي من تأكيد سلامة النظام.\n");problems++}
    else->append("ℹ️ لم تظهر مؤشرات Root معروفة في الفحص المحلي. هذا لا يثبت أن الجهاز معتمد أو غير معدل.\n")
   }
   if(mods.isNotEmpty()){append("⚠️ توجد حزم واتساب معدلة معروفة: ${mods.joinToString()}.\n");problems++}else append("✅ لم تظهر حزم واتساب معدلة معروفة.\n")
   if(Build.VERSION.SDK_INT<=24)append("ℹ️ Android قديم نسبيًا؛ قد تؤثر سلامة النظام أو اعتماد الجهاز على تحقق الخدمات الحديثة.\n")
   append("\n🎯 النتيجة: ")
   append(if(problems==0)"لم يظهر خلل محلي واضح، لكن قبول الجهاز والحزمة يظل قرارًا من خدمات واتساب/Google." else "تم اكتشاف $problems نقطة محلية تحتاج مراجعة.")
   if(autoRepair)append("\n🔧 جارٍ اختيار الإصلاح المناسب...")
  }
  lastReport=report;result.text=report;if(autoRepair)result.postDelayed({smartRepair()},700)
 }

 private fun smartRepair(){val wa=installed("com.whatsapp");val src=if(wa)installer("com.whatsapp") else null;when{
  !isOnline()->openProtectedFix("إصلاح الشبكة","لا توجد شبكة نشطة. سيتم فتح إعدادات الشبكة. اتصل بالإنترنت ثم ارجع وسيعاد الفحص.",Intent(Settings.ACTION_WIRELESS_SETTINGS))
  !autoTime()->openProtectedFix("إصلاح الوقت","فعّل التاريخ والوقت التلقائيين ثم ارجع وسيعاد الفحص.",Intent(Settings.ACTION_DATE_SETTINGS))
  !wa->{dialog("تثبيت واتساب الرسمي","لم أجد واتساب. سيتم فتح Google Play.");openPlay()}
  src!="com.android.vending"->fullReinstallFlow()
  systemIntegrityStatus().first=="ROOT_INDICATORS"->dialog("مؤشرات Root/تعديل نظام","اكتشف المساعد مؤشرات محلية لروت أو تعديل النظام. إعادة تثبيت واتساب وحدها قد لا تحل رفض التحقق. لا يحاول المساعد إزالة Root تلقائيًا لأن ذلك يعتمد على طريقة تعديل الجهاز.")
  else->AlertDialog.Builder(this).setTitle("الفحص المحلي لم يجد سببًا مباشرًا").setMessage("إذا استمرت رسالة «يجب تنزيل تطبيق واتساب الرسمي»، ابدأ مسار إعادة التثبيت النظيفة ثم أعد الفحص.").setPositiveButton("إصلاح مشكلة الرسمي"){_,_->officialRejectedFlow()}.setNegativeButton("إرسال التقرير"){_,_->sendReportToSupport()}.show()
 }}

 private fun officialRejectedFlow(){AlertDialog.Builder(this).setTitle("🛡️ رفض التحقق من واتساب الرسمي").setMessage("سنبدأ مسارًا نظيفًا: فتح إعدادات واتساب، ثم الإزالة وإعادة التثبيت من Google Play وإعادة الفحص. قد تُحذف البيانات المحلية.").setPositiveButton("ابدأ الإصلاح الكامل"){_,_->fullReinstallFlow()}.setNegativeButton("إلغاء",null).show()}
 private fun fullReinstallFlow(){
  if(!installed("com.whatsapp")){prefs.edit().putString("stage","install").apply();dialog("تثبيت واتساب الرسمي","واتساب غير مثبت. سيتم فتح Google Play.");openPlay();return}
  AlertDialog.Builder(this).setTitle("الإصلاح الكامل").setMessage("سيتم فتح معلومات تطبيق واتساب. يمكنك مسح التخزين/البيانات ثم الرجوع للمساعد لمتابعة الإزالة وإعادة التثبيت. قد تفقد المحادثات غير المنسوخة احتياطيًا.").setPositiveButton("فتح إعدادات واتساب"){_,_->prefs.edit().putString("stage","uninstall").apply();openWhatsAppSettings()}.setNegativeButton("إزالة واتساب الآن"){_,_->startUninstall()}.show()
 }
 private fun startUninstall(){prefs.edit().putString("stage","uninstall").apply();try{startActivity(Intent(Intent.ACTION_DELETE,Uri.parse("package:com.whatsapp")))}catch(e:Exception){openWhatsAppSettings()}}
 private fun sendReportToSupport(){if(lastReport=="لم يتم إجراء فحص بعد."){dialog("أجرِ الفحص أولًا","نفّذ الفحص لإنشاء تقرير حقيقي للجهاز.");return};val msg="السلام عليكم سامر، لدي مشكلة في واتساب. هذا تقرير الفحص من تطبيق مساعد واتساب اليمن:\n\n$lastReport\n\nأحتاج تشخيص المشكلة والحل المناسب.";openUrl("https://wa.me/$supportWhatsApp?text=${Uri.encode(msg)}")}
 private fun openObWhatsAppUpdate(){AlertDialog.Builder(this).setTitle("واتساب عمر العنابي (OBWhatsApp)").setMessage("هذا تطبيق معدل وغير تابع لواتساب أو Meta وقد يحمل مخاطر حظر أو خصوصية. سيتم فتح صفحة التحديث الخارجية فقط.").setPositiveButton("فتح صفحة التحديث"){_,_->openUrl(obWhatsAppUpdateUrl)}.setNegativeButton("إلغاء",null).show()}
 private fun openObWhatsAppTelegram(){AlertDialog.Builder(this).setTitle("قناة تحديثات واتساب عمر").setMessage("سيتم فتح قناة التحديثات الخارجية.").setPositiveButton("فتح القناة"){_,_->openUrl(obWhatsAppTelegramUrl)}.setNegativeButton("إلغاء",null).show()}
 private fun openUrl(url:String){try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(url)))}catch(e:Exception){dialog("تعذر فتح الرابط","لا يوجد تطبيق قادر على فتح الرابط حاليًا.")}}
 private fun openProtectedFix(t:String,m:String,i:Intent){AlertDialog.Builder(this).setTitle(t).setMessage(m).setPositiveButton("ابدأ الإصلاح"){_,_->pendingRecheck=true;safeStart(i)}.setNegativeButton("إلغاء",null).show()}
 private fun dialog(t:String,m:String)=AlertDialog.Builder(this).setTitle(t).setMessage(m).setPositiveButton("حسنًا",null).show()
 private fun showBanHelp()=dialog("الحساب المحظور","إذا كان الحظر صادرًا من خوادم واتساب فلا يمكن فكه من الهاتف. استخدم طلب المراجعة داخل واتساب الرسمي عند ظهوره.")
 private fun showCodeHelp()=dialog("رمز التفعيل لا يصل","تأكد من الرقم ومفتاح الدولة والشبكة، ثم استخدم SMS أو المكالمة عند إتاحتها. لا يقرأ المساعد رمز التفعيل ولا يتجاوزه.")
 private fun openWhatsAppSettings(){safeStart(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:com.whatsapp")))}
 private fun openPlay(){try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.whatsapp")))}catch(e:Exception){openUrl("https://play.google.com/store/apps/details?id=com.whatsapp")}}
 private fun safeStart(i:Intent){try{startActivity(i)}catch(e:Exception){dialog("تعذر فتح الشاشة","تعذر فتح شاشة النظام المطلوبة على هذا الجهاز.")}}
}