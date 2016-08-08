package com.movelayoutdemo;

import com.zjl.customview.MoveLayout;
import com.zjl.customview.MoveLayout.OnMovingListener;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * @author ZhengJingle
 */
public class MainActivity extends Activity {
	MoveLayout ml_main;
	ImageView iv_active;
	Button button1;
	TextView tv,tv_x,tv_y;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		ml_main=(MoveLayout)findViewById(R.id.ml_main);
		
		iv_active=(ImageView)findViewById(R.id.iv_active);
		tv=(TextView)findViewById(R.id.textView1);
		tv_x=(TextView)findViewById(R.id.tv_x);
		tv_y=(TextView)findViewById(R.id.tv_y);
		button1=(Button)findViewById(R.id.button1);
		button1.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				ml_main.moveToPrecent(1.0);//移动到百分比位置
			}
			
		});
		
		/* 
		 * 被动移动views
		 * 
		 * 如果你的被动移动view想实现复杂的移动，例如曲线移动，就不要在这里设置。
		 * 可以在下面OnMovingListener中的onMoving(double movingPrecent)里自己写代码实现。
		 */
		ml_main.setPassiveMoveItem(button1, 50, 500);
		ml_main.setPassiveMoveItem(tv_x, 500, 0);
		ml_main.setPassiveMoveItem(tv_y, 0, 800);
		
		//主动移动view
		ml_main.setActiveMoveItem(iv_active, 0, 0, false);
		
		//移动区域
		ml_main.setActiveMoveArea(false, 100, 400, 200, 800);
		
		//移动中监听
		ml_main.setOnMovingListener(new OnMovingListener(){

			@Override
			public void onMoving(double movingPrecent) {
				// TODO 自动生成的方法存根
				tv.setText(movingPrecent*100+"%");
			}
			
		});
		
	}
	
}
