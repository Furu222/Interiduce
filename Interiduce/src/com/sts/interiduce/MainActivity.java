/**
 * MainActivity
 * トップページから画面投稿
 * @author furudate
 * @version 1.0
 */

package com.sts.interiduce;

import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.media.MediaScannerConnection;
import android.media.MediaScannerConnection.OnScanCompletedListener;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends Activity {

	static final int REQUEST_CODE_CAMERA = 1; /* カメラを判定するコード */
	static final int REQUEST_CODE_GALLERY = 2; /* ギャラリーを判定するコード */
	
	private Bitmap bm;
	private Uri bitmapUri;
	


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		Button btn = (Button)findViewById(R.id.button1);
		btn.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO 自動生成されたメソッド・スタブ
				Intent intent = new Intent(MainActivity.this, PhotoUploadActivity.class);
				startActivity(intent);
			}
		});
	}

	/**
	 * メニューの作成
	 * 
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// menu/activity_main.xmlからメニューを作成
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
	
	/**
	 * メニューが選択されたときの処理
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item){
		switch (item.getItemId()){ // if使うとエラー（itemがInt形式なため）
			case android.R.id.home:
				// アプリアイコン（ホームアイコン）を押した時の処理
//				Intent intent = new Intent(this, MainActivity.class);
//				intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//				startActivity(intent);
				Toast.makeText(this, "home", Toast.LENGTH_SHORT).show();
				break;
			case R.id.menu_upload:
				// アップロードボタンが押された時
				String[] str_items = {"カメラで撮影", "ギャラリーの選択", "キャンセル"};
				new AlertDialog.Builder(this)
				.setTitle("写真をアップロード")
				.setItems(str_items, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO 自動生成されたメソッド・スタブ
						switch(which){
						case 0:
							wakeupCamera(); // カメラ起動
							break;
						case 1:
							wakeupGallery(); // ギャラリー起動
							break;
						default:
							// キャンセルを選んだ場合
							break;
						}
					}
				}).show();
				
				break;
			case R.id.menu_settings:
				// 設定ボタンを押した時
				Toast.makeText(this, "Setting", Toast.LENGTH_SHORT).show();
				break;
		}
		return true;
	}
	
	/**
	 * カメラ起動後
	 * 
	 * Environment.getExternalStoragePublicDirectoryでピクチャーのパスを取得し、書き込む
	 */
	protected void wakeupCamera(){
		File mediaStorageDir = new File(
			Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES
			), "PictureSaveDir"
		);
		if (! mediaStorageDir.exists() & ! mediaStorageDir.mkdir()){
			return;
		}
		String timeStamp = new SimpleDateFormat("yyyMMddHHmmss").format(new Date());
		File mediaFile;
		mediaFile = new File(mediaStorageDir.getPath() + File.separator + timeStamp + ".JPG");
	    Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
	    bitmapUri = Uri.fromFile(mediaFile);
	    i.putExtra(MediaStore.EXTRA_OUTPUT, bitmapUri); // 画像をmediaUriに書き込み
	    startActivityForResult(i, REQUEST_CODE_CAMERA);
	}
	
	
	/**
	 * ギャラリー起動
	 */
	protected void wakeupGallery(){
		Intent i = new Intent();
		i.setType("image/*"); // 画像のみが表示されるようにフィルターをかける
		i.setAction(Intent.ACTION_GET_CONTENT); // 出0他を取得するアプリをすべて開く
		startActivityForResult(i, REQUEST_CODE_GALLERY);
	}
	
	
	/**
	 * カメラまたはギャラリーから戻ってきた時の処理（requestCodeによって分岐)
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if (resultCode == RESULT_OK){
			if (bm != null)
				bm.recycle(); // 直前のBitmapが読み込まれていたら開放する
			
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inSampleSize = 4; // 元の1/4サイズでbitmap取得
			
			switch(requestCode){
			case 1: // カメラの場合
				bm = BitmapFactory.decodeFile(bitmapUri.getPath(), options);
				// 撮影した画像をギャラリーのインデックスに追加されるようにスキャンする。
				// これをやらないと、アプリ起動中に撮った写真が反映されない
				String[] paths = {bitmapUri.getPath()};
				String[] mimeTypes = {"image/*"};
				MediaScannerConnection.scanFile(getApplicationContext(), paths, mimeTypes, new OnScanCompletedListener(){
					@Override
					public void onScanCompleted(String path, Uri uri){
					}
				});
				break;
			case 2: // ギャラリーの場合
				try{
					ContentResolver cr = getContentResolver();
					String[] columns = {MediaStore.Images.Media.DATA};
					Cursor c = cr.query(data.getData(), columns, null, null, null);
					c.moveToFirst();
					bitmapUri = Uri.fromFile(new File(c.getString(0)));
					InputStream is = getContentResolver().openInputStream(data.getData());
					bm = BitmapFactory.decodeStream(is, null, options);
					is.close();
				}catch(Exception e){
					e.printStackTrace();
				}
				break;
			}
		}
	}
	

}
