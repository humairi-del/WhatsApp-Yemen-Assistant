package com.samer.whatsappyemenassistant

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
 private lateinit var result: TextView
 private fun button(t:String, action:()->Unit)=Button(this).apply{text=t;setOnClickListener{action()}}
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL;setPadding(28,42,28,36)}
  root.addView(TextView(this).apply{text="🇾🇪 مساعد واتساب اليمن";textSize=27f;gravity=Gravity.CENTER})
  root.addView(TextView(this).apply{text="تطوير: سامر حوشب | Samer Hoshab";textSize=15f;gravity=Gravity.CENTER;setPadding(0,6,0,20)})
  root.addView(TextView(this).apply{text="مركز تشخيص واستعادة واتساب — بدون قراءة المحادثات أو رموز التحقق";textSize=16f;gravity=Gravity.CENTER;setPadding(0,0,0,18)})
  root.addView(button("🔍 الفحص الشامل"){diagnose()},LinearLayout.LayoutParams(-1,-2))
  result=TextView(this).apply{text="ابدأ بالفحص الشامل، ثم اختر المشكلة من الأدوات أدناه.";textSize=16f;setPadding(8,20,8,20)};root.addView(result)
  root.addView(button("🚫 واتساب محظور / لا يمكن استخدام هذا الحساب"){showBanHelp()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🛡️ تظهر رسالة: استخدم واتساب الرسمي"){showOfficialHelp()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("📩 رمز التفعيل لا يصل"){showCodeHelp()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("📱 فحص توافق الجهاز القديم"){showCompatibility()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("▶️ فتح واتساب الرسمي"){openWhatsApp()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🛒 صفحة واتساب في Google Play"){openPlay()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("⚙️ إعدادات واتساب"){openWhatsAppSettings()},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("🛡️ إعدادات الأمان"){startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))},LinearLayout.LayoutParams(-1,-2))
  root.addView(button("📤 مشاركة تقرير الفحص"){shareReport()},LinearLayout.LayoutParams(-1,-2))
  root.addView(TextView(this).apply{text="مهم: لا يستطيع أي تطبيق فك حظر فرضته واتساب أو تجاوز التحقق. هذه الأداة تحدد السبب وتوصلك لمسار الاستعادة الرسمي.";textSize=14f;gravity=Gravity.CENTER;setPadding(4,22,4,8)})
  setContentView(ScrollView(this).apply{addView(root)})
 }
 private fun installed(p:String)=try{packageManager.getPackageInfo(p,0);true}catch(e:Exception){false}
 private fun installer(p:String):String?=try{if(Build.VERSION.SDK_INT>=30)packageManager.getInstallSourceInfo(p).installingPackageName else @Suppress("DEPRECATION") packageManager.getInstallerPackageName(p)}catch(e:Exception){null}
 private fun diagnose(){
  val wa=installed("com.whatsapp");val gms=installed("com.google.android.gms");val play=installed("com.android.vending");val src=if(wa)installer("com.whatsapp") else null
  val rooted=listOf("/system/bin/su","/system/xbin/su","/sbin/su","/su/bin/su").any{java.io.File(it).exists()}
  result.text=buildString{
   append("📋 نتيجة الفحص الشامل\n\nAndroid: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
   append(if(Build.VERSION.SDK_INT<23)"⚠️ أندرويد قديم جدًا لهذا الإصدار من المساعد.\n" else "✅ نظام أندرويد متوافق مع المساعد.\n")
   append(if(wa)"✅ واتساب مثبت.\n" else "❌ واتساب غير مثبت.\n")
   if(wa){append("مصدر التثبيت: ${src?:"غير معروف"}\n");append(if(src=="com.android.vending")"✅ التثبيت صادر من Google Play.\n" else "⚠️ المصدر ليس Google Play أو تعذر التحقق منه.\n")}
   append(if(gms)"✅ خدمات Google Play موجودة.\n" else "⚠️ خدمات Google Play غير موجودة/غير مرئية.\n")
   append(if(play)"✅ متجر Google Play موجود.\n" else "⚠️ متجر Google Play غير موجود/غير مرئي.\n")
   append(if(rooted)"⚠️ وُجد مؤشر SU/Root؛ قد يؤثر في سلامة الجهاز أو التحقق.\n" else "✅ لم يظهر مؤشر Root بسيط في المسارات المعروفة.\n")
   append("\n🔎 إذا استمرت رسالة التطبيق الرسمي رغم أن المصدر Google Play، فافحص اعتماد Play Protect، النسخ/الاستنساخ، النظام المعدل، ثم أعد تثبيت النسخة الرسمية بعد حفظ النسخة الاحتياطية.\n")
   append("\n🚫 الحظر المرتبط بالحساب لا يمكن إزالته من الهاتف؛ استخدم طلب المراجعة داخل واتساب أو دعم واتساب الرسمي.\n")
   append("\n📩 للتفعيل في اليمن: استخدم +967 بدون صفر إضافي، وتأكد من استقبال SMS والمكالمات ومن الوقت والتاريخ التلقائي، ولا تكرر طلب الرمز بسرعة.")
  }
 }
 private fun dialog(title:String,msg:String)=AlertDialog.Builder(this).setTitle(title).setMessage(msg).setPositiveButton("حسنًا",null).show()
 private fun showBanHelp()=dialog("استعادة الحساب المحظور","إذا ظهرت «لا يمكن لهذا الحساب استخدام واتساب» أو رسالة حظر:\n\n1) افتح واتساب الرسمي وابحث عن «طلب مراجعة».\n2) أرسل طلب المراجعة من نفس الرقم.\n3) أزل التطبيقات المعدلة أو أدوات الأتمتة/الإرسال المزعج إن وجدت.\n4) لا تحاول تغيير هوية الجهاز أو تجاوز الحظر؛ الحظر من خوادم واتساب ولا يستطيع هذا التطبيق فكه.\n5) إذا لم يظهر طلب المراجعة، استخدم قناة دعم واتساب الرسمية.")
 private fun showOfficialHelp()=dialog("مشكلة واتساب الرسمي","نفّذ بالترتيب:\n\n1) تأكد أن مصدر التثبيت Google Play.\n2) حدّث متجر Google Play وخدمات Google Play.\n3) افتح Play Protect وتحقق من اعتماد الجهاز.\n4) أوقف نسخ التطبيقات/الاستنساخ لواتساب مؤقتًا.\n5) إذا كان الجهاز Root أو ROM معدلًا فقد تفشل فحوص السلامة.\n6) احفظ النسخة الاحتياطية ثم أعد تثبيت واتساب الرسمي عند الحاجة.\n\nالتطبيق لا يتجاوز فحوص واتساب أو Google.")
 private fun showCodeHelp()=dialog("رمز التفعيل","1) اختر اليمن +967 واكتب الرقم بدون صفر إضافي في البداية.\n2) تأكد أن الشريحة نفسها تستقبل SMS والمكالمات.\n3) فعّل التاريخ والوقت التلقائي.\n4) انتظر انتهاء العداد داخل واتساب قبل إعادة المحاولة.\n5) استخدم خيار المكالمة عندما يتيحه واتساب.\n6) لا تشارك رمز التفعيل مع أي شخص أو تطبيق.")
 private fun showCompatibility(){val msg="جهازك: Android ${Build.VERSION.RELEASE} — API ${Build.VERSION.SDK_INT}.\n\nهذا المساعد يعمل من Android 6 (API 23). توافق واتساب نفسه قد يتغير، لذلك يجب الاعتماد على متطلبات واتساب الرسمية الحالية.";dialog("توافق الجهاز",msg)}
 private fun openWhatsApp(){packageManager.getLaunchIntentForPackage("com.whatsapp")?.let{startActivity(it)}?:openPlay()}
 private fun openPlay(){try{startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("market://details?id=com.whatsapp")))}catch(e:Exception){startActivity(Intent(Intent.ACTION_VIEW,Uri.parse("https://play.google.com/store/apps/details?id=com.whatsapp")))}}
 private fun openWhatsAppSettings(){try{startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:com.whatsapp")))}catch(e:Exception){startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))}}
 private fun shareReport(){val text=result.text.toString();startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply{type="text/plain";putExtra(Intent.EXTRA_SUBJECT,"تقرير مساعد واتساب اليمن");putExtra(Intent.EXTRA_TEXT,text)},"مشاركة التقرير"))}
}
