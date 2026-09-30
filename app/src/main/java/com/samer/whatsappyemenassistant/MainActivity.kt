package com.samer.whatsappyemenassistant

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
 private lateinit var result: TextView
 override fun onCreate(savedInstanceState: Bundle?) {
  super.onCreate(savedInstanceState)
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL;setPadding(32,48,32,32)}
  root.addView(TextView(this).apply{text="🇾🇪 مساعد واتساب اليمن";textSize=27f;gravity=Gravity.CENTER})
  root.addView(TextView(this).apply{text="تطوير: سامر حوشب | Samer Hoshab";textSize=15f;gravity=Gravity.CENTER;setPadding(0,8,0,28)})
  root.addView(TextView(this).apply{text="تشخيص آمن لمشاكل واتساب. لا يقرأ محادثاتك أو رموز التحقق.";textSize=16f;gravity=Gravity.CENTER;setPadding(0,0,0,24)})
  root.addView(Button(this).apply{text="🔍 فحص الهاتف";setOnClickListener{diagnose()}},LinearLayout.LayoutParams(-1,-2))
  result=TextView(this).apply{text="اضغط فحص الهاتف لبدء التشخيص.";textSize=16f;setPadding(8,24,8,24)};root.addView(result)
  root.addView(Button(this).apply{text="⚙️ إعدادات التطبيقات";setOnClickListener{startActivity(Intent(Settings.ACTION_APPLICATION_SETTINGS))}},LinearLayout.LayoutParams(-1,-2))
  root.addView(Button(this).apply{text="🛡️ إعدادات الأمان";setOnClickListener{startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))}},LinearLayout.LayoutParams(-1,-2))
  setContentView(ScrollView(this).apply{addView(root)})
 }
 private fun installed(p:String)=try{packageManager.getPackageInfo(p,0);true}catch(e:Exception){false}
 private fun installer(p:String):String?=try{if(Build.VERSION.SDK_INT>=30)packageManager.getInstallSourceInfo(p).installingPackageName else @Suppress("DEPRECATION") packageManager.getInstallerPackageName(p)}catch(e:Exception){null}
 private fun diagnose(){
  val wa=installed("com.whatsapp");val gms=installed("com.google.android.gms");val play=installed("com.android.vending");val src=if(wa)installer("com.whatsapp") else null
  result.text=buildString{
   append("نتيجة الفحص\n\nAndroid: ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})\n")
   append(if(Build.VERSION.SDK_INT<23)"⚠️ إصدار أندرويد قديم.\n" else "✅ إصدار أندرويد مناسب للتطبيق المساعد.\n")
   append(if(wa)"✅ واتساب مثبت.\n" else "❌ واتساب غير مثبت.\n");if(wa)append("مصدر التثبيت: ${src?:"غير معروف"}\n")
   append(if(gms)"✅ خدمات Google Play موجودة.\n" else "⚠️ خدمات Google Play غير موجودة/غير مرئية.\n")
   append(if(play)"✅ متجر Google Play موجود.\n" else "⚠️ متجر Google Play غير موجود/غير مرئي.\n")
   append("\nإذا كان واتساب رسميًا وتظهر رسالة «تحتاج إلى تطبيق واتساب الرسمي»، افحص اعتماد الجهاز وPlay Protect والنظام/Root والاستنساخ.\n")
   append("\nإذا لم يصل رمز التفعيل: تأكد من +967 والشريحة واستقبال SMS/المكالمات والوقت التلقائي.\n\nلا يتجاوز التطبيق تحقق واتساب ولا يولد رموز تفعيل.")
  }
 }
}
