package jp.techacademy.yusuke.shimozono.autoslideshowapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

//以下は追加
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Handler
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.view.View
import android.webkit.PermissionRequest
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import com.google.android.material.snackbar.Snackbar

class MainActivity : AppCompatActivity() {

    private val PERMISSION_REQUEST_CODE = 100
    private var listId = arrayListOf<Long>() //画像のIDの全データを格納するリスト
    private var i:Int = 0 //listIdのインデックス番号を指定する変数
    private var mHandler = Handler()
    private var mTimer: Timer? = null
    private var mCursor: Cursor? = null
    //mはメンバ変数の意味。今回は mCursor を使わなかったが、このようにcursorをメンバ変数で宣言する方法でもOK。

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Android6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                //許可されている
                getContentsInfo()
            } else {
                //許可されていない許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_REQUEST_CODE
                )
            }
            //Android5系以下の場合
        } else {
            getContentsInfo()
        }

        //戻るボタンの動作
        btnBack.setOnClickListener {
            if (i == 0) {
                i = listId.count() - 1
                var imageUri2 =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, listId[i])
                imageView.setImageURI(imageUri2)
            } else {
                i -= 1
                var imageUri2 =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, listId[i])
                imageView.setImageURI(imageUri2)
            }
        }

        //進むボタンの動作
        btnNext.setOnClickListener {
            if (i == listId.count() -1) {
                i = 0
                var imageUri2 =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, listId[i])
                imageView.setImageURI(imageUri2)
            } else {
                i += 1
                var imageUri2 =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, listId[i])
                imageView.setImageURI(imageUri2)
            }
        }

        //再生・停止ボタンの動作
        btnStart.setOnClickListener {
            //再生する場合
            if (btnStart.text == "再生") {
                btnStart.text = "停止"
                btnBack.isEnabled = false
                btnNext.isEnabled = false

            mTimer = Timer()
            mTimer!!.schedule(object : TimerTask() {
                override fun run() {
                    mHandler.post {
                        //画像IDが配列の最後の場合
                        if (i == listId.count() - 1) {
                            i = 0
                            var imageUri2 =
                                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, listId[i])
                            imageView.setImageURI(imageUri2)
                        } else //画像IDが最後以外の場合
                        {
                            i += 1
                            var imageUri2 =
                                ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, listId[i])
                            imageView.setImageURI(imageUri2)
                        }
                    }
                }
            }, 2000, 2000)

            //停止する場合
            } else {
                btnStart.text = "再生"
                btnBack.isEnabled = true
                btnNext.isEnabled = true
                mTimer!!.cancel()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE ->
                //画像フォルダへのアクセスが許可されている場合
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()
                } else //画像フォルダへのアクセスが許可されていない場合
                {
                    btnStart.isEnabled = false
                    btnBack.isEnabled = false
                    btnNext.isEnabled = false
                    textView.text = "「端末内部の写真、メディア、ファイルへのアクセス」を許可してください"
                }
        }
    }

    private fun getContentsInfo() {
        //画像の情報を取得する
        var cursor = contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, //データの種類
            null,//項目 (null = 全項目)
            null,//フィルタ条件 (null = フィルタなし)
            null,//フィルタ用パラメータ
            null//ソート (nullソートなし)
        )

        if (cursor!!.moveToFirst()) {
            do {
                //indexからIDを取得し、そのIDから画像のURIを取得する
                var fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                var id = cursor.getLong(fieldIndex)
                var imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                Log.d(
                    "check99",
                    "cursor:$cursor, fieldIndex:$fieldIndex, id:$id, imageUri:$imageUri"
                )

                imageView.setImageURI(imageUri)
                listId.add(id) //画像IDを配列listIdへ格納する
            } while (cursor.moveToNext())
        }
        cursor.close()
        Log.d("check99", "listIdの要素数は${listId.count()}")
    }
}
